package com.example.taxipilot.navigation

// Hôte de navigation principal — orchestre toute la navigation de l'app selon l'état d'authentification.
// Logique de routage :
//   AuthState.Loading             → spinner (vérification session Firebase en cours)
//   AuthState.Unauthenticated     → NavHost auth (Login ↔ Register)
//   AuthState.NeedsProprietaireLink → LinkCodeScreen (chauffeur non lié)
//   AuthState.Authenticated       → NavHost principal (Owner | Driver | Client + Profile)
//
// La destination de démarrage du NavHost principal dépend du rôle :
//   PROPRIETAIRE → OWNER_ROUTE | CHAUFFEUR → DRIVER_ROUTE | CLIENT → CLIENT_ROUTE
//
// toRoomId() : convertit un UID Firebase (String) en Long déterministe pour les clés Room.
// Le service NotificationListenerService est démarré/arrêté selon le statut du chauffeur.

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.taxipilot.notification.NotificationListenerService
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taxipilot.FcmRepository
import com.example.taxipilot.auth.AuthRepository
import com.example.taxipilot.core.data.firestore.FirestoreUser
import com.example.taxipilot.auth.AuthViewModel
import com.example.taxipilot.auth.LinkCodeScreen
import com.example.taxipilot.auth.LoginScreen
import com.example.taxipilot.auth.ProfileScreen
import com.example.taxipilot.auth.RegisterScreen
import com.example.taxipilot.auth.UserRole
import com.example.taxipilot.core.data.repository.ChargeRepository
import com.example.taxipilot.core.data.repository.ChauffeurRepository
import com.example.taxipilot.core.data.repository.FirestoreReservationRepository
import com.example.taxipilot.core.data.repository.FirestoreUserRepository
import com.example.taxipilot.core.data.repository.LocationRepository
import com.example.taxipilot.core.data.repository.TaxiRepository
import com.example.taxipilot.core.data.repository.TrajetRepository
import com.example.taxipilot.core.data.repository.TripRepository
import com.example.taxipilot.feature.client.CLIENT_ROUTE
import com.example.taxipilot.feature.client.ClientViewModel
import com.example.taxipilot.feature.client.clientGraph
import com.example.taxipilot.feature.driver.DRIVER_ROUTE
import com.example.taxipilot.feature.driver.DriverViewModel
import com.example.taxipilot.feature.driver.driverGraph
import com.example.taxipilot.feature.owner.OWNER_ROUTE
import com.example.taxipilot.feature.owner.OwnerViewModel
import com.example.taxipilot.feature.owner.ownerGraph
import kotlin.math.abs

private const val LOGIN_ROUTE    = "login"
private const val REGISTER_ROUTE = "register"
private const val PROFILE_ROUTE  = "profile"

// Convertit un UID Firebase (String) en Long déterministe pour être utilisé comme clé Room.
// Utilise abs(hashCode()) — même UID donnera toujours le même Long.
private fun String.toRoomId(): Long = abs(hashCode().toLong())

