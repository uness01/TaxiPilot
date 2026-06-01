package com.example.taxipilot.feature.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.database.entity.ChargeEntity
import com.example.taxipilot.core.data.database.entity.TypeCharge
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriverSummaryTab(viewModel: DriverViewModel, modifier: Modifier = Modifier) {
    val summary by viewModel.dailySummary.collectAsState()
    val today = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(Date())

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                today.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Bilan du jour",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn("Courses",  summary.nbCourses.toString(),
                            MaterialTheme.colorScheme.onPrimaryContainer)
                        VerticalDivider(modifier = Modifier.height(48.dp))
                        StatColumn("Recettes", summary.totalRecettes.toAmountString(), Color(0xFF2E7D32))
                        VerticalDivider(modifier = Modifier.height(48.dp))
                        StatColumn("Charges",  summary.totalCharges.toAmountString(),  Color(0xFFC62828))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Net du jour",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            summary.benefice.toAmountString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.benefice >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }

        // ── Today's Firestore courses ─────────────────────────────────────────
        if (summary.firestoreCourses.isNotEmpty()) {
            item { SectionLabel("Courses du jour (${summary.firestoreCourses.size})") }
            items(summary.firestoreCourses, key = { it.id }) { course ->
                SummaryCourseRow(course)
            }
        } else {
            item { EmptyStateRow("Aucune course terminée aujourd'hui") }
        }

        // ── Today's charges ───────────────────────────────────────────────────
        if (summary.charges.isNotEmpty()) {
            item { SectionLabel("Charges du jour (${summary.charges.size})") }
            items(summary.charges, key = { it.id }) { charge ->
                SummaryChargeRow(charge)
            }
        } else {
            item { EmptyStateRow("Aucune charge enregistrée aujourd'hui") }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SummaryCourseRow(course: FirestoreReservation) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.FRENCH) }
    val time = course.completedAt?.let { timeFmt.format(Date(it)) }
        ?: timeFmt.format(Date(course.createdAt))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(time, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(course.depart, style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold)
                Text("→ ${course.arrivee}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                course.clientName.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                (course.prixFinal ?: course.prixEstime).toAmountString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun SummaryChargeRow(charge: ChargeEntity) {
    val typeColor = when (charge.type) {
        TypeCharge.DIESEL     -> Color(0xFF2196F3)
        TypeCharge.REPARATION -> Color(0xFFFF9800)
        TypeCharge.AUTRE      -> Color(0xFF9E9E9E)
    }
    val time = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(charge.date))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(time, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(charge.description, style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold)
                Surface(shape = RoundedCornerShape(3.dp), color = typeColor.copy(alpha = 0.15f)) {
                    Text(charge.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall, color = typeColor)
                }
            }
            Text("- ${charge.montant.toAmountString()}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
        }
    }
}

@Composable
private fun EmptyStateRow(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun Double.toAmountString() = "%.0f MAD".format(this)
