package com.example.mavmotors

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedVehiclesActivity : AppCompatActivity() {

    private lateinit var userSavedVehicleDao: UserSavedVehicleDao
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var emptyStateText: TextView

    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager.applyTheme(this)
        setContentView(R.layout.saved_vehicles)

        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        val db = DatabaseProvider.getDatabase(this)
        userSavedVehicleDao = db.userSavedVehicleDao()

        recyclerView = findViewById(R.id.savedRecyclerView)
        emptyStateText = findViewById(R.id.emptyStateText)
        val backButton = findViewById<ImageView>(R.id.backButton)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        vehicleAdapter = VehicleAdapter(emptyList()) { vehicle, isSaved ->
            handleHeartClick(vehicle, isSaved)
        }
        recyclerView.adapter = vehicleAdapter

        // Make sure back button is clickable and on top
        backButton.apply {
            bringToFront()
            isClickable = true
            isFocusable = true
            setOnClickListener {
                finish()
            }
        }

        loadSavedVehicles()
    }

    override fun onResume() {
        super.onResume()
        if (currentUserId != -1) {
            loadSavedVehicles()
        }
    }

    private fun handleHeartClick(vehicle: Vehicle, isSaved: Boolean) {
        lifecycleScope.launch {
            if (currentUserId != -1) {
                if (isSaved) {
                    val saved = UserSavedVehicle(
                        userId = currentUserId,
                        vehicleId = vehicle.id
                    )
                    userSavedVehicleDao.saveVehicle(saved)
                    Toast.makeText(this@SavedVehiclesActivity, "Saved to favorites", Toast.LENGTH_SHORT).show()
                } else {
                    userSavedVehicleDao.unsaveVehicle(currentUserId, vehicle.id)
                    Toast.makeText(this@SavedVehiclesActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    loadSavedVehicles()
                }
            }
        }
    }

    private fun loadSavedVehicles() {
        lifecycleScope.launch {
            val vehicles = withContext(Dispatchers.IO) {
                if (currentUserId != -1) {
                    userSavedVehicleDao.getSavedVehiclesForUser(currentUserId)
                } else {
                    emptyList()
                }
            }

            withContext(Dispatchers.Main) {
                vehicleAdapter.updateVehicles(vehicles)

                vehicles.forEach { vehicle ->
                    vehicleAdapter.setSavedState(vehicle.id, true)
                }

                if (vehicles.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateText.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyStateText.visibility = View.GONE
                }
            }
        }
    }
}