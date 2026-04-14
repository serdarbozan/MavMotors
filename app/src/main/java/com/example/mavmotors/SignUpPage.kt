package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SignUpPage : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup_page)

        val emailField = findViewById<EditText>(R.id.editText)
        val usernameField = findViewById<EditText>(R.id.editText2)
        val passwordField = findViewById<EditText>(R.id.editText3)
        val confirmPasswordField = findViewById<EditText>(R.id.editText4)
        val signUpButton = findViewById<MaterialButton>(R.id.button)

        val prefs: SharedPreferences = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)

        signUpButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())
            {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            else if (password != confirmPassword)
            {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else
            {
                prefs.edit()
                    .putString("saved_email", email)
                    .putString("saved_username", username)
                    .putString("saved_password", password)
                    .apply()

                Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, LogInPage::class.java))
                finish()
            }
        }
    }
}