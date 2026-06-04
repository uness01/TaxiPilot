package com.example.taxipilot.notification

// Service foreground — maintient un listener Firestore actif en arrière-plan pour les chauffeurs.
// Rôle : afficher une notification Android dès qu'une nouvelle course apparaît dans Firestore,
// même quand l'app est en arrière-plan ou que l'écran est éteint.
//
// Architecture :
//   - START_STICKY : redémarré automatiquement par Android si tué par le système
//   - serviceStartTime : ignore les documents Firestore créés AVANT le démarrage du service
//     (évite de ré-alerter pour l'historique existant au premier snapshot)
//   - Notification foreground silencieuse (CHANNEL_SERVICE) requise depuis Android O
//
// Déduplication avec TaxiPilotMessagingService :
//   Les deux services utilisent change.document.id.hashCode() comme notifId.
//   → Si FCM et Firestore arrivent en même temps, le deuxième écrase le premier (pas de doublon).
//
// Cycle de vie contrôlé depuis TaxiPilotNavHost :
//   Chauffeur passe hors_service → stop() | Chauffeur se déconnecte → stop() (DisposableEffect)

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

class NotificationListenerService : Service() {

    private val db = FirebaseFirestore.getInstance()
    private var firestoreReg: ListenerRegistration? = null

    // Horodatage de démarrage du service — seuls les documents PLUS RÉCENTS sont alertés
    private val serviceStartTime = System.currentTimeMillis()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Démarre en foreground avec une notification silencieuse (obligatoire Android O+)
        startForeground(NOTIF_ID_FOREGROUND, buildForegroundNotification())
        attachFirestoreListener() // commence à écouter la collection "notifications"
        return START_STICKY       // redémarré par Android si tué
    }

    // Attache le listener Firestore sur la collection "notifications" ciblant les CHAUFFEUR
    private fun attachFirestoreListener() {
        firestoreReg?.remove() // supprime l'ancien listener si déjà actif
        firestoreReg = db.collection("notifications")
            .whereEqualTo("targetRole", "CHAUFFEUR")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e(TAG, "Firestore listener error: ${err.message}")
                    return@addSnapshotListener
                }
                snap?.documentChanges?.forEach { change ->
                    // Seuls les nouveaux documents (pas les mises à jour ni suppressions)
                    if (change.type == DocumentChange.Type.ADDED) {
                        val createdAt = change.document.getLong("createdAt") ?: 0L
                        // Ignore les documents antérieurs au démarrage du service (historique)
                        if (createdAt >= serviceStartTime) {
                            val message = change.document.getString("message") ?: return@forEach
                            val reservationId = change.document.getString("reservationId")
                            // ID partagé avec TaxiPilotMessagingService → pas de doublon
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

    // Affiche une notification Android heads-up avec vibration pour alerter le chauffeur
    // Taper dessus ouvre MainActivity et navigue vers la réservation si reservationId est fourni
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
            .setPriority(NotificationCompat.PRIORITY_HIGH) // bandeau heads-up
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))     // vibration pour attirer l'attention
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notif)
    }

    // Notification foreground silencieuse — requise pour maintenir le service en vie (Android O+)
    private fun buildForegroundNotification() =
        NotificationCompat.Builder(this, TaxiPilotApp.CHANNEL_SERVICE)
            .setContentTitle("TaxiPilot")
            .setContentText("En attente de nouvelles courses…")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)  // pas de son ni de vibration pour la notification de service
            .setOngoing(true) // non supprimable par l'utilisateur
            .build()

    override fun onDestroy() {
        firestoreReg?.remove() // supprime le listener Firestore pour éviter les fuites mémoire
        firestoreReg = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null // pas de binding (service démarré, pas lié)

    companion object {
        private const val NOTIF_ID_FOREGROUND = 9001 // ID fixe de la notification foreground

        // Démarre le service (startForegroundService sur Android O+, startService sinon)
        fun start(context: Context) {
            val intent = Intent(context, NotificationListenerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        // Arrête le service et supprime le listener Firestore
        fun stop(context: Context) {
            context.stopService(Intent(context, NotificationListenerService::class.java))
        }
    }
}
