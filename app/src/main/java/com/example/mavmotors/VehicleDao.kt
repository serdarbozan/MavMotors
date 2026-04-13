package com.example.mavmotors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query

@Dao
interface VehicleDao{
   @Insert
   suspend fun insertVehicle(newVehicle: Vehicle)

   @Update
   suspend fun updateVehicle(chosenVehicle: Vehicle)

   @Delete
   suspend fun deleteVehicle(chosenVehicle: Vehicle)

   @Query("DELETE FROM vehicles")
   suspend fun deleteAllVehicles()

   @Query("SELECT * FROM vehicles")
   suspend fun getAllVehicles(): List<Vehicle>

   @Query("""
       SELECT type
       FROM vehicles
       GROUP BY type
       ORDER BY COUNT(vehicleId) DESC
       LIMIT 3
       """)
   suspend fun getTopVehicleTypes(): List<String>

}