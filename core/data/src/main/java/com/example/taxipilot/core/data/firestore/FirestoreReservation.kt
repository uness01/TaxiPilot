package com.example.taxipilot.core.data.firestore

/**
 * Firestore document model for a taxi reservation.
 * This is the single source of truth for the reservation flow —
 * Room ReservationEntity is retired from the booking flow.
 */
data class FirestoreReservation(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val depart: String = "",
    val arrivee: String = "",
    val prixEstime: Double = 0.0,
    val type: String = TYPE_IMMEDIATE,
    val scheduledTime: Long? = null,
    val status: String = STATUS_EN_ATTENTE,
    val chauffeurId: String? = null,
    val chauffeurName: String? = null,
    val proprietaireId: String? = null,
    val taxiAssigned: String? = null,   // matricule of the assigned taxi
    val prixFinal: Double? = null,
    val distanceReelle: Double? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    // Geo-coordinates saved at booking time from OSMDroid map picker
    val departLat: Double? = null,
    val departLng: Double? = null,
    val arriveeLat: Double? = null,
    val arriveeLng: Double? = null,
    val distanceKm: Double = 0.0
) {
    companion object {
        const val TYPE_IMMEDIATE = "immediate"
        const val TYPE_PLANIFIEE = "planifiee"

        const val STATUS_EN_ATTENTE = "en_attente"
        const val STATUS_ACCEPTEE   = "acceptee"
        const val STATUS_EN_COURS   = "en_cours"
        const val STATUS_TERMINEE   = "terminee"
        const val STATUS_ANNULEE    = "annulee"
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id"             to id,
        "clientId"       to clientId,
        "clientName"     to clientName,
        "clientPhone"    to clientPhone,
        "depart"         to depart,
        "arrivee"        to arrivee,
        "prixEstime"     to prixEstime,
        "type"           to type,
        "scheduledTime"  to scheduledTime,
        "status"         to status,
        "chauffeurId"    to chauffeurId,
        "chauffeurName"  to chauffeurName,
        "proprietaireId" to proprietaireId,
        "taxiAssigned"   to taxiAssigned,
        "prixFinal"      to prixFinal,
        "distanceReelle" to distanceReelle,
        "completedAt"    to completedAt,
        "createdAt"      to createdAt,
        "departLat"      to departLat,
        "departLng"      to departLng,
        "arriveeLat"     to arriveeLat,
        "arriveeLng"     to arriveeLng,
        "distanceKm"     to distanceKm
    )

    val isImmediate get() = type == TYPE_IMMEDIATE
    val isActive    get() = status in listOf(STATUS_ACCEPTEE, STATUS_EN_COURS)
    val isFinished  get() = status in listOf(STATUS_TERMINEE, STATUS_ANNULEE)
}
