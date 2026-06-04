package com.example.taxipilot.core.data.repository

// Repository pour les chauffeurs locaux (Room/SQLite).
// Expose les opérations CRUD et des raccourcis métier comme getActifs().
// Utilisé par OwnerViewModel pour gérer les chauffeurs de la flotte.

import com.example.taxipilot.core.data.database.dao.ChauffeurDao
import com.example.taxipilot.core.data.database.entity.ChauffeurEntity
import com.example.taxipilot.core.data.database.entity.ChauffeurStatut
import kotlinx.coroutines.flow.Flow

class ChauffeurRepository(private val dao: ChauffeurDao) {

    // Tous les chauffeurs (flux observable, trié par nom)
    fun getAll(): Flow<List<ChauffeurEntity>> = dao.getAll()

    // Raccourci : uniquement les chauffeurs actifs (statut = ACTIF)
    fun getActifs(): Flow<List<ChauffeurEntity>> = dao.getByStatut(ChauffeurStatut.ACTIF)

    // Chauffeurs filtrés par statut (ACTIF, INACTIF, SUSPENDU)
    fun getByStatut(statut: ChauffeurStatut): Flow<List<ChauffeurEntity>> = dao.getByStatut(statut)

    // Recherche one-shot par ID Room
    suspend fun getById(id: Long): ChauffeurEntity? = dao.getById(id)

    // Recherche par téléphone — utilisé pour éviter les doublons à la création
    suspend fun getByTelephone(telephone: String): ChauffeurEntity? =
        dao.getByTelephone(telephone)

    // Insère un nouveau chauffeur et retourne son ID généré
    suspend fun insert(chauffeur: ChauffeurEntity): Long = dao.insert(chauffeur)

    // Met à jour les informations d'un chauffeur
    suspend fun update(chauffeur: ChauffeurEntity) = dao.update(chauffeur)

    // Change uniquement le statut d'un chauffeur (ex : ACTIF → SUSPENDU)
    suspend fun updateStatut(id: Long, statut: ChauffeurStatut) = dao.updateStatut(id, statut)

    // Supprime un chauffeur (ses trajets auront chauffeurId mis à null)
    suspend fun delete(chauffeur: ChauffeurEntity) = dao.delete(chauffeur)
}
