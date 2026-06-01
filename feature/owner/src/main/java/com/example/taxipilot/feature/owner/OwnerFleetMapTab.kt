package com.example.taxipilot.feature.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.taxipilot.core.data.firestore.FirestoreUser
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OwnerFleetMapTab(
    viewModel: OwnerViewModel,
    modifier: Modifier = Modifier
) {
    val chauffeurs by viewModel.myChauffeurs.collectAsState()
    val enCourse   = chauffeurs.filter {
        it.currentLat != null && it.currentLng != null
    }

    var selectedChauffeur by remember { mutableStateOf<FirestoreUser?>(null) }

    Column(modifier = modifier) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Flotte en temps réel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (enCourse.isNotEmpty()) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "${enCourse.size} en course",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (enCourse.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (chauffeurs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚖", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Aucun chauffeur lié",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return
        }

        if (enCourse.isEmpty()) {
            // Show offline list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📡", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Aucun chauffeur en course actuellement",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${chauffeurs.size} chauffeur(s) lié(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return
        }

        // ── OSMDroid map ──────────────────────────────────────────────────────
        FleetMapView(
            chauffeurs = enCourse,
            onMarkerTap = { selectedChauffeur = it },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        // ── Bottom legend ─────────────────────────────────────────────────────
        Text(
            "Appuyez sur un marqueur pour voir les détails",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth()
        )
    }

    // ── Chauffeur detail bottom sheet ─────────────────────────────────────────
    selectedChauffeur?.let { ch ->
        AlertDialog(
            onDismissRequest = { selectedChauffeur = null },
            title = { Text(ch.nom) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Taxi : ${ch.assignedTaxi ?: "Non assigné"}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Statut : ${ch.statut}",
                        style = MaterialTheme.typography.bodyMedium)
                    if (ch.currentLat != null && ch.currentLng != null) {
                        Text(
                            "Position : %.5f, %.5f".format(ch.currentLat, ch.currentLng),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedChauffeur = null }) { Text("Fermer") }
            }
        )
    }
}

@Composable
private fun FleetMapView(
    chauffeurs: List<FirestoreUser>,
    onMarkerTap: (FirestoreUser) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    // Center on first chauffeur
    val centerLat = chauffeurs.firstOrNull()?.currentLat ?: 33.5731
    val centerLng = chauffeurs.firstOrNull()?.currentLng ?: -7.5898

    AndroidView(
        factory = { context ->
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(13.0)
                controller.setCenter(GeoPoint(centerLat, centerLng))
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            chauffeurs.forEach { ch ->
                val lat = ch.currentLat ?: return@forEach
                val lng = ch.currentLng ?: return@forEach
                val marker = Marker(mapView).apply {
                    position = GeoPoint(lat, lng)
                    title    = ch.nom
                    snippet  = ch.assignedTaxi ?: ""
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onMarkerTap(ch)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        },
        modifier = modifier
    )
}
