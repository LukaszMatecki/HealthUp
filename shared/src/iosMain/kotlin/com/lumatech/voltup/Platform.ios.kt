package com.lumatech.voltup

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice


actual fun currentTimeMillis(): Long {
    val date = NSDate()
    return ((date.timeIntervalSinceReferenceDate + 978307200.0) * 1000.0).toLong()
}

actual fun formatSessionDate(timestampMs: Long): String {
    if (timestampMs <= 0L) return ""
    val refSeconds = (timestampMs / 1000.0) - 978307200.0
    val date = NSDate(timeIntervalSinceReferenceDate = refSeconds)
    val formatter = NSDateFormatter()
    formatter.dateFormat = "dd.MM.yyyy, HH:mm"
    return formatter.stringFromDate(date)
}

actual fun getDayOfWeekIndex(timestampMs: Long): Int {
    val epochDays = timestampMs / 86400000L
    val dayIdx = ((epochDays + 3) % 7).toInt()
    return if (dayIdx < 0) (dayIdx + 7) % 7 else dayIdx
}

actual object VoltUpStorage {
    actual fun getString(key: String, defaultValue: String): String =
        NSUserDefaults.standardUserDefaults.stringForKey(key) ?: defaultValue

    actual fun setString(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val userDefaults = NSUserDefaults.standardUserDefaults
        return if (userDefaults.objectForKey(key) != null) {
            userDefaults.boolForKey(key)
        } else {
            defaultValue
        }
    }

    actual fun setBoolean(key: String, value: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(value, forKey = key)
    }
}

actual fun getUserCurrentLocation(): Pair<Double, Double>? = null
