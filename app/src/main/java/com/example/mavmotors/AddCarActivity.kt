package com.example.mavmotors

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
        setContentView(R.layout.activity_add_car)

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()

        val imagePickerCard = findViewById<CardView>(R.id.imagePickerCard)
        selectedImageView = findViewById(R.id.selectedImageView)
        cameraIcon = findViewById(R.id.cameraIcon)
        imageHintText = findViewById(R.id.imageHintText)

        val typeInput = findViewById<EditText>(R.id.typeInput)
        val priceInput = findViewById<EditText>(R.id.priceInput)
        val mileageInput = findViewById<EditText>(R.id.mileageInput)
        val yearInput = findViewById<EditText>(R.id.yearInput)
        val saveButton = findViewById<Button>(R.id.saveCarButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        imagePickerCard.setOnClickListener {
            openGallery()
        }

        saveButton.setOnClickListener {
            val type = typeInput.text.toString()
            val price = priceInput.text.toString().toDoubleOrNull()
            val mileage = mileageInput.text.toString().toIntOrNull()
            val year = yearInput.text.toString().toIntOrNull()

            if (type.isNotEmpty() && price != null && mileage != null && year != null) {
                lifecycleScope.launch {
                    // Save image to internal storage and get the path
                    val imagePath = saveImageToInternalStorage(selectedImageUri)

                    val vehicle = Vehicle(
                        type = type,
                        price = price,
                        mileage = mileage,
                        year = year,
                        postedDate = System.currentTimeMillis(),
                        status = "Available",
                        imagePath = imagePath  // Store the local file path
                    )

                    vehicleDao.insertVehicle(vehicle)

                    Toast.makeText(this@AddCarActivity, "Car added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
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
                // Create a unique filename
                val filename = "vehicle_${UUID.randomUUID()}.jpg"

                // Open input stream from the URI
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Create directory if it doesn't exist
                val imagesDir = File(filesDir, "vehicle_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                // Save the image
                val imageFile = File(imagesDir, filename)
                val outputStream = FileOutputStream(imageFile)

                // Compress and save
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

                outputStream.flush()
                outputStream.close()
                inputStream?.close()

                // Return the absolute path
                imageFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}