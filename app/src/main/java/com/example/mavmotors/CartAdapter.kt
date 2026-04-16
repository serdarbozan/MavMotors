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
        holder.titleText.text = "${vehicle.year} ${vehicle.type}"
        holder.priceText.text = "$${String.format("%,.2f", vehicle.price)}"

        if (vehicle.imagePath.isNotEmpty()) {
            val imageFile = File(vehicle.imagePath)
            if (imageFile.exists()) {
                Glide.with(holder.itemView.context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_car)
                    .error(R.drawable.placeholder_car)
                    .centerCrop()
                    .into(holder.imageView)
            }
        }

        holder.removeButton.setOnClickListener { onRemoveClick(vehicle) }
        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, VehicleDetailActivity::class.java)
            intent.putExtra("VEHICLE_ID", vehicle.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}