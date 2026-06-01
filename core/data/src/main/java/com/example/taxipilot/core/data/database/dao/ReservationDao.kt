package com.example.taxipilot.core.data.database.dao

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.ReservationEntity
import com.example.taxipilot.core.data.database.entity.ReservationStatut
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {

    @Query("SELECT * FROM reservations ORDER BY datePickup ASC")
    fun getAll(): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE statut = :statut ORDER BY datePickup ASC")
    fun getByStatut(statut: ReservationStatut): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE taxiId = :taxiId ORDER BY datePickup ASC")
    fun getByTaxi(taxiId: Long): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE chauffeurId = :chauffeurId ORDER BY datePickup ASC")
    fun getByChauffeur(chauffeurId: Long): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE clientId = :clientId ORDER BY datePickup DESC")
    fun getByClientId(clientId: Long): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE datePickup BETWEEN :debut AND :fin ORDER BY datePickup ASC")
    fun getByPeriode(debut: Long, fin: Long): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun getById(id: Long): ReservationEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reservation: ReservationEntity): Long

    @Update
    suspend fun update(reservation: ReservationEntity)

    @Query("UPDATE reservations SET statut = :statut WHERE id = :id")
    suspend fun updateStatut(id: Long, statut: ReservationStatut)

    @Query("UPDATE reservations SET taxiId = :taxiId, chauffeurId = :chauffeurId, statut = 'CONFIRMEE' WHERE id = :id")
    suspend fun confirmer(id: Long, taxiId: Long, chauffeurId: Long)

    @Delete
    suspend fun delete(reservation: ReservationEntity)
}
