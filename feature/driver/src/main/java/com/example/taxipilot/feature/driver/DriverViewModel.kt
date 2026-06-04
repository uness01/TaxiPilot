package com.example.taxipilot.feature.driver

// ViewModel de l'espace Chauffeur — gère tout le cycle de vie d'une course et les données du chauffeur.
//
// Responsabilités principales :
//   1. Statut de service (en_service / hors_service / en_course)
//      - observeStatut() : maintient _statut synchronisé avec Firestore en temps réel
//      - toggleStatut() : bascule en_service ↔ hors_service, notifie le propriétaire, logue
//   2. Réservations disponibles → filtres selon statut (vide si hors_service)
//   3. Cycle de vie d'une course : accepterReservation() → demarrerCourse() → terminerCourse()
//   4. Partage de position GPS : onLocationUpdate() (toutes les ~15 s) + stopLocationSharing()
//   5. Résumé quotidien : combine myCourses (Firestore) + myCharges (Room)
//   6. Commandes vocales : onVoiceResult() → VoiceParser → confirmPending() → DB
//   7. Saisie manuelle : saveTrajetManuel() + saveChargeManuel()
//
// Vérification de proximité dans terminerCourse() :
//   Si le chauffeur est à plus de 500 m de la destination → TerminerState.TooFar
//   L'écran peut forcer la terminaison (forceTerminate = true).

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taxipilot.core.data.database.entity.*
import com.example.taxipilot.core.data.database.entity.TaxiStatut
import com.example.taxipilot.core.data.firestore.FirestoreCharge
import com.example.taxipilot.core.data.firestore.FirestoreNotification
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import com.example.taxipilot.core.data.firestore.FirestoreServiceLog
import com.example.taxipilot.core.data.firestore.FirestoreUser
import com.example.taxipilot.core.data.maps.OsrmRepository
import com.example.taxipilot.core.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// ── Types d'état UI ───────────────────────────────────────────────────────────

// Résumé quotidien du chauffeur (onglet Résumé)
data class DailySummaryUiState(
    val firestoreCourses: List<FirestoreReservation> = emptyList(), // courses terminées aujourd'hui
    val charges: List<ChargeEntity> = emptyList(),                  // dépenses du jour
    val totalRecettes: Double = 0.0,                                // CA du jour
    val totalCharges: Double = 0.0,                                 // dépenses du jour
    val benefice: Double = 0.0,                                     // bénéfice = recettes - charges
    val nbCourses: Int = 0                                          // nombre de courses terminées
)

// État de l'interface vocale
sealed class VoiceUiState {
    object Idle : VoiceUiState()                                            // aucune commande en cours
    data class PendingConfirmation(val command: VoiceParser.VoiceCommand) : VoiceUiState() // attente confirmation
    data class Saved(val message: String) : VoiceUiState()                 // enregistrement réussi
    data class Error(val message: String) : VoiceUiState()                 // erreur
}

// ── ViewModel ────────────────────────────────────────────────────────────────

