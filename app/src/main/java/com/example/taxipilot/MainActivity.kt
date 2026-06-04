package com.example.taxipilot

// Point d'entrée principal de l'application.
// Responsabilités :
//   1. Instancier la base de données Room et tous les repositories (lazy → créés seulement quand utilisés)
//   2. Récupérer l'ID de réservation éventuellement passé par une notification push (EXTRA_RESERVATION_ID)
//   3. Lancer le NavHost Compose avec le thème de l'app
//
// Pattern "manual DI" (Dependency Injection sans Hilt) :
//   Chaque repository est créé ici et passé au TaxiPilotNavHost,
//   qui les transmet aux ViewModels via leurs factories.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.taxipilot.auth.AuthRepository
import com.example.taxipilot.core.data.database.TaxiPilotDatabase
import com.example.taxipilot.core.data.repository.*
import com.example.taxipilot.navigation.TaxiPilotNavHost
import com.example.taxipilot.ui.theme.TaxiPilotTheme

class MainActivity : ComponentActivity() {

    companion object {
        // Clé de l'extra Intent — utilisée quand l'utilisateur tape sur une notification de course
        const val EXTRA_RESERVATION_ID = "reservation_id"
    }

    // Base de données Room (SQLite local) — instanciée paresseusement
    private val database by lazy { TaxiPilotDatabase.getInstance(this) }

    // ── Repositories locaux (Room) ────────────────────────────────────────────
    private val authRepository              by lazy { AuthRepository() }
    private val taxiRepository              by lazy { TaxiRepository(database.taxiDao()) }
    private val chauffeurRepository         by lazy { ChauffeurRepository(database.chauffeurDao()) }
    private val trajetRepository            by lazy { TrajetRepository(database.trajetDao()) }
    private val chargeRepository            by lazy { ChargeRepository(database.chargeDao()) }

    // ── Repositories cloud (Firestore) ────────────────────────────────────────
    private val locationRepository          by lazy { LocationRepository() }
    private val firestoreReservationRepository by lazy { FirestoreReservationRepository() }
    private val firestoreUserRepository        by lazy { FirestoreUserRepository() }
    private val firestoreNotificationRepository by lazy { FirestoreNotificationRepository() }

    // ── Service de notifications push (FCM) ──────────────────────────────────
    private val fcmRepository                  by lazy { FcmRepository(firestoreNotificationRepository, firestoreUserRepository) }

    // Conservé pour la compilation (ancienne architecture locale)
    private val tripRepository by lazy { TripRepository(database.tripDao()) }

    // ID de réservation reçu via l'Intent d'une notification — survit aux recompositions Compose
    private var pendingReservationId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Récupère l'ID de réservation si l'activité a été lancée par un tap sur notification
        pendingReservationId = intent.getStringExtra(EXTRA_RESERVATION_ID)
        enableEdgeToEdge() // affichage plein écran (contenu sous la barre de statut)
        setContent {
            TaxiPilotTheme {
                // NavHost principal : gère l'auth, la navigation et les 3 espaces (Owner/Driver/Client)
                TaxiPilotNavHost(
                    authRepository                 = authRepository,
                    taxiRepository                 = taxiRepository,
                    chauffeurRepository            = chauffeurRepository,
                    trajetRepository               = trajetRepository,
                    chargeRepository               = chargeRepository,
                    firestoreReservationRepository = firestoreReservationRepository,
                    firestoreUserRepository        = firestoreUserRepository,
                    fcmRepository                  = fcmRepository,
                    locationRepository             = locationRepository,
                    tripRepository                 = tripRepository,
                    initialReservationId           = pendingReservationId,
                    onReservationHandled           = { pendingReservationId = null }
                )
            }
        }
    }
}
