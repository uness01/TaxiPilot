package com.example.taxipilot.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class DriverLocation(
    val chauffeurId: String = "",
    val chauffeurNom: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val destination: String = "",
    val updatedAt: Long = 0L
)

class LocationRepository {

    private val col = FirebaseFirestore.getInstance().collection("activeLocations")

    /** Real-time stream of all currently-active driver locations. */
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

    suspend fun removeLocation(chauffeurId: String) {
        runCatching { col.document(chauffeurId).delete().await() }
    }
}
