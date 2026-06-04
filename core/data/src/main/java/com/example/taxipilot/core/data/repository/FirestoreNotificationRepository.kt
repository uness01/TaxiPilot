package com.example.taxipilot.core.data.repository

// Repository Firestore pour les notifications internes.
// Deux consommateurs de cette collection :
//   1. NotificationListenerService (service foreground du chauffeur) :
//      écoute les nouveaux documents ciblant CHAUFFEUR et affiche des notifications Android
//   2. OwnerViewModel :
//      écoute les messages ciblant le PROPRIETAIRE précis (changements de statut des chauffeurs)
//
// La méthode create() retourne l'ID Firestore du document créé.
// Cet ID est partagé avec TaxiPilotMessagingService pour dédupliquer les notifications
// (un seul bandeau Android même si FCM + Firestore arrivent quasi simultanément).

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
    private val col = db.collection("notifications") // collection Firestore des notifications

    // Flux des NOUVEAUX messages pour un propriétaire précis.
    // N'émet que les documents créés APRÈS le démarrage du listener (sinon re-alerte l'historique).
    // Filtre sur targetRole = PROPRIETAIRE et targetUid = proprietaireUid.
    fun getNewMessagesForProprietaire(
        proprietaireUid: String,
        sinceTimestamp: Long = System.currentTimeMillis() // ignore les documents plus anciens
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
                    // Seulement les nouvelles insertions (pas les mises à jour ou suppressions)
                    if (change.type == DocumentChange.Type.ADDED) {
                        val createdAt = change.document.getLong("createdAt") ?: 0L
                        if (createdAt >= sinceTimestamp) {
                            val msg = change.document.getString("message") ?: return@forEach
                            trySend(msg) // émet le message dans le Flow
                        }
                    }
                }
            }
        awaitClose { reg.remove() }
    }

    // Crée un document de notification dans Firestore et retourne son ID.
    // Retourne une chaîne vide en cas d'erreur (non bloquant).
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