class DriverViewModel(
    private val trajetRepository: TrajetRepository,
    private val chargeRepository: ChargeRepository,
    private val taxiRepository: TaxiRepository,
    private val firestoreReservationRepository: FirestoreReservationRepository,
    private val locationRepository: LocationRepository,
    private val firestoreUserRepository: FirestoreUserRepository,
    private val firestoreChargeRepository: FirestoreChargeRepository = FirestoreChargeRepository(),
    private val firestoreNotificationRepository: FirestoreNotificationRepository = FirestoreNotificationRepository(),
    private val firestoreServiceLogRepository: FirestoreServiceLogRepository = FirestoreServiceLogRepository(),
    val chauffeurId: Long,      // ID Room du chauffeur (dérivé de l'UID Firebase via toRoomId())
    val chauffeurUid: String,   // UID Firebase du chauffeur (utilisé pour Firestore)
    val chauffeurNom: String = "",
    val proprietaireId: String? = null,     // UID du propriétaire (null si non lié)
    val assignedTaxi: String? = null,       // matricule du taxi assigné
    private val initialStatut: String = FirestoreUser.STATUT_EN_SERVICE
) : ViewModel() {

    // ── Statut de service ─────────────────────────────────────────────────────

    // Initialise le statut : traite l'ancien "disponible" comme "en_service"
    private val _statut = MutableStateFlow(
        if (initialStatut == FirestoreUser.STATUT_DISPONIBLE) FirestoreUser.STATUT_EN_SERVICE
        else initialStatut
    )
    val statut: StateFlow<String> = _statut.asStateFlow()

    private val _proprietaireInfo = MutableStateFlow<FirestoreUser?>(null)
    val proprietaireInfo: StateFlow<FirestoreUser?> = _proprietaireInfo.asStateFlow()

    init {
        // Écoute le statut Firestore en temps réel → corrige le bug "statut figé sur hors_service"
        viewModelScope.launch {
            firestoreUserRepository.observeStatut(chauffeurUid).collect { firestoreStatut ->
                _statut.value = if (firestoreStatut == FirestoreUser.STATUT_DISPONIBLE)
                    FirestoreUser.STATUT_EN_SERVICE else firestoreStatut
            }
        }
        // Charge les infos du propriétaire pour l'afficher dans l'écran de profil
        if (proprietaireId != null) {
            viewModelScope.launch {
                _proprietaireInfo.value = firestoreUserRepository.getUser(proprietaireId)
            }
        }
        // Pré-sélectionne le taxi assigné (pour associer les charges et trajets)
        if (assignedTaxi != null) {
            viewModelScope.launch {
                val taxi = taxiRepository.getByImmatriculation(assignedTaxi)
                if (taxi != null) _selectedTaxiId.value = taxi.id
            }
        }
    }

    // Bascule entre en_service ↔ hors_service
    // Effets : mise à jour Firestore, mise à jour Room taxi, log, notification propriétaire
    fun toggleStatut() {
        val next = if (_statut.value == FirestoreUser.STATUT_HORS_SERVICE)
            FirestoreUser.STATUT_EN_SERVICE
        else
            FirestoreUser.STATUT_HORS_SERVICE
        viewModelScope.launch {
            // 1. Firestore d'abord → le snapshot listener confirmera via _statut
            firestoreUserRepository.updateStatut(chauffeurUid, next)
            // 2. Mise à jour optimiste locale pour une UI instantanée
            _statut.value = next

            // 3. Met à jour le statut Room du taxi (libre ou assigné)
            assignedTaxi?.let { matricule ->
                val taxi = taxiRepository.getByImmatriculation(matricule)
                if (taxi != null) {
                    val newTaxiStatut = if (next == FirestoreUser.STATUT_HORS_SERVICE)
                        TaxiStatut.DISPONIBLE else TaxiStatut.ASSIGNE
                    taxiRepository.updateStatut(taxi.id, newTaxiStatut)
                }
            }

            // 4. Enregistre le changement dans les logs de service
            if (proprietaireId != null) {
                firestoreServiceLogRepository.log(
                    FirestoreServiceLog(
                        chauffeurUid   = chauffeurUid,
                        proprietaireId = proprietaireId,
                        statut         = next,
                        timestamp      = System.currentTimeMillis()
                    )
                )

                // 5. Notifie le propriétaire du changement de statut
                val statusLabel = if (next == FirestoreUser.STATUT_EN_SERVICE)
                    "en service" else "hors service"
                firestoreNotificationRepository.create(
                    FirestoreNotification(
                        type       = FirestoreNotification.TYPE_STATUT_CHANGE,
                        message    = "${chauffeurNom.ifBlank { "Chauffeur" }} est maintenant $statusLabel",
                        targetRole = FirestoreNotification.ROLE_PROPRIETAIRE,
                        targetUid  = proprietaireId,
                        createdAt  = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Met à jour le statut de service sans les notifications/logs (usage interne)
    private fun setStatut(newStatut: String) {
        _statut.value = newStatut
        viewModelScope.launch { firestoreUserRepository.updateStatut(chauffeurUid, newStatut) }
    }

    // ── Position GPS (pour vérification de proximité à la fin) ───────────────

    private var lastLat: Double? = null
    private var lastLng: Double? = null

    // ── Sélection du taxi local (Room) ────────────────────────────────────────

    val allTaxis: StateFlow<List<TaxiEntity>> = taxiRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedTaxiId = MutableStateFlow<Long?>(null)
    val selectedTaxiId: StateFlow<Long?> = _selectedTaxiId.asStateFlow()

    // Sélectionne manuellement un taxi (pour les chauffeurs sans taxi assigné par Firestore)
    fun selectTaxi(id: Long) { _selectedTaxiId.value = id }

    // ── Flux de réservations Firestore ────────────────────────────────────────

    // Réservations disponibles (EN_ATTENTE) — liste vide si hors_service
    val availableReservations: StateFlow<List<FirestoreReservation>> =
        _statut.flatMapLatest { currentStatut ->
            if (currentStatut == FirestoreUser.STATUT_HORS_SERVICE) {
                flowOf(emptyList()) // pas de courses affichées quand hors service
            } else {
                firestoreReservationRepository.getAvailable()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Toutes les réservations du chauffeur (historique + course active)
    val myCourses: StateFlow<List<FirestoreReservation>> =
        firestoreReservationRepository.getByChauffeur(chauffeurUid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // true si une course est en cours (ACCEPTEE ou EN_COURS)
    val hasActiveReservation: StateFlow<Boolean> =
        myCourses.map { list ->
            list.any { it.status == FirestoreReservation.STATUS_ACCEPTEE ||
                       it.status == FirestoreReservation.STATUS_EN_COURS }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // La réservation actuellement EN_COURS (null si aucune)
    val activeReservation: StateFlow<FirestoreReservation?> =
        myCourses.map { list ->
            list.firstOrNull { it.status == FirestoreReservation.STATUS_EN_COURS }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── État d'acceptation ────────────────────────────────────────────────────

    private val _acceptState = MutableStateFlow<AcceptState>(AcceptState.Idle)
    val acceptState: StateFlow<AcceptState> = _acceptState.asStateFlow()

    // Tente d'accepter une réservation (transaction Firestore — first-come-first-served)
    fun accepterReservation(reservationId: String) {
        if (_acceptState.value is AcceptState.Loading) return // évite le double-tap
        viewModelScope.launch {
            _acceptState.value = AcceptState.Loading
            val success = firestoreReservationRepository.accept(
                reservationId  = reservationId,
                chauffeurId    = chauffeurUid,
                chauffeurName  = chauffeurNom,
                proprietaireId = proprietaireId,
                taxiAssigned   = assignedTaxi
            )
            _acceptState.value = if (success) AcceptState.Success else AcceptState.Taken
        }
    }

    fun clearAcceptState() { _acceptState.value = AcceptState.Idle }

    // États possibles lors d'une tentative d'acceptation
    sealed class AcceptState {
        object Idle    : AcceptState()
        object Loading : AcceptState()
        object Success : AcceptState()
        object Taken   : AcceptState() // quelqu'un d'autre a accepté en premier
    }

    // ── État de terminaison ───────────────────────────────────────────────────

    private val _terminerState = MutableStateFlow<TerminerState>(TerminerState.Idle)
    val terminerState: StateFlow<TerminerState> = _terminerState.asStateFlow()

    fun clearTerminerState() { _terminerState.value = TerminerState.Idle }

    sealed class TerminerState {
        object Idle    : TerminerState()
        object Loading : TerminerState()
        object Success : TerminerState()
        data class TooFar(val distanceMeters: Int) : TerminerState() // chauffeur trop loin de la destination
        data class Error(val message: String) : TerminerState()
    }

    // ── Cycle de vie d'une course ─────────────────────────────────────────────

    // Démarre une course : statut Firestore → EN_COURS, statut local → en_course, statut taxi → EN_COURSE
    fun demarrerCourse(reservationId: String) {
        viewModelScope.launch {
            try {
                firestoreReservationRepository.demarrer(reservationId)
                setStatut(FirestoreUser.STATUT_EN_COURSE)
                assignedTaxi?.let { matricule ->
                    val taxi = taxiRepository.getByImmatriculation(matricule)
                    if (taxi != null) taxiRepository.updateStatut(taxi.id, TaxiStatut.EN_COURSE)
                }
            } catch (e: Exception) {
                Log.w("DriverVM", "demarrerCourse failed: ${e.message}")
            }
        }
    }

    // Termine une course :
    //   1. Vérification de proximité (500 m max) sauf forceTerminate
    //   2. Mise à jour Firestore (terminee + prixFinal + distanceReelle)
    //   3. Arrêt du partage de position
    //   4. Retour au statut en_service, taxi → ASSIGNE
    fun terminerCourse(
        reservation: FirestoreReservation,
        prixFinal: Double,
        distanceReelle: Double,
        forceTerminate: Boolean = false // true = ignorer la vérification de proximité
    ) {
        viewModelScope.launch {
            _terminerState.value = TerminerState.Loading

            // Vérification : chauffeur doit être à moins de 500 m de la destination
            val arrLat = reservation.arriveeLat
            val arrLng = reservation.arriveeLng
            if (!forceTerminate && arrLat != null && arrLng != null) {
                val cLat = lastLat
                val cLng = lastLng
                if (cLat != null && cLng != null) {
                    val dist = OsrmRepository.distanceBetween(cLat, cLng, arrLat, arrLng)
                    if (dist > 500.0) {
                        _terminerState.value = TerminerState.TooFar(dist.toInt())
                        return@launch
                    }
                }
            }

            try {
                firestoreReservationRepository.terminer(
                    reservation.id, prixFinal, distanceReelle
                )
                stopLocationSharing()
                setStatut(FirestoreUser.STATUT_EN_SERVICE) // retour en service
                assignedTaxi?.let { matricule ->
                    val taxi = taxiRepository.getByImmatriculation(matricule)
                    if (taxi != null) taxiRepository.updateStatut(taxi.id, TaxiStatut.ASSIGNE)
                }
                _terminerState.value = TerminerState.Success
            } catch (e: Exception) {
                Log.e("DriverVM", "terminerCourse failed: ${e.message}")
                _terminerState.value = TerminerState.Error(
                    "Erreur réseau. Vérifiez votre connexion et réessayez."
                )
            }
        }
    }

    // ── Partage de position GPS ───────────────────────────────────────────────

    // Met à jour la position GPS dans Firestore et LocationRepository (carte flotte)
    // Appelé par DriverScreen toutes les ~15 s via LocationTracker
    fun onLocationUpdate(lat: Double, lng: Double, destination: String) {
        lastLat = lat
        lastLng = lng
        viewModelScope.launch {
            locationRepository.updateLocation(
                chauffeurId  = chauffeurUid,
                chauffeurNom = chauffeurNom,
                lat          = lat,
                lng          = lng,
                destination  = destination
            )
            firestoreUserRepository.updateCurrentLocation(chauffeurUid, lat, lng)
        }
    }

    // Supprime la position GPS de Firestore (appelé quand la course se termine)
    fun stopLocationSharing() {
        viewModelScope.launch {
            locationRepository.removeLocation(chauffeurUid)
            firestoreUserRepository.clearCurrentLocation(chauffeurUid)
        }
    }

    // ── Résumé quotidien ──────────────────────────────────────────────────────

    // Charges du taxi sélectionné (ou toutes si aucun taxi sélectionné)
    val myCharges: StateFlow<List<ChargeEntity>> = combine(
        chargeRepository.getAll(), _selectedTaxiId
    ) { charges, taxiId ->
        if (taxiId != null) charges.filter { it.taxiId == taxiId } else charges
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Toutes les charges (pour l'onglet Historique, sans filtre de taxi)
    val allChargesHistory: StateFlow<List<ChargeEntity>> = chargeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Combine courses Firestore + charges Room pour calculer le bilan du jour
    val dailySummary: StateFlow<DailySummaryUiState> = combine(
        myCourses, myCharges
    ) { courses, charges ->
        val start = todayStart()
        val end   = todayEnd()

        // Courses terminées aujourd'hui (completedAt en priorité, createdAt en fallback)
        val todayCourses = courses.filter {
            it.status == FirestoreReservation.STATUS_TERMINEE &&
            (it.completedAt ?: it.createdAt) in start..end
        }
        val todayCharges = charges.filter { it.date in start..end }

        val recettes     = todayCourses.sumOf { it.prixFinal ?: it.prixEstime }
        val totalCharges = todayCharges.sumOf { it.montant }

        DailySummaryUiState(
            firestoreCourses = todayCourses,
            charges          = todayCharges,
            totalRecettes    = recettes,
            totalCharges     = totalCharges,
            benefice         = recettes - totalCharges,
            nbCourses        = todayCourses.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailySummaryUiState())

    // ── Machine à états vocale ────────────────────────────────────────────────

    private val _voiceUiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val voiceUiState: StateFlow<VoiceUiState> = _voiceUiState.asStateFlow()

    // Traite le texte reconnu par la reconnaissance vocale
    fun onVoiceResult(text: String) {
        val command = VoiceParser.parse(text)
        when (command) {
            is VoiceParser.VoiceCommand.NouveauTrajet,
            is VoiceParser.VoiceCommand.NouvelleCharge ->
                _voiceUiState.value = VoiceUiState.PendingConfirmation(command) // attente confirmation
            is VoiceParser.VoiceCommand.Reponse -> {
                val pending = _voiceUiState.value as? VoiceUiState.PendingConfirmation
                if (pending != null) {
                    if (command.confirmee) confirmPending(pending.command) // "oui" → enregistre
                    else _voiceUiState.value = VoiceUiState.Idle            // "non" → annule
                }
            }
            VoiceParser.VoiceCommand.Incompris ->
                _voiceUiState.value = VoiceUiState.Error(
                    "Commande non reconnue. Dites « trajet de Rabat à Casablanca 350 MAD »."
                )
        }
    }

    // Confirme une commande vocale en attente → enregistre en DB
    fun confirmPending(command: VoiceParser.VoiceCommand) {
        viewModelScope.launch {
            try {
                when (command) {
                    is VoiceParser.VoiceCommand.NouveauTrajet -> saveTrajet(command)
                    is VoiceParser.VoiceCommand.NouvelleCharge -> saveCharge(command)
                    else -> Unit
                }
            } catch (e: Exception) {
                _voiceUiState.value = VoiceUiState.Error("Erreur lors de l'enregistrement.")
            }
        }
    }

    fun dismissVoiceState() { _voiceUiState.value = VoiceUiState.Idle }
    fun onVoiceError(message: String) { _voiceUiState.value = VoiceUiState.Error(message) }

    // ── Saisie manuelle ───────────────────────────────────────────────────────

    private val _manualSaveState = MutableStateFlow<ManualSaveState>(ManualSaveState.Idle)
    val manualSaveState: StateFlow<ManualSaveState> = _manualSaveState.asStateFlow()

    // Enregistre un trajet manuellement (formulaire dans l'onglet Résumé)
    fun saveTrajetManuel(
        depart: String, arrivee: String,
        distanceKm: Double?, montant: Double?
    ) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                trajetRepository.insert(
                    TrajetEntity(
                        chauffeurId    = chauffeurId,
                        taxiId         = _selectedTaxiId.value,
                        adresseDepart  = depart,
                        adresseArrivee = arrivee,
                        montant        = montant,
                        dateDebut      = now, dateFin = now,
                        statut         = TrajetStatut.TERMINE
                    )
                )
                _manualSaveState.value = ManualSaveState.Success("Course enregistrée ✓")
            } catch (e: Exception) {
                _manualSaveState.value = ManualSaveState.Error("Erreur d'enregistrement")
            }
        }
    }

    // Enregistre une charge manuellement (formulaire) + la mirore dans Firestore
    fun saveChargeManuel(type: TypeCharge, description: String, montant: Double) {
        viewModelScope.launch {
            try {
                // Résolution du taxi : ID local ou lookup par matricule (gère la race condition d'init)
                val taxiId = _selectedTaxiId.value ?: assignedTaxi?.let {
                    taxiRepository.getByImmatriculation(it)?.id
                }
                if (taxiId == null) {
                    _manualSaveState.value = ManualSaveState.Error(
                        "Aucun véhicule assigné. Contactez votre propriétaire."
                    )
                    return@launch
                }
                if (_selectedTaxiId.value == null) _selectedTaxiId.value = taxiId // cache pour futures appels

                chargeRepository.insert(
                    ChargeEntity(taxiId = taxiId, type = type, description = description, montant = montant)
                )
                // Mirore dans Firestore pour que le propriétaire la voie sur son dashboard
                if (proprietaireId != null) {
                    firestoreChargeRepository.create(
                        FirestoreCharge(
                            chauffeurUid   = chauffeurUid,
                            proprietaireId = proprietaireId,
                            type           = type.name,
                            description    = description,
                            montant        = montant
                        )
                    )
                }
                _manualSaveState.value = ManualSaveState.Success("Charge enregistrée ✓")
            } catch (e: Exception) {
                _manualSaveState.value = ManualSaveState.Error("Erreur d'enregistrement")
            }
        }
    }

    fun clearManualSaveState() { _manualSaveState.value = ManualSaveState.Idle }

    sealed class ManualSaveState {
        object Idle : ManualSaveState()
        data class Success(val message: String) : ManualSaveState()
        data class Error(val message: String) : ManualSaveState()
    }

    // ── Enregistrement vocal interne ──────────────────────────────────────────

    private suspend fun saveTrajet(cmd: VoiceParser.VoiceCommand.NouveauTrajet) {
        val now = System.currentTimeMillis()
        trajetRepository.insert(
            TrajetEntity(
                chauffeurId    = chauffeurId,
                taxiId         = _selectedTaxiId.value,
                adresseDepart  = cmd.depart,
                adresseArrivee = cmd.arrivee,
                montant        = cmd.montant,
                dateDebut      = now, dateFin = now,
                statut         = TrajetStatut.TERMINE
            )
        )
        val msg = buildString {
            append("Trajet enregistré, de ${cmd.depart} à ${cmd.arrivee}")
            cmd.montant?.let { append(", ${it.toLong()} MAD") }
        }
        _voiceUiState.value = VoiceUiState.Saved(msg)
    }

    private suspend fun saveCharge(cmd: VoiceParser.VoiceCommand.NouvelleCharge) {
        val taxiId = _selectedTaxiId.value ?: assignedTaxi?.let {
            taxiRepository.getByImmatriculation(it)?.id
        }
        if (taxiId == null) {
            _voiceUiState.value = VoiceUiState.Error(
                "Aucun véhicule assigné. Contactez votre propriétaire."
            )
            return
        }
        if (_selectedTaxiId.value == null) _selectedTaxiId.value = taxiId
        val montant = cmd.montant ?: 0.0
        chargeRepository.insert(
            ChargeEntity(
                taxiId = taxiId, type = cmd.type,
                description = cmd.description, montant = montant
            )
        )
        if (proprietaireId != null) {
            firestoreChargeRepository.create(
                FirestoreCharge(
                    chauffeurUid   = chauffeurUid,
                    proprietaireId = proprietaireId,
                    type           = cmd.type.name,
                    description    = cmd.description,
                    montant        = montant
                )
            )
        }
        val msg = buildString {
            append("Charge enregistrée, ${cmd.description}")
            cmd.montant?.let { append(", ${it.toLong()} MAD") }
        }
        _voiceUiState.value = VoiceUiState.Saved(msg)
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    // Timestamp de minuit du jour courant
    private fun todayStart(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Timestamp de 23:59:59.999 du jour courant
    private fun todayEnd(): Long = todayStart() + 86_400_000L - 1L

    // ── Factory manuelle (pas de Hilt) ────────────────────────────────────────
    class Factory(
        private val trajetRepository: TrajetRepository,
        private val chargeRepository: ChargeRepository,
        private val taxiRepository: TaxiRepository,
        private val firestoreReservationRepository: FirestoreReservationRepository,
        private val locationRepository: LocationRepository,
        private val firestoreUserRepository: FirestoreUserRepository,
        private val chauffeurId: Long,
        private val chauffeurUid: String,
        private val chauffeurNom: String = "",
        private val proprietaireId: String? = null,
        private val assignedTaxi: String? = null,
        private val initialStatut: String = FirestoreUser.STATUT_EN_SERVICE
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DriverViewModel(
                trajetRepository = trajetRepository,
                chargeRepository = chargeRepository,
                taxiRepository = taxiRepository,
                firestoreReservationRepository = firestoreReservationRepository,
                locationRepository = locationRepository,
                firestoreUserRepository = firestoreUserRepository,
                chauffeurId = chauffeurId,
                chauffeurUid = chauffeurUid,
                chauffeurNom = chauffeurNom,
                proprietaireId = proprietaireId,
                assignedTaxi = assignedTaxi,
                initialStatut = initialStatut
            ) as T
    }
}
