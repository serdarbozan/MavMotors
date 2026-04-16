package com.example.mavmotors

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File

class EditVehicleActivity : AppCompatActivity() {

    private lateinit var vehicleDao: VehicleDao
    private var vehicleId: Int = -1
    private var currentVehicle: Vehicle? = null
    private var selectedImageUri: Uri? = null
    private var imageChanged = false

    private lateinit var vehicleImage: ImageView
    private lateinit var typeInput: AutoCompleteTextView
    private lateinit var priceInput: EditText
    private lateinit var mileageInput: EditText
    private lateinit var yearInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var transmissionInput: AutoCompleteTextView
    private lateinit var fuelTypeInput: AutoCompleteTextView
    private lateinit var colorInput: EditText

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                imageChanged = true
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(vehicleImage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_edit_vehicle)

        vehicleId = intent.getIntExtra("VEHICLE_ID", -1)
        if (vehicleId == -1) {
            finish()
            return
        }

        val db = DatabaseProvider.getDatabase(this)
        vehicleDao = db.vehicleDao()

        vehicleImage = findViewById(R.id.editVehicleImage)
        typeInput = findViewById(R.id.editTypeInput)
        priceInput = findViewById(R.id.editPriceInput)
        mileageInput = findViewById(R.id.editMileageInput)
        yearInput = findViewById(R.id.editYearInput)
        descriptionInput = findViewById(R.id.editDescriptionInput)
        transmissionInput = findViewById(R.id.editTransmissionInput)
        fuelTypeInput = findViewById(R.id.editFuelTypeInput)
        colorInput = findViewById(R.id.editColorInput)

        // Setup dropdowns
        val types = arrayOf("SUV", "Sedan", "Truck", "Coupe", "Convertible", "Hatchback", "Van", "Wagon")
        typeInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, types))

        val transmissions = arrayOf("Automatic", "Manual", "CVT", "DCT")
        transmissionInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, transmissions))

        val fuelTypes = arrayOf("Gasoline", "Diesel", "Electric", "Hybrid", "Plug-in Hybrid")
        fuelTypeInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, fuelTypes))

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        vehicleImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        loadVehicleData()

        findViewById<Button>(R.id.saveChangesButton).setOnClickListener {
            saveChanges()
        }

        findViewById<Button>(R.id.deleteListingButton).setOnClickListener {
            deleteListing()
        }
    }

    private fun loadVehicleData() {
        lifecycleScope.launch {
            currentVehicle = vehicleDao.getVehicleById(vehicleId)
            currentVehicle?.let { vehicle ->
                typeInput.setText(vehicle.type)
                priceInput.setText(vehicle.price.toInt().toString())
                mileageInput.setText(vehicle.mileage.toString())
                yearInput.setText(vehicle.year.toString())
                descriptionInput.setText(vehicle.description)
                transmissionInput.setText(vehicle.transmission)
                fuelTypeInput.setText(vehicle.fuelType)
                colorInput.setText(vehicle.color)

                if (vehicle.imagePath.isNotEmpty()) {
                    val imageFile = File(vehicle.imagePath)
                    if (imageFile.exists()) {
                        Glide.with(this@EditVehicleActivity)
                            .load(imageFile)
                            .placeholder(R.drawable.placeholder_car)
                            .error(R.drawable.placeholder_car)
                            .centerCrop()
                            .into(vehicleImage)
                    }
                }
            }
        }
    }

    private fun saveChanges() {
        val type = typeInput.text.toString().trim()
        val priceStr = priceInput.text.toString().trim()
        val mileageStr = mileageInput.text.toString().trim()
        val yearStr = yearInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val transmission = transmissionInput.text.toString().trim()
        val fuelType = fuelTypeInput.text.toString().trim()
        val color = colorInput.text.toString().trim()

        if (type.isEmpty() || priceStr.isEmpty() || mileageStr.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        val mileage = mileageStr.toIntOrNull()
        val year = yearStr.toIntOrNull()

        if (price == null || mileage == null || year == null) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            currentVehicle?.let { vehicle ->
                val updatedVehicle = vehicle.copy(
                    type = type,
                    price = price,
                    mileage = mileage,
                    year = year,
                    description = description,
                    transmission = transmission.ifEmpty { "Automatic" },
                    fuelType = fuelType.ifEmpty { "Gasoline" },
                    color = color.ifEmpty { "Black" }
                )

                vehicleDao.updateVehicle(updatedVehicle)
                Toast.makeText(this@EditVehicleActivity, "Listing updated!", Toast.LENGTH_SHORT).show()

                // Return to VehicleDetailActivity and refresh
                val intent = Intent(this@EditVehicleActivity, VehicleDetailActivity::class.java)
                intent.putExtra("VEHICLE_ID", vehicleId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    private fun deleteListing() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Listing")
            .setMessage("Are you sure you want to delete this listing?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    currentVehicle?.let { vehicle ->
                        vehicleDao.deleteVehicle(vehicle)
                        Toast.makeText(this@EditVehicleActivity, "Listing deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}