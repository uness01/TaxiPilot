package com.example.taxipilot.core.data.firestore

data class FirestoreServiceLog(
    val id: String = "",
    val chauffeurUid: String = "",
    val proprietaireId: String = "",
    val statut: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id"             to id,
        "chauffeurUid"   to chauffeurUid,
        "proprietaireId" to proprietaireId,
        "statut"         to statut,
        "timestamp"      to timestamp
    )
}
