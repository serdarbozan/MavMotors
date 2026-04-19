package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    override fun onResume() {
        super.onResume()
        // Reload vehicle data when returning from edit
        if (vehicleId != -1) {
            loadVehicleDetails()
        }
    }

    private fun loadVehicleDetails() {
        lifecycleScope.launch {
            currentVehicle = vehicleDao.getVehicleById(vehicleId)
            currentVehicle?.let { vehicle ->
                findViewById<TextView>(R.id.detailTitle).text = "${vehicle.year} ${vehicle.brand} ${vehicle.model} "
                findViewById<TextView>(R.id.detailType).text = "${vehicle.type}"
                findViewById<TextView>(R.id.detailPrice).text = "$${String.format("%,.2f", vehicle.price)}"
                findViewById<TextView>(R.id.detailMileage).text = "${vehicle.mileage} miles"
                findViewById<TextView>(R.id.detailDescription).text = vehicle.description.ifEmpty { "No description provided." }
                findViewById<TextView>(R.id.detailTransmission).text = vehicle.transmission
                findViewById<TextView>(R.id.detailFuelType).text = vehicle.fuelType
                findViewById<TextView>(R.id.detailColor).text = vehicle.color

                // Load image - check if it's a sample resource or file path
                if (vehicle.isSampleImage && vehicle.imagePath.isNotEmpty()) {
                    val resourceId = resources.getIdentifier(
                        vehicle.imagePath,
                        "drawable",
                        packageName
                    )
                    if (resourceId != 0) {
                        Glide.with(this@VehicleDetailActivity)
                            .load(resourceId)
                            .placeholder(R.drawable.placeholder_car)
                            .error(R.drawable.placeholder_car)
                            .centerCrop()
                            .into(findViewById<ImageView>(R.id.detailImage))
                    }
                } else if (vehicle.imagePath.isNotEmpty()) {
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
                val isSold = vehicle.status == "Sold"
                val addToCartButton = findViewById<Button>(R.id.addToCartButton)
                val buyNowButton = findViewById<Button>(R.id.buyNowButton)
                val editListingButton = findViewById<Button>(R.id.editListingButton)

                if (isSold) {
                    addToCartButton.visibility = android.view.View.VISIBLE
                    addToCartButton.text = "SOLD"
                    addToCartButton.isEnabled = false
                    addToCartButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#888888")
                    )
                    buyNowButton.visibility = android.view.View.GONE
                    editListingButton.visibility = android.view.View.GONE

                } else if (isOwner) {
                    addToCartButton.visibility = android.view.View.GONE
                    buyNowButton.visibility = android.view.View.GONE
                    editListingButton.visibility = android.view.View.VISIBLE
                    editListingButton.setOnClickListener {
                        val intent = Intent(this@VehicleDetailActivity, EditVehicleActivity::class.java)
                        intent.putExtra("VEHICLE_ID", vehicleId)
                        startActivity(intent)
                    }
                } else {
                    editListingButton.visibility = android.view.View.GONE
                    buyNowButton.visibility = android.view.View.VISIBLE
                    addToCartButton.visibility = android.view.View.VISIBLE
                    addToCartButton.text = "Add to Cart"
                    addToCartButton.isEnabled = true
                    addToCartButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(this@VehicleDetailActivity, R.color.main_orange)
                    )

                    buyNowButton.setOnClickListener {
                        val intent = Intent(this@VehicleDetailActivity, PaymentActivity::class.java)
                        intent.putExtra("VEHICLE_ID", vehicleId)
                        startActivity(intent)
                    }

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