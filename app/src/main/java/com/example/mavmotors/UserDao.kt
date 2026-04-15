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

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("UPDATE users SET darkMode = :darkMode WHERE id = :userId")
    suspend fun updateDarkMode(userId: Int, darkMode: Boolean)

    @Query("SELECT * FROM vehicles WHERE sellerId = :userId ORDER BY postedDate DESC")
    suspend fun getVehiclesBySeller(userId: Int): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE sellerId != :userId ORDER BY postedDate DESC")
    suspend fun getVehiclesFromOtherSellers(userId: Int): List<Vehicle>
}