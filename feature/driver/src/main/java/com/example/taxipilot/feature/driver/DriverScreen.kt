package com.example.taxipilot.feature.driver

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.firestore.FirestoreUser

private enum class DriverTab { VOICE, COURSES, SUMMARY, HISTORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    viewModel: DriverViewModel,
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    initialReservationId: String? = null,
    onReservationHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val voiceEngine = remember { VoiceEngine(context) }
    DisposableEffect(Unit) { onDispose { voiceEngine.destroy() } }

    var selectedTab by remember { mutableStateOf(DriverTab.VOICE) }
    val myCourses by viewModel.myCourses.collectAsState()
    val pendingCount = myCourses.count { it.isActive }
    val statut by viewModel.statut.collectAsState()
    var showStatutConfirm by remember { mutableStateOf(false) }

    // Jump straight to Courses tab when opened from a notification
    LaunchedEffect(initialReservationId) {
        if (initialReservationId != null) selectedTab = DriverTab.COURSES
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (selectedTab) {
                        DriverTab.VOICE   -> "Commandes vocales"
                        DriverTab.COURSES -> "Mes courses"
                        DriverTab.SUMMARY -> "Résumé du jour"
                        DriverTab.HISTORY -> "Historique"
                    })
                },
                actions = {
                    // Status toggle — hidden while en_course (auto-managed)
                    if (statut != FirestoreUser.STATUT_EN_COURSE) {
                        val isEnService = statut != FirestoreUser.STATUT_HORS_SERVICE
                        Surface(
                            onClick = { showStatutConfirm = true },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isEnService) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                    else Color(0xFF9E9E9E).copy(alpha = 0.15f),
                            contentColor = if (isEnService) Color(0xFF2E7D32) else Color(0xFF757575)
                        ) {
                            Text(
                                text = if (isEnService) "🟢 En service" else "🔴 Hors service",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
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
                NavigationBarItem(
                    selected = selectedTab == DriverTab.VOICE,
                    onClick = { selectedTab = DriverTab.VOICE },
                    icon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                    label = { Text("Vocal") }
                )
                NavigationBarItem(
                    selected = selectedTab == DriverTab.COURSES,
                    onClick = { selectedTab = DriverTab.COURSES },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) Badge { Text(pendingCount.toString()) }
                            }
                        ) {
                            Icon(Icons.Filled.DateRange, contentDescription = null)
                        }
                    },
                    label = { Text("Courses") }
                )
                NavigationBarItem(
                    selected = selectedTab == DriverTab.SUMMARY,
                    onClick = { selectedTab = DriverTab.SUMMARY },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Résumé") }
                )
                NavigationBarItem(
                    selected = selectedTab == DriverTab.HISTORY,
                    onClick = { selectedTab = DriverTab.HISTORY },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Historique") }
                )
            }
        }
    ) { padding ->
        val mod = Modifier.fillMaxSize().padding(padding)
        when (selectedTab) {
            DriverTab.VOICE   -> DriverVoiceTab(viewModel, voiceEngine, mod)
            DriverTab.COURSES -> DriverReservationsTab(
                viewModel            = viewModel,
                modifier             = mod,
                initialReservationId = initialReservationId,
                onReservationHandled = onReservationHandled
            )
            DriverTab.SUMMARY -> DriverSummaryTab(viewModel, mod)
            DriverTab.HISTORY -> DriverHistoryTab(viewModel, mod)
        }
    }

    // ── Status change confirmation dialog ─────────────────────────────────────
    if (showStatutConfirm) {
        val isEnService = statut != FirestoreUser.STATUT_HORS_SERVICE
        val newStatutLabel = if (isEnService) "Hors service" else "En service"
        AlertDialog(
            onDismissRequest = { showStatutConfirm = false },
            title = { Text("Changer de statut") },
            text = {
                val taxiInfo = viewModel.assignedTaxi?.let { " (véhicule : $it)" } ?: ""
                Text(
                    "Passer en « $newStatutLabel »$taxiInfo ?\n\n" +
                    if (isEnService)
                        "Vous ne recevrez plus de nouvelles courses."
                    else
                        "Vous serez à nouveau disponible pour de nouvelles courses.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    showStatutConfirm = false
                    viewModel.toggleStatut()
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showStatutConfirm = false }) { Text("Annuler") }
            }
        )
    }
}
