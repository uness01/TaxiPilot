package com.example.taxipilot.auth

// Modèle de profil utilisateur côté app (Firebase Auth + Firestore).
// Représente le document Firestore collection "users" mappé pour l'usage dans l'app.
// Distinct de FirestoreUser (utilisé dans les modules feature) pour éviter la dépendance circulaire.
//
// Champs spécifiques par rôle :
//   PROPRIETAIRE : codeProprietaire (6 chiffres, partagé avec ses chauffeurs)
//   CHAUFFEUR    : proprietaireId, assignedTaxi (matricule), statut
//
// toMap() sérialise le profil pour l'écriture dans Firestore lors de l'inscription.

data class UserProfile(
    val uid: String = "",              // UID Firebase Auth (identifiant unique global)
    val email: String = "",            // email de connexion
    val nom: String = "",              // nom complet
    val telephone: String = "",        // numéro de téléphone
    val role: UserRole = UserRole.CLIENT,
    val fcmToken: String? = null,      // jeton FCM pour les notifications push
    val createdAt: Long = System.currentTimeMillis(),
    // Propriétaire seulement : code à 6 chiffres partagé avec ses chauffeurs pour le liage
    val codeProprietaire: String? = null,
    // Chauffeur seulement : UID du propriétaire auquel il est lié (null tant que non lié)
    val proprietaireId: String? = null,
    // Chauffeur seulement : matricule du taxi assigné par son propriétaire
    val assignedTaxi: String? = null,
    // Chauffeur seulement : "disponible" | "en_service" | "hors_service" | "en_course"
    val statut: String = "disponible"
) {
    // Sérialise le profil en Map pour set() dans Firestore (utilisé à l'inscription)
    fun toMap(): Map<String, Any?> = mapOf(
        "uid"              to uid,
        "email"            to email,
        "nom"              to nom,
        "telephone"        to telephone,
        "role"             to role.name,
        "fcmToken"         to fcmToken,
        "createdAt"        to createdAt,
        "codeProprietaire" to codeProprietaire,
        "proprietaireId"   to proprietaireId,
        "assignedTaxi"     to assignedTaxi,
        "statut"           to statut
    )
}
