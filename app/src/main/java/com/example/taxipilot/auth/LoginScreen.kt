package com.example.taxipilot.auth

// Écran de connexion — premier écran affiché aux utilisateurs non connectés.
// Champs : email + mot de passe (avec toggle visibilité).
// Le bouton "Se connecter" est désactivé si les champs sont vides ou si un chargement est en cours.
// Les erreurs (mauvais mot de passe, email invalide, etc.) s'affichent en rouge sous le formulaire.
// LaunchedEffect(email, password) : efface l'erreur dès que l'utilisateur recommence à taper.

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit // callback pour aller sur l'écran d'inscription
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // toggle affichage mot de passe

    // Efface l'erreur automatiquement dès que l'utilisateur modifie email ou mot de passe
    LaunchedEffect(email, password) { viewModel.clearError() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Titre et sous-titre de l'app
        Text(
            text = "TaxiPilot",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Gérez votre flotte, où que vous soyez",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

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

        // Champ mot de passe avec bouton œil pour afficher/masquer
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
                        contentDescription = if (passwordVisible) "Masquer" else "Afficher"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Message d'erreur (mauvais mot de passe, email invalide, etc.)
        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Bouton de connexion — désactivé si champs vides ou chargement en cours
        Button(
            onClick = { viewModel.signIn(email.trim(), password) },
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
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
                Text("Se connecter")
            }
        }

        Spacer(Modifier.height(24.dp))

        HorizontalDivider()

        Spacer(Modifier.height(16.dp))

        // Lien vers l'écran d'inscription pour les nouveaux utilisateurs
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Pas encore de compte ?",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onNavigateToRegister) {
                Text("S'inscrire")
            }
        }
    }
}
