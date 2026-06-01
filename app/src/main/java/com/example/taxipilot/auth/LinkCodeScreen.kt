package com.example.taxipilot.auth

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

/**
 * Shown to a newly registered CHAUFFEUR who has not yet linked to a Propriétaire.
 * The chauffeur enters the 6-digit code their employer gave them.
 */
@Composable
fun LinkCodeScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val linkState by viewModel.linkState.collectAsState()
    var code by remember { mutableStateOf("") }

    val isLoading = linkState is AuthViewModel.LinkState.Loading
    val errorMsg  = (linkState as? AuthViewModel.LinkState.Error)?.message

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

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
            label = { Text("Code Propriétaire") },
            placeholder = { Text("ex: 482910") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = errorMsg != null,
            modifier = Modifier.fillMaxWidth()
        )

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

        TextButton(onClick = { viewModel.signOut() }) {
            Text(
                "Se déconnecter",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
