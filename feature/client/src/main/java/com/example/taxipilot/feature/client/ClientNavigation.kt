package com.example.taxipilot.feature.client

// Extension de navigation pour l'espace Client (passager).
// clientGraph() est une extension sur NavGraphBuilder — elle enregistre la route "client"
// dans le NavHost principal.
// Appelée depuis TaxiPilotNavHost lors de la construction du graphe de navigation principal.

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val CLIENT_ROUTE = "client" // route constante pour l'espace client

// Enregistre la destination "client" dans le graphe de navigation fourni
fun NavGraphBuilder.clientGraph(
    navController: NavHostController,
    viewModel: ClientViewModel,
    onSignOut: () -> Unit // callback de déconnexion
) {
    composable(CLIENT_ROUTE) {
        ClientScreen(
            viewModel           = viewModel,
            onSignOut           = onSignOut,
            onNavigateToProfile = { navController.navigate("profile") }
        )
    }
}
