package com.example.taxipilot.core.data.database.dao

// DAO pour la table "taxis" — flotte gérée par le propriétaire.
// Fournit des requêtes triées par marque/modèle, filtrées par statut, et des mises à jour ciblées.
// ABORT sur INSERT : refuse d'ajouter un taxi dont la plaque existe déjà (contrainte de cohérence).

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.TaxiEntity
import com.example.taxipilot.core.data.database.entity.TaxiStatut
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxiDao {

    // Tous les taxis triés alphabétiquement par marque puis modèle
    @Query("SELECT * FROM taxis ORDER BY marque, modele")
    fun getAll(): Flow<List<TaxiEntity>>

    // Taxis filtrés par statut (ex : tous les DISPONIBLE)
    @Query("SELECT * FROM taxis WHERE statut = :statut")
    fun getByStatut(statut: TaxiStatut): Flow<List<TaxiEntity>>

    // Recherche d'un taxi par son ID (one-shot, retourne null si absent)
    @Query("SELECT * FROM taxis WHERE id = :id")
    suspend fun getById(id: Long): TaxiEntity?

    // Recherche par immatriculation — utilisé pour lier un chauffeur Firestore à un taxi Room
    @Query("SELECT * FROM taxis WHERE immatriculation = :immatriculation LIMIT 1")
    suspend fun getByImmatriculation(immatriculation: String): TaxiEntity?

    // Insère un nouveau taxi ; ABORT si l'immatriculation existe déjà
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(taxi: TaxiEntity): Long

    // Met à jour tous les champs d'un taxi existant
    @Update
    suspend fun update(taxi: TaxiEntity)

    // Mise à jour rapide du statut uniquement (sans toucher aux autres champs)
    @Query("UPDATE taxis SET statut = :statut WHERE id = :id")
    suspend fun updateStatut(id: Long, statut: TaxiStatut)

    // Mise à jour rapide du kilométrage uniquement
    @Query("UPDATE taxis SET kilometrage = :km WHERE id = :id")
    suspend fun updateKilometrage(id: Long, km: Int)

    // Supprime un taxi (en cascade : ses charges Room sont aussi supprimées)
    @Delete
    suspend fun delete(taxi: TaxiEntity)
}
