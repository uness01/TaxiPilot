package com.example.taxipilot.core.data.repository

import com.example.taxipilot.core.data.database.dao.ChauffeurDao
import com.example.taxipilot.core.data.database.entity.ChauffeurEntity
import com.example.taxipilot.core.data.database.entity.ChauffeurStatut
import kotlinx.coroutines.flow.Flow

class ChauffeurRepository(private val dao: ChauffeurDao) {

    fun getAll(): Flow<List<ChauffeurEntity>> = dao.getAll()

    fun getActifs(): Flow<List<ChauffeurEntity>> = dao.getByStatut(ChauffeurStatut.ACTIF)

    fun getByStatut(statut: ChauffeurStatut): Flow<List<ChauffeurEntity>> = dao.getByStatut(statut)

    suspend fun getById(id: Long): ChauffeurEntity? = dao.getById(id)

    suspend fun getByTelephone(telephone: String): ChauffeurEntity? =
        dao.getByTelephone(telephone)

    suspend fun insert(chauffeur: ChauffeurEntity): Long = dao.insert(chauffeur)

    suspend fun update(chauffeur: ChauffeurEntity) = dao.update(chauffeur)

    suspend fun updateStatut(id: Long, statut: ChauffeurStatut) = dao.updateStatut(id, statut)

    suspend fun delete(chauffeur: ChauffeurEntity) = dao.delete(chauffeur)
}
