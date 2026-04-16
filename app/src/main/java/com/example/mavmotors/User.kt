package com.example.mavmotors

import androidx.room.Entity
import androidx.room.PrimaryKey

object UserRole {
    const val BUYER  = "BUYER"
    const val SELLER = "SELLER"
    const val ADMIN  = "ADMIN"
}

object UserStatus {
    const val ACTIVE    = "ACTIVE"
    const val SUSPENDED = "SUSPENDED"
}

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
    val role: String = UserRole.BUYER,
    val status: String = UserStatus.ACTIVE
)
