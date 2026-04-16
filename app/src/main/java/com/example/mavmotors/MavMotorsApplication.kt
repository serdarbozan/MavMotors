package com.example.mavmotors

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MavMotorsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemeManager.applyTheme(this)
        seedAdminUser()
    }

    private fun seedAdminUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseProvider.getDatabase(this@MavMotorsApplication)
            val existing = db.userDao().getUserByEmail("admin@admin.com")
            if (existing == null) {
                db.userDao().insertUser(
                    User(
                        email    = "admin@admin.com",
                        username = "Admin",
                        password = "admin",
                        role     = UserRole.ADMIN,
                        status   = UserStatus.ACTIVE
                    )
                )
            }
        }
    }
}
