package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LandingPage : AppCompatActivity() {
    private lateinit var vehicleDao: VehicleDao
    private lateinit var userSavedVehicleDao: UserSavedVehicleDao
    private lateinit var userDao: UserDao
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPrefs: SharedPreferences

    private var currentUserId: Int = -1
    private var currentUser: User? = null
    private var showingMyListings: Boolean = false
    private var currentFilterType: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.landing_page)

        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()
        userSavedVehicleDao = db.userSavedVehicleDao()
        userDao = db.userDao()

        lifecycleScope.launch {
            currentUser = userDao.getUserById(currentUserId)
            updateUsernameDisplay()
            loadUserAvatar()
        }

        val showMyListings = intent.getBooleanExtra("SHOW_MY_LISTINGS", false)

        recyclerView = findViewById(R.id.carRender)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        vehicleAdapter = VehicleAdapter(emptyList()) { vehicle, isSaved ->
            handleHeartClick(vehicle, isSaved)
        }
        recyclerView.adapter = vehicleAdapter

        findViewById<MaterialButton>(R.id.addBtn).setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }

        findViewById<ImageView>(R.id.profileAvatar).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        setupBottomNavigation()
        setupSearch()

        lifecycleScope.launch {
            insertSampleVehiclesIfEmpty()
            loadTopVehicleTypes()
            if (showMyListings) {
                showingMyListings = true
                loadMyListings()
            } else {
                loadAllVehicles()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            currentUser = userDao.getUserById(currentUserId)
            updateUsernameDisplay()
            loadUserAvatar()
            loadTopVehicleTypes()
            if (showingMyListings) {
                loadMyListings()
            } else {
                loadAllVehicles()
            }
        }
    }

    private fun updateUsernameDisplay() {
        currentUser?.let {
            findViewById<TextView>(R.id.usernameDisplay).text = it.username
        }
    }

    private fun loadUserAvatar() {
        currentUser?.let { user ->
            if (user.avatarPath.isNotEmpty()) {
                val avatarFile = File(user.avatarPath)
                if (avatarFile.exists()) {
                    Glide.with(this)
                        .load(avatarFile)
                        .circleCrop()
                        .placeholder(R.drawable.default_avatar)
                        .into(findViewById<ImageView>(R.id.profileAvatar))
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        findViewById<TextView>(R.id.homeBtn).setOnClickListener {
            showingMyListings = false
            currentFilterType = "All"
            lifecycleScope.launch {
                loadTopVehicleTypes()
                loadAllVehicles()
            }
        }

        findViewById<TextView>(R.id.savedBtn).setOnClickListener {
            startActivity(Intent(this, SavedVehiclesActivity::class.java))
        }

        findViewById<TextView>(R.id.sellTxt).setOnClickListener {
            startActivity(Intent(this, MyListingsActivity::class.java))
        }

        findViewById<TextView>(R.id.cartBtn).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        findViewById<TextView>(R.id.ordersBtn).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
    }

    private fun setupSearch() {
        val searchInput = findViewById<EditText>(R.id.searchInput)
        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = searchInput.text.toString().trim()
            lifecycleScope.launch {
                if (query.isNotEmpty()) {
                    searchVehicles(query)
                } else {
                    loadAllVehicles()
                }
            }
            true
        }
    }

    private fun handleHeartClick(vehicle: Vehicle, isSaved: Boolean) {
        lifecycleScope.launch {
            if (currentUserId != -1) {
                if (isSaved) {
                    userSavedVehicleDao.saveVehicle(UserSavedVehicle(currentUserId, vehicle.id))
                } else {
                    userSavedVehicleDao.unsaveVehicle(currentUserId, vehicle.id)
                }
            }
        }
    }

    private suspend fun insertSampleVehiclesIfEmpty() {
        if (vehicleDao.getAllVehicles().isEmpty()) {
            listOf(
                Vehicle(type = "SUV", price = 32500.0, mileage = 15000, year = 2023,
                    postedDate = System.currentTimeMillis(), status = "Available",
                    description = "Spacious family SUV with leather seats", sellerId = 0),
                Vehicle(type = "Sedan", price = 28000.0, mileage = 22000, year = 2022,
                    postedDate = System.currentTimeMillis(), status = "Available",
                    description = "Fuel-efficient sedan, perfect for commuting", sellerId = 0)
            ).forEach { vehicleDao.insertVehicle(it) }
        }
    }

    private suspend fun loadTopVehicleTypes() {
        val topTypes = vehicleDao.getTopVehicleTypes()
        withContext(Dispatchers.Main) {
            val filterChips = findViewById<ChipGroup>(R.id.filterChips)
            val selectedType = currentFilterType
            filterChips.removeAllViews()

            val allChip = Chip(ContextThemeWrapper(filterChips.context, R.style.CustomFilterChip), null, 0).apply {
                text = "All"
                isCheckable = true
                id = View.generateViewId()
            }
            filterChips.addView(allChip)

            topTypes.forEach { type ->
                filterChips.addView(Chip(ContextThemeWrapper(filterChips.context, R.style.CustomFilterChip), null, 0).apply {
                    text = type
                    isCheckable = true
                    id = View.generateViewId()
                })
            }

            if (selectedType == "All") {
                allChip.isChecked = true
            } else {
                for (i in 0 until filterChips.childCount) {
                    (filterChips.getChildAt(i) as? Chip)?.let {
                        if (it.text == selectedType) {
                            it.isChecked = true
                            return@withContext
                        }
                    }
                }
                allChip.isChecked = true
            }

            filterChips.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isEmpty()) allChip.isChecked = true
                val selectedChip = findViewById<Chip>(group.checkedChipId)
                currentFilterType = selectedChip?.text?.toString() ?: "All"
                filterVehiclesByType(currentFilterType)
            }
        }
    }

    private fun filterVehiclesByType(type: String?) {
        currentFilterType = type ?: "All"
        lifecycleScope.launch {
            val vehicles = withContext(Dispatchers.IO) {
                if (showingMyListings) {
                    if (type == null || type == "All") vehicleDao.getVehiclesBySeller(currentUserId)
                    else vehicleDao.getVehiclesBySellerAndType(currentUserId, type)
                } else {
                    if (type == null || type == "All") vehicleDao.getVehiclesFromOtherSellers(currentUserId)
                    else vehicleDao.getVehiclesFromOtherSellersByType(currentUserId, type)
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
            if (currentUserId != -1) vehicleDao.getVehiclesFromOtherSellers(currentUserId)
            else vehicleDao.getAllVehicles()
        }
        withContext(Dispatchers.Main) {
            vehicleAdapter.updateVehicles(vehicles)
            loadSavedStatesForVehicles(vehicles)
        }
    }

    private suspend fun loadMyListings() {
        val vehicles = withContext(Dispatchers.IO) {
            if (currentUserId != -1) vehicleDao.getVehiclesBySeller(currentUserId)
            else emptyList()
        }
        withContext(Dispatchers.Main) {
            vehicleAdapter.updateVehicles(vehicles)
            loadSavedStatesForVehicles(vehicles)
        }
    }

    private suspend fun searchVehicles(query: String) {
        val vehicles = withContext(Dispatchers.IO) {
            val allResults = vehicleDao.searchVehicles(query)
            if (showingMyListings) {
                allResults.filter { it.sellerId == currentUserId }
            } else {
                allResults.filter { it.sellerId != currentUserId || it.sellerId == 0 }
            }
        }
        withContext(Dispatchers.Main) {
            vehicleAdapter.updateVehicles(vehicles)
            loadSavedStatesForVehicles(vehicles)
        }
    }

    private suspend fun loadSavedStatesForVehicles(vehicles: List<Vehicle>) {
        if (currentUserId == -1) return
        vehicles.forEach { vehicle ->
            vehicleAdapter.setSavedState(vehicle.id, userSavedVehicleDao.isVehicleSaved(currentUserId, vehicle.id))
        }
    }
}