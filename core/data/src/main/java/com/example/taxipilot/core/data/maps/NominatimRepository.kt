package com.example.taxipilot.core.data.maps

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val TAG = "Nominatim"

data class NominatimResult(
    val lat: Double,
    val lon: Double,
    val displayName: String
) {
    /**
     * Returns "Street/Neighbourhood, City" — at most 40 chars.
     * Skips postal codes, country names, and French/Arabic admin divisions
     * (arrondissement, préfecture, région, wilaya…).
     */
    fun shortName(): String = formatShortAddress(displayName)
}

private val SKIP_WORDS = listOf(
    "arrondissement", "arrondissements",
    "préfecture", "prefecture",
    "région", "region",
    "wilaya", "commune", "municipalité",
    "maroc", "morocco", "algérie", "algeria", "tunisie", "tunisia",
    "mauritanie", "mauritania"
)

/** Shared address formatter used by NominatimResult and reverse geocoding. */
internal fun formatShortAddress(displayName: String): String {
    val parts = displayName.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val street = parts.firstOrNull() ?: return displayName.take(40)

    // City: first part after the street that is not a postal code and not an admin division
    val city = parts.drop(1).firstOrNull { part ->
        val lower = part.lowercase()
        !lower.all { c -> c.isDigit() || c == ' ' } &&
        SKIP_WORDS.none { lower.contains(it) }
    }

    val result = if (city != null) "$street, $city" else street
    return if (result.length > 40) result.take(37) + "…" else result
}

/**
 * OpenStreetMap Nominatim geocoding.
 *
 * Usage policy: https://operations.osmfoundation.org/policies/nominatim/
 * — Identify app with a valid User-Agent.
 * — Max 1 request / second (debounce searches in the UI layer).
 */
class NominatimRepository {

    /** Forward geocoding: free-text query → list of matching locations. */
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
                    setRequestProperty("User-Agent", "TaxiPilotApp/1.0")
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

    /** Reverse geocoding: coordinates → short human-readable address ("Street, City"). */
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
                    // Street-level label: prefer road, then neighbourhood, then suburb
                    val street = addr.optString("road").ifBlank {
                        addr.optString("neighbourhood").ifBlank {
                            addr.optString("suburb").ifBlank { "" }
                        }
                    }
                    // City-level label: prefer city, then town, then village, then county
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
            }.getOrElse { "$lat, $lon" }
        }
}
