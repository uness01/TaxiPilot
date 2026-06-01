package com.example.taxipilot.feature.driver

import com.example.taxipilot.core.data.database.entity.TypeCharge

/**
 * Parses French/Moroccan-Arabic voice commands into structured [VoiceCommand] objects.
 *
 * Supported commands (French + Darija numbers, currency = dirhams/MAD):
 *   Trajet : "trajet de [origin] à [destination]" + optional "pour [amount] dirhams"
 *   Charge : "diesel [amount]" | "réparation [desc] [amount]" | "charge [desc] [amount]"
 *   Confirm: "oui" | "confirmer" | "ok"
 *   Cancel : "non" | "annuler"
 *
 * Currency aliases accepted: dirhams / dirham / DH / dh / درهم / MAD / mad
 * Darija numbers: miyya=100, miyatayn=200, telt miyya=300,ربع مية=400, خمس مية=500
 */
object VoiceParser {

    sealed class VoiceCommand {
        data class NouveauTrajet(
            val depart: String,
            val arrivee: String,
            val montant: Double?
        ) : VoiceCommand()

        data class NouvelleCharge(
            val type: TypeCharge,
            val description: String,
            val montant: Double?
        ) : VoiceCommand()

        data class Reponse(val confirmee: Boolean) : VoiceCommand()

        object Incompris : VoiceCommand()
    }

    // ── Public API ───────────────────────────────────────────────────────────

    fun parse(raw: String): VoiceCommand {
        val t = raw.lowercase().trim()
            .replace("é", "e").replace("è", "e").replace("ê", "e")
            .replace("à", "a").replace("â", "a")
            .replace("î", "i").replace("ô", "o").replace("û", "u")
            .replace("ç", "c")
        val tOriginal = raw.lowercase().trim() // keep accents for display

        // Confirmation / refusal (check before trip to avoid "ok" in addresses)
        if (isConfirmation(t)) return VoiceCommand.Reponse(true)
        if (isRefusal(t)) return VoiceCommand.Reponse(false)

        // Trip
        if (isTrajet(t)) return parseTrajet(tOriginal)

        // Expense
        return parseCharge(t, tOriginal) ?: VoiceCommand.Incompris
    }

    /** Build a TTS confirmation string for a pending command. */
    fun describeCommand(cmd: VoiceCommand): String = when (cmd) {
        is VoiceCommand.NouveauTrajet -> buildString {
            append("Trajet de ${cmd.depart} à ${cmd.arrivee}")
            cmd.montant?.let { append(", ${it.toLong()} dirhams") }
            append(". Confirmer ?")
        }
        is VoiceCommand.NouvelleCharge -> buildString {
            append("Charge ${cmd.type.ttsLabel()}, ${cmd.description}")
            cmd.montant?.let { append(", ${it.toLong()} dirhams") }
            append(". Confirmer ?")
        }
        else -> ""
    }

    // ── Detection helpers ────────────────────────────────────────────────────

    private fun isConfirmation(t: String) =
        t.matches(Regex("(oui|confirme[r]?|ok|d accord|enregistre[r]?|c est bon|parfait|valider?)"))
            || t.startsWith("oui") || t == "ok"

    private fun isRefusal(t: String) =
        t.matches(Regex("(non|annule[r]?|cancel|stop|pas ca|erreur|faux)"))
            || t.startsWith("non")

    private fun isTrajet(t: String) =
        t.contains("trajet") || t.contains("course") || t.contains("prise en charge") ||
                (t.contains("de ") && t.contains(" a "))

    // ── Trajet parsing ───────────────────────────────────────────────────────

