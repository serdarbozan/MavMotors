package com.example.mavmotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class MyListingsAdapter(
    private var vehicles: List<Vehicle>,
    private val onEditClick: (Vehicle) -> Unit,
    private val onDeleteClick: (Vehicle) -> Unit,
    private val onItemClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<MyListingsAdapter.ListingViewHolder>() {

    class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.listingImage)
        val titleText: TextView = itemView.findViewById(R.id.listingTitle)
        val priceText: TextView = itemView.findViewById(R.id.listingPrice)
        val statusText: TextView = itemView.findViewById(R.id.listingStatus)
        val editButton: ImageView = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_listing, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val vehicle = vehicles[position]
        val context = holder.itemView.context

        holder.titleText.text = "${vehicle.year} ${vehicle.type}"
        holder.priceText.text = "$${String.format("%,.2f", vehicle.price)}"
        holder.statusText.text = vehicle.status

        if (vehicle.imagePath.isNotEmpty()) {
            val imageFile = File(vehicle.imagePath)
            if (imageFile.exists()) {
                Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_car)
                    .error(R.drawable.placeholder_car)
                    .centerCrop()
                    .into(holder.imageView)
            }
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_car)
        }

        holder.editButton.setOnClickListener { onEditClick(vehicle) }
        holder.deleteButton.setOnClickListener { onDeleteClick(vehicle) }
        holder.itemView.setOnClickListener { onItemClick(vehicle) }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}