package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SettingsActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var currentUser: User

    private lateinit var avatarImage: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var emailTextView: TextView
    private lateinit var darkModeSwitch: Switch
    private lateinit var saveProfileButton: Button
    private lateinit var myListingsButton: TextView
    private lateinit var savedVehiclesButton: TextView
    private lateinit var logoutButton: TextView

    private var selectedAvatarUri: Uri? = null
    private var tempDarkMode: Boolean = true

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedAvatarUri = uri
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(avatarImage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        val userId = sharedPrefs.getInt("logged_in_user_id", -1)

        val db = DatabaseProvider.getDatabase(this)
        userDao = db.userDao()

        avatarImage = findViewById(R.id.avatarImage)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailTextView = findViewById(R.id.emailTextView)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        saveProfileButton = findViewById(R.id.saveProfileButton)
        myListingsButton = findViewById(R.id.myListingsButton)
        savedVehiclesButton = findViewById(R.id.savedVehiclesButton)
        logoutButton = findViewById(R.id.logoutButton)
        val backButton = findViewById<ImageView>(R.id.backButton)

        lifecycleScope.launch {
            currentUser = userDao.getUserById(userId) ?: return@launch

            emailTextView.text = currentUser.email
            usernameEditText.setText(currentUser.username)

            tempDarkMode = ThemeManager.isDarkMode(this@SettingsActivity)
            darkModeSwitch.isChecked = tempDarkMode

            if (currentUser.avatarPath.isNotEmpty()) {
                val avatarFile = File(currentUser.avatarPath)
                if (avatarFile.exists()) {
                    Glide.with(this@SettingsActivity)
                        .load(avatarFile)
                        .circleCrop()
                        .placeholder(R.drawable.default_avatar)
                        .into(avatarImage)
                }
            }
        }

        avatarImage.setOnClickListener {
            openGallery()
        }

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            tempDarkMode = isChecked
            ThemeManager.saveThemePreference(this, isChecked)
            ThemeManager.applyTheme(this)
            recreate()
        }

        saveProfileButton.setOnClickListener {
            val newUsername = usernameEditText.text.toString().trim()
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                var avatarPath = currentUser.avatarPath
                if (selectedAvatarUri != null) {
                    avatarPath = saveAvatarToInternalStorage(selectedAvatarUri)
                }

                val updatedUser = currentUser.copy(
                    username = newUsername,
                    avatarPath = avatarPath,
                    darkMode = tempDarkMode
                )

                userDao.updateUser(updatedUser)

                // Save the theme preference permanently for this user
                ThemeManager.saveThemePreference(this@SettingsActivity, tempDarkMode)

                Toast.makeText(this@SettingsActivity, "Profile updated!", Toast.LENGTH_SHORT).show()

                // Restart activity to apply the new theme cleanly
                finish()
                startActivity(intent)
            }
        }

        myListingsButton.setOnClickListener {
            val intent = Intent(this, MyListingsActivity::class.java)
            startActivity(intent)
        }

        savedVehiclesButton.setOnClickListener {
            val intent = Intent(this, SavedVehiclesActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            sharedPrefs.edit().remove("logged_in_user_id").apply()
            val intent = Intent(this, LogInPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun saveAvatarToInternalStorage(imageUri: Uri?): String {
        if (imageUri == null) return ""

        return try {
            val filename = "avatar_${UUID.randomUUID()}.jpg"
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

            val imagesDir = File(filesDir, "avatars")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val imageFile = File(imagesDir, filename)
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream?.close()

            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}