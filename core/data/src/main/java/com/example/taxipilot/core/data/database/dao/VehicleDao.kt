package com.example.taxipilot.core.data.database.dao

// DAO pour la table "vehicles" — véhicules d'un propriétaire.
// Fournit des requêtes pour lister tous les véhicules, filtrer par propriétaire, ou par disponibilité.

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    // Tous les véhicules (flux observable)
    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    // Véhicules appartenant à un propriétaire donné
    @Query("SELECT * FROM vehicles WHERE ownerId = :ownerId")
    fun getVehiclesByOwner(ownerId: Long): Flow<List<VehicleEntity>>

    // Uniquement les véhicules marqués disponibles (isAvailable = true)
    @Query("SELECT * FROM vehicles WHERE isAvailable = 1")
    fun getAvailableVehicles(): Flow<List<VehicleEntity>>

    // Insère ou remplace un véhicule (REPLACE évite les doublons sur conflits)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    // Met à jour un véhicule existant
    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    // Supprime un véhicule
    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)
}
