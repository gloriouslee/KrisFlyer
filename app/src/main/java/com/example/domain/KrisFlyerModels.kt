package com.example.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class AppLanguage(val code: String, val label: String) {
    VI("vi", "Tiếng Việt"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
    }
}

enum class KrisFlyerTier(
    val titleVi: String,
    val titleEn: String,
    val shortBadge: String,
    val starAllianceStatus: String,
    val tierBonusRate: Double,
    val baggageBonusKg: Int,
    val loungeAccessVi: String,
    val loungeAccessEn: String,
    val feeWaiverDescVi: String,
    val feeWaiverDescEn: String
) {
    KRISFLYER(
        titleVi = "Hội viên KrisFlyer",
        titleEn = "KrisFlyer Member",
        shortBadge = "KF",
        starAllianceStatus = "Base Member",
        tierBonusRate = 0.0,
        baggageBonusKg = 0,
        loungeAccessVi = "Chưa bao gồm phòng chờ (có thể vào bằng vé Thương gia/Suites)",
        loungeAccessEn = "Pay-per-use or Business/First cabin ticket required",
        feeWaiverDescVi = "Phí đổi/hoàn vé thưởng tiêu chuẩn",
        feeWaiverDescEn = "Standard award change & redeposit fees"
    ),
    ELITE_SILVER(
        titleVi = "KrisFlyer Elite Silver",
        titleEn = "KrisFlyer Elite Silver",
        shortBadge = "KFES",
        starAllianceStatus = "Star Alliance Silver",
        tierBonusRate = 0.25,
        baggageBonusKg = 10,
        loungeAccessVi = "Ưu tiên danh sách chờ & chọn ghế tiêu chuẩn miễn phí",
        loungeAccessEn = "Priority waitlist & complimentary Standard seats",
        feeWaiverDescVi = "Thưởng thêm +25% dặm bay trên chuyến bay SQ & Scoot",
        feeWaiverDescEn = "+25% tier bonus miles on eligible SQ & Scoot flights"
    ),
    ELITE_GOLD(
        titleVi = "KrisFlyer Elite Gold",
        titleEn = "KrisFlyer Elite Gold",
        shortBadge = "KFEG",
        starAllianceStatus = "Star Alliance Gold",
        tierBonusRate = 0.25,
        baggageBonusKg = 20,
        loungeAccessVi = "Phòng chờ KrisFlyer Gold & hơn 1.000 phòng chờ Star Alliance Gold (+1 khách)",
        loungeAccessEn = "KrisFlyer Gold & 1,000+ Star Alliance Gold Lounges (+1 guest)",
        feeWaiverDescVi = "Ưu tiên làm thủ tục, lên máy bay, +20kg hành lý & +25% dặm thưởng",
        feeWaiverDescEn = "Priority check-in/boarding, +20kg baggage & +25% bonus miles"
    ),
    PPS_CLUB(
        titleVi = "PPS Club",
        titleEn = "PPS Club",
        shortBadge = "PPS",
        starAllianceStatus = "Star Alliance Gold",
        tierBonusRate = 0.25,
        baggageBonusKg = 32,
        loungeAccessVi = "Phòng chờ SilverKris Business Lounge toàn cầu (+1 khách)",
        loungeAccessEn = "SilverKris Business Lounge worldwide (+1 guest)",
        feeWaiverDescVi = "Miễn phí đổi/hoàn vé thưởng & ưu tiên cao suất vé Saver",
        feeWaiverDescEn = "Waived award change/cancel fees & priority Saver availability"
    ),
    SOLITAIRE_PPS(
        titleVi = "Solitaire PPS Club",
        titleEn = "Solitaire PPS Club",
        shortBadge = "SOLITAIRE",
        starAllianceStatus = "Star Alliance Gold",
        tierBonusRate = 0.25,
        baggageBonusKg = 40,
        loungeAccessVi = "Phòng chờ SilverKris First Class Lounge (bao gồm bạn đồng hành)",
        loungeAccessEn = "SilverKris First Class Lounge worldwide (with companion)",
        feeWaiverDescVi = "Đặc quyền cao nhất: Phòng chờ Hạng Nhất, thẻ phụ & miễn mọi phí dịch vụ",
        feeWaiverDescEn = "Top-tier First Lounge access, supplementary card & full fee waivers"
    );

    fun localizedTitle(lang: AppLanguage): String = if (lang == AppLanguage.VI) titleVi else titleEn
    fun localizedLounge(lang: AppLanguage): String = if (lang == AppLanguage.VI) loungeAccessVi else loungeAccessEn
    fun localizedPerks(lang: AppLanguage): String = if (lang == AppLanguage.VI) feeWaiverDescVi else feeWaiverDescEn
}

