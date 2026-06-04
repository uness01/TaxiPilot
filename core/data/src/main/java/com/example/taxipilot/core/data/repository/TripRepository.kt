package com.example.taxipilot.core.data.repository

// Repository pour les courses locales (Room/SQLite, ancienne architecture).
// Conservé pour la compatibilité de compilation avec MainActivity.
// Dans le flux actuel, FirestoreReservationRepository est la source de vérité pour les courses.

import com.example.taxipilot.core.data.database.dao.TripDao
import com.example.taxipilot.core.data.database.entity.TripEntity
import com.example.taxipilot.core.data.database.entity.TripStatus
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

    // Toutes les courses (flux observable, du plus récent)
    fun getAllTrips(): Flow<List<TripEntity>> = tripDao.getAllTrips()

    // Courses d'un client précis
    fun getTripsByClient(clientId: Long): Flow<List<TripEntity>> =
        tripDao.getTripsByClient(clientId)

    // Courses d'un chauffeur précis
    fun getTripsByDriver(driverId: Long): Flow<List<TripEntity>> =
        tripDao.getTripsByDriver(driverId)

    // Courses filtrées par statut
    fun getTripsByStatus(status: TripStatus): Flow<List<TripEntity>> =
        tripDao.getTripsByStatus(status)

    // Insère une course et retourne l'ID généré
    suspend fun insertTrip(trip: TripEntity): Long = tripDao.insertTrip(trip)

    // Met à jour une course existante
    suspend fun updateTrip(trip: TripEntity) = tripDao.updateTrip(trip)

    // Supprime une course
    suspend fun deleteTrip(trip: TripEntity) = tripDao.deleteTrip(trip)
}
