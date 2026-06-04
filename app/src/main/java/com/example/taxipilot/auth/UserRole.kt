package com.example.taxipilot.auth

// Enum des rôles utilisateur Firebase Auth (côté app, dans le module :app).
// Distinct de FirestoreUser.ROLE_* (chaînes Firestore) et de UserRole Room (UserEntity).
// label() retourne le texte affiché dans l'interface (RegisterScreen, ProfileScreen).

enum class UserRole {
    PROPRIETAIRE, // propriétaire d'une ou plusieurs flottes de taxis
    CHAUFFEUR,    // chauffeur employé par un propriétaire
    CLIENT;       // passager qui réserve des courses

    // Nom affiché dans l'UI (boutons segmentés de RegisterScreen)
    fun label() = when (this) {
        PROPRIETAIRE -> "Propriétaire"
        CHAUFFEUR    -> "Chauffeur"
        CLIENT       -> "Client"
    }
}
