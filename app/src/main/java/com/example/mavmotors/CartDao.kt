package com.example.mavmotors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CartDao {

    @Insert
    suspend fun addToCart(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND vehicleId = :vehicleId")
    suspend fun removeFromCart(userId: Int, vehicleId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM cart_items WHERE userId = :userId AND vehicleId = :vehicleId)")
    suspend fun isInCart(userId: Int, vehicleId: Int): Boolean

    @Query("SELECT * FROM vehicles WHERE id IN (SELECT vehicleId FROM cart_items WHERE userId = :userId)")
    suspend fun getCartVehicles(userId: Int): List<Vehicle>

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Int)
}