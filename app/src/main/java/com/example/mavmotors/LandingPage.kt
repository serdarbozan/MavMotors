package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
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
import android.widget.Spinner
import android.widget.Toast

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
    private var currentSort = "none"
    private var filterMinPrice = 0
    private var filterMaxPrice = Int.MAX_VALUE

    private var filterMinYear = 0
    private var filterMaxYear = Int.MAX_VALUE

    private var filterMinMileage = 0
    private var filterMaxMileage = Int.MAX_VALUE

    private var filterMake: String = "All"
    private var filterModel: String = "All"

    private val filterLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult

                filterMake = data.getStringExtra("MAKE") ?: "All"
                filterModel = data.getStringExtra("MODEL") ?: "All"

                filterMinPrice = data.getIntExtra("MIN_PRICE", 0)
                filterMaxPrice = data.getIntExtra("MAX_PRICE", Int.MAX_VALUE)

                filterMinYear = data.getIntExtra("MIN_YEAR", 0)
                filterMaxYear = data.getIntExtra("MAX_YEAR", Int.MAX_VALUE)

                filterMinMileage = data.getIntExtra("MIN_MILEAGE", 0)
                filterMaxMileage = data.getIntExtra("MAX_MILEAGE", Int.MAX_VALUE)

                filterVehiclesByType(currentFilterType)
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.landing_page)

        val sortSpinner = findViewById<Spinner>(R.id.sortSpinner)
        val sortOptions = arrayOf(
            "Sort/Filter    ",
            "Price: Low → High",
            "Price: High → Low",
            "Year: Newest",
            "Mileage: Low → High",
            "More Filters..."
        )

        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            sortOptions
        )

        adapter.setDropDownViewResource(R.layout.spinner_item)
        sortSpinner.adapter = adapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> { currentSort = "none" }
                    1 -> currentSort = "price_low"
                    2 -> currentSort = "price_high"
                    3 -> currentSort = "year_new"
                    4 -> currentSort = "mileage_low"
                    5 -> {
                        showFilterBottomSheet()
                        sortSpinner.setSelection(0)
                        return
                    }
                }
                filterVehiclesByType(currentFilterType)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

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
        showingMyListings = intent.getBooleanExtra("SHOW_MY_LISTINGS", false)

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
            if (showingMyListings) {
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
            currentSort = "none"
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
                    Toast.makeText(this@LandingPage, "Saved to favorites", Toast.LENGTH_SHORT).show()
                } else {
                    userSavedVehicleDao.unsaveVehicle(currentUserId, vehicle.id)
                    Toast.makeText(this@LandingPage, "Removed from favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun insertSampleVehiclesIfEmpty() {
        if (vehicleDao.getAllVehicles().isEmpty()) {
            listOf(
                Vehicle(brand = "Audi", model = "RS Q8", type = "SUV", price = 32500.0, mileage = 15000, year = 2023,
                    postedDate = System.currentTimeMillis(), status = "Available",
                    description = "Spacious family SUV with leather seats", sellerId = 0,
                    transmission = "Automatic", fuelType = "Gasoline", color = "Black",
                    imagePath = "black_suv", isSampleImage = true),
                Vehicle(brand = "Mazda", model = "Mazda6", type = "Sedan", price = 28000.0, mileage = 22000, year = 2022,
                    postedDate = System.currentTimeMillis(), status = "Available",
                    description = "Fuel-efficient sedan, perfect for commuting", sellerId = 0,
                    transmission = "Automatic", fuelType = "Gasoline", color = "White",
                    imagePath = "white_sedan", isSampleImage = true),
                Vehicle(brand = "Chevrolet" , model = "Silverado", type = "Truck", price = 35000.0, mileage = 18000, year = 2023,
                    postedDate = System.currentTimeMillis(), status = "Available",
                    description = "Heavy-duty truck with towing package", sellerId = 0,
                    transmission = "Automatic", fuelType = "Diesel", color = "Red",
                    imagePath = "red_silverado", isSampleImage = true),
                Vehicle(brand = "Ford", model = "Mustang", type = "Coupe", price = 42000.0, mileage = 8000, year = 2024,
                    postedDate = System.currentTimeMillis(), status = "Available",
                    description = "Sporty coupe with premium sound system", sellerId = 0,
                    transmission = "Manual", fuelType = "Gasoline", color = "Blue",
                    imagePath = "blue_mustang", isSampleImage = true)
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

                val baseList = if (showingMyListings) {
                    if (type == null || type == "All")
                        vehicleDao.getVehiclesBySeller(currentUserId)
                    else
                        vehicleDao.getVehiclesBySellerAndType(currentUserId, type)
                } else {
                    if (type == null || type == "All")
                        vehicleDao.getAllVehicles()
                    else
                        vehicleDao.getVehiclesByType(type)
                }

                baseList.filter { v ->

                    val makeMatch = filterMake == "All" ||
                            v.brand.equals(filterMake, ignoreCase = true)

                    val modelMatch = filterModel == "All" ||
                            v.model.equals(filterModel, ignoreCase = true)
                    v.price in filterMinPrice.toDouble()..filterMaxPrice.toDouble() &&
                            v.year in filterMinYear..filterMaxYear &&
                            v.mileage in filterMinMileage..filterMaxMileage &&
                            makeMatch &&
                            modelMatch
                }
            }

            withContext(Dispatchers.Main) {
                renderVehicles(vehicles)
            }
        }
    }

    private suspend fun loadMyListings() {
        filterVehiclesByType(currentFilterType)
    }

    private suspend fun loadAllVehicles() {
        filterVehiclesByType(currentFilterType)
    }

    private suspend fun searchVehicles(query: String) {
        val vehicles = withContext(Dispatchers.IO) {
            vehicleDao.searchVehicles(query).filter { v ->
                v.price >= filterMinPrice.toDouble() &&
                        v.price <= filterMaxPrice.toDouble() &&
                        v.year in filterMinYear..filterMaxYear &&
                        v.mileage in filterMinMileage..filterMaxMileage
            }
        }

        withContext(Dispatchers.Main) {
            renderVehicles(vehicles)
        }
    }

    private fun showFilterBottomSheet() {
        val intent = Intent(this, FilterActivity::class.java)
        filterLauncher.launch(intent)
    }

    private fun sortVehicles(list: List<Vehicle>): List<Vehicle> {
        return when (currentSort) {
            "price_low" -> list.sortedBy { it.price }
            "price_high" -> list.sortedByDescending { it.price }
            "year_new" -> list.sortedByDescending { it.year }
            "mileage_low" -> list.sortedBy { it.mileage }
            else -> list
        }
    }

    private suspend fun loadSavedStatesForVehicles(vehicles: List<Vehicle>) {
        if (currentUserId == -1) return
        vehicles.forEach { vehicle ->
            vehicleAdapter.setSavedState(vehicle.id, userSavedVehicleDao.isVehicleSaved(currentUserId, vehicle.id))
        }
    }

    private fun applyFilters(maxPrice: Int, maxYear: Int) {
        lifecycleScope.launch {
            val vehicles = withContext(Dispatchers.IO) {
                vehicleDao.getAllVehicles().filter {
                    it.price <= maxPrice && it.year <= maxYear
                }
            }
            withContext(Dispatchers.Main) {
                val sorted = sortVehicles(vehicles)
                vehicleAdapter.updateVehicles(sorted)
                loadSavedStatesForVehicles(sorted)
            }
        }
    }
    private fun renderVehicles(list: List<Vehicle>) {
        val sorted = sortVehicles(list)
        vehicleAdapter.updateVehicles(sorted)
        lifecycleScope.launch {
            loadSavedStatesForVehicles(sorted)
        }
    }
}