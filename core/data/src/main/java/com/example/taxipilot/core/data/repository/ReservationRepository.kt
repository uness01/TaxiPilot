package com.example.taxipilot.core.data.repository

// Repository pour les réservations locales Room (ancienne architecture).
// Dans le flux actuel, FirestoreReservationRepository est la source de vérité pour les réservations.
// Ce repository reste pour la compatibilité de compilation.

import com.example.taxipilot.core.data.database.dao.ReservationDao
import com.example.taxipilot.core.data.database.entity.ReservationEntity
import com.example.taxipilot.core.data.database.entity.ReservationStatut
import kotlinx.coroutines.flow.Flow

class ReservationRepository(private val dao: ReservationDao) {

    // Toutes les réservations (flux observable, triées par heure de pickup)
    fun getAll(): Flow<List<ReservationEntity>> = dao.getAll()

    // Raccourci : uniquement les réservations en attente
    fun getEnAttente(): Flow<List<ReservationEntity>> =
        dao.getByStatut(ReservationStatut.EN_ATTENTE)

    // Réservations filtrées par statut
    fun getByStatut(statut: ReservationStatut): Flow<List<ReservationEntity>> =
        dao.getByStatut(statut)

    // Réservations d'un taxi précis
    fun getByTaxi(taxiId: Long): Flow<List<ReservationEntity>> = dao.getByTaxi(taxiId)

    // Réservations d'un chauffeur précis
    fun getByChauffeur(chauffeurId: Long): Flow<List<ReservationEntity>> =
        dao.getByChauffeur(chauffeurId)

    // Réservations d'un client précis (historique passager)
    fun getByClientId(clientId: Long): Flow<List<ReservationEntity>> =
        dao.getByClientId(clientId)

    // Réservations dans une fenêtre temporelle (planning journalier/hebdomadaire)
    fun getByPeriode(debut: Long, fin: Long): Flow<List<ReservationEntity>> =
        dao.getByPeriode(debut, fin)

    // Recherche one-shot par ID
    suspend fun getById(id: Long): ReservationEntity? = dao.getById(id)

    // Insère une réservation et retourne l'ID généré
    suspend fun insert(reservation: ReservationEntity): Long = dao.insert(reservation)

    // Met à jour une réservation existante
    suspend fun update(reservation: ReservationEntity) = dao.update(reservation)

    // Change uniquement le statut d'une réservation
    suspend fun updateStatut(id: Long, statut: ReservationStatut) = dao.updateStatut(id, statut)

    // Confirme une réservation : assigne taxi + chauffeur et passe le statut à CONFIRMEE
    suspend fun confirmer(id: Long, taxiId: Long, chauffeurId: Long) =
        dao.confirmer(id, taxiId, chauffeurId)

    // Supprime une réservation
    suspend fun delete(reservation: ReservationEntity) = dao.delete(reservation)
}