fun determineTier(eliteMiles: Int, ppsValueSgd: Int): KrisFlyerTier = when {
    ppsValueSgd >= 50_000 -> KrisFlyerTier.SOLITAIRE_PPS
    ppsValueSgd >= 25_000 -> KrisFlyerTier.PPS_CLUB
    eliteMiles >= 50_000 -> KrisFlyerTier.ELITE_GOLD
    eliteMiles >= 25_000 -> KrisFlyerTier.ELITE_SILVER
    else -> KrisFlyerTier.KRISFLYER
}

enum class AwardZone(val zoneCode: Int, val labelVi: String, val labelEn: String) {
    SINGAPORE(1, "Singapore", "Singapore"),
    SE_ASIA(3, "Đông Nam Á", "Southeast Asia"),
    NORTH_ASIA_1(4, "Đông Á (HK/TW)", "North Asia I"),
    NORTH_ASIA_2(7, "Nhật Bản / Hàn Quốc", "Japan & South Korea"),
    OCEANIA(9, "Úc / New Zealand", "Australia & NZ"),
    MIDDLE_EAST(10, "Trung Đông", "Middle East"),
    EUROPE(11, "Châu Âu", "Europe"),
    US_WEST(12, "Bờ Tây Hoa Kỳ", "US West Coast"),
    US_EAST(13, "Bờ Đông Hoa Kỳ", "US East Coast")
}

data class Airport(
    val code: String,
    val cityVi: String,
    val cityEn: String,
    val countryVi: String,
    val countryEn: String,
    val lat: Double,
    val lon: Double,
    val zone: AwardZone,
    val sampleFlightNo: String
) {
    fun localizedCity(lang: AppLanguage): String = if (lang == AppLanguage.VI) cityVi else cityEn
}

