package com.example.taxipilot.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaxiStatut {
    DISPONIBLE,
    ASSIGNE,
    EN_COURSE,
    EN_MAINTENANCE
}

@Entity(tableName = "taxis")
data class TaxiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val marque: String,
    val modele: String,
    val immatriculation: String,
    val annee: Int,
    val couleur: String,
    val kilometrage: Int = 0,
    val statut: TaxiStatut = TaxiStatut.DISPONIBLE
)
