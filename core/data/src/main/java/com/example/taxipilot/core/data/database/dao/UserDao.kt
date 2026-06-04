package com.example.taxipilot.core.data.database.dao

// DAO (Data Access Object) pour la table "users".
// Interface Room : chaque fonction génère automatiquement le SQL correspondant.
// Les fonctions retournant Flow sont observables en temps réel (mises à jour automatiques).
// Les fonctions suspend doivent être appelées depuis une coroutine (pas sur le thread principal).

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.UserEntity
import com.example.taxipilot.core.data.database.entity.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Retourne tous les utilisateurs sous forme de flux observable (se met à jour si la table change)
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    // Filtre les utilisateurs par rôle (OWNER, DRIVER, CLIENT)
    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    // Recherche un utilisateur précis par son ID (retourne null si introuvable)
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    // Insère un utilisateur ; si l'ID existe déjà, remplace l'enregistrement (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    // Met à jour tous les champs d'un utilisateur existant (identifié par son id)
    @Update
    suspend fun updateUser(user: UserEntity)

    // Supprime un utilisateur (en cascade : ses véhicules et courses sont aussi supprimés)
    @Delete
    suspend fun deleteUser(user: UserEntity)
}
