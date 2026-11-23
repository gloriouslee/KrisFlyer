package com.example

import com.example.domain.AwardCabin
import com.example.domain.AwardType
import com.example.domain.CabinFareClass
import com.example.domain.KrisFlyerCalculator
import com.example.domain.KrisFlyerNetwork
import com.example.domain.KrisFlyerTier
import com.example.domain.determineTier
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KrisFlyerCalculatorTest {

    @Test
    fun tierDetermination_matchesKrisFlyerAndPpsThresholds() {
        assertEquals(KrisFlyerTier.KRISFLYER, determineTier(18_200, 4_800))
        assertEquals(KrisFlyerTier.ELITE_SILVER, determineTier(25_000, 0))
        assertEquals(KrisFlyerTier.ELITE_GOLD, determineTier(50_000, 10_000))
        assertEquals(KrisFlyerTier.PPS_CLUB, determineTier(10_000, 25_000))
        assertEquals(KrisFlyerTier.SOLITAIRE_PPS, determineTier(0, 50_000))
    }

    @Test
    fun flightEarn_calculatesBusinessPpsAndTierBonus() {
        val sin = KrisFlyerNetwork.byCode("SIN")
        val nrt = KrisFlyerNetwork.byCode("NRT")
        val preview = KrisFlyerCalculator.calculateFlightEarn(
            origin = sin,
            destination = nrt,
            cabin = CabinFareClass.BUSINESS,
            tier = KrisFlyerTier.ELITE_GOLD,
            isRoundTrip = false,
            ticketFareSgd = 4_200
        )
        assertTrue(preview.distanceMiles in 3200..3400)
        assertEquals((preview.distanceMiles * 1.5).roundToInt(), preview.baseMiles)
        assertEquals((preview.distanceMiles * 0.25).roundToInt(), preview.tierBonusMiles)
        assertEquals(4_200, preview.ppsValueSgd)
    }

    @Test
    fun awardChart_calculatesSaverAndSpontaneousEscapes() {
        val sin = KrisFlyerNetwork.byCode("SIN")
        val nrt = KrisFlyerNetwork.byCode("NRT")
        val saver = KrisFlyerCalculator.calculateAwardQuote(
            origin = sin,
            destination = nrt,
            cabin = AwardCabin.BUSINESS,
            awardType = AwardType.SAVER,
            isRoundTrip = false,
            tier = KrisFlyerTier.KRISFLYER
        )
        val escapes = KrisFlyerCalculator.calculateAwardQuote(
            origin = sin,
            destination = nrt,
            cabin = AwardCabin.BUSINESS,
            awardType = AwardType.SPONTANEOUS_ESCAPES,
            isRoundTrip = false,
            tier = KrisFlyerTier.KRISFLYER
        )
        assertEquals(52_000, saver.milesRequired)
        assertTrue(escapes.milesRequired < saver.milesRequired)
    }

    @Test
    fun flightEarn_calculatesScootSpendAndEliteAccelerator() {
        val sin = KrisFlyerNetwork.byCode("SIN")
        val sgn = KrisFlyerNetwork.byCode("SGN")
        val preview = KrisFlyerCalculator.calculateFlightEarn(
            origin = sin,
            destination = sgn,
            cabin = CabinFareClass.ECONOMY_STANDARD,
            tier = KrisFlyerTier.ELITE_SILVER,
            isRoundTrip = true,
            ticketFareSgd = 400,
            carrier = com.example.domain.AirlineCarrier.SCOOT
        )
        assertEquals(400, preview.baseMiles)
        assertEquals(100, preview.tierBonusMiles)
        assertEquals(500, preview.totalKrisFlyerMiles)
        assertEquals(1_250, preview.eliteMilesEarned)
        assertEquals(0, preview.ppsValueSgd)
    }
}
