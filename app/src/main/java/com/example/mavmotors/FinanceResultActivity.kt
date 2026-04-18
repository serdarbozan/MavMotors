package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class FinanceResultActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_finance_result)

        val approved = intent.getBooleanExtra("approved", false)
        val apr = intent.getDoubleExtra("apr", 0.0)
        val monthlyPayment = intent.getDoubleExtra("monthlyPayment", 0.0)
        val reason = intent.getStringExtra("reason") ?: "No reason provided"

        val vehicleId = intent.getIntExtra("vehicleId", 0)
        val vehicleName = intent.getStringExtra("vehicleName") ?: "Unknown Vehicle"
        val vehiclePrice = intent.getDoubleExtra("vehiclePrice", 0.0)
        val downPayment = intent.getDoubleExtra("downPayment", 0.0)
        val termMonths = intent.getIntExtra("termMonths", 0)

        val resultStatusText = findViewById<TextView>(R.id.resultStatusText)
        val aprText = findViewById<TextView>(R.id.aprText)
        val monthlyPaymentText = findViewById<TextView>(R.id.monthlyPaymentText)
        val reasonText = findViewById<TextView>(R.id.reasonText)
        val doneButton = findViewById<Button>(R.id.doneButton)

        if (approved)
        {
            resultStatusText.text = "Approved"
            aprText.text = "APR: " + String.format("%.2f", apr) + "%"
            monthlyPaymentText.text =
                "Estimated Monthly Payment: $" + String.format("%.2f", monthlyPayment)
            doneButton.text = "View Receipt"
        }
        else
        {
            resultStatusText.text = "Rejected"
            aprText.text = "APR: --"
            monthlyPaymentText.text = "Estimated Monthly Payment: --"
            doneButton.text = "Done"
        }

        reasonText.text = "Reason: $reason"

        doneButton.setOnClickListener {
            if (approved)
            {
                val sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
                val currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

                val db = DatabaseProvider.getDatabase(this)
                val vehicleDao = db.vehicleDao()
                val cartDao = db.cartDao()
                val orderDao = db.orderDao()

                lifecycleScope.launch {
                    val details =
                        "Down Payment: $" + String.format("%,.2f", downPayment) +
                                "\nAPR: " + String.format("%.2f", apr) + "%" +
                                "\nEstimated Monthly Payment: $" + String.format("%.2f", monthlyPayment) +
                                "\nLoan Term: $termMonths months"

                    orderDao.insertOrder(
                        Order(
                            userId = currentUserId,
                            vehicleId = vehicleId,
                            vehicleName = vehicleName,
                            totalAmount = vehiclePrice,
                            paymentMethod = "Financing",
                            details = details
                        )
                    )

                    vehicleDao.markVehicleAsSold(vehicleId)
                    cartDao.removeFromCart(currentUserId, vehicleId)

                    val receiptIntent = Intent(this@FinanceResultActivity, ReceiptActivity::class.java)
                    receiptIntent.putExtra("vehicleName", vehicleName)
                    receiptIntent.putExtra("vehiclePrice", vehiclePrice)
                    receiptIntent.putExtra("paymentMethod", "Financing")
                    receiptIntent.putExtra("details", details)
                    startActivity(receiptIntent)
                    finish()
                }
            }
            else
            {
                finish()
            }
        }
    }
}