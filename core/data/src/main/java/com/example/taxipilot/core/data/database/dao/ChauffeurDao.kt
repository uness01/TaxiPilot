package com.example.taxipilot.core.data.database.dao

// DAO pour la table "chauffeurs" — gestion locale des chauffeurs de la flotte.
// Fournit des flux triés par nom/prénom, des recherches par statut ou téléphone.
// ABORT sur INSERT : empêche les doublons (pas de chauffeur avec le même ID accidentellement).

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.ChauffeurEntity
import com.example.taxipilot.core.data.database.entity.ChauffeurStatut
import kotlinx.coroutines.flow.Flow

@Dao
interface ChauffeurDao {

    // Tous les chauffeurs triés par nom puis prénom (ordre alphabétique)
    @Query("SELECT * FROM chauffeurs ORDER BY nom, prenom")
    fun getAll(): Flow<List<ChauffeurEntity>>

    // Chauffeurs filtrés par statut (ACTIF, INACTIF, SUSPENDU)
    @Query("SELECT * FROM chauffeurs WHERE statut = :statut ORDER BY nom, prenom")
    fun getByStatut(statut: ChauffeurStatut): Flow<List<ChauffeurEntity>>

    // Recherche d'un chauffeur par son ID Room (one-shot)
    @Query("SELECT * FROM chauffeurs WHERE id = :id")
    suspend fun getById(id: Long): ChauffeurEntity?

    // Recherche par numéro de téléphone — utilisé pour éviter les doublons à l'inscription
    @Query("SELECT * FROM chauffeurs WHERE telephone = :telephone LIMIT 1")
    suspend fun getByTelephone(telephone: String): ChauffeurEntity?

    // Insère un nouveau chauffeur (ABORT si conflit sur la clé primaire)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(chauffeur: ChauffeurEntity): Long

    // Met à jour les informations d'un chauffeur existant
    @Update
    suspend fun update(chauffeur: ChauffeurEntity)

    // Mise à jour rapide du statut uniquement (ex : passer ACTIF → SUSPENDU)
    @Query("UPDATE chauffeurs SET statut = :statut WHERE id = :id")
    suspend fun updateStatut(id: Long, statut: ChauffeurStatut)

    // Supprime un chauffeur (ses trajets auront chauffeurId mis à null via SET_NULL)
    @Delete
    suspend fun delete(chauffeur: ChauffeurEntity)
}
