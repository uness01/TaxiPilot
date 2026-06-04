package com.example.taxipilot.core.data.firestore

// Modèle de document Firestore pour une dépense (charge) déclarée par un chauffeur.
// Miroir cloud de ChargeEntity (Room) — permet au propriétaire de voir les dépenses
// de ses chauffeurs en temps réel sur son tableau de bord.
//
// Flux de création :
//   Chauffeur saisit une charge (formulaire ou vocal)
//     → ChargeEntity insérée en local (Room)
//     → FirestoreCharge créée dans Firestore (visible par le propriétaire)

data class FirestoreCharge(
    val id: String = "",                              // ID du document Firestore (auto-généré)
    val chauffeurUid: String = "",                    // UID Firebase du chauffeur qui a déclaré la dépense
    val proprietaireId: String = "",                  // UID du propriétaire (pour filtrer les charges)
    val type: String = "",                            // catégorie : "DIESEL", "REPARATION" ou "AUTRE"
    val description: String = "",                     // description libre (ex : "Vidange moteur")
    val montant: Double = 0.0,                        // montant en MAD
    val date: Long = System.currentTimeMillis()       // date de la dépense (ms depuis epoch)
) {
    // Convertit l'objet en Map pour l'écriture dans Firestore
    fun toMap(): Map<String, Any?> = mapOf(
        "id"             to id,
        "chauffeurUid"   to chauffeurUid,
        "proprietaireId" to proprietaireId,
        "type"           to type,
        "description"    to description,
        "montant"        to montant,
        "date"           to date
    )
}
