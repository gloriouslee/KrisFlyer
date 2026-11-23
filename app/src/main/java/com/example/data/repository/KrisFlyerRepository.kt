package com.example.data.repository

import com.example.data.local.AwardBookingEntity
import com.example.data.local.KrisFlyerDao
import com.example.data.local.MileageTransactionEntity
import com.example.data.local.SimulatorProfileEntity
import com.example.domain.Airport
import com.example.domain.AwardCabin
import com.example.domain.AwardType
import com.example.domain.CabinFareClass
import com.example.domain.KrisFlyerCalculator
import com.example.domain.KrisFlyerNetwork
import com.example.domain.KrisFlyerTier
import com.example.domain.determineTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

enum class TierPreset(
    val kfMiles: Int,
    val eliteMiles: Int,
    val ppsValue: Int,
    val expiringMiles: Int
) {
    FRESH_START(0, 0, 0, 0),
    NEAR_SILVER(38_450, 18_200, 4_800, 3_200),
    ELITE_GOLD(86_500, 54_200, 11_400, 8_500),
    PPS_CLUB(168_000, 62_000, 29_500, 0),
    SOLITAIRE_PPS(345_000, 95_000, 56_800, 0)
}

data class SimulationResult(
    val oldTier: KrisFlyerTier,
    val newTier: KrisFlyerTier,
    val milesDelta: Int,
    val pnrCreated: String? = null
)

class KrisFlyerRepository(private val dao: KrisFlyerDao) {
    private val seedMutex = Mutex()

    val profileFlow: Flow<SimulatorProfileEntity?> = dao.observeProfile()
    val transactionsFlow: Flow<List<MileageTransactionEntity>> = dao.observeTransactions()
    val bookingsFlow: Flow<List<AwardBookingEntity>> = dao.observeBookings()

    suspend fun ensureSeeded() {
        seedMutex.withLock {
            if (dao.getProfileOnce() == null) {
                seedInitialState(TierPreset.NEAR_SILVER, "MINH ANH NGUYEN", "en")
            }
        }
    }

    suspend fun simulateFlight(
        origin: Airport,
        destination: Airport,
        cabin: CabinFareClass,
        isRoundTrip: Boolean,
        ticketFareSgd: Int
    ): SimulationResult {
        val profile = dao.getProfileOnce() ?: return SimulationResult(KrisFlyerTier.KRISFLYER, KrisFlyerTier.KRISFLYER, 0)
        val oldTier = determineTier(profile.eliteMiles, profile.ppsValueSgd)
        val preview = KrisFlyerCalculator.calculateFlightEarn(
            origin = origin,
            destination = destination,
            cabin = cabin,
            tier = oldTier,
            isRoundTrip = isRoundTrip,
            ticketFareSgd = ticketFareSgd
        )
        val updatedKf = profile.krisFlyerMiles + preview.totalKrisFlyerMiles
        val updatedElite = profile.eliteMiles + preview.eliteMilesEarned
        val updatedPps = profile.ppsValueSgd + preview.ppsValueSgd
        val newTier = determineTier(updatedElite, updatedPps)

        dao.upsertProfile(
            profile.copy(
                krisFlyerMiles = updatedKf,
                eliteMiles = updatedElite,
                ppsValueSgd = updatedPps
            )
        )

        val flightNo = KrisFlyerNetwork.suggestedFlightNumber(origin, destination)
        val tripArrow = if (isRoundTrip) "⇄" else "→"
        dao.insertTransaction(
            MileageTransactionEntity(
                titleVi = "$flightNo • ${origin.code} $tripArrow ${destination.code}",
                titleEn = "$flightNo • ${origin.code} $tripArrow ${destination.code}",
                subtitleVi = "${cabin.titleVi} (${cabin.bookingCodes}) • ${preview.distanceMiles} dặm bay",
                subtitleEn = "${cabin.titleEn} (${cabin.bookingCodes}) • ${preview.distanceMiles} flown mi",
                category = "FLIGHT",
                krisFlyerMilesDelta = preview.totalKrisFlyerMiles,
                eliteMilesDelta = preview.eliteMilesEarned,
                ppsValueDeltaSgd = preview.ppsValueSgd,
                badgeCode = flightNo,
                timestampMillis = System.currentTimeMillis()
            )
        )

        return SimulationResult(oldTier, newTier, preview.totalKrisFlyerMiles)
    }

    suspend fun simulatePartnerEarn(
        titleVi: String,
        titleEn: String,
        subtitleVi: String,
        subtitleEn: String,
        badgeCode: String,
        milesEarned: Int
    ): SimulationResult {
        val profile = dao.getProfileOnce() ?: return SimulationResult(KrisFlyerTier.KRISFLYER, KrisFlyerTier.KRISFLYER, 0)
        val tier = determineTier(profile.eliteMiles, profile.ppsValueSgd)
        dao.upsertProfile(profile.copy(krisFlyerMiles = profile.krisFlyerMiles + milesEarned))
        dao.insertTransaction(
            MileageTransactionEntity(
                titleVi = titleVi,
                titleEn = titleEn,
                subtitleVi = subtitleVi,
                subtitleEn = subtitleEn,
                category = "PARTNER",
                krisFlyerMilesDelta = milesEarned,
                eliteMilesDelta = 0,
                ppsValueDeltaSgd = 0,
                badgeCode = badgeCode,
                timestampMillis = System.currentTimeMillis()
            )
        )
        return SimulationResult(tier, tier, milesEarned)
    }

