package com.example.taxipilot.core.data.database.dao

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.ChargeEntity
import com.example.taxipilot.core.data.database.entity.TypeCharge
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeDao {

    @Query("SELECT * FROM charges ORDER BY date DESC")
    fun getAll(): Flow<List<ChargeEntity>>

    @Query("SELECT * FROM charges WHERE taxiId = :taxiId ORDER BY date DESC")
    fun getByTaxi(taxiId: Long): Flow<List<ChargeEntity>>

    @Query("SELECT * FROM charges WHERE type = :type ORDER BY date DESC")
    fun getByType(type: TypeCharge): Flow<List<ChargeEntity>>

    @Query("SELECT * FROM charges WHERE taxiId = :taxiId AND type = :type ORDER BY date DESC")
    fun getByTaxiAndType(taxiId: Long, type: TypeCharge): Flow<List<ChargeEntity>>

    @Query("SELECT * FROM charges WHERE date BETWEEN :debut AND :fin ORDER BY date DESC")
    fun getByPeriode(debut: Long, fin: Long): Flow<List<ChargeEntity>>

    @Query("""
        SELECT COALESCE(SUM(montant), 0.0) FROM charges
        WHERE taxiId = :taxiId AND date BETWEEN :debut AND :fin
    """)
    suspend fun getTotalChargesTaxi(taxiId: Long, debut: Long, fin: Long): Double

    @Query("SELECT COALESCE(SUM(montant), 0.0) FROM charges WHERE date BETWEEN :debut AND :fin")
    suspend fun getTotalChargesPeriode(debut: Long, fin: Long): Double

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(charge: ChargeEntity): Long

    @Update
    suspend fun update(charge: ChargeEntity)

    @Delete
    suspend fun delete(charge: ChargeEntity)
}
