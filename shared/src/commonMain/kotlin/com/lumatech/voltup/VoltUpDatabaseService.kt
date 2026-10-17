package com.lumatech.voltup

interface VoltUpDatabaseService {
    // 1. Tabela Użytkowników (users)
    suspend fun saveUser(user: VoltUpUser): Boolean
    suspend fun getUser(userId: String): VoltUpUser?
    suspend fun getUserByEmail(email: String): VoltUpUser?
    suspend fun updateUserStats(
        userId: String,
        pointsDelta: Int,
        whDelta: Double,
        co2Delta: Double,
        newLevel: Int
    ): Boolean

    // 2. Tabela Historii Sesji Treningowych (workout_sessions)
    suspend fun saveWorkoutSession(session: WorkoutSessionDto): Boolean
    suspend fun getWorkoutSessions(userId: String): List<WorkoutSessionDto>

    // 3. Tabela Osiągnięć Użytkownika (user_achievements)
    suspend fun saveAchievement(achievement: UserAchievementDto): Boolean
    suspend fun getUserAchievements(userId: String): List<UserAchievementDto>

    // 4. Tabela Wyzwań Tygodniowych (user_challenges)
    suspend fun saveChallenge(challenge: UserChallengeDto): Boolean
    suspend fun getUserChallenges(userId: String): List<UserChallengeDto>
    suspend fun claimChallenge(userId: String, challengeId: String): Boolean

    // 5. Tabela Odebranych Kuponów (user_coupons / redeemed_rewards)
    suspend fun saveCoupon(coupon: UserCouponDto): Boolean
    suspend fun getUserCoupons(userId: String): List<UserCouponDto>
    suspend fun updateCouponActivation(couponId: String, isActivated: Boolean, activatedAtMs: Long?): Boolean

    // 6. Tabela Ustawień (user_settings)
    suspend fun saveUserSettings(settings: UserSettingsDto): Boolean
    suspend fun getUserSettings(userId: String): UserSettingsDto?

    // 7. Globalny Ranking (Leaderboard)
    suspend fun getLeaderboard(limit: Int = 50): List<LeaderboardUser>

    // 8. Usunięcie użytkownika i jego danych
    suspend fun deleteUser(userId: String): Boolean
}

expect fun getDatabaseService(): VoltUpDatabaseService
