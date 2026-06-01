package com.example.taxipilot.feature.driver

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
import com.example.taxipilot.core.data.database.entity.ChargeEntity
import com.example.taxipilot.core.data.database.entity.TypeCharge
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ── Period filter ─────────────────────────────────────────────────────────────

private enum class HistoryPeriod(val label: String) {
    TODAY("Aujourd'hui"),
    WEEK("7 jours"),
    MONTH("30 jours"),
    ALL("Tout")
}

private fun HistoryPeriod.startMillis(): Long {
    val cal = Calendar.getInstance()
    return when (this) {
        HistoryPeriod.TODAY -> cal.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        HistoryPeriod.WEEK  -> System.currentTimeMillis() - 7L  * 86_400_000L
        HistoryPeriod.MONTH -> System.currentTimeMillis() - 30L * 86_400_000L
        HistoryPeriod.ALL   -> 0L
    }
}

// ── Mixed history entry ───────────────────────────────────────────────────────

private sealed class HistoryEntry {
    data class Course(val res: FirestoreReservation) : HistoryEntry()
    data class Charge(val charge: ChargeEntity)      : HistoryEntry()

    val timestamp: Long get() = when (this) {
        is Course -> res.completedAt ?: res.createdAt
        is Charge -> charge.date
    }
}

// ── Main composable ───────────────────────────────────────────────────────────

@Composable
fun DriverHistoryTab(viewModel: DriverViewModel, modifier: Modifier = Modifier) {
    val allCourses by viewModel.myCourses.collectAsState()
    val allCharges by viewModel.allChargesHistory.collectAsState()
    var period     by remember { mutableStateOf(HistoryPeriod.TODAY) }

    val start = remember(period) { period.startMillis() }

    val entries = remember(allCourses, allCharges, period) {
        val courses = allCourses
            .filter { it.status == FirestoreReservation.STATUS_TERMINEE }
            .filter { (it.completedAt ?: it.createdAt) >= start }
            .map { HistoryEntry.Course(it) }

        val charges = allCharges
            .filter { it.date >= start }
            .map { HistoryEntry.Charge(it) }

        (courses + charges).sortedByDescending { it.timestamp }
    }

    val totalRevenu  = entries.filterIsInstance<HistoryEntry.Course>()
        .sumOf { it.res.prixFinal ?: it.res.prixEstime }
    val totalCharges = entries.filterIsInstance<HistoryEntry.Charge>()
        .sumOf { it.charge.montant }
    val bénéfice = totalRevenu - totalCharges

    Column(modifier = modifier) {

        // ── Period selector ───────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HistoryPeriod.entries) { p ->
                FilterChip(
                    selected = period == p,
                    onClick  = { period = p },
                    label    = { Text(p.label) }
                )
            }
        }

        // ── Summary totals ────────────────────────────────────────────────────
        if (entries.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TotalItem("Revenus",  "%.0f MAD".format(totalRevenu),  Color(0xFF2E7D32))
                    TotalItem("Charges",  "%.0f MAD".format(totalCharges), Color(0xFFC62828))
                    TotalItem("Bénéfice", "%.0f MAD".format(bénéfice),
                        if (bénéfice >= 0) Color(0xFF2E7D32) else Color(0xFFC62828))
                }
            }
        }

        // ── List ──────────────────────────────────────────────────────────────
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune entrée pour cette période",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { e ->
                    when (e) {
                        is HistoryEntry.Course -> "c_${e.res.id}"
                        is HistoryEntry.Charge -> "ch_${e.charge.id}"
                    }
                }) { entry ->
                    when (entry) {
                        is HistoryEntry.Course -> HistoryCourseCard(entry.res)
                        is HistoryEntry.Charge -> HistoryChargeCard(entry.charge)
                    }
                }
            }
        }
    }
}

// ── Course card (green) ───────────────────────────────────────────────────────

@Composable
private fun HistoryCourseCard(res: FirestoreReservation) {
    val dtFmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH) }
    val time  = res.completedAt?.let { dtFmt.format(Date(it)) }
        ?: dtFmt.format(Date(res.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E7D32).copy(alpha = 0.06f)
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                        contentColor = Color(0xFF2E7D32)
                    ) {
                        Text("Course", modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text(time, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(res.depart, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("→ ${res.arrivee}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                res.clientName.takeIf { it.isNotBlank() }?.let {
                    Text("Client : $it", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                res.distanceReelle?.takeIf { it > 0.0 }?.let {
                    Text("%.1f km".format(it), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "+ %.0f MAD".format(res.prixFinal ?: res.prixEstime),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

// ── Charge card (red) ─────────────────────────────────────────────────────────

@Composable
private fun HistoryChargeCard(charge: ChargeEntity) {
    val dtFmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH) }
    val typeColor = when (charge.type) {
        TypeCharge.DIESEL     -> Color(0xFF2196F3)
        TypeCharge.REPARATION -> Color(0xFFFF9800)
        TypeCharge.AUTRE      -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFC62828).copy(alpha = 0.05f)
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = typeColor.copy(alpha = 0.15f),
                        contentColor = typeColor
                    ) {
                        Text(charge.type.label(), modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text(dtFmt.format(Date(charge.date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(charge.description, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            Text(
                "- %.0f MAD".format(charge.montant),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336)
            )
        }
    }
}

// ── Total summary item ────────────────────────────────────────────────────────

@Composable
private fun TotalItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
    }
}

private fun TypeCharge.label() = when (this) {
    TypeCharge.DIESEL     -> "Diesel"
    TypeCharge.REPARATION -> "Réparation"
    TypeCharge.AUTRE      -> "Autre"
}
