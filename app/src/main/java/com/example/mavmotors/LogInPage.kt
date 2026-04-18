package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class LogInPage : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.login_page)

        try {
            val db = DatabaseProvider.getDatabase(this)
            userDao = db.userDao()
        } catch (e: Exception) {
            Log.e("LogInPage", "Database initialization failed", e)
            Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)

        // Create admin account in background (doesn't block UI)
        lifecycleScope.launch {
            try {
                ensureAdminExists()
            } catch (e: Exception) {
                Log.e("LogInPage", "Admin creation failed", e)
            }
        }

        // Check if user is already logged in
        val savedUserId = sharedPrefs.getInt("logged_in_user_id", -1)
        if (savedUserId != -1) {
            lifecycleScope.launch {
                try {
                    val user = userDao.getUserById(savedUserId)
                    if (user != null && user.status == UserStatus.ACTIVE) {
                        // Migrate old global theme preference to this user
                        ThemeManager.migrateGlobalToUserPreference(this@LogInPage)
                        navigateToDestination(user)
                    } else {
                        // Clear invalid saved login
                        sharedPrefs.edit { remove("logged_in_user_id") }
                    }
                } catch (e: Exception) {
                    Log.e("LogInPage", "Auto-login check failed", e)
                    sharedPrefs.edit { remove("logged_in_user_id") }
                }
            }
        }

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<MaterialButton>(R.id.logInBtn)
        val signUpLink = findViewById<TextView>(R.id.gotoSignUp)

        loginButton.setOnClickListener {
            val enteredEmail = emailInput.text.toString().trim()
            val enteredPassword = passwordInput.text.toString().trim()

            if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val user = userDao.login(enteredEmail, enteredPassword)

                    when {
                        user == null -> {
                            Toast.makeText(this@LogInPage, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                        user.status == UserStatus.SUSPENDED -> {
                            Toast.makeText(
                                this@LogInPage,
                                "Your account has been suspended. Please contact support.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        user.status == UserStatus.BANNED -> {
                            Toast.makeText(
                                this@LogInPage,
                                "Your account has been banned.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            sharedPrefs.edit {
                                putInt("logged_in_user_id", user.id)
                            }
                            // Migrate any old global theme preference to this user
                            ThemeManager.migrateGlobalToUserPreference(this@LogInPage)

                            Toast.makeText(this@LogInPage, "Login successful", Toast.LENGTH_SHORT).show()
                            navigateToDestination(user)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LogInPage", "Login failed", e)
                    Toast.makeText(this@LogInPage, "Login error. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpPage::class.java))
            finish()
        }
    }

    private fun navigateToDestination(user: User) {
        if (user.role == UserRole.ADMIN) {
            navigateToAdminPanel()
        } else {
            navigateToLandingPage(user)
        }
    }

    private fun navigateToLandingPage(user: User) {
        val intent = Intent(this, LandingPage::class.java)
        intent.putExtra("USERNAME", user.username)
        intent.putExtra("USER_ID", user.id)
        startActivity(intent)
        finish()
    }

    private fun navigateToAdminPanel() {
        val intent = Intent(this, AdminPanelActivity::class.java)
        startActivity(intent)
        finish()
    }

    private suspend fun ensureAdminExists() {
        val adminEmail = "admin@admin.com"
        val existingAdmin = userDao.getUserByEmail(adminEmail)
        if (existingAdmin == null) {
            val admin = User(
                email = adminEmail,
                username = "Administrator",
                password = "admin",
                role = UserRole.ADMIN,
                status = UserStatus.ACTIVE,
                darkMode = true,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(admin)
            Log.d("LogInPage", "Default admin account created")
        }
    }
}