package com.example.taxipilot.core.data.repository

// Repository pour les taxis (Room/SQLite).
// Expose les opérations CRUD et des raccourcis métier comme getDisponibles().
// Utilisé par OwnerViewModel (gestion de flotte) et DriverViewModel (statut du taxi assigné).

import com.example.taxipilot.core.data.database.dao.TaxiDao
import com.example.taxipilot.core.data.database.entity.TaxiEntity
import com.example.taxipilot.core.data.database.entity.TaxiStatut
import kotlinx.coroutines.flow.Flow

class TaxiRepository(private val dao: TaxiDao) {

    // Tous les taxis de la flotte (flux observable)
    fun getAll(): Flow<List<TaxiEntity>> = dao.getAll()

    // Raccourci : uniquement les taxis disponibles (statut = DISPONIBLE)
    fun getDisponibles(): Flow<List<TaxiEntity>> = dao.getByStatut(TaxiStatut.DISPONIBLE)

    // Taxis filtrés par statut (DISPONIBLE, ASSIGNE, EN_COURSE, EN_MAINTENANCE)
    fun getByStatut(statut: TaxiStatut): Flow<List<TaxiEntity>> = dao.getByStatut(statut)

    // Recherche one-shot par ID
    suspend fun getById(id: Long): TaxiEntity? = dao.getById(id)

    // Recherche par immatriculation — utilisé pour lier un chauffeur Firestore à un taxi Room
    suspend fun getByImmatriculation(immatriculation: String): TaxiEntity? =
        dao.getByImmatriculation(immatriculation)

    // Ajoute un taxi à la flotte et retourne l'ID généré
    suspend fun insert(taxi: TaxiEntity): Long = dao.insert(taxi)

    // Met à jour tous les champs d'un taxi
    suspend fun update(taxi: TaxiEntity) = dao.update(taxi)

    // Change uniquement le statut d'un taxi (opération fréquente lors des courses)
    suspend fun updateStatut(id: Long, statut: TaxiStatut) = dao.updateStatut(id, statut)

    // Met à jour le kilométrage d'un taxi
    suspend fun updateKilometrage(id: Long, km: Int) = dao.updateKilometrage(id, km)

    // Supprime un taxi de la flotte (ses charges sont supprimées en cascade)
    suspend fun delete(taxi: TaxiEntity) = dao.delete(taxi)
}
