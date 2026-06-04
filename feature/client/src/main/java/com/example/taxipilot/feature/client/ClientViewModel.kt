package com.example.taxipilot.feature.client

// ViewModel de l'espace Client (passager) — gère la réservation de courses.
//
// Fonctionnalités :
//   1. Flux temps réel des réservations du client (myReservations) depuis Firestore
//   2. activeReservation : la course non terminée la plus récente (null si aucune)
//   3. estimerPrix() : calcul du prix estimé selon la distance et le type (immédiate +20%)
//   4. createReservation() : crée la réservation dans Firestore + notifie les chauffeurs via FCM
//   5. cancelReservation() : annule une réservation
//
// Prix : 3.50 MAD/km, minimum 15 MAD, majoration de 20% pour les courses immédiates.
// Les coordonnées GPS sont passées lors de la création pour permettre la vérification
// de proximité quand le chauffeur veut terminer la course.

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
    val clientUid: String,                // UID Firebase du client connecté
    val initialNom: String = "",          // nom pré-rempli dans le formulaire de réservation
    val initialTelephone: String = "",    // téléphone pré-rempli
    private val onNotifyNewReservation: (suspend (depart: String, arrivee: String) -> Unit)? = null
    // callback pour envoyer les notifications FCM aux chauffeurs après réservation
) : ViewModel() {

    // ── Flux de réservations en temps réel ────────────────────────────────────

    // Toutes les réservations du client (flux Firestore)
    val myReservations: StateFlow<List<FirestoreReservation>> =
        reservationRepo.getByClient(clientUid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // La réservation active (non terminée / non annulée)
    val activeReservation: StateFlow<FirestoreReservation?> =
        myReservations.map { list ->
            list.firstOrNull { !it.isFinished }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── État de réservation ───────────────────────────────────────────────────

    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Idle)
    val bookingState: StateFlow<BookingState> = _bookingState.asStateFlow()

    // ── Estimation du prix ────────────────────────────────────────────────────

    // Tarif : 3.50 MAD/km, minimum 15 MAD
    // Majoration immédiate : +20% pour une course demandée maintenant
    fun estimerPrix(distanceKm: Double, type: String): Double {
        val base = maxOf(15.0, distanceKm * 3.50)
        return if (type == FirestoreReservation.TYPE_IMMEDIATE) base * 1.20 else base
    }

    // ── Actions de réservation ────────────────────────────────────────────────

    // Crée une réservation dans Firestore, puis notifie les chauffeurs disponibles via FCM
    fun createReservation(
        clientNom: String,
        clientTelephone: String,
        adresseDepart: String,
        adresseArrivee: String,
        scheduledTime: Long?,      // heure planifiée (null pour immédiate)
        type: String,              // "immediate" ou "planifiee"
        prixEstime: Double,
        departLat: Double? = null, // coordonnées GPS pour la vérification de proximité
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
                // Notification FCM aux chauffeurs en service (best-effort — n'est pas bloquant)
                runCatching {
                    onNotifyNewReservation?.invoke(adresseDepart, adresseArrivee)
                }
            } catch (e: Exception) {
                _bookingState.value = BookingState.Error("Erreur lors de la réservation.")
            }
        }
    }

    // Annule une réservation (statut → ANNULEE dans Firestore)
    fun cancelReservation(reservationId: String) {
        viewModelScope.launch { reservationRepo.cancel(reservationId) }
    }

    // Remet l'état de réservation à Idle (après affichage du succès ou de l'erreur)
    fun resetBookingState() { _bookingState.value = BookingState.Idle }

    // ── États possibles de réservation ───────────────────────────────────────

    sealed class BookingState {
        data object Idle    : BookingState()
        data object Loading : BookingState()
        data class  Success(val reservationId: String) : BookingState()
        data class  Error(val message: String)         : BookingState()
    }

    // ── Factory manuelle (pas de Hilt) ────────────────────────────────────────

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
