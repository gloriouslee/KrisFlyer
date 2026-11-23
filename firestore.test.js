const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function sampleProfile(uid, ts) {
  return {
    userId: uid,
    memberName: "ALICE TAN",
    membershipNumber: "880 492 7150",
    krisFlyerMiles: 38450,
    eliteMiles: 18200,
    ppsValueSgd: 4800,
    expiringMiles: 3200,
    expiringDateLabel: "31 MAR 2027",
    languageCode: "en",
    darkMode: "SYSTEM",
    createdAt: ts,
    updatedAt: ts,
  };
}

function sampleTx(uid, ts) {
  return {
    userId: uid,
    titleVi: "SQ178 • SGN → SIN",
    titleEn: "SQ178 • SGN → SIN",
    subtitleVi: "Thương gia (Z)",
    subtitleEn: "Business Class (Z)",
    category: "FLIGHT",
    krisFlyerMilesDelta: 1275,
    eliteMilesDelta: 1020,
    ppsValueDeltaSgd: 1150,
    badgeCode: "SQ178",
    createdAt: ts,
  };
}

function sampleBooking(uid, ts, status = "CONFIRMED") {
  return {
    userId: uid,
    pnr: "SQ8K4M",
    flightNumber: "SQ178",
    originCode: "SGN",
    destinationCode: "SIN",
    originCity: "Ho Chi Minh City",
    destinationCity: "Singapore (Changi)",
    cabinLabel: "Business",
    awardTypeLabel: "Saver Award",
    milesCost: 24000,
    taxesSgd: 64,
    seatNumber: "11A",
    departureDateLabel: "28 NOV 2026",
    departureTimeLabel: "12:15",
    gate: "G07",
    status,
    roundTrip: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

test("Unauthenticated user cannot read or write profile, transactions, or bookings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(sampleProfile(ALICE_UID, ts)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("bookings").get());
});

test("Owner can create and partially update profile; ghost fields and immutable mutations fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(sampleProfile(ALICE_UID, created))
  );

  // Partial update on balances & preferences succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      krisFlyerMiles: 45000,
      languageCode: "vi",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Immutable createdAt cannot change
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Shadow update / ghost field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user isolation blocks Bob from reading or modifying Alice's data", async () => {
  const ts = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).set(sampleProfile(ALICE_UID, ts));
    await db.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set(sampleTx(ALICE_UID, ts));
    await db.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_1").set(sampleBooking(ALICE_UID, ts));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ krisFlyerMiles: 999999, updatedAt: new Date(Date.now() - 1000) }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("transactions").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("bookings").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_1").update({ status: "CANCELLED", updatedAt: new Date(Date.now() - 1000) }));
});

test("Transactions are append-only and Bookings lock after terminal status", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set(sampleTx(ALICE_UID, created))
  );
  // Transaction updates are blocked
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").update({ krisFlyerMilesDelta: 999999 })
  );

  // Booking lifecycle: CONFIRMED -> CHECKED_IN -> COMPLETED -> locked!
  const bkRef = aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_1");
  await assertSucceeds(bkRef.set(sampleBooking(ALICE_UID, created, "CONFIRMED")));
  await assertSucceeds(bkRef.update({ status: "CHECKED_IN", updatedAt: new Date(Date.now() - 2000) }));
  await assertSucceeds(bkRef.update({ status: "COMPLETED", updatedAt: new Date(Date.now() - 1000) }));

  // Terminal state lock rejects further status change
  await assertFails(bkRef.update({ status: "CANCELLED", updatedAt: new Date(Date.now() - 500) }));

  // Future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_future").set(
      sampleBooking(ALICE_UID, new Date(Date.now() + 60000), "CONFIRMED")
    )
  );
});
