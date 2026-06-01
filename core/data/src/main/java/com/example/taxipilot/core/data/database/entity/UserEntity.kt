package com.example.taxipilot.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole { OWNER, DRIVER, CLIENT }

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)
