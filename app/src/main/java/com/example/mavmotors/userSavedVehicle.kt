package com.example.mavmotors

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "user_saved_vehicles",
    primaryKeys = ["userId", "vehicleId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserSavedVehicle(
    val userId: Int,
    val vehicleId: Int,
    val savedAt: Long = System.currentTimeMillis()
)