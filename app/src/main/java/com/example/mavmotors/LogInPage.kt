package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LogInPage : AppCompatActivity()
{
    private lateinit var userDao: UserDao
    private lateinit var sharedPrefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_page)

        val db = DatabaseProvider.getDatabase(this)
        userDao = db.userDao()
        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp)

        btnLogin.setOnClickListener {
            val enteredEmail = etEmail.text.toString().trim()
            val enteredPassword = etPassword.text.toString().trim()

            if (enteredEmail.isEmpty() || enteredPassword.isEmpty())
            {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = userDao.login(enteredEmail, enteredPassword)

                if (user != null)
                {
                    sharedPrefs.edit {
                        putInt("logged_in_user_id", user.id)
                    }

                    Toast.makeText(this@LogInPage, "Login successful", Toast.LENGTH_SHORT).show()
                    navigateToLandingPage(user)
                }
                else
                {
                    Toast.makeText(this@LogInPage, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpPage::class.java))
        }
    }

    private fun navigateToLandingPage(user: User)
    {
        val intent = Intent(this, LandingPage::class.java)
        intent.putExtra("USERNAME", user.username)
        intent.putExtra("USER_ID", user.id)
        startActivity(intent)
        finish()
    }
}