object KrisFlyerNetwork {
    val airports = listOf(
        Airport("SIN", "Singapore (Changi)", "Singapore (Changi)", "Singapore", "Singapore", 1.3644, 103.9915, AwardZone.SINGAPORE, "SQ101"),
        Airport("SGN", "TP. Hồ Chí Minh", "Ho Chi Minh City", "Việt Nam", "Vietnam", 10.8188, 106.6519, AwardZone.SE_ASIA, "SQ178"),
        Airport("HAN", "Hà Nội", "Hanoi", "Việt Nam", "Vietnam", 21.2187, 105.8042, AwardZone.SE_ASIA, "SQ192"),
        Airport("DAD", "Đà Nẵng", "Da Nang", "Việt Nam", "Vietnam", 16.0439, 108.1994, AwardZone.SE_ASIA, "SQ172"),
        Airport("BKK", "Bangkok", "Bangkok", "Thái Lan", "Thailand", 13.6900, 100.7501, AwardZone.SE_ASIA, "SQ708"),
        Airport("CGK", "Jakarta", "Jakarta", "Indonesia", "Indonesia", -6.1256, 106.6559, AwardZone.SE_ASIA, "SQ956"),
        Airport("HKG", "Hồng Kông", "Hong Kong", "Trung Quốc", "Hong Kong SAR", 22.3080, 113.9185, AwardZone.NORTH_ASIA_1, "SQ892"),
        Airport("NRT", "Tokyo (Narita)", "Tokyo (Narita)", "Nhật Bản", "Japan", 35.7720, 140.3929, AwardZone.NORTH_ASIA_2, "SQ638"),
        Airport("HND", "Tokyo (Haneda)", "Tokyo (Haneda)", "Nhật Bản", "Japan", 35.5494, 139.7798, AwardZone.NORTH_ASIA_2, "SQ634"),
        Airport("ICN", "Seoul (Incheon)", "Seoul (Incheon)", "Hàn Quốc", "South Korea", 37.4602, 126.4407, AwardZone.NORTH_ASIA_2, "SQ600"),
        Airport("SYD", "Sydney", "Sydney", "Úc", "Australia", -33.9399, 151.1753, AwardZone.OCEANIA, "SQ231"),
        Airport("MEL", "Melbourne", "Melbourne", "Úc", "Australia", -37.6690, 144.8410, AwardZone.OCEANIA, "SQ217"),
        Airport("DXB", "Dubai", "Dubai", "UAE", "UAE", 25.2532, 55.3657, AwardZone.MIDDLE_EAST, "SQ494"),
        Airport("LHR", "London (Heathrow)", "London (Heathrow)", "Anh", "United Kingdom", 51.4700, -0.4543, AwardZone.EUROPE, "SQ322"),
        Airport("CDG", "Paris (CDG)", "Paris (CDG)", "Pháp", "France", 49.0097, 2.5479, AwardZone.EUROPE, "SQ336"),
        Airport("FRA", "Frankfurt", "Frankfurt", "Đức", "Germany", 50.0379, 8.5622, AwardZone.EUROPE, "SQ026"),
        Airport("ZRH", "Zurich", "Zurich", "Thụy Sĩ", "Switzerland", 47.4582, 8.5555, AwardZone.EUROPE, "SQ346"),
        Airport("SFO", "San Francisco", "San Francisco", "Hoa Kỳ", "USA", 37.6213, -122.3790, AwardZone.US_WEST, "SQ032"),
        Airport("LAX", "Los Angeles", "Los Angeles", "Hoa Kỳ", "USA", 33.9416, -118.4085, AwardZone.US_WEST, "SQ038"),
        Airport("JFK", "New York (JFK)", "New York (JFK)", "Hoa Kỳ", "USA", 40.6413, -73.7781, AwardZone.US_EAST, "SQ024")
    )

    fun byCode(code: String): Airport = airports.firstOrNull { it.code == code } ?: airports.first()

    /** Great-circle distance in statute miles (minimum 500 miles for KrisFlyer accrual consistency). */
    fun distanceMiles(origin: Airport, destination: Airport): Int {
        if (origin.code == destination.code) return 0
        val earthRadiusMiles = 3958.8
        val dLat = Math.toRadians(destination.lat - origin.lat)
        val dLon = Math.toRadians(destination.lon - origin.lon)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(origin.lat)) * cos(Math.toRadians(destination.lat)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadiusMiles * c).roundToInt().coerceAtLeast(500)
    }

    fun suggestedFlightNumber(origin: Airport, destination: Airport): String {
        return when {
            origin.code == "SIN" -> destination.sampleFlightNo
            destination.code == "SIN" -> {
                val baseNum = origin.sampleFlightNo.removePrefix("SQ").toIntOrNull() ?: 178
                "SQ${baseNum + 1}"
            }
            else -> origin.sampleFlightNo
        }
    }
}

enum class CabinFareClass(
    val titleVi: String,
    val titleEn: String,
    val bookingCodes: String,
    val kfRate: Double,
    val eliteRate: Double,
    val ppsEligible: Boolean,
    val baseFarePer1000MilesSgd: Int,
    val cabinShort: String
) {
    ECONOMY_LITE("Phổ thông Lite", "Economy Lite", "V, K", 0.50, 0.50, false, 145, "Economy"),
    ECONOMY_VALUE("Phổ thông Value", "Economy Value", "Q, N", 0.75, 0.75, false, 190, "Economy"),
    ECONOMY_STANDARD("Phổ thông Standard", "Economy Standard", "M, H, W", 1.00, 1.00, false, 260, "Economy"),
    ECONOMY_FLEXI("Phổ thông Flexi", "Economy Flexi", "Y, B, E", 1.00, 1.00, false, 360, "Economy"),
    PREMIUM_ECONOMY("Phổ thông Đặc biệt", "Premium Economy", "S, T, P, R", 1.25, 1.25, false, 520, "Prem Econ"),
    BUSINESS("Thương gia (Business)", "Business Class", "Z, C, J, U, D", 1.50, 1.50, true, 1380, "Business"),
    FIRST_SUITES("Hạng Nhất / Suites", "First Class / Suites", "F, A", 2.00, 2.00, true, 2650, "Suites / First");

    fun localizedTitle(lang: AppLanguage): String = if (lang == AppLanguage.VI) titleVi else titleEn
}

