package com.example.taxipilot.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TrajetStatut {
    EN_ATTENTE,
    EN_COURS,
    TERMINE,
    ANNULE
}

@Entity(
    tableName = "trajets",
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
    val taxiId: Long? = null,
    val chauffeurId: Long? = null,
    val reservationId: Long? = null,
    val adresseDepart: String,
    val adresseArrivee: String,
    val dateDebut: Long? = null,
    val dateFin: Long? = null,
    val distanceKm: Double? = null,
    val montant: Double? = null,
    val statut: TrajetStatut = TrajetStatut.EN_ATTENTE
)
