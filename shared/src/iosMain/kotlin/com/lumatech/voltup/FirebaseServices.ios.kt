package com.lumatech.voltup

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosMockAuthService : VoltUpAuthService {
    private val _currentUser = MutableStateFlow<VoltUpUser?>(null)
    override val currentUser: StateFlow<VoltUpUser?> = _currentUser.asStateFlow()

    override suspend fun signIn(email: String, password: String): AuthResult {
        val user = VoltUpUser(
            userId = "ios-mock-uid",
            email = email,
            userName = "iOS Tester",
            avatarId = 1,
            voltPoints = 150,
            totalEnergyWh = 3450.0,
            co2SavedKg = 2.41,
            userLevel = 2,
            streakDays = 3,
            lastActiveDate = "2026-10-17",
            createdAt = 1700000000000L
        )
        _currentUser.value = user
        return AuthResult(isSuccess = true, user = user)
    }

    override suspend fun signUp(email: String, password: String, userName: String, avatarId: Int): AuthResult {
        val user = VoltUpUser(
            userId = "ios-mock-uid",
            email = email,
            userName = userName,
            avatarId = avatarId,
            voltPoints = 0,
            totalEnergyWh = 0.0,
            co2SavedKg = 0.0,
            userLevel = 0,
            streakDays = 1,
            lastActiveDate = "2026-10-17",
            createdAt = 1700000000000L
        )
        _currentUser.value = user
        return AuthResult(isSuccess = true, user = user)
    }

    override suspend fun sendPasswordReset(email: String): AuthResult {
        return AuthResult(isSuccess = true)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): AuthResult {
        return AuthResult(isSuccess = true)
    }

    override suspend fun updateProfile(userName: String, avatarId: Int): AuthResult {
        val isEn = VoltUpStorage.getString(VoltUpPreferenceKeys.KEY_SAVED_LANGUAGE, "") == AppLanguage.ENGLISH.name
        val cur = _currentUser.value ?: return AuthResult(false, null, if (isEn) "No active session." else "Brak sesji")
        val updated = cur.copy(userName = userName, avatarId = avatarId)
        _currentUser.value = updated
        return AuthResult(true, updated)
    }

    override suspend fun deleteAccount(): AuthResult {
        val uid = _currentUser.value?.userId
        if (uid != null) {
            getDatabaseService().deleteUser(uid)
        }
        _currentUser.value = null
        return AuthResult(isSuccess = true)
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }
}

class IosMockDatabaseService : VoltUpDatabaseService {
    private var mockUser: VoltUpUser? = null
    private val sessions = mutableListOf<WorkoutSessionDto>()
    private val achievements = mutableListOf<UserAchievementDto>()
    private val challenges = mutableListOf<UserChallengeDto>()
    private val coupons = mutableListOf<UserCouponDto>()
    private var settings: UserSettingsDto? = null

    override suspend fun saveUser(user: VoltUpUser): Boolean {
        mockUser = user
        return true
    }

    override suspend fun getUser(userId: String): VoltUpUser? = mockUser

    override suspend fun getUserByEmail(email: String): VoltUpUser? =
        if (mockUser?.email?.equals(email.trim(), ignoreCase = true) == true) mockUser else null

    override suspend fun updateUserStats(
        userId: String,
        pointsDelta: Int,
        whDelta: Double,
        co2Delta: Double,
        newLevel: Int,
        rankingPointsDelta: Int
    ): Boolean {
        mockUser?.let {
            mockUser = it.copy(
                voltPoints = it.voltPoints + pointsDelta,
                rankingPoints = it.rankingPoints + rankingPointsDelta,
                totalEnergyWh = it.totalEnergyWh + whDelta,
                co2SavedKg = it.co2SavedKg + co2Delta,
                userLevel = newLevel
            )
        }
        return true
    }

    override suspend fun saveWorkoutSession(session: WorkoutSessionDto): Boolean {
        sessions.add(0, session)
        return true
    }

