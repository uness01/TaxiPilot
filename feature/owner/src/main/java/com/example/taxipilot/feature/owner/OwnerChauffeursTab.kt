package com.example.taxipilot.feature.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.database.entity.ChauffeurEntity
import com.example.taxipilot.core.data.database.entity.ChauffeurStatut
import com.example.taxipilot.core.data.database.entity.TaxiEntity
import com.example.taxipilot.core.data.database.entity.TaxiStatut
import com.example.taxipilot.core.data.firestore.FirestoreCharge
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import com.example.taxipilot.core.data.firestore.FirestoreServiceLog
import com.example.taxipilot.core.data.firestore.FirestoreUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerChauffeursTab(viewModel: OwnerViewModel, modifier: Modifier = Modifier) {
    val myChauffeurs    by viewModel.myChauffeurs.collectAsState()
    val allTaxis        by viewModel.allTaxis.collectAsState()
    val localChauffs    by viewModel.allChauffeurs.collectAsState()
    val allReservations by viewModel.allReservations.collectAsState()
    val allFsCharges    by viewModel.firestoreCharges.collectAsState()
    var showAddDialog   by remember { mutableStateOf(false) }
    var editTarget      by remember { mutableStateOf<ChauffeurEntity?>(null) }
    var statutTarget    by remember { mutableStateOf<ChauffeurEntity?>(null) }
    var taxiTarget      by remember { mutableStateOf<FirestoreUser?>(null) }
    var detailTarget    by remember { mutableStateOf<FirestoreUser?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Ajouter local") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Firestore-linked chauffeurs (via app registration) ─────────────
            if (myChauffeurs.isNotEmpty()) {
                item {
                    Text("Chauffeurs de l'app (${myChauffeurs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary)
                }
                items(myChauffeurs, key = { "fs_${it.uid}" }) { ch ->
                    FirestoreChauffeurCard(
                        chauffeur    = ch,
                        onAssignTaxi = { taxiTarget = ch },
                        onUnassign   = { viewModel.unassignTaxi(ch.uid, ch.assignedTaxi) },
                        onClick      = { detailTarget = ch }
                    )
                }
                item { Spacer(Modifier.height(4.dp)) }
            }

            // ── Local Room chauffeurs ─────────────────────────────────────────
            if (localChauffs.isNotEmpty()) {
                item {
                    Text("Chauffeurs locaux (${localChauffs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(localChauffs, key = { it.id }) { chauffeur ->
                    ChauffeurCard(
                        chauffeur = chauffeur,
                        onEdit = { editTarget = chauffeur },
                        onDelete = { viewModel.deleteChauffeur(chauffeur) },
                        onChangeStatut = { statutTarget = chauffeur }
                    )
                }
            }

            if (myChauffeurs.isEmpty() && localChauffs.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Aucun chauffeur",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("Partagez votre code depuis le Tableau de bord",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (showAddDialog) {
        ChauffeurFormDialog(
            chauffeur = null,
            onConfirm = { viewModel.addChauffeur(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
    editTarget?.let { c ->
        ChauffeurFormDialog(
            chauffeur = c,
            onConfirm = { viewModel.updateChauffeur(it); editTarget = null },
            onDismiss = { editTarget = null }
        )
    }
    statutTarget?.let { c ->
        ChauffeurStatutDialog(
            current = c.statut,
            onConfirm = { viewModel.updateChauffeurStatut(c.id, it); statutTarget = null },
            onDismiss = { statutTarget = null }
        )
    }
    taxiTarget?.let { ch ->
        // Taxis held by hors_service chauffeurs are effectively free to reassign
        val horsServiceMatricules = myChauffeurs
            .filter { it.statut == FirestoreUser.STATUT_HORS_SERVICE && it.uid != ch.uid }
            .mapNotNull { it.assignedTaxi }
            .toSet()
        AssignTaxiDialog(
            chauffeur   = ch,
            taxis       = allTaxis.filter {
                it.statut == TaxiStatut.DISPONIBLE ||
                it.immatriculation == ch.assignedTaxi ||
                it.immatriculation in horsServiceMatricules
            },
            onConfirm   = { matricule ->
                viewModel.assignTaxiToChauffeur(ch.uid, matricule)
                taxiTarget = null
            },
            onDismiss   = { taxiTarget = null }
        )
    }
    detailTarget?.let { ch ->
        var serviceLogs by remember(ch.uid) { mutableStateOf<List<FirestoreServiceLog>>(emptyList()) }
        LaunchedEffect(ch.uid) {
            serviceLogs = viewModel.getServiceLogs(ch.uid)
        }
        ChauffeurDetailDialog(
            chauffeur    = ch,
            reservations = allReservations.filter { it.chauffeurId == ch.uid },
            charges      = allFsCharges.filter { it.chauffeurUid == ch.uid },
            serviceLogs  = serviceLogs,
            onDismiss    = { detailTarget = null }
        )
    }
}

@Composable
private fun ChauffeurCard(
    chauffeur: ChauffeurEntity,
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
                        "${chauffeur.prenom} ${chauffeur.nom}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        chauffeur.telephone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ChauffeurStatutBadge(chauffeur.statut)
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
                if (chauffeur.email.isNotBlank()) {
                    InfoPillCh("Email", chauffeur.email)
                }
                InfoPillCh("Permis", chauffeur.numeroPermis)
                InfoPillCh("Embauche", chauffeur.dateEmbauche.toDateString())
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer ce chauffeur ?") },
            text = { Text("${chauffeur.prenom} ${chauffeur.nom} sera supprimé définitivement.") },
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
private fun InfoPillCh(key: String, value: String) {
    Column {
        Text(key, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ChauffeurFormDialog(
    chauffeur: ChauffeurEntity?,
    onConfirm: (ChauffeurEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var nom by remember { mutableStateOf(chauffeur?.nom ?: "") }
    var prenom by remember { mutableStateOf(chauffeur?.prenom ?: "") }
    var telephone by remember { mutableStateOf(chauffeur?.telephone ?: "") }
    var email by remember { mutableStateOf(chauffeur?.email ?: "") }
    var numeroPermis by remember { mutableStateOf(chauffeur?.numeroPermis ?: "") }

    val isValid = nom.isNotBlank() && prenom.isNotBlank() &&
            telephone.isNotBlank() && numeroPermis.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (chauffeur == null) "Ajouter un chauffeur" else "Modifier le chauffeur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prenom, onValueChange = { prenom = it },
                        label = { Text("Prénom *") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = nom, onValueChange = { nom = it },
                        label = { Text("Nom *") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = telephone, onValueChange = { telephone = it },
                    label = { Text("Téléphone *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = numeroPermis, onValueChange = { numeroPermis = it },
                    label = { Text("N° Permis *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        ChauffeurEntity(
                            id = chauffeur?.id ?: 0,
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            telephone = telephone.trim(),
                            email = email.trim(),
                            numeroPermis = numeroPermis.trim(),
                            dateEmbauche = chauffeur?.dateEmbauche ?: System.currentTimeMillis(),
                            statut = chauffeur?.statut ?: ChauffeurStatut.ACTIF
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
private fun ChauffeurStatutDialog(
    current: ChauffeurStatut,
    onConfirm: (ChauffeurStatut) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Changer le statut") },
        text = {
            Column {
                ChauffeurStatut.entries.forEach { statut ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = selected == statut, onClick = { selected = statut })
                        Spacer(Modifier.width(8.dp))
                        ChauffeurStatutBadge(statut)
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

// ── Firestore chauffeur card ──────────────────────────────────────────────────

@Composable
private fun FirestoreChauffeurCard(
    chauffeur: FirestoreUser,
    onAssignTaxi: () -> Unit,
    onUnassign: () -> Unit,
    onClick: () -> Unit = {}
) {
    val (statutLabel, statutColor) = when (chauffeur.statut) {
        FirestoreUser.STATUT_EN_COURSE    -> "En course"    to Color(0xFF2196F3)
        FirestoreUser.STATUT_HORS_SERVICE -> "Hors service" to Color(0xFF9E9E9E)
        else                              -> "En service"   to Color(0xFF4CAF50)
    }

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(chauffeur.nom, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(chauffeur.telephone, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statutColor.copy(alpha = 0.15f),
                    contentColor = statutColor
                ) {
                    Text(
                        statutLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Assigned taxi row
            if (chauffeur.assignedTaxi != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Taxi : ${chauffeur.assignedTaxi}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold)
                    Row {
                        TextButton(onClick = onAssignTaxi, contentPadding = PaddingValues(4.dp)) {
                            Text("Changer", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(
                            onClick = onUnassign,
                            contentPadding = PaddingValues(4.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Retirer", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onAssignTaxi,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Assigner un taxi", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ── Taxi assignment dialog ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignTaxiDialog(
    chauffeur: FirestoreUser,
    taxis: List<TaxiEntity>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMatricule by remember { mutableStateOf(chauffeur.assignedTaxi ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assigner un taxi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chauffeur : ${chauffeur.nom}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (taxis.isEmpty()) {
                    Text("Aucun taxi enregistré. Ajoutez d'abord un taxi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedMatricule.ifBlank { "Choisir un taxi..." },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Taxi") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            taxis.forEach { taxi ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(taxi.immatriculation, fontWeight = FontWeight.SemiBold)
                                            Text("${taxi.marque} ${taxi.modele}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedMatricule = taxi.immatriculation
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (selectedMatricule.isNotBlank()) onConfirm(selectedMatricule) },
                enabled = selectedMatricule.isNotBlank()
            ) { Text("Confirmer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ── Chauffeur detail dialog ───────────────────────────────────────────────────

@Composable
private fun ChauffeurDetailDialog(
    chauffeur: FirestoreUser,
    reservations: List<FirestoreReservation>,
    charges: List<FirestoreCharge>,
    serviceLogs: List<FirestoreServiceLog> = emptyList(),
    onDismiss: () -> Unit
) {
    val dtFmt    = remember { SimpleDateFormat("dd/MM/yy HH:mm", Locale.FRENCH) }
    val dtFmtDay = remember { SimpleDateFormat("dd/MM/yy", Locale.FRENCH) }
    val totalRecettes = reservations
        .filter { it.status == FirestoreReservation.STATUS_TERMINEE }
        .sumOf { it.prixFinal ?: it.prixEstime }
    val totalCharges = charges.sumOf { it.montant }
    val terminee = reservations.filter { it.status == FirestoreReservation.STATUS_TERMINEE }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(chauffeur.nom, style = MaterialTheme.typography.titleLarge)
                Text(chauffeur.telephone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 480.dp)
            ) {
                // ── Financial summary ─────────────────────────────────────────
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Résumé financier",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Recettes :", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(totalRecettes.toMontant(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32))
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Charges :", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("− ${totalCharges.toMontant()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            val benefice = totalRecettes - totalCharges
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bénéfice :", style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    (if (benefice >= 0) "" else "− ") + benefice.toMontant(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (benefice >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            }
                            Text("${terminee.size} course(s) terminée(s) · ${charges.size} charge(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                    }
                }

                // ── Trips ─────────────────────────────────────────────────────
                if (terminee.isNotEmpty()) {
                    item {
                        Text("Courses terminées (${terminee.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    items(terminee.take(10), key = { "trip_${it.id}" }) { res ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(dtFmtDay.format(Date(res.completedAt ?: res.createdAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text((res.prixFinal ?: res.prixEstime).toMontant(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32))
                                }
                                Text("${res.depart} → ${res.arrivee}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (terminee.size > 10) {
                        item {
                            Text("+ ${terminee.size - 10} course(s) supplémentaire(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Charges ───────────────────────────────────────────────────
                if (charges.isNotEmpty()) {
                    item {
                        Text("Charges (${charges.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    items(charges.take(10), key = { "ch_${it.id}" }) { charge ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(charge.description, style = MaterialTheme.typography.bodySmall)
                                    Text(charge.type, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("− ${charge.montant.toMontant()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // ── Service log history ───────────────────────────────────────
                if (serviceLogs.isNotEmpty()) {
                    item {
                        Text("Historique de service (${serviceLogs.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    items(serviceLogs.take(20), key = { "log_${it.id}" }) { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (log.statut) {
                                "en_service"   -> "🟢"
                                "hors_service" -> "🔴"
                                "en_course"    -> "🔵"
                                else           -> "⚪"
                            }
                            Text("$icon ${log.statut.replace('_', ' ')}",
                                style = MaterialTheme.typography.bodySmall)
                            Text(dtFmt.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}
