package com.example.taxipilot.auth

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val nom: String = "",
    val telephone: String = "",
    val role: UserRole = UserRole.CLIENT,
    val fcmToken: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    // Proprietaire: unique 6-digit code shared with their chauffeurs
    val codeProprietaire: String? = null,
    // Chauffeur: set after entering proprietaire code
    val proprietaireId: String? = null,
    // Chauffeur: matricule of assigned taxi (set by proprietaire)
    val assignedTaxi: String? = null,
    // Chauffeur: disponible | en_course
    val statut: String = "disponible"
) {
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
