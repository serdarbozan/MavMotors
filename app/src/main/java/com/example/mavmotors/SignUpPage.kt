package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SignUpPage : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.signup_page)

        val db = DatabaseProvider.getDatabase(this)
        userDao = db.userDao()

        val emailField = findViewById<EditText>(R.id.emailInput)
        val usernameField = findViewById<EditText>(R.id.usernameInput)
        val passwordField = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordField = findViewById<EditText>(R.id.confirmInput)
        val signUpButton = findViewById<MaterialButton>(R.id.signUpBtn)
        val loginLink = findViewById<TextView>(R.id.loginTxtBtn)

        signUpButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Email must end with @mavs.uta.edu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val existingEmail = userDao.getUserByEmail(email)
                if (existingEmail != null) {
                    Toast.makeText(this@SignUpPage, "Email already registered", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val existingUsername = userDao.getUserByUsername(username)
                if (existingUsername != null) {
                    Toast.makeText(this@SignUpPage, "Username already taken", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val newUser = User(
                    email = email,
                    username = username,
                    password = password,
                    darkMode = true,
                    createdAt = System.currentTimeMillis(),
                    role = UserRole.BUYER,
                    status = UserStatus.ACTIVE
                )

                userDao.insertUser(newUser)

                Toast.makeText(this@SignUpPage, "Account created! Please log in.", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@SignUpPage, LogInPage::class.java)
                startActivity(intent)
                finish()
            }
        }

        loginLink.setOnClickListener {
            val intent = Intent(this, LogInPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.endsWith("@mavs.uta.edu")
    }
}