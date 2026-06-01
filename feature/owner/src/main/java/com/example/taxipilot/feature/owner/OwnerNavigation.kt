package com.example.taxipilot.feature.owner

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val OWNER_ROUTE = "owner"

fun NavGraphBuilder.ownerGraph(
    navController: NavHostController,
    viewModel: OwnerViewModel,
    onSignOut: () -> Unit
) {
    composable(OWNER_ROUTE) {
        OwnerScreen(
            viewModel          = viewModel,
            onSignOut          = onSignOut,
            onNavigateToProfile = { navController.navigate("profile") }
        )
    }
}
