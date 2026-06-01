package com.example.taxipilot.feature.owner

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.taxipilot.core.data.database.entity.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Formatting helpers ────────────────────────────────────────────────────────

fun Long?.toDateString(pattern: String = "dd/MM/yyyy"): String =
    if (this == null) "—"
    else SimpleDateFormat(pattern, Locale.FRENCH).format(Date(this))

fun Double.toMontant(): String = "%.2f MAD".format(this)

// ── Status badges ─────────────────────────────────────────────────────────────

@Composable
fun StatutBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun TaxiStatutBadge(statut: TaxiStatut) = when (statut) {
    TaxiStatut.DISPONIBLE     -> StatutBadge("🟢 Disponible",  Color(0xFF4CAF50))
    TaxiStatut.ASSIGNE        -> StatutBadge("🟡 Assigné",     Color(0xFFFFC107))
    TaxiStatut.EN_COURSE      -> StatutBadge("🔵 En course",   Color(0xFF2196F3))
    TaxiStatut.EN_MAINTENANCE -> StatutBadge("🔧 Maintenance", Color(0xFFFF9800))
}

@Composable
fun ChauffeurStatutBadge(statut: ChauffeurStatut) = when (statut) {
    ChauffeurStatut.ACTIF    -> StatutBadge("Actif",    Color(0xFF4CAF50))
    ChauffeurStatut.INACTIF  -> StatutBadge("Inactif",  Color(0xFF9E9E9E))
    ChauffeurStatut.SUSPENDU -> StatutBadge("Suspendu", Color(0xFFF44336))
}

@Composable
fun TrajetStatutBadge(statut: TrajetStatut) = when (statut) {
    TrajetStatut.EN_ATTENTE -> StatutBadge("En attente", Color(0xFFFF9800))
    TrajetStatut.EN_COURS   -> StatutBadge("En cours",   Color(0xFF2196F3))
    TrajetStatut.TERMINE    -> StatutBadge("Terminé",    Color(0xFF4CAF50))
    TrajetStatut.ANNULE     -> StatutBadge("Annulé",     Color(0xFFF44336))
}

@Composable
fun TypeChargeBadge(type: TypeCharge) = when (type) {
    TypeCharge.DIESEL     -> StatutBadge("Diesel",      Color(0xFF2196F3))
    TypeCharge.REPARATION -> StatutBadge("Réparation",  Color(0xFFFF9800))
    TypeCharge.AUTRE      -> StatutBadge("Autre",       Color(0xFF9E9E9E))
}

// ── Enum label helpers ────────────────────────────────────────────────────────

fun TaxiStatut.label() = when (this) {
    TaxiStatut.DISPONIBLE     -> "Disponible"
    TaxiStatut.ASSIGNE        -> "Assigné"
    TaxiStatut.EN_COURSE      -> "En course"
    TaxiStatut.EN_MAINTENANCE -> "En maintenance"
}

fun ChauffeurStatut.label() = when (this) {
    ChauffeurStatut.ACTIF    -> "Actif"
    ChauffeurStatut.INACTIF  -> "Inactif"
    ChauffeurStatut.SUSPENDU -> "Suspendu"
}

fun TrajetStatut.label() = when (this) {
    TrajetStatut.EN_ATTENTE -> "En attente"
    TrajetStatut.EN_COURS   -> "En cours"
    TrajetStatut.TERMINE    -> "Terminés"
    TrajetStatut.ANNULE     -> "Annulés"
}

fun TypeCharge.label() = when (this) {
    TypeCharge.DIESEL     -> "Diesel"
    TypeCharge.REPARATION -> "Réparation"
    TypeCharge.AUTRE      -> "Autre"
}
