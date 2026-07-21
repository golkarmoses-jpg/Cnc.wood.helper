package com.example.cncwood

import android.content.Context

class PreferenceManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("cnc_preferences", Context.MODE_PRIVATE)

    fun setFeedRate(rate: Int) {
        sharedPreferences.edit().putInt("feed_rate", rate).apply()
    }

    fun getFeedRate(): Int {
        return sharedPreferences.getInt("feed_rate", 100)
    }

    fun setSafeHeight(height: Double) {
        sharedPreferences.edit().putFloat("safe_height", height.toFloat()).apply()
    }

    fun getSafeHeight(): Double {
        return sharedPreferences.getFloat("safe_height", 10f).toDouble()
    }

    fun setSpindleSpeed(speed: Int) {
        sharedPreferences.edit().putInt("spindle_speed", speed).apply()
    }

    fun getSpindleSpeed(): Int {
        return sharedPreferences.getInt("spindle_speed", 1000)
    }

    fun setAutoSave(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("auto_save", enabled).apply()
    }

    fun isAutoSaveEnabled(): Boolean {
        return sharedPreferences.getBoolean("auto_save", true)
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