    private fun parseTrajet(raw: String): VoiceCommand.NouveauTrajet {
        val t = raw.lowercase()
        // Pattern: (trajet|course)? de <origin> à <dest> (pour <montant>)?
        val currency = """(?:mad|dirham[s]?|dh|dinar[s]?|da|درهم)?"""
        val fullRegex = Regex(
            """(?:trajet|course|de)\s+de\s+(.+?)\s+[aà]\s+(.+?)(?:\s+pour\s+([\d.,]+)\s*$currency)?$"""
        )
        val simpleRegex = Regex(
            """de\s+(.+?)\s+[aà]\s+(.+?)(?:\s+pour\s+([\d.,]+)\s*$currency)?$"""
        )

        // Tolerates "X km" between destination and price
        val kmRegex = Regex(
            """(?:trajet|course)?\s*de\s+(.+?)\s+[aà]\s+(.+?)\s+\d+\s*km[s]?\s*(?:pour\s+)?([\d.,]+)\s*(?:mad|dirham[s]?|dh|درهم|dinar[s]?|da)?$"""
        )
        val match = fullRegex.find(t) ?: kmRegex.find(t) ?: simpleRegex.find(t)
        if (match != null) {
            val depart = match.groupValues[1].trim().capitalizeWords()
            val arrivee = match.groupValues[2]
                .replace(Regex("""\s*pour\s+[\d.,]+.*"""), "").trim().capitalizeWords()
            val montant = match.groupValues.getOrNull(3)?.replace(",", ".")?.toDoubleOrNull()
                ?: extractMontant(t)
            return VoiceCommand.NouveauTrajet(depart.ifBlank { "Départ" }, arrivee.ifBlank { "Arrivée" }, montant)
        }

        // Fallback: just extract a number as montant, no addresses parsed
        return VoiceCommand.NouveauTrajet("Départ non précisé", "Arrivée non précisée", extractMontant(t))
    }

    // ── Charge parsing ───────────────────────────────────────────────────────

    private fun parseCharge(t: String, raw: String): VoiceCommand.NouvelleCharge? {
        val montant = extractMontant(t)

        if (t.contains("diesel") || t.contains("carburant") || t.contains("essence")) {
            return VoiceCommand.NouvelleCharge(TypeCharge.DIESEL, "Plein diesel", montant)
        }
        if (t.contains("reparation") || t.contains("revision") || t.contains("mecanique") ||
            t.contains("pneu") || t.contains("frein") || t.contains("vidange") ||
            t.contains("huile") || t.contains("batterie")
        ) {
            val desc = extractDescription(t, setOf("reparation", "revision", "pour", "le", "la", "les"))
                .ifBlank { "Réparation" }.capitalizeWords()
            return VoiceCommand.NouvelleCharge(TypeCharge.REPARATION, desc, montant)
        }
        if (t.contains("charge") || t.contains("depense") || t.contains("frais") ||
            t.contains("achat") || t.contains("lavage") || t.contains("peage")
        ) {
            val desc = extractDescription(t, setOf("charge", "depense", "frais", "pour", "le", "la"))
                .ifBlank { "Charge diverse" }.capitalizeWords()
            return VoiceCommand.NouvelleCharge(TypeCharge.AUTRE, desc, montant)
        }
        return null
    }

    // ── Low-level extractors ─────────────────────────────────────────────────

    private fun extractMontant(t: String): Double? {
        // French number words (common taxi amounts)
        val wordNumbers = mapOf(
            "cent" to 100.0, "deux cents" to 200.0, "trois cents" to 300.0,
            "quatre cents" to 400.0, "cinq cents" to 500.0, "mille" to 1000.0,
            "deux mille" to 2000.0, "trois mille" to 3000.0, "cinq mille" to 5000.0,
            // Moroccan Darija number words
            "miyatayn" to 200.0, "miyatain" to 200.0,   // 200 (2 variants)
            "telt miyya" to 300.0, "tlet miyya" to 300.0, // 300
            "reb miyya" to 400.0, "reb3 miyya" to 400.0,  // 400
            "khemsmiyya" to 500.0, "khems miyya" to 500.0, // 500
            "miyya" to 100.0                               // 100 (last — lowest priority)
        )
        for ((word, value) in wordNumbers) {
            if (t.contains(word)) return value
        }
        // Digit forms: 500, 1500, 2.500, 2,500
        val regex = Regex("""(\d[\d\s]*(?:[.,]\d+)?)\s*(?:mad|dirham[s]?|dh|درهم|dinar[s]?|da|euro[s]?|€)?""")
        return regex.find(t)?.groupValues?.get(1)
            ?.replace(" ", "")?.replace(",", ".")?.toDoubleOrNull()
    }

    private fun extractDescription(t: String, stopWords: Set<String>): String =
        t.replace(Regex("""\d+(?:[.,]\d+)?\s*(?:mad|dirham[s]?|dh|درهم|dinar[s]?|da)?"""), "")
            .split(" ")
            .filter { it.length > 2 && it !in stopWords }
            .joinToString(" ")
            .trim()
            .take(60)

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }

    private fun TypeCharge.ttsLabel() = when (this) {
        TypeCharge.DIESEL     -> "diesel"
        TypeCharge.REPARATION -> "réparation"
        TypeCharge.AUTRE      -> "autre"
    }
}
