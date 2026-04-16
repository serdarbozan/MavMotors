package com.example.mavmotors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AdminLogDao {

    @Insert
    suspend fun insertLog(log: AdminLog)

    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<AdminLog>

    @Query("SELECT * FROM admin_logs WHERE adminId = :adminId ORDER BY timestamp DESC")
    suspend fun getLogsByAdmin(adminId: Int): List<AdminLog>
}
