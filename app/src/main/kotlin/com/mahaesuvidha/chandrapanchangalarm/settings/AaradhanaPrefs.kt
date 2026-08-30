package com.mahaesuvidha.chandrapanchangalarm.settings

import android.content.Context
import com.mahaesuvidha.chandrapanchangalarm.model.BirthProfileStore

class AaradhanaPrefs(private val context: Context) {
    private val prefs get() = context.getSharedPreferences("life_alarm_aaradhana", Context.MODE_PRIVATE)
    private fun key(suffix: String): String {
        val p = BirthProfileStore.load(context.applicationContext)
        val id = p?.let { "${it.name}|${it.birthDate}|${it.birthTime}|${it.birthPlace}" } ?: "default"
        return "${id.hashCode()}_$suffix"
    }
    var specialHourly: Boolean
        get() = prefs.getBoolean(key("special_hourly"), false)
        set(value) = prefs.edit().putBoolean(key("special_hourly"), value).apply()
}
