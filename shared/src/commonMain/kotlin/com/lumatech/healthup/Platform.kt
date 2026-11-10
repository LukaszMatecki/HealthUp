package com.lumatech.healthup


expect fun currentTimeMillis(): Long

expect fun formatSessionDate(timestampMs: Long): String

expect object HealthUpStorage {
    fun getString(key: String, defaultValue: String = ""): String
    fun setString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)
    fun remove(key: String)
}

object HealthUpPreferenceKeys {
    const val KEY_REMEMBER_ME = "remember_me"
    const val KEY_SAVED_EMAIL = "saved_email"
    const val KEY_SAVED_LANGUAGE = "saved_language"
    const val KEY_SAVED_THEME = "saved_theme"
    const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
    @Deprecated("Plaintext passwords must not be persisted; retained only to purge legacy values.")
    const val KEY_SAVED_PASSWORD = "saved_password"
    const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    const val KEY_PUSH_NOTIFICATIONS = "push_notifications"
}

expect fun getDayOfWeekIndex(timestampMs: Long = currentTimeMillis()): Int

expect fun getUserCurrentLocation(): Pair<Double, Double>?