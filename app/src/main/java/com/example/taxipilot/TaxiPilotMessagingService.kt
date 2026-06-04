package com.example.taxipilot

// Service FCM (Firebase Cloud Messaging) — reçoit les notifications push.
// Deux cas d'utilisation :
//   1. onNewToken() : appelé quand le token FCM change (premier lancement / rotation de token)
//      → enregistre le nouveau token dans Firestore pour que l'app puisse recevoir des pushes
//   2. onMessageReceived() : appelé quand un message FCM arrive en PREMIER PLAN
//      → affiche manuellement une notification Android
//      (en arrière-plan/tué, FCM affiche automatiquement la partie "notification" du payload)
//
// Déduplication : le notifId est basé sur le notif_doc_id Firestore partagé avec
// NotificationListenerService → un seul bandeau Android par alerte même si les deux services
// reçoivent le message quasi simultanément.

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

    // Scope de coroutine lié au cycle de vie du service
    private val serviceJob   = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Appelé quand le token FCM est renouvelé (rare : premier install ou rotation forcée).
    // On met à jour le token dans Firestore pour que les futures notifications push arrivent.
    override fun onNewToken(token: String) {
        val uid = AuthRepository().currentUser?.uid ?: return
        serviceScope.launch {
            AuthRepository().updateFcmToken(uid, token)
        }
    }

    // Appelé quand un message FCM arrive pendant que l'app est en PREMIER PLAN.
    // (En arrière-plan ou tué → FCM affiche automatiquement la partie "notification" du payload)
    // Le notifId est dérivé du notif_doc_id Firestore → même ID que NotificationListenerService
    // → si les deux arrivent presque en même temps, le deuxième écrase le premier (pas de doublons).
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data["title"]
            ?: message.notification?.title
            ?: "Nouvelle course disponible"
        val body = message.data["body"]
            ?: message.notification?.body
            ?: message.data.let { d -> "${d["depart"]} → ${d["arrivee"]}" }.trim(' ', '→', ' ')

        val reservationId = message.data["reservationId"]
        // ID partagé avec NotificationListenerService pour dédupliquer les bandeaux
        val notifId = message.data["notif_doc_id"]?.hashCode()
            ?: System.currentTimeMillis().toInt()
        showForegroundNotification(title, body, notifId, reservationId)
    }

    // Affiche une notification Android avec un Intent qui ouvre MainActivity en tapant dessus
    // Si reservationId est présent, l'activité s'ouvre directement sur la réservation concernée
    private fun showForegroundNotification(
        title: String,
        body: String,
        notifId: Int = 0,
        reservationId: String? = null
    ) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Intent qui ouvre MainActivity et passe l'ID de réservation si disponible
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
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // texte extensible
            .setAutoCancel(true)   // disparaît quand l'utilisateur tape dessus
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // bandeau heads-up
            .build()

        manager.notify(notifId, notification)
    }

    override fun onDestroy() {
        serviceJob.cancel() // annule toutes les coroutines en cours
        super.onDestroy()
    }
}
