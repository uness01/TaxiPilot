package com.example.taxipilot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TripStatus { PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED }

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["driverId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("clientId"), Index("driverId")]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val driverId: Long? = null,
    val originAddress: String,
    val destinationAddress: String,
    val status: TripStatus = TripStatus.PENDING,
    val fare: Double? = null,
    val requestedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
