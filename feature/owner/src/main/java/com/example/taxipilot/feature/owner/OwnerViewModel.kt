package com.example.taxipilot.feature.owner

// ViewModel de l'espace Propriétaire — agrège toutes les données de la flotte et du tableau de bord.
//
// Sources de données :
//   - Room (local)     : taxis, chauffeurs, trajets, charges → TaxiRepository, ChauffeurRepository, etc.
//   - Firestore (cloud): réservations, chauffeurs liés, charges des chauffeurs, notifications, logs
//
// Flux principal (dashboardState) :
//   combine(taxis, chauffeurs, firestoreCharges, allReservations, myChauffeurs) →
//   calcule recettes, charges, bénéfice, stats par chauffeur (courses du jour, CA du jour)
//
// Code propriétaire :
//   getOrGenerateProprietaireCode() : récupère ou crée le code 6 chiffres à l'init
//   regenerateCode() : génère un nouveau code si le propriétaire veut le changer

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

// État du tableau de bord propriétaire
data class DashboardUiState(
    val totalRecettes: Double = 0.0,          // total des recettes (réservations terminées)
    val totalCharges: Double = 0.0,           // total des dépenses (Firestore charges)
    val benefice: Double = 0.0,               // bénéfice = recettes - charges
    val nbTaxis: Int = 0,                     // nombre de taxis dans la flotte Room
    val nbChauffeursActifs: Int = 0,          // chauffeurs avec statut ACTIF dans Room
    val nbTrajetsTermines: Int = 0,           // réservations Firestore terminées
    val nbReservationsEnAttente: Int = 0,     // réservations en attente d'un chauffeur
    val nbReservationsEnCours: Int = 0,       // courses en cours actuellement
    val chargesParType: Map<String, Double> = emptyMap(), // dépenses ventilées par type
    val chauffeurStats: List<ChauffeurStat> = emptyList() // stats par chauffeur (triées par CA)
)

