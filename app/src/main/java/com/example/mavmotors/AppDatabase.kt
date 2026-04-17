package com.example.mavmotors

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Vehicle::class, User::class, UserSavedVehicle::class, CartItem::class, AdminLog::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun userDao(): UserDao
    abstract fun userSavedVehicleDao(): UserSavedVehicleDao
    abstract fun cartDao(): CartDao
    abstract fun adminLogDao(): AdminLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mav_motors_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object DatabaseProvider {
    fun getDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
}