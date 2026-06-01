package com.example.taxipilot.core.data.firestore

/**
 * Firestore document model for a user (chauffeur or proprietaire).
 * Kept in core:data so feature modules (owner, driver) can use it
 * without depending on the app's auth package.
 */
data class FirestoreUser(
    val uid: String = "",
    val nom: String = "",
    val telephone: String = "",
    val role: String = "",
    val fcmToken: String? = null,
    // Proprietaire only
    val codeProprietaire: String? = null,
    // Chauffeur only
    val proprietaireId: String? = null,
    val assignedTaxi: String? = null,   // matricule of assigned taxi
    val statut: String = STATUT_DISPONIBLE,
    // Live GPS — updated every ~15 s by LocationTracker while en_course
    val currentLat: Double? = null,
    val currentLng: Double? = null
) {
    companion object {
        const val ROLE_CHAUFFEUR      = "CHAUFFEUR"
        const val ROLE_PROPRIETAIRE   = "PROPRIETAIRE"
        const val STATUT_DISPONIBLE   = "disponible"   // legacy — treated as en_service
        const val STATUT_EN_SERVICE   = "en_service"
        const val STATUT_HORS_SERVICE = "hors_service"
        const val STATUT_EN_COURSE    = "en_course"
    }
}
