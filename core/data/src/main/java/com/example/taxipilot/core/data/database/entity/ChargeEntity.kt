package com.example.taxipilot.core.data.database.entity

// Table "charges" — dépenses liées à un taxi (carburant, réparations, autres).
// Clé étrangère sur TaxiEntity : si le taxi est supprimé, ses charges le sont aussi (CASCADE).
// Cette table est la source de vérité locale pour les dépenses (côté chauffeur et propriétaire).
// Les charges sont aussi mirrorées dans Firestore (FirestoreCharge) pour que le propriétaire les voie.

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Catégorie de dépense — utilisée aussi pour le parsing vocal.
enum class TypeCharge {
    DIESEL,     // plein de carburant
    REPARATION, // réparation ou entretien mécanique
    AUTRE       // toute autre dépense (péage, lavage, etc.)
}

@Entity(
    tableName = "charges",
    foreignKeys = [
        ForeignKey(
            entity = TaxiEntity::class,
            parentColumns = ["id"],
            childColumns = ["taxiId"],
            onDelete = ForeignKey.CASCADE // toutes les charges supprimées si le taxi est effacé
        )
    ],
    indices = [Index("taxiId")]
)
data class ChargeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taxiId: Long,                                     // taxi concerné par la dépense
    val type: TypeCharge,                                 // catégorie (DIESEL, REPARATION, AUTRE)
    val description: String,                              // description libre (ex : "Vidange huile")
    val montant: Double,                                  // montant en MAD
    val date: Long = System.currentTimeMillis(),          // date de la dépense
    val kilometrageAuMoment: Int? = null                  // kilométrage au moment de la charge (optionnel)
)
