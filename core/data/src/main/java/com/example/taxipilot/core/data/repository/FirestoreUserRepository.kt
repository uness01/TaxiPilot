package com.example.taxipilot.core.data.repository

import android.util.Log
import com.example.taxipilot.core.data.firestore.FirestoreUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreUsers"

class FirestoreUserRepository {

    private val db    = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    // ── Chauffeur linking ─────────────────────────────────────────────────────

    /**
     * Returns the proprietaire whose codeProprietaire matches [code], or null.
     */
    suspend fun getProprietaireByCode(code: String): FirestoreUser? = runCatching {
        val query = users
            .whereEqualTo("codeProprietaire", code)
            .whereEqualTo("role", FirestoreUser.ROLE_PROPRIETAIRE)
            .limit(1)
            .get().await()
        query.documents.firstOrNull()?.toFirestoreUser()
    }.getOrElse {
        Log.w(TAG, "getProprietaireByCode error: ${it.message}")
        null
    }

    /**
     * Sets [proprietaireId] on the chauffeur's Firestore user document.
     */
    suspend fun linkChauffeurToProprietaire(chauffeurUid: String, proprietaireId: String) {
        runCatching {
            users.document(chauffeurUid)
                .update("proprietaireId", proprietaireId).await()
        }.onFailure { Log.w(TAG, "linkChauffeur error: ${it.message}") }
    }

    // ── Real-time fleet flows ─────────────────────────────────────────────────

    /**
     * Live list of all users linked to [proprietaireId].
     * Uses a single-field query (no composite index needed).
     * Only chauffeurs ever have proprietaireId set, so the role filter is redundant.
     */
    fun getChauffeursByProprietaire(proprietaireId: String): Flow<List<FirestoreUser>> =
        callbackFlow {
            val reg = users
                .whereEqualTo("proprietaireId", proprietaireId)   // single-field — no index required
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        Log.e(TAG, "Fleet listener error: ${err.code} — ${err.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snap?.documents?.mapNotNull { it.toFirestoreUser() } ?: emptyList())
                }
            awaitClose { reg.remove() }
        }.catch { e ->
            Log.e(TAG, "Fleet flow error: ${e.message}")
            emit(emptyList())
        }

