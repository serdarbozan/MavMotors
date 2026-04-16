package com.example.mavmotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminUsersAdapter(
    private var users: List<User>,
    private val onSuspendToggle: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView   = view.findViewById(R.id.tvAdminUserName)
        val tvEmail: TextView      = view.findViewById(R.id.tvAdminUserEmail)
        val tvRole: TextView       = view.findViewById(R.id.tvAdminUserRole)
        val tvStatus: TextView     = view.findViewById(R.id.tvAdminUserStatus)
        val btnToggle: Button      = view.findViewById(R.id.btnToggleSuspend)
        val btnDelete: Button      = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUsername.text = user.username
        holder.tvEmail.text    = user.email
        holder.tvRole.text     = "Role: ${user.role}"
        holder.tvStatus.text   = "Status: ${user.status}"

        holder.btnToggle.text =
            if (user.status == UserStatus.SUSPENDED) "Activate" else "Suspend"

        holder.btnToggle.setOnClickListener { onSuspendToggle(user) }
        holder.btnDelete.setOnClickListener { onDelete(user) }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
