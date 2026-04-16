package com.example.mavmotors

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AddCarActivity : AppCompatActivity() {

    private lateinit var vehicleDao: VehicleDao
    private var selectedImageUri: Uri? = null
    private lateinit var selectedImageView: ImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var imageHintText: TextView
    private lateinit var sharedPrefs: SharedPreferences

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                selectedImageView.visibility = android.view.View.VISIBLE
                cameraIcon.visibility = android.view.View.GONE
                imageHintText.visibility = android.view.View.GONE

                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(selectedImageView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_add_car)

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()
        sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)

        val imagePickerCard = findViewById<CardView>(R.id.imagePickerCard)
        selectedImageView = findViewById(R.id.selectedImageView)
        cameraIcon = findViewById(R.id.cameraIcon)
        imageHintText = findViewById(R.id.imageHintText)

        val typeInput = findViewById<AutoCompleteTextView>(R.id.typeInput)
        val priceInput = findViewById<EditText>(R.id.priceInput)
        val mileageInput = findViewById<EditText>(R.id.mileageInput)
        val yearInput = findViewById<EditText>(R.id.yearInput)
        val transmissionInput = findViewById<AutoCompleteTextView>(R.id.transmissionInput)
        val fuelTypeInput = findViewById<AutoCompleteTextView>(R.id.fuelTypeInput)
        val colorInput = findViewById<EditText>(R.id.colorInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val saveButton = findViewById<Button>(R.id.saveCarButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        // Setup dropdown adapters
        val vehicleTypes = arrayOf("SUV", "Sedan", "Truck", "Coupe", "Convertible", "Hatchback", "Van", "Wagon")
        typeInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, vehicleTypes))

        val transmissions = arrayOf("Automatic", "Manual", "CVT", "DCT")
        transmissionInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, transmissions))

        val fuelTypes = arrayOf("Gasoline", "Diesel", "Electric", "Hybrid", "Plug-in Hybrid")
        fuelTypeInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, fuelTypes))

        imagePickerCard.setOnClickListener {
            openGallery()
        }

        saveButton.setOnClickListener {
            val type = typeInput.text.toString().trim()
            val priceStr = priceInput.text.toString().trim()
            val mileageStr = mileageInput.text.toString().trim()
            val yearStr = yearInput.text.toString().trim()
            val transmission = transmissionInput.text.toString().trim()
            val fuelType = fuelTypeInput.text.toString().trim()
            val color = colorInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            val currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

            if (currentUserId == -1) {
                Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (type.isEmpty() || priceStr.isEmpty() || mileageStr.isEmpty() || yearStr.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            val mileage = mileageStr.toIntOrNull()
            val year = yearStr.toIntOrNull()

            if (price == null || mileage == null || year == null) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val imagePath = saveImageToInternalStorage(selectedImageUri)

                val vehicle = Vehicle(
                    type = type,
                    price = price,
                    mileage = mileage,
                    year = year,
                    postedDate = System.currentTimeMillis(),
                    status = "Available",
                    imagePath = imagePath,
                    sellerId = currentUserId,
                    description = description,
                    transmission = transmission.ifEmpty { "Automatic" },
                    fuelType = fuelType.ifEmpty { "Gasoline" },
                    color = color.ifEmpty { "Black" }
                )

                vehicleDao.insertVehicle(vehicle)

                Toast.makeText(this@AddCarActivity, "Car added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private suspend fun saveImageToInternalStorage(imageUri: Uri?): String {
        if (imageUri == null) return ""

        return withContext(Dispatchers.IO) {
            try {
                val filename = "vehicle_${UUID.randomUUID()}.jpg"
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val imagesDir = File(filesDir, "vehicle_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val imageFile = File(imagesDir, filename)
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

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
}