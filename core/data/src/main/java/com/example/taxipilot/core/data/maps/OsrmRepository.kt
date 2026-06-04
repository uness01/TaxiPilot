package com.example.taxipilot.core.data.maps

// Repository pour le calcul d'itinéraires via l'API OSRM (Open Source Routing Machine).
// Utilisé pour :
//   1. Calculer la distance et la durée entre deux points (départ → arrivée) avant une course
//   2. Obtenir le tracé de la route (polyline) pour l'afficher sur la carte
//   3. Calculer la distance entre le chauffeur et la destination (vérification de proximité)
//
// Note : router.project-osrm.org est un serveur de démo public. Pour la production à fort
// trafic, il faut héberger sa propre instance OSRM.
//
// Format des coordonnées OSRM : longitude,latitude (l'inverse de la convention habituelle !)
// La méthode getRoute() corrige cela et retourne des points en (lat, lon).

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*

private const val TAG = "OSRM"

// Résultat d'un calcul d'itinéraire OSRM
data class OsrmRoute(
    val distanceKm: Double,                         // distance totale en kilomètres
    val durationMinutes: Double,                    // durée estimée en minutes
    val polylinePoints: List<Pair<Double, Double>>  // liste de points (lat, lon) pour tracer la route
)

class OsrmRepository {

    // Calcule l'itinéraire de conduite entre deux points GPS.
    // Retourne null si le serveur est inaccessible ou si aucun itinéraire n'est trouvé.
    suspend fun getRoute(
        fromLat: Double, fromLon: Double,
        toLat: Double,   toLon: Double
    ): OsrmRoute? = withContext(Dispatchers.IO) {
        runCatching {
            // OSRM attend les coordonnées en format lon,lat (longitude d'abord)
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
            val meters   = route.getDouble("distance")    // distance en mètres
            val seconds  = route.getDouble("duration")    // durée en secondes
            val coords   = route.getJSONObject("geometry").getJSONArray("coordinates")

            // GeoJSON retourne [lon, lat] → on inverse en (lat, lon) pour la convention Android
            val points = (0 until coords.length()).map { i ->
                val p = coords.getJSONArray(i)
                Pair(p.getDouble(1), p.getDouble(0)) // inversion : [lon,lat] → (lat,lon)
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
        // Calcule la distance en mètres entre deux points GPS par la formule de Haversine.
        // Utilisé pour vérifier que le chauffeur est bien à moins de 500 m de la destination
        // avant de l'autoriser à appuyer sur "Terminer la course".
        // R = 6 371 000 m (rayon moyen de la Terre)
        fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R    = 6_371_000.0
            val phi1 = Math.toRadians(lat1)
            val phi2 = Math.toRadians(lat2)
            val dphi = Math.toRadians(lat2 - lat1)
            val dlam = Math.toRadians(lon2 - lon1)
            val a = sin(dphi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dlam / 2).pow(2)
            return R * 2 * atan2(sqrt(a), sqrt(1 - a)) // distance en mètres
        }
    }
}
