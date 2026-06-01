package com.example.taxipilot.feature.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import com.example.taxipilot.core.data.maps.NominatimRepository
import com.example.taxipilot.core.data.maps.NominatimResult
import com.example.taxipilot.core.data.maps.OsrmRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import android.view.MotionEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GREEN  = Color(0xFF2E7D32)
private val ORANGE = Color(0xFFE65100)

// ── Main screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    viewModel: ClientViewModel,
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val myReservations  by viewModel.myReservations.collectAsState()
    val activeRes       by viewModel.activeReservation.collectAsState()
    val bookingState    by viewModel.bookingState.collectAsState()

    var selectedTab by remember(activeRes) {
        mutableIntStateOf(if (activeRes != null) 1 else 0)
    }

    // Form state
    var clientNom       by remember { mutableStateOf(viewModel.initialNom) }
    var clientTelephone by remember { mutableStateOf(viewModel.initialTelephone) }

    // Map-picked locations
    var departResult    by remember { mutableStateOf<NominatimResult?>(null) }
    var arriveeResult   by remember { mutableStateOf<NominatimResult?>(null) }
    var osrmDistanceKm  by remember { mutableStateOf(0.0) }
    var isCalcRoute     by remember { mutableStateOf(false) }

    // Map picker overlay state
    var mapPickerTarget by remember { mutableStateOf<String?>(null) } // "depart" | "arrivee" | null

    // Date/time for planified
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour   by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var planifierPending by remember { mutableStateOf(false) }

    val prixNormal    = viewModel.estimerPrix(osrmDistanceKm.coerceAtLeast(1.0), FirestoreReservation.TYPE_PLANIFIEE)
    val prixImmediate = viewModel.estimerPrix(osrmDistanceKm.coerceAtLeast(1.0), FirestoreReservation.TYPE_IMMEDIATE)
    val formValid     = clientNom.isNotBlank() && clientTelephone.isNotBlank() &&
                        departResult != null && arriveeResult != null

    // Auto-calculate OSRM route when both locations are set
    val scope = rememberCoroutineScope()
    val osrm  = remember { OsrmRepository() }
    LaunchedEffect(departResult, arriveeResult) {
        val dep = departResult ?: return@LaunchedEffect
        val arr = arriveeResult ?: return@LaunchedEffect
        isCalcRoute = true
        val route = osrm.getRoute(dep.lat, dep.lon, arr.lat, arr.lon)
        osrmDistanceKm = route?.distanceKm ?: 0.0
        isCalcRoute = false
    }

    LaunchedEffect(bookingState) {
        if (bookingState is ClientViewModel.BookingState.Success) {
            departResult = null; arriveeResult = null; osrmDistanceKm = 0.0
            selectedDateMillis = null; selectedHour = 8; selectedMinute = 0
            planifierPending = false; selectedTab = 1
            viewModel.resetBookingState()
        }
    }

    // ── Date picker ───────────────────────────────────────────────────────────
    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false; planifierPending = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false; showTimePicker = true
                }) { Text("Suivant") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false; planifierPending = false }) {
                    Text("Annuler")
                }
            }
        ) { DatePicker(state = dpState) }
    }

    // ── Time picker ───────────────────────────────────────────────────────────
    if (showTimePicker) {
        val tpState = rememberTimePickerState(
            initialHour = selectedHour, initialMinute = selectedMinute, is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false; planifierPending = false },
            title = { Text("Heure de prise en charge") },
            text  = { TimePicker(state = tpState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = tpState.hour; selectedMinute = tpState.minute
                    showTimePicker = false
                    if (planifierPending) {
                        planifierPending = false
                        val pickup = (selectedDateMillis ?: 0L) +
                            selectedHour * 3_600_000L + selectedMinute * 60_000L
                        viewModel.createReservation(
                            clientNom, clientTelephone,
                            departResult?.displayName ?: "",
                            arriveeResult?.displayName ?: "",
                            scheduledTime = pickup,
                            type          = FirestoreReservation.TYPE_PLANIFIEE,
                            prixEstime    = prixNormal,
                            departLat     = departResult?.lat,
                            departLng     = departResult?.lon,
                            arriveeLat    = arriveeResult?.lat,
                            arriveeLng    = arriveeResult?.lon,
                            distanceKm    = osrmDistanceKm
                        )
                    }
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false; planifierPending = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Espace Client") },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Outlined.AccountCircle, contentDescription = "Mon profil")
                        }
                        IconButton(onClick = onSignOut) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Se déconnecter")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        text = { Text("Réserver") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        text = {
                            val active = myReservations.count { !it.isFinished }
                            Text(if (active > 0) "Mes courses ($active)" else "Mes courses")
                        })
                }
                when (selectedTab) {
                    0 -> ReservationForm(
                        clientNom, { clientNom = it },
                        clientTelephone, { clientTelephone = it },
                        departResult, arriveeResult,
                        osrmDistanceKm, isCalcRoute,
                        prixNormal, prixImmediate,
                        isLoading = bookingState is ClientViewModel.BookingState.Loading,
                        error = (bookingState as? ClientViewModel.BookingState.Error)?.message,
                        onPickDepart   = { mapPickerTarget = "depart" },
                        onPickArrivee  = { mapPickerTarget = "arrivee" },
                        onPlanifier = { planifierPending = true; showDatePicker = true },
                        onMaintenant = {
                            viewModel.createReservation(
                                clientNom, clientTelephone,
                                departResult?.displayName ?: "",
                                arriveeResult?.displayName ?: "",
                                scheduledTime = null,
                                type          = FirestoreReservation.TYPE_IMMEDIATE,
                                prixEstime    = prixImmediate,
                                departLat     = departResult?.lat,
                                departLng     = departResult?.lon,
                                arriveeLat    = arriveeResult?.lat,
                                arriveeLng    = arriveeResult?.lon,
                                distanceKm    = osrmDistanceKm
                            )
                        }
                    )
                    1 -> ReservationList(
                        reservations = myReservations,
                        onCancel     = viewModel::cancelReservation
                    )
                }
            }
        }

        // ── Full-screen map picker overlay ────────────────────────────────────
        if (mapPickerTarget != null) {
            MapPickerOverlay(
                title      = if (mapPickerTarget == "depart") "Choisir le départ" else "Choisir l'arrivée",
                onConfirm  = { result ->
                    if (mapPickerTarget == "depart") departResult = result
                    else                             arriveeResult = result
                    mapPickerTarget = null
                },
                onDismiss  = { mapPickerTarget = null }
            )
        }
    }
}

