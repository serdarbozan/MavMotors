package com.example.mavmotors

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object ThemeManager {

    private const val PREFS_NAME = "app_preferences"
    private const val KEY_DARK_MODE_PREFIX = "dark_mode_user_"
    private const val KEY_GLOBAL_DARK_MODE = "dark_mode_global"

    // Get the current logged-in user ID
    private fun getCurrentUserId(context: Context): Int {
        val sharedPrefs = context.getSharedPreferences("MavMotorsPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("logged_in_user_id", -1)
    }

    // Get the preference key for the current user
    private fun getUserDarkModeKey(context: Context): String {
        val userId = getCurrentUserId(context)
        return if (userId != -1) {
            "${KEY_DARK_MODE_PREFIX}${userId}"
        } else {
            KEY_GLOBAL_DARK_MODE
        }
    }

    // Apply theme based on current user's saved preference
    fun applyTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = getUserDarkModeKey(context)
        val isDarkMode = prefs.getBoolean(key, true) // Default to dark mode

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    // Save theme preference for the current user
    fun saveThemePreference(context: Context, isDarkMode: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = getUserDarkModeKey(context)
        prefs.edit { putBoolean(key, isDarkMode) }
    }

    // Check if dark mode is active for the current user
    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = getUserDarkModeKey(context)
        return prefs.getBoolean(key, true)
    }

    // Migrate old global preference to user-specific (call once after login)
    fun migrateGlobalToUserPreference(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val globalValue = prefs.getBoolean(KEY_GLOBAL_DARK_MODE, true)
        val userKey = getUserDarkModeKey(context)

        // Only migrate if user key doesn't exist yet
        if (!prefs.contains(userKey)) {
            prefs.edit { putBoolean(userKey, globalValue) }
        }
    }
}