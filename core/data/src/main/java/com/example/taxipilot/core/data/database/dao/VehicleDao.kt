package com.example.taxipilot.core.data.database.dao

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE ownerId = :ownerId")
    fun getVehiclesByOwner(ownerId: Long): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE isAvailable = 1")
    fun getAvailableVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)
}
