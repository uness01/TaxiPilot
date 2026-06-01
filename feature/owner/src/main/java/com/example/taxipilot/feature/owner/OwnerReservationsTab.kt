package com.example.taxipilot.feature.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val STATUS_FILTERS = listOf(
    null                                   to "Toutes",
    FirestoreReservation.STATUS_EN_ATTENTE to "En attente",
    FirestoreReservation.STATUS_ACCEPTEE   to "Acceptées",
    FirestoreReservation.STATUS_EN_COURS   to "En cours",
    FirestoreReservation.STATUS_TERMINEE   to "Terminées",
    FirestoreReservation.STATUS_ANNULEE    to "Annulées"
)

@Composable
fun OwnerReservationsTab(viewModel: OwnerViewModel, modifier: Modifier = Modifier) {
    val allReservations by viewModel.allReservations.collectAsState()
    var filterStatus by remember { mutableStateOf<String?>(null) }

    val filtered = remember(allReservations, filterStatus) {
        if (filterStatus == null) allReservations
        else allReservations.filter { it.status == filterStatus }
    }

    Column(modifier = modifier) {
        // ── Stats row ─────────────────────────────────────────────────────────
        ReservationStatsRow(allReservations)

        // ── Filter chips ──────────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(STATUS_FILTERS) { (status, label) ->
                val count = if (status == null) allReservations.size
                            else allReservations.count { it.status == status }
                FilterChip(
                    selected = filterStatus == status,
                    onClick  = { filterStatus = status },
                    label    = { Text("$label ($count)") }
                )
            }
        }

        // ── List ──────────────────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune réservation", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { res ->
                    OwnerReservationCard(res)
                }
            }
        }
    }
}

// ── Stats row ─────────────────────────────────────────────────────────────────

@Composable
private fun ReservationStatsRow(reservations: List<FirestoreReservation>) {
    val enAttente = reservations.count { it.status == FirestoreReservation.STATUS_EN_ATTENTE }
    val enCours   = reservations.count { it.status == FirestoreReservation.STATUS_EN_COURS  }
    val terminee  = reservations.count { it.status == FirestoreReservation.STATUS_TERMINEE  }
    val ca        = reservations.filter { it.status == FirestoreReservation.STATUS_TERMINEE }
        .sumOf { it.prixFinal ?: it.prixEstime }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("En attente", enAttente.toString(), Color(0xFFFF9800))
            StatItem("En cours",   enCours.toString(),   Color(0xFF2196F3))
            StatItem("Terminées",  terminee.toString(),  Color(0xFF4CAF50))
            StatItem("CA",         "%.0f MAD".format(ca), Color(0xFF2E7D32))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

// ── Reservation card ──────────────────────────────────────────────────────────

@Composable
private fun OwnerReservationCard(res: FirestoreReservation) {
    val dtFmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        dtFmt.format(Date(res.scheduledTime ?: res.createdAt)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        if (res.isImmediate) "🚨 Immédiate" else "🗓 Planifiée",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (res.isImmediate) Color(0xFFE65100)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(res.status)
            }

            HorizontalDivider()

            Text("Client : ${res.clientName}  ·  ${res.clientPhone}",
                style = MaterialTheme.typography.bodySmall)
            Text("De : ${res.depart}", style = MaterialTheme.typography.bodySmall)
            Text("À  : ${res.arrivee}", style = MaterialTheme.typography.bodySmall)

            if (res.chauffeurName != null) {
                Text("Chauffeur : ${res.chauffeurName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Estimé : %.2f MAD".format(res.prixEstime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                res.prixFinal?.let {
                    Text("Final : %.2f MAD".format(it),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32))
                }
            }
        }
    }
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
