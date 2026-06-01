package com.example.taxipilot.feature.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.database.entity.TaxiEntity
import com.example.taxipilot.core.data.database.entity.TaxiStatut

@Composable
fun OwnerTaxisTab(viewModel: OwnerViewModel, modifier: Modifier = Modifier) {
    val taxis by viewModel.allTaxis.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<TaxiEntity?>(null) }
    var statutTarget by remember { mutableStateOf<TaxiEntity?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Ajouter un taxi") }
            )
        }
    ) { padding ->
        if (taxis.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun taxi enregistré", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(taxis, key = { it.id }) { taxi ->
                    TaxiCard(
                        taxi = taxi,
                        onEdit = { editTarget = taxi },
                        onDelete = { viewModel.deleteTaxi(taxi) },
                        onChangeStatut = { statutTarget = taxi }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (showAddDialog) {
        TaxiFormDialog(
            taxi = null,
            onConfirm = { viewModel.addTaxi(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
    editTarget?.let { t ->
        TaxiFormDialog(
            taxi = t,
            onConfirm = { viewModel.updateTaxi(it); editTarget = null },
            onDismiss = { editTarget = null }
        )
    }
    statutTarget?.let { t ->
        TaxiStatutDialog(
            current = t.statut,
            onConfirm = { viewModel.updateTaxiStatut(t.id, it); statutTarget = null },
            onDismiss = { statutTarget = null }
        )
    }
}

@Composable
private fun TaxiCard(
    taxi: TaxiEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangeStatut: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${taxi.marque} ${taxi.modele}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        taxi.immatriculation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TaxiStatutBadge(taxi.statut)
                Spacer(Modifier.width(4.dp))
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Modifier") },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Changer le statut") },
                            onClick = { menuExpanded = false; onChangeStatut() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Supprimer", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuExpanded = false; showDeleteConfirm = true }
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoPill("Année", taxi.annee.toString())
                InfoPill("Couleur", taxi.couleur)
                InfoPill("Km", "${taxi.kilometrage} km")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer ce taxi ?") },
            text = {
                Text(
                    "${taxi.marque} ${taxi.modele} (${taxi.immatriculation}) sera supprimé " +
                            "ainsi que toutes ses charges associées."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun InfoPill(key: String, value: String) {
    Column {
        Text(key, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TaxiFormDialog(
    taxi: TaxiEntity?,
    onConfirm: (TaxiEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var marque by remember { mutableStateOf(taxi?.marque ?: "") }
    var modele by remember { mutableStateOf(taxi?.modele ?: "") }
    var immatriculation by remember { mutableStateOf(taxi?.immatriculation ?: "") }
    var annee by remember { mutableStateOf(taxi?.annee?.toString() ?: "") }
    var couleur by remember { mutableStateOf(taxi?.couleur ?: "") }
    var kilometrage by remember { mutableStateOf(taxi?.kilometrage?.toString() ?: "0") }

    val isValid = marque.isNotBlank() && modele.isNotBlank() &&
            immatriculation.isNotBlank() && annee.toIntOrNull() != null && couleur.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (taxi == null) "Ajouter un taxi" else "Modifier le taxi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = marque, onValueChange = { marque = it },
                    label = { Text("Marque *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = modele, onValueChange = { modele = it },
                    label = { Text("Modèle *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = immatriculation, onValueChange = { immatriculation = it.uppercase() },
                    label = { Text("Immatriculation *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = annee, onValueChange = { annee = it },
                        label = { Text("Année *") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = couleur, onValueChange = { couleur = it },
                        label = { Text("Couleur *") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = kilometrage, onValueChange = { kilometrage = it },
                    label = { Text("Kilométrage") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        TaxiEntity(
                            id = taxi?.id ?: 0,
                            marque = marque.trim(),
                            modele = modele.trim(),
                            immatriculation = immatriculation.trim(),
                            annee = annee.toInt(),
                            couleur = couleur.trim(),
                            kilometrage = kilometrage.toIntOrNull() ?: 0,
                            statut = taxi?.statut ?: TaxiStatut.DISPONIBLE
                        )
                    )
                },
                enabled = isValid
            ) { Text("Confirmer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun TaxiStatutDialog(
    current: TaxiStatut,
    onConfirm: (TaxiStatut) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Changer le statut") },
        text = {
            Column {
                TaxiStatut.entries.forEach { statut ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selected == statut,
                            onClick = { selected = statut }
                        )
                        Spacer(Modifier.width(8.dp))
                        TaxiStatutBadge(statut)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Confirmer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