// Statistiques quotidiennes pour un chauffeur dans le tableau de bord
data class ChauffeurStat(
    val uid: String,          // UID Firebase du chauffeur
    val nom: String,          // nom affiché
    val statut: String,       // en_service / hors_service / en_course
    val assignedTaxi: String?, // matricule du taxi assigné
    val tripsToday: Int,      // nombre de courses terminées aujourd'hui
    val revenueToday: Double  // CA généré aujourd'hui
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
    val proprietaireId: String, // UID Firebase du propriétaire connecté
    codeProprietaireInitial: String? = null // code passé depuis le profil (peut être null si vieux compte)
) : ViewModel() {

    // ── Code propriétaire ─────────────────────────────────────────────────────

    private val _codeProprietaire = MutableStateFlow<String?>(codeProprietaireInitial)
    val codeProprietaire: StateFlow<String?> = _codeProprietaire.asStateFlow()

    init {
        // Vérifie/génère le code au démarrage pour les vieux comptes sans code
        viewModelScope.launch {
            val code = firestoreUserRepository.getOrGenerateProprietaireCode(proprietaireId)
            if (code != null) _codeProprietaire.value = code
        }
    }

    // Génère un nouveau code unique et invalide l'ancien (les chauffeurs déjà liés ne sont pas affectés)
    fun regenerateCode() {
        viewModelScope.launch {
            val newCode = firestoreUserRepository.regenerateProprietaireCode(proprietaireId)
            if (newCode != null) _codeProprietaire.value = newCode
        }
    }

    // ── Flux Room (données locales de la flotte) ──────────────────────────────

    val allTaxis: StateFlow<List<TaxiEntity>> = taxiRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allChauffeurs: StateFlow<List<ChauffeurEntity>> = chauffeurRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTrajets: StateFlow<List<TrajetEntity>> = trajetRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCharges: StateFlow<List<ChargeEntity>> = chargeRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Flux Firestore (données cloud) ────────────────────────────────────────

    // Toutes les réservations Firestore (vue globale de l'onglet Réservations)
    val allReservations: StateFlow<List<FirestoreReservation>> =
        firestoreReservationRepository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Chauffeurs liés à ce propriétaire (via proprietaireId dans Firestore)
    val myChauffeurs: StateFlow<List<FirestoreUser>> =
        firestoreUserRepository.getChauffeursByProprietaire(proprietaireId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Charges déclarées par les chauffeurs de ce propriétaire (mirrorées depuis Room)
    val firestoreCharges: StateFlow<List<FirestoreCharge>> =
        firestoreChargeRepository.getByProprietaire(proprietaireId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Messages de changement de statut des chauffeurs (ex : "Chauffeur X est en service")
    // Émis en temps réel → OwnerScreen les affiche dans un Snackbar
    val driverStatusMessages: Flow<String> =
        firestoreNotificationRepository.getNewMessagesForProprietaire(proprietaireId)

    // Récupère l'historique des logs de service d'un chauffeur (one-shot)
    suspend fun getServiceLogs(chauffeurUid: String): List<FirestoreServiceLog> =
        firestoreServiceLogRepository.getByChauffeur(chauffeurUid)

    // ── Tableau de bord (combinaison de tous les flux) ────────────────────────

    // combine() écoute 5 flux simultanément et recalcule l'état à chaque changement
    val dashboardState: StateFlow<DashboardUiState> = combine(
        taxiRepository.getAll(),
        chauffeurRepository.getAll(),
        firestoreCharges,
        allReservations,
        myChauffeurs
    ) { taxis, chauffeurs, fsCharges, reservations, myChauffeurList ->
        val myChauffeurUids = myChauffeurList.map { it.uid }.toSet()

        // Recettes = somme des prixFinal (ou prixEstime) des réservations terminées par nos chauffeurs
        val myReservations = reservations.filter { it.chauffeurId in myChauffeurUids }
        val recettes = myReservations
            .filter { it.status == FirestoreReservation.STATUS_TERMINEE }
            .sumOf { it.prixFinal ?: it.prixEstime }

        val totalCharges = fsCharges.sumOf { it.montant }

        // Stats du jour par chauffeur (courses terminées aujourd'hui)
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
        }.sortedByDescending { it.revenueToday } // tri par CA décroissant

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

    // ── Gestion des taxis (Room) ──────────────────────────────────────────────

    fun addTaxi(taxi: TaxiEntity)    = viewModelScope.launch { taxiRepository.insert(taxi) }
    fun updateTaxi(taxi: TaxiEntity) = viewModelScope.launch { taxiRepository.update(taxi) }
    fun deleteTaxi(taxi: TaxiEntity) = viewModelScope.launch { taxiRepository.delete(taxi) }
    // Change le statut d'un taxi (DISPONIBLE, ASSIGNE, EN_COURSE, EN_MAINTENANCE)
    fun updateTaxiStatut(id: Long, statut: TaxiStatut) =
        viewModelScope.launch { taxiRepository.updateStatut(id, statut) }

    // ── Gestion des chauffeurs locaux (Room) ──────────────────────────────────

    fun addChauffeur(c: ChauffeurEntity)    = viewModelScope.launch { chauffeurRepository.insert(c) }
    fun updateChauffeur(c: ChauffeurEntity) = viewModelScope.launch { chauffeurRepository.update(c) }
    fun deleteChauffeur(c: ChauffeurEntity) = viewModelScope.launch { chauffeurRepository.delete(c) }
    fun updateChauffeurStatut(id: Long, statut: ChauffeurStatut) =
        viewModelScope.launch { chauffeurRepository.updateStatut(id, statut) }

    // ── Mutations Firestore (chauffeurs cloud) ────────────────────────────────

    // Assigne un taxi à un chauffeur : écrit dans Firestore + met à jour le statut Room à ASSIGNE
    fun assignTaxiToChauffeur(chauffeurUid: String, taxiMatricule: String) {
        viewModelScope.launch {
            firestoreUserRepository.assignTaxiToChauffeur(chauffeurUid, taxiMatricule)
            val taxi = taxiRepository.getByImmatriculation(taxiMatricule)
            if (taxi != null) taxiRepository.updateStatut(taxi.id, TaxiStatut.ASSIGNE)
        }
    }

    // Retire le taxi d'un chauffeur : efface dans Firestore + remet le taxi à DISPONIBLE dans Room
    fun unassignTaxi(chauffeurUid: String, taxiMatricule: String?) {
        viewModelScope.launch {
            firestoreUserRepository.unassignTaxi(chauffeurUid)
            if (!taxiMatricule.isNullOrBlank()) {
                val taxi = taxiRepository.getByImmatriculation(taxiMatricule)
                if (taxi != null) taxiRepository.updateStatut(taxi.id, TaxiStatut.DISPONIBLE)
            }
        }
    }

    // ── Gestion des charges (Room) ────────────────────────────────────────────

    fun addCharge(charge: ChargeEntity)  = viewModelScope.launch { chargeRepository.insert(charge) }
    fun deleteCharge(charge: ChargeEntity) = viewModelScope.launch { chargeRepository.delete(charge) }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    // Retourne le timestamp du début du jour courant (minuit) en millisecondes
    private fun todayStartMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // ── Factory manuelle (pas de Hilt) ────────────────────────────────────────
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
