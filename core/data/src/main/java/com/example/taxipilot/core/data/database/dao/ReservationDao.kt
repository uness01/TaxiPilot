package com.example.taxipilot.core.data.database.dao

// DAO pour la table "reservations" — réservations locales Room (ancienne architecture).
// Dans le flux actuel, FirestoreReservationRepository est la source de vérité.
// La méthode confirmer() est un raccourci SQL pour assigner taxi + chauffeur en une seule requête.

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.ReservationEntity
import com.example.taxipilot.core.data.database.entity.ReservationStatut
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {

    // Toutes les réservations triées par heure de prise en charge (la plus proche d'abord)
    @Query("SELECT * FROM reservations ORDER BY datePickup ASC")
    fun getAll(): Flow<List<ReservationEntity>>

    // Réservations filtrées par statut (ex : EN_ATTENTE uniquement)
    @Query("SELECT * FROM reservations WHERE statut = :statut ORDER BY datePickup ASC")
    fun getByStatut(statut: ReservationStatut): Flow<List<ReservationEntity>>

    // Réservations assignées à un taxi précis
    @Query("SELECT * FROM reservations WHERE taxiId = :taxiId ORDER BY datePickup ASC")
    fun getByTaxi(taxiId: Long): Flow<List<ReservationEntity>>

    // Réservations assignées à un chauffeur précis
    @Query("SELECT * FROM reservations WHERE chauffeurId = :chauffeurId ORDER BY datePickup ASC")
    fun getByChauffeur(chauffeurId: Long): Flow<List<ReservationEntity>>

    // Réservations passées par un client précis (historique du passager)
    @Query("SELECT * FROM reservations WHERE clientId = :clientId ORDER BY datePickup DESC")
    fun getByClientId(clientId: Long): Flow<List<ReservationEntity>>

    // Réservations dans une fenêtre temporelle [debut, fin] (pour planning quotidien/hebdo)
    @Query("SELECT * FROM reservations WHERE datePickup BETWEEN :debut AND :fin ORDER BY datePickup ASC")
    fun getByPeriode(debut: Long, fin: Long): Flow<List<ReservationEntity>>

    // Recherche d'une réservation précise par ID (one-shot)
    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun getById(id: Long): ReservationEntity?

    // Insère une nouvelle réservation (ABORT si conflit)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reservation: ReservationEntity): Long

    // Met à jour une réservation existante
    @Update
    suspend fun update(reservation: ReservationEntity)

    // Mise à jour rapide du statut uniquement
    @Query("UPDATE reservations SET statut = :statut WHERE id = :id")
    suspend fun updateStatut(id: Long, statut: ReservationStatut)

    // Raccourci : assigne taxi + chauffeur et passe le statut à CONFIRMEE en une seule requête SQL
    @Query("UPDATE reservations SET taxiId = :taxiId, chauffeurId = :chauffeurId, statut = 'CONFIRMEE' WHERE id = :id")
    suspend fun confirmer(id: Long, taxiId: Long, chauffeurId: Long)

    // Supprime une réservation
    @Delete
    suspend fun delete(reservation: ReservationEntity)
}
