package com.example.taxipilot.core.data.database.entity

// Table "vehicles" — véhicules appartenant à un propriétaire (UserEntity).
// Clé étrangère sur UserEntity.id : si le propriétaire est supprimé, ses véhicules le sont aussi (CASCADE).
// Index sur ownerId pour accélérer les requêtes "tous les véhicules de cet owner".

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
        onDelete = ForeignKey.CASCADE // suppression en cascade si le propriétaire est effacé
    )],
    indices = [Index("ownerId")] // index pour accélérer getVehiclesByOwner()
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,          // référence vers UserEntity.id du propriétaire
    val brand: String,          // marque du véhicule (ex : Dacia)
    val model: String,          // modèle (ex : Logan)
    val licensePlate: String,   // plaque d'immatriculation
    val isAvailable: Boolean = true // true = disponible pour une course
)
