package com.example.taxipilot.core.data.repository

// Repository Firestore pour les utilisateurs (chauffeurs et propriétaires).
// Gère :
//   - Le liage chauffeur ↔ propriétaire (via le code à 6 chiffres)
//   - La liste en temps réel des chauffeurs d'un propriétaire
//   - L'assignation/désassignation de taxi
//   - La position GPS temps réel du chauffeur pendant une course
//   - Le statut de service (en_service / hors_service / en_course)
//   - Les tokens FCM pour l'envoi de notifications push
//   - La génération et régénération du code propriétaire

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
    private val users = db.collection("users") // collection Firestore des utilisateurs

    // ── Liage chauffeur ↔ propriétaire ────────────────────────────────────────

    // Cherche le propriétaire dont le codeProprietaire correspond à [code] (retourne null si absent)
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

    // Inscrit un chauffeur sous un propriétaire (écrit proprietaireId dans son document Firestore)
    suspend fun linkChauffeurToProprietaire(chauffeurUid: String, proprietaireId: String) {
        runCatching {
            users.document(chauffeurUid)
                .update("proprietaireId", proprietaireId).await()
        }.onFailure { Log.w(TAG, "linkChauffeur error: ${it.message}") }
    }

    // ── Flux temps réel (flotte) ──────────────────────────────────────────────

    // Liste en temps réel de tous les chauffeurs liés à un propriétaire.
    // Requête sur un seul champ → pas besoin d'index composite Firestore.
    fun getChauffeursByProprietaire(proprietaireId: String): Flow<List<FirestoreUser>> =
        callbackFlow {
            val reg = users
                .whereEqualTo("proprietaireId", proprietaireId)
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

    // Retourne le code existant du propriétaire, ou en génère un nouveau unique (6 chiffres)
    // si le champ est absent (comptes anciens sans code).
    suspend fun getOrGenerateProprietaireCode(uid: String): String? = runCatching {
        val doc = users.document(uid).get().await()
        val existing = doc.getString("codeProprietaire")
        if (!existing.isNullOrBlank()) return@runCatching existing

        // Génère un code unique en vérifiant les conflits dans Firestore
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

    // Propriétaire assigne un taxi (par matricule) à un chauffeur
    suspend fun assignTaxiToChauffeur(chauffeurUid: String, taxiMatricule: String) {
        runCatching {
            users.document(chauffeurUid).update("assignedTaxi", taxiMatricule).await()
        }.onFailure { Log.w(TAG, "assignTaxi error: ${it.message}") }
    }

    // Propriétaire retire le taxi d'un chauffeur (assignedTaxi → null)
    suspend fun unassignTaxi(chauffeurUid: String) {
        runCatching {
            users.document(chauffeurUid).update("assignedTaxi", null).await()
        }.onFailure { Log.w(TAG, "unassignTaxi error: ${it.message}") }
    }

    // Met à jour la position GPS du chauffeur (appelé toutes les ~15 s pendant une course)
    suspend fun updateCurrentLocation(uid: String, lat: Double, lng: Double) {
        runCatching {
            users.document(uid).update(
                mapOf("currentLat" to lat, "currentLng" to lng)
            ).await()
        }.onFailure { Log.w(TAG, "updateCurrentLocation error: ${it.message}") }
    }

    // Efface la position GPS à la fin de la course (currentLat/Lng → null)
    suspend fun clearCurrentLocation(uid: String) {
        runCatching {
            users.document(uid).update(
                mapOf("currentLat" to null, "currentLng" to null)
            ).await()
        }.onFailure { Log.w(TAG, "clearCurrentLocation error: ${it.message}") }
    }

    // One-shot : lit le statut actuel d'un utilisateur (retourne null si erreur réseau)
    suspend fun getStatut(uid: String): String? = runCatching {
        users.document(uid).get().await().getString("statut")
    }.getOrElse {
        Log.w(TAG, "getStatut error: ${it.message}")
        null
    }

    // Flux temps réel du statut d'un utilisateur (snapshot listener → Flow)
    // Utilisé par DriverViewModel pour rester synchronisé avec Firestore après toggleStatut()
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

    // One-shot : récupère un profil utilisateur complet par son UID (ex : infos du propriétaire)
    suspend fun getUser(uid: String): FirestoreUser? = runCatching {
        users.document(uid).get().await().toFirestoreUser()
    }.getOrElse {
        Log.w(TAG, "getUser error: ${it.message}")
        null
    }

    // Génère un nouveau code alphanumériquer unique (6 caractères) et le sauvegarde.
    // Les chauffeurs déjà liés ne sont PAS affectés — seuls les nouveaux liages utilisent le nouveau code.
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

    // Met à jour le statut de service d'un chauffeur (en_service / hors_service / en_course)
    suspend fun updateStatut(userUid: String, statut: String) {
        runCatching {
            users.document(userUid).update("statut", statut).await()
        }.onFailure { Log.w(TAG, "updateStatut error: ${it.message}") }
    }

    // Récupère les tokens FCM de TOUS les chauffeurs (pour broadcast de notifications)
    suspend fun getAllChauffeurFcmTokens(): List<String> = runCatching {
        val snap = users.whereEqualTo("role", FirestoreUser.ROLE_CHAUFFEUR).get().await()
        snap.documents.mapNotNull { it.getString("fcmToken") }.filter { it.isNotBlank() }
    }.getOrElse {
        Log.w(TAG, "getAllChauffeurFcmTokens error: ${it.message}")
        emptyList()
    }

    // Récupère les tokens FCM uniquement des chauffeurs en_service (disponibles pour de nouvelles courses)
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

    // ── Désérialisation ───────────────────────────────────────────────────────

    // Convertit un DocumentSnapshot en FirestoreUser (retourne null si invalide)
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
