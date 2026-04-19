package com.example.mavmotors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brand: String,
    val model: String,
    val type: String,
    val price: Double,
    val mileage: Int,
    val year: Int,
    val postedDate: Long,
    val status: String = "Available",  // "Available" or "Sold"
    val imagePath: String = "",
    val sellerId: Int = 0,
    val description: String = "",
    val transmission: String = "Automatic",
    val fuelType: String = "Gasoline",
    val color: String = "Black",
    val isSampleImage: Boolean = false
)
