package com.example.mavmotors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val price: Double,
    val mileage: Int,
    val year: Int,
    val postedDate: Long,
    val status: String,
    val imagePath: String = "",
    val sellerId: Int = 0,
    val description: String = "",           // NEW
    val transmission: String = "Automatic", // NEW
    val fuelType: String = "Gasoline",      // NEW
    val color: String = "Black"             // NEW
)