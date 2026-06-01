package com.example.taxipilot

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
        const val EXTRA_RESERVATION_ID = "reservation_id"
    }

    private val database by lazy { TaxiPilotDatabase.getInstance(this) }

    private val authRepository              by lazy { AuthRepository() }
    private val taxiRepository              by lazy { TaxiRepository(database.taxiDao()) }
    private val chauffeurRepository         by lazy { ChauffeurRepository(database.chauffeurDao()) }
    private val trajetRepository            by lazy { TrajetRepository(database.trajetDao()) }
    private val chargeRepository            by lazy { ChargeRepository(database.chargeDao()) }
    private val locationRepository          by lazy { LocationRepository() }
    private val firestoreReservationRepository by lazy { FirestoreReservationRepository() }
    private val firestoreUserRepository        by lazy { FirestoreUserRepository() }
    private val firestoreNotificationRepository by lazy { FirestoreNotificationRepository() }
    private val fcmRepository                  by lazy { FcmRepository(firestoreNotificationRepository, firestoreUserRepository) }

    // Legacy — kept for compilation
    private val tripRepository by lazy { TripRepository(database.tripDao()) }

    // Reservation ID from notification tap — survives recomposition
    private var pendingReservationId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingReservationId = intent.getStringExtra(EXTRA_RESERVATION_ID)
        enableEdgeToEdge()
        setContent {
            TaxiPilotTheme {
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
