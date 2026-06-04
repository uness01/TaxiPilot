package com.example.taxipilot.feature.driver

// Extension de navigation pour l'espace Chauffeur.
// driverGraph() est une extension sur NavGraphBuilder — elle enregistre la route "driver"
// dans le NavHost principal.
// initialReservationId : ID de réservation reçu via notification tap (peut être null)
//   → passé à DriverScreen pour ouvrir automatiquement l'onglet Réservations sur cette course
// onReservationHandled : callback appelé une fois la réservation traitée → efface pendingReservationId

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val DRIVER_ROUTE = "driver" // route constante pour l'espace chauffeur

// Enregistre la destination "driver" dans le graphe de navigation fourni
fun NavGraphBuilder.driverGraph(
    navController: NavHostController,
    viewModel: DriverViewModel,
    onSignOut: () -> Unit,
    initialReservationId: String? = null, // ID de réservation depuis une notification (nullable)
    onReservationHandled: () -> Unit = {} // appelé quand la réservation a été affichée
) {
    composable(DRIVER_ROUTE) {
        DriverScreen(
            viewModel            = viewModel,
            onSignOut            = onSignOut,
            onNavigateToProfile  = { navController.navigate("profile") },
            initialReservationId = initialReservationId,
            onReservationHandled = onReservationHandled
        )
    }
}
