package com.example.taxipilot.core.data.database.dao

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.ChauffeurEntity
import com.example.taxipilot.core.data.database.entity.ChauffeurStatut
import kotlinx.coroutines.flow.Flow

@Dao
interface ChauffeurDao {

    @Query("SELECT * FROM chauffeurs ORDER BY nom, prenom")
    fun getAll(): Flow<List<ChauffeurEntity>>

    @Query("SELECT * FROM chauffeurs WHERE statut = :statut ORDER BY nom, prenom")
    fun getByStatut(statut: ChauffeurStatut): Flow<List<ChauffeurEntity>>

    @Query("SELECT * FROM chauffeurs WHERE id = :id")
    suspend fun getById(id: Long): ChauffeurEntity?

    @Query("SELECT * FROM chauffeurs WHERE telephone = :telephone LIMIT 1")
    suspend fun getByTelephone(telephone: String): ChauffeurEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(chauffeur: ChauffeurEntity): Long

    @Update
    suspend fun update(chauffeur: ChauffeurEntity)

    @Query("UPDATE chauffeurs SET statut = :statut WHERE id = :id")
    suspend fun updateStatut(id: Long, statut: ChauffeurStatut)

    @Delete
    suspend fun delete(chauffeur: ChauffeurEntity)
}
