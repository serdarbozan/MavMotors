package com.example.mavmotors

import android.app.Application

class MavMotorsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemeManager.applyTheme(this)
    }
}