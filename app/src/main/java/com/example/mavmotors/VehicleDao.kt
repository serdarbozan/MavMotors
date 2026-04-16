package com.example.mavmotors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query

@Dao
interface VehicleDao {
    @Insert
    suspend fun insertVehicle(newVehicle: Vehicle)

    @Update
    suspend fun updateVehicle(chosenVehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(chosenVehicle: Vehicle)

    @Query("DELETE FROM vehicles")
    suspend fun deleteAllVehicles()

    @Query("SELECT * FROM vehicles ORDER BY postedDate DESC")
    suspend fun getAllVehicles(): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Int): Vehicle?

    @Query("SELECT * FROM vehicles WHERE type = :type ORDER BY postedDate DESC")
    suspend fun getVehiclesByType(type: String): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE sellerId = :userId ORDER BY postedDate DESC")
    suspend fun getVehiclesBySeller(userId: Int): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE sellerId != :userId ORDER BY postedDate DESC")
    suspend fun getVehiclesFromOtherSellers(userId: Int): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE sellerId = :userId AND type = :type ORDER BY postedDate DESC")
    suspend fun getVehiclesBySellerAndType(userId: Int, type: String): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE sellerId != :userId AND type = :type ORDER BY postedDate DESC")
    suspend fun getVehiclesFromOtherSellersByType(userId: Int, type: String): List<Vehicle>

    // Search query
    @Query("""
        SELECT * FROM vehicles 
        WHERE type LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        OR CAST(year AS TEXT) LIKE '%' || :query || '%'
        ORDER BY postedDate DESC
    """)
    suspend fun searchVehicles(query: String): List<Vehicle>

    @Query("""
    SELECT type
    FROM vehicles
    GROUP BY type
    ORDER BY COUNT(id) DESC
    LIMIT 5
""")
    suspend fun getTopVehicleTypes(): List<String>
}