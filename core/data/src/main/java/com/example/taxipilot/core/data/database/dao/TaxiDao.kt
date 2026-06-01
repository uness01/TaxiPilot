package com.example.taxipilot.core.data.database.dao

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.TaxiEntity
import com.example.taxipilot.core.data.database.entity.TaxiStatut
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxiDao {

    @Query("SELECT * FROM taxis ORDER BY marque, modele")
    fun getAll(): Flow<List<TaxiEntity>>

    @Query("SELECT * FROM taxis WHERE statut = :statut")
    fun getByStatut(statut: TaxiStatut): Flow<List<TaxiEntity>>

    @Query("SELECT * FROM taxis WHERE id = :id")
    suspend fun getById(id: Long): TaxiEntity?

    @Query("SELECT * FROM taxis WHERE immatriculation = :immatriculation LIMIT 1")
    suspend fun getByImmatriculation(immatriculation: String): TaxiEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(taxi: TaxiEntity): Long

    @Update
    suspend fun update(taxi: TaxiEntity)

    @Query("UPDATE taxis SET statut = :statut WHERE id = :id")
    suspend fun updateStatut(id: Long, statut: TaxiStatut)

    @Query("UPDATE taxis SET kilometrage = :km WHERE id = :id")
    suspend fun updateKilometrage(id: Long, km: Int)

    @Delete
    suspend fun delete(taxi: TaxiEntity)
}
