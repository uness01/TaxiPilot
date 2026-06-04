package com.example.taxipilot.feature.owner

// Extension de navigation pour l'espace Propriétaire.
// ownerGraph() est une extension sur NavGraphBuilder — elle enregistre la route "owner"
// dans le NavHost principal sans avoir besoin d'un NavController imbriqué.
// Appelée depuis TaxiPilotNavHost lors de la construction du graphe de navigation principal.

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val OWNER_ROUTE = "owner" // route constante pour éviter les fautes de frappe

// Enregistre la destination "owner" dans le graphe de navigation fourni
fun NavGraphBuilder.ownerGraph(
    navController: NavHostController, // contrôleur principal (pour naviguer vers "profile")
    viewModel: OwnerViewModel,
    onSignOut: () -> Unit             // callback de déconnexion (fourni par TaxiPilotNavHost)
) {
    composable(OWNER_ROUTE) {
        OwnerScreen(
            viewModel          = viewModel,
            onSignOut          = onSignOut,
            onNavigateToProfile = { navController.navigate("profile") }
        )
    }
}
