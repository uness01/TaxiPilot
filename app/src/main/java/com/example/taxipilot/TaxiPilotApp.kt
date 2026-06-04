package com.example.taxipilot

// Classe Application — point d'initialisation global de l'app.
// Créée avant toute Activity. Responsabilités :
//   1. Initialiser Firebase (Auth + Firestore + FCM)
//   2. Créer les canaux de notification Android (obligatoire depuis Android O / API 26)
//
// Deux canaux de notification :
//   - CHANNEL_COURSES  : IMPORTANCE_HIGH → bandeau visible avec vibration (nouvelles courses)
//   - CHANNEL_SERVICE  : IMPORTANCE_MIN  → silencieux, sans badge (service foreground du chauffeur)

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class TaxiPilotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this) // initialise Firebase (Auth, Firestore, FCM)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // Les canaux de notification n'existent que sur Android O (API 26) et supérieur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Canal "Nouvelles courses" — haute importance → bandeau heads-up avec vibration et LED
            val coursesChannel = NotificationChannel(
                CHANNEL_COURSES,
                "Nouvelles courses",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes de nouvelles courses disponibles"
                enableVibration(true)
                enableLights(true)
            }
            manager.createNotificationChannel(coursesChannel)

            // Canal "Service TaxiPilot" — importance minimale, silencieux, sans badge
            // Requis pour maintenir le service foreground (NotificationListenerService) en vie
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "Service TaxiPilot",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Service d'écoute de nouvelles courses"
                setShowBadge(false)
            }
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_COURSES = "courses_channel" // ID du canal pour les alertes de courses
        const val CHANNEL_SERVICE = "service_channel" // ID du canal pour le service foreground
    }
}
