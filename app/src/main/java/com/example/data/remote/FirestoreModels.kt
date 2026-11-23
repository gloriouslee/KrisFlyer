package com.example.data.remote

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class KrisFlyerProfileDoc(
    val userId: String = "",
    val memberName: String = "MINH ANH NGUYEN",
    val membershipNumber: String = "880 492 7150",
    val krisFlyerMiles: Long = 0L,
    val eliteMiles: Long = 0L,
    val ppsValueSgd: Long = 0L,
    val expiringMiles: Long = 0L,
    val expiringDateLabel: String = "31 MAR 2027",
    val languageCode: String = "en",
    val darkMode: String = "SYSTEM",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MileageTransactionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val titleVi: String = "",
    val titleEn: String = "",
    val subtitleVi: String = "",
    val subtitleEn: String = "",
    val category: String = "FLIGHT",
    val krisFlyerMilesDelta: Long = 0L,
    val eliteMilesDelta: Long = 0L,
    val ppsValueDeltaSgd: Long = 0L,
    val badgeCode: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AwardBookingDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val pnr: String = "",
    val flightNumber: String = "",
    val originCode: String = "",
    val destinationCode: String = "",
    val originCity: String = "",
    val destinationCity: String = "",
    val cabinLabel: String = "",
    val awardTypeLabel: String = "",
    val milesCost: Long = 0L,
    val taxesSgd: Long = 0L,
    val seatNumber: String = "",
    val departureDateLabel: String = "",
    val departureTimeLabel: String = "",
    val gate: String = "",
    val status: String = "CONFIRMED",
    val roundTrip: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
