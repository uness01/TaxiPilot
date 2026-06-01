package com.example.taxipilot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TypeCharge {
    DIESEL,
    REPARATION,
    AUTRE
}

@Entity(
    tableName = "charges",
    foreignKeys = [
        ForeignKey(
            entity = TaxiEntity::class,
            parentColumns = ["id"],
            childColumns = ["taxiId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taxiId")]
)
data class ChargeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taxiId: Long,
    val type: TypeCharge,
    val description: String,
    val montant: Double,
    val date: Long = System.currentTimeMillis(),
    val kilometrageAuMoment: Int? = null
)
