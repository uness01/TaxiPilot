package com.example.taxipilot.core.data.database.entity

// Table "trips" — courses demandées par les clients (auth locale).
// Deux clés étrangères vers UserEntity : clientId (obligatoire) et driverId (nullable).
// Si le client est supprimé → la course est supprimée (CASCADE).
// Si le chauffeur est supprimé → driverId passe à NULL (SET_NULL).
// Note : dans le flow Firestore, FirestoreReservation est la source de vérité ;
//        TripEntity est conservé pour compatibilité de compilation.

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Cycle de vie d'une course client.
enum class TripStatus { PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED }

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE // supprime la course si le client est effacé
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["driverId"],
            onDelete = ForeignKey.SET_NULL // met driverId à null si le chauffeur est effacé
        )
    ],
    indices = [Index("clientId"), Index("driverId")]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,                     // qui a demandé la course
    val driverId: Long? = null,             // chauffeur assigné (null tant que non accepté)
    val originAddress: String,              // adresse de départ
    val destinationAddress: String,         // adresse d'arrivée
    val status: TripStatus = TripStatus.PENDING, // état de la course
    val fare: Double? = null,               // montant final (null jusqu'à la fin)
    val requestedAt: Long = System.currentTimeMillis(), // heure de la demande
    val completedAt: Long? = null           // heure de fin (null tant que non terminée)
)
