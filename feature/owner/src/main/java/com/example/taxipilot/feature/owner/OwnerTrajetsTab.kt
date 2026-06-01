package com.example.taxipilot.feature.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.database.entity.TrajetEntity
import com.example.taxipilot.core.data.database.entity.TrajetStatut

@Composable
fun OwnerTrajetsTab(viewModel: OwnerViewModel, modifier: Modifier = Modifier) {
    val allTrajets by viewModel.allTrajets.collectAsState()
    val allTaxis by viewModel.allTaxis.collectAsState()
    val allChauffeurs by viewModel.allChauffeurs.collectAsState()
    var activeFilter by remember { mutableStateOf<TrajetStatut?>(null) }

    val taxiMap = remember(allTaxis) { allTaxis.associateBy { it.id } }
    val chauffeurMap = remember(allChauffeurs) { allChauffeurs.associateBy { it.id } }
    val filtered = remember(allTrajets, activeFilter) {
        if (activeFilter == null) allTrajets
        else allTrajets.filter { it.statut == activeFilter }
    }

    Column(modifier = modifier) {
        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = activeFilter == null,
                    onClick = { activeFilter = null },
                    label = { Text("Tous (${allTrajets.size})") }
                )
            }
            items(TrajetStatut.entries) { statut ->
                val count = allTrajets.count { it.statut == statut }
                FilterChip(
                    selected = activeFilter == statut,
                    onClick = { activeFilter = if (activeFilter == statut) null else statut },
                    label = { Text("${statut.label()} ($count)") }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Aucun trajet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { trajet ->
                    TrajetCard(
                        trajet = trajet,
                        taxiLabel = taxiMap[trajet.taxiId]
                            ?.let { "${it.marque} ${it.modele} · ${it.immatriculation}" }
                            ?: "—",
                        chauffeurLabel = chauffeurMap[trajet.chauffeurId]
                            ?.let { "${it.prenom} ${it.nom}" }
                            ?: "—"
                    )
                }
            }
        }
    }
}

@Composable
private fun TrajetCard(
    trajet: TrajetEntity,
    taxiLabel: String,
    chauffeurLabel: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Origin → Destination + Statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        trajet.adresseDepart,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "→ ${trajet.adresseArrivee}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                TrajetStatutBadge(trajet.statut)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Taxi & Chauffeur
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LabeledField("Taxi", taxiLabel)
                LabeledField("Chauffeur", chauffeurLabel)
            }

            Spacer(Modifier.height(6.dp))

            // Date & Montant
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Départ: ${trajet.dateDebut.toDateString("dd/MM/yyyy HH:mm")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (trajet.dateFin != null) {
                        Text(
                            "Fin: ${trajet.dateFin.toDateString("dd/MM/yyyy HH:mm")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    trajet.montant?.let {
                        Text(
                            it.toMontant(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    trajet.distanceKm?.let {
                        Text(
                            "${"%.1f".format(it)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
