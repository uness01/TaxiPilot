package com.example.taxipilot.feature.owner

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.taxipilot.core.data.firestore.FirestoreReservation

private enum class OwnerTab(
    val shortLabel: String,
    val fullLabel: String,
    val icon: ImageVector
) {
    DASHBOARD(    "Accueil",       "Tableau de bord",  Icons.Filled.Home),
    RESERVATIONS( "Réservations",  "Réservations",     Icons.AutoMirrored.Filled.List),
    TAXIS(        "Taxis",         "Mes Taxis",        Icons.Filled.Build),
    CHAUFFEURS(   "Chauffeurs",    "Chauffeurs",       Icons.Filled.Person),
    CHARGES(      "Charges",       "Charges",          Icons.Filled.ShoppingCart),
    FLOTTE(       "Flotte",        "Suivi Flotte",     Icons.Filled.LocationOn)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerScreen(
    viewModel: OwnerViewModel,
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = OwnerTab.entries

    // Live count for the Réservations badge
    val allReservations by viewModel.allReservations.collectAsState()
    val activeCount = allReservations.count {
        it.status == FirestoreReservation.STATUS_EN_ATTENTE ||
        it.status == FirestoreReservation.STATUS_EN_COURS
    }

    // Driver status change notifications → Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.driverStatusMessages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(tabs[selectedTab].fullLabel) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Mon profil")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Se déconnecter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        icon = {
                            if (tab == OwnerTab.RESERVATIONS && activeCount > 0) {
                                BadgedBox(badge = { Badge { Text(activeCount.toString()) } }) {
                                    Icon(tab.icon, contentDescription = tab.fullLabel)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.fullLabel)
                            }
                        },
                        label = { Text(tab.shortLabel) }
                    )
                }
            }
        }
    ) { padding ->
        val mod = Modifier.fillMaxSize().padding(padding)
        when (tabs[selectedTab]) {
            OwnerTab.DASHBOARD    -> OwnerDashboardTab(viewModel, mod)
            OwnerTab.RESERVATIONS -> OwnerReservationsTab(viewModel, mod)
            OwnerTab.TAXIS        -> OwnerTaxisTab(viewModel, mod)
            OwnerTab.CHAUFFEURS   -> OwnerChauffeursTab(viewModel, mod)
            OwnerTab.CHARGES      -> OwnerChargesTab(viewModel, mod)
            OwnerTab.FLOTTE       -> OwnerFleetMapTab(viewModel, mod)
        }
    }
}