// ── Booking form ──────────────────────────────────────────────────────────────

@Composable
private fun ReservationForm(
    clientNom: String, onNomChange: (String) -> Unit,
    clientTel: String, onTelChange: (String) -> Unit,
    departResult: NominatimResult?,
    arriveeResult: NominatimResult?,
    distanceKm: Double,
    isCalcRoute: Boolean,
    prixNormal: Double, prixImmediate: Double,
    isLoading: Boolean,
    error: String?,
    onPickDepart: () -> Unit,
    onPickArrivee: () -> Unit,
    onPlanifier: () -> Unit,
    onMaintenant: () -> Unit
) {
    val formValid = clientNom.isNotBlank() && clientTel.isNotBlank() &&
                    departResult != null && arriveeResult != null

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { Text("Nouvelle réservation", style = MaterialTheme.typography.titleMedium) }

        if (error != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error, modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        item {
            OutlinedTextField(clientNom, onNomChange,
                label = { Text("Votre nom") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        item {
            OutlinedTextField(clientTel, onTelChange,
                label = { Text("Votre téléphone") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        }

        // Départ picker
        item {
            LocationPickerField(
                label    = "Adresse de départ",
                result   = departResult,
                icon     = Icons.Filled.LocationOn,
                iconTint = GREEN,
                onClick  = onPickDepart
            )
        }
        // Arrivée picker
        item {
            LocationPickerField(
                label    = "Adresse d'arrivée",
                result   = arriveeResult,
                icon     = Icons.Filled.LocationOn,
                iconTint = Color(0xFFF44336),
                onClick  = onPickArrivee
            )
        }

        // Distance + price estimation card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Estimation du tarif",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    if (isCalcRoute) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("Calcul du trajet…", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (distanceKm > 0.0) {
                        Text("Distance : %.1f km".format(distanceKm),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tarif planifié", style = MaterialTheme.typography.bodyMedium)
                        Text("%.2f MAD".format(prixNormal), fontWeight = FontWeight.Bold, color = GREEN)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tarif immédiat (+20%)", style = MaterialTheme.typography.bodyMedium)
                        Text("%.2f MAD".format(prixImmediate), fontWeight = FontWeight.Bold, color = ORANGE)
                    }
                    Text("Base : 3,50 MAD/km · Minimum 15,00 MAD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.55f))
                }
            }
        }

        // Action buttons
        item {
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPlanifier, enabled = formValid,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GREEN)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗓 Planifier", fontWeight = FontWeight.Bold)
                            Text("%.2f MAD".format(prixNormal), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Button(
                        onClick = onMaintenant, enabled = formValid,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ORANGE)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚖 Maintenant", fontWeight = FontWeight.Bold)
                            Text("%.2f MAD".format(prixImmediate), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// ── Location picker button ────────────────────────────────────────────────────

@Composable
private fun LocationPickerField(
    label: String,
    result: NominatimResult?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (result != null) {
                    Text(result.shortName(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                } else {
                    Text("Appuyez pour choisir sur la carte",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Filled.Map, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp))
        }
    }
}

// ── Full-screen map picker overlay ────────────────────────────────────────────

@Composable
private fun MapPickerOverlay(
    title: String,
    onConfirm: (NominatimResult) -> Unit,
    onDismiss: () -> Unit
) {
    val scope         = rememberCoroutineScope()
    val nominatim     = remember { NominatimRepository() }

    var searchQuery    by remember { mutableStateOf("") }
    var suggestions    by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var isSearching    by remember { mutableStateOf(false) }
    var selected       by remember { mutableStateOf<NominatimResult?>(null) }
    var showSuggestions by remember { mutableStateOf(false) }

    // Debounce search
    LaunchedEffect(searchQuery) {
        if (searchQuery.length < 3) { suggestions = emptyList(); showSuggestions = false; return@LaunchedEffect }
        delay(450)
        isSearching = true
        suggestions = nominatim.search(searchQuery)
        showSuggestions = suggestions.isNotEmpty()
        isSearching = false
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // OSMDroid map
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).also { mv ->
                    mv.setTileSource(TileSourceFactory.MAPNIK)
                    mv.setMultiTouchControls(true)
                    mv.controller.setZoom(13.0)
                    mv.controller.setCenter(GeoPoint(33.5731, -7.5898)) // Casablanca default

                    val eventsOverlay = object : Overlay() {
                        override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                            val p = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                            scope.launch {
                                val addr = nominatim.reverse(p.latitude, p.longitude)
                                val result = NominatimResult(p.latitude, p.longitude, addr)
                                selected = result
                                searchQuery = result.shortName()
                                showSuggestions = false
                                updateMapMarker(mv, p)
                            }
                            return true
                        }
                    }
                    mv.overlays.add(0, eventsOverlay)
                    mapViewRef = mv
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar with search
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Title + close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Fermer",
                        modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(4.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = { Text("Rechercher une adresse…") },
                leadingIcon   = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon  = {
                    if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Suggestions dropdown
            if (showSuggestions) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.heightIn(max = 220.dp)) {
                        suggestions.forEach { result ->
                            TextButton(
                                onClick = {
                                    selected = result
                                    searchQuery = result.shortName()
                                    showSuggestions = false
                                    mapViewRef?.let { mv ->
                                        val gp = GeoPoint(result.lat, result.lon)
                                        mv.controller.animateTo(gp)
                                        mv.controller.setZoom(15.0)
                                        updateMapMarker(mv, gp)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    result.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        // Hint
        if (selected == null) {
            Text(
                "Appuyez sur la carte pour placer le marqueur",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Confirm button
        Button(
            onClick  = { selected?.let { onConfirm(it) } },
            enabled  = selected != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp)
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Confirmer cette position", fontWeight = FontWeight.Bold)
        }
    }
}

private fun updateMapMarker(mapView: MapView, point: GeoPoint) {
    mapView.overlays.removeAll { it is Marker }
    val marker = Marker(mapView).apply {
        position = point
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
    mapView.overlays.add(marker)
    mapView.controller.animateTo(point)
    mapView.invalidate()
}

// ── Reservation list ──────────────────────────────────────────────────────────

@Composable
private fun ReservationList(
    reservations: List<FirestoreReservation>,
    onCancel: (String) -> Unit
) {
    if (reservations.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucune réservation", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(reservations, key = { it.id }) { res ->
            ReservationCard(res, onCancel = { onCancel(res.id) })
        }
    }
}

@Composable
private fun ReservationCard(res: FirestoreReservation, onCancel: () -> Unit) {
    val dtFmt = remember { SimpleDateFormat("dd/MM/yyyy 'à' HH:mm", Locale.FRENCH) }
    val displayTime = res.scheduledTime?.let { dtFmt.format(Date(it)) }
        ?: dtFmt.format(Date(res.createdAt))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(displayTime, style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                    if (res.isImmediate) {
                        Text("Course immédiate", style = MaterialTheme.typography.labelSmall,
                            color = ORANGE, fontWeight = FontWeight.Bold)
                    }
                }
                StatusBadge(res.status)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
            Text("De : ${res.depart}", style = MaterialTheme.typography.bodySmall)
            Text("À : ${res.arrivee}", style = MaterialTheme.typography.bodySmall)
            if (res.distanceKm > 0.0) {
                Text("Distance : %.1f km".format(res.distanceKm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (res.prixFinal != null) {
                Text("Prix final : %.2f MAD".format(res.prixFinal),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold, color = GREEN)
            } else {
                Text("Estimation : %.2f MAD".format(res.prixEstime),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold, color = GREEN)
            }
            if (res.chauffeurName != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                Text("Chauffeur : ${res.chauffeurName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (res.taxiAssigned != null) {
                    Text("Véhicule : ${res.taxiAssigned}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            StatusTimeline(res.status)
            if (res.status == FirestoreReservation.STATUS_EN_ATTENTE) {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Annuler") }
            }
        }
    }
}

// ── Status timeline ───────────────────────────────────────────────────────────

@Composable
private fun StatusTimeline(status: String) {
    val steps = listOf(
        FirestoreReservation.STATUS_EN_ATTENTE to "En attente",
        FirestoreReservation.STATUS_ACCEPTEE   to "Acceptée",
        FirestoreReservation.STATUS_EN_COURS   to "En cours",
        FirestoreReservation.STATUS_TERMINEE   to "Terminée"
    )
    if (status == FirestoreReservation.STATUS_ANNULEE) {
        Text("Réservation annulée", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error)
        return
    }
    val currentIdx = steps.indexOfFirst { it.first == status }.coerceAtLeast(0)
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, (_, label) ->
            val done    = idx <= currentIdx
            val current = idx == currentIdx
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when { current -> MaterialTheme.colorScheme.primary; done -> GREEN; else -> MaterialTheme.colorScheme.surfaceVariant },
                    modifier = Modifier.size(10.dp)
                ) {}
                Text(label, style = MaterialTheme.typography.labelSmall,
                    color = when { current -> MaterialTheme.colorScheme.primary; done -> GREEN; else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) },
                    maxLines = 1)
            }
            if (idx < steps.lastIndex) {
                HorizontalDivider(modifier = Modifier.weight(0.5f).padding(bottom = 10.dp),
                    color = if (idx < currentIdx) GREEN else MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

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
    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.15f), contentColor = color) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall)
    }
}
