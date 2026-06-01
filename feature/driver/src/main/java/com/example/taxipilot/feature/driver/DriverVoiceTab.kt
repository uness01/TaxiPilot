package com.example.taxipilot.feature.driver

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.taxipilot.core.data.database.entity.TypeCharge

private enum class InputMode { VOCAL, MANUEL }
private enum class ManuelTab { TRAJET, CHARGE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverVoiceTab(
    viewModel: DriverViewModel,
    voiceEngine: VoiceEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceUiState by viewModel.voiceUiState.collectAsState()
    val listenState by voiceEngine.listenState.collectAsState()
    val partialText by voiceEngine.partialText.collectAsState()
    val allTaxis by viewModel.allTaxis.collectAsState()
    val selectedTaxiId by viewModel.selectedTaxiId.collectAsState()
    val assignedTaxi = viewModel.assignedTaxi
    val manualSaveState by viewModel.manualSaveState.collectAsState()

    var inputMode by remember { mutableStateOf(InputMode.VOCAL) }

    // TTS: speak saved/error messages automatically
    LaunchedEffect(voiceUiState) {
        when (val s = voiceUiState) {
            is VoiceUiState.Saved  -> voiceEngine.speak(s.message) { viewModel.dismissVoiceState() }
            is VoiceUiState.Error  -> voiceEngine.speak(s.message)
            is VoiceUiState.PendingConfirmation ->
                voiceEngine.speak(VoiceParser.describeCommand(s.command))
            else -> Unit
        }
    }

    // Runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening(viewModel, voiceEngine)
        else viewModel.onVoiceError("Permission microphone refusée.")
    }

    // Mic pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )
    val isListening = listenState == VoiceEngine.ListenState.LISTENING

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        // ── Assigned taxi info (read-only) ────────────────────────────────────
        val displayTaxi = allTaxis.find { it.id == selectedTaxiId }
        AssignedTaxiCard(
            matricule = assignedTaxi,
            displayTaxi = displayTaxi
        )

        Spacer(Modifier.height(16.dp))

