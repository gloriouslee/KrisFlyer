package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simulator_profile")
data class SimulatorProfileEntity(
    @PrimaryKey val id: Int = 1,
    val memberName: String,
    val membershipNumber: String,
    val krisFlyerMiles: Int,
    val eliteMiles: Int,
    val ppsValueSgd: Int,
    val expiringMiles: Int,
    val expiringDateLabel: String,
    val languageCode: String = "en",
    val darkModeOverride: Boolean? = null
)

@Entity(tableName = "mileage_transactions")
data class MileageTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleVi: String,
    val titleEn: String,
    val subtitleVi: String,
    val subtitleEn: String,
    val category: String, // FLIGHT, PARTNER, REDEMPTION, BONUS, REFUND
    val krisFlyerMilesDelta: Int,
    val eliteMilesDelta: Int,
    val ppsValueDeltaSgd: Int,
    val badgeCode: String,
    val timestampMillis: Long
)

@Entity(tableName = "award_bookings")
data class AwardBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pnr: String,
    val flightNumber: String,
    val originCode: String,
    val destinationCode: String,
    val originCity: String,
    val destinationCity: String,
    val cabinLabel: String,
    val awardTypeLabel: String,
    val milesCost: Int,
    val taxesSgd: Int,
    val seatNumber: String,
    val departureDateLabel: String,
    val departureTimeLabel: String,
    val gate: String,
    val status: String, // CONFIRMED, WAITLIST, CHECKED_IN, COMPLETED, CANCELLED
    val isRoundTrip: Boolean,
    val createdAtMillis: Long
)
