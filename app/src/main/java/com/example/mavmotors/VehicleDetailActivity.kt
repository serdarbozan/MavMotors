package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File

class VehicleDetailActivity : AppCompatActivity() {

    private lateinit var vehicleDao: VehicleDao
    private lateinit var cartDao: CartDao
    private lateinit var userSavedVehicleDao: UserSavedVehicleDao
    private lateinit var sharedPrefs: SharedPreferences
    private var currentUserId: Int = -1
    private var vehicleId: Int = -1
    private var currentVehicle: Vehicle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_vehicle_detail)

        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)
        vehicleId = intent.getIntExtra("VEHICLE_ID", -1)

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()
        cartDao = db.cartDao()
        userSavedVehicleDao = db.userSavedVehicleDao()

        if (vehicleId == -1) {
            finish()
            return
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        loadVehicleDetails()
    }

    private fun loadVehicleDetails() {
        lifecycleScope.launch {
            currentVehicle = vehicleDao.getVehicleById(vehicleId)
            currentVehicle?.let { vehicle ->
                findViewById<TextView>(R.id.detailTitle).text = "${vehicle.year} ${vehicle.type}"
                findViewById<TextView>(R.id.detailPrice).text = "$${String.format("%,.2f", vehicle.price)}"
                findViewById<TextView>(R.id.detailMileage).text = "${vehicle.mileage} miles"
                findViewById<TextView>(R.id.detailDescription).text = vehicle.description.ifEmpty { "No description provided." }
                findViewById<TextView>(R.id.detailTransmission).text = vehicle.transmission
                findViewById<TextView>(R.id.detailFuelType).text = vehicle.fuelType
                findViewById<TextView>(R.id.detailColor).text = vehicle.color

                if (vehicle.imagePath.isNotEmpty()) {
                    val imageFile = File(vehicle.imagePath)
                    if (imageFile.exists()) {
                        Glide.with(this@VehicleDetailActivity)
                            .load(imageFile)
                            .placeholder(R.drawable.placeholder_car)
                            .error(R.drawable.placeholder_car)
                            .centerCrop()
                            .into(findViewById<ImageView>(R.id.detailImage))
                    }
                }

                val isOwner = vehicle.sellerId == currentUserId
                val addToCartButton = findViewById<Button>(R.id.addToCartButton)
                val editListingButton = findViewById<Button>(R.id.editListingButton)

                if (isOwner) {
                    // Owner - hide add to cart, show edit
                    addToCartButton.visibility = android.view.View.GONE
                    editListingButton.visibility = android.view.View.VISIBLE
                    editListingButton.setOnClickListener {
                        val intent = Intent(this@VehicleDetailActivity, EditVehicleActivity::class.java)
                        intent.putExtra("VEHICLE_ID", vehicleId)
                        startActivity(intent)
                    }
                } else {
                    // Buyer - show add to cart, hide edit
                    editListingButton.visibility = android.view.View.GONE
                    addToCartButton.visibility = android.view.View.VISIBLE

                    val isInCart = cartDao.isInCart(currentUserId, vehicleId)
                    if (isInCart) {
                        addToCartButton.text = "In Cart"
                        addToCartButton.isEnabled = false
                    } else {
                        addToCartButton.setOnClickListener {
                            lifecycleScope.launch {
                                cartDao.addToCart(CartItem(userId = currentUserId, vehicleId = vehicleId))
                                Toast.makeText(this@VehicleDetailActivity, "Added to cart", Toast.LENGTH_SHORT).show()
                                addToCartButton.text = "In Cart"
                                addToCartButton.isEnabled = false
                            }
                        }
                    }
                }
            }
        }
    }
}