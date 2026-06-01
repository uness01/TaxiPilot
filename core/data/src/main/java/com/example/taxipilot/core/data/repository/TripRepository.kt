package com.example.taxipilot.core.data.repository

import com.example.taxipilot.core.data.database.dao.TripDao
import com.example.taxipilot.core.data.database.entity.TripEntity
import com.example.taxipilot.core.data.database.entity.TripStatus
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

    fun getAllTrips(): Flow<List<TripEntity>> = tripDao.getAllTrips()

    fun getTripsByClient(clientId: Long): Flow<List<TripEntity>> =
        tripDao.getTripsByClient(clientId)

    fun getTripsByDriver(driverId: Long): Flow<List<TripEntity>> =
        tripDao.getTripsByDriver(driverId)

    fun getTripsByStatus(status: TripStatus): Flow<List<TripEntity>> =
        tripDao.getTripsByStatus(status)

    suspend fun insertTrip(trip: TripEntity): Long = tripDao.insertTrip(trip)

    suspend fun updateTrip(trip: TripEntity) = tripDao.updateTrip(trip)

    suspend fun deleteTrip(trip: TripEntity) = tripDao.deleteTrip(trip)
}
