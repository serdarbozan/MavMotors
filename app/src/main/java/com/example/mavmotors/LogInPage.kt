package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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

        val db = DatabaseProvider.getDatabase(this)
        userDao = db.userDao()
        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)

        // Check if user is already logged in
        val savedUserId = sharedPrefs.getInt("logged_in_user_id", -1)
        if (savedUserId != -1) {
            lifecycleScope.launch {
                val user = userDao.getUserById(savedUserId)
                if (user != null) {
                    navigateToLandingPage(user)
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
                val user = userDao.login(enteredEmail, enteredPassword)

                if (user != null) {
                    sharedPrefs.edit {
                        putInt("logged_in_user_id", user.id)
                    }
                    Toast.makeText(this@LogInPage, "Login successful", Toast.LENGTH_SHORT).show()
                    navigateToLandingPage(user)
                } else {
                    Toast.makeText(this@LogInPage, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpPage::class.java))
            finish()
        }
    }

    private fun navigateToLandingPage(user: User) {
        val intent = Intent(this, LandingPage::class.java)
        intent.putExtra("USERNAME", user.username)
        intent.putExtra("USER_ID", user.id)
        startActivity(intent)
        finish()
    }
}