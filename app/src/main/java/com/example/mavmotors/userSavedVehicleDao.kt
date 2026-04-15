package com.example.mavmotors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserSavedVehicleDao {

    @Insert
    suspend fun saveVehicle(userSavedVehicle: UserSavedVehicle)

    @Query("DELETE FROM user_saved_vehicles WHERE userId = :userId AND vehicleId = :vehicleId")
    suspend fun unsaveVehicle(userId: Int, vehicleId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM user_saved_vehicles WHERE userId = :userId AND vehicleId = :vehicleId)")
    suspend fun isVehicleSaved(userId: Int, vehicleId: Int): Boolean

    @Query("SELECT * FROM vehicles WHERE id IN (SELECT vehicleId FROM user_saved_vehicles WHERE userId = :userId) ORDER BY postedDate DESC")
    suspend fun getSavedVehiclesForUser(userId: Int): List<Vehicle>

    @Query("DELETE FROM user_saved_vehicles WHERE userId = :userId")
    suspend fun deleteAllSavedForUser(userId: Int)
}