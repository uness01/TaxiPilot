package com.example.taxipilot.core.data.firestore

// Modèle de document Firestore pour un utilisateur (chauffeur ou propriétaire).
// Placé dans core:data (pas dans app:auth) pour que les modules feature (owner, driver)
// puissent l'utiliser sans dépendre du module app.
//
// Deux types d'utilisateurs ont des champs spécifiques :
//   - PROPRIETAIRE : codeProprietaire (partagé avec ses chauffeurs pour le liage)
//   - CHAUFFEUR    : proprietaireId (lien vers son patron), assignedTaxi (matricule),
//                    statut (en_service/hors_service/en_course), currentLat/Lng (GPS temps réel)

data class FirestoreUser(
    val uid: String = "",                                // UID Firebase Auth
    val nom: String = "",                                // nom complet
    val telephone: String = "",                          // numéro de téléphone
    val role: String = "",                               // "CHAUFFEUR" ou "PROPRIETAIRE"
    val fcmToken: String? = null,                        // jeton FCM pour les notifications push
    // Champs Propriétaire uniquement
    val codeProprietaire: String? = null,                // code 6 chiffres partagé avec les chauffeurs
    // Champs Chauffeur uniquement
    val proprietaireId: String? = null,                  // UID du propriétaire auquel il est lié
    val assignedTaxi: String? = null,                    // matricule du taxi assigné par le propriétaire
    val statut: String = STATUT_DISPONIBLE,              // état de service du chauffeur
    // Position GPS en temps réel (mise à jour toutes les ~15 s pendant une course)
    val currentLat: Double? = null,
    val currentLng: Double? = null
) {
    companion object {
        // Rôles Firestore (correspondent aux valeurs stockées dans le champ "role")
        const val ROLE_CHAUFFEUR      = "CHAUFFEUR"
        const val ROLE_PROPRIETAIRE   = "PROPRIETAIRE"

        // Statuts de service du chauffeur
        const val STATUT_DISPONIBLE   = "disponible"   // ancien statut (traité comme en_service)
        const val STATUT_EN_SERVICE   = "en_service"   // connecté et disponible pour des courses
        const val STATUT_HORS_SERVICE = "hors_service" // déconnecté, ne reçoit pas de notifications
        const val STATUT_EN_COURSE    = "en_course"    // actuellement en train d'effectuer une course
    }
}
