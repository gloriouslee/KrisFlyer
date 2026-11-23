package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.AwardBookingDoc
import com.example.data.remote.KrisFlyerProfileDoc
import com.example.data.remote.MileageTransactionDoc
import com.example.data.repository.FirestoreKrisFlyerRepository
import com.example.data.repository.TierPreset
import com.example.domain.AirlineCarrier
import com.example.domain.Airport
import com.example.domain.AppLanguage
import com.example.domain.AwardCabin
import com.example.domain.AwardQuote
import com.example.domain.AwardType
import com.example.domain.CabinFareClass
import com.example.domain.FlightEarnPreview
import com.example.domain.KrisFlyerCalculator
import com.example.domain.KrisFlyerNetwork
import com.example.domain.KrisFlyerTier
import com.example.domain.determineTier
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EarnSimulatorState(
    val carrier: AirlineCarrier = AirlineCarrier.SINGAPORE_AIRLINES,
    val origin: Airport = KrisFlyerNetwork.byCode("SGN"),
    val destination: Airport = KrisFlyerNetwork.byCode("NRT"),
    val cabin: CabinFareClass = CabinFareClass.BUSINESS,
    val isRoundTrip: Boolean = true,
    val ticketFareSgd: Int = 4_600
)

data class RedeemSimulatorState(
    val origin: Airport = KrisFlyerNetwork.byCode("SIN"),
    val destination: Airport = KrisFlyerNetwork.byCode("NRT"),
    val cabin: AwardCabin = AwardCabin.BUSINESS,
    val awardType: AwardType = AwardType.SAVER,
    val isRoundTrip: Boolean = false
)

data class KrisFlyerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userEmail: String? = null,
    val profile: KrisFlyerProfileDoc? = null,
    val currentTier: KrisFlyerTier = KrisFlyerTier.KRISFLYER,
    val language: AppLanguage = AppLanguage.EN,
    val transactions: List<MileageTransactionDoc> = emptyList(),
    val bookings: List<AwardBookingDoc> = emptyList(),
    val earnState: EarnSimulatorState = EarnSimulatorState(),
    val earnPreview: FlightEarnPreview = KrisFlyerCalculator.calculateFlightEarn(
        KrisFlyerNetwork.byCode("SGN"),
        KrisFlyerNetwork.byCode("NRT"),
        CabinFareClass.BUSINESS,
        KrisFlyerTier.KRISFLYER,
        true,
        4_600
    ),
    val redeemState: RedeemSimulatorState = RedeemSimulatorState(),
    val awardQuote: AwardQuote = KrisFlyerCalculator.calculateAwardQuote(
        KrisFlyerNetwork.byCode("SIN"),
        KrisFlyerNetwork.byCode("NRT"),
        AwardCabin.BUSINESS,
        AwardType.SAVER,
        false,
        KrisFlyerTier.KRISFLYER
    ),
    val celebratedUpgradeTier: KrisFlyerTier? = null,
    val snackbarMessage: String? = null
)

