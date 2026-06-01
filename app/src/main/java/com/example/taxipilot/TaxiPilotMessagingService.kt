package com.example.taxipilot

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.taxipilot.auth.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TaxiPilotMessagingService : FirebaseMessagingService() {

    private val serviceJob   = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    /**
     * Called when the FCM token is refreshed (rare — first install / token rotation).
     * Save it to Firestore so other devices can reach this one.
     */
    override fun onNewToken(token: String) {
        val uid = AuthRepository().currentUser?.uid ?: return
        serviceScope.launch {
            AuthRepository().updateFcmToken(uid, token)
        }
    }

    /**
     * Called when a message arrives while the app is in the FOREGROUND.
     * For background/killed state, FCM automatically shows the "notification" payload
     * — no code needed for that case.
     */
    /**
     * Called when FCM message arrives while app is in the FOREGROUND.
     * (Background/killed → system shows the `notification` payload automatically.)
     *
     * Uses [notifDocId] as the Android notification ID so that a duplicate
     * from [NotificationListenerService] (Firestore listener) overwrites this
     * one instead of stacking.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data["title"]
            ?: message.notification?.title
            ?: "Nouvelle course disponible"
        val body = message.data["body"]
            ?: message.notification?.body
            ?: message.data.let { d -> "${d["depart"]} → ${d["arrivee"]}" }.trim(' ', '→', ' ')

        val reservationId = message.data["reservationId"]
        // Shared ID with NotificationListenerService — prevents duplicate banner
        val notifId = message.data["notif_doc_id"]?.hashCode()
            ?: System.currentTimeMillis().toInt()
        showForegroundNotification(title, body, notifId, reservationId)
    }

    private fun showForegroundNotification(
        title: String,
        body: String,
        notifId: Int = 0,
        reservationId: String? = null
    ) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (reservationId != null) putExtra(MainActivity.EXTRA_RESERVATION_ID, reservationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notifId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, TaxiPilotApp.CHANNEL_COURSES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(notifId, notification)
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
