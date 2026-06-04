package com.example.taxipilot.core.data.maps

// Repository pour le géocodage via l'API OpenStreetMap Nominatim.
// Deux opérations :
//   - search()   : géocodage direct (texte libre → liste de lieux avec coordonnées GPS)
//   - reverse()  : géocodage inverse (coordonnées GPS → adresse lisible "Rue, Ville")
//
// Règles d'utilisation de Nominatim :
//   - Max 1 requête/seconde → le debounce doit être géré dans la couche UI
//   - Identifier l'app avec un User-Agent valide ("TaxiPilotApp/1.0")
//   - Ne pas utiliser pour du trafic très élevé (préférer héberger sa propre instance)

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val TAG = "Nominatim"

// Résultat d'une recherche Nominatim : coordonnées + nom affiché
data class NominatimResult(
    val lat: Double,           // latitude du lieu
    val lon: Double,           // longitude du lieu
    val displayName: String    // nom complet retourné par Nominatim
) {
    // Retourne un nom court "Rue, Ville" (max 40 caractères) en filtrant les divisions administratives
    fun shortName(): String = formatShortAddress(displayName)
}

// Mots à ignorer pour construire le nom court (divisions administratives Maroc/Maghreb)
private val SKIP_WORDS = listOf(
    "arrondissement", "arrondissements",
    "préfecture", "prefecture",
    "région", "region",
    "wilaya", "commune", "municipalité",
    "maroc", "morocco", "algérie", "algeria", "tunisie", "tunisia",
    "mauritanie", "mauritania"
)

// Formatte une adresse complète en version courte "Rue, Ville" (max 40 caractères)
// Utilisé par NominatimResult.shortName() et le géocodage inverse
internal fun formatShortAddress(displayName: String): String {
    val parts = displayName.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val street = parts.firstOrNull() ?: return displayName.take(40)

    // Ville : premier segment après la rue qui n'est ni un code postal ni une division admin
    val city = parts.drop(1).firstOrNull { part ->
        val lower = part.lowercase()
        !lower.all { c -> c.isDigit() || c == ' ' } &&
        SKIP_WORDS.none { lower.contains(it) }
    }

    val result = if (city != null) "$street, $city" else street
    return if (result.length > 40) result.take(37) + "…" else result
}

class NominatimRepository {

    // Géocodage direct : texte libre → liste de résultats avec coordonnées GPS
    // Utilisé dans les champs de saisie d'adresse (départ et arrivée) avec autocomplétion
    suspend fun search(query: String, limit: Int = 5): List<NominatimResult> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            runCatching {
                val encoded = URLEncoder.encode(query.trim(), "UTF-8")
                val url = URL(
                    "https://nominatim.openstreetmap.org/search" +
                    "?q=$encoded&format=json&limit=$limit&addressdetails=0"
                )
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "TaxiPilotApp/1.0") // obligatoire selon Nominatim
                    connectTimeout = 10_000
                    readTimeout    = 10_000
                }
                val body = conn.inputStream.bufferedReader().readText().also { conn.disconnect() }
                val arr  = JSONArray(body)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    NominatimResult(
                        lat         = obj.getString("lat").toDouble(),
                        lon         = obj.getString("lon").toDouble(),
                        displayName = obj.getString("display_name")
                    )
                }
            }.getOrElse { e ->
                Log.w(TAG, "search error: ${e.message}")
                emptyList()
            }
        }

    // Géocodage inverse : coordonnées GPS → adresse courte lisible ("Rue, Ville")
    // Utilisé pour afficher l'adresse au relâchement du marker sur la carte OSMDroid
    suspend fun reverse(lat: Double, lon: Double): String =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = URL(
                    "https://nominatim.openstreetmap.org/reverse" +
                    "?lat=$lat&lon=$lon&format=json&addressdetails=1"
                )
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "TaxiPilotApp/1.0")
                    connectTimeout = 10_000
                    readTimeout    = 10_000
                }
                val body = conn.inputStream.bufferedReader().readText().also { conn.disconnect() }
                val json = JSONObject(body)
                val addr = json.optJSONObject("address")
                if (addr != null) {
                    // Rue : préfère "road", sinon "neighbourhood", sinon "suburb"
                    val street = addr.optString("road").ifBlank {
                        addr.optString("neighbourhood").ifBlank {
                            addr.optString("suburb").ifBlank { "" }
                        }
                    }
                    // Ville : préfère "city", sinon "town", sinon "village", sinon "county"
                    val city = addr.optString("city").ifBlank {
                        addr.optString("town").ifBlank {
                            addr.optString("village").ifBlank {
                                addr.optString("county").ifBlank { "" }
                            }
                        }
                    }
                    val result = when {
                        street.isNotBlank() && city.isNotBlank() -> "$street, $city"
                        street.isNotBlank() -> street
                        city.isNotBlank()   -> city
                        else -> formatShortAddress(
                            json.optString("display_name").ifBlank { "$lat, $lon" }
                        )
                    }
                    if (result.length > 40) result.take(37) + "…" else result
                } else {
                    formatShortAddress(
                        json.optString("display_name").ifBlank { "$lat, $lon" }
                    )
                }
            }.getOrElse { "$lat, $lon" } // fallback : affiche les coordonnées brutes si erreur
        }
}
