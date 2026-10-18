package com.lumatech.voltup

interface VoltUpDatabaseService {
    suspend fun saveUser(user: VoltUpUser): Boolean
    suspend fun getUser(userId: String): VoltUpUser?
    suspend fun getUserByEmail(email: String): VoltUpUser?
    suspend fun updateUserStats(
        userId: String,
        pointsDelta: Int,
        whDelta: Double,
        co2Delta: Double,
        newLevel: Int,
        rankingPointsDelta: Int = 0
    ): Boolean

    suspend fun saveWorkoutSession(session: WorkoutSessionDto): Boolean
    suspend fun getWorkoutSessions(userId: String): List<WorkoutSessionDto>

    suspend fun saveAchievement(achievement: UserAchievementDto): Boolean
    suspend fun getUserAchievements(userId: String): List<UserAchievementDto>

    suspend fun saveChallenge(challenge: UserChallengeDto): Boolean
    suspend fun getUserChallenges(userId: String): List<UserChallengeDto>
    suspend fun claimChallenge(userId: String, challengeId: String): Boolean

    suspend fun saveCoupon(coupon: UserCouponDto): Boolean
    suspend fun getUserCoupons(userId: String): List<UserCouponDto>
    suspend fun updateCouponActivation(couponId: String, isActivated: Boolean, activatedAtMs: Long?): Boolean
    suspend fun deleteCoupon(userId: String, couponId: String): Boolean

    suspend fun saveUserSettings(settings: UserSettingsDto): Boolean
    suspend fun getUserSettings(userId: String): UserSettingsDto?

    suspend fun getLeaderboard(limit: Int = 50): List<LeaderboardUser>

    suspend fun deleteUser(userId: String): Boolean

    suspend fun getDevices(): List<KineticStation>
    suspend fun saveDevice(device: KineticStation): Boolean

    fun clearCache()
}

expect fun getDatabaseService(): VoltUpDatabaseService
