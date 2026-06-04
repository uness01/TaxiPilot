package com.example.taxipilot.core.data.repository

// Repository pour les trajets locaux (Room/SQLite).
// Expose les opérations CRUD et les calculs de recettes (agrégations SQL).
// Utilisé par DriverViewModel (saisie de courses) et OwnerViewModel (tableau de bord).

import com.example.taxipilot.core.data.database.dao.TrajetDao
import com.example.taxipilot.core.data.database.entity.TrajetEntity
import com.example.taxipilot.core.data.database.entity.TrajetStatut
import kotlinx.coroutines.flow.Flow

class TrajetRepository(private val dao: TrajetDao) {

    // Tous les trajets (flux observable, du plus récent)
    fun getAll(): Flow<List<TrajetEntity>> = dao.getAll()

    // Trajets d'un taxi précis
    fun getByTaxi(taxiId: Long): Flow<List<TrajetEntity>> = dao.getByTaxi(taxiId)

    // Trajets d'un chauffeur précis (son historique)
    fun getByChauffeur(chauffeurId: Long): Flow<List<TrajetEntity>> = dao.getByChauffeur(chauffeurId)

    // Trajets filtrés par statut
    fun getByStatut(statut: TrajetStatut): Flow<List<TrajetEntity>> = dao.getByStatut(statut)

    // Recherche one-shot par ID
    suspend fun getById(id: Long): TrajetEntity? = dao.getById(id)

    // Trajet lié à une réservation Room précise
    suspend fun getByReservation(reservationId: Long): TrajetEntity? =
        dao.getByReservation(reservationId)

    // Total des recettes d'un chauffeur sur une période [debut, fin] (trajets terminés)
    suspend fun getTotalRecettesChauffeur(chauffeurId: Long, debut: Long, fin: Long): Double =
        dao.getTotalRecettesChauffeur(chauffeurId, debut, fin)

    // Cumul total des recettes générées par un taxi (tous temps confondus)
    suspend fun getTotalRecettesTaxi(taxiId: Long): Double =
        dao.getTotalRecettesTaxi(taxiId)

    // Insère un trajet et retourne l'ID généré
    suspend fun insert(trajet: TrajetEntity): Long = dao.insert(trajet)

    // Met à jour un trajet existant
    suspend fun update(trajet: TrajetEntity) = dao.update(trajet)

    // Change uniquement le statut d'un trajet
    suspend fun updateStatut(id: Long, statut: TrajetStatut) = dao.updateStatut(id, statut)

    // Supprime un trajet
    suspend fun delete(trajet: TrajetEntity) = dao.delete(trajet)
}
