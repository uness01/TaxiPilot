package com.example.taxipilot.core.data.maps

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*

private const val TAG = "OSRM"

data class OsrmRoute(
    val distanceKm: Double,
    val durationMinutes: Double,
    /** Polyline as (lat, lon) pairs — note OSRM returns [lon, lat] in GeoJSON, already swapped here. */
    val polylinePoints: List<Pair<Double, Double>>
)

/**
 * OSRM routing via the public demo server.
 *
 * NOTE: router.project-osrm.org is a demo server. For production deployments
 * with high traffic, self-host OSRM: https://project-osrm.org/
 */
class OsrmRepository {

    suspend fun getRoute(
        fromLat: Double, fromLon: Double,
        toLat: Double,   toLon: Double
    ): OsrmRoute? = withContext(Dispatchers.IO) {
        runCatching {
            // OSRM expects coordinates as lon,lat
            val url = URL(
                "https://router.project-osrm.org/route/v1/driving/" +
                "$fromLon,$fromLat;$toLon,$toLat" +
                "?overview=full&geometries=geojson"
            )
            val conn = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", "TaxiPilotApp/1.0")
                connectTimeout = 15_000
                readTimeout    = 15_000
            }
            val body  = conn.inputStream.bufferedReader().readText().also { conn.disconnect() }
            val json  = JSONObject(body)
            if (json.getString("code") != "Ok") return@runCatching null

            val route    = json.getJSONArray("routes").getJSONObject(0)
            val meters   = route.getDouble("distance")
            val seconds  = route.getDouble("duration")
            val coords   = route.getJSONObject("geometry").getJSONArray("coordinates")

            val points = (0 until coords.length()).map { i ->
                val p = coords.getJSONArray(i)
                Pair(p.getDouble(1), p.getDouble(0)) // GeoJSON is [lon, lat] → (lat, lon)
            }

            OsrmRoute(
                distanceKm      = meters / 1000.0,
                durationMinutes = seconds / 60.0,
                polylinePoints  = points
            )
        }.getOrElse { e ->
            Log.w(TAG, "getRoute error: ${e.message}")
            null
        }
    }

    companion object {
        /**
         * Straight-line distance in metres between two WGS-84 coordinates
         * using the Haversine formula. Used for the 500 m proximity check
         * when a chauffeur taps "Terminer".
         */
        fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R    = 6_371_000.0
            val phi1 = Math.toRadians(lat1)
            val phi2 = Math.toRadians(lat2)
            val dphi = Math.toRadians(lat2 - lat1)
            val dlam = Math.toRadians(lon2 - lon1)
            val a = sin(dphi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dlam / 2).pow(2)
            return R * 2 * atan2(sqrt(a), sqrt(1 - a))
        }
    }
}
