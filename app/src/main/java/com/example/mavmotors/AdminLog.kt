package com.example.mavmotors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_logs")
data class AdminLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val adminId: Int,
    val adminUsername: String,
    val action: String,
    val targetId: Int,
    val targetDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)

object AdminAction {
    const val DELETE_LISTING = "DELETE_LISTING"
    const val DELETE_USER    = "DELETE_USER"
    const val SUSPEND_USER   = "SUSPEND_USER"
    const val ACTIVATE_USER  = "ACTIVATE_USER"
    const val CHANGE_ROLE    = "CHANGE_ROLE"
}