    /**
     * Returns the existing [codeProprietaire] for [uid].
     * If the field is missing (old account), generates a unique 6-digit code,
     * saves it to Firestore, and returns it.
     */
    suspend fun getOrGenerateProprietaireCode(uid: String): String? = runCatching {
        val doc = users.document(uid).get().await()
        val existing = doc.getString("codeProprietaire")
        if (!existing.isNullOrBlank()) return@runCatching existing

        // No code yet — generate a unique one
        var code: String
        do {
            code = (100000..999999).random().toString()
            val conflict = users.whereEqualTo("codeProprietaire", code).limit(1).get().await()
        } while (!conflict.isEmpty)

        users.document(uid).update("codeProprietaire", code).await()
        code
    }.getOrElse {
        Log.w(TAG, "getOrGenerateProprietaireCode error: ${it.message}")
        null
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    /** Owner assigns a taxi (by matricule) to a chauffeur. */
    suspend fun assignTaxiToChauffeur(chauffeurUid: String, taxiMatricule: String) {
        runCatching {
            users.document(chauffeurUid).update("assignedTaxi", taxiMatricule).await()
        }.onFailure { Log.w(TAG, "assignTaxi error: ${it.message}") }
    }

    /** Remove taxi assignment from a chauffeur. */
    suspend fun unassignTaxi(chauffeurUid: String) {
        runCatching {
            users.document(chauffeurUid).update("assignedTaxi", null).await()
        }.onFailure { Log.w(TAG, "unassignTaxi error: ${it.message}") }
    }

    /** Write chauffeur's current GPS position (called every ~15 s while en_course). */
    suspend fun updateCurrentLocation(uid: String, lat: Double, lng: Double) {
        runCatching {
            users.document(uid).update(
                mapOf("currentLat" to lat, "currentLng" to lng)
            ).await()
        }.onFailure { Log.w(TAG, "updateCurrentLocation error: ${it.message}") }
    }

    /** Clear GPS position when course ends. */
    suspend fun clearCurrentLocation(uid: String) {
        runCatching {
            users.document(uid).update(
                mapOf("currentLat" to null, "currentLng" to null)
            ).await()
        }.onFailure { Log.w(TAG, "clearCurrentLocation error: ${it.message}") }
    }

    /** Fetch the current statut string for a single user (one-shot). */
    suspend fun getStatut(uid: String): String? = runCatching {
        users.document(uid).get().await().getString("statut")
    }.getOrElse {
        Log.w(TAG, "getStatut error: ${it.message}")
        null
    }

    /** Real-time statut stream for a single user via snapshot listener. */
    fun observeStatut(uid: String): Flow<String> = callbackFlow {
        val reg = users.document(uid).addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(TAG, "observeStatut error: ${err.message}")
                return@addSnapshotListener
            }
            val statut = snap?.getString("statut") ?: return@addSnapshotListener
            trySend(statut)
        }
        awaitClose { reg.remove() }
    }.catch { e -> Log.e(TAG, "observeStatut flow error: ${e.message}") }

    /** One-shot fetch of any user document by UID (used to load proprietaire info). */
    suspend fun getUser(uid: String): FirestoreUser? = runCatching {
        users.document(uid).get().await().toFirestoreUser()
    }.getOrElse {
        Log.w(TAG, "getUser error: ${it.message}")
        null
    }

    /**
     * Generate a new unique 6-character alphanumeric code, save it on the proprietaire's
     * document, and return it.  The old code becomes invalid for new registrations;
     * already-linked chauffeurs are NOT affected.
     */
    suspend fun regenerateProprietaireCode(uid: String): String? = runCatching {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        do {
            code = (1..6).map { chars.random() }.joinToString("")
            val conflict = users.whereEqualTo("codeProprietaire", code).limit(1).get().await()
        } while (!conflict.isEmpty)
        users.document(uid).update("codeProprietaire", code).await()
        code
    }.getOrElse {
        Log.w(TAG, "regenerateProprietaireCode error: ${it.message}")
        null
    }

    /** Update chauffeur statut (disponible / en_course). */
    suspend fun updateStatut(userUid: String, statut: String) {
        runCatching {
            users.document(userUid).update("statut", statut).await()
        }.onFailure { Log.w(TAG, "updateStatut error: ${it.message}") }
    }

    /** Fetch FCM tokens for all CHAUFFEUR users. */
    suspend fun getAllChauffeurFcmTokens(): List<String> = runCatching {
        val snap = users.whereEqualTo("role", FirestoreUser.ROLE_CHAUFFEUR).get().await()
        snap.documents.mapNotNull { it.getString("fcmToken") }.filter { it.isNotBlank() }
    }.getOrElse {
        Log.w(TAG, "getAllChauffeurFcmTokens error: ${it.message}")
        emptyList()
    }

    /** Fetch FCM tokens only for chauffeurs who are en_service (available for new reservations). */
    suspend fun getActiveChauffeurFcmTokens(): List<String> = runCatching {
        val snap = users
            .whereEqualTo("role", FirestoreUser.ROLE_CHAUFFEUR)
            .whereEqualTo("statut", FirestoreUser.STATUT_EN_SERVICE)
            .get().await()
        snap.documents.mapNotNull { it.getString("fcmToken") }.filter { it.isNotBlank() }
    }.getOrElse {
        Log.w(TAG, "getActiveChauffeurFcmTokens error: ${it.message}")
        emptyList()
    }

    // ── Deserialization ───────────────────────────────────────────────────────

    private fun DocumentSnapshot.toFirestoreUser(): FirestoreUser? = runCatching {
        FirestoreUser(
            uid              = getString("uid")              ?: id,
            nom              = getString("nom")              ?: "",
            telephone        = getString("telephone")        ?: "",
            role             = getString("role")             ?: "",
            fcmToken         = getString("fcmToken"),
            codeProprietaire = getString("codeProprietaire"),
            proprietaireId   = getString("proprietaireId"),
            assignedTaxi     = getString("assignedTaxi"),
            statut           = getString("statut")           ?: FirestoreUser.STATUT_DISPONIBLE,
            currentLat       = getDouble("currentLat"),
            currentLng       = getDouble("currentLng")
        )
    }.getOrElse {
        Log.w(TAG, "Failed to parse user $id: ${it.message}")
        null
    }
}
