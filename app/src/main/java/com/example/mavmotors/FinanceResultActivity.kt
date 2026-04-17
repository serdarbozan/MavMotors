package com.example.mavmotors

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
        }
        else
        {
            resultStatusText.text = "Rejected"
            aprText.text = "APR: --"
            monthlyPaymentText.text = "Estimated Monthly Payment: --"
        }

        reasonText.text = "Reason: $reason"

        doneButton.setOnClickListener {
            finish()
        }
    }
}