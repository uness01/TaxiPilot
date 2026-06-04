package com.example.taxipilot.core.data.database.entity

// Table "taxis" — flotte de taxis gérée par le propriétaire.
// TaxiStatut reflète l'état opérationnel du taxi en temps réel.
// Cette table est la source de vérité locale pour les taxis (Room/SQLite).

import androidx.room.Entity
import androidx.room.PrimaryKey

// États possibles d'un taxi dans la flotte.
enum class TaxiStatut {
    DISPONIBLE,      // libre, peut accepter une course
    ASSIGNE,         // attribué à un chauffeur mais pas encore en route
    EN_COURSE,       // actuellement en train d'effectuer un trajet
    EN_MAINTENANCE   // immobilisé pour réparation ou entretien
}

@Entity(tableName = "taxis")
data class TaxiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val marque: String,         // marque du taxi (ex : Renault)
    val modele: String,         // modèle (ex : Symbol)
    val immatriculation: String, // numéro de plaque — utilisé aussi dans Firestore pour lier au chauffeur
    val annee: Int,             // année de fabrication
    val couleur: String,        // couleur du véhicule
    val kilometrage: Int = 0,   // kilométrage actuel (mis à jour manuellement)
    val statut: TaxiStatut = TaxiStatut.DISPONIBLE // état par défaut : disponible
)
