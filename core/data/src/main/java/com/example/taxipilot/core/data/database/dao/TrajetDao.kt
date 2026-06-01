package com.example.taxipilot.core.data.database.dao

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.TrajetEntity
import com.example.taxipilot.core.data.database.entity.TrajetStatut
import kotlinx.coroutines.flow.Flow

@Dao
interface TrajetDao {

    @Query("SELECT * FROM trajets ORDER BY dateDebut DESC")
    fun getAll(): Flow<List<TrajetEntity>>

    @Query("SELECT * FROM trajets WHERE taxiId = :taxiId ORDER BY dateDebut DESC")
    fun getByTaxi(taxiId: Long): Flow<List<TrajetEntity>>

    @Query("SELECT * FROM trajets WHERE chauffeurId = :chauffeurId ORDER BY dateDebut DESC")
    fun getByChauffeur(chauffeurId: Long): Flow<List<TrajetEntity>>

    @Query("SELECT * FROM trajets WHERE statut = :statut ORDER BY dateDebut DESC")
    fun getByStatut(statut: TrajetStatut): Flow<List<TrajetEntity>>

    @Query("SELECT * FROM trajets WHERE reservationId = :reservationId LIMIT 1")
    suspend fun getByReservation(reservationId: Long): TrajetEntity?

    @Query("SELECT * FROM trajets WHERE id = :id")
    suspend fun getById(id: Long): TrajetEntity?

    @Query("""
        SELECT COALESCE(SUM(montant), 0.0) FROM trajets
        WHERE chauffeurId = :chauffeurId
          AND statut = 'TERMINE'
          AND dateFin BETWEEN :debut AND :fin
    """)
    suspend fun getTotalRecettesChauffeur(chauffeurId: Long, debut: Long, fin: Long): Double

    @Query("""
        SELECT COALESCE(SUM(montant), 0.0) FROM trajets
        WHERE taxiId = :taxiId AND statut = 'TERMINE'
    """)
    suspend fun getTotalRecettesTaxi(taxiId: Long): Double

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(trajet: TrajetEntity): Long

    @Update
    suspend fun update(trajet: TrajetEntity)

    @Query("UPDATE trajets SET statut = :statut WHERE id = :id")
    suspend fun updateStatut(id: Long, statut: TrajetStatut)

    @Delete
    suspend fun delete(trajet: TrajetEntity)
}
