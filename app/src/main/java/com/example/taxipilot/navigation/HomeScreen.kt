package com.example.taxipilot.navigation

// Écran d'accueil de sélection de l'espace — non utilisé dans le flux Firebase actuel
// (TaxiPilotNavHost redirige directement vers l'espace du rôle de l'utilisateur connecté).
// Conservé comme écran de fallback ou pour des tests sans authentification.
// Affiche trois boutons permettant de choisir manuellement Propriétaire / Chauffeur / Client.

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToOwner: () -> Unit,  // navigue vers l'espace propriétaire
    onNavigateToDriver: () -> Unit, // navigue vers l'espace chauffeur
    onNavigateToClient: () -> Unit  // navigue vers l'espace client
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TaxiPilot",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Choisissez votre espace",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(48.dp))

            // Boutons de navigation vers chaque espace
            SpaceButton(label = "Propriétaire", onClick = onNavigateToOwner)
            Spacer(Modifier.height(16.dp))
            SpaceButton(label = "Chauffeur", onClick = onNavigateToDriver)
            Spacer(Modifier.height(16.dp))
            SpaceButton(label = "Client", onClick = onNavigateToClient)
        }
    }
}

// Bouton pleine largeur pour la sélection de l'espace utilisateur
@Composable
private fun SpaceButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
