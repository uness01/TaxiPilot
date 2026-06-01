package com.example.taxipilot.core.data.repository

import com.example.taxipilot.core.data.database.dao.ReservationDao
import com.example.taxipilot.core.data.database.entity.ReservationEntity
import com.example.taxipilot.core.data.database.entity.ReservationStatut
import kotlinx.coroutines.flow.Flow

class ReservationRepository(private val dao: ReservationDao) {

    fun getAll(): Flow<List<ReservationEntity>> = dao.getAll()

    fun getEnAttente(): Flow<List<ReservationEntity>> =
        dao.getByStatut(ReservationStatut.EN_ATTENTE)

    fun getByStatut(statut: ReservationStatut): Flow<List<ReservationEntity>> =
        dao.getByStatut(statut)

    fun getByTaxi(taxiId: Long): Flow<List<ReservationEntity>> = dao.getByTaxi(taxiId)

    fun getByChauffeur(chauffeurId: Long): Flow<List<ReservationEntity>> =
        dao.getByChauffeur(chauffeurId)

    fun getByClientId(clientId: Long): Flow<List<ReservationEntity>> =
        dao.getByClientId(clientId)

    fun getByPeriode(debut: Long, fin: Long): Flow<List<ReservationEntity>> =
        dao.getByPeriode(debut, fin)

    suspend fun getById(id: Long): ReservationEntity? = dao.getById(id)

    suspend fun insert(reservation: ReservationEntity): Long = dao.insert(reservation)

    suspend fun update(reservation: ReservationEntity) = dao.update(reservation)

    suspend fun updateStatut(id: Long, statut: ReservationStatut) = dao.updateStatut(id, statut)

    suspend fun confirmer(id: Long, taxiId: Long, chauffeurId: Long) =
        dao.confirmer(id, taxiId, chauffeurId)

    suspend fun delete(reservation: ReservationEntity) = dao.delete(reservation)
}
