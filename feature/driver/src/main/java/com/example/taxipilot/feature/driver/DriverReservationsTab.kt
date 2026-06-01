package com.example.taxipilot.feature.driver

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import com.example.taxipilot.core.data.firestore.FirestoreUser
import com.example.taxipilot.core.data.maps.OsrmRepository
import com.example.taxipilot.core.data.maps.OsrmRoute
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class CoursesTab { DISPONIBLES, MES_COURSES }

@Composable
fun DriverReservationsTab(
    viewModel: DriverViewModel,
    modifier: Modifier = Modifier,
    initialReservationId: String? = null,
    onReservationHandled: () -> Unit = {}
) {
    val available       by viewModel.availableReservations.collectAsState()
    val myCourses       by viewModel.myCourses.collectAsState()
    val hasActive       by viewModel.hasActiveReservation.collectAsState()
    val acceptState     by viewModel.acceptState.collectAsState()
    val activeForGps    by viewModel.activeReservation.collectAsState()
    val statut          by viewModel.statut.collectAsState()

    val terminerState   by viewModel.terminerState.collectAsState()
    var activeTab by remember { mutableStateOf(CoursesTab.DISPONIBLES) }

    // Notification tap: just ensure we're on the DISPONIBLES tab and clear the pending ID
    LaunchedEffect(initialReservationId) {
        if (initialReservationId != null) {
            activeTab = CoursesTab.DISPONIBLES
            onReservationHandled()
        }
    }

    // Course preview dialog state
    var previewReservation by remember { mutableStateOf<FirestoreReservation?>(null) }

    // Show snackbar when someone was faster
    var showTakenSnack by remember { mutableStateOf(false) }
    var terminerErrorMsg by remember { mutableStateOf<String?>(null) }
    // Pending completion data preserved across TooFar dialog
    var pendingTerminer by remember { mutableStateOf<Triple<FirestoreReservation, Double, Double>?>(null) }
    var tooFarMeters by remember { mutableStateOf(0) }

    LaunchedEffect(acceptState) {
        if (acceptState is DriverViewModel.AcceptState.Taken) {
            showTakenSnack = true
            viewModel.clearAcceptState()
        }
        if (acceptState is DriverViewModel.AcceptState.Success) {
            previewReservation = null
            activeTab = CoursesTab.MES_COURSES
            viewModel.clearAcceptState()
        }
    }

    LaunchedEffect(terminerState) {
        when (val s = terminerState) {
            is DriverViewModel.TerminerState.Error -> {
                terminerErrorMsg = s.message
                viewModel.clearTerminerState()
            }
            is DriverViewModel.TerminerState.Success -> {
                pendingTerminer = null
                tooFarMeters = 0
                terminerErrorMsg = null
                viewModel.clearTerminerState()
            }
            is DriverViewModel.TerminerState.TooFar -> {
                tooFarMeters = s.distanceMeters
                viewModel.clearTerminerState()
            }
            else -> Unit
        }
    }

    // Trip completion dialog state
    var completionTarget by remember { mutableStateOf<FirestoreReservation?>(null) }

    // GPS tracking side-effect
    LocationTracker(
        active      = activeForGps != null,
        destination = activeForGps?.arrivee ?: "",
        onLocation  = viewModel::onLocationUpdate
    )

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Sub-tabs ──────────────────────────────────────────────────────
            TabRow(selectedTabIndex = activeTab.ordinal) {
                Tab(
                    selected = activeTab == CoursesTab.DISPONIBLES,
                    onClick  = { activeTab = CoursesTab.DISPONIBLES },
                    text = {
                        BadgedBox(badge = {
                            if (available.isNotEmpty()) Badge { Text(available.size.toString()) }
                        }) { Text("Disponibles") }
                    }
                )
                Tab(
                    selected = activeTab == CoursesTab.MES_COURSES,
                    onClick  = { activeTab = CoursesTab.MES_COURSES },
                    text = {
                        val active = myCourses.count { it.isActive }
                        BadgedBox(badge = {
                            if (active > 0) Badge { Text(active.toString()) }
                        }) { Text("Mes courses") }
                    }
                )
            }

            // ── GPS banner ────────────────────────────────────────────────────
            if (activeForGps != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📡", modifier = Modifier.padding(end = 8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("GPS actif — Course en cours",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White, fontWeight = FontWeight.Bold)
                            Text("→ ${activeForGps?.arrivee}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // ── Course preview dialog ─────────────────────────────────────────
            previewReservation?.let { res ->
                CoursePreviewDialog(
                    reservation = res,
                    isAccepting = acceptState is DriverViewModel.AcceptState.Loading,
                    onAccept = { viewModel.accepterReservation(res.id) },
                    onDismiss = { previewReservation = null }
                )
            }

            when (activeTab) {
                CoursesTab.DISPONIBLES -> AvailableList(
                    reservations = available,
                    hasActive    = hasActive,
                    isAccepting  = acceptState is DriverViewModel.AcceptState.Loading,
                    onPreview    = { previewReservation = it },
                    statut       = statut
                )
                CoursesTab.MES_COURSES -> MyCoursesList(
                    reservations   = myCourses,
                    onDemarrer     = viewModel::demarrerCourse,
                    onTerminerClick = { completionTarget = it }
                )
            }
        }

        // ── Snackbars ─────────────────────────────────────────────────────────
        if (showTakenSnack) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { showTakenSnack = false }) { Text("OK") } }
            ) { Text("Course déjà prise par un autre chauffeur.") }
        }
        if (terminerErrorMsg != null) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor   = MaterialTheme.colorScheme.onErrorContainer,
                action = { TextButton(onClick = { terminerErrorMsg = null }) { Text("OK") } }
            ) { Text(terminerErrorMsg!!) }
        }
    }

    // ── Trip completion dialog ────────────────────────────────────────────────
    completionTarget?.let { res ->
        CompletionDialog(
            reservation = res,
            onConfirm = { prix, distance ->
                pendingTerminer = Triple(res, prix, distance)
                viewModel.terminerCourse(res, prix, distance)
                completionTarget = null
            },
            onDismiss = { completionTarget = null }
        )
    }

    // ── TooFar confirmation dialog ────────────────────────────────────────────
    if (tooFarMeters > 0 && pendingTerminer != null) {
        val (pendingRes, pendingPrix, pendingDist) = pendingTerminer!!
        AlertDialog(
            onDismissRequest = { tooFarMeters = 0 },
            title = { Text("Trop loin de la destination") },
            text = {
                Text(
                    "Vous êtes à ${tooFarMeters} m de la destination (seuil : 500 m).\n\n" +
                    "Êtes-vous sûr de vouloir terminer la course ?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tooFarMeters = 0
                        viewModel.terminerCourse(pendingRes, pendingPrix, pendingDist, forceTerminate = true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Forcer la terminaison") }
            },
            dismissButton = {
                TextButton(onClick = { tooFarMeters = 0; pendingTerminer = null }) {
                    Text("Annuler")
                }
            }
        )
    }

}

// ── Available courses list ────────────────────────────────────────────────────

@Composable
private fun AvailableList(
    reservations: List<FirestoreReservation>,
    hasActive: Boolean,
    isAccepting: Boolean,
    onPreview: (FirestoreReservation) -> Unit,
    statut: String = FirestoreUser.STATUT_EN_SERVICE
) {
    // Hors service: block all course display
    if (statut == FirestoreUser.STATUT_HORS_SERVICE) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text("🔴", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Vous êtes hors service.\nActivez votre service pour voir les courses.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }
    if (reservations.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🚖", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(8.dp))
                Text("Aucune course disponible",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (hasActive) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        "⚠️ Terminez votre course active avant d'en accepter une nouvelle.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        items(reservations, key = { it.id }) { res ->
            AvailableCard(
                reservation = res,
                canPreview  = !isAccepting,
                onPreview   = { onPreview(res) }
            )
        }
    }
}

@Composable
private fun AvailableCard(
    reservation: FirestoreReservation,
    canPreview: Boolean,
    onPreview: () -> Unit
) {
    val dtFmt = remember { SimpleDateFormat("dd/MM 'à' HH:mm", Locale.FRENCH) }
    val isImmediate = reservation.isImmediate

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isImmediate)
                Color(0xFFE65100).copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isImmediate) "🚨 URGENT — Immédiate" else "🗓 Planifiée",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isImmediate) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                )
                Text(
                    dtFmt.format(
                        Date(reservation.scheduledTime ?: reservation.createdAt)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🟢 ", style = MaterialTheme.typography.bodySmall)
                Text(reservation.depart, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔴 ", style = MaterialTheme.typography.bodySmall)
                Text(reservation.arrivee, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Client : ${reservation.clientName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.2f MAD".format(reservation.prixEstime),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32))
            }

            OutlinedButton(
                onClick = onPreview,
                enabled = canPreview,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isImmediate) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Voir la course →", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Course preview dialog ─────────────────────────────────────────────────────

@Composable
private fun CoursePreviewDialog(
    reservation: FirestoreReservation,
    isAccepting: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val isImmediate = reservation.isImmediate
    val hasCoords = reservation.departLat != null && reservation.arriveeLat != null

    AlertDialog(
        onDismissRequest = { if (!isAccepting) onDismiss() },
        title = {
            Text(
                if (isImmediate) "🚨 Course immédiate" else "🗓 Course planifiée",
                color = if (isImmediate) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟢 ", style = MaterialTheme.typography.bodySmall)
                    Text(reservation.depart, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔴 ", style = MaterialTheme.typography.bodySmall)
                    Text(reservation.arrivee, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Client : ${reservation.clientName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.2f MAD".format(reservation.prixEstime),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32))
                }
                if (reservation.distanceKm > 0.0) {
                    Text("Distance estimée : %.1f km".format(reservation.distanceKm),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (hasCoords) {
                    Spacer(Modifier.height(4.dp))
                    RouteMapView(
                        reservation = reservation,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                enabled = !isAccepting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isImmediate) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                )
            ) {
                if (isAccepting) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Accepter", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isAccepting) { Text("Ignorer") }
        }
    )
}

// ── My courses list ───────────────────────────────────────────────────────────

@Composable
private fun MyCoursesList(
    reservations: List<FirestoreReservation>,
    onDemarrer: (String) -> Unit,
    onTerminerClick: (FirestoreReservation) -> Unit
) {
    if (reservations.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucune course acceptée", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reservations, key = { it.id }) { res ->
            MyCourseCard(res, onDemarrer, onTerminerClick)
        }
    }
}

@Composable
private fun MyCourseCard(
    res: FirestoreReservation,
    onDemarrer: (String) -> Unit,
    onTerminerClick: (FirestoreReservation) -> Unit
) {
    val dtFmt = remember { SimpleDateFormat("dd/MM 'à' HH:mm", Locale.FRENCH) }
    val hasCoords = res.departLat != null && res.arriveeLat != null
    var showMap by remember(res.id) { mutableStateOf(res.status == FirestoreReservation.STATUS_EN_COURS && hasCoords) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dtFmt.format(Date(res.scheduledTime ?: res.createdAt)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(res.status)
            }

            HorizontalDivider()

            Text("Client : ${res.clientName}  ·  ${res.clientPhone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("De : ${res.depart}", style = MaterialTheme.typography.bodySmall)
            Text("À : ${res.arrivee}", style = MaterialTheme.typography.bodySmall)

            val priceLabel = res.prixFinal?.let { "%.2f MAD (final)".format(it) }
                ?: "Estimé : %.2f MAD".format(res.prixEstime)
            Text(priceLabel, style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))

            // Route map (visible for EN_COURS when coords available, toggleable)
            if (hasCoords && res.status != FirestoreReservation.STATUS_ANNULEE) {
                TextButton(
                    onClick = { showMap = !showMap },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showMap) "Masquer la carte" else "Afficher l'itinéraire")
                }
                if (showMap) {
                    RouteMapView(reservation = res, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            }

            when (res.status) {
                FirestoreReservation.STATUS_ACCEPTEE -> {
                    Button(
                        onClick = { onDemarrer(res.id) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) { Text("Démarrer la course", fontWeight = FontWeight.Bold) }
                }
                FirestoreReservation.STATUS_EN_COURS -> {
                    Button(
                        onClick = { onTerminerClick(res) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("Terminer la course", fontWeight = FontWeight.Bold) }
                }
                FirestoreReservation.STATUS_TERMINEE -> {
                    res.distanceReelle?.let {
                        Text("Distance réelle : %.1f km".format(it),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> Unit
            }
        }
    }
}

// ── Route map (OSMDroid + OSRM) ───────────────────────────────────────────────

@Composable
private fun RouteMapView(
    reservation: FirestoreReservation,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val osrm    = remember { OsrmRepository() }
    var route   by remember(reservation.id) { mutableStateOf<OsrmRoute?>(null) }

    val dLat = reservation.departLat  ?: return
    val dLng = reservation.departLng  ?: return
    val aLat = reservation.arriveeLat ?: return
    val aLng = reservation.arriveeLng ?: return

    LaunchedEffect(reservation.id) {
        route = osrm.getRoute(dLat, dLng, aLat, aLng)
    }

    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().userAgentValue = ctx.packageName
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(13.0)
                controller.setCenter(GeoPoint((dLat + aLat) / 2, (dLng + aLng) / 2))
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            val pts = route?.polylinePoints
            if (!pts.isNullOrEmpty()) {
                val geoPoints = pts.map { (lat, lon) -> GeoPoint(lat, lon) }
                val polyline = Polyline().apply {
                    setPoints(geoPoints)
                    outlinePaint.color = android.graphics.Color.parseColor("#1565C0")
                    outlinePaint.strokeWidth = 8f
                }
                mapView.overlays.add(polyline)

                val startMarker = Marker(mapView).apply {
                    position = GeoPoint(dLat, dLng)
                    title = "Départ"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                val endMarker = Marker(mapView).apply {
                    position = GeoPoint(aLat, aLng)
                    title = "Arrivée"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.addAll(listOf(startMarker, endMarker))

                val bounds = BoundingBox.fromGeoPoints(geoPoints)
                mapView.post { mapView.zoomToBoundingBox(bounds, true, 80) }
            }
            mapView.invalidate()
        },
        modifier = modifier.clip(RoundedCornerShape(8.dp))
    )
}

// ── Trip completion dialog ────────────────────────────────────────────────────

@Composable
private fun CompletionDialog(
    reservation: FirestoreReservation,
    onConfirm: (prixFinal: Double, distanceReelle: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var prixStr     by remember { mutableStateOf(reservation.prixEstime.toLong().toString()) }
    var distanceStr by remember { mutableStateOf(reservation.distanceReelle?.toString() ?: "") }

    val prixValid      = prixStr.toDoubleOrNull() != null
    // Distance is optional — blank is accepted, only validate if user typed something
    val distanceValid  = distanceStr.isBlank() || distanceStr.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terminer la course") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${reservation.depart} → ${reservation.arrivee}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = prixStr,
                    onValueChange = { prixStr = it },
                    label = { Text("Prix final (MAD) *") },
                    placeholder = { Text("ex: 350") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("MAD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = prixStr.isNotBlank() && !prixValid
                )
                OutlinedTextField(
                    value = distanceStr,
                    onValueChange = { distanceStr = it },
                    label = { Text("Distance réelle (km) — optionnel") },
                    placeholder = { Text("ex: 35.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("km") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !distanceValid
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prix = prixStr.toDoubleOrNull() ?: return@Button
                    val dist = distanceStr.toDoubleOrNull() ?: 0.0
                    onConfirm(prix, dist)
                },
                enabled = prixValid && distanceValid
            ) { Text("Confirmer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

// ── Status badge ──────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(status: String) {
    val (text, color) = when (status) {
        FirestoreReservation.STATUS_EN_ATTENTE -> "En attente" to Color(0xFFFF9800)
        FirestoreReservation.STATUS_ACCEPTEE   -> "Acceptée"   to Color(0xFF4CAF50)
        FirestoreReservation.STATUS_EN_COURS   -> "En cours"   to Color(0xFF2196F3)
        FirestoreReservation.STATUS_TERMINEE   -> "Terminée"   to Color(0xFF9E9E9E)
        FirestoreReservation.STATUS_ANNULEE    -> "Annulée"    to Color(0xFFF44336)
        else                                   -> status       to Color(0xFF9E9E9E)
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall)
    }
}

// ── GPS location tracker (side-effect composable) ─────────────────────────────

@SuppressLint("MissingPermission")
@Composable
private fun LocationTracker(
    active: Boolean,
    destination: String,
    onLocation: (lat: Double, lng: Double, destination: String) -> Unit
) {
    val context = LocalContext.current
    val permissionGranted = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* next recompose will re-evaluate */ }

    LaunchedEffect(active) {
        if (active && !permissionGranted) {
            permLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    DisposableEffect(active, permissionGranted) {
        if (!active || !permissionGranted) return@DisposableEffect onDispose {}

        val client  = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15_000L)
            .setMinUpdateIntervalMillis(10_000L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onLocation(it.latitude, it.longitude, destination) }
            }
        }
        client.requestLocationUpdates(request, callback, context.mainLooper)
        onDispose { client.removeLocationUpdates(callback) }
    }
}
