package com.example.taxipilot.core.data.database.dao

// DAO pour la table "trajets" — historique local des courses effectuées.
// Contient deux requêtes d'agrégation importantes pour les recettes :
//   - getTotalRecettesChauffeur : chiffre d'affaires d'un chauffeur sur une période
//   - getTotalRecettesTaxi : cumul total des recettes d'un taxi donné
// COALESCE(..., 0.0) garantit que la somme retourne 0 et non null si aucune ligne ne correspond.

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.TrajetEntity
import com.example.taxipilot.core.data.database.entity.TrajetStatut
import kotlinx.coroutines.flow.Flow

@Dao
interface TrajetDao {

    // Tous les trajets triés du plus récent au plus ancien
    @Query("SELECT * FROM trajets ORDER BY dateDebut DESC")
    fun getAll(): Flow<List<TrajetEntity>>

    // Trajets effectués par un taxi précis
    @Query("SELECT * FROM trajets WHERE taxiId = :taxiId ORDER BY dateDebut DESC")
    fun getByTaxi(taxiId: Long): Flow<List<TrajetEntity>>

    // Trajets effectués par un chauffeur précis (son historique de travail)
    @Query("SELECT * FROM trajets WHERE chauffeurId = :chauffeurId ORDER BY dateDebut DESC")
    fun getByChauffeur(chauffeurId: Long): Flow<List<TrajetEntity>>

    // Trajets filtrés par statut (ex : tous les EN_COURS)
    @Query("SELECT * FROM trajets WHERE statut = :statut ORDER BY dateDebut DESC")
    fun getByStatut(statut: TrajetStatut): Flow<List<TrajetEntity>>

    // Trajet lié à une réservation précise (one-shot, au plus un)
    @Query("SELECT * FROM trajets WHERE reservationId = :reservationId LIMIT 1")
    suspend fun getByReservation(reservationId: Long): TrajetEntity?

    // Recherche d'un trajet par son ID Room
    @Query("SELECT * FROM trajets WHERE id = :id")
    suspend fun getById(id: Long): TrajetEntity?

    // Somme des recettes d'un chauffeur sur une période [debut, fin] (trajets TERMINE uniquement)
    // Retourne 0.0 si aucun trajet terminé dans la période (COALESCE)
    @Query("""
        SELECT COALESCE(SUM(montant), 0.0) FROM trajets
        WHERE chauffeurId = :chauffeurId
          AND statut = 'TERMINE'
          AND dateFin BETWEEN :debut AND :fin
    """)
    suspend fun getTotalRecettesChauffeur(chauffeurId: Long, debut: Long, fin: Long): Double

    // Cumul total des recettes d'un taxi depuis sa création dans la DB
    @Query("""
        SELECT COALESCE(SUM(montant), 0.0) FROM trajets
        WHERE taxiId = :taxiId AND statut = 'TERMINE'
    """)
    suspend fun getTotalRecettesTaxi(taxiId: Long): Double

    // Insère un nouveau trajet (ABORT si conflit)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(trajet: TrajetEntity): Long

    // Met à jour un trajet existant
    @Update
    suspend fun update(trajet: TrajetEntity)

    // Mise à jour rapide du statut uniquement
    @Query("UPDATE trajets SET statut = :statut WHERE id = :id")
    suspend fun updateStatut(id: Long, statut: TrajetStatut)

    // Supprime un trajet
    @Delete
    suspend fun delete(trajet: TrajetEntity)
}
