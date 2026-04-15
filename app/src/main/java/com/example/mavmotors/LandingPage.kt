package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LandingPage : AppCompatActivity() {
    private lateinit var vehicleDao: VehicleDao
    private lateinit var userSavedVehicleDao: UserSavedVehicleDao
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPrefs: SharedPreferences

    private var currentUserId: Int = -1
    private var showingMyListings: Boolean = false
    private var currentFilterType: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager.applyTheme(this)
        setContentView(R.layout.landing_page)

        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        val menuBtn = findViewById<ImageView>(R.id.menuBtn)
        menuBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val username = intent.getStringExtra("USERNAME") ?: "User"
        findViewById<TextView>(R.id.usernameDisplay).text = username

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()
        userSavedVehicleDao = db.userSavedVehicleDao()

        recyclerView = findViewById(R.id.carRender)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        vehicleAdapter = VehicleAdapter(emptyList()) { vehicle, isSaved ->
            handleHeartClick(vehicle, isSaved)
        }
        recyclerView.adapter = vehicleAdapter

        val addButton = findViewById<Button>(R.id.addBtn)
        addButton.setOnClickListener {
            val intent = Intent(this, AddCarActivity::class.java)
            startActivity(intent)
        }

        setupBottomNavigation()

        lifecycleScope.launch {
            insertSampleVehiclesIfEmpty()
            loadTopVehicleTypes()
            loadAllVehicles()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            loadTopVehicleTypes()
            loadAllVehicles()
        }
    }

    private fun setupBottomNavigation() {
        val homeBtn = findViewById<TextView>(R.id.homeBtn)
        val savedBtn = findViewById<TextView>(R.id.savedBtn)
        val sellTxt = findViewById<TextView>(R.id.sellTxt)
        val logoutBtn = findViewById<TextView>(R.id.logoutBtn)

        homeBtn.setOnClickListener {
            showingMyListings = false
            currentFilterType = "All"
            lifecycleScope.launch {
                loadTopVehicleTypes()
                loadAllVehicles()
            }
        }

        savedBtn.setOnClickListener {
            val intent = Intent(this, SavedVehiclesActivity::class.java)
            startActivity(intent)
        }

        sellTxt.setOnClickListener {
            showingMyListings = true
            currentFilterType = "All"
            lifecycleScope.launch {
                val filterChips = findViewById<ChipGroup>(R.id.filterChips)
                for (i in 0 until filterChips.childCount) {
                    val child = filterChips.getChildAt(i)
                    if (child is Chip && child.text == "All") {
                        child.isChecked = true
                        break
                    }
                }
                loadMyListings()
            }
        }

        logoutBtn.setOnClickListener {
            sharedPrefs.edit().remove("logged_in_user_id").apply()
            val intent = Intent(this, LogInPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
                    Toast.makeText(this@LandingPage, "Saved to favorites", Toast.LENGTH_SHORT).show()
                } else {
                    userSavedVehicleDao.unsaveVehicle(currentUserId, vehicle.id)
                    Toast.makeText(this@LandingPage, "Removed from favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun insertSampleVehiclesIfEmpty() {
        val existingVehicles = vehicleDao.getAllVehicles()

        if (existingVehicles.isEmpty()) {
            val sampleVehicles = listOf(
                Vehicle(
                    type = "SUV",
                    price = 32500.0,
                    mileage = 15000,
                    year = 2023,
                    postedDate = System.currentTimeMillis(),
                    status = "Available",
                    imagePath = "",
                    sellerId = 0
                ),
                Vehicle(
                    type = "Sedan",
                    price = 28000.0,
                    mileage = 22000,
                    year = 2022,
                    postedDate = System.currentTimeMillis(),
                    status = "Available",
                    imagePath = "",
                    sellerId = 0
                )
            )

            for (vehicle in sampleVehicles) {
                vehicleDao.insertVehicle(vehicle)
            }
        }
    }

    private suspend fun loadTopVehicleTypes() {
        val topTypes: List<String> = vehicleDao.getTopVehicleTypes()

        withContext(Dispatchers.Main) {
            val filterChips = findViewById<ChipGroup>(R.id.filterChips)
            val selectedChipId = filterChips.checkedChipId
            val selectedType = if (selectedChipId != View.NO_ID) {
                val selectedChip = findViewById<Chip>(selectedChipId)
                selectedChip?.text?.toString()
            } else {
                currentFilterType
            }

            filterChips.removeAllViews()

            val allChip = Chip(
                ContextThemeWrapper(filterChips.context, R.style.CustomFilterChip),
                null,
                0
            ).apply {
                text = "All"
                isCheckable = true
                id = View.generateViewId()
            }
            filterChips.addView(allChip)

            for (type in topTypes) {
                val chip = Chip(
                    ContextThemeWrapper(filterChips.context, R.style.CustomFilterChip),
                    null,
                    0
                ).apply {
                    text = type
                    isCheckable = true
                    id = View.generateViewId()
                }
                filterChips.addView(chip)
            }

            if (selectedType == "All") {
                allChip.isChecked = true
            } else {
                var found = false
                for (i in 0 until filterChips.childCount) {
                    val child = filterChips.getChildAt(i)
                    if (child is Chip && child.text?.toString() == selectedType) {
                        child.isChecked = true
                        found = true
                        break
                    }
                }
                if (!found) {
                    allChip.isChecked = true
                }
            }

            filterChips.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isEmpty()) {
                    allChip.isChecked = true
                }
                val currentSelectedId = group.checkedChipId
                if (currentSelectedId != View.NO_ID) {
                    val selectedChip = findViewById<Chip>(currentSelectedId)
                    currentFilterType = selectedChip?.text?.toString() ?: "All"
                    filterVehiclesByType(currentFilterType)
                }
            }
        }
    }

    private fun filterVehiclesByType(type: String?) {
        currentFilterType = type ?: "All"
        lifecycleScope.launch {
            val vehicles = withContext(Dispatchers.IO) {
                if (showingMyListings) {
                    if (type == null || type == "All") {
                        vehicleDao.getVehiclesBySeller(currentUserId)
                    } else {
                        vehicleDao.getVehiclesBySellerAndType(currentUserId, type)
                    }
                } else {
                    if (type == null || type == "All") {
                        vehicleDao.getVehiclesFromOtherSellers(currentUserId)
                    } else {
                        vehicleDao.getVehiclesFromOtherSellersByType(currentUserId, type)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                vehicleAdapter.updateVehicles(vehicles)
                loadSavedStatesForVehicles(vehicles)
            }
        }
    }

    private suspend fun loadAllVehicles() {
        val vehicles = withContext(Dispatchers.IO) {
            if (currentUserId != -1) {
                vehicleDao.getVehiclesFromOtherSellers(currentUserId)
            } else {
                vehicleDao.getAllVehicles()
            }
        }

        withContext(Dispatchers.Main) {
            vehicleAdapter.updateVehicles(vehicles)
            loadSavedStatesForVehicles(vehicles)
        }
    }

    private suspend fun loadMyListings() {
        val vehicles = withContext(Dispatchers.IO) {
            if (currentUserId != -1) {
                vehicleDao.getVehiclesBySeller(currentUserId)
            } else {
                emptyList()
            }
        }

        withContext(Dispatchers.Main) {
            vehicleAdapter.updateVehicles(vehicles)
            loadSavedStatesForVehicles(vehicles)
        }
    }

    private suspend fun loadSavedStatesForVehicles(vehicles: List<Vehicle>) {
        if (currentUserId == -1) return

        for (vehicle in vehicles) {
            val isSaved = userSavedVehicleDao.isVehicleSaved(currentUserId, vehicle.id)
            vehicleAdapter.setSavedState(vehicle.id, isSaved)
        }
    }
}