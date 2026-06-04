package com.example.taxipilot.core.data.repository

// Repository pour les véhicules locaux (Room/SQLite).
// Utilisé avec UserEntity (auth locale) dans l'ancienne architecture.
// Les opérations CRUD délèguent directement au VehicleDao.

import com.example.taxipilot.core.data.database.dao.VehicleDao
import com.example.taxipilot.core.data.database.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {

    // Tous les véhicules (flux observable)
    fun getAllVehicles(): Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()

    // Véhicules d'un propriétaire donné
    fun getVehiclesByOwner(ownerId: Long): Flow<List<VehicleEntity>> =
        vehicleDao.getVehiclesByOwner(ownerId)

    // Uniquement les véhicules disponibles
    fun getAvailableVehicles(): Flow<List<VehicleEntity>> = vehicleDao.getAvailableVehicles()

    // Insère un véhicule et retourne l'ID généré
    suspend fun insertVehicle(vehicle: VehicleEntity): Long = vehicleDao.insertVehicle(vehicle)

    // Met à jour un véhicule existant
    suspend fun updateVehicle(vehicle: VehicleEntity) = vehicleDao.updateVehicle(vehicle)

    // Supprime un véhicule
    suspend fun deleteVehicle(vehicle: VehicleEntity) = vehicleDao.deleteVehicle(vehicle)
}
