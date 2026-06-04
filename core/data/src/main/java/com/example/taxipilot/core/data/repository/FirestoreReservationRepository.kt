package com.example.taxipilot.core.data.repository

// Repository Firestore pour les réservations — SOURCE DE VÉRITÉ principale du flux de booking.
// Toutes les opérations sur les réservations passent par ce fichier.
//
// Architecture des flux :
//   - snapshotFlow() : wraps un listener Firestore en Flow Kotlin (temps réel)
//   - Les erreurs Firestore (PERMISSION_DENIED, index manquant, réseau) émettent une liste vide
//     et ne ferment PAS le Flow → l'app reste vivante et se reconnecte automatiquement.
//
// Opération critique : accept() utilise une TRANSACTION Firestore pour éviter
// que deux chauffeurs acceptent la même course simultanément (first-come-first-served).

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
    private val col = db.collection("reservations") // collection principale Firestore

    // ── Création ──────────────────────────────────────────────────────────────

    // Crée une nouvelle réservation dans Firestore et retourne son ID généré
    suspend fun create(reservation: FirestoreReservation): String {
        val doc    = col.document()              // génère un ID unique Firestore
        val withId = reservation.copy(id = doc.id) // on stocke l'ID dans le document aussi
        doc.set(withId.toMap()).await()
        return doc.id
    }

    // ── Flux temps réel ───────────────────────────────────────────────────────

    // Réservations EN_ATTENTE (feed "disponibles" pour les chauffeurs), triées par date de création
    // Pas d'orderBy Firestore pour éviter un index composite — tri fait en mémoire
    fun getAvailable(): Flow<List<FirestoreReservation>> = snapshotFlow(
        query = col.whereEqualTo("status", FirestoreReservation.STATUS_EN_ATTENTE)
    ).map { list -> list.sortedBy { it.createdAt } }

    // Réservations assignées à un chauffeur précis (son historique + course active)
    fun getByChauffeur(chauffeurId: String): Flow<List<FirestoreReservation>> = snapshotFlow(
        query = col.whereEqualTo("chauffeurId", chauffeurId)
    ).map { list -> list.sortedByDescending { it.createdAt } }

    // Réservations faites par un client précis (son historique de courses)
    fun getByClient(clientId: String): Flow<List<FirestoreReservation>> = snapshotFlow(
        query = col.whereEqualTo("clientId", clientId)
    ).map { list -> list.sortedByDescending { it.createdAt } }

    // Toutes les réservations (vue globale pour le propriétaire)
    fun getAll(): Flow<List<FirestoreReservation>> = snapshotFlow(query = col)
        .map { list -> list.sortedByDescending { it.createdAt } }

    // ── Acceptation atomique (first-come-first-served) ────────────────────────

    // Tente de réserver une course pour un chauffeur.
    // Utilise une transaction Firestore pour éviter les doubles acceptations.
    // Retourne true si succès, false si la course est déjà prise ou introuvable.
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
            // Si la réservation n'est plus EN_ATTENTE, quelqu'un d'autre a été plus rapide
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

    // ── Transitions de statut ─────────────────────────────────────────────────

    // Chauffeur démarre la course → statut passe à EN_COURS
    suspend fun demarrer(reservationId: String) =
        col.document(reservationId).update("status", FirestoreReservation.STATUS_EN_COURS).await()

    // Chauffeur termine la course → enregistre le prix final, la distance réelle et l'heure de fin
    suspend fun terminer(reservationId: String, prixFinal: Double, distanceReelle: Double) =
        col.document(reservationId).update(
            mapOf(
                "status"         to FirestoreReservation.STATUS_TERMINEE,
                "prixFinal"      to prixFinal,
                "distanceReelle" to distanceReelle,
                "completedAt"    to System.currentTimeMillis()
            )
        ).await()

    // Réservations des chauffeurs d'un propriétaire (pour le tableau de bord owner)
    fun getByProprietaire(proprietaireId: String): Flow<List<FirestoreReservation>> = snapshotFlow(
        query = col.whereEqualTo("proprietaireId", proprietaireId)
    ).map { list -> list.sortedByDescending { it.createdAt } }

    // Client annule sa réservation
    suspend fun cancel(reservationId: String) =
        col.document(reservationId).update("status", FirestoreReservation.STATUS_ANNULEE).await()

    // ── Flux snapshot interne (factorisation) ─────────────────────────────────

    // Wraps une requête Firestore en Flow Kotlin avec gestion sécurisée des erreurs.
    // Les erreurs émettent une liste vide (pas de crash) et le listener reste actif.
    private fun snapshotFlow(
        query: com.google.firebase.firestore.Query
    ): Flow<List<FirestoreReservation>> = callbackFlow {
        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e(TAG, "Firestore error: ${err.code} — ${err.message}")
                trySend(emptyList()) // émet une liste vide pour garder l'UI en vie
                return@addSnapshotListener
            }
            trySend(snap?.documents?.mapNotNull { it.toReservation() } ?: emptyList())
        }
        awaitClose { reg.remove() } // supprime le listener quand le Flow est annulé
    }.catch { e ->
        Log.e(TAG, "Unexpected flow error: ${e.message}")
        emit(emptyList())
    }

    // ── Désérialisation ───────────────────────────────────────────────────────

    // Convertit un DocumentSnapshot Firestore en FirestoreReservation (retourne null si invalide)
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
