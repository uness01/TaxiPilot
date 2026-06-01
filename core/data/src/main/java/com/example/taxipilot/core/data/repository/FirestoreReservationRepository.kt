package com.example.taxipilot.core.data.repository

import android.util.Log
import com.example.taxipilot.core.data.firestore.FirestoreReservation
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreReservations"

class FirestoreReservationRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("reservations")

    // ── Create ────────────────────────────────────────────────────────────────

    suspend fun create(reservation: FirestoreReservation): String {
        val doc    = col.document()
        val withId = reservation.copy(id = doc.id)
        doc.set(withId.toMap()).await()
        return doc.id
    }

    // ── Real-time flows ───────────────────────────────────────────────────────

    /**
     * All EN_ATTENTE reservations — chauffeur "available" feed.
     *
     * NOTE: No orderBy here to avoid needing a composite Firestore index.
     * Results are sorted in-memory by createdAt ascending.
     */
    fun getAvailable(): Flow<List<FirestoreReservation>> = snapshotFlow(
        query = col.whereEqualTo("status", FirestoreReservation.STATUS_EN_ATTENTE)
    ).map { list -> list.sortedBy { it.createdAt } }

    /**
     * Reservations assigned to a specific chauffeur, sorted newest first.
     */
    fun getByChauffeur(chauffeurId: String): Flow<List<FirestoreReservation>> = snapshotFlow(
        query = col.whereEqualTo("chauffeurId", chauffeurId)
    ).map { list -> list.sortedByDescending { it.createdAt } }

    /**
     * Reservations made by a specific client, sorted newest first.
     */
    fun getByClient(clientId: String): Flow<List<FirestoreReservation>> = snapshotFlow(
        query = col.whereEqualTo("clientId", clientId)
    ).map { list -> list.sortedByDescending { it.createdAt } }

    /**
     * All reservations — Propriétaire overview, sorted newest first.
     */
    fun getAll(): Flow<List<FirestoreReservation>> = snapshotFlow(query = col)
        .map { list -> list.sortedByDescending { it.createdAt } }

    // ── Atomic accept (first-come-first-served) ───────────────────────────────

    /**
     * Atomically claims a reservation for a chauffeur.
     * Returns true on success, false if someone else was faster or reservation not found.
     */
    suspend fun accept(
        reservationId: String,
        chauffeurId: String,
        chauffeurName: String,
        proprietaireId: String? = null,
        taxiAssigned: String? = null
    ): Boolean = try {
        db.runTransaction { tx ->
            val ref     = col.document(reservationId)
            val snap    = tx.get(ref)
            val current = snap.toReservation() ?: throw Exception("not_found")
            if (current.status != FirestoreReservation.STATUS_EN_ATTENTE) {
                throw Exception("already_taken")
            }
            tx.update(
                ref, mapOf(
                    "status"         to FirestoreReservation.STATUS_ACCEPTEE,
                    "chauffeurId"    to chauffeurId,
                    "chauffeurName"  to chauffeurName,
                    "proprietaireId" to proprietaireId,
                    "taxiAssigned"   to taxiAssigned
                )
            )
        }.await()
        true
    } catch (e: Exception) {
        Log.w(TAG, "accept failed: ${e.message}")
        false
    }

    // ── Status transitions ────────────────────────────────────────────────────

    suspend fun demarrer(reservationId: String) =
        col.document(reservationId).update("status", FirestoreReservation.STATUS_EN_COURS).await()

    suspend fun terminer(reservationId: String, prixFinal: Double, distanceReelle: Double) =
        col.document(reservationId).update(
            mapOf(
                "status"         to FirestoreReservation.STATUS_TERMINEE,
                "prixFinal"      to prixFinal,
                "distanceReelle" to distanceReelle,
                "completedAt"    to System.currentTimeMillis()
            )
        ).await()

    /**
     * All reservations where this proprietaire's chauffeurs completed the trip.
     */
    fun getByProprietaire(proprietaireId: String): Flow<List<FirestoreReservation>> = snapshotFlow(
        query = col.whereEqualTo("proprietaireId", proprietaireId)
    ).map { list -> list.sortedByDescending { it.createdAt } }

    suspend fun cancel(reservationId: String) =
        col.document(reservationId).update("status", FirestoreReservation.STATUS_ANNULEE).await()

    // ── Internal: safe snapshot flow ──────────────────────────────────────────

    /**
     * Wraps a Firestore query in a callbackFlow.
     *
     * Errors (PERMISSION_DENIED, FAILED_PRECONDITION / missing index, network) are
     * logged and converted to an empty list — the listener stays registered and
     * will recover automatically once conditions improve (rules fixed, index created,
     * connection restored).  We never call close(err) so the flow never terminates
     * with an exception, which would otherwise crash via viewModelScope.
     */
    private fun snapshotFlow(
        query: com.google.firebase.firestore.Query
    ): Flow<List<FirestoreReservation>> = callbackFlow {
        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e(TAG, "Firestore error: ${err.code} — ${err.message}")
                // Emit empty list so the app stays alive; Firestore will retry automatically.
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snap?.documents?.mapNotNull { it.toReservation() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }.catch { e ->
        // Belt-and-suspenders: catch any unexpected flow errors
        Log.e(TAG, "Unexpected flow error: ${e.message}")
        emit(emptyList())
    }

    // ── Deserialization ───────────────────────────────────────────────────────

    private fun DocumentSnapshot.toReservation(): FirestoreReservation? = runCatching {
        FirestoreReservation(
            id             = getString("id")            ?: id,
            clientId       = getString("clientId")      ?: "",
            clientName     = getString("clientName")    ?: "",
            clientPhone    = getString("clientPhone")   ?: "",
            depart         = getString("depart")        ?: "",
            arrivee        = getString("arrivee")       ?: "",
            prixEstime     = getDouble("prixEstime")    ?: 0.0,
            type           = getString("type")          ?: FirestoreReservation.TYPE_IMMEDIATE,
            scheduledTime  = getLong("scheduledTime"),
            status         = getString("status")        ?: FirestoreReservation.STATUS_EN_ATTENTE,
            chauffeurId    = getString("chauffeurId"),
            chauffeurName  = getString("chauffeurName"),
            proprietaireId = getString("proprietaireId"),
            taxiAssigned   = getString("taxiAssigned"),
            prixFinal      = getDouble("prixFinal"),
            distanceReelle = getDouble("distanceReelle"),
            completedAt    = getLong("completedAt"),
            createdAt      = getLong("createdAt")       ?: System.currentTimeMillis(),
            departLat      = getDouble("departLat"),
            departLng      = getDouble("departLng"),
            arriveeLat     = getDouble("arriveeLat"),
            arriveeLng     = getDouble("arriveeLng"),
            distanceKm     = getDouble("distanceKm")    ?: 0.0
        )
    }.getOrElse {
        Log.w(TAG, "Failed to parse document $id: ${it.message}")
        null
    }
}
