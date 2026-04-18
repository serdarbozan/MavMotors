package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReceiptActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_receipt)

        val vehicleText = findViewById<TextView>(R.id.receiptVehicleText)
        val priceText = findViewById<TextView>(R.id.receiptPriceText)
        val methodText = findViewById<TextView>(R.id.receiptMethodText)
        val detailsText = findViewById<TextView>(R.id.receiptDetailsText)
        val doneButton = findViewById<Button>(R.id.receiptDoneButton)

        val vehicleName = intent.getStringExtra("vehicleName") ?: "Unknown Vehicle"
        val vehiclePrice = intent.getDoubleExtra("vehiclePrice", 0.0)
        val paymentMethod = intent.getStringExtra("paymentMethod") ?: "Unknown"
        val details = intent.getStringExtra("details") ?: "No additional details"

        vehicleText.text = "Vehicle: $vehicleName"
        priceText.text = "Price: $" + String.format("%,.2f", vehiclePrice)
        methodText.text = "Payment Method: $paymentMethod"
        detailsText.text = "Details: $details"

        doneButton.setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}