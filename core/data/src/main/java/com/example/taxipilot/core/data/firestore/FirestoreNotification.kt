package com.example.taxipilot.core.data.firestore

data class FirestoreNotification(
    val id: String = "",
    val type: String = "",
    val message: String = "",
    val targetRole: String = "",
    /** Optional: UID of a specific user to target (e.g. proprietaire UID). */
    val targetUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_NEW_RESERVATION = "new_reservation"
        const val TYPE_STATUT_CHANGE   = "statut_change"
        const val ROLE_CHAUFFEUR       = "CHAUFFEUR"
        const val ROLE_PROPRIETAIRE    = "PROPRIETAIRE"
    }

    fun toMap(): Map<String, Any> = mapOf(
        "id"         to id,
        "type"       to type,
        "message"    to message,
        "targetRole" to targetRole,
        "targetUid"  to targetUid,
        "createdAt"  to createdAt
    )
}
