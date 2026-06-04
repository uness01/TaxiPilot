package com.example.taxipilot.core.data.firestore

// Modèle de document Firestore pour une notification interne.
// Deux usages principaux :
//   1. TYPE_NEW_RESERVATION → envoyé à tous les CHAUFFEURS quand un client réserve
//      (le service NotificationListenerService écoute cette collection en temps réel)
//   2. TYPE_STATUT_CHANGE   → envoyé au PROPRIETAIRE quand un chauffeur change de statut
//      (OwnerViewModel écoute via getNewMessagesForProprietaire)
//
// targetRole = qui reçoit la notification ("CHAUFFEUR" ou "PROPRIETAIRE")
// targetUid  = UID spécifique (utilisé pour cibler un propriétaire précis)

data class FirestoreNotification(
    val id: String = "",                              // ID du document Firestore
    val type: String = "",                            // type de notification (voir constantes)
    val message: String = "",                         // texte affiché dans la notification Android
    val targetRole: String = "",                      // rôle ciblé : "CHAUFFEUR" ou "PROPRIETAIRE"
    val targetUid: String = "",                       // UID d'un utilisateur spécifique (optionnel)
    val createdAt: Long = System.currentTimeMillis()  // horodatage de création
) {
    companion object {
        // Types de notification
        const val TYPE_NEW_RESERVATION = "new_reservation" // nouvelle course disponible
        const val TYPE_STATUT_CHANGE   = "statut_change"   // changement de statut d'un chauffeur

        // Rôles ciblés
        const val ROLE_CHAUFFEUR    = "CHAUFFEUR"
        const val ROLE_PROPRIETAIRE = "PROPRIETAIRE"
    }

    // Convertit l'objet en Map pour l'écriture dans Firestore
    fun toMap(): Map<String, Any> = mapOf(
        "id"         to id,
        "type"       to type,
        "message"    to message,
        "targetRole" to targetRole,
        "targetUid"  to targetUid,
        "createdAt"  to createdAt
    )
}
