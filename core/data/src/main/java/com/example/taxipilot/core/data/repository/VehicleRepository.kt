package com.example.taxipilot.core.data.repository

import com.example.taxipilot.core.data.database.dao.VehicleDao
import com.example.taxipilot.core.data.database.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {

    fun getAllVehicles(): Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()

    fun getVehiclesByOwner(ownerId: Long): Flow<List<VehicleEntity>> =
        vehicleDao.getVehiclesByOwner(ownerId)

    fun getAvailableVehicles(): Flow<List<VehicleEntity>> = vehicleDao.getAvailableVehicles()

    suspend fun insertVehicle(vehicle: VehicleEntity): Long = vehicleDao.insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: VehicleEntity) = vehicleDao.updateVehicle(vehicle)

    suspend fun deleteVehicle(vehicle: VehicleEntity) = vehicleDao.deleteVehicle(vehicle)
}
