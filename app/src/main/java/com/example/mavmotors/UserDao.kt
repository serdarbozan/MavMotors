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

    // Admin functions
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<User>

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: Int, status: UserStatus)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: Int, role: UserRole)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)

    @Query("UPDATE users SET status = 'SUSPENDED' WHERE id = :userId")
    suspend fun suspendUser(userId: Int)

    @Query("UPDATE users SET status = 'ACTIVE' WHERE id = :userId")
    suspend fun activateUser(userId: Int)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)
}