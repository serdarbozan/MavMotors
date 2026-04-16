package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyListingsActivity : AppCompatActivity() {

    private lateinit var vehicleDao: VehicleDao
    private lateinit var listingsAdapter: MyListingsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var emptyStateText: TextView

    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_my_listings)

        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()

        recyclerView = findViewById(R.id.listingsRecyclerView)
        emptyStateText = findViewById(R.id.emptyStateText)
        val backButton = findViewById<ImageView>(R.id.backButton)
        val addButton = findViewById<MaterialButton>(R.id.addListingButton)

        // Use LinearLayoutManager for vertical list (not grid like LandingPage)
        recyclerView.layoutManager = LinearLayoutManager(this)

        listingsAdapter = MyListingsAdapter(
            emptyList(),
            onEditClick = { vehicle -> editListing(vehicle) },
            onDeleteClick = { vehicle -> confirmDelete(vehicle) },
            onItemClick = { vehicle -> viewListing(vehicle) }
        )
        recyclerView.adapter = listingsAdapter

        backButton.setOnClickListener { finish() }

        addButton.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }

        loadMyListings()
    }

    override fun onResume() {
        super.onResume()
        loadMyListings()
    }

    private fun loadMyListings() {
        lifecycleScope.launch {
            val vehicles = withContext(Dispatchers.IO) {
                if (currentUserId != -1) {
                    vehicleDao.getVehiclesBySeller(currentUserId)
                } else {
                    emptyList()
                }
            }

            withContext(Dispatchers.Main) {
                listingsAdapter.updateVehicles(vehicles)

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

    private fun editListing(vehicle: Vehicle) {
        val intent = Intent(this, EditVehicleActivity::class.java)
        intent.putExtra("VEHICLE_ID", vehicle.id)
        startActivity(intent)
    }

    private fun confirmDelete(vehicle: Vehicle) {
        AlertDialog.Builder(this)
            .setTitle("Delete Listing")
            .setMessage("Are you sure you want to delete this listing?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    vehicleDao.deleteVehicle(vehicle)
                    Toast.makeText(this@MyListingsActivity, "Listing deleted", Toast.LENGTH_SHORT).show()
                    loadMyListings()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun viewListing(vehicle: Vehicle) {
        val intent = Intent(this, VehicleDetailActivity::class.java)
        intent.putExtra("VEHICLE_ID", vehicle.id)
        startActivity(intent)
    }
}