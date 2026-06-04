package com.example.taxipilot.core.data.database.dao

// DAO pour la table "trips" — courses client (auth locale, ancienne architecture).
// Fournit des flux filtrés par client, chauffeur ou statut, triés du plus récent au plus ancien.
// Note : dans le flux Firestore actuel, FirestoreReservationRepository est la source de vérité.

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.TripEntity
import com.example.taxipilot.core.data.database.entity.TripStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    // Toutes les courses, du plus récent au plus ancien
    @Query("SELECT * FROM trips ORDER BY requestedAt DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    // Courses d'un client précis (historique du passager)
    @Query("SELECT * FROM trips WHERE clientId = :clientId ORDER BY requestedAt DESC")
    fun getTripsByClient(clientId: Long): Flow<List<TripEntity>>

    // Courses assignées à un chauffeur précis (son historique de travail)
    @Query("SELECT * FROM trips WHERE driverId = :driverId ORDER BY requestedAt DESC")
    fun getTripsByDriver(driverId: Long): Flow<List<TripEntity>>

    // Courses filtrées par statut (ex : toutes les PENDING)
    @Query("SELECT * FROM trips WHERE status = :status ORDER BY requestedAt DESC")
    fun getTripsByStatus(status: TripStatus): Flow<List<TripEntity>>

    // Insère ou remplace une course
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    // Met à jour une course existante (ex : changer le statut)
    @Update
    suspend fun updateTrip(trip: TripEntity)

    // Supprime une course
    @Delete
    suspend fun deleteTrip(trip: TripEntity)
}
