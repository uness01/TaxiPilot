package com.example.taxipilot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ReservationStatut {
    EN_ATTENTE,
    CONFIRMEE,
    EN_COURS,
    ANNULEE,
    TERMINEE
}

enum class ReservationType {
    NORMALE,
    IMMEDIATE
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
    val clientId: Long? = null,
    val clientNom: String,
    val clientTelephone: String,
    val adresseDepart: String,
    val adresseArrivee: String,
    val dateReservation: Long = System.currentTimeMillis(),
    val datePickup: Long,
    val taxiId: Long? = null,
    val chauffeurId: Long? = null,
    val statut: ReservationStatut = ReservationStatut.EN_ATTENTE,
    val type: ReservationType = ReservationType.NORMALE,
    val prixEstime: Double? = null,
    val notes: String = ""
)
