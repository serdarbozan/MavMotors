package com.example.mavmotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class AdminListingsAdapter(
    private var vehicles: List<Vehicle>,
    private val sellerMap: Map<Int, String>,
    private val onDelete: (Vehicle) -> Unit
) : RecyclerView.Adapter<AdminListingsAdapter.ListingViewHolder>() {

    inner class ListingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivAdminListingImage)
        val tvType: TextView    = view.findViewById(R.id.tvAdminListingType)
        val tvPrice: TextView   = view.findViewById(R.id.tvAdminListingPrice)
        val tvSeller: TextView  = view.findViewById(R.id.tvAdminListingSeller)
        val tvStatus: TextView  = view.findViewById(R.id.tvAdminListingStatus)
        val tvYear: TextView    = view.findViewById(R.id.tvAdminListingYear)
        val btnDelete: Button   = view.findViewById(R.id.btnDeleteListing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_listing, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val v = vehicles[position]
        val context = holder.itemView.context

        holder.tvType.text   = v.type
        holder.tvPrice.text  = "$${"%,.0f".format(v.price)}"
        holder.tvSeller.text = "Seller: ${sellerMap[v.sellerId] ?: "Unknown"}"
        holder.tvStatus.text = "Status: ${v.status}"
        holder.tvYear.text   = "${v.year}"
        holder.btnDelete.setOnClickListener { onDelete(v) }

        // Load image - check if it's a sample resource or file path
        if (v.isSampleImage && v.imagePath.isNotEmpty()) {
            // Load from drawable resource
            val resourceId = context.resources.getIdentifier(
                v.imagePath,
                "drawable",
                context.packageName
            )
            if (resourceId != 0) {
                Glide.with(context)
                    .load(resourceId)
                    .placeholder(R.drawable.placeholder_car)
                    .error(R.drawable.placeholder_car)
                    .centerCrop()
                    .into(holder.ivImage)
            } else {
                holder.ivImage.setImageResource(R.drawable.placeholder_car)
            }
        } else if (v.imagePath.isNotEmpty()) {
            // Load from file path (user-added images)
            val imageFile = File(v.imagePath)
            if (imageFile.exists()) {
                Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_car)
                    .error(R.drawable.placeholder_car)
                    .centerCrop()
                    .into(holder.ivImage)
            } else {
                holder.ivImage.setImageResource(R.drawable.placeholder_car)
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_car)
        }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateData(newVehicles: List<Vehicle>, newSellerMap: Map<Int, String>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}
