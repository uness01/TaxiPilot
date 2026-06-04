package com.example.taxipilot.core.data.repository

// Repository Firestore pour les journaux de service (logs de changements de statut).
// Chaque fois qu'un chauffeur bascule entre en_service ↔ hors_service, une entrée est loguée.
// Utilisé par OwnerViewModel pour afficher l'historique de présence des chauffeurs.
// Collection Firestore : "serviceLogs"

import android.util.Log
import com.example.taxipilot.core.data.firestore.FirestoreServiceLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreServiceLogs"

class FirestoreServiceLogRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("serviceLogs") // collection Firestore des logs de service

    // Enregistre un changement de statut dans Firestore (non bloquant en cas d'erreur)
    suspend fun log(entry: FirestoreServiceLog) {
        runCatching {
            val doc = col.document()
            doc.set(entry.copy(id = doc.id).toMap()).await()
        }.onFailure { Log.w(TAG, "log error: ${it.message}") }
    }

    // Retourne tous les logs d'un chauffeur, triés du plus récent au plus ancien (one-shot)
    // Utilisé dans OwnerViewModel.getServiceLogs() quand le propriétaire consulte un chauffeur
    suspend fun getByChauffeur(chauffeurUid: String): List<FirestoreServiceLog> = runCatching {
        col.whereEqualTo("chauffeurUid", chauffeurUid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()
            .documents
            .mapNotNull { doc ->
                FirestoreServiceLog(
                    id             = doc.getString("id") ?: doc.id,
                    chauffeurUid   = doc.getString("chauffeurUid") ?: "",
                    proprietaireId = doc.getString("proprietaireId") ?: "",
                    statut         = doc.getString("statut") ?: "",
                    timestamp      = doc.getLong("timestamp") ?: 0L
                )
            }
    }.getOrElse {
        Log.w(TAG, "getByChauffeur error: ${it.message}")
        emptyList()
    }
}
