package com.example.taxipilot.feature.driver

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val DRIVER_ROUTE = "driver"

fun NavGraphBuilder.driverGraph(
    navController: NavHostController,
    viewModel: DriverViewModel,
    onSignOut: () -> Unit,
    initialReservationId: String? = null,
    onReservationHandled: () -> Unit = {}
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
