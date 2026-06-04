package com.example.taxipilot

// Repository d'envoi de notifications push pour les nouvelles réservations.
// Double canal d'alerte pour maximiser la fiabilité :
//
//   Canal 1 — Firestore "notifications" collection :
//     → NotificationListenerService le surveille en temps réel
//     → Fonctionne quand l'app est ouverte ou en arrière-plan avec le service actif
//
//   Canal 2 — FCM Legacy HTTP API (POST vers fcm.googleapis.com/fcm/send) :
//     → Réveille l'app même quand elle est complètement tuée (killed)
//     → Nécessite FCM_SERVER_KEY à configurer depuis Firebase Console
//
// Configuration : remplacer "YOUR_FCM_SERVER_KEY_HERE" par la clé serveur FCM
//   Firebase Console → Project Settings → Cloud Messaging → Legacy server key

import android.util.Log
import com.example.taxipilot.core.data.firestore.FirestoreNotification
import com.example.taxipilot.core.data.repository.FirestoreNotificationRepository
import com.example.taxipilot.core.data.repository.FirestoreUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "FcmRepository"

class FcmRepository(
    private val notificationRepository: FirestoreNotificationRepository = FirestoreNotificationRepository(),
    private val userRepository: FirestoreUserRepository = FirestoreUserRepository()
) {
    // ▼ REMPLACER PAR LA CLEF SERVEUR FCM depuis Firebase Console → Cloud Messaging ▼
    private val FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE"
    // ▲──────────────────────────────────────────────────────────────────────────────▲

    // Envoie une alerte de nouvelle réservation sur les deux canaux (Firestore + FCM)
    // Appelé par ClientViewModel.createReservation() après création d'une réservation
    suspend fun sendNewReservationNotification(depart: String, arrivee: String) {
        // Canal 1 : écrit dans Firestore → NotificationListenerService la reçoit en temps réel
        val docId = notificationRepository.create(
            FirestoreNotification(
                type       = FirestoreNotification.TYPE_NEW_RESERVATION,
                message    = "$depart → $arrivee",
                targetRole = FirestoreNotification.ROLE_CHAUFFEUR,
                createdAt  = System.currentTimeMillis()
            )
        )

        // Canal 2 : push FCM — skip si la clé n'est pas configurée
        if (FCM_SERVER_KEY == "YOUR_FCM_SERVER_KEY_HERE") {
            Log.w(TAG, "FCM server key not configured — notifications only work while app is open")
            return
        }
        // Ne notifie que les chauffeurs en_service (pas ceux hors_service ou en_course)
        val tokens = userRepository.getActiveChauffeurFcmTokens()
        if (tokens.isEmpty()) {
            Log.d(TAG, "No chauffeur FCM tokens found")
            return
        }
        sendFcmPush(tokens, depart, arrivee, docId)
    }

    // Envoie le payload FCM aux tokens par lots de 1000 (limite de l'API FCM Legacy)
    // Inclut à la fois le payload "notification" (affiché auto en background) et "data" (pour deduplication)
    private suspend fun sendFcmPush(
        tokens: List<String>,
        depart: String,
        arrivee: String,
        notifDocId: String // ID Firestore partagé avec NotificationListenerService → pas de doublon
    ) = withContext(Dispatchers.IO) {
        tokens.chunked(1000).forEach { chunk ->
            try {
                val conn = (URL("https://fcm.googleapis.com/fcm/send").openConnection()
                        as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "key=$FCM_SERVER_KEY")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput      = true
                    connectTimeout = 10_000
                    readTimeout    = 10_000
                }

                val payload = JSONObject().apply {
                    put("registration_ids", JSONArray(chunk)) // liste des tokens destinataires
                    put("priority", "high")                   // wake-lock sur Android
                    // Payload "notification" — affiché automatiquement en background/tué par le système
                    put("notification", JSONObject().apply {
                        put("title",      "Nouvelle course disponible")
                        put("body",       "$depart → $arrivee")
                        put("channel_id", TaxiPilotApp.CHANNEL_COURSES)
                        put("sound",      "default")
                    })
                    // Payload "data" — traité par onMessageReceived() en premier plan + deduplication
                    put("data", JSONObject().apply {
                        put("type",         "new_reservation")
                        put("depart",       depart)
                        put("arrivee",      arrivee)
                        put("notif_doc_id", notifDocId) // même ID que le document Firestore
                    })
                }

                conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                if (code == 200) Log.d(TAG, "FCM sent to ${chunk.size} tokens")
                else Log.w(TAG, "FCM HTTP $code: ${conn.errorStream?.bufferedReader()?.readText()}")
                conn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "FCM send error: ${e.message}")
            }
        }
    }
}
