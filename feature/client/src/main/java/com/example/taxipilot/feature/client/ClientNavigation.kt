package com.example.taxipilot.feature.client

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val CLIENT_ROUTE = "client"

fun NavGraphBuilder.clientGraph(
    navController: NavHostController,
    viewModel: ClientViewModel,
    onSignOut: () -> Unit
) {
    composable(CLIENT_ROUTE) {
        ClientScreen(
            viewModel           = viewModel,
            onSignOut           = onSignOut,
            onNavigateToProfile = { navController.navigate("profile") }
        )
    }
}