        // ── Mode toggle ───────────────────────────────────────────────────────
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = inputMode == InputMode.VOCAL,
                onClick = { inputMode = InputMode.VOCAL },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("🎤  Vocal") }
            SegmentedButton(
                selected = inputMode == InputMode.MANUEL,
                onClick = { inputMode = InputMode.MANUEL },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("✏️  Manuel") }
        }

        Spacer(Modifier.height(20.dp))

        when (inputMode) {
            InputMode.VOCAL -> {
                // ── Mic button ────────────────────────────────────────────────
                val micEnabled = listenState == VoiceEngine.ListenState.IDLE &&
                        voiceUiState !is VoiceUiState.PendingConfirmation

                Box(contentAlignment = Alignment.Center) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .scale(pulseScale)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            if (micEnabled) {
                                if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    startListening(viewModel, voiceEngine)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else if (isListening) {
                                voiceEngine.stopListening()
                            }
                        },
                        modifier = Modifier.size(120.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = when {
                                isListening -> MaterialTheme.colorScheme.error
                                else        -> MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Text(
                            text = when (listenState) {
                                VoiceEngine.ListenState.IDLE       -> "🎤"
                                VoiceEngine.ListenState.LISTENING  -> "⏹"
                                VoiceEngine.ListenState.PROCESSING -> "⏳"
                            },
                            fontSize = 44.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = when (listenState) {
                        VoiceEngine.ListenState.IDLE       -> if (micEnabled) "Appuyer pour parler" else "En attente…"
                        VoiceEngine.ListenState.LISTENING  -> "En écoute… (appuyer pour arrêter)"
                        VoiceEngine.ListenState.PROCESSING -> "Traitement…"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (partialText.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\"$partialText\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Voice state feedback
                when (val state = voiceUiState) {
                    is VoiceUiState.PendingConfirmation -> {
                        ConfirmationCard(
                            command = state.command,
                            onConfirm = { viewModel.confirmPending(state.command) },
                            onCancel  = { viewModel.dismissVoiceState() }
                        )
                    }
                    is VoiceUiState.Saved -> {
                        FeedbackCard(
                            message = state.message,
                            isSuccess = true,
                            onDismiss = viewModel::dismissVoiceState
                        )
                    }
                    is VoiceUiState.Error -> {
                        FeedbackCard(
                            message = state.message,
                            isSuccess = false,
                            onDismiss = viewModel::dismissVoiceState
                        )
                    }
                    VoiceUiState.Idle -> Unit
                }

                Spacer(Modifier.height(24.dp))

                VoiceGuide()
            }

            InputMode.MANUEL -> {
                ManuelForm(
                    viewModel = viewModel,
                    saveState = manualSaveState
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Manuel form ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManuelForm(
    viewModel: DriverViewModel,
    saveState: DriverViewModel.ManualSaveState
) {
    var activeTab by remember { mutableStateOf(ManuelTab.TRAJET) }

    // Trajet fields
    var depart      by remember { mutableStateOf("") }
    var arrivee     by remember { mutableStateOf("") }
    var distanceStr by remember { mutableStateOf("") }
    var montantStr  by remember { mutableStateOf("") }

    // Charge fields
    var chargeType    by remember { mutableStateOf(TypeCharge.DIESEL) }
    var chargeDesc    by remember { mutableStateOf("") }
    var chargeMontant by remember { mutableStateOf("") }
    var typeExpanded  by remember { mutableStateOf(false) }

    // Auto-clear form on success
    LaunchedEffect(saveState) {
        if (saveState is DriverViewModel.ManualSaveState.Success) {
            if (activeTab == ManuelTab.TRAJET) {
                depart = ""; arrivee = ""; distanceStr = ""; montantStr = ""
            } else {
                chargeDesc = ""; chargeMontant = ""
            }
        }
    }

    TabRow(selectedTabIndex = activeTab.ordinal) {
        Tab(
            selected = activeTab == ManuelTab.TRAJET,
            onClick = { activeTab = ManuelTab.TRAJET; viewModel.clearManualSaveState() },
            text = { Text("Course") }
        )
        Tab(
            selected = activeTab == ManuelTab.CHARGE,
            onClick = { activeTab = ManuelTab.CHARGE; viewModel.clearManualSaveState() },
            text = { Text("Charge") }
        )
    }

    Spacer(Modifier.height(16.dp))

    when (activeTab) {
        ManuelTab.TRAJET -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = depart,
                    onValueChange = { depart = it },
                    label = { Text("Adresse de départ") },
                    placeholder = { Text("ex: Rabat") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = arrivee,
                    onValueChange = { arrivee = it },
                    label = { Text("Adresse d'arrivée") },
                    placeholder = { Text("ex: Casablanca") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = distanceStr,
                        onValueChange = { distanceStr = it },
                        label = { Text("Distance (km)") },
                        placeholder = { Text("optionnel") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text("km") }
                    )
                    OutlinedTextField(
                        value = montantStr,
                        onValueChange = { montantStr = it },
                        label = { Text("Montant (MAD)") },
                        placeholder = { Text("ex: 350") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text("MAD") }
                    )
                }
                Button(
                    onClick = {
                        viewModel.saveTrajetManuel(
                            depart   = depart.trim().ifBlank { "Départ" },
                            arrivee  = arrivee.trim().ifBlank { "Arrivée" },
                            distanceKm = distanceStr.toDoubleOrNull(),
                            montant    = montantStr.toDoubleOrNull()
                        )
                    },
                    enabled = depart.isNotBlank() && arrivee.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Enregistrer la course") }
            }
        }

        ManuelTab.CHARGE -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = chargeType.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type de charge") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        TypeCharge.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label()) },
                                onClick = { chargeType = type; typeExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = chargeDesc,
                    onValueChange = { chargeDesc = it },
                    label = { Text("Description") },
                    placeholder = { Text("ex: Plein diesel") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = chargeMontant,
                    onValueChange = { chargeMontant = it },
                    label = { Text("Montant (MAD)") },
                    placeholder = { Text("ex: 500") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("MAD") }
                )
                Button(
                    onClick = {
                        viewModel.saveChargeManuel(
                            type        = chargeType,
                            description = chargeDesc.trim().ifBlank { chargeType.label() },
                            montant     = chargeMontant.toDoubleOrNull() ?: 0.0
                        )
                    },
                    enabled = chargeMontant.toDoubleOrNull() != null,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Enregistrer la charge") }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Save state feedback
    when (saveState) {
        is DriverViewModel.ManualSaveState.Success -> {
            FeedbackCard(
                message = saveState.message,
                isSuccess = true,
                onDismiss = viewModel::clearManualSaveState
            )
        }
        is DriverViewModel.ManualSaveState.Error -> {
            FeedbackCard(
                message = saveState.message,
                isSuccess = false,
                onDismiss = viewModel::clearManualSaveState
            )
        }
        DriverViewModel.ManualSaveState.Idle -> Unit
    }
}

private fun TypeCharge.label() = when (this) {
    TypeCharge.DIESEL     -> "Diesel / Carburant"
    TypeCharge.REPARATION -> "Réparation / Entretien"
    TypeCharge.AUTRE      -> "Autre charge"
}

// ── Assigned taxi info ────────────────────────────────────────────────────────

@Composable
private fun AssignedTaxiCard(
    matricule: String?,
    displayTaxi: com.example.taxipilot.core.data.database.entity.TaxiEntity?
) {
    val text = when {
        displayTaxi != null -> "${displayTaxi.marque} ${displayTaxi.modele} · ${displayTaxi.immatriculation}"
        matricule != null   -> matricule
        else                -> "Aucun véhicule assigné"
    }
    OutlinedTextField(
        value = text,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text("Véhicule assigné") },
        modifier = Modifier.fillMaxWidth()
    )
}

// ── Confirmation card ─────────────────────────────────────────────────────────

@Composable
private fun ConfirmationCard(
    command: VoiceParser.VoiceCommand,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Confirmer cette commande ?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                VoiceParser.describeCommand(command).removeSuffix(" Confirmer ?"),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "(Dites « oui » ou « non »)",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Annuler")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onConfirm) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Confirmer")
                }
            }
        }
    }
}

// ── Feedback card ─────────────────────────────────────────────────────────────

@Composable
internal fun FeedbackCard(message: String, isSuccess: Boolean, onDismiss: () -> Unit) {
    val containerColor = if (isSuccess)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.errorContainer
    val contentColor = if (isSuccess)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSuccess) "✓" else "✗",
                fontSize = 20.sp,
                color = contentColor,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Fermer",
                    tint = contentColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Voice guide ───────────────────────────────────────────────────────────────

@Composable
private fun VoiceGuide() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Exemples de commandes vocales",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            GuideRow("🚕", "Trajet", "« trajet de Rabat à Casablanca trois cent cinquante dirhams »")
            GuideRow("🚕", "Trajet", "« de Kénitra à Rabat pour 120 dirhams »")
            GuideRow("🚕", "Trajet", "« course de Marrakech à Agadir 95 km 450 DH »")
            GuideRow("⛽", "Diesel", "« charge diesel deux cents dirhams »")
            GuideRow("🔧", "Réparation", "« réparation freins 800 dirhams »")
            GuideRow("🧽", "Lavage", "« lavage 80 DH »")
            GuideRow("✅", "Confirmer", "« oui » ou « confirmer »")
            GuideRow("❌", "Annuler", "« non » ou « annuler »")
        }
    }
}

@Composable
private fun GuideRow(emoji: String, label: String, example: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 16.sp, modifier = Modifier.width(28.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(example, style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic)
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun startListening(viewModel: DriverViewModel, engine: VoiceEngine) {
    engine.startListening(
        onResult = viewModel::onVoiceResult,
        onError  = viewModel::onVoiceError
    )
}
