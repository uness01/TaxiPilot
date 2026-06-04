package com.example.taxipilot.core.data.database.entity

// Table "chauffeurs" — chauffeurs gérés localement par le propriétaire (Room/SQLite).
// Distinct de FirestoreUser qui représente le compte Firebase du chauffeur.
// ChauffeurStatut détermine si le chauffeur est opérationnel ou non.

import androidx.room.Entity
import androidx.room.PrimaryKey

// État contractuel du chauffeur dans la flotte locale.
enum class ChauffeurStatut {
    ACTIF,    // chauffeur en activité normale
    INACTIF,  // temporairement indisponible
    SUSPENDU  // suspendu par le propriétaire
}

@Entity(tableName = "chauffeurs")
data class ChauffeurEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,                                      // nom de famille
    val prenom: String,                                   // prénom
    val telephone: String,                                // numéro unique — utilisé pour recherche
    val email: String = "",                               // email (optionnel)
    val numeroPermis: String,                             // numéro de permis de conduire
    val dateEmbauche: Long = System.currentTimeMillis(),  // date d'embauche (ms depuis epoch)
    val statut: ChauffeurStatut = ChauffeurStatut.ACTIF   // actif par défaut à la création
)
