package com.example.taxipilot.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChauffeurStatut {
    ACTIF,
    INACTIF,
    SUSPENDU
}

@Entity(tableName = "chauffeurs")
data class ChauffeurEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String = "",
    val numeroPermis: String,
    val dateEmbauche: Long = System.currentTimeMillis(),
    val statut: ChauffeurStatut = ChauffeurStatut.ACTIF
)
