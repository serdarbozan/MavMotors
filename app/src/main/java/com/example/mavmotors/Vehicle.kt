package com.example.mavmotors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val vehicleId: Int = 0,
    val type: String,
    val price: Double,
    val mileage: Int,
    val postedDate: Long,
    val status: String,
    val isActive: Boolean = true
)