class KrisFlyerViewModel(
    private val repository: FirestoreKrisFlyerRepository,
    private val currentUserId: String,
    private val userDisplayName: String?,
    private val userEmail: String?
) : ViewModel() {

    private val earnFlow = MutableStateFlow(EarnSimulatorState())
    private val redeemFlow = MutableStateFlow(RedeemSimulatorState())
    private val celebratedUpgradeFlow = MutableStateFlow<KrisFlyerTier?>(null)
    private val snackbarFlow = MutableStateFlow<String?>(null)
    private val streamErrorFlow = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            val seedResult = repository.ensureUserSeeded(currentUserId, userDisplayName)
            seedResult.onFailure { err ->
                streamErrorFlow.value = err.localizedMessage ?: "Failed to initialize cloud profile"
            }
        }
    }

    val uiState: StateFlow<KrisFlyerUiState> = combine(
        repository.observeProfile(currentUserId).catch { error ->
            handleStreamError(error, "profile")
            emit(null)
        },
        repository.observeTransactions(currentUserId).catch { error ->
            handleStreamError(error, "transactions")
            emit(emptyList())
        },
        repository.observeBookings(currentUserId).catch { error ->
            handleStreamError(error, "bookings")
            emit(emptyList())
        },
        earnFlow,
        redeemFlow,
        celebratedUpgradeFlow,
        snackbarFlow,
        streamErrorFlow
    ) { values ->
        val profile = values[0] as KrisFlyerProfileDoc?
        @Suppress("UNCHECKED_CAST")
        val transactions = values[1] as List<MileageTransactionDoc>
        @Suppress("UNCHECKED_CAST")
        val bookings = values[2] as List<AwardBookingDoc>
        val earnState = values[3] as EarnSimulatorState
        val redeemState = values[4] as RedeemSimulatorState
        val celebratedTier = values[5] as KrisFlyerTier?
        val snackbar = values[6] as String?
        val streamError = values[7] as String?

        val tier = if (profile != null) {
            determineTier(profile.eliteMiles.toInt(), profile.ppsValueSgd.toInt())
        } else {
            KrisFlyerTier.KRISFLYER
        }
        val lang = AppLanguage.fromCode(profile?.languageCode ?: "en")

        val earnPreview = KrisFlyerCalculator.calculateFlightEarn(
            origin = earnState.origin,
            destination = earnState.destination,
            cabin = earnState.cabin,
            tier = tier,
            isRoundTrip = earnState.isRoundTrip,
            ticketFareSgd = earnState.ticketFareSgd,
            carrier = earnState.carrier
        )
        val awardQuote = KrisFlyerCalculator.calculateAwardQuote(
            origin = redeemState.origin,
            destination = redeemState.destination,
            cabin = redeemState.cabin,
            awardType = redeemState.awardType,
            isRoundTrip = redeemState.isRoundTrip,
            tier = tier
        )

        KrisFlyerUiState(
            isLoading = profile == null && streamError == null,
            errorMessage = streamError,
            userEmail = userEmail,
            profile = profile,
            currentTier = tier,
            language = lang,
            transactions = transactions,
            bookings = bookings,
            earnState = earnState,
            earnPreview = earnPreview,
            redeemState = redeemState,
            awardQuote = awardQuote,
            celebratedUpgradeTier = celebratedTier,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = KrisFlyerUiState(userEmail = userEmail)
    )

    private fun handleStreamError(error: Throwable, source: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing $source", rootError)
        streamErrorFlow.value = rootError.message ?: "Failed to sync $source"
    }

    fun updateEarnCarrier(carrier: AirlineCarrier) {
        earnFlow.update { current ->
            val fare = if (carrier == AirlineCarrier.SCOOT) {
                if (current.isRoundTrip) 480 else 240
            } else {
                KrisFlyerCalculator.estimateTicketFareSgd(
                    current.origin, current.destination, current.cabin, current.isRoundTrip
                )
            }
            current.copy(carrier = carrier, ticketFareSgd = fare)
        }
    }

    fun updateEarnRoute(origin: Airport? = null, destination: Airport? = null) {
        earnFlow.update { current ->
            val newOrigin = origin ?: current.origin
            val newDest = destination ?: current.destination
            val estimatedFare = if (current.carrier == AirlineCarrier.SCOOT) {
                current.ticketFareSgd
            } else {
                KrisFlyerCalculator.estimateTicketFareSgd(
                    newOrigin, newDest, current.cabin, current.isRoundTrip
                )
            }
            current.copy(origin = newOrigin, destination = newDest, ticketFareSgd = estimatedFare)
        }
    }

    fun swapEarnAirports() {
        earnFlow.update { current ->
            current.copy(origin = current.destination, destination = current.origin)
        }
    }

    fun updateEarnCabin(cabin: CabinFareClass) {
        earnFlow.update { current ->
            val estimatedFare = KrisFlyerCalculator.estimateTicketFareSgd(
                current.origin, current.destination, cabin, current.isRoundTrip
            )
            current.copy(cabin = cabin, ticketFareSgd = estimatedFare)
        }
    }

    fun updateEarnRoundTrip(isRoundTrip: Boolean) {
        earnFlow.update { current ->
            val estimatedFare = if (current.carrier == AirlineCarrier.SCOOT) {
                if (isRoundTrip) 480 else 240
            } else {
                KrisFlyerCalculator.estimateTicketFareSgd(
                    current.origin, current.destination, current.cabin, isRoundTrip
                )
            }
            current.copy(isRoundTrip = isRoundTrip, ticketFareSgd = estimatedFare)
        }
    }

    fun updateEarnFareSgd(fareSgd: Int) {
        earnFlow.update { it.copy(ticketFareSgd = fareSgd.coerceIn(50, 35_000)) }
    }

    fun simulateFlightEarn() {
        val state = earnFlow.value
        val lang = uiState.value.language
        if (state.origin.code == state.destination.code) {
            snackbarFlow.value = if (lang == AppLanguage.VI) {
                "Điểm đi và điểm đến phải khác nhau"
            } else {
                "Origin and destination must differ"
            }
            return
        }
        viewModelScope.launch {
            repository.simulateFlight(
                userId = currentUserId,
                origin = state.origin,
                destination = state.destination,
                cabin = state.cabin,
                isRoundTrip = state.isRoundTrip,
                ticketFareSgd = state.ticketFareSgd,
                carrier = state.carrier
            ).onSuccess { result ->
                if (result.newTier.ordinal > result.oldTier.ordinal) {
                    celebratedUpgradeFlow.value = result.newTier
                }
                snackbarFlow.value = if (lang == AppLanguage.VI) {
                    "Đã cộng +${formatNumber(result.milesDelta)} dặm KrisFlyer từ chuyến bay mô phỏng!"
                } else {
                    "Credited +${formatNumber(result.milesDelta)} KrisFlyer miles from simulated flight!"
                }
            }.onFailure { err ->
                snackbarFlow.value = err.localizedMessage ?: "Failed to simulate flight"
            }
        }
    }

    fun simulatePartnerEarn(
        titleVi: String,
        titleEn: String,
        subtitleVi: String,
        subtitleEn: String,
        badgeCode: String,
        miles: Int
    ) {
        val lang = uiState.value.language
        viewModelScope.launch {
            repository.simulatePartnerEarn(
                currentUserId, titleVi, titleEn, subtitleVi, subtitleEn, badgeCode, miles
            ).onSuccess {
                snackbarFlow.value = if (lang == AppLanguage.VI) {
                    "Đã cộng +${formatNumber(miles)} dặm từ $titleVi"
                } else {
                    "Credited +${formatNumber(miles)} miles from $titleEn"
                }
            }.onFailure { err ->
                snackbarFlow.value = err.localizedMessage ?: "Partner earn failed"
            }
        }
    }

    fun updateRedeemRoute(origin: Airport? = null, destination: Airport? = null) {
        redeemFlow.update { current ->
            current.copy(
                origin = origin ?: current.origin,
                destination = destination ?: current.destination
            )
        }
    }

    fun swapRedeemAirports() {
        redeemFlow.update { current ->
            current.copy(origin = current.destination, destination = current.origin)
        }
    }

    fun updateRedeemCabin(cabin: AwardCabin) {
        redeemFlow.update { it.copy(cabin = cabin) }
    }

    fun updateRedeemAwardType(awardType: AwardType) {
        redeemFlow.update { it.copy(awardType = awardType) }
    }

    fun updateRedeemRoundTrip(isRoundTrip: Boolean) {
        redeemFlow.update { it.copy(isRoundTrip = isRoundTrip) }
    }

    fun redeemAwardFlight(onSuccess: () -> Unit = {}) {
        val state = redeemFlow.value
        val current = uiState.value
        val lang = current.language
        if (state.origin.code == state.destination.code) {
            snackbarFlow.value = if (lang == AppLanguage.VI) {
                "Vui lòng chọn điểm đến khác điểm khởi hành"
            } else {
                "Please select a different destination airport"
            }
            return
        }
        val currentMiles = current.profile?.krisFlyerMiles ?: 0L
        if (currentMiles < current.awardQuote.milesRequired) {
            snackbarFlow.value = if (lang == AppLanguage.VI) {
                "Không đủ dặm KrisFlyer! Hãy vào mục Tích Dặm để mô phỏng thêm."
            } else {
                "Insufficient KrisFlyer miles! Simulate more flights or partner points first."
            }
            return
        }

        val forceWaitlist = !current.awardQuote.instantConfirmationLikely
        viewModelScope.launch {
            repository.redeemAwardFlight(
                userId = currentUserId,
                origin = state.origin,
                destination = state.destination,
                cabin = state.cabin,
                awardType = state.awardType,
                isRoundTrip = state.isRoundTrip,
                forceWaitlist = forceWaitlist
            ).onSuccess { result ->
                snackbarFlow.value = if (lang == AppLanguage.VI) {
                    "Xuất vé thưởng thành công (${result.pnrCreated})!"
                } else {
                    "Award ticket issued (${result.pnrCreated})!"
                }
                onSuccess()
            }.onFailure { err ->
                snackbarFlow.value = err.localizedMessage ?: "Redemption failed"
            }
        }
    }

    fun redeemLifestyleVoucher(
        titleVi: String,
        titleEn: String,
        subtitleVi: String,
        subtitleEn: String,
        badgeCode: String,
        milesCost: Int
    ) {
        val lang = uiState.value.language
        viewModelScope.launch {
            repository.redeemLifestyleVoucher(
                currentUserId, titleVi, titleEn, subtitleVi, subtitleEn, badgeCode, milesCost
            ).onSuccess {
                snackbarFlow.value = if (lang == AppLanguage.VI) {
                    "Đổi thành công: $titleVi (-${formatNumber(milesCost)} dặm)"
                } else {
                    "Redeemed: $titleEn (-${formatNumber(milesCost)} miles)"
                }
            }.onFailure {
                snackbarFlow.value = if (lang == AppLanguage.VI) {
                    "Không đủ dặm KrisFlyer để đổi ưu đãi này"
                } else {
                    "Not enough KrisFlyer miles for this reward"
                }
            }
        }
    }

    fun advanceBookingStatus(booking: AwardBookingDoc) {
        val lang = uiState.value.language
        viewModelScope.launch {
            repository.advanceBookingStatus(currentUserId, booking.id, booking.status)
                .onSuccess {
                    snackbarFlow.value = if (lang == AppLanguage.VI) {
                        "Đã cập nhật trạng thái chuyến bay mô phỏng"
                    } else {
                        "Updated simulated booking status"
                    }
                }
                .onFailure { err ->
                    snackbarFlow.value = err.localizedMessage ?: "Failed to update booking"
                }
        }
    }

    fun cancelAndRefundBooking(booking: AwardBookingDoc) {
        val lang = uiState.value.language
        viewModelScope.launch {
            repository.cancelAndRefundBooking(currentUserId, booking)
                .onSuccess {
                    snackbarFlow.value = if (lang == AppLanguage.VI) {
                        "Đã hủy vé thưởng và hoàn lại 100% dặm KrisFlyer"
                    } else {
                        "Award booking cancelled & miles redeposited"
                    }
                }
                .onFailure { err ->
                    snackbarFlow.value = err.localizedMessage ?: "Failed to cancel booking"
                }
        }
    }

    fun applyPreset(preset: TierPreset) {
        val lang = uiState.value.language
        viewModelScope.launch {
            repository.applyPreset(currentUserId, preset)
                .onSuccess {
                    snackbarFlow.value = if (lang == AppLanguage.VI) {
                        "Đã chuyển trạng thái mô phỏng mới"
                    } else {
                        "Applied simulator tier preset"
                    }
                }
                .onFailure { err ->
                    snackbarFlow.value = err.localizedMessage ?: "Failed to apply preset"
                }
        }
    }

    fun updateMemberName(name: String) {
        viewModelScope.launch {
            repository.updateMemberName(currentUserId, name)
                .onFailure { err -> snackbarFlow.value = err.localizedMessage }
        }
    }

    fun toggleLanguage() {
        val next = if (uiState.value.language == AppLanguage.VI) "en" else "vi"
        viewModelScope.launch {
            repository.setLanguage(currentUserId, next)
        }
    }

    fun setDarkMode(darkMode: Boolean) {
        val mode = if (darkMode) "DARK" else "LIGHT"
        viewModelScope.launch {
            repository.setDarkMode(currentUserId, mode)
        }
    }

    fun dismissTierCelebration() {
        celebratedUpgradeFlow.value = null
    }

    fun clearSnackbar() {
        snackbarFlow.value = null
    }

    private companion object {
        const val TAG = "KrisFlyerVM"
    }
}

fun formatNumber(value: Int): String = "%,d".format(value)
fun formatNumber(value: Long): String = "%,d".format(value)
