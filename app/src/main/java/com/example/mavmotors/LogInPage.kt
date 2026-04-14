package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LogInPage : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_page)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp)

        val prefs: SharedPreferences = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)

        btnLogin.setOnClickListener {
            val enteredEmail = etEmail.text.toString().trim()
            val enteredPassword = etPassword.text.toString().trim()

            val savedEmail = prefs.getString("saved_email", "")
            val savedPassword = prefs.getString("saved_password", "")

            if (enteredEmail.isEmpty() || enteredPassword.isEmpty())
            {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
            else if (enteredEmail == savedEmail && enteredPassword == savedPassword)
            {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LandingPage::class.java))
                finish()
            }
            else
            {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpPage::class.java))
        }
    }
}