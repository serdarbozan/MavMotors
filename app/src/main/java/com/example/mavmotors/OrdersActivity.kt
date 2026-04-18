package com.example.mavmotors

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File

class OrdersActivity : AppCompatActivity()
{
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var emptyOrdersText: TextView
    private lateinit var orderDao: OrderDao
    private lateinit var vehicleDao: VehicleDao
    private lateinit var orderVehicleAdapter: OrderVehicleAdapter
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_orders)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        val sharedPrefs = getSharedPreferences("MavMotorsPrefs", MODE_PRIVATE)
        currentUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        val db = DatabaseProvider.getDatabase(this)
        orderDao = db.orderDao()
        vehicleDao = db.vehicleDao()

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
        emptyOrdersText = findViewById(R.id.emptyOrdersText)

        ordersRecyclerView.layoutManager = GridLayoutManager(this, 2)
        orderVehicleAdapter = OrderVehicleAdapter(emptyList())
        ordersRecyclerView.adapter = orderVehicleAdapter

        loadOrders()
    }

    private fun loadOrders()
    {
        lifecycleScope.launch {
            val orders = orderDao.getOrdersForUser(currentUserId)

            if (orders.isEmpty())
            {
                emptyOrdersText.visibility = View.VISIBLE
                ordersRecyclerView.visibility = View.GONE
                return@launch
            }

            val purchasedVehicles = mutableListOf<Vehicle>()

            for (order in orders)
            {
                val vehicle = vehicleDao.getVehicleById(order.vehicleId)
                if (vehicle != null)
                {
                    purchasedVehicles.add(vehicle)
                }
            }

            if (purchasedVehicles.isEmpty())
            {
                emptyOrdersText.visibility = View.VISIBLE
                ordersRecyclerView.visibility = View.GONE
            }
            else
            {
                emptyOrdersText.visibility = View.GONE
                ordersRecyclerView.visibility = View.VISIBLE
                orderVehicleAdapter.updateVehicles(purchasedVehicles)
            }
        }
    }
}

class OrderVehicleAdapter(
    private var vehicles: List<Vehicle>
) : RecyclerView.Adapter<OrderVehicleAdapter.OrderVehicleViewHolder>()
{
    class OrderVehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val vehicleImage: ImageView = itemView.findViewById(R.id.vehicleImage)
        val typeTextView: TextView = itemView.findViewById(R.id.carTitle)
        val priceTextView: TextView = itemView.findViewById(R.id.priceText)
        val mileageTextView: TextView = itemView.findViewById(R.id.mileageText)
        val yearTextView: TextView = itemView.findViewById(R.id.yearText)
        val heartCheckBox: CheckBox = itemView.findViewById(R.id.checkBox3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderVehicleViewHolder
    {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.car_template, parent, false)

        return OrderVehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderVehicleViewHolder, position: Int)
    {
        val vehicle = vehicles[position]
        val context = holder.itemView.context
        val isDarkMode = ThemeManager.isDarkMode(context)

        if (vehicle.isSampleImage && vehicle.imagePath.isNotEmpty())
        {
            val resourceId = context.resources.getIdentifier(
                vehicle.imagePath,
                "drawable",
                context.packageName
            )

            if (resourceId != 0)
            {
                Glide.with(context)
                    .load(resourceId)
                    .placeholder(R.drawable.placeholder_car)
                    .error(R.drawable.placeholder_car)
                    .centerCrop()
                    .into(holder.vehicleImage)
            }
            else
            {
                holder.vehicleImage.setImageResource(R.drawable.placeholder_car)
            }
        }
        else if (vehicle.imagePath.isNotEmpty())
        {
            val imageFile = File(vehicle.imagePath)
            if (imageFile.exists())
            {
                Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_car)
                    .error(R.drawable.placeholder_car)
                    .centerCrop()
                    .into(holder.vehicleImage)
            }
            else
            {
                holder.vehicleImage.setImageResource(R.drawable.placeholder_car)
            }
        }
        else
        {
            holder.vehicleImage.setImageResource(R.drawable.placeholder_car)
        }

        holder.typeTextView.text = "${vehicle.year} ${vehicle.type}"
        holder.priceTextView.text = "$${String.format("%,.2f", vehicle.price)}"
        holder.mileageTextView.text = "${vehicle.mileage} miles"
        holder.yearTextView.text = vehicle.year.toString()

        if (isDarkMode)
        {
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.infoTitle_dark))
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.infoPrice_dark))
            holder.mileageTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails_dark))
            holder.yearTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails_dark))
        }
        else
        {
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.infoTitle))
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.infoPrice))
            holder.mileageTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails))
            holder.yearTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails))
        }

        holder.heartCheckBox.isChecked = false
        holder.heartCheckBox.buttonTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, android.R.color.transparent)
        )

        val heartContainer = holder.heartCheckBox.parent as View
        heartContainer.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val intent = Intent(context, VehicleDetailActivity::class.java)
            intent.putExtra("VEHICLE_ID", vehicle.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateVehicles(newVehicles: List<Vehicle>)
    {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}