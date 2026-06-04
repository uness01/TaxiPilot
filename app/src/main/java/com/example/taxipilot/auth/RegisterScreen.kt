package com.example.taxipilot.auth

// Écran d'inscription — permet de créer un nouveau compte Firebase.
// Champs : nom complet, email, mot de passe (toggle visibilité), téléphone.
// Sélection du rôle via des boutons segmentés (Propriétaire / Chauffeur / Client).
// Défilement vertical activé (verticalScroll) pour les petits écrans.
// Le bouton "Créer mon compte" est désactivé si un champ est vide ou si chargement en cours.

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit // callback pour retourner à l'écran de connexion
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CLIENT) } // rôle par défaut
    var passwordVisible by remember { mutableStateOf(false) }

    // Efface l'erreur dès que l'utilisateur modifie un champ
    LaunchedEffect(nom, email, password, telephone) { viewModel.clearError() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // défilement pour petits écrans
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Créer un compte", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Rejoignez TaxiPilot",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // Champ nom complet
        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text("Nom complet") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Champ email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Champ mot de passe avec toggle visibilité
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility
                                      else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Champ téléphone (clavier numérique)
        OutlinedTextField(
            value = telephone,
            onValueChange = { telephone = it },
            label = { Text("Téléphone (ex: 0612345678)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Je suis :",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(8.dp))

        // Sélecteur de rôle — boutons segmentés (choix unique : Propriétaire / Chauffeur / Client)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            UserRole.entries.forEachIndexed { index, role ->
                SegmentedButton(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    shape = SegmentedButtonDefaults.itemShape(index, UserRole.entries.size),
                    label = { Text(role.label()) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Message d'erreur (email déjà utilisé, mot de passe trop court, etc.)
        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }

        // Bouton de création de compte — désactivé si champs vides ou chargement
        Button(
            onClick = {
                viewModel.register(
                    email = email.trim(),
                    password = password,
                    nom = nom.trim(),
                    telephone = telephone.trim(),
                    role = selectedRole
                )
            },
            enabled = nom.isNotBlank() && email.isNotBlank() &&
                      password.isNotBlank() && telephone.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Créer mon compte")
            }
        }

        Spacer(Modifier.height(20.dp))

        HorizontalDivider()

        Spacer(Modifier.height(12.dp))

        // Lien de retour vers l'écran de connexion
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Déjà un compte ?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onNavigateToLogin) {
                Text("Se connecter")
            }
        }
    }
}
