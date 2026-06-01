package com.example.taxipilot.core.data.database.dao

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.TripEntity
import com.example.taxipilot.core.data.database.entity.TripStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY requestedAt DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE clientId = :clientId ORDER BY requestedAt DESC")
    fun getTripsByClient(clientId: Long): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE driverId = :driverId ORDER BY requestedAt DESC")
    fun getTripsByDriver(driverId: Long): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status = :status ORDER BY requestedAt DESC")
    fun getTripsByStatus(status: TripStatus): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)
}
