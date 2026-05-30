package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gourmet_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_DIETARY = "dietary_preference"
        const val KEY_ALARM_SOUND = "alarm_sound"
        const val KEY_ALARM_VIBRATE = "alarm_vibrate"
    }

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true) // default to Dark theme as per guidelines (appealing)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var dietaryPreference: String
        get() = prefs.getString(KEY_DIETARY, "None") ?: "None"
        set(value) = prefs.edit().putString(KEY_DIETARY, value).apply()

    var isAlarmSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALARM_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_ALARM_SOUND, value).apply()

    var isAlarmVibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALARM_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_ALARM_VIBRATE, value).apply()
}
