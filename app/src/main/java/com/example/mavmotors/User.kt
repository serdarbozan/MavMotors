package com.example.mavmotors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String, // In production, store hashed passwords!
    val darkMode: Boolean = true, // Default to dark mode
    val createdAt: Long = System.currentTimeMillis()
)