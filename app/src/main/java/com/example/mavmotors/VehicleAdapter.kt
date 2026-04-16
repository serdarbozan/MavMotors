package com.example.mavmotors

import android.content.res.ColorStateList
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
    private var vehicles: List<Vehicle>,
    private val onHeartClick: ((Vehicle, Boolean) -> Unit)? = null
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

        val isDarkMode = ThemeManager.isDarkMode(context)

        // Load image - check if it's a sample resource or file path
        if (vehicle.isSampleImage && vehicle.imagePath.isNotEmpty()) {
            // Load from drawable resource
            val resourceId = context.resources.getIdentifier(
                vehicle.imagePath,
                "drawable",
                context.packageName
            )
            if (resourceId != 0) {
                Glide.with(context)
                    .load(resourceId)
                    .placeholder(R.drawable.placeholder_car)
                    .error(R.drawable.placeholder_car)
                    .centerCrop()
                    .into(holder.vehicleImage)
            } else {
                holder.vehicleImage.setImageResource(R.drawable.placeholder_car)
            }
        } else if (vehicle.imagePath.isNotEmpty()) {
            // Load from file path (user-added images)
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

        // Apply theme-aware text colors
        if (isDarkMode) {
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.infoTitle_dark))
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.infoPrice_dark))
            holder.mileageTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails_dark))
            holder.yearTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails_dark))
        } else {
            holder.typeTextView.setTextColor(ContextCompat.getColor(context, R.color.infoTitle))
            holder.priceTextView.setTextColor(ContextCompat.getColor(context, R.color.infoPrice))
            holder.mileageTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails))
            holder.yearTextView.setTextColor(ContextCompat.getColor(context, R.color.infoDetails))
        }

        // Heart checkbox - Force correct colors
        holder.heartCheckBox.buttonTintList = null
        holder.heartCheckBox.setOnCheckedChangeListener(null)

        val isChecked = favoriteStates[vehicle.id] ?: false
        holder.heartCheckBox.isChecked = isChecked

        // Set the correct heart color based on checked state
        if (isChecked) {
            holder.heartCheckBox.buttonTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.main_orange)
            )
        } else {
            holder.heartCheckBox.buttonTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.heart_inactive)
            )
        }

        holder.heartCheckBox.setOnCheckedChangeListener { _, checked ->
            if (checked != favoriteStates[vehicle.id]) {
                favoriteStates[vehicle.id] = checked
                // Update tint immediately when clicked
                if (checked) {
                    holder.heartCheckBox.buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.main_orange)
                    )
                } else {
                    holder.heartCheckBox.buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.heart_inactive)
                    )
                }
                onHeartClick?.invoke(vehicle, checked)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(context, VehicleDetailActivity::class.java)
            intent.putExtra("VEHICLE_ID", vehicle.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        favoriteStates.clear()
        notifyDataSetChanged()
    }

    fun setSavedState(vehicleId: Int, isSaved: Boolean) {
        favoriteStates[vehicleId] = isSaved
        val position = vehicles.indexOfFirst { it.id == vehicleId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }
}