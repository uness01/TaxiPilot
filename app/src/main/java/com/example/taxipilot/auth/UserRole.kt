package com.example.taxipilot.auth

enum class UserRole {
    PROPRIETAIRE,
    CHAUFFEUR,
    CLIENT;

    fun label() = when (this) {
        PROPRIETAIRE -> "Propriétaire"
        CHAUFFEUR    -> "Chauffeur"
        CLIENT       -> "Client"
    }
}
