package com.example.taxipilot.feature.driver

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

// ── UI state types ────────────────────────────────────────────────────────────

data class DailySummaryUiState(
    val firestoreCourses: List<FirestoreReservation> = emptyList(),
    val charges: List<ChargeEntity> = emptyList(),
    val totalRecettes: Double = 0.0,
    val totalCharges: Double = 0.0,
    val benefice: Double = 0.0,
    val nbCourses: Int = 0
)

sealed class VoiceUiState {
    object Idle : VoiceUiState()
    data class PendingConfirmation(val command: VoiceParser.VoiceCommand) : VoiceUiState()
    data class Saved(val message: String) : VoiceUiState()
    data class Error(val message: String) : VoiceUiState()
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
    val chauffeurId: Long,
    val chauffeurUid: String,
    val chauffeurNom: String = "",
    /** Firestore UID of the proprietaire this chauffeur is linked to. */
    val proprietaireId: String? = null,
    /** Matricule of the taxi assigned to this chauffeur by the proprietaire. */
    val assignedTaxi: String? = null,
    /** Current service status from Firestore profile. */
    private val initialStatut: String = FirestoreUser.STATUT_EN_SERVICE
) : ViewModel() {

    // ── Statut (en_service / hors_service / en_course) ────────────────────────

    private val _statut = MutableStateFlow(
        // Treat legacy "disponible" as en_service so old accounts work
        if (initialStatut == FirestoreUser.STATUT_DISPONIBLE) FirestoreUser.STATUT_EN_SERVICE
        else initialStatut
    )
    val statut: StateFlow<String> = _statut.asStateFlow()

    // Declared before init so it's ready when init block runs
    private val _proprietaireInfo = MutableStateFlow<FirestoreUser?>(null)
    val proprietaireInfo: StateFlow<FirestoreUser?> = _proprietaireInfo.asStateFlow()

    init {
        // Real-time Firestore snapshot listener — keeps _statut always in sync.
        // This replaces the one-shot getStatut() and fixes the "stuck on hors_service" bug.
        viewModelScope.launch {
            firestoreUserRepository.observeStatut(chauffeurUid).collect { firestoreStatut ->
                _statut.value = if (firestoreStatut == FirestoreUser.STATUT_DISPONIBLE)
                    FirestoreUser.STATUT_EN_SERVICE else firestoreStatut
            }
        }
        // Fetch proprietaire info for "Mon Employeur" section in the driver profile
        if (proprietaireId != null) {
            viewModelScope.launch {
                _proprietaireInfo.value = firestoreUserRepository.getUser(proprietaireId)
            }
        }
        // Auto-select the taxi assigned by proprietaire (for charge/trajet association)
        if (assignedTaxi != null) {
            viewModelScope.launch {
                val taxi = taxiRepository.getByImmatriculation(assignedTaxi)
                if (taxi != null) _selectedTaxiId.value = taxi.id
            }
        }
    }

    fun toggleStatut() {
        val next = if (_statut.value == FirestoreUser.STATUT_HORS_SERVICE)
            FirestoreUser.STATUT_EN_SERVICE
        else
            FirestoreUser.STATUT_HORS_SERVICE
        viewModelScope.launch {
            // 1. Update Firestore first — snapshot listener will confirm via _statut update
            firestoreUserRepository.updateStatut(chauffeurUid, next)
            // 2. Optimistic local update for immediate UI feedback
            _statut.value = next

            // 2. Update local Room taxi status (Fix 6 — free/re-bind taxi on chauffeur's device)
            assignedTaxi?.let { matricule ->
                val taxi = taxiRepository.getByImmatriculation(matricule)
                if (taxi != null) {
                    val newTaxiStatut = if (next == FirestoreUser.STATUT_HORS_SERVICE)
                        TaxiStatut.DISPONIBLE else TaxiStatut.ASSIGNE
                    taxiRepository.updateStatut(taxi.id, newTaxiStatut)
                }
            }

            // 3. Log status change (Fix 8)
            if (proprietaireId != null) {
                firestoreServiceLogRepository.log(
                    FirestoreServiceLog(
                        chauffeurUid   = chauffeurUid,
                        proprietaireId = proprietaireId,
                        statut         = next,
                        timestamp      = System.currentTimeMillis()
                    )
                )

                // 4. Notify proprietaire (Fix 7)
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

    private fun setStatut(newStatut: String) {
        _statut.value = newStatut
        viewModelScope.launch { firestoreUserRepository.updateStatut(chauffeurUid, newStatut) }
    }

    // ── Last known location (for proximity check) ─────────────────────────────

    private var lastLat: Double? = null
    private var lastLng: Double? = null

    // ── Taxi selection (local Room) ───────────────────────────────────────────

    val allTaxis: StateFlow<List<TaxiEntity>> = taxiRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedTaxiId = MutableStateFlow<Long?>(null)
    val selectedTaxiId: StateFlow<Long?> = _selectedTaxiId.asStateFlow()

    fun selectTaxi(id: Long) { _selectedTaxiId.value = id }

    // ── Firestore reservation flows ───────────────────────────────────────────

    // When hors_service: return empty list and skip the Firestore listener entirely.
    val availableReservations: StateFlow<List<FirestoreReservation>> =
        _statut.flatMapLatest { currentStatut ->
            if (currentStatut == FirestoreUser.STATUT_HORS_SERVICE) {
                flowOf(emptyList())
            } else {
                firestoreReservationRepository.getAvailable()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val myCourses: StateFlow<List<FirestoreReservation>> =
        firestoreReservationRepository.getByChauffeur(chauffeurUid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val hasActiveReservation: StateFlow<Boolean> =
        myCourses.map { list ->
            list.any { it.status == FirestoreReservation.STATUS_ACCEPTEE ||
                       it.status == FirestoreReservation.STATUS_EN_COURS }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val activeReservation: StateFlow<FirestoreReservation?> =
        myCourses.map { list ->
            list.firstOrNull { it.status == FirestoreReservation.STATUS_EN_COURS }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Accept state ──────────────────────────────────────────────────────────

    private val _acceptState = MutableStateFlow<AcceptState>(AcceptState.Idle)
    val acceptState: StateFlow<AcceptState> = _acceptState.asStateFlow()

    fun accepterReservation(reservationId: String) {
        if (_acceptState.value is AcceptState.Loading) return
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

    sealed class AcceptState {
        object Idle    : AcceptState()
        object Loading : AcceptState()
        object Success : AcceptState()
        object Taken   : AcceptState()
    }

    // ── Terminer state (tracks completion, surfaces errors) ───────────────────

    private val _terminerState = MutableStateFlow<TerminerState>(TerminerState.Idle)
    val terminerState: StateFlow<TerminerState> = _terminerState.asStateFlow()

    fun clearTerminerState() { _terminerState.value = TerminerState.Idle }

    sealed class TerminerState {
        object Idle    : TerminerState()
        object Loading : TerminerState()
        object Success : TerminerState()
        data class TooFar(val distanceMeters: Int) : TerminerState()
        data class Error(val message: String) : TerminerState()
    }

    // ── Trip lifecycle ────────────────────────────────────────────────────────

    fun demarrerCourse(reservationId: String) {
        viewModelScope.launch {
            try {
                firestoreReservationRepository.demarrer(reservationId)
                setStatut(FirestoreUser.STATUT_EN_COURSE)
                // Best-effort: update local Room taxi to EN_COURSE (no-op on chauffeur device if taxi not in Room)
                assignedTaxi?.let { matricule ->
                    val taxi = taxiRepository.getByImmatriculation(matricule)
                    if (taxi != null) taxiRepository.updateStatut(taxi.id, TaxiStatut.EN_COURSE)
                }
            } catch (e: Exception) {
                Log.w("DriverVM", "demarrerCourse failed: ${e.message}")
            }
        }
    }

    fun terminerCourse(
        reservation: FirestoreReservation,
        prixFinal: Double,
        distanceReelle: Double,
        forceTerminate: Boolean = false
    ) {
        viewModelScope.launch {
            _terminerState.value = TerminerState.Loading

            // Proximity check: warn if chauffeur is more than 500 m from destination
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
                setStatut(FirestoreUser.STATUT_EN_SERVICE)
                // Best-effort: restore taxi to ASSIGNE after trip ends
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

    // ── Location sharing ──────────────────────────────────────────────────────

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

    fun stopLocationSharing() {
        viewModelScope.launch {
            locationRepository.removeLocation(chauffeurUid)
            firestoreUserRepository.clearCurrentLocation(chauffeurUid)
        }
    }

    // ── Daily summary — uses Firestore terminee courses + Room charges ─────────

    val myCharges: StateFlow<List<ChargeEntity>> = combine(
        chargeRepository.getAll(), _selectedTaxiId
    ) { charges, taxiId ->
        if (taxiId != null) charges.filter { it.taxiId == taxiId } else charges
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** All charges regardless of selected taxi — for history tab. */
    val allChargesHistory: StateFlow<List<ChargeEntity>> = chargeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dailySummary: StateFlow<DailySummaryUiState> = combine(
        myCourses, myCharges
    ) { courses, charges ->
        val start = todayStart()
        val end   = todayEnd()

        // Revenue = terminee courses completed today (uses completedAt, falls back to createdAt)
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

    // ── Voice state machine ───────────────────────────────────────────────────

    private val _voiceUiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val voiceUiState: StateFlow<VoiceUiState> = _voiceUiState.asStateFlow()

    fun onVoiceResult(text: String) {
        val command = VoiceParser.parse(text)
        when (command) {
            is VoiceParser.VoiceCommand.NouveauTrajet,
            is VoiceParser.VoiceCommand.NouvelleCharge ->
                _voiceUiState.value = VoiceUiState.PendingConfirmation(command)
            is VoiceParser.VoiceCommand.Reponse -> {
                val pending = _voiceUiState.value as? VoiceUiState.PendingConfirmation
                if (pending != null) {
                    if (command.confirmee) confirmPending(pending.command)
                    else _voiceUiState.value = VoiceUiState.Idle
                }
            }
            VoiceParser.VoiceCommand.Incompris ->
                _voiceUiState.value = VoiceUiState.Error(
                    "Commande non reconnue. Dites « trajet de Rabat à Casablanca 350 MAD »."
                )
        }
    }

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

    // ── Manual save ───────────────────────────────────────────────────────────

    private val _manualSaveState = MutableStateFlow<ManualSaveState>(ManualSaveState.Idle)
    val manualSaveState: StateFlow<ManualSaveState> = _manualSaveState.asStateFlow()

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

    fun saveChargeManuel(type: TypeCharge, description: String, montant: Double) {
        viewModelScope.launch {
            try {
                // Auto-resolve taxi: prefer already-resolved local ID,
                // fall back to a live DB lookup by matricule (handles init race condition).
                val taxiId = _selectedTaxiId.value ?: assignedTaxi?.let {
                    taxiRepository.getByImmatriculation(it)?.id
                }
                if (taxiId == null) {
                    _manualSaveState.value = ManualSaveState.Error(
                        "Aucun véhicule assigné. Contactez votre propriétaire."
                    )
                    return@launch
                }
                // Cache for future calls within this session
                if (_selectedTaxiId.value == null) _selectedTaxiId.value = taxiId

                chargeRepository.insert(
                    ChargeEntity(taxiId = taxiId, type = type, description = description, montant = montant)
                )
                // Mirror to Firestore so proprietaire can see it
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

    // ── DB writes (voice) ─────────────────────────────────────────────────────

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
        // Auto-resolve taxi: prefer already-resolved local ID,
        // fall back to a live DB lookup by matricule (handles init race condition).
        val taxiId = _selectedTaxiId.value ?: assignedTaxi?.let {
            taxiRepository.getByImmatriculation(it)?.id
        }
        if (taxiId == null) {
            _voiceUiState.value = VoiceUiState.Error(
                "Aucun véhicule assigné. Contactez votre propriétaire."
            )
            return
        }
        // Cache for future calls within this session
        if (_selectedTaxiId.value == null) _selectedTaxiId.value = taxiId
        val montant = cmd.montant ?: 0.0
        chargeRepository.insert(
            ChargeEntity(
                taxiId = taxiId, type = cmd.type,
                description = cmd.description, montant = montant
            )
        )
        // Mirror to Firestore so proprietaire can see it
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

    // ── Utils ─────────────────────────────────────────────────────────────────

    private fun todayStart(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun todayEnd(): Long = todayStart() + 86_400_000L - 1L

    // ── Factory ───────────────────────────────────────────────────────────────

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
