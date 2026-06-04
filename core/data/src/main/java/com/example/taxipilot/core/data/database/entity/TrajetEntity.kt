package com.example.taxipilot.core.data.database.entity

// Table "trajets" — historique des courses effectuées (Room/SQLite).
// Trois clés étrangères : taxiId, chauffeurId, reservationId (toutes nullable → SET_NULL).
// Un trajet peut être créé manuellement (saisie voix ou formulaire) ou issu d'une réservation.
// C'est la source de vérité locale pour les recettes du chauffeur.

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Cycle de vie d'un trajet local.
enum class TrajetStatut {
    EN_ATTENTE, // créé mais pas encore démarré
    EN_COURS,   // chauffeur en route
    TERMINE,    // course terminée, montant enregistré
    ANNULE      // course annulée
}

@Entity(
    tableName = "trajets",
    foreignKeys = [
        ForeignKey(
            entity = TaxiEntity::class,
            parentColumns = ["id"],
            childColumns = ["taxiId"],
            onDelete = ForeignKey.SET_NULL // met taxiId à null si le taxi est supprimé
        ),
        ForeignKey(
            entity = ChauffeurEntity::class,
            parentColumns = ["id"],
            childColumns = ["chauffeurId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ReservationEntity::class,
            parentColumns = ["id"],
            childColumns = ["reservationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("taxiId"),
        Index("chauffeurId"),
        Index("reservationId")
    ]
)
data class TrajetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taxiId: Long? = null,           // taxi utilisé (peut être null si non assigné)
    val chauffeurId: Long? = null,      // ID Room du chauffeur (≠ UID Firebase)
    val reservationId: Long? = null,    // réservation Room liée (null si trajet manuel)
    val adresseDepart: String,          // adresse de prise en charge
    val adresseArrivee: String,         // adresse de destination
    val dateDebut: Long? = null,        // heure de départ (ms depuis epoch)
    val dateFin: Long? = null,          // heure d'arrivée
    val distanceKm: Double? = null,     // distance calculée (km)
    val montant: Double? = null,        // montant encaissé (MAD)
    val statut: TrajetStatut = TrajetStatut.EN_ATTENTE
)
