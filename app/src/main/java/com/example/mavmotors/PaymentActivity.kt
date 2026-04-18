package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity()
{
    private lateinit var cartDao: CartDao
    private var currentUserId: Int = -1
    private var vehicleId: Int = 0
    private var vehicleName: String = "Unknown Vehicle"
    private var vehiclePrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_payment)

        val sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        val db = DatabaseProvider.getDatabase(this)
        cartDao = db.cartDao()

        val backButton = findViewById<Button>(R.id.backButton)
        val totalText = findViewById<TextView>(R.id.totalText)
        val paymentGroup = findViewById<RadioGroup>(R.id.paymentMethodGroup)
        val fullPaymentFields = findViewById<LinearLayout>(R.id.fullPaymentFields)
        val financeFields = findViewById<LinearLayout>(R.id.financeFields)
        val submitButton = findViewById<Button>(R.id.submitPaymentButton)

        val downPaymentInput = findViewById<EditText>(R.id.downPaymentInput)
        val incomeInput = findViewById<EditText>(R.id.incomeInput)
        val creditScoreInput = findViewById<EditText>(R.id.creditScoreInput)
        val employmentMonthsInput = findViewById<EditText>(R.id.employmentMonthsInput)
        val termMonthsInput = findViewById<EditText>(R.id.termMonthsInput)

        backButton.setOnClickListener {
            finish()
        }

        paymentGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.cashOption)
            {
                fullPaymentFields.visibility = LinearLayout.VISIBLE
                financeFields.visibility = LinearLayout.GONE
            }
            else if (checkedId == R.id.financeOption)
            {
                fullPaymentFields.visibility = LinearLayout.GONE
                financeFields.visibility = LinearLayout.VISIBLE
            }
        }

        lifecycleScope.launch {
            val cartVehicles = cartDao.getCartVehicles(currentUserId)

            if (cartVehicles.isEmpty())
            {
                totalText.text = "No vehicle found in cart"
                Toast.makeText(this@PaymentActivity, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val selectedVehicle = cartVehicles.first()
            vehicleId = selectedVehicle.id
            vehicleName = "${selectedVehicle.year} ${selectedVehicle.type}"
            vehiclePrice = selectedVehicle.price

            totalText.text = "Total: $" + String.format("%,.2f", vehiclePrice)
        }

        submitButton.setOnClickListener {
            val checkedId = paymentGroup.checkedRadioButtonId

            if (checkedId == -1)
            {
                Toast.makeText(this, "Select a payment option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (checkedId == R.id.cashOption)
            {
                Toast.makeText(this, "Pay in Full flow will be added next", Toast.LENGTH_SHORT).show()
            }
            else if (checkedId == R.id.financeOption)
            {
                val downPayment = downPaymentInput.text.toString().toDoubleOrNull()
                val monthlyIncome = incomeInput.text.toString().toDoubleOrNull()
                val creditScore = creditScoreInput.text.toString().toIntOrNull()
                val employmentMonths = employmentMonthsInput.text.toString().toIntOrNull()
                val termMonths = termMonthsInput.text.toString().toIntOrNull()

                if (downPayment == null || monthlyIncome == null || creditScore == null ||
                    employmentMonths == null || termMonths == null)
                {
                    Toast.makeText(this, "Fill in all financing fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (termMonths !in listOf(36, 48, 60, 72))
                {
                    Toast.makeText(this, "Loan term must be 36, 48, 60, or 72", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val intent = Intent(this, FinanceProcessingActivity::class.java)
                intent.putExtra("vehicleId", vehicleId)
                intent.putExtra("vehicleName", vehicleName)
                intent.putExtra("vehiclePrice", vehiclePrice)
                intent.putExtra("downPayment", downPayment)
                intent.putExtra("monthlyIncome", monthlyIncome)
                intent.putExtra("creditScore", creditScore)
                intent.putExtra("employmentMonths", employmentMonths)
                intent.putExtra("termMonths", termMonths)
                startActivity(intent)
            }
        }
    }
}