    suspend fun redeemAwardFlight(
        origin: Airport,
        destination: Airport,
        cabin: AwardCabin,
        awardType: AwardType,
        isRoundTrip: Boolean,
        forceWaitlist: Boolean
    ): SimulationResult? {
        val profile = dao.getProfileOnce() ?: return null
        val tier = determineTier(profile.eliteMiles, profile.ppsValueSgd)
        val quote = KrisFlyerCalculator.calculateAwardQuote(
            origin = origin,
            destination = destination,
            cabin = cabin,
            awardType = awardType,
            isRoundTrip = isRoundTrip,
            tier = tier
        )
        if (profile.krisFlyerMiles < quote.milesRequired) return null

        val pnr = generatePnr()
        val flightNo = KrisFlyerNetwork.suggestedFlightNumber(origin, destination)
        val status = if (forceWaitlist) "WAITLIST" else "CONFIRMED"
        val now = System.currentTimeMillis()
        val gates = listOf("A12", "B04", "B09", "E22", "F34")
        val depTimes = listOf("09:15", "13:40", "18:25", "23:55")

        dao.upsertProfile(
            profile.copy(krisFlyerMiles = profile.krisFlyerMiles - quote.milesRequired)
        )
        dao.insertBooking(
            AwardBookingEntity(
                pnr = pnr,
                flightNumber = flightNo,
                originCode = origin.code,
                destinationCode = destination.code,
                originCity = origin.cityEn,
                destinationCity = destination.cityEn,
                cabinLabel = cabin.titleEn,
                awardTypeLabel = awardType.titleEn,
                milesCost = quote.milesRequired,
                taxesSgd = quote.taxesSgd,
                seatNumber = cabin.seatSample,
                departureDateLabel = "15 DEC 2026",
                departureTimeLabel = depTimes.random(),
                gate = gates.random(),
                status = status,
                isRoundTrip = isRoundTrip,
                createdAtMillis = now
            )
        )

        val tripArrow = if (isRoundTrip) "⇄" else "→"
        dao.insertTransaction(
            MileageTransactionEntity(
                titleVi = "Đổi vé thưởng $flightNo • ${origin.code} $tripArrow ${destination.code}",
                titleEn = "Award Redemption $flightNo • ${origin.code} $tripArrow ${destination.code}",
                subtitleVi = "${cabin.titleVi} • ${awardType.titleVi} • PNR $pnr",
                subtitleEn = "${cabin.titleEn} • ${awardType.titleEn} • PNR $pnr",
                category = "REDEMPTION",
                krisFlyerMilesDelta = -quote.milesRequired,
                eliteMilesDelta = 0,
                ppsValueDeltaSgd = 0,
                badgeCode = pnr,
                timestampMillis = now
            )
        )

        return SimulationResult(tier, tier, -quote.milesRequired, pnrCreated = pnr)
    }