enum class AwardCabin(
    val titleVi: String,
    val titleEn: String,
    val seatSample: String
) {
    ECONOMY("Phổ thông", "Economy", "42K"),
    PREMIUM_ECONOMY("Phổ thông Đặc biệt", "Premium Economy", "31A"),
    BUSINESS("Thương gia", "Business", "11A"),
    FIRST_SUITES("Suites / Hạng Nhất", "Suites / First", "1A");

    fun localizedTitle(lang: AppLanguage): String = if (lang == AppLanguage.VI) titleVi else titleEn
}

enum class AwardType(
    val titleVi: String,
    val titleEn: String,
    val multiplier: Double,
    val descVi: String,
    val descEn: String
) {
    SPONTANEOUS_ESCAPES(
        "Spontaneous Escapes (-30%)",
        "Spontaneous Escapes (-30%)",
        0.70,
        "Ưu đãi tháng giảm 30% dặm Saver trên chặng chọn lọc",
        "Monthly 30% off Saver promo on selected routes"
    ),
    SAVER(
        "Vé Thưởng Saver",
        "Saver Award",
        1.00,
        "Mức dặm tiết kiệm nhất, số ghế giới hạn",
        "Lowest mileage tier, capacity controlled"
    ),
    ADVANTAGE(
        "Vé Thưởng Advantage",
        "Advantage Award",
        1.45,
        "Số ghế dồi dào hơn, tặng kèm 2 chặng dừng miễn phí",
        "Expanded seat inventory with complimentary stopovers"
    );

    fun localizedTitle(lang: AppLanguage): String = if (lang == AppLanguage.VI) titleVi else titleEn
    fun localizedDesc(lang: AppLanguage): String = if (lang == AppLanguage.VI) descVi else descEn
}

enum class AirlineCarrier(
    val code: String,
    val titleVi: String,
    val titleEn: String
) {
    SINGAPORE_AIRLINES("SQ", "Singapore Airlines (SQ)", "Singapore Airlines (SQ)"),
    SCOOT("TR", "Scoot (TR)", "Scoot (TR)");

    fun localizedTitle(lang: AppLanguage): String = if (lang == AppLanguage.VI) titleVi else titleEn
}

data class FlightEarnPreview(
    val distanceMiles: Int,
    val baseMiles: Int,
    val tierBonusMiles: Int,
    val totalKrisFlyerMiles: Int,
    val eliteMilesEarned: Int,
    val ppsValueSgd: Int
)

data class AwardQuote(
    val milesRequired: Int,
    val taxesSgd: Int,
    val instantConfirmationLikely: Boolean,
    val availabilityRatePercent: Int
)

object KrisFlyerCalculator {
    fun calculateFlightEarn(
        origin: Airport,
        destination: Airport,
        cabin: CabinFareClass,
        tier: KrisFlyerTier,
        isRoundTrip: Boolean,
        ticketFareSgd: Int,
        carrier: AirlineCarrier = AirlineCarrier.SINGAPORE_AIRLINES
    ): FlightEarnPreview {
        val singleDistance = KrisFlyerNetwork.distanceMiles(origin, destination)
        val totalDistance = if (isRoundTrip) singleDistance * 2 else singleDistance
        if (carrier == AirlineCarrier.SCOOT) {
            // Scoot official KrisFlyer rules: 1 KF mile per S$1 spent, +25% bonus for Elite/PPS,
            // 2.5 Elite miles per base+bonus KF mile earned, 0 PPS Value.
            val baseMiles = ticketFareSgd.coerceAtLeast(0)
            val tierBonus = if (tier.tierBonusRate > 0.0) (baseMiles * tier.tierBonusRate).roundToInt() else 0
            val totalKf = baseMiles + tierBonus
            val eliteMiles = (totalKf * 2.5).roundToInt()
            return FlightEarnPreview(
                distanceMiles = totalDistance,
                baseMiles = baseMiles,
                tierBonusMiles = tierBonus,
                totalKrisFlyerMiles = totalKf,
                eliteMilesEarned = eliteMiles,
                ppsValueSgd = 0
            )
        }
        val baseMiles = (totalDistance * cabin.kfRate).roundToInt()
        val tierBonus = if (tier.tierBonusRate > 0.0) (totalDistance * tier.tierBonusRate).roundToInt() else 0
        val eliteMiles = (totalDistance * cabin.eliteRate).roundToInt()
        val ppsValue = if (cabin.ppsEligible) ticketFareSgd.coerceAtLeast(0) else 0
        return FlightEarnPreview(
            distanceMiles = totalDistance,
            baseMiles = baseMiles,
            tierBonusMiles = tierBonus,
            totalKrisFlyerMiles = baseMiles + tierBonus,
            eliteMilesEarned = eliteMiles,
            ppsValueSgd = ppsValue
        )
    }

