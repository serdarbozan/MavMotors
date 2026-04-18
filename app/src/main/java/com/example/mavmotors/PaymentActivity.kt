package com.example.mavmotors

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private lateinit var cartDao: CartDao
    private lateinit var orderDao: OrderDao
    private lateinit var vehicleDao: VehicleDao
    private var currentUserId: Int = -1
    private var vehicleId: Int = 0
    private var vehicleName: String = "Unknown Vehicle"
    private var vehiclePrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_payment)

        val sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        val db = DatabaseProvider.getDatabase(this)
        cartDao = db.cartDao()
        orderDao = db.orderDao()
        vehicleDao = db.vehicleDao()

        val backButton = findViewById<ImageView>(R.id.backButton)
        val totalText = findViewById<TextView>(R.id.totalText)
        val paymentGroup = findViewById<RadioGroup>(R.id.paymentMethodGroup)
        val cashOption = findViewById<RadioButton>(R.id.cashOption)
        val financeOption = findViewById<RadioButton>(R.id.financeOption)
        val fullPaymentCard = findViewById<CardView>(R.id.fullPaymentCard)
        val financeCard = findViewById<CardView>(R.id.financeCard)
        val submitButton = findViewById<Button>(R.id.submitPaymentButton)

        // Full payment fields
        val cardholderNameInput = findViewById<EditText>(R.id.cardholderNameInput)
        val cardNumberInput = findViewById<EditText>(R.id.cardNumberInput)
        val expiryInput = findViewById<EditText>(R.id.expiryInput)
        val cvvInput = findViewById<EditText>(R.id.cvvInput)
        val zipInput = findViewById<EditText>(R.id.zipInput)

        // Finance fields
        val downPaymentInput = findViewById<EditText>(R.id.downPaymentInput)
        val incomeInput = findViewById<EditText>(R.id.incomeInput)
        val creditScoreInput = findViewById<EditText>(R.id.creditScoreInput)
        val employmentMonthsInput = findViewById<EditText>(R.id.employmentMonthsInput)
        val termMonthsInput = findViewById<EditText>(R.id.termMonthsInput)

        backButton.setOnClickListener {
            finish()
        }

        // Set default selection - Pay in Full
        cashOption.isChecked = true
        fullPaymentCard.visibility = View.VISIBLE
        financeCard.visibility = View.GONE

        paymentGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.cashOption) {
                fullPaymentCard.visibility = View.VISIBLE
                financeCard.visibility = View.GONE
            } else if (checkedId == R.id.financeOption) {
                fullPaymentCard.visibility = View.GONE
                financeCard.visibility = View.VISIBLE
            }
        }

        lifecycleScope.launch {
            val cartVehicles = cartDao.getCartVehicles(currentUserId)

            if (cartVehicles.isEmpty()) {
                totalText.text = "$0.00"
                Toast.makeText(this@PaymentActivity, "Cart is empty", Toast.LENGTH_SHORT).show()
                submitButton.isEnabled = false
                return@launch
            }

            val selectedVehicle = cartVehicles.first()
            vehicleId = selectedVehicle.id
            vehicleName = "${selectedVehicle.year} ${selectedVehicle.type}"
            vehiclePrice = selectedVehicle.price

            totalText.text = "$${String.format("%,.2f", vehiclePrice)}"
        }

        submitButton.setOnClickListener {
            val checkedId = paymentGroup.checkedRadioButtonId

            if (checkedId == -1) {
                Toast.makeText(this, "Select a payment option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (checkedId == R.id.cashOption) {
                // Validate full payment fields
                val cardholderName = cardholderNameInput.text.toString().trim()
                val cardNumber = cardNumberInput.text.toString().trim()
                val expiry = expiryInput.text.toString().trim()
                val cvv = cvvInput.text.toString().trim()
                val zip = zipInput.text.toString().trim()

                if (cardholderName.isEmpty() || cardNumber.isEmpty() ||
                    expiry.isEmpty() || cvv.isEmpty() || zip.isEmpty()) {
                    Toast.makeText(this, "Please fill in all card details", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show()

                // Process payment, mark vehicle as sold, create order, and clear cart
                lifecycleScope.launch {
                    // Mark vehicle as sold so it disappears from main feed
                    vehicleDao.markVehicleAsSold(vehicleId)

                    // Create order
                    val order = Order(
                        userId = currentUserId,
                        vehicleId = vehicleId,
                        vehicleName = vehicleName,
                        totalAmount = vehiclePrice,
                        paymentMethod = "Pay in Full",
                        details = "Card ending in ${cardNumber.takeLast(4)}",
                        createdAt = System.currentTimeMillis()
                    )
                    orderDao.insertOrder(order)

                    // Clear cart
                    cartDao.clearCart(currentUserId)

                    Toast.makeText(this@PaymentActivity, "Payment successful! Thank you for your purchase.", Toast.LENGTH_LONG).show()

                    // Return to landing page
                    val intent = Intent(this@PaymentActivity, LandingPage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }

            } else if (checkedId == R.id.financeOption) {
                val downPayment = downPaymentInput.text.toString().toDoubleOrNull()
                val monthlyIncome = incomeInput.text.toString().toDoubleOrNull()
                val creditScore = creditScoreInput.text.toString().toIntOrNull()
                val employmentMonths = employmentMonthsInput.text.toString().toIntOrNull()
                val termMonths = termMonthsInput.text.toString().toIntOrNull()

                if (downPayment == null || monthlyIncome == null || creditScore == null ||
                    employmentMonths == null || termMonths == null) {
                    Toast.makeText(this, "Fill in all financing fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (termMonths !in listOf(36, 48, 60, 72)) {
                    Toast.makeText(this, "Loan term must be 36, 48, 60, or 72", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Mark vehicle as sold and create order for financing
                lifecycleScope.launch {
                    vehicleDao.markVehicleAsSold(vehicleId)

                    val order = Order(
                        userId = currentUserId,
                        vehicleId = vehicleId,
                        vehicleName = vehicleName,
                        totalAmount = vehiclePrice,
                        paymentMethod = "Financing",
                        details = "Down: $$downPayment, Term: $termMonths months",
                        createdAt = System.currentTimeMillis()
                    )
                    orderDao.insertOrder(order)
                    cartDao.clearCart(currentUserId)
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