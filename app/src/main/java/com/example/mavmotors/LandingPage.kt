package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
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
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme before setting content view
        ThemeManager.applyTheme(this)

        enableEdgeToEdge()
        setContentView(R.layout.landing_page)

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()

        // Setup RecyclerView
        recyclerView = findViewById(R.id.carRender)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        vehicleAdapter = VehicleAdapter(emptyList())
        recyclerView.adapter = vehicleAdapter

        // Add button click listener
        val addButton = findViewById<Button>(R.id.addBtn)
        addButton.setOnClickListener {
            val intent = Intent(this, AddCarActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            insertSampleVehiclesIfEmpty()
            loadTopVehicleTypes()
            loadAllVehicles()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            // Refresh both chips and vehicles when returning to this screen
            loadTopVehicleTypes()
            loadAllVehicles()
        }
    }

    private suspend fun insertSampleVehiclesIfEmpty() {
        val existingVehicles = vehicleDao.getAllVehicles()

        if (existingVehicles.isEmpty()) {
            android.util.Log.d("LandingPage", "Database is empty - adding sample vehicles")

            val sampleVehicles = listOf(
                Vehicle(
                    type = "SUV",
                    price = 32500.0,
                    mileage = 15000,
                    year = 2023,
                    postedDate = System.currentTimeMillis(),
                    status = "Available",
                    imagePath = ""
                ),
                Vehicle(
                    type = "Sedan",
                    price = 28000.0,
                    mileage = 22000,
                    year = 2022,
                    postedDate = System.currentTimeMillis(),
                    status = "Available",
                    imagePath = ""
                )
            )

            for (vehicle in sampleVehicles) {
                vehicleDao.insertVehicle(vehicle)
            }

            android.util.Log.d("LandingPage", "Added ${sampleVehicles.size} sample vehicles")
        } else {
            android.util.Log.d("LandingPage", "Database already has ${existingVehicles.size} vehicles - skipping sample data")
        }
    }

    private suspend fun loadTopVehicleTypes() {
        val topTypes: List<String> = vehicleDao.getTopVehicleTypes()

        withContext(Dispatchers.Main) {
            val filterChips = findViewById<ChipGroup>(R.id.filterChips)

            // Save the currently selected chip text before clearing
            val selectedChipId = filterChips.checkedChipId
            val selectedType = if (selectedChipId != View.NO_ID) {
                val selectedChip = findViewById<Chip>(selectedChipId)
                selectedChip?.text?.toString()
            } else {
                "All"
            }

            filterChips.removeAllViews()

            // Create "All" chip
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

            // Create type chips
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

            // Restore selection or default to "All"
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

            // Add chip click listener
            filterChips.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isEmpty()) {
                    allChip.isChecked = true
                }

                val currentSelectedId = group.checkedChipId
                if (currentSelectedId != View.NO_ID) {
                    val selectedChip = findViewById<Chip>(currentSelectedId)
                    filterVehiclesByType(selectedChip?.text?.toString())
                }
            }
        }
    }

    private fun filterVehiclesByType(type: String?) {
        lifecycleScope.launch {
            val vehicles = withContext(Dispatchers.IO) {
                if (type == null || type == "All") {
                    vehicleDao.getAllVehicles()
                } else {
                    vehicleDao.getVehiclesByType(type)
                }
            }
            withContext(Dispatchers.Main) {
                vehicleAdapter.updateVehicles(vehicles)
            }
        }
    }

    private suspend fun loadAllVehicles() {
        val vehicles = withContext(Dispatchers.IO) {
            vehicleDao.getAllVehicles()
        }

        withContext(Dispatchers.Main) {
            vehicleAdapter.updateVehicles(vehicles)
        }
    }
}