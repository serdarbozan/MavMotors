package com.example.mavmotors

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AdminListingsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: AdminListingsAdapter
    private var currentAdminId: Int = -1
    private var adminUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_admin_listings)

        db = DatabaseProvider.getDatabase(this)

        val sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentAdminId = sharedPrefs.getInt("logged_in_user_id", -1)

        val recyclerView = findViewById<RecyclerView>(R.id.rvAdminListings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminListingsAdapter(
            emptyList(),
            emptyMap(),
            onDelete = { vehicle -> confirmDelete(vehicle) }
        )
        recyclerView.adapter = adapter

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAdminListings)
            .setNavigationOnClickListener { finish() }

        verifyAdminAndLoad()
    }

    private fun verifyAdminAndLoad() {
        lifecycleScope.launch {
            val admin = db.userDao().getUserById(currentAdminId)
            if (admin == null || admin.role != UserRole.ADMIN) {
                Toast.makeText(this@AdminListingsActivity, "Access denied.", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            adminUsername = admin.username
            loadListings()
        }
    }

    private fun loadListings() {
        lifecycleScope.launch {
            val vehicles  = db.vehicleDao().getAllVehicles()
            val allUsers  = db.userDao().getAllUsers()
            val sellerMap = allUsers.associate { it.id to it.username }
            adapter.updateData(vehicles, sellerMap)
        }
    }

    private fun confirmDelete(vehicle: Vehicle) {
        AlertDialog.Builder(this)
            .setTitle("Delete Listing")
            .setMessage("Remove the listing for '${vehicle.type} (${vehicle.year})'?")
            .setPositiveButton("Delete") { _, _ -> deleteListing(vehicle) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteListing(vehicle: Vehicle) {
        lifecycleScope.launch {
            db.vehicleDao().deleteVehicle(vehicle)
            db.adminLogDao().insertLog(
                AdminLog(
                    adminId           = currentAdminId,
                    adminUsername     = adminUsername,
                    action            = AdminAction.DELETE_LISTING,
                    targetId          = vehicle.id,
                    targetDescription = "${vehicle.type} ${vehicle.year}"
                )
            )
            Toast.makeText(
                this@AdminListingsActivity,
                "Listing removed.",
                Toast.LENGTH_SHORT
            ).show()
            loadListings()
        }
    }
}
