package com.example.mavmotors

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: AdminUsersAdapter
    private var currentAdminId: Int = -1
    private var adminUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_admin_users)

        db = DatabaseProvider.getDatabase(this)

        val sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentAdminId = sharedPrefs.getInt("logged_in_user_id", -1)

        val recyclerView = findViewById<RecyclerView>(R.id.rvAdminUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminUsersAdapter(
            emptyList(),
            onSuspendToggle = { user -> confirmToggleSuspend(user) },
            onDelete        = { user -> confirmDelete(user) }
        )
        recyclerView.adapter = adapter

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAdminUsers)
            .setNavigationOnClickListener { finish() }

        verifyAdminAndLoad()
    }

    private fun verifyAdminAndLoad() {
        lifecycleScope.launch {
            val admin = db.userDao().getUserById(currentAdminId)
            if (admin == null || admin.role != UserRole.ADMIN) {
                Toast.makeText(this@AdminUsersActivity, "Access denied.", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            adminUsername = admin.username
            loadUsers()
        }
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val users = db.userDao().getAllUsers()
                .filter { it.id != currentAdminId }
            adapter.updateData(users)
        }
    }

    private fun confirmToggleSuspend(user: User) {
        val action = if (user.status == UserStatus.SUSPENDED) "activate" else "suspend"
        AlertDialog.Builder(this)
            .setTitle("Confirm")
            .setMessage("Are you sure you want to $action ${user.username}?")
            .setPositiveButton("Yes") { _, _ -> toggleSuspend(user) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleSuspend(user: User) {
        lifecycleScope.launch {
            val logAction: String
            if (user.status == UserStatus.SUSPENDED) {
                db.userDao().activateUser(user.id)
                logAction = AdminAction.ACTIVATE_USER
                Toast.makeText(this@AdminUsersActivity, "${user.username} activated.", Toast.LENGTH_SHORT).show()
            } else {
                db.userDao().suspendUser(user.id)
                logAction = AdminAction.SUSPEND_USER
                Toast.makeText(this@AdminUsersActivity, "${user.username} suspended.", Toast.LENGTH_SHORT).show()
            }
            db.adminLogDao().insertLog(
                AdminLog(
                    adminId           = currentAdminId,
                    adminUsername     = adminUsername,
                    action            = logAction,
                    targetId          = user.id,
                    targetDescription = user.username
                )
            )
            loadUsers()
        }
    }

    private fun confirmDelete(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Permanently delete ${user.username}? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteUser(user) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUser(user: User) {
        lifecycleScope.launch {
            db.userDao().deleteUserById(user.id)
            db.adminLogDao().insertLog(
                AdminLog(
                    adminId           = currentAdminId,
                    adminUsername     = adminUsername,
                    action            = AdminAction.DELETE_USER,
                    targetId          = user.id,
                    targetDescription = user.username
                )
            )
            Toast.makeText(this@AdminUsersActivity, "${user.username} deleted.", Toast.LENGTH_SHORT).show()
            loadUsers()
        }
    }
}
