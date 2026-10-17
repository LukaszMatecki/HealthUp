package com.lumatech.voltup

/**
 * Model tabeli Użytkowników (users)
 * Główna reprezentacja konta i stanu portfela użytkownika w Firebase Firestore.
 */
data class VoltUpUser(
    val userId: String = "",
    val email: String = "",
    val userName: String = "",
    val avatarId: Int = 1,
    val voltPoints: Int = 0,
    val rankingPoints: Int = 0,
    val totalEnergyWh: Double = 0.0,
    val co2SavedKg: Double = 0.0,
    val userLevel: Int = 0,
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val createdAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "user_id" to userId,
        "email" to email,
        "user_name" to userName,
        "avatar_id" to avatarId,
        "volt_points" to voltPoints,
        "ranking_points" to rankingPoints,
        "total_energy_wh" to totalEnergyWh,
        "co2_saved_kg" to co2SavedKg,
        "user_level" to userLevel,
        "streak_days" to streakDays,
        "last_active_date" to lastActiveDate,
        "created_at" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): VoltUpUser {
            val voltPts = (map["volt_points"] as? Number ?: map["points"] as? Number)?.toInt() ?: 0
            val rankPts = (map["ranking_points"] as? Number)?.toInt() ?: voltPts
            return VoltUpUser(
                userId = (map["user_id"] as? String ?: map["id"] as? String).orEmpty(),
                email = (map["email"] as? String ?: map["mail"] as? String).orEmpty(),
                userName = (map["user_name"] as? String ?: map["name"] as? String ?: map["nazwa"] as? String).orEmpty().ifBlank { "Użytkownik VoltUp" },
                avatarId = (map["avatar_id"] as? Number ?: map["avatar"] as? Number)?.toInt() ?: 1,
                voltPoints = voltPts,
                rankingPoints = rankPts,
                totalEnergyWh = (map["total_energy_wh"] as? Number ?: map["energy_wh"] as? Number)?.toDouble() ?: 0.0,
                co2SavedKg = (map["co2_saved_kg"] as? Number ?: map["co2"] as? Number)?.toDouble() ?: 0.0,
                userLevel = (map["user_level"] as? Number ?: map["level"] as? Number)?.toInt() ?: 0,
                streakDays = (map["streak_days"] as? Number ?: map["streak"] as? Number)?.toInt() ?: 1,
                lastActiveDate = (map["last_active_date"] as? String).orEmpty(),
                createdAt = (map["created_at"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

/**
 * Model tabeli Historii Sesji Treningowych (workout_sessions)
 * Każdy ukończony trening na stacji miejskiej zapisuje jeden wiersz/dokument.
 */
data class WorkoutSessionDto(
    val sessionId: String = "",
    val userId: String = "",
    val stationId: String = "",
    val stationName: String = "",
    val stationLocation: String = "",
    val durationSeconds: Int = 0,
    val generatedWh: Double = 0.0,
    val earnedPoints: Int = 0,
    val co2SavedKg: Double = 0.0,
    val caloriesKcal: Int = 0,
    val createdAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "session_id" to sessionId,
        "user_id" to userId,
        "station_id" to stationId,
        "station_name" to stationName,
        "station_location" to stationLocation,
        "duration_seconds" to durationSeconds,
        "generated_wh" to generatedWh,
        "earned_points" to earnedPoints,
        "co2_saved_kg" to co2SavedKg,
        "calories_kcal" to caloriesKcal,
        "created_at" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): WorkoutSessionDto {
            return WorkoutSessionDto(
                sessionId = (map["session_id"] as? String).orEmpty(),
                userId = (map["user_id"] as? String).orEmpty(),
                stationId = (map["station_id"] as? String).orEmpty(),
                stationName = (map["station_name"] as? String).orEmpty(),
                stationLocation = (map["station_location"] as? String).orEmpty(),
                durationSeconds = (map["duration_seconds"] as? Number)?.toInt() ?: 0,
                generatedWh = (map["generated_wh"] as? Number)?.toDouble() ?: 0.0,
                earnedPoints = (map["earned_points"] as? Number)?.toInt() ?: 0,
                co2SavedKg = (map["co2_saved_kg"] as? Number)?.toDouble() ?: 0.0,
                caloriesKcal = (map["calories_kcal"] as? Number)?.toInt() ?: 0,
                createdAt = (map["created_at"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

/**
 * Model tabeli Osiągnięć Użytkownika (user_achievements)
 * Przechowuje postęp w poziomowalnych osiągnięciach.
 */
data class UserAchievementDto(
    val userId: String = "",
    val achievementId: String = "",
    val currentLevel: Int = 0,
    val currentProgress: Double = 0.0,
    val updatedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "user_id" to userId,
        "achievement_id" to achievementId,
        "current_level" to currentLevel,
        "current_progress" to currentProgress,
        "updated_at" to updatedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserAchievementDto {
            return UserAchievementDto(
                userId = (map["user_id"] as? String).orEmpty(),
                achievementId = (map["achievement_id"] as? String).orEmpty(),
                currentLevel = (map["current_level"] as? Number)?.toInt() ?: 0,
                currentProgress = (map["current_progress"] as? Number)?.toDouble() ?: 0.0,
                updatedAt = (map["updated_at"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

/**
 * Model tabeli Wyzwań Tygodniowych (user_challenges)
 * Stan wyzwań tygodniowych przypisanych do danego tygodnia i użytkownika.
 */
data class UserChallengeDto(
    val id: String = "",
    val userId: String = "",
    val challengeId: String = "",
    val weekCode: String = "",
    val currentProgress: Float = 0.0f,
    val currentValue: Double = 0.0,
    val targetValue: Double = 100.0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "user_id" to userId,
        "challenge_id" to challengeId,
        "week_code" to weekCode,
        "current_progress" to currentProgress,
        "current_value" to currentValue,
        "target_value" to targetValue,
        "is_completed" to isCompleted,
        "is_claimed" to isClaimed
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserChallengeDto {
            return UserChallengeDto(
                id = (map["id"] as? String).orEmpty(),
                userId = (map["user_id"] as? String).orEmpty(),
                challengeId = (map["challenge_id"] as? String).orEmpty(),
                weekCode = (map["week_code"] as? String).orEmpty(),
                currentProgress = (map["current_progress"] as? Number)?.toFloat() ?: 0f,
                currentValue = (map["current_value"] as? Number)?.toDouble() ?: 0.0,
                targetValue = (map["target_value"] as? Number)?.toDouble() ?: 100.0,
                isCompleted = (map["is_completed"] as? Boolean) ?: false,
                isClaimed = (map["is_claimed"] as? Boolean) ?: false
            )
        }
    }
}

/**
 * Model tabeli Odebranych Kuponów (user_coupons / redeemed_rewards)
 * Bilet lub kupon odebrany przez użytkownika ze sklepu nagród.
 */
data class UserCouponDto(
    val couponId: String = "",
    val userId: String = "",
    val rewardId: String = "",
    val discountCode: String = "",
    val costPoints: Int = 0,
    val validityMinutes: Int = 20,
    val isActivated: Boolean = false,
    val activatedAt: Long? = null,
    val redeemedAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "coupon_id" to couponId,
        "user_id" to userId,
        "reward_id" to rewardId,
        "discount_code" to discountCode,
        "cost_points" to costPoints,
        "validity_minutes" to validityMinutes,
        "is_activated" to isActivated,
        "activated_at" to activatedAt,
        "redeemed_at" to redeemedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserCouponDto {
            return UserCouponDto(
                couponId = (map["coupon_id"] as? String).orEmpty(),
                userId = (map["user_id"] as? String).orEmpty(),
                rewardId = (map["reward_id"] as? String).orEmpty(),
                discountCode = (map["discount_code"] as? String).orEmpty(),
                costPoints = (map["cost_points"] as? Number)?.toInt() ?: 0,
                validityMinutes = (map["validity_minutes"] as? Number)?.toInt() ?: 20,
                isActivated = (map["is_activated"] as? Boolean) ?: false,
                activatedAt = (map["activated_at"] as? Number)?.toLong(),
                redeemedAt = (map["redeemed_at"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

/**
 * Model tabeli Ustawień (user_settings)
 * Ustawienia preferencji synchronizowane z chmurą Firebase.
 */
data class UserSettingsDto(
    val userId: String = "",
    val themeMode: String = "SYSTEM",
    val notificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
) {
    fun toMap(): Map<String, Any> = mapOf(
        "user_id" to userId,
        "theme_mode" to themeMode,
        "notifications_enabled" to notificationsEnabled,
        "sound_effects_enabled" to soundEffectsEnabled,
        "haptic_feedback_enabled" to hapticFeedbackEnabled
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserSettingsDto {
            return UserSettingsDto(
                userId = (map["user_id"] as? String).orEmpty(),
                themeMode = (map["theme_mode"] as? String) ?: "SYSTEM",
                notificationsEnabled = (map["notifications_enabled"] as? Boolean) ?: true,
                soundEffectsEnabled = (map["sound_effects_enabled"] as? Boolean) ?: true,
                hapticFeedbackEnabled = (map["haptic_feedback_enabled"] as? Boolean) ?: true
            )
        }
    }
}
