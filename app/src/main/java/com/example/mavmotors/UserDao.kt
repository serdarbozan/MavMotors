package com.example.mavmotors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("UPDATE users SET darkMode = :darkMode WHERE id = :userId")
    suspend fun updateDarkMode(userId: Int, darkMode: Boolean)

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameTaken(username: String): Int
}