@Composable
fun TaxiPilotNavHost(
    authRepository: AuthRepository,
    taxiRepository: TaxiRepository,
    chauffeurRepository: ChauffeurRepository,
    trajetRepository: TrajetRepository,
    chargeRepository: ChargeRepository,
    firestoreReservationRepository: FirestoreReservationRepository,
    firestoreUserRepository: FirestoreUserRepository,
    fcmRepository: FcmRepository,
    locationRepository: LocationRepository,
    @Suppress("UNUSED_PARAMETER") tripRepository: TripRepository, // conservé pour compatibilité
    initialReservationId: String? = null, // ID reçu depuis une notification tap
    onReservationHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(authRepository)
    )
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {

        // Attente de la vérification Firebase → spinner centré
        is AuthViewModel.AuthState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Non connecté → NavHost auth (Login ↔ Register)
        is AuthViewModel.AuthState.Unauthenticated -> {
            val authNavController = rememberNavController()
            NavHost(
                navController    = authNavController,
                startDestination = LOGIN_ROUTE,
                modifier         = modifier
            ) {
                composable(LOGIN_ROUTE) {
                    LoginScreen(
                        viewModel            = authViewModel,
                        onNavigateToRegister = { authNavController.navigate(REGISTER_ROUTE) }
                    )
                }
                composable(REGISTER_ROUTE) {
                    RegisterScreen(
                        viewModel          = authViewModel,
                        onNavigateToLogin  = { authNavController.popBackStack() }
                    )
                }
            }
        }

        // Chauffeur sans propriétaire → écran de saisie du code
        is AuthViewModel.AuthState.NeedsProprietaireLink -> {
            LinkCodeScreen(viewModel = authViewModel, modifier = modifier)
        }

        // Connecté → NavHost principal avec l'espace correspondant au rôle
        is AuthViewModel.AuthState.Authenticated -> {
            val mainNavController = rememberNavController()
            val profile   = state.profile
            val uid       = profile.uid
            val context   = LocalContext.current

            // Destination de départ selon le rôle de l'utilisateur
            val startDest = when (profile.role) {
                UserRole.PROPRIETAIRE -> OWNER_ROUTE
                UserRole.CHAUFFEUR    -> DRIVER_ROUTE
                UserRole.CLIENT       -> CLIENT_ROUTE
            }

            // ViewModels créés avec des clés uniques (uid) pour éviter les conflits multi-comptes
            val ownerViewModel: OwnerViewModel = viewModel(
                key = "owner_$uid",
                factory = OwnerViewModel.Factory(
                    taxiRepository                = taxiRepository,
                    chauffeurRepository           = chauffeurRepository,
                    trajetRepository              = trajetRepository,
                    chargeRepository              = chargeRepository,
                    firestoreReservationRepository = firestoreReservationRepository,
                    firestoreUserRepository       = firestoreUserRepository,
                    proprietaireId                = uid,
                    codeProprietaire              = profile.codeProprietaire
                )
            )
            val driverViewModel: DriverViewModel = viewModel(
                key = "driver_$uid",
                factory = DriverViewModel.Factory(
                    trajetRepository              = trajetRepository,
                    chargeRepository              = chargeRepository,
                    taxiRepository                = taxiRepository,
                    firestoreReservationRepository = firestoreReservationRepository,
                    locationRepository            = locationRepository,
                    firestoreUserRepository       = firestoreUserRepository,
                    chauffeurId                   = uid.toRoomId(), // UID Firebase → Long Room
                    chauffeurUid                  = uid,
                    chauffeurNom                  = profile.nom,
                    proprietaireId                = profile.proprietaireId,
                    assignedTaxi                  = profile.assignedTaxi,
                    initialStatut                 = profile.statut
                )
            )
            val clientViewModel: ClientViewModel = viewModel(
                key = "client_$uid",
                factory = ClientViewModel.Factory(
                    reservationRepo          = firestoreReservationRepository,
                    clientUid                = uid,
                    initialNom               = profile.nom,
                    initialTelephone         = profile.telephone,
                    onNotifyNewReservation   = { depart, arrivee ->
                        // Notifie les chauffeurs via FCM + Firestore quand un client réserve
                        fcmRepository.sendNewReservationNotification(depart, arrivee)
                    }
                )
            )

            // Pour les chauffeurs : démarrer/arrêter le service de notifications selon le statut
            // hors_service → service arrêté (plus de notifications reçues)
            // en_service / en_course → service démarré
            if (profile.role == UserRole.CHAUFFEUR) {
                val driverStatut by driverViewModel.statut.collectAsState()
                LaunchedEffect(driverStatut) {
                    if (driverStatut == FirestoreUser.STATUT_HORS_SERVICE) {
                        NotificationListenerService.stop(context)
                    } else {
                        NotificationListenerService.start(context)
                    }
                }
                // Arrête le service quand le chauffeur se déconnecte
                DisposableEffect(uid) {
                    onDispose { NotificationListenerService.stop(context) }
                }
            }

            NavHost(
                navController    = mainNavController,
                startDestination = startDest,
                modifier         = modifier
            ) {
                ownerGraph(mainNavController,  ownerViewModel)  { authViewModel.signOut() }
                driverGraph(
                    navController        = mainNavController,
                    viewModel            = driverViewModel,
                    onSignOut            = { authViewModel.signOut() },
                    initialReservationId = initialReservationId,
                    onReservationHandled = onReservationHandled
                )
                clientGraph(mainNavController, clientViewModel) { authViewModel.signOut() }
                // Écran de profil — partagé entre les 3 rôles
                composable(PROFILE_ROUTE) {
                    val proprietaireInfo by driverViewModel.proprietaireInfo.collectAsState()
                    ProfileScreen(
                        viewModel        = authViewModel,
                        profile          = profile,
                        onBack           = { mainNavController.popBackStack() },
                        // Infos de l'employeur uniquement pour les chauffeurs
                        proprietaireInfo = if (profile.role == UserRole.CHAUFFEUR) proprietaireInfo else null
                    )
                }
            }
        }
    }
}
