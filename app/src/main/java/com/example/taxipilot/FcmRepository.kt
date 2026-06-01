package com.example.taxipilot

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

/**
 * Sends new-reservation alerts via two channels:
 *
 * 1. **Firestore** `notifications` collection — picked up by [NotificationListenerService]
 *    while the chauffeur's app is open/in background with the service alive.
 *
 * 2. **FCM Legacy HTTP API** — wakes the app even when it is completely killed.
 *    Requires [FCM_SERVER_KEY] to be filled in from Firebase Console.
 *
 * How to configure FCM_SERVER_KEY:
 *   Firebase Console → Project Settings → Cloud Messaging → Legacy server key
 */
class FcmRepository(
    private val notificationRepository: FirestoreNotificationRepository = FirestoreNotificationRepository(),
    private val userRepository: FirestoreUserRepository = FirestoreUserRepository()
) {
    // ▼▼▼  PASTE YOUR FCM SERVER KEY FROM FIREBASE CONSOLE → Cloud Messaging  ▼▼▼
    private val FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE"
    // ▲▲▲────────────────────────────────────────────────────────────────────▲▲▲

    suspend fun sendNewReservationNotification(depart: String, arrivee: String) {
        // 1. Write to Firestore — NotificationListenerService receives it in real-time
        val docId = notificationRepository.create(
            FirestoreNotification(
                type       = FirestoreNotification.TYPE_NEW_RESERVATION,
                message    = "$depart → $arrivee",
                targetRole = FirestoreNotification.ROLE_CHAUFFEUR,
                createdAt  = System.currentTimeMillis()
            )
        )

        // 2. Send FCM push — delivers even when app is killed
        if (FCM_SERVER_KEY == "YOUR_FCM_SERVER_KEY_HERE") {
            Log.w(TAG, "FCM server key not configured — notifications only work while app is open")
            return
        }
        // Only notify chauffeurs who are en_service — hors_service and en_course are excluded
        val tokens = userRepository.getActiveChauffeurFcmTokens()
        if (tokens.isEmpty()) {
            Log.d(TAG, "No chauffeur FCM tokens found")
            return
        }
        sendFcmPush(tokens, depart, arrivee, docId)
    }

    private suspend fun sendFcmPush(
        tokens: List<String>,
        depart: String,
        arrivee: String,
        notifDocId: String
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
                    put("registration_ids", JSONArray(chunk))
                    put("priority", "high")
                    // notification payload — auto-shown in background/killed
                    put("notification", JSONObject().apply {
                        put("title",      "Nouvelle course disponible")
                        put("body",       "$depart → $arrivee")
                        put("channel_id", TaxiPilotApp.CHANNEL_COURSES)
                        put("sound",      "default")
                    })
                    // data payload — used for deduplication + deep-link
                    put("data", JSONObject().apply {
                        put("type",         "new_reservation")
                        put("depart",       depart)
                        put("arrivee",      arrivee)
                        put("notif_doc_id", notifDocId)  // shared with Firestore doc → same notification ID
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
