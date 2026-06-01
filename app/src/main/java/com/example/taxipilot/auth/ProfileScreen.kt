package com.example.taxipilot.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.taxipilot.core.data.firestore.FirestoreUser
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    profile: UserProfile,
    onBack: () -> Unit,
    proprietaireInfo: FirestoreUser? = null
) {
    val updateState by viewModel.profileUpdateState.collectAsState()

    var nom        by remember(profile.nom) { mutableStateOf(profile.nom) }
    var telephone  by remember(profile.telephone) { mutableStateOf(profile.telephone) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPasswordSection by remember { mutableStateOf(false) }
    var snackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(updateState) {
        when (val s = updateState) {
            is AuthViewModel.ProfileUpdateState.Success -> {
                snackMessage = "Profil mis à jour avec succès"
                newPassword = ""
                confirmPassword = ""
                viewModel.clearProfileUpdateState()
            }
            is AuthViewModel.ProfileUpdateState.Error -> {
                snackMessage = s.message
                viewModel.clearProfileUpdateState()
            }
            else -> Unit
        }
    }

    val profileValid = nom.isNotBlank() && telephone.isNotBlank()
    val passwordsMatch = newPassword == confirmPassword
    val passwordValid = newPassword.length >= 6

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            snackMessage?.let { msg ->
                Snackbar(
                    action = { TextButton(onClick = { snackMessage = null }) { Text("OK") } },
                    modifier = Modifier.padding(8.dp)
                ) { Text(msg) }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Role badge ────────────────────────────────────────────────────
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    profile.role.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // ── Basic info ────────────────────────────────────────────────────
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Informations personnelles",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom complet *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telephone,
                        onValueChange = { telephone = it },
                        label = { Text("Téléphone *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = profile.email,
                        onValueChange = {},
                        label = { Text("Email (non modifiable)") },
                        singleLine = true,
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.updateProfile(nom, telephone) },
                        enabled = profileValid && updateState !is AuthViewModel.ProfileUpdateState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (updateState is AuthViewModel.ProfileUpdateState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Enregistrer les modifications")
                        }
                    }
                }
            }

            // ── Role-specific info (read-only) ────────────────────────────────
            when (profile.role) {
                UserRole.PROPRIETAIRE -> {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Informations Propriétaire",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Code de partage :")
                                Text(
                                    profile.codeProprietaire ?: "—",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                "Partagez ce code avec vos chauffeurs pour qu'ils puissent vous rejoindre.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                UserRole.CHAUFFEUR -> {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Informations Chauffeur",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxi assigné :", style = MaterialTheme.typography.bodySmall)
                                Text(profile.assignedTaxi ?: "Non assigné",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (profile.assignedTaxi != null) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Statut :", style = MaterialTheme.typography.bodySmall)
                                Text(profile.statut,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // ── Mon Employeur ─────────────────────────────────────────
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Mon Employeur",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            if (profile.proprietaireId == null) {
                                Text(
                                    "Non lié à un propriétaire",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (proprietaireInfo == null) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("👤 Nom :", style = MaterialTheme.typography.bodySmall)
                                    Text(proprietaireInfo.nom.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("📞 Téléphone :", style = MaterialTheme.typography.bodySmall)
                                    Text(proprietaireInfo.telephone.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("🔑 Code utilisé :", style = MaterialTheme.typography.bodySmall)
                                    Text(proprietaireInfo.codeProprietaire ?: "—",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                UserRole.CLIENT -> Unit
            }

            // ── Change password ───────────────────────────────────────────────
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Changer le mot de passe",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { showPasswordSection = !showPasswordSection }) {
                            Text(if (showPasswordSection) "Annuler" else "Modifier")
                        }
                    }
                    if (showPasswordSection) {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Nouveau mot de passe *") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            isError = newPassword.isNotBlank() && !passwordValid,
                            supportingText = if (newPassword.isNotBlank() && !passwordValid) {
                                { Text("6 caractères minimum") }
                            } else null
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirmer le mot de passe *") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            isError = confirmPassword.isNotBlank() && !passwordsMatch,
                            supportingText = if (confirmPassword.isNotBlank() && !passwordsMatch) {
                                { Text("Les mots de passe ne correspondent pas") }
                            } else null
                        )
                        Button(
                            onClick = { viewModel.updatePassword(newPassword) },
                            enabled = passwordValid && passwordsMatch &&
                                    updateState !is AuthViewModel.ProfileUpdateState.Loading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Mettre à jour le mot de passe")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
