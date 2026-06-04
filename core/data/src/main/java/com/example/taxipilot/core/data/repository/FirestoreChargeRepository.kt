package com.example.taxipilot.core.data.repository

// Repository Firestore pour les charges (dépenses des chauffeurs).
// Les charges sont d'abord créées en local (ChargeEntity Room) par le chauffeur,
// puis mirrorées ici dans Firestore pour que le propriétaire puisse les voir sur son dashboard.
//
// Deux vues disponibles :
//   - getByProprietaire : toutes les dépenses des chauffeurs d'un propriétaire (vue owner)
//   - getByChauffeur    : dépenses d'un chauffeur spécifique (vue chauffeur — non utilisée actuellement)

import android.util.Log
import com.example.taxipilot.core.data.firestore.FirestoreCharge
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreCharges"

class FirestoreChargeRepository {

    private val db  = FirebaseFirestore.getInstance()
    private val col = db.collection("charges") // collection Firestore des charges

    // Crée une charge dans Firestore et retourne son ID (chaîne vide si erreur)
    suspend fun create(charge: FirestoreCharge): String = runCatching {
        val doc = col.document()
        val withId = charge.copy(id = doc.id)
        doc.set(withId.toMap()).await()
        doc.id
    }.getOrElse { e ->
        Log.w(TAG, "create error: ${e.message}")
        ""
    }

    // Flux des charges de tous les chauffeurs d'un propriétaire, triées par date (plus récent d'abord)
    fun getByProprietaire(proprietaireId: String): Flow<List<FirestoreCharge>> =
        snapshotFlow(col.whereEqualTo("proprietaireId", proprietaireId))
            .map { list -> list.sortedByDescending { it.date } }

    // Flux des charges d'un chauffeur précis (pour vue historique chauffeur)
    fun getByChauffeur(chauffeurUid: String): Flow<List<FirestoreCharge>> =
        snapshotFlow(col.whereEqualTo("chauffeurUid", chauffeurUid))
            .map { list -> list.sortedByDescending { it.date } }

    // Wraps une requête Firestore en Flow avec gestion sécurisée des erreurs
    private fun snapshotFlow(
        query: com.google.firebase.firestore.Query
    ): Flow<List<FirestoreCharge>> = callbackFlow {
        val reg = query.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e(TAG, "Firestore error: ${err.code} — ${err.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snap?.documents?.mapNotNull { it.toCharge() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }.catch { e ->
        Log.e(TAG, "Unexpected flow error: ${e.message}")
        emit(emptyList())
    }

    // Convertit un DocumentSnapshot en FirestoreCharge (null si invalide)
    private fun DocumentSnapshot.toCharge(): FirestoreCharge? = runCatching {
        FirestoreCharge(
            id             = getString("id")             ?: id,
            chauffeurUid   = getString("chauffeurUid")   ?: "",
            proprietaireId = getString("proprietaireId") ?: "",
            type           = getString("type")           ?: "",
            description    = getString("description")    ?: "",
            montant        = getDouble("montant")        ?: 0.0,
            date           = getLong("date")             ?: System.currentTimeMillis()
        )
    }.getOrElse {
        Log.w(TAG, "Failed to parse charge $id: ${it.message}")
        null
    }
}
