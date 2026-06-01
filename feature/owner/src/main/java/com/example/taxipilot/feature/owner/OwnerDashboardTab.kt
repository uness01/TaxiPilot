package com.example.taxipilot.feature.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.firestore.FirestoreUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OwnerDashboardTab(viewModel: OwnerViewModel, modifier: Modifier = Modifier) {
    val state              by viewModel.dashboardState.collectAsState()
    val myChauffeurs       by viewModel.myChauffeurs.collectAsState()
    val codeProprietaire   by viewModel.codeProprietaire.collectAsState()

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        // ── Proprietaire code card ────────────────────────────────────────────
        item {
            ProprietaireCodeCard(
                code = codeProprietaire,
                onRegenerate = { viewModel.regenerateCode() }
            )
        }

        // ── KPIs ─────────────────────────────────────────────────────────────
        item {
            SectionTitle("Vue d'ensemble")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard("Taxis",             state.nbTaxis.toString(),                   Modifier.weight(1f))
                KpiCard("Chauffeurs liés",   myChauffeurs.size.toString(),               Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiCard("Courses terminées", state.nbTrajetsTermines.toString(),         Modifier.weight(1f))
                KpiCard("En attente",        state.nbReservationsEnAttente.toString(),   Modifier.weight(1f))
            }
        }

        // ── Finances ─────────────────────────────────────────────────────────
        item {
            SectionTitle("Finances (ma flotte)")
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FinanceRow("Total recettes",   state.totalRecettes, Color(0xFF4CAF50))
                    HorizontalDivider()
                    FinanceRow("Total charges",    state.totalCharges,  Color(0xFFF44336))
                    HorizontalDivider()
                    FinanceRow("Bénéfice net",      state.benefice,
                        if (state.benefice >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        bold = true)
                }
            }
        }

        // ── Fleet — per-chauffeur breakdown ───────────────────────────────────
        if (state.chauffeurStats.isNotEmpty()) {
            item {
                SectionTitle("Ma flotte — aujourd'hui")
                Spacer(Modifier.height(8.dp))
            }
            items(state.chauffeurStats, key = { it.uid }) { stat ->
                ChauffeurStatCard(stat)
            }
        }

        // ── Charges by type ───────────────────────────────────────────────────
        if (state.chargesParType.isNotEmpty()) {
            item {
                SectionTitle("Répartition des charges")
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.chargesParType.entries
                            .filter { it.value > 0.0 }
                            .sortedByDescending { it.value }
                            .forEach { (typeStr, montant) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        typeStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(montant.toMontant(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold)
                                }
                                if (state.totalCharges > 0.0) {
                                    LinearProgressIndicator(
                                        progress = { (montant / state.totalCharges).toFloat() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

// ── Proprietaire code card ────────────────────────────────────────────────────

@Composable
private fun ProprietaireCodeCard(code: String?, onRegenerate: () -> Unit) {
    val clipboard  = LocalClipboardManager.current
    val scope      = rememberCoroutineScope()
    var justCopied       by remember { mutableStateOf(false) }
    var showRegenDialog  by remember { mutableStateOf(false) }

    if (showRegenDialog) {
        AlertDialog(
            onDismissRequest = { showRegenDialog = false },
            title = { Text("Changer votre code ?") },
            text = {
                Text(
                    "Attention : les nouveaux chauffeurs devront utiliser ce nouveau code. " +
                    "Les chauffeurs déjà liés ne sont pas affectés.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    showRegenDialog = false
                    onRegenerate()
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showRegenDialog = false }) { Text("Annuler") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Votre code Propriétaire",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))

            if (code != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        code,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        letterSpacing = TextUnit(6f, TextUnitType.Sp)
                    )
                    Spacer(Modifier.width(12.dp))
                    FilledTonalIconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(code))
                            scope.launch {
                                justCopied = true
                                delay(2000)
                                justCopied = false
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copier le code",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (justCopied) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Code copié !",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32)
                    )
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Donnez ce code à vos chauffeurs lors de leur inscription",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showRegenDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔄 Générer nouveau code")
            }
        }
    }
}

// ── Per-chauffeur stat card ───────────────────────────────────────────────────

@Composable
private fun ChauffeurStatCard(stat: ChauffeurStat) {
    val statutColor = when (stat.statut) {
        FirestoreUser.STATUT_EN_COURSE  -> Color(0xFF2196F3)
        else                            -> Color(0xFF4CAF50)
    }
    val statutLabel = when (stat.statut) {
        FirestoreUser.STATUT_EN_COURSE  -> "En course"
        else                            -> "Disponible"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stat.nom, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = statutColor.copy(alpha = 0.15f),
                        contentColor = statutColor
                    ) {
                        Text(statutLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    stat.assignedTaxi?.let {
                        Text("🚕 $it", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${stat.tripsToday} course${if (stat.tripsToday != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.0f MAD".format(stat.revenueToday),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun KpiCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FinanceRow(label: String, value: Double, color: Color, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value.toMontant(), style = MaterialTheme.typography.bodyMedium,
            color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
    }
}
