package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.remote.AwardBookingDoc
import com.example.data.remote.KrisFlyerProfileDoc
import com.example.data.remote.MileageTransactionDoc
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.example.domain.AirlineCarrier
import com.example.domain.Airport
import com.example.domain.AwardCabin
import com.example.domain.AwardType
import com.example.domain.CabinFareClass
import com.example.domain.KrisFlyerCalculator
import com.example.domain.KrisFlyerNetwork
import com.example.domain.KrisFlyerTier
import com.example.domain.determineTier
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FirestoreKrisFlyerRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUid: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        if (expectedUid != null) {
            check(uid == expectedUid) { "Signed-in user changed; refusing cross-user operation." }
        }
        return uid
    }

    fun observeProfile(userId: String): Flow<KrisFlyerProfileDoc?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            KrisFlyerProfileDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeTransactions(userId: String): Flow<List<MileageTransactionDoc>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/transactions"
        emitAll(
            db.collection("users").document(uid).collection("transactions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MileageTransactionDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeBookings(userId: String): Flow<List<AwardBookingDoc>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/bookings"
        emitAll(
            db.collection("users").document(uid).collection("bookings")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        AwardBookingDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserSeeded(userId: String, displayName: String?): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val userDoc = db.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                val safeName = (displayName?.trim()?.uppercase()?.take(60))
                    ?.takeIf { it.isNotEmpty() } ?: "KRISFLYER MEMBER"
                seedInitialState(
                    uid = uid,
                    preset = TierPreset.NEAR_SILVER,
                    memberName = safeName,
                    languageCode = "en",
                    darkMode = "SYSTEM"
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun simulateFlight(
        userId: String,
        origin: Airport,
        destination: Airport,
        cabin: CabinFareClass,
        isRoundTrip: Boolean,
        ticketFareSgd: Int,
        carrier: AirlineCarrier
    ): Result<SimulationResult> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val profileSnap = db.collection("users").document(uid).get().await()
            val profile = profileSnap.toObject(KrisFlyerProfileDoc::class.java)
                ?: return Result.failure(IllegalStateException("Profile not found"))

            val oldTier = determineTier(profile.eliteMiles.toInt(), profile.ppsValueSgd.toInt())
            val preview = KrisFlyerCalculator.calculateFlightEarn(
                origin = origin,
                destination = destination,
                cabin = cabin,
                tier = oldTier,
                isRoundTrip = isRoundTrip,
                ticketFareSgd = ticketFareSgd,
                carrier = carrier
            )

            val updatedKf = (profile.krisFlyerMiles + preview.totalKrisFlyerMiles).coerceIn(0L, 100_000_000L)
            val updatedElite = (profile.eliteMiles + preview.eliteMilesEarned).coerceIn(0L, 100_000_000L)
            val updatedPps = (profile.ppsValueSgd + preview.ppsValueSgd).coerceIn(0L, 100_000_000L)
            val newTier = determineTier(updatedElite.toInt(), updatedPps.toInt())

            val flightNo = if (carrier == AirlineCarrier.SCOOT) {
                val baseNum = destination.sampleFlightNo.removePrefix("SQ").toIntOrNull() ?: 802
                "TR$baseNum"
            } else {
                KrisFlyerNetwork.suggestedFlightNumber(origin, destination)
            }
            val tripArrow = if (isRoundTrip) "⇄" else "→"

            val subtitleVi = if (carrier == AirlineCarrier.SCOOT) {
                "Scoot (TR) • Chi tiêu S$$ticketFareSgd • 2.5x Elite Miles"
            } else {
                "${cabin.titleVi} (${cabin.bookingCodes}) • ${preview.distanceMiles} dặm bay"
            }
            val subtitleEn = if (carrier == AirlineCarrier.SCOOT) {
                "Scoot (TR) • S$$ticketFareSgd spend • 2.5x Elite Miles"
            } else {
                "${cabin.titleEn} (${cabin.bookingCodes}) • ${preview.distanceMiles} flown mi"
            }

            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            batch.update(
                userRef,
                mapOf(
                    "krisFlyerMiles" to updatedKf,
                    "eliteMiles" to updatedElite,
                    "ppsValueSgd" to updatedPps,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val txRef = userRef.collection("transactions").document()
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "titleVi" to "$flightNo • ${origin.code} $tripArrow ${destination.code}".take(120),
                    "titleEn" to "$flightNo • ${origin.code} $tripArrow ${destination.code}".take(120),
                    "subtitleVi" to subtitleVi.take(180),
                    "subtitleEn" to subtitleEn.take(180),
                    "category" to "FLIGHT",
                    "krisFlyerMilesDelta" to preview.totalKrisFlyerMiles.toLong(),
                    "eliteMilesDelta" to preview.eliteMilesEarned.toLong(),
                    "ppsValueDeltaSgd" to preview.ppsValueSgd.toLong(),
                    "badgeCode" to flightNo.take(16),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(SimulationResult(oldTier, newTier, preview.totalKrisFlyerMiles))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun simulatePartnerEarn(
        userId: String,
        titleVi: String,
        titleEn: String,
        subtitleVi: String,
        subtitleEn: String,
        badgeCode: String,
        milesEarned: Int
    ): Result<SimulationResult> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val profile = db.collection("users").document(uid).get().await()
                .toObject(KrisFlyerProfileDoc::class.java)
                ?: return Result.failure(IllegalStateException("Profile not found"))

            val tier = determineTier(profile.eliteMiles.toInt(), profile.ppsValueSgd.toInt())
            val updatedKf = (profile.krisFlyerMiles + milesEarned).coerceIn(0L, 100_000_000L)

            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            batch.update(
                userRef,
                mapOf(
                    "krisFlyerMiles" to updatedKf,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val txRef = userRef.collection("transactions").document()
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "titleVi" to titleVi.take(120),
                    "titleEn" to titleEn.take(120),
                    "subtitleVi" to subtitleVi.take(180),
                    "subtitleEn" to subtitleEn.take(180),
                    "category" to "PARTNER",
                    "krisFlyerMilesDelta" to milesEarned.toLong(),
                    "eliteMilesDelta" to 0L,
                    "ppsValueDeltaSgd" to 0L,
                    "badgeCode" to badgeCode.take(16),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(SimulationResult(tier, tier, milesEarned))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun redeemAwardFlight(
        userId: String,
        origin: Airport,
        destination: Airport,
        cabin: AwardCabin,
        awardType: AwardType,
        isRoundTrip: Boolean,
        forceWaitlist: Boolean
    ): Result<SimulationResult> {
        val path = "users/$userId/bookings"
        return try {
            val uid = requireUserId(userId)
            val profile = db.collection("users").document(uid).get().await()
                .toObject(KrisFlyerProfileDoc::class.java)
                ?: return Result.failure(IllegalStateException("Profile not found"))

            val tier = determineTier(profile.eliteMiles.toInt(), profile.ppsValueSgd.toInt())
            val quote = KrisFlyerCalculator.calculateAwardQuote(
                origin = origin,
                destination = destination,
                cabin = cabin,
                awardType = awardType,
                isRoundTrip = isRoundTrip,
                tier = tier
            )
            if (profile.krisFlyerMiles < quote.milesRequired) {
                return Result.failure(IllegalStateException("Insufficient KrisFlyer miles"))
            }

            val pnr = generatePnr()
            val flightNo = KrisFlyerNetwork.suggestedFlightNumber(origin, destination)
            val status = if (forceWaitlist) "WAITLIST" else "CONFIRMED"
            val gates = listOf("A12", "B04", "B09", "E22", "F34")
            val depTimes = listOf("09:15", "13:40", "18:25", "23:55")

            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            batch.update(
                userRef,
                mapOf(
                    "krisFlyerMiles" to (profile.krisFlyerMiles - quote.milesRequired).coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val bookingRef = userRef.collection("bookings").document()
            batch.set(
                bookingRef,
                mapOf(
                    "userId" to uid,
                    "pnr" to pnr,
                    "flightNumber" to flightNo,
                    "originCode" to origin.code,
                    "destinationCode" to destination.code,
                    "originCity" to origin.cityEn.take(80),
                    "destinationCity" to destination.cityEn.take(80),
                    "cabinLabel" to cabin.titleEn.take(60),
                    "awardTypeLabel" to awardType.titleEn.take(60),
                    "milesCost" to quote.milesRequired.toLong(),
                    "taxesSgd" to quote.taxesSgd.toLong(),
                    "seatNumber" to cabin.seatSample,
                    "departureDateLabel" to "15 DEC 2026",
                    "departureTimeLabel" to depTimes.random(),
                    "gate" to gates.random(),
                    "status" to status,
                    "roundTrip" to isRoundTrip,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            val tripArrow = if (isRoundTrip) "⇄" else "→"
            val txRef = userRef.collection("transactions").document()
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "titleVi" to "Đổi vé thưởng $flightNo • ${origin.code} $tripArrow ${destination.code}".take(120),
                    "titleEn" to "Award Redemption $flightNo • ${origin.code} $tripArrow ${destination.code}".take(120),
                    "subtitleVi" to "${cabin.titleVi} • ${awardType.titleVi} • PNR $pnr".take(180),
                    "subtitleEn" to "${cabin.titleEn} • ${awardType.titleEn} • PNR $pnr".take(180),
                    "category" to "REDEMPTION",
                    "krisFlyerMilesDelta" to -quote.milesRequired.toLong(),
                    "eliteMilesDelta" to 0L,
                    "ppsValueDeltaSgd" to 0L,
                    "badgeCode" to pnr,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(SimulationResult(tier, tier, -quote.milesRequired, pnrCreated = pnr))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun redeemLifestyleVoucher(
        userId: String,
        titleVi: String,
        titleEn: String,
        subtitleVi: String,
        subtitleEn: String,
        badgeCode: String,
        milesCost: Int
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val profile = db.collection("users").document(uid).get().await()
                .toObject(KrisFlyerProfileDoc::class.java)
                ?: return Result.failure(IllegalStateException("Profile not found"))

            if (profile.krisFlyerMiles < milesCost) {
                return Result.failure(IllegalStateException("Not enough miles"))
            }

            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            batch.update(
                userRef,
                mapOf(
                    "krisFlyerMiles" to (profile.krisFlyerMiles - milesCost).coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val txRef = userRef.collection("transactions").document()
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "titleVi" to titleVi.take(120),
                    "titleEn" to titleEn.take(120),
                    "subtitleVi" to subtitleVi.take(180),
                    "subtitleEn" to subtitleEn.take(180),
                    "category" to "REDEMPTION",
                    "krisFlyerMilesDelta" to -milesCost.toLong(),
                    "eliteMilesDelta" to 0L,
                    "ppsValueDeltaSgd" to 0L,
                    "badgeCode" to badgeCode.take(16),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun advanceBookingStatus(userId: String, bookingId: String, currentStatus: String): Result<Unit> {
        val path = "users/$userId/bookings/$bookingId"
        return try {
            val uid = requireUserId(userId)
            require(bookingId.isNotBlank()) { "Booking ID required" }
            val nextStatus = when (currentStatus) {
                "WAITLIST" -> "CONFIRMED"
                "CONFIRMED" -> "CHECKED_IN"
                "CHECKED_IN" -> "COMPLETED"
                else -> return Result.success(Unit)
            }
            db.collection("users").document(uid).collection("bookings").document(bookingId)
                .update(
                    mapOf(
                        "status" to nextStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun cancelAndRefundBooking(userId: String, booking: AwardBookingDoc): Result<Unit> {
        val path = "users/$userId/bookings/${booking.id}"
        return try {
            val uid = requireUserId(userId)
            require(booking.id.isNotBlank()) { "Booking ID required" }
            if (booking.status == "CANCELLED" || booking.status == "COMPLETED") {
                return Result.success(Unit)
            }

            val profile = db.collection("users").document(uid).get().await()
                .toObject(KrisFlyerProfileDoc::class.java)
                ?: return Result.failure(IllegalStateException("Profile not found"))

            val batch = db.batch()
            val userRef = db.collection("users").document(uid)
            val bookingRef = userRef.collection("bookings").document(booking.id)
            batch.update(
                bookingRef,
                mapOf(
                    "status" to "CANCELLED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                userRef,
                mapOf(
                    "krisFlyerMiles" to (profile.krisFlyerMiles + booking.milesCost).coerceIn(0L, 100_000_000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val txRef = userRef.collection("transactions").document()
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "titleVi" to "Hoàn dặm vé thưởng ${booking.flightNumber} (${booking.pnr})".take(120),
                    "titleEn" to "Award Miles Redeposit ${booking.flightNumber} (${booking.pnr})".take(120),
                    "subtitleVi" to "Hoàn vé ${booking.originCode} → ${booking.destinationCode} • ${booking.cabinLabel}".take(180),
                    "subtitleEn" to "Cancelled ${booking.originCode} → ${booking.destinationCode} • ${booking.cabinLabel}".take(180),
                    "category" to "REFUND",
                    "krisFlyerMilesDelta" to booking.milesCost,
                    "eliteMilesDelta" to 0L,
                    "ppsValueDeltaSgd" to 0L,
                    "badgeCode" to booking.pnr.take(16),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateMemberName(userId: String, name: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val clean = name.trim().uppercase().take(60)
            require(clean.isNotEmpty()) { "Member name cannot be empty" }
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "memberName" to clean,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun setLanguage(userId: String, languageCode: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val validCode = if (languageCode == "vi") "vi" else "en"
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "languageCode" to validCode,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun setDarkMode(userId: String, darkMode: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val validMode = when (darkMode) {
                "DARK", "LIGHT" -> darkMode
                else -> "SYSTEM"
            }
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "darkMode" to validMode,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun applyPreset(userId: String, preset: TierPreset): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val userRef = db.collection("users").document(uid)
            val current = userRef.get().await().toObject(KrisFlyerProfileDoc::class.java)
            val name = current?.memberName ?: "KRISFLYER MEMBER"
            val lang = current?.languageCode ?: "en"
            val darkMode = current?.darkMode ?: "SYSTEM"

            // Delete existing subcollection docs cleanly in batches
            val existingTxs = userRef.collection("transactions").get().await()
            val existingBookings = userRef.collection("bookings").get().await()
            val deleteBatch = db.batch()
            existingTxs.documents.forEach { deleteBatch.delete(it.reference) }
            existingBookings.documents.forEach { deleteBatch.delete(it.reference) }
            deleteBatch.commit().await()

            if (current != null) {
                userRef.update(
                    mapOf(
                        "krisFlyerMiles" to preset.kfMiles.toLong(),
                        "eliteMiles" to preset.eliteMiles.toLong(),
                        "ppsValueSgd" to preset.ppsValue.toLong(),
                        "expiringMiles" to preset.expiringMiles.toLong(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                if (preset != TierPreset.FRESH_START) {
                    seedStarterRecords(uid)
                }
            } else {
                seedInitialState(uid, preset, name, lang, darkMode)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    private suspend fun seedInitialState(
        uid: String,
        preset: TierPreset,
        memberName: String,
        languageCode: String,
        darkMode: String
    ) {
        val userRef = db.collection("users").document(uid)
        userRef.set(
            mapOf(
                "userId" to uid,
                "memberName" to memberName.take(60),
                "membershipNumber" to "880 492 7150",
                "krisFlyerMiles" to preset.kfMiles.toLong(),
                "eliteMiles" to preset.eliteMiles.toLong(),
                "ppsValueSgd" to preset.ppsValue.toLong(),
                "expiringMiles" to preset.expiringMiles.toLong(),
                "expiringDateLabel" to "31 MAR 2027",
                "languageCode" to languageCode,
                "darkMode" to darkMode,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        if (preset != TierPreset.FRESH_START) {
            seedStarterRecords(uid)
        }
    }

    private suspend fun seedStarterRecords(uid: String) {
        val userRef = db.collection("users").document(uid)
        val batch = db.batch()
        val tx1 = userRef.collection("transactions").document()
        batch.set(
            tx1,
            mapOf(
                "userId" to uid,
                "titleVi" to "SQ178 • SGN → SIN",
                "titleEn" to "SQ178 • SGN → SIN",
                "subtitleVi" to "Thương gia (Business Z) • Chặng mở đầu mô phỏng",
                "subtitleEn" to "Business Class (Z) • Simulated starter flight",
                "category" to "FLIGHT",
                "krisFlyerMilesDelta" to 1275L,
                "eliteMilesDelta" to 1020L,
                "ppsValueDeltaSgd" to 1150L,
                "badgeCode" to "SQ178",
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        val tx2 = userRef.collection("transactions").document()
        batch.set(
            tx2,
            mapOf(
                "userId" to uid,
                "titleVi" to "TR305 • SIN → SGN (Scoot)",
                "titleEn" to "TR305 • SIN → SGN (Scoot)",
                "subtitleVi" to "Scoot (TR) • Chi tiêu S$240 • 2.5x Elite Miles",
                "subtitleEn" to "Scoot (TR) • S$240 spend • 2.5x Elite Miles",
                "category" to "FLIGHT",
                "krisFlyerMilesDelta" to 240L,
                "eliteMilesDelta" to 600L,
                "ppsValueDeltaSgd" to 0L,
                "badgeCode" to "TR305",
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        val tx3 = userRef.collection("transactions").document()
        batch.set(
            tx3,
            mapOf(
                "userId" to uid,
                "titleVi" to "Quy đổi điểm thẻ ngân hàng",
                "titleEn" to "Bank Credit Card Points Transfer",
                "subtitleVi" to "Chuyển đổi đối tác tài chính sang KrisFlyer",
                "subtitleEn" to "Partner points conversion to KrisFlyer",
                "category" to "PARTNER",
                "krisFlyerMilesDelta" to 15000L,
                "eliteMilesDelta" to 0L,
                "ppsValueDeltaSgd" to 0L,
                "badgeCode" to "BANK",
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        val bk1 = userRef.collection("bookings").document()
        batch.set(
            bk1,
            mapOf(
                "userId" to uid,
                "pnr" to "SQ8K4M",
                "flightNumber" to "SQ178",
                "originCode" to "SGN",
                "destinationCode" to "SIN",
                "originCity" to "Ho Chi Minh City",
                "destinationCity" to "Singapore (Changi)",
                "cabinLabel" to "Business",
                "awardTypeLabel" to "Saver Award",
                "milesCost" to 24000L,
                "taxesSgd" to 64L,
                "seatNumber" to "11A",
                "departureDateLabel" to "28 NOV 2026",
                "departureTimeLabel" to "12:15",
                "gate" to "G07",
                "status" to "CONFIRMED",
                "roundTrip" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.commit().await()
    }

    private fun generatePnr(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return "SQ" + (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