    fun estimateTicketFareSgd(
        origin: Airport,
        destination: Airport,
        cabin: CabinFareClass,
        isRoundTrip: Boolean
    ): Int {
        val dist = KrisFlyerNetwork.distanceMiles(origin, destination)
        val legs = if (isRoundTrip) 2 else 1
        val raw = ((dist / 1000.0).coerceAtLeast(0.65) * cabin.baseFarePer1000MilesSgd * legs).roundToInt()
        return ((raw + 49) / 50) * 50
    }

    /** Authentic Singapore Airlines KrisFlyer Zone-to-Zone Saver base rates (one-way). */
    fun calculateAwardQuote(
        origin: Airport,
        destination: Airport,
        cabin: AwardCabin,
        awardType: AwardType,
        isRoundTrip: Boolean,
        tier: KrisFlyerTier
    ): AwardQuote {
        val saverOneWay = baseSaverChart(origin.zone, destination.zone, cabin)
        val adjustedOneWay = ((saverOneWay * awardType.multiplier).roundToInt() / 500) * 500
        val totalMiles = adjustedOneWay * (if (isRoundTrip) 2 else 1)

        val dist = KrisFlyerNetwork.distanceMiles(origin, destination)
        val baseTaxOneWay = when {
            destination.zone == AwardZone.EUROPE || origin.zone == AwardZone.EUROPE -> 118
            dist > 5000 -> 88
            dist > 2500 -> 68
            else -> 54
        }
        val cabinTaxSurcharge = if (cabin == AwardCabin.BUSINESS || cabin == AwardCabin.FIRST_SUITES) 22 else 0
        val totalTaxes = (baseTaxOneWay + cabinTaxSurcharge) * (if (isRoundTrip) 2 else 1)

        val availabilityProb = when (awardType) {
            AwardType.ADVANTAGE -> 96
            AwardType.SAVER -> when (cabin) {
                AwardCabin.ECONOMY, AwardCabin.PREMIUM_ECONOMY -> if (tier >= KrisFlyerTier.ELITE_GOLD) 92 else 82
                AwardCabin.BUSINESS -> if (tier >= KrisFlyerTier.PPS_CLUB) 85 else if (tier >= KrisFlyerTier.ELITE_GOLD) 68 else 52
                AwardCabin.FIRST_SUITES -> if (tier >= KrisFlyerTier.SOLITAIRE_PPS) 70 else if (tier >= KrisFlyerTier.PPS_CLUB) 48 else 28
            }
            AwardType.SPONTANEOUS_ESCAPES -> if (cabin == AwardCabin.FIRST_SUITES) 20 else 78
        }

        return AwardQuote(
            milesRequired = totalMiles.coerceAtLeast(5_000),
            taxesSgd = totalTaxes,
            instantConfirmationLikely = availabilityProb >= 60,
            availabilityRatePercent = availabilityProb
        )
    }

