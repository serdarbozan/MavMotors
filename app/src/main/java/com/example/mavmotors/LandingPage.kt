package com.example.mavmotors

import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LandingPage : AppCompatActivity() {
    private lateinit var vehicleDao: VehicleDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.landing_page)

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()

        lifecycleScope.launch{
            insertSampleVehicle()
            loadTopVehicleTypes()
            loadAllVehicles()
        }
    }
    private suspend fun insertSampleVehicle(){
        vehicleDao.deleteAllVehicles()
        val newVehicles = listOf(
            Vehicle(type = "SUV", price = 30000.0, mileage = 5000, postedDate = System.currentTimeMillis(), status = "Available"),
            Vehicle(type="Sedan", price= 30000.0, mileage = 6000, postedDate = System.currentTimeMillis(), status = "Available"),
            Vehicle(type="Convertible", price= 30000.0, mileage = 7000, postedDate = System.currentTimeMillis(), status = "Available")
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

    }
}