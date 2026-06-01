package com.example.taxipilot.core.data.firestore

data class FirestoreCharge(
    val id: String = "",
    val chauffeurUid: String = "",
    val proprietaireId: String = "",
    val type: String = "",
    val description: String = "",
    val montant: Double = 0.0,
    val date: Long = System.currentTimeMillis()
) {
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
