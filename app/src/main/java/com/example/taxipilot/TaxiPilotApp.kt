package com.example.taxipilot

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class TaxiPilotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Courses channel — HIGH importance so it pops up as a heads-up notification
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

            // Service channel — MIN importance, silent, for the foreground service indicator
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
        const val CHANNEL_COURSES = "courses_channel"
        const val CHANNEL_SERVICE = "service_channel"
    }
}
