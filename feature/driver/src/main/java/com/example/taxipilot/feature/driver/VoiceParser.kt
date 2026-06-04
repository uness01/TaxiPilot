package com.example.taxipilot.feature.driver

// Parser de commandes vocales pour le chauffeur.
// Convertit du texte brut (français + darija marocaine) en VoiceCommand structuré.
//
// Commandes supportées :
//   - Trajet  : "trajet de [départ] à [destination]" + optionnel "pour [montant] dirhams"
//   - Charge  : "diesel [montant]" | "réparation [desc] [montant]" | "charge [desc] [montant]"
//   - Confirmer: "oui" / "confirmer" / "ok" / "d accord"
//   - Annuler  : "non" / "annuler" / "stop"
//
// Devises acceptées : dirhams / dirham / DH / dh / درهم / MAD / mad / dinar / DA / €
// Nombres en français : cent, deux cents, trois cents, mille, deux mille...
// Nombres en darija : miyya=100, miyatayn=200, telt miyya=300, reb miyya=400, khems miyya=500
//
// parse() normalise d'abord les accents et cédilles pour simplifier les regex,
// mais conserve l'original (tOriginal) pour l'affichage dans l'UI.

import com.example.taxipilot.core.data.database.entity.TypeCharge

object VoiceParser {

    // Types de commandes vocales reconnaissables
    sealed class VoiceCommand {
        data class NouveauTrajet(
            val depart: String,   // adresse de départ (capitalisée)
            val arrivee: String,  // adresse d'arrivée (capitalisée)
            val montant: Double?  // montant en MAD (null si non précisé)
        ) : VoiceCommand()

        data class NouvelleCharge(
            val type: TypeCharge,    // DIESEL, REPARATION ou AUTRE
            val description: String, // description de la charge
            val montant: Double?     // montant en MAD (null si non précisé)
        ) : VoiceCommand()

        data class Reponse(val confirmee: Boolean) : VoiceCommand() // oui=true, non=false

        object Incompris : VoiceCommand() // aucune commande reconnue
    }

    // ── API publique ──────────────────────────────────────────────────────────

    // Analyse le texte brut et retourne une VoiceCommand structurée
    fun parse(raw: String): VoiceCommand {
        // Normalise les accents pour simplifier les regex (garde l'original pour l'affichage)
        val t = raw.lowercase().trim()
            .replace("é", "e").replace("è", "e").replace("ê", "e")
            .replace("à", "a").replace("â", "a")
            .replace("î", "i").replace("ô", "o").replace("û", "u")
            .replace("ç", "c")
        val tOriginal = raw.lowercase().trim()

        // Confirmation / refus détectés en premier (avant trajet, pour éviter "ok" dans les adresses)
        if (isConfirmation(t)) return VoiceCommand.Reponse(true)
        if (isRefusal(t)) return VoiceCommand.Reponse(false)

        // Trajet : "de X à Y" avec ou sans montant
        if (isTrajet(t)) return parseTrajet(tOriginal)

        // Charge : diesel / réparation / autre
        return parseCharge(t, tOriginal) ?: VoiceCommand.Incompris
    }

    // Construit le texte TTS de confirmation pour une commande en attente
    // Exemple : "Trajet de Casablanca à Rabat, 150 dirhams. Confirmer ?"
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

    // ── Détection ─────────────────────────────────────────────────────────────

    // Mots de confirmation (oui / ok / confirmer / d accord / enregistrer / parfait / valider)
    private fun isConfirmation(t: String) =
        t.matches(Regex("(oui|confirme[r]?|ok|d accord|enregistre[r]?|c est bon|parfait|valider?)"))
            || t.startsWith("oui") || t == "ok"

    // Mots de refus (non / annuler / stop / erreur / faux)
    private fun isRefusal(t: String) =
        t.matches(Regex("(non|annule[r]?|cancel|stop|pas ca|erreur|faux)"))
            || t.startsWith("non")

    // Détecte un trajet : "trajet", "course", "prise en charge", ou "de X à Y"
    private fun isTrajet(t: String) =
        t.contains("trajet") || t.contains("course") || t.contains("prise en charge") ||
                (t.contains("de ") && t.contains(" a "))

    // ── Parsing de trajets ────────────────────────────────────────────────────

