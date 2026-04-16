package com.example.mavmotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminLogsAdapter(
    private val logs: List<AdminLog>
) : RecyclerView.Adapter<AdminLogsAdapter.LogViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAction: TextView      = view.findViewById(R.id.tvLogAction)
        val tvAdmin: TextView       = view.findViewById(R.id.tvLogAdmin)
        val tvTarget: TextView      = view.findViewById(R.id.tvLogTarget)
        val tvTimestamp: TextView   = view.findViewById(R.id.tvLogTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        holder.tvAction.text    = log.action
        holder.tvAdmin.text     = "By: ${log.adminUsername}"
        holder.tvTarget.text    = "Target: ${log.targetDescription} (ID: ${log.targetId})"
        holder.tvTimestamp.text = dateFormat.format(Date(log.timestamp))
    }

    override fun getItemCount(): Int = logs.size
}
