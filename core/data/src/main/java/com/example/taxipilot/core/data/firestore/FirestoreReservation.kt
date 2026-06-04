package com.example.taxipilot.core.data.firestore

// Modèle de document Firestore pour une réservation de taxi.
// C'est la SOURCE DE VÉRITÉ principale pour tout le flux de réservation en temps réel.
// Room ReservationEntity n'est plus utilisé pour les nouvelles réservations.
//
// Cycle de vie d'une réservation :
//   EN_ATTENTE → (chauffeur accepte) → ACCEPTEE → (démarre) → EN_COURS → (termine) → TERMINEE
//   ou → ANNULEE (par le client à n'importe quelle étape avant TERMINEE)
//
// Les coordonnées GPS (departLat/Lng, arriveeLat/Lng) sont enregistrées au moment de la réservation
// depuis le sélecteur de carte OSMDroid, et utilisées pour le calcul OSRM et la vérification de proximité.

data class FirestoreReservation(
    val id: String = "",                    // ID du document Firestore (auto-généré)
    val clientId: String = "",              // UID Firebase du client
    val clientName: String = "",            // nom saisi par le client
    val clientPhone: String = "",           // téléphone du client
    val depart: String = "",                // adresse de départ (texte)
    val arrivee: String = "",               // adresse d'arrivée (texte)
    val prixEstime: Double = 0.0,           // prix estimé en MAD (calculé avant la course)
    val type: String = TYPE_IMMEDIATE,      // "immediate" ou "planifiee"
    val scheduledTime: Long? = null,        // heure planifiée (null si immédiate)
    val status: String = STATUS_EN_ATTENTE, // état actuel de la réservation
    val chauffeurId: String? = null,        // UID Firebase du chauffeur qui a accepté
    val chauffeurName: String? = null,      // nom du chauffeur (affiché au client)
    val proprietaireId: String? = null,     // UID Firebase du propriétaire du chauffeur
    val taxiAssigned: String? = null,       // matricule du taxi assigné
    val prixFinal: Double? = null,          // prix réel encaissé (null jusqu'à TERMINEE)
    val distanceReelle: Double? = null,     // distance réelle en km (renseignée à la fin)
    val completedAt: Long? = null,          // horodatage de fin de course
    val createdAt: Long = System.currentTimeMillis(), // horodatage de création
    val departLat: Double? = null,          // latitude du point de départ (GPS)
    val departLng: Double? = null,          // longitude du point de départ
    val arriveeLat: Double? = null,         // latitude de la destination (GPS)
    val arriveeLng: Double? = null,         // longitude de la destination
    val distanceKm: Double = 0.0            // distance calculée par OSRM (km)
) {
    companion object {
        // Types de réservation
        const val TYPE_IMMEDIATE = "immediate" // course demandée maintenant
        const val TYPE_PLANIFIEE = "planifiee" // course programmée pour plus tard

        // Statuts possibles d'une réservation (cycle de vie)
        const val STATUS_EN_ATTENTE = "en_attente" // en attente d'un chauffeur
        const val STATUS_ACCEPTEE   = "acceptee"   // chauffeur assigné, pas encore en route
        const val STATUS_EN_COURS   = "en_cours"   // course en train de se dérouler
        const val STATUS_TERMINEE   = "terminee"   // course terminée avec succès
        const val STATUS_ANNULEE    = "annulee"    // course annulée
    }

    // Convertit l'objet en Map pour l'écriture dans Firestore (set/update)
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

    // true si la réservation est une course immédiate (pas planifiée)
    val isImmediate get() = type == TYPE_IMMEDIATE
    // true si la course est acceptée ou en cours (chauffeur actif)
    val isActive    get() = status in listOf(STATUS_ACCEPTEE, STATUS_EN_COURS)
    // true si la course est terminée ou annulée (plus aucune action possible)
    val isFinished  get() = status in listOf(STATUS_TERMINEE, STATUS_ANNULEE)
}
