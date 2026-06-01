package com.example.taxipilot.feature.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taxipilot.core.data.database.entity.*
import com.example.taxipilot.core.data.firestore.FirestoreCharge
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import com.example.taxipilot.core.data.firestore.FirestoreServiceLog
import com.example.taxipilot.core.data.firestore.FirestoreUser
import com.example.taxipilot.core.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val totalRecettes: Double = 0.0,
    val totalCharges: Double = 0.0,
    val benefice: Double = 0.0,
    val nbTaxis: Int = 0,
    val nbChauffeursActifs: Int = 0,
    val nbTrajetsTermines: Int = 0,
    val nbReservationsEnAttente: Int = 0,
    val nbReservationsEnCours: Int = 0,
    val chargesParType: Map<String, Double> = emptyMap(),
    val chauffeurStats: List<ChauffeurStat> = emptyList()
)

data class ChauffeurStat(
    val uid: String,
    val nom: String,
    val statut: String,
    val assignedTaxi: String?,
    val tripsToday: Int,
    val revenueToday: Double
)

class OwnerViewModel(
    private val taxiRepository: TaxiRepository,
    private val chauffeurRepository: ChauffeurRepository,
    private val trajetRepository: TrajetRepository,
    private val chargeRepository: ChargeRepository,
    private val firestoreReservationRepository: FirestoreReservationRepository,
    private val firestoreUserRepository: FirestoreUserRepository,
    private val firestoreChargeRepository: FirestoreChargeRepository = FirestoreChargeRepository(),
    private val firestoreNotificationRepository: FirestoreNotificationRepository = FirestoreNotificationRepository(),
    private val firestoreServiceLogRepository: FirestoreServiceLogRepository = FirestoreServiceLogRepository(),
    val proprietaireId: String,
    codeProprietaireInitial: String? = null
) : ViewModel() {

    // ── Proprietaire code (always loaded from Firestore, generated if missing) ──

    private val _codeProprietaire = MutableStateFlow<String?>(codeProprietaireInitial)
    val codeProprietaire: StateFlow<String?> = _codeProprietaire.asStateFlow()

    init {
        // Always verify/fetch the code — handles old accounts missing the field
        // and the first render where the profile may not have carried it yet
        viewModelScope.launch {
            val code = firestoreUserRepository.getOrGenerateProprietaireCode(proprietaireId)
            if (code != null) _codeProprietaire.value = code
        }
    }

    /**
     * Generate a new unique 6-character alphanumeric code and update Firestore.
     * Already-linked chauffeurs are NOT affected.
     */
    fun regenerateCode() {
        viewModelScope.launch {
            val newCode = firestoreUserRepository.regenerateProprietaireCode(proprietaireId)
            if (newCode != null) _codeProprietaire.value = newCode
        }
    }

    val allTaxis: StateFlow<List<TaxiEntity>> = taxiRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allChauffeurs: StateFlow<List<ChauffeurEntity>> = chauffeurRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTrajets: StateFlow<List<TrajetEntity>> = trajetRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCharges: StateFlow<List<ChargeEntity>> = chargeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Firestore — all reservations (global view for Réservations tab) ────────

    val allReservations: StateFlow<List<FirestoreReservation>> =
        firestoreReservationRepository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Firestore — chauffeurs linked to this proprietaire ────────────────────

    val myChauffeurs: StateFlow<List<FirestoreUser>> =
        firestoreUserRepository.getChauffeursByProprietaire(proprietaireId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Firestore charges (from all linked chauffeurs) ────────────────────────

    val firestoreCharges: StateFlow<List<FirestoreCharge>> =
        firestoreChargeRepository.getByProprietaire(proprietaireId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Proprietaire notifications (chauffeur status changes) ─────────────────

    /**
     * Emits notification messages as they arrive targeting this proprietaire.
     * Each emission is a new message string to display as a Snackbar.
     */
    val driverStatusMessages: Flow<String> =
        firestoreNotificationRepository.getNewMessagesForProprietaire(proprietaireId)

    // ── Service logs ──────────────────────────────────────────────────────────

    /** One-shot fetch of service log entries for a chauffeur. */
    suspend fun getServiceLogs(chauffeurUid: String): List<FirestoreServiceLog> =
        firestoreServiceLogRepository.getByChauffeur(chauffeurUid)

    // ── Dashboard ─────────────────────────────────────────────────────────────

    val dashboardState: StateFlow<DashboardUiState> = combine(
        taxiRepository.getAll(),
        chauffeurRepository.getAll(),
        firestoreCharges,
        allReservations,
        myChauffeurs
    ) { taxis, chauffeurs, fsCharges, reservations, myChauffeurList ->
        val myChauffeurUids = myChauffeurList.map { it.uid }.toSet()

        // Revenue = prixFinal from terminee reservations by our chauffeurs
        val myReservations = reservations.filter { it.chauffeurId in myChauffeurUids }
        val recettes = myReservations
            .filter { it.status == FirestoreReservation.STATUS_TERMINEE }
            .sumOf { it.prixFinal ?: it.prixEstime }

        val totalCharges = fsCharges.sumOf { it.montant }

        // Today's stats per chauffeur
        val todayStart = todayStartMillis()
        val todayEnd   = todayStart + 86_400_000L - 1L
        val chauffeurStats = myChauffeurList.map { ch ->
            val chReservations = reservations.filter {
                it.chauffeurId == ch.uid &&
                it.status == FirestoreReservation.STATUS_TERMINEE &&
                (it.completedAt ?: it.createdAt) in todayStart..todayEnd
            }
            ChauffeurStat(
                uid          = ch.uid,
                nom          = ch.nom,
                statut       = ch.statut,
                assignedTaxi = ch.assignedTaxi,
                tripsToday   = chReservations.size,
                revenueToday = chReservations.sumOf { it.prixFinal ?: it.prixEstime }
            )
        }.sortedByDescending { it.revenueToday }

        DashboardUiState(
            totalRecettes           = recettes,
            totalCharges            = totalCharges,
            benefice                = recettes - totalCharges,
            nbTaxis                 = taxis.size,
            nbChauffeursActifs      = chauffeurs.count { it.statut == ChauffeurStatut.ACTIF },
            nbTrajetsTermines       = myReservations.count { it.status == FirestoreReservation.STATUS_TERMINEE },
            nbReservationsEnAttente = reservations.count { it.status == FirestoreReservation.STATUS_EN_ATTENTE },
            nbReservationsEnCours   = reservations.count { it.status == FirestoreReservation.STATUS_EN_COURS },
            chargesParType          = fsCharges.groupBy { it.type }
                .mapValues { (_, list) -> list.sumOf { it.montant } },
            chauffeurStats          = chauffeurStats
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    // ── Taxis ─────────────────────────────────────────────────────────────────

    fun addTaxi(taxi: TaxiEntity)    = viewModelScope.launch { taxiRepository.insert(taxi) }
    fun updateTaxi(taxi: TaxiEntity) = viewModelScope.launch { taxiRepository.update(taxi) }
    fun deleteTaxi(taxi: TaxiEntity) = viewModelScope.launch { taxiRepository.delete(taxi) }
    fun updateTaxiStatut(id: Long, statut: TaxiStatut) =
        viewModelScope.launch { taxiRepository.updateStatut(id, statut) }

    // ── Room chauffeurs (local) ───────────────────────────────────────────────

    fun addChauffeur(c: ChauffeurEntity)    = viewModelScope.launch { chauffeurRepository.insert(c) }
    fun updateChauffeur(c: ChauffeurEntity) = viewModelScope.launch { chauffeurRepository.update(c) }
    fun deleteChauffeur(c: ChauffeurEntity) = viewModelScope.launch { chauffeurRepository.delete(c) }
    fun updateChauffeurStatut(id: Long, statut: ChauffeurStatut) =
        viewModelScope.launch { chauffeurRepository.updateStatut(id, statut) }

    // ── Firestore chauffeur mutations ─────────────────────────────────────────

    fun assignTaxiToChauffeur(chauffeurUid: String, taxiMatricule: String) {
        viewModelScope.launch {
            firestoreUserRepository.assignTaxiToChauffeur(chauffeurUid, taxiMatricule)
            // Mark taxi as assigned in local Room so it disappears from the available list
            val taxi = taxiRepository.getByImmatriculation(taxiMatricule)
            if (taxi != null) taxiRepository.updateStatut(taxi.id, TaxiStatut.ASSIGNE)
        }
    }

    fun unassignTaxi(chauffeurUid: String, taxiMatricule: String?) {
        viewModelScope.launch {
            firestoreUserRepository.unassignTaxi(chauffeurUid)
            if (!taxiMatricule.isNullOrBlank()) {
                val taxi = taxiRepository.getByImmatriculation(taxiMatricule)
                if (taxi != null) taxiRepository.updateStatut(taxi.id, TaxiStatut.DISPONIBLE)
            }
        }
    }

    // ── Charges ───────────────────────────────────────────────────────────────

    fun addCharge(charge: ChargeEntity)  = viewModelScope.launch { chargeRepository.insert(charge) }
    fun deleteCharge(charge: ChargeEntity) = viewModelScope.launch { chargeRepository.delete(charge) }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun todayStartMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val taxiRepository: TaxiRepository,
        private val chauffeurRepository: ChauffeurRepository,
        private val trajetRepository: TrajetRepository,
        private val chargeRepository: ChargeRepository,
        private val firestoreReservationRepository: FirestoreReservationRepository,
        private val firestoreUserRepository: FirestoreUserRepository,
        private val proprietaireId: String,
        private val codeProprietaire: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OwnerViewModel(
                taxiRepository = taxiRepository,
                chauffeurRepository = chauffeurRepository,
                trajetRepository = trajetRepository,
                chargeRepository = chargeRepository,
                firestoreReservationRepository = firestoreReservationRepository,
                firestoreUserRepository = firestoreUserRepository,
                proprietaireId = proprietaireId,
                codeProprietaireInitial = codeProprietaire
            ) as T
    }
}
