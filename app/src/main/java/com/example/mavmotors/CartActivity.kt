package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {

    private lateinit var cartDao: CartDao
    private lateinit var cartAdapter: CartAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_cart)

        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        val db = DatabaseProvider.getDatabase(this)
        cartDao = db.cartDao()

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.cartRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(emptyList()) { vehicle ->
            removeFromCart(vehicle)
        }
        recyclerView.adapter = cartAdapter

        findViewById<Button>(R.id.checkoutButton).setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }

        loadCartItems()
    }

    private fun loadCartItems() {
        lifecycleScope.launch {
            val vehicles = cartDao.getCartVehicles(currentUserId)
            cartAdapter.updateVehicles(vehicles)

            findViewById<TextView>(R.id.emptyCartText).visibility =
                if (vehicles.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun removeFromCart(vehicle: Vehicle) {
        lifecycleScope.launch {
            cartDao.removeFromCart(currentUserId, vehicle.id)
            loadCartItems()
            Toast.makeText(this@CartActivity, "Removed from cart", Toast.LENGTH_SHORT).show()
        }
    }
}