    private fun baseSaverChart(from: AwardZone, to: AwardZone, cabin: AwardCabin): Int {
        // Normalize zone pair assuming SIN or transit via SIN
        val furthestZone = if (from == AwardZone.SINGAPORE) to
        else if (to == AwardZone.SINGAPORE) from
        else if (from.zoneCode >= to.zoneCode) from else to

        val isMultiRegionTransit = from != AwardZone.SINGAPORE && to != AwardZone.SINGAPORE && from != to

        val baseSinRate = when (furthestZone) {
            AwardZone.SINGAPORE -> when (cabin) {
                AwardCabin.ECONOMY -> 8_500
                AwardCabin.PREMIUM_ECONOMY -> 14_500
                AwardCabin.BUSINESS -> 21_000
                AwardCabin.FIRST_SUITES -> 30_500
            }
            AwardZone.SE_ASIA -> when (cabin) {
                AwardCabin.ECONOMY -> 13_500
                AwardCabin.PREMIUM_ECONOMY -> 19_000
                AwardCabin.BUSINESS -> 24_000
                AwardCabin.FIRST_SUITES -> 33_500
            }
            AwardZone.NORTH_ASIA_1 -> when (cabin) {
                AwardCabin.ECONOMY -> 16_500
                AwardCabin.PREMIUM_ECONOMY -> 25_500
                AwardCabin.BUSINESS -> 34_000
                AwardCabin.FIRST_SUITES -> 45_000
            }
            AwardZone.NORTH_ASIA_2 -> when (cabin) {
                AwardCabin.ECONOMY -> 27_000
                AwardCabin.PREMIUM_ECONOMY -> 39_500
                AwardCabin.BUSINESS -> 52_000
                AwardCabin.FIRST_SUITES -> 77_000
            }
            AwardZone.OCEANIA -> when (cabin) {
                AwardCabin.ECONOMY -> 30_500
                AwardCabin.PREMIUM_ECONOMY -> 51_000
                AwardCabin.BUSINESS -> 68_500
                AwardCabin.FIRST_SUITES -> 93_500
            }
            AwardZone.MIDDLE_EAST -> when (cabin) {
                AwardCabin.ECONOMY -> 29_000
                AwardCabin.PREMIUM_ECONOMY -> 42_500
                AwardCabin.BUSINESS -> 56_500
                AwardCabin.FIRST_SUITES -> 85_000
            }
            AwardZone.EUROPE -> when (cabin) {
                AwardCabin.ECONOMY -> 42_000
                AwardCabin.PREMIUM_ECONOMY -> 71_000
                AwardCabin.BUSINESS -> 103_500
                AwardCabin.FIRST_SUITES -> 141_000
            }
            AwardZone.US_WEST -> when (cabin) {
                AwardCabin.ECONOMY -> 42_000
                AwardCabin.PREMIUM_ECONOMY -> 75_000
                AwardCabin.BUSINESS -> 107_000
                AwardCabin.FIRST_SUITES -> 148_500
            }
            AwardZone.US_EAST -> when (cabin) {
                AwardCabin.ECONOMY -> 44_000
                AwardCabin.PREMIUM_ECONOMY -> 80_500
                AwardCabin.BUSINESS -> 111_500
                AwardCabin.FIRST_SUITES -> 148_500
            }
        }

        return if (isMultiRegionTransit) (baseSinRate * 1.15).roundToInt() else baseSinRate
    }
}

data class MilestoneReward(
    val thresholdEliteMiles: Int,
    val titleVi: String,
    val titleEn: String
)

object MilestoneRewardsCatalog {
    val items = listOf(
        MilestoneReward(1_000, "Giảm giá vé Scoot 20%", "20% Scoot Voucher"),
        MilestoneReward(2_500, "1.000 Dặm thưởng KrisFlyer", "1,000 Bonus KrisFlyer Miles"),
        MilestoneReward(5_000, "Chọn ghế tiêu chuẩn miễn phí", "Complimentary Standard Seat"),
        MilestoneReward(10_000, "Voucher KrisShop S$50", "S$50 KrisShop Voucher"),
        MilestoneReward(15_000, "2.500 Dặm thưởng KrisFlyer", "2,500 Bonus KrisFlyer Miles"),
        MilestoneReward(25_000, "Đạt hạng Elite Silver", "Unlock Elite Silver Status"),
        MilestoneReward(35_000, "Nâng hạng 1 chặng (Advance Upgrade)", "One-cabin Advance Upgrade"),
        MilestoneReward(50_000, "Đạt hạng Elite Gold & Star Alliance Gold", "Unlock Elite Gold & Star Gold")
    )
}
