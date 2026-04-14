package com.example.mavmotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class VehicleAdapter(
    private var vehicles: List<Vehicle>
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private val favoriteStates = mutableMapOf<Int, Boolean>()

    class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vehicleImage: ImageView = itemView.findViewById(R.id.vehicleImage)
        val typeTextView: TextView = itemView.findViewById(R.id.carTitle)
        val priceTextView: TextView = itemView.findViewById(R.id.priceText)
        val mileageTextView: TextView = itemView.findViewById(R.id.mileageText)
        val yearTextView: TextView = itemView.findViewById(R.id.yearText)
        val heartCheckBox: CheckBox = itemView.findViewById(R.id.checkBox3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.car_template, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicles[position]
        val context = holder.itemView.context

        // Check if dark mode is active
        val isDarkMode = ThemeManager.isDarkMode(context)

        // Load image from local path
        if (vehicle.imagePath.isNotEmpty()) {
            val imageFile = File(vehicle.imagePath)
            if (imageFile.exists()) {
                Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_car)
                    .error(R.drawable.placeholder_car)
                    .centerCrop()
                    .into(holder.vehicleImage)
            } else {
                holder.vehicleImage.setImageResource(R.drawable.placeholder_car)
            }
        } else {
            holder.vehicleImage.setImageResource(R.drawable.placeholder_car)
        }

        // Set text content
        holder.typeTextView.text = "${vehicle.year} ${vehicle.type}"
        holder.priceTextView.text = "$${String.format("%,.2f", vehicle.price)}"
        holder.mileageTextView.text = "${vehicle.mileage} miles"
        holder.yearTextView.text = vehicle.year.toString()

        // Apply theme-aware colors
        if (isDarkMode) {
            // DARK MODE COLORS
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.infoTitle_dark))
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.infoPrice_dark))
            holder.mileageTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails_dark))
            holder.yearTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails_dark))
        } else {
            // LIGHT MODE COLORS
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.infoTitle))
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.infoPrice))
            holder.mileageTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails))
            holder.yearTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails))
        }

        // Heart checkbox
        holder.heartCheckBox.buttonTintList = null
        holder.heartCheckBox.setOnCheckedChangeListener(null)
        holder.heartCheckBox.isChecked = favoriteStates[position] ?: false
        holder.heartCheckBox.setOnCheckedChangeListener { _, isChecked ->
            favoriteStates[position] = isChecked
        }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        favoriteStates.clear()
        notifyDataSetChanged()
    }
}