package com.example.taxipilot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["ownerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("ownerId")]
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val brand: String,
    val model: String,
    val licensePlate: String,
    val isAvailable: Boolean = true
)