    suspend fun redeemLifestyleVoucher(
        titleVi: String,
        titleEn: String,
        subtitleVi: String,
        subtitleEn: String,
        badgeCode: String,
        milesCost: Int
    ): Boolean {
        val profile = dao.getProfileOnce() ?: return false
        if (profile.krisFlyerMiles < milesCost) return false

        dao.upsertProfile(profile.copy(krisFlyerMiles = profile.krisFlyerMiles - milesCost))
        dao.insertTransaction(
            MileageTransactionEntity(
                titleVi = titleVi,
                titleEn = titleEn,
                subtitleVi = subtitleVi,
                subtitleEn = subtitleEn,
                category = "REDEMPTION",
                krisFlyerMilesDelta = -milesCost,
                eliteMilesDelta = 0,
                ppsValueDeltaSgd = 0,
                badgeCode = badgeCode,
                timestampMillis = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun advanceBookingStatus(bookingId: Long) {
        val booking = dao.getBookingById(bookingId) ?: return
        val nextStatus = when (booking.status) {
            "WAITLIST" -> "CONFIRMED"
            "CONFIRMED" -> "CHECKED_IN"
            "CHECKED_IN" -> "COMPLETED"
            else -> return
        }
        dao.updateBooking(booking.copy(status = nextStatus))
    }

    suspend fun cancelAndRefundBooking(bookingId: Long) {
        val booking = dao.getBookingById(bookingId) ?: return
        if (booking.status == "CANCELLED" || booking.status == "COMPLETED") return
        val profile = dao.getProfileOnce() ?: return

        dao.updateBooking(booking.copy(status = "CANCELLED"))
        dao.upsertProfile(profile.copy(krisFlyerMiles = profile.krisFlyerMiles + booking.milesCost))
        dao.insertTransaction(
            MileageTransactionEntity(
                titleVi = "Hoàn dặm vé thưởng ${booking.flightNumber} (${booking.pnr})",
                titleEn = "Award Miles Redeposit ${booking.flightNumber} (${booking.pnr})",
                subtitleVi = "Hoàn vé ${booking.originCode} → ${booking.destinationCode} • ${booking.cabinLabel}",
                subtitleEn = "Cancelled ${booking.originCode} → ${booking.destinationCode} • ${booking.cabinLabel}",
                category = "REFUND",
                krisFlyerMilesDelta = booking.milesCost,
                eliteMilesDelta = 0,
                ppsValueDeltaSgd = 0,
                badgeCode = booking.pnr,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateMemberName(name: String) {
        val profile = dao.getProfileOnce() ?: return
        val clean = name.trim().uppercase().take(28)
        if (clean.isNotEmpty()) {
            dao.upsertProfile(profile.copy(memberName = clean))
        }
    }

    suspend fun setLanguage(languageCode: String) {
        val profile = dao.getProfileOnce() ?: return
        dao.upsertProfile(profile.copy(languageCode = languageCode))
    }

    suspend fun setDarkModeOverride(darkMode: Boolean?) {
        val profile = dao.getProfileOnce() ?: return
        dao.upsertProfile(profile.copy(darkModeOverride = darkMode))
    }

    suspend fun applyPreset(preset: TierPreset) {
        val current = dao.getProfileOnce()
        val name = current?.memberName ?: "MINH ANH NGUYEN"
        val lang = current?.languageCode ?: "en"
        val darkOverride = current?.darkModeOverride
        dao.clearTransactions()
        dao.clearBookings()
        seedInitialState(preset, name, lang, darkOverride)
    }

    private suspend fun seedInitialState(
        preset: TierPreset,
        memberName: String,
        languageCode: String,
        darkModeOverride: Boolean? = null
    ) {
        val now = System.currentTimeMillis()
        dao.upsertProfile(
            SimulatorProfileEntity(
                id = 1,
                memberName = memberName,
                membershipNumber = "880 492 7150",
                krisFlyerMiles = preset.kfMiles,
                eliteMiles = preset.eliteMiles,
                ppsValueSgd = preset.ppsValue,
                expiringMiles = preset.expiringMiles,
                expiringDateLabel = "31 MAR 2027",
                languageCode = languageCode,
                darkModeOverride = darkModeOverride
            )
        )

        if (preset != TierPreset.FRESH_START) {
            dao.insertTransactions(
                listOf(
                    MileageTransactionEntity(
                        titleVi = "SQ178 • SGN → SIN",
                        titleEn = "SQ178 • SGN → SIN",
                        subtitleVi = "Thương gia (Business Z) • Chặng mở đầu mô phỏng",
                        subtitleEn = "Business Class (Z) • Simulated starter flight",
                        category = "FLIGHT",
                        krisFlyerMilesDelta = 1_275,
                        eliteMilesDelta = 1_020,
                        ppsValueDeltaSgd = 1_150,
                        badgeCode = "SQ178",
                        timestampMillis = now - 86_400_000L * 4
                    ),
                    MileageTransactionEntity(
                        titleVi = "SQ638 • SIN → NRT",
                        titleEn = "SQ638 • SIN → NRT",
                        subtitleVi = "Phổ thông Đặc biệt (S) • 3.324 dặm bay",
                        subtitleEn = "Premium Economy (S) • 3,324 flown mi",
                        category = "FLIGHT",
                        krisFlyerMilesDelta = 4_986,
                        eliteMilesDelta = 4_155,
                        ppsValueDeltaSgd = 0,
                        badgeCode = "SQ638",
                        timestampMillis = now - 86_400_000L * 11
                    ),
                    MileageTransactionEntity(
                        titleVi = "Quy đổi điểm thẻ ngân hàng",
                        titleEn = "Bank Credit Card Points Transfer",
                        subtitleVi = "Chuyển đổi đối tác tài chính sang KrisFlyer",
                        subtitleEn = "Partner points conversion to KrisFlyer",
                        category = "PARTNER",
                        krisFlyerMilesDelta = 15_000,
                        eliteMilesDelta = 0,
                        ppsValueDeltaSgd = 0,
                        badgeCode = "BANK",
                        timestampMillis = now - 86_400_000L * 19
                    )
                )
            )

            dao.insertBooking(
                AwardBookingEntity(
                    pnr = "SQ8K4M",
                    flightNumber = "SQ178",
                    originCode = "SGN",
                    destinationCode = "SIN",
                    originCity = "Ho Chi Minh City",
                    destinationCity = "Singapore (Changi)",
                    cabinLabel = "Business",
                    awardTypeLabel = "Saver Award",
                    milesCost = 24_000,
                    taxesSgd = 64,
                    seatNumber = "11A",
                    departureDateLabel = "28 NOV 2026",
                    departureTimeLabel = "12:15",
                    gate = "G07",
                    status = "CONFIRMED",
                    isRoundTrip = false,
                    createdAtMillis = now - 86_400_000L * 2
                )
            )
        }
    }

    private fun generatePnr(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return "SQ" + (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
