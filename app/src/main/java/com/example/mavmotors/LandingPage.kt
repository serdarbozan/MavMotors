package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LandingPage : AppCompatActivity() {
    private lateinit var vehicleDao: VehicleDao
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.landing_page)

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()

        recyclerView = findViewById(R.id.carRender)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        vehicleAdapter = VehicleAdapter(emptyList())
        recyclerView.adapter = vehicleAdapter

        val addButton = findViewById<Button>(R.id.addBtn)
        addButton.setOnClickListener {
            val intent = Intent(this, AddCarActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch{
            insertSampleVehicle()
            loadTopVehicleTypes()
            loadAllVehicles()
        }

    }

    override fun onResume(){
        super.onResume()
        lifecycleScope.launch{
            loadAllVehicles()
        }
    }
    private suspend fun insertSampleVehicle(){
        vehicleDao.deleteAllVehicles()
        val newVehicles = listOf(
            Vehicle(type = "SUV", price = 30000.0, mileage = 5000, postedDate = System.currentTimeMillis(), status = "Available", year = 2019),
            Vehicle(type="Sedan", price= 30000.0, mileage = 6000, postedDate = System.currentTimeMillis(), status = "Available", year = 2019),
            Vehicle(type="Convertible", price= 30000.0, mileage = 7000, postedDate = System.currentTimeMillis(), status = "Available", year = 2019),
            Vehicle(type="Convertible", price= 30000.0, mileage = 7000, postedDate = System.currentTimeMillis(), status = "Available", year = 2019),
            Vehicle(type="Convertible", price= 30000.0, mileage = 7000, postedDate = System.currentTimeMillis(), status = "Available", year = 2019)
        )
        for(v in newVehicles){
            vehicleDao.insertVehicle(v)
        }

    }
    private suspend fun loadTopVehicleTypes() {
        val topTypes: List<String> = vehicleDao.getTopVehicleTypes()

        withContext(Dispatchers.Main) {
            val filterChips = findViewById<ChipGroup>(R.id.filterChips)
            filterChips.removeAllViews()

            val allChip = Chip(
                ContextThemeWrapper(filterChips.context, R.style.CustomFilterChip), null, 0).apply {
                text = "All"
                isCheckable = true
            }
            filterChips.addView(allChip)
            for (type in topTypes) {
                val chip = Chip(
                    ContextThemeWrapper(filterChips.context, R.style.CustomFilterChip), null, 0).apply {
                    text = type
                    isCheckable = true
                }
                filterChips.addView(chip)
            }
        }
    }

    private suspend fun loadAllVehicles(){
        val vehicles = withContext(Dispatchers.IO){
            vehicleDao.getAllVehicles()
        }

        withContext(Dispatchers.Main){
            vehicleAdapter.updateVehicles(vehicles)
        }
    }
}