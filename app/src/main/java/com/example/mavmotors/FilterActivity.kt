package com.example.mavmotors

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import com.google.android.material.slider.RangeSlider
import com.google.android.material.button.MaterialButton

class FilterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_filter)

        val makeSpinner = findViewById<Spinner>(R.id.makeSpinner)
        val modelSpinner = findViewById<Spinner>(R.id.modelSpinner)
        val priceSlider = findViewById<com.google.android.material.slider.RangeSlider>(R.id.priceSlider)
        val yearSlider = findViewById<com.google.android.material.slider.RangeSlider>(R.id.yearSlider)
        val mileageSlider = findViewById<com.google.android.material.slider.RangeSlider>(R.id.mileageSlider)

        priceSlider.valueFrom = 0f
        priceSlider.valueTo = 100000f
        priceSlider.values = listOf(0f, 100000f)

        yearSlider.valueFrom = 2000f
        yearSlider.valueTo = 2026f
        yearSlider.values = listOf(2000f, 2026f)

        mileageSlider.valueFrom = 0f
        mileageSlider.valueTo = 300000f
        mileageSlider.values = listOf(0f, 300000f)

        priceSlider.stepSize = 1000f
        yearSlider.stepSize = 1f
        mileageSlider.stepSize = 1000f

        val applyBtn = findViewById<Button>(R.id.applyFilterBtn)

        applyBtn.setOnClickListener {

            val selectedMake = makeSpinner.selectedItem.toString()
            val selectedModel = modelSpinner.selectedItem.toString()

            val priceValues = priceSlider.values.map { it.toInt() }
            val yearValues = yearSlider.values.map { it.toInt() }
            val mileageValues = mileageSlider.values.map { it.toInt() }

            val resultIntent = Intent().apply {
                putExtra("MAKE", selectedMake)
                putExtra("MODEL", selectedModel)

                putExtra("MIN_PRICE", priceValues[0])
                putExtra("MAX_PRICE", priceValues[1])

                putExtra("MIN_YEAR", yearValues[0])
                putExtra("MAX_YEAR", yearValues[1])

                putExtra("MIN_MILEAGE", mileageValues[0])
                putExtra("MAX_MILEAGE", mileageValues[1])
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }

        val resetBtn = findViewById<MaterialButton>(R.id.resetFilterBtn)

        resetBtn.setOnClickListener {

            // Reset spinners
            makeSpinner.setSelection(0)
            modelSpinner.setSelection(0)

            // Reset sliders
            priceSlider.values = listOf(0f, 100000f)
            yearSlider.values = listOf(2000f, 2026f)
            mileageSlider.values = listOf(0f, 300000f)

            val resultIntent = Intent().apply {
                putExtra("MAKE", "All")
                putExtra("MODEL", "All")

                putExtra("MIN_PRICE", 0)
                putExtra("MAX_PRICE", 100000)

                putExtra("MIN_YEAR", 2000)
                putExtra("MAX_YEAR", 2026)

                putExtra("MIN_MILEAGE", 0)
                putExtra("MAX_MILEAGE", 300000)
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
        val modelsMap = mapOf(

            "Audi" to listOf("All", "A3", "A4", "A6", "Q3", "Q5", "Q7", "RS Q8"),

            "BMW" to listOf("All", "3 Series", "5 Series", "7 Series", "X3", "X5", "X7"),

            "Cadillac" to listOf("All", "CT4", "CT5", "Escalade", "XT4", "XT5", "XT6"),

            "Chevrolet" to listOf("All", "Malibu", "Impala", "Camaro", "Silverado", "Equinox", "Tahoe"),

            "Ford" to listOf("All", "F-150", "Mustang", "Explorer", "Escape", "Edge", "Focus"),

            "GMC" to listOf("All", "Sierra", "Terrain", "Acadia", "Yukon"),

            "Honda" to listOf("All", "Civic", "Accord", "CR-V", "Pilot", "HR-V"),

            "Hyundai" to listOf("All", "Elantra", "Sonata", "Tucson", "Santa Fe", "Kona"),

            "Jaguar" to listOf("All", "XE", "XF", "XJ", "F-PACE"),

            "Jeep" to listOf("All", "Wrangler", "Grand Cherokee", "Cherokee", "Compass"),

            "Kia" to listOf("All", "Forte", "Optima", "Sportage", "Sorento", "Telluride"),

            "Land Rover" to listOf("All", "Range Rover", "Discovery", "Defender", "Evoque"),

            "Lincoln" to listOf("All", "Navigator", "Aviator", "Corsair", "Nautilus"),

            "Mazda" to listOf("All", "Mazda3", "Mazda6", "CX-3", "CX-5", "CX-9"),

            "Mercedes-Benz" to listOf("All", "A-Class", "C-Class", "E-Class", "S-Class", "GLA", "GLC", "GLE"),

            "Nissan" to listOf("All", "Altima", "Sentra", "Maxima", "Rogue", "Pathfinder"),

            "Renault" to listOf("All", "Clio", "Megane", "Captur", "Kadjar"),

            "Subaru" to listOf("All", "Impreza", "Legacy", "Outback", "Forester", "Crosstrek"),

            "Tesla" to listOf("All", "Model 3", "Model S", "Model X", "Model Y"),

            "Toyota" to listOf("All", "Corolla", "Camry", "Prius", "RAV4", "Highlander", "Tacoma"),

            "Volkswagen" to listOf("All", "Golf", "Passat", "Jetta", "Tiguan", "Atlas")
        )

        val makes = mutableListOf("All").apply {
            addAll(modelsMap.keys.sorted())
        }

        val makeAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            makes
        )
        makeAdapter.setDropDownViewResource(R.layout.spinner_item)
        makeSpinner.adapter = makeAdapter

        makeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMake = makes[position]
                val models = modelsMap[selectedMake] ?: listOf("All")

                val modelAdapter = ArrayAdapter(
                    this@FilterActivity,
                    R.layout.spinner_item,
                    models
                )

                modelAdapter.setDropDownViewResource(R.layout.spinner_item)
                modelSpinner.adapter = modelAdapter
                modelSpinner.setSelection(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}