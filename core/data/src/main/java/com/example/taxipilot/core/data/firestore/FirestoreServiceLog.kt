package com.example.taxipilot.core.data.firestore

// Modèle de document Firestore pour un journal de changements de statut du chauffeur.
// Chaque fois qu'un chauffeur passe en_service ↔ hors_service, une entrée est créée.
// Le propriétaire peut consulter ces logs depuis son tableau de bord (onglet Chauffeurs).
// Stocké dans la collection Firestore "serviceLogs".

data class FirestoreServiceLog(
    val id: String = "",                              // ID du document Firestore (auto-généré)
    val chauffeurUid: String = "",                    // UID Firebase du chauffeur
    val proprietaireId: String = "",                  // UID du propriétaire (pour filtrer les logs)
    val statut: String = "",                          // nouveau statut ("en_service" ou "hors_service")
    val timestamp: Long = System.currentTimeMillis()  // horodatage du changement
) {
    // Convertit l'objet en Map pour l'écriture dans Firestore
    fun toMap(): Map<String, Any> = mapOf(
        "id"             to id,
        "chauffeurUid"   to chauffeurUid,
        "proprietaireId" to proprietaireId,
        "statut"         to statut,
        "timestamp"      to timestamp
    )
}
