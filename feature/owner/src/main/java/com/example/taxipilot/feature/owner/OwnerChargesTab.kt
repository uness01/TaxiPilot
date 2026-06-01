package com.example.taxipilot.feature.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.firestore.FirestoreCharge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerChargesTab(viewModel: OwnerViewModel, modifier: Modifier = Modifier) {
    val charges     by viewModel.firestoreCharges.collectAsState()
    val myChauffeurs by viewModel.myChauffeurs.collectAsState()

    val chauffeurNames = remember(myChauffeurs) {
        myChauffeurs.associate { it.uid to it.nom }
    }

    // Collect distinct type strings for the filter chips
    val types = remember(charges) {
        charges.map { it.type }.distinct().sorted()
    }

    var activeFilter by remember { mutableStateOf<String?>(null) }
    val filtered = remember(charges, activeFilter) {
        if (activeFilter == null) charges else charges.filter { it.type == activeFilter }
    }
    val totalFiltered = remember(filtered) { filtered.sumOf { it.montant } }

    Column(modifier = modifier.fillMaxSize()) {
        // ── Filter chips ──────────────────────────────────────────────────────
        if (types.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick  = { activeFilter = null },
                        label    = { Text("Tous (${charges.size})") }
                    )
                }
                items(types) { type ->
                    val count = charges.count { it.type == type }
                    FilterChip(
                        selected = activeFilter == type,
                        onClick  = { activeFilter = if (activeFilter == type) null else type },
                        label    = { Text("$type ($count)") }
                    )
                }
            }
        }

        // ── Total banner ──────────────────────────────────────────────────────
        if (filtered.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${filtered.size} charge(s) — tous chauffeurs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        "− ${totalFiltered.toMontant()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // ── List ──────────────────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Aucune charge enregistrée",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Les charges sont créées par les chauffeurs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { charge ->
                    FirestoreChargeCard(
                        charge       = charge,
                        chauffeurNom = chauffeurNames[charge.chauffeurUid] ?: charge.chauffeurUid
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun FirestoreChargeCard(
    charge: FirestoreCharge,
    chauffeurNom: String
) {
    val dtFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        charge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Chauffeur : $chauffeurNom",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "− ${charge.montant.toMontant()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            charge.type,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                dtFmt.format(Date(charge.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
