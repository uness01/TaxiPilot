package com.example.taxipilot.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.taxipilot.MainActivity
import com.example.taxipilot.TaxiPilotApp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private const val TAG = "NotifListenerService"

/**
 * Foreground service that maintains a real-time Firestore listener on the
 * "notifications" collection.  When a new document targeting CHAUFFEUR role
 * appears (created after this service started), it fires a local Android
 * notification so the chauffeur sees the alert even if the app is in the
 * background or the screen is off.
 *
 * The service runs as long as the chauffeur is logged in.  Start/stop is
 * driven by TaxiPilotNavHost via DisposableEffect.
 */
class NotificationListenerService : Service() {

    private val db = FirebaseFirestore.getInstance()
    private var firestoreReg: ListenerRegistration? = null

    // Only show notifications created AFTER this service instance started —
    // prevents re-alerting for historical documents on initial snapshot delivery.
    private val serviceStartTime = System.currentTimeMillis()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID_FOREGROUND, buildForegroundNotification())
        attachFirestoreListener()
        return START_STICKY   // restart automatically if killed by the OS
    }

    private fun attachFirestoreListener() {
        firestoreReg?.remove()
        firestoreReg = db.collection("notifications")
            .whereEqualTo("targetRole", "CHAUFFEUR")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e(TAG, "Firestore listener error: ${err.message}")
                    return@addSnapshotListener
                }
                snap?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val createdAt = change.document.getLong("createdAt") ?: 0L
                        if (createdAt >= serviceStartTime) {
                            val message = change.document.getString("message") ?: return@forEach
                            val reservationId = change.document.getString("reservationId")
                            // Use the Firestore docId as notification ID — same ID used by
                            // TaxiPilotMessagingService — so only one banner shows per alert.
                            showCourseNotification(
                                message      = message,
                                notifId      = change.document.id.hashCode(),
                                reservationId = reservationId
                            )
                        }
                    }
                }
            }
    }

    private fun showCourseNotification(
        message: String,
        notifId: Int,
        reservationId: String? = null
    ) {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (reservationId != null) putExtra(MainActivity.EXTRA_RESERVATION_ID, reservationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notifId, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(this, TaxiPilotApp.CHANNEL_COURSES)
            .setContentTitle("Nouvelle course disponible")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notif)
    }

    /** Silent, minimal notification required to keep the foreground service alive. */
    private fun buildForegroundNotification() =
        NotificationCompat.Builder(this, TaxiPilotApp.CHANNEL_SERVICE)
            .setContentTitle("TaxiPilot")
            .setContentText("En attente de nouvelles courses…")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        firestoreReg?.remove()
        firestoreReg = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIF_ID_FOREGROUND = 9001

        fun start(context: Context) {
            val intent = Intent(context, NotificationListenerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NotificationListenerService::class.java))
        }
    }
}