    override suspend fun getWorkoutSessions(userId: String): List<WorkoutSessionDto> = sessions

    override suspend fun saveAchievement(achievement: UserAchievementDto): Boolean {
        val idx = achievements.indexOfFirst { it.achievementId == achievement.achievementId }
        if (idx != -1) achievements[idx] = achievement else achievements.add(achievement)
        return true
    }

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> = achievements

    override suspend fun saveChallenge(challenge: UserChallengeDto): Boolean {
        val idx = challenges.indexOfFirst { it.challengeId == challenge.challengeId }
        if (idx != -1) challenges[idx] = challenge else challenges.add(challenge)
        return true
    }

    override suspend fun getUserChallenges(userId: String): List<UserChallengeDto> = challenges

    override suspend fun claimChallenge(userId: String, challengeId: String): Boolean {
        val idx = challenges.indexOfFirst { it.challengeId == challengeId }
        if (idx != -1) {
            challenges[idx] = challenges[idx].copy(isClaimed = true, isCompleted = true)
        }
        return true
    }

    override suspend fun saveCoupon(coupon: UserCouponDto): Boolean {
        coupons.add(0, coupon)
        return true
    }

    override suspend fun getUserCoupons(userId: String): List<UserCouponDto> {
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L
        val cutoff = currentTimeMillis() - thirtyDaysMs
        return coupons.filter { it.userId == userId && (it.redeemedAt == 0L || it.redeemedAt >= cutoff) }
    }

    override suspend fun updateCouponActivation(couponId: String, isActivated: Boolean, activatedAtMs: Long?): Boolean {
        val idx = coupons.indexOfFirst { it.couponId == couponId || it.discountCode == couponId }
        if (idx != -1) {
            coupons[idx] = coupons[idx].copy(isActivated = isActivated, activatedAt = activatedAtMs)
        }
        return true
    }

    override suspend fun deleteCoupon(userId: String, couponId: String): Boolean {
        coupons.removeAll { it.couponId == couponId || (it.userId == userId && it.discountCode == couponId) }
        return true
    }

    override suspend fun saveUserSettings(settings: UserSettingsDto): Boolean {
        this.settings = settings
        return true
    }

    override suspend fun getUserSettings(userId: String): UserSettingsDto? = settings

    override suspend fun getLeaderboard(limit: Int): List<LeaderboardUser> {
        val list = mutableListOf<LeaderboardUser>()
        mockUser?.let {
            list.add(LeaderboardUser(it.userId, it.userName, it.rankingPoints, it.userLevel, isCurrentUser = true))
        }
        return list.sortedByDescending { it.points }.take(limit)
    }

    override suspend fun deleteUser(userId: String): Boolean {
        if (mockUser?.userId == userId) mockUser = null
        sessions.removeAll { it.userId == userId }
        achievements.removeAll { it.userId == userId }
        challenges.removeAll { it.userId == userId }
        coupons.removeAll { it.userId == userId }
        settings = null
        return true
    }

    private val devices = mutableListOf<KineticStation>()

    override suspend fun getDevices(): List<KineticStation> {
        if (devices.isEmpty()) {
            devices.addAll(MockData.sampleStations)
        }
        return devices
    }

    override suspend fun saveDevice(device: KineticStation): Boolean {
        val idx = devices.indexOfFirst { it.id == device.id }
        if (idx != -1) devices[idx] = device else devices.add(device)
        return true
    }

    override fun clearCache() {
        mockUser = null
        sessions.clear()
        achievements.clear()
        challenges.clear()
        coupons.clear()
        settings = null
    }
}

private val iosAuthService by lazy { IosMockAuthService() }
private val iosDatabaseService by lazy { IosMockDatabaseService() }

actual fun getAuthService(): VoltUpAuthService = iosAuthService
actual fun getDatabaseService(): VoltUpDatabaseService = iosDatabaseService
