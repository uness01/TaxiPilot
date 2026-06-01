package com.example.taxipilot.feature.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import com.example.taxipilot.core.data.repository.FirestoreReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientViewModel(
    private val reservationRepo: FirestoreReservationRepository,
    val clientUid: String,
    val initialNom: String = "",
    val initialTelephone: String = "",
    private val onNotifyNewReservation: (suspend (depart: String, arrivee: String) -> Unit)? = null
) : ViewModel() {

    // ── Real-time reservation streams ─────────────────────────────────────────

    val myReservations: StateFlow<List<FirestoreReservation>> =
        reservationRepo.getByClient(clientUid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Most recent reservation that is not yet finished. */
    val activeReservation: StateFlow<FirestoreReservation?> =
        myReservations.map { list ->
            list.firstOrNull { !it.isFinished }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Booking state ─────────────────────────────────────────────────────────

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState.asStateFlow()

    // ── Price estimation ──────────────────────────────────────────────────────

    /** 3.50 MAD/km, minimum 15 MAD, +20 % surcharge for immediate. */
    fun estimerPrix(distanceKm: Double, type: String): Double {
        val base = maxOf(15.0, distanceKm * 3.50)
        return if (type == FirestoreReservation.TYPE_IMMEDIATE) base * 1.20 else base
    }

    // ── Booking actions ───────────────────────────────────────────────────────

    fun createReservation(
        clientNom: String,
        clientTelephone: String,
        adresseDepart: String,
        adresseArrivee: String,
        scheduledTime: Long?,
        type: String,
        prixEstime: Double,
        departLat: Double? = null,
        departLng: Double? = null,
        arriveeLat: Double? = null,
        arriveeLng: Double? = null,
        distanceKm: Double = 0.0
    ) {
        viewModelScope.launch {
            _bookingState.value = BookingState.Loading
            try {
                val id = reservationRepo.create(
                    FirestoreReservation(
                        clientId      = clientUid,
                        clientName    = clientNom,
                        clientPhone   = clientTelephone,
                        depart        = adresseDepart,
                        arrivee       = adresseArrivee,
                        prixEstime    = prixEstime,
                        type          = type,
                        scheduledTime = scheduledTime,
                        status        = FirestoreReservation.STATUS_EN_ATTENTE,
                        departLat     = departLat,
                        departLng     = departLng,
                        arriveeLat    = arriveeLat,
                        arriveeLng    = arriveeLng,
                        distanceKm    = distanceKm
                    )
                )
                _bookingState.value = BookingState.Success(id)
                // Notify all chauffeurs via FCM (best-effort — never crash on failure)
                runCatching {
                    onNotifyNewReservation?.invoke(adresseDepart, adresseArrivee)
                }
            } catch (e: Exception) {
                _bookingState.value = BookingState.Error("Erreur lors de la réservation.")
            }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch { reservationRepo.cancel(reservationId) }
    }

    fun resetBookingState() { _bookingState.value = BookingState.Idle }

    // ── Booking state types ───────────────────────────────────────────────────

    sealed class BookingState {
        data object Idle    : BookingState()
        data object Loading : BookingState()
        data class  Success(val reservationId: String) : BookingState()
        data class  Error(val message: String)         : BookingState()
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val reservationRepo: FirestoreReservationRepository,
        private val clientUid: String,
        private val initialNom: String = "",
        private val initialTelephone: String = "",
        private val onNotifyNewReservation: (suspend (String, String) -> Unit)? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ClientViewModel(
                reservationRepo, clientUid, initialNom, initialTelephone,
                onNotifyNewReservation
            ) as T
    }
}
