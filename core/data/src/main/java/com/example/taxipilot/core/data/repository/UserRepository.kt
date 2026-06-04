package com.example.taxipilot.core.data.repository

// Couche Repository pour les utilisateurs locaux (Room/SQLite).
// Le Repository est le seul point d'accès aux données depuis les ViewModels.
// Il fait le pont entre le DAO (accès DB brut) et la logique métier.
// Principe : les ViewModels ne connaissent pas Room, ils utilisent uniquement le Repository.

import com.example.taxipilot.core.data.database.dao.UserDao
import com.example.taxipilot.core.data.database.entity.UserEntity
import com.example.taxipilot.core.data.database.entity.UserRole
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    // Flux de tous les utilisateurs (observable, se met à jour si la DB change)
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    // Flux des utilisateurs filtrés par rôle
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>> = userDao.getUsersByRole(role)

    // Recherche one-shot d'un utilisateur par son ID (retourne null si absent)
    suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)

    // Insère un utilisateur et retourne l'ID généré
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)

    // Met à jour un utilisateur existant
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    // Supprime un utilisateur (ses véhicules et courses sont supprimés en cascade)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
}
