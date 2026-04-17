package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FinanceProcessingActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_finance_processing)

        val statusText = findViewById<TextView>(R.id.statusText)

        val vehiclePrice = intent.getDoubleExtra("vehiclePrice", 0.0)
        val downPayment = intent.getDoubleExtra("downPayment", 0.0)
        val monthlyIncome = intent.getDoubleExtra("monthlyIncome", 0.0)
        val creditScore = intent.getIntExtra("creditScore", 0)
        val employmentMonths = intent.getIntExtra("employmentMonths", 0)
        val termMonths = intent.getIntExtra("termMonths", 0)

        statusText.text = "Sending application to lenders..."

        Handler(Looper.getMainLooper()).postDelayed({
            statusText.text = "Reviewing credit application..."

            Handler(Looper.getMainLooper()).postDelayed({
                val decision = FinancingEngine.evaluate(
                    vehiclePrice = vehiclePrice,
                    downPayment = downPayment,
                    monthlyIncome = monthlyIncome,
                    creditScore = creditScore,
                    employmentMonths = employmentMonths,
                    termMonths = termMonths
                )

                val intent = Intent(this, FinanceResultActivity::class.java)
                intent.putExtra("approved", decision.approved)
                intent.putExtra("apr", decision.apr)
                intent.putExtra("monthlyPayment", decision.monthlyPayment)
                intent.putExtra("reason", decision.reason)
                startActivity(intent)
                finish()
            }, 1500)
        }, 1500)
    }
}