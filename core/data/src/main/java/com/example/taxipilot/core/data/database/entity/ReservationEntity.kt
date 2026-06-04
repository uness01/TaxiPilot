package com.example.taxipilot.core.data.database.entity

// Table "reservations" — réservations locales Room (ancienne architecture).
// Dans le flux actuel, FirestoreReservation est la source de vérité pour les réservations.
// ReservationEntity reste pour la compatibilité avec le code existant et les clés étrangères TrajetEntity.
// Deux clés étrangères nullable : taxiId et chauffeurId (SET_NULL si supprimé).

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Cycle de vie d'une réservation locale.
enum class ReservationStatut {
    EN_ATTENTE, // soumise, en attente d'un chauffeur
    CONFIRMEE,  // taxi et chauffeur assignés
    EN_COURS,   // course en train de se dérouler
    ANNULEE,    // annulée par le client ou l'opérateur
    TERMINEE    // course effectuée avec succès
}

// Type de réservation : planifiée à l'avance ou immédiate.
enum class ReservationType {
    NORMALE,   // réservée pour un horaire futur précis
    IMMEDIATE  // demande sur-le-champ
}

@Entity(
    tableName = "reservations",
    foreignKeys = [
        ForeignKey(
            entity = TaxiEntity::class,
            parentColumns = ["id"],
            childColumns = ["taxiId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ChauffeurEntity::class,
            parentColumns = ["id"],
            childColumns = ["chauffeurId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("taxiId"), Index("chauffeurId")]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long? = null,                           // ID Room du client (nullable)
    val clientNom: String,                                // nom saisi à la réservation
    val clientTelephone: String,                          // téléphone du client
    val adresseDepart: String,                            // adresse de prise en charge
    val adresseArrivee: String,                           // adresse de destination
    val dateReservation: Long = System.currentTimeMillis(), // heure de création
    val datePickup: Long,                                 // heure prévue de prise en charge
    val taxiId: Long? = null,                             // taxi assigné (null si pas encore)
    val chauffeurId: Long? = null,                        // chauffeur assigné
    val statut: ReservationStatut = ReservationStatut.EN_ATTENTE,
    val type: ReservationType = ReservationType.NORMALE,
    val prixEstime: Double? = null,                       // prix estimé avant la course
    val notes: String = ""                                // notes libres
)
