package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var currentAdminId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_admin_panel)

        db = DatabaseProvider.getDatabase(this)

        val sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentAdminId = sharedPrefs.getInt("logged_in_user_id", -1)

        if (!isAuthorizedAdmin()) return

        loadStats()

        // Changed from Button to LinearLayout (matching the new design)
        findViewById<android.widget.LinearLayout>(R.id.btnManageUsers).setOnClickListener {
            startActivity(Intent(this, AdminUsersActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.btnManageListings).setOnClickListener {
            startActivity(Intent(this, AdminListingsActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.btnViewLogs).setOnClickListener {
            startActivity(Intent(this, AdminLogsActivity::class.java))
        }

        // Logout button is still a MaterialButton
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAdminLogout).setOnClickListener {
            sharedPrefs.edit().remove("logged_in_user_id").apply()
            startActivity(Intent(this, LogInPage::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun isAuthorizedAdmin(): Boolean {
        if (currentAdminId == -1) {
            deny(); return false
        }
        return true
    }

    private fun deny() {
        Toast.makeText(this, "Access denied: Admins only.", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun loadStats() {
        lifecycleScope.launch {
            val admin = db.userDao().getUserById(currentAdminId)

            if (admin == null || admin.role != UserRole.ADMIN) {
                deny(); return@launch
            }

            val totalUsers    = db.userDao().getAllUsers().size
            val totalListings = db.vehicleDao().getAllVehicles().size

            findViewById<TextView>(R.id.tvAdminWelcome).text =
                "Welcome, ${admin.username}"
            findViewById<TextView>(R.id.tvTotalUsersValue).text =
                totalUsers.toString()
            findViewById<TextView>(R.id.tvTotalListingsValue).text =
                totalListings.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }
}