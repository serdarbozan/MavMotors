package com.example.mavmotors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val vehicleId: Int,
    val vehicleName: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val details: String,
    val createdAt: Long = System.currentTimeMillis()
)