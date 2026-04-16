package com.example.mavmotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminListingsAdapter(
    private var vehicles: List<Vehicle>,
    private val sellerMap: Map<Int, String>,
    private val onDelete: (Vehicle) -> Unit
) : RecyclerView.Adapter<AdminListingsAdapter.ListingViewHolder>() {

    inner class ListingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        holder.tvType.text   = v.type
        holder.tvPrice.text  = "$${"%,.0f".format(v.price)}"
        holder.tvSeller.text = "Seller: ${sellerMap[v.sellerId] ?: "Unknown"}"
        holder.tvStatus.text = "Status: ${v.status}"
        holder.tvYear.text   = "${v.year}"
        holder.btnDelete.setOnClickListener { onDelete(v) }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateData(newVehicles: List<Vehicle>, newSellerMap: Map<Int, String>) {
        vehicles  = newVehicles
        notifyDataSetChanged()
    }
}
