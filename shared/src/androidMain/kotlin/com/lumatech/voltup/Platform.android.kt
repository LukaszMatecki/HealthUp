package com.lumatech.voltup

import android.content.Context
import android.content.SharedPreferences
import android.os.Build


actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun formatSessionDate(timestampMs: Long): String {
    if (timestampMs <= 0L) return ""
    return java.text.SimpleDateFormat("dd.MM.yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestampMs))
}

actual fun getDayOfWeekIndex(timestampMs: Long): Int {
    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Europe/Warsaw"))
    cal.timeInMillis = timestampMs
    val dow = cal.get(java.util.Calendar.DAY_OF_WEEK)
    return (dow + 5) % 7
}

internal var healthUpAppContext: Context? = null
internal var voltUpAppContext: Context?
    get() = healthUpAppContext
    set(value) { healthUpAppContext = value }

actual object HealthUpStorage {
    private val prefs: SharedPreferences?
        get() = healthUpAppContext?.getSharedPreferences("healthup_prefs", Context.MODE_PRIVATE)

    private val legacyPrefs: SharedPreferences?
        get() = healthUpAppContext?.getSharedPreferences("voltup_prefs", Context.MODE_PRIVATE)

    actual fun getString(key: String, defaultValue: String): String {
        return prefs?.getString(key, null)
            ?: legacyPrefs?.getString(key, null)
            ?: defaultValue
    }

    actual fun setString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        if (prefs != null && prefs!!.contains(key)) {
            return prefs!!.getBoolean(key, defaultValue)
        }
        if (legacyPrefs != null && legacyPrefs!!.contains(key)) {
            return legacyPrefs!!.getBoolean(key, defaultValue)
        }
        return defaultValue
    }

    actual fun setBoolean(key: String, value: Boolean) {
        prefs?.edit()?.putBoolean(key, value)?.apply()
    }
}



@android.annotation.SuppressLint("MissingPermission")
actual fun getUserCurrentLocation(): Pair<Double, Double>? {
    val ctx = healthUpAppContext ?: return null
    return try {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return null
        val gps = try { lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER) } catch (_: SecurityException) { null }
        val net = try { lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER) } catch (_: SecurityException) { null }
        val best = gps ?: net
        if (best != null) {
            Pair(best.latitude, best.longitude)
        } else null
    } catch (_: Exception) {
        null
    }
}
