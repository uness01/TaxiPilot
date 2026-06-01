package com.example.taxipilot.core.data.repository

import com.example.taxipilot.core.data.database.dao.UserDao
import com.example.taxipilot.core.data.database.entity.UserEntity
import com.example.taxipilot.core.data.database.entity.UserRole
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>> = userDao.getUsersByRole(role)

    suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)

    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
}
