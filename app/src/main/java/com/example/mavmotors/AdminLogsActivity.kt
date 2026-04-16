package com.example.mavmotors

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AdminLogsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var currentAdminId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_admin_logs)

        db = DatabaseProvider.getDatabase(this)

        val sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentAdminId = sharedPrefs.getInt("logged_in_user_id", -1)

        val recyclerView = findViewById<RecyclerView>(R.id.rvAdminLogs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAdminLogs)
            .setNavigationOnClickListener { finish() }

        lifecycleScope.launch {
            val admin = db.userDao().getUserById(currentAdminId)
            if (admin == null || admin.role != UserRole.ADMIN) {
                Toast.makeText(this@AdminLogsActivity, "Access denied.", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }

            val logs = db.adminLogDao().getAllLogs()
            recyclerView.adapter = AdminLogsAdapter(logs)
        }
    }
}
