package com.example.taxipilot.core.data.repository

import android.util.Log
import com.example.taxipilot.core.data.firestore.FirestoreServiceLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreServiceLogs"

class FirestoreServiceLogRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("serviceLogs")

    /** Appends a status-change entry for this chauffeur. */
    suspend fun log(entry: FirestoreServiceLog) {
        runCatching {
            val doc = col.document()
            doc.set(entry.copy(id = doc.id).toMap()).await()
        }.onFailure { Log.w(TAG, "log error: ${it.message}") }
    }

    /** Returns all log entries for a given chauffeur, newest first. */
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
