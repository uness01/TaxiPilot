package com.example.taxipilot.core.data.repository

// Repository Firestore pour les positions GPS des chauffeurs en cours de course.
// Collection Firestore : "activeLocations" — un document par chauffeur actif.
// La position est mise à jour toutes les ~15 secondes pendant une course.
// Quand la course se termine, le document est supprimé (removeLocation).
// Utilisé par OwnerViewModel pour afficher la flotte en temps réel sur la carte.

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Modèle de données pour une position GPS de chauffeur actif
data class DriverLocation(
    val chauffeurId: String = "",   // UID Firebase du chauffeur
    val chauffeurNom: String = "",  // nom affiché sur la carte
    val lat: Double = 0.0,          // latitude GPS
    val lng: Double = 0.0,          // longitude GPS
    val destination: String = "",   // adresse de destination (affichée sur la carte)
    val updatedAt: Long = 0L        // horodatage de la dernière mise à jour
)

class LocationRepository {

    private val col = FirebaseFirestore.getInstance().collection("activeLocations")

    // Flux temps réel de toutes les positions GPS des chauffeurs actifs
    // (utilisé par la carte de flotte du propriétaire — OwnerFleetMapTab)
    fun getActiveLocations(): Flow<List<DriverLocation>> = callbackFlow {
        val listener = col.addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { doc ->
                DriverLocation(
                    chauffeurId  = doc.getString("chauffeurId")  ?: return@mapNotNull null,
                    chauffeurNom = doc.getString("chauffeurNom") ?: "",
                    lat          = doc.getDouble("lat")          ?: return@mapNotNull null,
                    lng          = doc.getDouble("lng")          ?: return@mapNotNull null,
                    destination  = doc.getString("destination")  ?: "",
                    updatedAt    = doc.getLong("updatedAt")      ?: 0L
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // Met à jour (ou crée) le document GPS du chauffeur dans Firestore
    // Appelé par DriverViewModel.onLocationUpdate() à chaque mise à jour GPS pendant une course
    suspend fun updateLocation(
        chauffeurId: String,
        chauffeurNom: String,
        lat: Double,
        lng: Double,
        destination: String
    ) {
        col.document(chauffeurId).set(
            mapOf(
                "chauffeurId"  to chauffeurId,
                "chauffeurNom" to chauffeurNom,
                "lat"          to lat,
                "lng"          to lng,
                "destination"  to destination,
                "updatedAt"    to System.currentTimeMillis()
            )
        ).await()
    }

    // Supprime le document GPS du chauffeur quand sa course est terminée
    // Appelé par DriverViewModel.stopLocationSharing()
    suspend fun removeLocation(chauffeurId: String) {
        runCatching { col.document(chauffeurId).delete().await() }
    }
}
