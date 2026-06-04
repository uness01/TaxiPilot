package com.example.taxipilot.core.data.database.entity

// Table "users" — stocke les utilisateurs locaux de l'app (auth locale Room).
// Distinct de FirestoreUser qui est le profil cloud Firebase.
// UserRole détermine quel écran est affiché après connexion.

import androidx.room.Entity
import androidx.room.PrimaryKey

// Rôle d'un utilisateur : propriétaire de flotte, chauffeur, ou client passager.
enum class UserRole { OWNER, DRIVER, CLIENT }

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // clé primaire auto-incrémentée
    val name: String,                                   // nom complet
    val phone: String,                                  // numéro de téléphone
    val role: UserRole,                                 // rôle dans l'application
    val createdAt: Long = System.currentTimeMillis()    // horodatage de création (ms depuis epoch)
)
