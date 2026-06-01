package com.example.taxipilot.core.data.repository

import com.example.taxipilot.core.data.database.dao.TrajetDao
import com.example.taxipilot.core.data.database.entity.TrajetEntity
import com.example.taxipilot.core.data.database.entity.TrajetStatut
import kotlinx.coroutines.flow.Flow

class TrajetRepository(private val dao: TrajetDao) {

    fun getAll(): Flow<List<TrajetEntity>> = dao.getAll()

    fun getByTaxi(taxiId: Long): Flow<List<TrajetEntity>> = dao.getByTaxi(taxiId)

    fun getByChauffeur(chauffeurId: Long): Flow<List<TrajetEntity>> = dao.getByChauffeur(chauffeurId)

    fun getByStatut(statut: TrajetStatut): Flow<List<TrajetEntity>> = dao.getByStatut(statut)

    suspend fun getById(id: Long): TrajetEntity? = dao.getById(id)

    suspend fun getByReservation(reservationId: Long): TrajetEntity? =
        dao.getByReservation(reservationId)

    suspend fun getTotalRecettesChauffeur(chauffeurId: Long, debut: Long, fin: Long): Double =
        dao.getTotalRecettesChauffeur(chauffeurId, debut, fin)

    suspend fun getTotalRecettesTaxi(taxiId: Long): Double =
        dao.getTotalRecettesTaxi(taxiId)

    suspend fun insert(trajet: TrajetEntity): Long = dao.insert(trajet)

    suspend fun update(trajet: TrajetEntity) = dao.update(trajet)

    suspend fun updateStatut(id: Long, statut: TrajetStatut) = dao.updateStatut(id, statut)

    suspend fun delete(trajet: TrajetEntity) = dao.delete(trajet)
}
