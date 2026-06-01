package com.example.taxipilot.core.data.repository

import com.example.taxipilot.core.data.database.dao.TaxiDao
import com.example.taxipilot.core.data.database.entity.TaxiEntity
import com.example.taxipilot.core.data.database.entity.TaxiStatut
import kotlinx.coroutines.flow.Flow

class TaxiRepository(private val dao: TaxiDao) {

    fun getAll(): Flow<List<TaxiEntity>> = dao.getAll()

    fun getDisponibles(): Flow<List<TaxiEntity>> = dao.getByStatut(TaxiStatut.DISPONIBLE)

    fun getByStatut(statut: TaxiStatut): Flow<List<TaxiEntity>> = dao.getByStatut(statut)

    suspend fun getById(id: Long): TaxiEntity? = dao.getById(id)

    suspend fun getByImmatriculation(immatriculation: String): TaxiEntity? =
        dao.getByImmatriculation(immatriculation)

    suspend fun insert(taxi: TaxiEntity): Long = dao.insert(taxi)

    suspend fun update(taxi: TaxiEntity) = dao.update(taxi)

    suspend fun updateStatut(id: Long, statut: TaxiStatut) = dao.updateStatut(id, statut)

    suspend fun updateKilometrage(id: Long, km: Int) = dao.updateKilometrage(id, km)

    suspend fun delete(taxi: TaxiEntity) = dao.delete(taxi)
}
