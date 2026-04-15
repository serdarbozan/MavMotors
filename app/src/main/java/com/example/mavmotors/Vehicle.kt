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
    val sellerId: Int = 0  // NEW: ID of the user who created this listing
)