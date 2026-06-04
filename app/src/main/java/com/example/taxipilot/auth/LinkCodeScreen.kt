package com.example.taxipilot.auth

// Écran de liage Chauffeur ↔ Propriétaire.
// Affiché UNIQUEMENT quand un chauffeur vient de s'inscrire et n'a pas encore de proprietaireId.
// Le chauffeur saisit le code à 6 chiffres que son propriétaire lui a communiqué.
// Si le code est valide → proprietaireId est écrit dans Firestore → l'app passe en mode Authenticated.
// Si le code est invalide → message d'erreur affiché, possibilité de réessayer.
// Bouton "Se déconnecter" disponible si le chauffeur s'est trompé de compte.

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LinkCodeScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val linkState by viewModel.linkState.collectAsState()
    var code by remember { mutableStateOf("") }

    val isLoading = linkState is AuthViewModel.LinkState.Loading
    val errorMsg  = (linkState as? AuthViewModel.LinkState.Error)?.message

    // Efface l'erreur dès que l'utilisateur modifie le code
    LaunchedEffect(code) { if (code.isNotBlank()) viewModel.clearLinkState() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🚕", style = MaterialTheme.typography.displayMedium)

        Spacer(Modifier.height(24.dp))

        Text(
            "Liez-vous à votre Propriétaire",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Votre Propriétaire vous a donné un code à 6 chiffres.\nEntrez-le pour accéder à l'application.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Champ de saisie du code — accepte seulement 6 chiffres, pas d'autres caractères
        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
            label = { Text("Code Propriétaire") },
            placeholder = { Text("ex: 482910") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = errorMsg != null, // rouge si code invalide
            modifier = Modifier.fillMaxWidth()
        )

        // Message d'erreur (code invalide ou erreur réseau)
        if (errorMsg != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        // Bouton Confirmer — actif uniquement quand le code a exactement 6 chiffres
        Button(
            onClick = { viewModel.linkToProprietaire(code) },
            enabled = code.length == 6 && !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Confirmer", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Bouton de déconnexion — si le chauffeur s'est trompé de compte
        TextButton(onClick = { viewModel.signOut() }) {
            Text(
                "Se déconnecter",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
