package com.example.taxipilot.core.data.repository

import android.util.Log
import com.example.taxipilot.core.data.firestore.FirestoreNotification
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreNotifications"

class FirestoreNotificationRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("notifications")

    /**
     * Writes a notification document to Firestore.
     * Chauffeurs' foreground service listens to this collection and shows
     * a local Android notification when a new document appears.
     */
    /**
     * Emits each NEW PROPRIETAIRE notification message targeted at [proprietaireUid]
     * as it arrives in real-time (only documents added after subscription).
     */
    fun getNewMessagesForProprietaire(
        proprietaireUid: String,
        sinceTimestamp: Long = System.currentTimeMillis()
    ): Flow<String> = callbackFlow {
        val reg = col
            .whereEqualTo("targetRole", FirestoreNotification.ROLE_PROPRIETAIRE)
            .whereEqualTo("targetUid", proprietaireUid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e(TAG, "Proprietaire notif listener error: ${err.message}")
                    return@addSnapshotListener
                }
                snap?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val createdAt = change.document.getLong("createdAt") ?: 0L
                        if (createdAt >= sinceTimestamp) {
                            val msg = change.document.getString("message") ?: return@forEach
                            trySend(msg)
                        }
                    }
                }
            }
        awaitClose { reg.remove() }
    }

    /** Creates the notification document and returns its Firestore ID (empty string on failure). */
    suspend fun create(notification: FirestoreNotification): String {
        return runCatching {
            val doc = col.document()
            doc.set(notification.copy(id = doc.id).toMap()).await()
            doc.id
        }.getOrElse {
            Log.w(TAG, "create error: ${it.message}")
            ""
        }
    }
}
