package com.example.mavmotors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val username: String,
    val password: String,
    val darkMode: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val avatarPath: String = "",
    val role: UserRole = UserRole.BUYER,
    val status: UserStatus = UserStatus.ACTIVE
)

enum class UserRole {
    BUYER, SELLER, ADMIN
}

enum class UserStatus {
    ACTIVE, SUSPENDED, BANNED
}