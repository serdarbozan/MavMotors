package com.example.mavmotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class CartAdapter(
    private var vehicles: List<Vehicle>,
    private val onRemoveClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.cartItemImage)
        val titleText: TextView = itemView.findViewById(R.id.cartItemTitle)
        val priceText: TextView = itemView.findViewById(R.id.cartItemPrice)
        val removeButton: ImageView = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val vehicle = vehicles[position]
        val context = holder.itemView.context

        holder.titleText.text = "${vehicle.year} ${vehicle.type}"
        holder.priceText.text = "$${String.format("%,.2f", vehicle.price)}"

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
                    .into(holder.imageView)
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_car)
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
                    .into(holder.imageView)
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_car)
            }
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_car)
        }

        holder.removeButton.setOnClickListener { onRemoveClick(vehicle) }
        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(context, VehicleDetailActivity::class.java)
            intent.putExtra("VEHICLE_ID", vehicle.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}