    // Extrait départ, arrivée et montant depuis une commande de trajet
    // Supporte : "trajet de X à Y pour Z MAD", "de X à Y", "X km" entre destination et prix
    private fun parseTrajet(raw: String): VoiceCommand.NouveauTrajet {
        val t = raw.lowercase()
        val currency = """(?:mad|dirham[s]?|dh|dinar[s]?|da|درهم)?"""
        // Regex principale : (trajet|course|de) de <départ> à <arrivée> (pour <montant>)?
        val fullRegex = Regex(
            """(?:trajet|course|de)\s+de\s+(.+?)\s+[aà]\s+(.+?)(?:\s+pour\s+([\d.,]+)\s*$currency)?$"""
        )
        val simpleRegex = Regex(
            """de\s+(.+?)\s+[aà]\s+(.+?)(?:\s+pour\s+([\d.,]+)\s*$currency)?$"""
        )
        // Tolère "X km" entre destination et prix (ex : "de Rabat à Casablanca 200 km pour 350 MAD")
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

        // Fallback : aucune adresse reconnue — retourne juste le montant si trouvé
        return VoiceCommand.NouveauTrajet("Départ non précisé", "Arrivée non précisée", extractMontant(t))
    }

    // ── Parsing de charges ────────────────────────────────────────────────────

    // Extrait le type, la description et le montant d'une commande de dépense
    private fun parseCharge(t: String, raw: String): VoiceCommand.NouvelleCharge? {
        val montant = extractMontant(t)

        // Carburant : diesel / carburant / essence
        if (t.contains("diesel") || t.contains("carburant") || t.contains("essence")) {
            return VoiceCommand.NouvelleCharge(TypeCharge.DIESEL, "Plein diesel", montant)
        }
        // Réparation : réparation / révision / mécanique / pneu / frein / vidange / huile / batterie
        if (t.contains("reparation") || t.contains("revision") || t.contains("mecanique") ||
            t.contains("pneu") || t.contains("frein") || t.contains("vidange") ||
            t.contains("huile") || t.contains("batterie")
        ) {
            val desc = extractDescription(t, setOf("reparation", "revision", "pour", "le", "la", "les"))
                .ifBlank { "Réparation" }.capitalizeWords()
            return VoiceCommand.NouvelleCharge(TypeCharge.REPARATION, desc, montant)
        }
        // Autre : charge / dépense / frais / achat / lavage / péage
        if (t.contains("charge") || t.contains("depense") || t.contains("frais") ||
            t.contains("achat") || t.contains("lavage") || t.contains("peage")
        ) {
            val desc = extractDescription(t, setOf("charge", "depense", "frais", "pour", "le", "la"))
                .ifBlank { "Charge diverse" }.capitalizeWords()
            return VoiceCommand.NouvelleCharge(TypeCharge.AUTRE, desc, montant)
        }
        return null // aucune charge reconnue
    }

    // ── Extracteurs de bas niveau ─────────────────────────────────────────────

    // Extrait un montant numérique depuis du texte (français + darija)
    // Reconnaît les mots (cent, mille, miyya...) et les chiffres (500, 1500, 2.500)
    private fun extractMontant(t: String): Double? {
        // Mots numériques français et darija marocaine
        val wordNumbers = mapOf(
            "cent" to 100.0, "deux cents" to 200.0, "trois cents" to 300.0,
            "quatre cents" to 400.0, "cinq cents" to 500.0, "mille" to 1000.0,
            "deux mille" to 2000.0, "trois mille" to 3000.0, "cinq mille" to 5000.0,
            "miyatayn" to 200.0, "miyatain" to 200.0,
            "telt miyya" to 300.0, "tlet miyya" to 300.0,
            "reb miyya" to 400.0, "reb3 miyya" to 400.0,
            "khemsmiyya" to 500.0, "khems miyya" to 500.0,
            "miyya" to 100.0 // en dernier (priorité la plus basse)
        )
        for ((word, value) in wordNumbers) {
            if (t.contains(word)) return value
        }
        // Formes chiffrées : 500, 1500, 2.500, 2,500
        val regex = Regex("""(\d[\d\s]*(?:[.,]\d+)?)\s*(?:mad|dirham[s]?|dh|درهم|dinar[s]?|da|euro[s]?|€)?""")
        return regex.find(t)?.groupValues?.get(1)
            ?.replace(" ", "")?.replace(",", ".")?.toDoubleOrNull()
    }

    // Extrait une description en supprimant les chiffres et les mots vides (stop words)
    private fun extractDescription(t: String, stopWords: Set<String>): String =
        t.replace(Regex("""\d+(?:[.,]\d+)?\s*(?:mad|dirham[s]?|dh|درهم|dinar[s]?|da)?"""), "")
            .split(" ")
            .filter { it.length > 2 && it !in stopWords }
            .joinToString(" ")
            .trim()
            .take(60) // limite à 60 caractères

    // Met la première lettre de chaque mot en majuscule ("casablanca" → "Casablanca")
    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }

    // Label TTS pour chaque type de charge
    private fun TypeCharge.ttsLabel() = when (this) {
        TypeCharge.DIESEL     -> "diesel"
        TypeCharge.REPARATION -> "réparation"
        TypeCharge.AUTRE      -> "autre"
    }
}
