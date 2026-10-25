package com.lumatech.voltup

import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.ui.tooling.preview.Preview

enum class MainTab(val label: String) {
    HOME("Pulpit"),
    MAP("Mapa"),
    REWARDS("Nagrody"),
    PROFILE("Profil");

    val icon: ImageVector
        get() = when (this) {
            HOME -> Icons.Default.Dashboard
            MAP -> Icons.Default.Map
            REWARDS -> Icons.Default.CardGiftcard
            PROFILE -> Icons.Default.Person
        }
}

sealed interface Screen {
    data object Login : Screen
    data object Register : Screen
    data object ForgotPassword : Screen
    data object Onboarding : Screen
    data object Main : Screen
    data class ActiveWorkout(val station: KineticStation) : Screen
    data object Settings : Screen
    data class QrScanner(val initialStation: KineticStation? = null) : Screen
    data object Achievements : Screen
    data object ManageAccount : Screen
    data object HelpGuide : Screen
    data object WorkoutHistory : Screen
}

class BottomBarWithNotchShape(
    private val notchRadius: Dp = 46.dp,
    private val notchDepth: Dp = 36.dp,
    private val cornerRadius: Dp = 20.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cornerRadius.toPx() }
        val nr = with(density) { notchRadius.toPx() }
        val nd = with(density) { notchDepth.toPx() }
        val cx = size.width / 2f

        val path = Path().apply {
            moveTo(0f, r)
            arcTo(
                rect = Rect(0f, 0f, 2 * r, 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(cx - nr - 12f, 0f)

            cubicTo(
                cx - nr / 1.6f, 0f,
                cx - nr / 1.8f, nd,
                cx, nd
            )
            cubicTo(
                cx + nr / 1.8f, nd,
                cx + nr / 1.6f, 0f,
                cx + nr + 12f, 0f
            )

            lineTo(size.width - r, 0f)
            arcTo(
                rect = Rect(size.width - 2 * r, 0f, size.width, 2 * r),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        return Outline.Generic(path)
    }
}

@Composable
fun App(
    previewScreenName: String? = null,
    previewThemeName: String? = null
) {
    val authService = remember { getAuthService() }
    val dbService = remember { getDatabaseService() }
    val currentAuthUser by authService.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val initialLanguage = remember {
        val langName = VoltUpStorage.getString(VoltUpPreferenceKeys.KEY_SAVED_LANGUAGE, "")
        AppLanguage.entries.find { it.name == langName } ?: AppLanguage.POLISH
    }
    val initialTheme = remember(previewThemeName) {
        if (previewThemeName != null) {
            if (previewThemeName.equals("LIGHT", ignoreCase = true)) AppThemeMode.LIGHT else AppThemeMode.DARK
        } else {
            val themeName = VoltUpStorage.getString(VoltUpPreferenceKeys.KEY_SAVED_THEME, "")
            AppThemeMode.entries.find { it.name == themeName } ?: AppThemeMode.SYSTEM
        }
    }
    val savedEmail = remember { VoltUpStorage.getString(VoltUpPreferenceKeys.KEY_SAVED_EMAIL, "") }
    val savedPassword = remember { VoltUpStorage.getString(VoltUpPreferenceKeys.KEY_SAVED_PASSWORD, "") }
    val rememberMe = remember { VoltUpStorage.getBoolean(VoltUpPreferenceKeys.KEY_REMEMBER_ME, false) }

    var appSettings by remember(initialTheme) { mutableStateOf(AppSettings(themeMode = initialTheme, language = initialLanguage)) }
    var isSplashVisible by remember { mutableStateOf(previewScreenName == null) }
    val hasValidAutoLogin = rememberMe && (currentAuthUser != null || (savedEmail.isNotBlank() && savedPassword.isNotBlank()))
    var isAutoLoggingIn by remember { mutableStateOf(if (previewScreenName != null) false else hasValidAutoLogin) }

    val initialScreen = remember(previewScreenName) {
        when (previewScreenName?.uppercase()) {
            "LOGIN" -> Screen.Login
            "REGISTER" -> Screen.Register
            "FORGOT_PASSWORD" -> Screen.ForgotPassword
            "ONBOARDING" -> Screen.Onboarding
            "HOME", "MAP", "REWARDS", "PROFILE" -> Screen.Main
            "WORKOUT" -> Screen.ActiveWorkout(MockData.sampleStations.first())
            "ACHIEVEMENTS" -> Screen.Achievements
            "HISTORY" -> Screen.WorkoutHistory
            "SETTINGS" -> Screen.Settings
            "MANAGE_ACCOUNT" -> Screen.ManageAccount
            "HELP" -> Screen.HelpGuide
            "QR" -> Screen.QrScanner()
            else -> if (hasValidAutoLogin) Screen.Main else Screen.Login
        }
    }
    var currentScreen by remember(initialScreen) { mutableStateOf<Screen>(initialScreen) }
    val initialTab = remember(previewScreenName) {
        when (previewScreenName?.uppercase()) {
            "MAP" -> MainTab.MAP
            "REWARDS" -> MainTab.REWARDS
            "PROFILE" -> MainTab.PROFILE
            else -> MainTab.HOME
        }
    }
    var currentTab by remember(initialTab) { mutableStateOf(initialTab) }
    var userStats by remember(previewScreenName) {
        mutableStateOf(
            if (previewScreenName != null) {
                UserStats(
                    points = 1850,
                    rankingPoints = 1850,
                    totalEnergyWh = 470.0,
                    weeklyEnergyWh = 470.0,
                    co2SavedKg = 0.33,
                    userName = "Jan Kowalski",
                    avatarId = 1,
                    level = 3,
                    levelTitle = "Miejski Odkrywca",
                    streakDays = 5,
                    totalCaloriesBurned = 333,
                    activeCardioMinutes = 47,
                    recentWorkouts = MockData.sampleRecentWorkouts
                )
            } else {
                UserStats(
                    points = 0,
                    totalEnergyWh = 0.0,
                    weeklyEnergyWh = 0.0,
                    co2SavedKg = 0.0,
                    userName = "Użytkownik VoltUp",
                    avatarId = 1,
                    level = 0,
                    levelTitle = "Nowicjusz VoltUp",
                    streakDays = 1,
                    recentWorkouts = emptyList()
                )
            }
        )
    }
    var achievements by remember(previewScreenName) {
        mutableStateOf(
            if (previewScreenName != null) {
                DefaultAchievements.initialAchievements.mapIndexed { idx, ach ->
                    if (idx < 5) ach.copy(level = 1, currentProgress = 5.0) else ach
                }
            } else {
                currentAuthUser?.userId?.takeIf { it.isNotBlank() }?.let { uid ->
                    getCachedAchievementsForUser(uid)
                } ?: DefaultAchievements.initialAchievements
            }
        )
    }
    var challenges by remember { mutableStateOf(MockData.sampleChallenges) }
    var redeemedRewards by remember(previewScreenName) {
        mutableStateOf(
            if (previewScreenName != null) {
                listOf(
                    RedeemedReward(
                        id = "RED-1",
                        reward = MockData.sampleRewards[1],
                        discountCode = "MPK-KINETIC-20",
                        redeemedDate = "18.10.2026"
                    )
                )
            } else emptyList()
        )
    }

    LaunchedEffect(Unit) {
        if (previewScreenName != null) {
            isSplashVisible = false
            isAutoLoggingIn = false
            return@LaunchedEffect
        }
        if (!rememberMe) {
            if (currentAuthUser != null) {
                authService.signOut()
            }
            isAutoLoggingIn = false
            currentScreen = Screen.Login
        } else {
            if (currentAuthUser == null && savedEmail.isNotBlank() && savedPassword.isNotBlank()) {
                isAutoLoggingIn = true
                val result = authService.signIn(savedEmail, savedPassword)
                if (result.isSuccess && result.user != null) {
                    currentScreen = Screen.Main
                } else {
                    isAutoLoggingIn = false
                    currentScreen = Screen.Login
                }
            }
        }
        delay(1200.milliseconds)
        isSplashVisible = false
        if (rememberMe && currentAuthUser != null) {
            delay(700.milliseconds)
            isAutoLoggingIn = false
        } else {
            isAutoLoggingIn = false
        }
    }

    fun resetAppStateToDefaults() {
        val uid = currentAuthUser?.userId.orEmpty()
        dbService.clearCache()
        userStats = UserStats(
            points = 0,
            rankingPoints = 0,
            totalEnergyWh = 0.0,
            weeklyEnergyWh = 0.0,
            co2SavedKg = 0.0,
            userName = "Użytkownik VoltUp",
            avatarId = 1,
            level = 0,
            levelTitle = calculateLevelTitle(0),
            streakDays = 1,
            recentWorkouts = emptyList(),
            accountCreatedAt = 0L
        )
        achievements = DefaultAchievements.initialAchievements.map {
            it.copy(level = 0, currentProgress = 0.0)
        }
        challenges = MockData.sampleChallenges.map {
            it.copy(currentValue = 0.0, currentProgress = 0.0f, isCompleted = false, isClaimed = false)
        }
        redeemedRewards = emptyList()

        if (uid.isNotBlank()) {
            listOf(
                "ach_about_viewed", "ach_dark_theme", "ach_profile_custom", "ach_archive_viewed",
                "ach_explore_history", "ach_qr_scanner", "ach_map_navigator", "ach_weekly_challenges", "ach_first_reward"
            ).forEach { k ->
                VoltUpStorage.setBoolean("${uid}_$k", false)
                VoltUpStorage.setBoolean("${uid}_ach_$k", false)
            }
        }
    }

    LaunchedEffect(currentAuthUser) {
        if (previewScreenName != null) return@LaunchedEffect
        val user = currentAuthUser
        if (user == null) {
            resetAppStateToDefaults()
            isAutoLoggingIn = false
            if (currentScreen != Screen.Login && currentScreen != Screen.Register && currentScreen != Screen.ForgotPassword && currentScreen != Screen.Onboarding) {
                currentScreen = Screen.Login
            }
        } else {
            achievements = getCachedAchievementsForUser(user.userId)
        }
    }

    LaunchedEffect(currentAuthUser?.userId) {
        if (previewScreenName != null) return@LaunchedEffect
        val u = currentAuthUser
        if (u == null) {
            resetAppStateToDefaults()
        } else {
            val freshUser = dbService.getUser(u.userId) ?: u
            val workouts = dbService.getWorkoutSessions(u.userId)
            val loadedSettings = dbService.getUserSettings(u.userId)
            val loadedAchievements = dbService.getUserAchievements(u.userId)
            val loadedChallenges = dbService.getUserChallenges(u.userId)
            val loadedCoupons = dbService.getUserCoupons(u.userId)

            val calculatedLevel = calculateUserLevel(freshUser.rankingPoints)
            userStats = userStats.copy(
                points = freshUser.voltPoints,
                rankingPoints = freshUser.rankingPoints,
                totalEnergyWh = freshUser.totalEnergyWh,
                co2SavedKg = freshUser.co2SavedKg,
                userName = freshUser.userName.ifBlank { "Użytkownik VoltUp" },
                avatarId = freshUser.avatarId,
                level = calculatedLevel,
                levelTitle = calculateLevelTitle(calculatedLevel),
                streakDays = freshUser.streakDays,
                accountCreatedAt = freshUser.createdAt,
                recentWorkouts = workouts.map { w ->
                    WorkoutSessionRecord(
                        id = w.sessionId,
                        stationName = w.stationName,
                        stationLocation = w.stationLocation,
                        dateText = formatSessionDate(w.createdAt).ifBlank { "" },
                        durationSeconds = w.durationSeconds,
                        generatedWh = w.generatedWh,
                        earnedPoints = w.earnedPoints,
                        co2SavedKg = w.co2SavedKg,
                        caloriesKcal = w.caloriesKcal,
                        timestampMs = w.createdAt
                    )
                }
            )

            if (loadedSettings != null) {
                val themeModeEnum = try {
                    AppThemeMode.valueOf(loadedSettings.themeMode)
                } catch (_: Exception) {
                    AppThemeMode.SYSTEM
                }
                VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_THEME, themeModeEnum.name)
                appSettings = appSettings.copy(
                    themeMode = themeModeEnum,
                    notificationsEnabled = loadedSettings.notificationsEnabled,
                    soundEffectsEnabled = loadedSettings.soundEffectsEnabled,
                    hapticFeedbackEnabled = loadedSettings.hapticFeedbackEnabled
                )
            }

            loadedAchievements.forEach { dto ->
                if (dto.currentLevel > 0) {
                    for (lvl in 1..dto.currentLevel) {
                        VoltUpStorage.setBoolean("${u.userId}_ach_awarded_${dto.achievementId}_lvl_$lvl", true)
                    }
                    val existingSavedLvl = VoltUpStorage.getString("${u.userId}_ach_level_${dto.achievementId}", "0").toIntOrNull() ?: 0
                    if (dto.currentLevel > existingSavedLvl) {
                        VoltUpStorage.setString("${u.userId}_ach_level_${dto.achievementId}", "${dto.currentLevel}")
                    }
                }
            }

            achievements = DefaultAchievements.initialAchievements.map { ach ->
                val match = loadedAchievements.firstOrNull { it.achievementId == ach.id }
                val savedLvl = VoltUpStorage.getString("${u.userId}_ach_level_${ach.id}", "0").toIntOrNull() ?: 0
                val bestLvl = maxOf(match?.currentLevel ?: 0, savedLvl, ach.level)
                val bestProgress = maxOf(match?.currentProgress ?: 0.0, ach.currentProgress)
                if (bestLvl > 0) {
                    for (lvl in 1..bestLvl) {
                        VoltUpStorage.setBoolean("${u.userId}_ach_awarded_${ach.id}_lvl_$lvl", true)
                    }
                    VoltUpStorage.setString("${u.userId}_ach_level_${ach.id}", "$bestLvl")
                }
                ach.copy(
                    level = bestLvl,
                    currentProgress = bestProgress
                )
            }

            val evalOnLoad = evaluateAchievements(
                currentAchievements = achievements,
                workouts = userStats.recentWorkouts,
                totalEnergyWh = userStats.totalEnergyWh,
                co2SavedKg = userStats.co2SavedKg,
                streakDays = userStats.streakDays,
                redeemedRewardsCount = loadedCoupons.size,
                isEnglish = appSettings.language == AppLanguage.ENGLISH,
                isDarkTheme = appSettings.themeMode == AppThemeMode.DARK,
                userId = u.userId
            )
            achievements = evalOnLoad.updatedAchievements
            if (evalOnLoad.bonusPointsEarned > 0) {
                val newRankingPoints = userStats.rankingPoints + evalOnLoad.bonusPointsEarned
                val newLevel = calculateUserLevel(newRankingPoints)
                val newTitle = calculateLevelTitle(newLevel)
                userStats = userStats.copy(
                    rankingPoints = newRankingPoints,
                    level = newLevel,
                    levelTitle = newTitle
                )
                coroutineScope.launch {
                    dbService.updateUserStats(
                        userId = u.userId,
                        pointsDelta = 0,
                        whDelta = 0.0,
                        co2Delta = 0.0,
                        newLevel = newLevel,
                        rankingPointsDelta = evalOnLoad.bonusPointsEarned
                    )
                    evalOnLoad.updatedAchievements.forEach { ach ->
                        dbService.saveAchievement(
                            UserAchievementDto(
                                userId = u.userId,
                                achievementId = ach.id,
                                currentLevel = ach.level,
                                currentProgress = ach.currentProgress,
                                updatedAt = currentTimeMillis()
                            )
                        )
                    }
                }
            }

            challenges = MockData.sampleChallenges.map { ch ->
                val cleanId = ch.id.removePrefix("${u.userId}_")
                val match = loadedChallenges.firstOrNull {
                    it.challengeId == ch.id || it.challengeId == cleanId || it.challengeId == "${u.userId}_${ch.id}"
                }
                if (match != null) {
                    val currVal = if (match.currentValue > 0) match.currentValue else (match.currentProgress * ch.targetValue)
                    ch.copy(
                        currentValue = currVal,
                        currentProgress = match.currentProgress,
                        isCompleted = match.isCompleted,
                        isClaimed = match.isClaimed
                    )
                } else {
                    ch.copy(
                        currentValue = 0.0,
                        currentProgress = 0.0f,
                        isCompleted = false,
                        isClaimed = false
                    )
                }
            }

            if (loadedCoupons.isNotEmpty()) {
                val cutoff = currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L
                redeemedRewards = loadedCoupons
                    .filter { !it.rewardId.contains("RANK") && (it.redeemedAt == 0L || it.redeemedAt >= cutoff) }
                    .map { c ->
                        val r = MockData.sampleRewards.firstOrNull { it.id == c.rewardId }
                            ?: Reward(
                                id = c.rewardId,
                                title = "Kupon VoltUp",
                                partner = "Partner Miejski",
                                costPoints = c.costPoints,
                                category = RewardCategory.ALL,
                                description = "Odebrany bilet/kupon",
                                discountCode = c.discountCode,
                                validityMinutes = c.validityMinutes
                            )
                        RedeemedReward(
                            id = c.couponId.ifBlank { "${c.rewardId}_${c.discountCode}" },
                            reward = r,
                            discountCode = c.discountCode,
                            redeemedDate = "Zapisany",
                            isReadyToScan = c.isReadyToScan,
                            isActivated = c.isActivated,
                            activatedTimestamp = c.activatedAt ?: 0L,
                            validityMinutes = c.validityMinutes
                        )
                    }
            } else {
                redeemedRewards = emptyList()
            }
        }
    }

    val checkAndAwardAchievements: (triggerKey: String?, showSnackbar: Boolean) -> Unit = { triggerKey, showSnackbar ->
        if (previewScreenName == null) {
            val uid = currentAuthUser?.userId.orEmpty()
        if (triggerKey != null && uid.isNotBlank()) {
            VoltUpStorage.setBoolean("${uid}_$triggerKey", true)
            val cleanKey = triggerKey.removePrefix("ach_")
            VoltUpStorage.setBoolean("${uid}_ach_$cleanKey", true)
        }

        val evalResult = evaluateAchievements(
            currentAchievements = achievements,
            workouts = userStats.recentWorkouts,
            totalEnergyWh = userStats.totalEnergyWh,
            co2SavedKg = userStats.co2SavedKg,
            streakDays = userStats.streakDays,
            redeemedRewardsCount = redeemedRewards.size,
            isEnglish = appSettings.language == AppLanguage.ENGLISH,
            isDarkTheme = appSettings.themeMode == AppThemeMode.DARK,
            userId = uid
        )

        if (evalResult.bonusPointsEarned > 0 || evalResult.updatedAchievements != achievements) {
            achievements = evalResult.updatedAchievements
            val pts = evalResult.bonusPointsEarned
            if (pts > 0) {
                val newRankingPoints = userStats.rankingPoints + pts
                val newLevel = calculateUserLevel(newRankingPoints)
                val newTitle = calculateLevelTitle(newLevel)

                userStats = userStats.copy(
                    rankingPoints = newRankingPoints,
                    level = newLevel,
                    levelTitle = newTitle
                )

                if (showSnackbar) {
                    evalResult.levelUpMessages.forEach { msg ->
                        SnackbarHelper.show(
                            text = msg,
                            icon = Icons.Default.EmojiEvents
                        )
                    }
                }

                currentAuthUser?.let { u ->
                    coroutineScope.launch {
                        dbService.updateUserStats(
                            userId = u.userId,
                            pointsDelta = 0,
                            whDelta = 0.0,
                            co2Delta = 0.0,
                            newLevel = newLevel,
                            rankingPointsDelta = pts
                        )
                        evalResult.updatedAchievements.forEach { ach ->
                            dbService.saveAchievement(
                                UserAchievementDto(
                                    userId = u.userId,
                                    achievementId = ach.id,
                                    currentLevel = ach.level,
                                    currentProgress = ach.currentProgress,
                                    updatedAt = currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            } else {
                currentAuthUser?.let { u ->
                    coroutineScope.launch {
                        evalResult.updatedAchievements.forEach { ach ->
                            dbService.saveAchievement(
                                UserAchievementDto(
                                    userId = u.userId,
                                    achievementId = ach.id,
                                    currentLevel = ach.level,
                                    currentProgress = ach.currentProgress,
                                    updatedAt = currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }
        }
        }
    }

    val canGoBack = when (currentScreen) {
        Screen.Login, Screen.Onboarding -> false
        Screen.Register, Screen.ForgotPassword -> true
        Screen.Main -> currentTab != MainTab.HOME
        else -> true
    }
    CommonBackHandler(enabled = canGoBack) {
        when (currentScreen) {
            is Screen.Register -> currentScreen = Screen.Login
            is Screen.ForgotPassword -> currentScreen = Screen.Login
            is Screen.Settings -> currentScreen = Screen.Main
            is Screen.ManageAccount -> currentScreen = Screen.Settings
            is Screen.HelpGuide -> currentScreen = Screen.Settings
            is Screen.QrScanner -> currentScreen = Screen.Main
            is Screen.Achievements -> currentScreen = Screen.Main
            is Screen.WorkoutHistory -> currentScreen = Screen.Main
            is Screen.ActiveWorkout -> { }
            is Screen.Main -> if (currentTab != MainTab.HOME) currentTab = MainTab.HOME
            is Screen.Login, is Screen.Onboarding -> { }
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides appSettings.language) {
        VoltUpTheme(themeMode = appSettings.themeMode) {
            Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (previewScreenName != null) {
                            androidx.compose.animation.EnterTransition.None togetherWith androidx.compose.animation.ExitTransition.None
                        } else {
                        val isEnteringSubScreen = targetState is Screen.Settings ||
                                targetState is Screen.HelpGuide ||
                                targetState is Screen.ManageAccount ||
                                targetState is Screen.ActiveWorkout ||
                                targetState is Screen.WorkoutHistory ||
                                targetState is Screen.Achievements ||
                                targetState is Screen.Register ||
                                targetState is Screen.ForgotPassword

                        val isReturningToMainOrLogin = (targetState is Screen.Main && initialState !is Screen.Login && initialState !is Screen.Register && initialState !is Screen.Onboarding) ||
                                (targetState is Screen.Login && (initialState is Screen.Register || initialState is Screen.ForgotPassword))

                        if (isReturningToMainOrLogin) {
                            (fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                                    scaleIn(animationSpec = androidx.compose.animation.core.tween(260), initialScale = 0.96f))
                                .togetherWith(
                                    fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                                            slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(240)) { it / 4 }
                                )
                        } else if (isEnteringSubScreen) {
                            (fadeIn(animationSpec = androidx.compose.animation.core.tween(260)) +
                                    slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(280)) { it / 4 } +
                                    scaleIn(animationSpec = androidx.compose.animation.core.tween(280), initialScale = 0.96f))
                                .togetherWith(
                                    fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                                            scaleOut(animationSpec = androidx.compose.animation.core.tween(220), targetScale = 0.96f)
                                )
                        } else {
                            (fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                                    slideInVertically(animationSpec = androidx.compose.animation.core.tween(240)) { it / 12 })
                                .togetherWith(
                                    fadeOut(animationSpec = androidx.compose.animation.core.tween(180))
                                )
                        }
                    }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Login -> {
                            LoginScreen(
                                authService = authService,
                                onNavigateToRegister = { currentScreen = Screen.Register },
                                onNavigateToForgotPassword = { currentScreen = Screen.ForgotPassword },
                                onLoginSuccess = { user ->
                                    currentScreen = Screen.Main
                                    SnackbarHelper.show(polishText = "Witaj ponownie w VoltUp, ${user.userName}!", englishText = "Welcome back to VoltUp, ${user.userName}!")
                                }
                            )
                        }

                        is Screen.Register -> {
                            RegisterScreen(
                                authService = authService,
                                onNavigateToLogin = { currentScreen = Screen.Login },
                                onRegisterSuccess = { user ->
                                    val onboardingCompleted = VoltUpStorage.getBoolean(VoltUpPreferenceKeys.KEY_ONBOARDING_COMPLETED, false)
                                    currentScreen = if (!onboardingCompleted) Screen.Onboarding else Screen.Main
                                    SnackbarHelper.show(polishText = "Konto utworzone! Witaj w VoltUp, ${user.userName}!", englishText = "Account created! Welcome to VoltUp, ${user.userName}!")
                                }
                            )
                        }

                        is Screen.Onboarding -> {
                            OnboardingScreen(
                                onFinish = {
                                    currentScreen = Screen.Main
                                }
                            )
                        }

                        is Screen.ForgotPassword -> {
                            ForgotPasswordScreen(
                                authService = authService,
                                onNavigateBack = { currentScreen = Screen.Login }
                            )
                        }

                        is Screen.Settings -> {
                            SettingsScreen(
                                settings = appSettings,
                                onSettingsChanged = { newSettings ->
                                    appSettings = newSettings
                                    VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_THEME, newSettings.themeMode.name)
                                    VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_LANGUAGE, newSettings.language.name)
                                    currentAuthUser?.let { u ->
                                        coroutineScope.launch {
                                            dbService.saveUserSettings(
                                                UserSettingsDto(
                                                    userId = u.userId,
                                                    themeMode = newSettings.themeMode.name,
                                                    notificationsEnabled = newSettings.notificationsEnabled,
                                                    soundEffectsEnabled = newSettings.soundEffectsEnabled,
                                                    hapticFeedbackEnabled = newSettings.hapticFeedbackEnabled
                                                )
                                            )
                                        }
                                    }
                                },
                                onNavigateBack = { currentScreen = Screen.Main },
                                onClearHistory = {
                                    userStats = userStats.copy(recentWorkouts = emptyList())
                                },
                                onResetDefaults = {
                                    val defaultSettings = AppSettings()
                                    appSettings = defaultSettings
                                    VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_THEME, defaultSettings.themeMode.name)
                                    VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_LANGUAGE, defaultSettings.language.name)
                                    SnackbarHelper.show(polishText = "Przywrócono ustawienia domyślne.", englishText = "Default settings restored.")
                                },
                                authService = authService,
                                userEmail = currentAuthUser?.email.orEmpty(),
                                onSignOut = {
                                    coroutineScope.launch {
                                        VoltUpStorage.setBoolean(VoltUpPreferenceKeys.KEY_REMEMBER_ME, false)
                                        VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_PASSWORD, "")
                                        authService.signOut()
                                        currentScreen = Screen.Login
                                        SnackbarHelper.show(polishText = "Wylogowano z konta.", englishText = "Logged out successfully.")
                                    }
                                },
                                onNavigateToManageAccount = {
                                    currentScreen = Screen.ManageAccount
                                },
                                onNavigateToHelpGuide = {
                                    currentScreen = Screen.HelpGuide
                                },
                                onTriggerAchievement = { key ->
                                    checkAndAwardAchievements(key, true)
                                }
                            )
                        }

                        is Screen.HelpGuide -> {
                            HelpGuideScreen(
                                onNavigateBack = { currentScreen = Screen.Settings }
                            )
                        }

                        is Screen.ManageAccount -> {
                            ManageAccountScreen(
                                userStats = userStats,
                                userEmail = if (previewScreenName != null) "jan.kowalski@voltup.pl" else currentAuthUser?.email.orEmpty(),
                                authService = authService,
                                onNavigateBack = { currentScreen = Screen.Settings },
                                onSignOut = {
                                    coroutineScope.launch {
                                        VoltUpStorage.setBoolean(VoltUpPreferenceKeys.KEY_REMEMBER_ME, false)
                                        VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_PASSWORD, "")
                                        authService.signOut()
                                        resetAppStateToDefaults()
                                        currentScreen = Screen.Login
                                        SnackbarHelper.show(polishText = "Wylogowano z konta.", englishText = "Logged out successfully.")
                                    }
                                },
                                onAccountDeleted = {
                                    VoltUpStorage.setBoolean(VoltUpPreferenceKeys.KEY_REMEMBER_ME, false)
                                    VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_PASSWORD, "")
                                    VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_EMAIL, "")
                                    resetAppStateToDefaults()
                                    currentScreen = Screen.Login
                                    SnackbarHelper.show(polishText = "Twoje konto zostało usunięte.", englishText = "Your account has been deleted.")
                                }
                            )
                        }

                        is Screen.QrScanner -> {
                            LaunchedEffect(Unit) {
                                checkAndAwardAchievements("ach_qr_scanner", true)
                            }
                            QrScannerScreen(
                                initialStation = screen.initialStation,
                                onStationDetected = { station ->
                                    currentScreen = Screen.ActiveWorkout(station)
                                },
                                onDismiss = { currentScreen = Screen.Main }
                            )
                        }

                        is Screen.Achievements -> {
                            LaunchedEffect(Unit) {
                                checkAndAwardAchievements(null, false)
                            }
                            AchievementsScreen(
                                badges = achievements.map { it.toBadgeModel(isEnglish = appSettings.language == AppLanguage.ENGLISH) },
                                userStats = userStats,
                                onNavigateBack = { currentScreen = Screen.Main }
                            )
                        }

                        is Screen.WorkoutHistory -> {
                            LaunchedEffect(Unit) {
                                checkAndAwardAchievements("ach_explore_history", true)
                            }
                            WorkoutHistoryScreen(
                                workouts = userStats.recentWorkouts,
                                onNavigateBack = { currentScreen = Screen.Main }
                            )
                        }

                        is Screen.ActiveWorkout -> {
                            WorkoutScreen(
                                station = screen.station,
                                onFinishWorkout = { earnedWh, earnedPoints, workoutRecord ->
                                    val newWorkouts = listOf(workoutRecord) + userStats.recentWorkouts
                                    val newTotalWh = userStats.totalEnergyWh + earnedWh
                                    val newCo2 = userStats.co2SavedKg + (earnedWh * 0.0007)

                                    val evalResult = evaluateAchievements(
                                        currentAchievements = achievements,
                                        workouts = newWorkouts,
                                        totalEnergyWh = newTotalWh,
                                        co2SavedKg = newCo2,
                                        streakDays = userStats.streakDays,
                                        redeemedRewardsCount = redeemedRewards.size,
                                        isEnglish = appSettings.language == AppLanguage.ENGLISH,
                                        isDarkTheme = appSettings.themeMode == AppThemeMode.DARK,
                                        userId = currentAuthUser?.userId.orEmpty()
                                    )

                                    achievements = evalResult.updatedAchievements
                                    val newRankingPoints = userStats.rankingPoints + evalResult.bonusPointsEarned
                                    val totalPoints = userStats.points + earnedPoints
                                    val newLevel = calculateUserLevel(newRankingPoints)
                                    val newTitle = calculateLevelTitle(newLevel)

                                    userStats = userStats.copy(
                                        rankingPoints = newRankingPoints,
                                        points = totalPoints,
                                        voltPoints = totalPoints,
                                        totalEnergyWh = newTotalWh,
                                        totalCaloriesBurned = userStats.totalCaloriesBurned + workoutRecord.caloriesKcal,
                                        co2SavedKg = newCo2,
                                        level = newLevel,
                                        levelTitle = newTitle,
                                        recentWorkouts = newWorkouts
                                    )

                                    if (evalResult.bonusPointsEarned > 0) {
                                        evalResult.levelUpMessages.forEach { msg ->
                                            SnackbarHelper.show(
                                                text = msg,
                                                icon = Icons.Default.EmojiEvents
                                            )
                                        }
                                    }

                                    val sessionPeakWatts = workoutRecord.generatedWh * 3600.0 / workoutRecord.durationSeconds.coerceAtLeast(1)
                                    challenges = challenges.map { ch ->
                                        if (!ch.isCompleted) {
                                            val newCurrVal = when (ch.id) {
                                                "C1" -> ch.currentValue + workoutRecord.caloriesKcal
                                                "C2" -> ch.currentValue + 1.0
                                                "C3" -> maxOf(ch.currentValue, sessionPeakWatts)
                                                else -> ch.currentValue + earnedWh
                                            }
                                            val newProg = (newCurrVal / ch.targetValue.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                                            ch.copy(
                                                currentValue = newCurrVal,
                                                currentProgress = newProg,
                                                isCompleted = newProg >= 1f
                                            )
                                        } else ch
                                    }

                                    currentAuthUser?.let { u ->
                                        coroutineScope.launch {
                                            dbService.saveWorkoutSession(
                                                WorkoutSessionDto(
                                                    sessionId = workoutRecord.id,
                                                    userId = u.userId,
                                                    stationId = screen.station.id,
                                                    stationName = screen.station.name,
                                                    stationLocation = screen.station.location,
                                                    durationSeconds = workoutRecord.durationSeconds,
                                                    generatedWh = workoutRecord.generatedWh,
                                                    earnedPoints = workoutRecord.earnedPoints,
                                                    co2SavedKg = workoutRecord.co2SavedKg,
                                                    caloriesKcal = workoutRecord.caloriesKcal,
                                                    createdAt = if (workoutRecord.timestampMs > 0L) workoutRecord.timestampMs else currentTimeMillis()
                                                )
                                            )

                                            dbService.updateUserStats(
                                                userId = u.userId,
                                                pointsDelta = earnedPoints,
                                                whDelta = earnedWh,
                                                co2Delta = earnedWh * 0.0007,
                                                newLevel = newLevel,
                                                rankingPointsDelta = evalResult.bonusPointsEarned
                                            )

                                            achievements.forEach { ach ->
                                                dbService.saveAchievement(
                                                    UserAchievementDto(
                                                        userId = u.userId,
                                                        achievementId = ach.id,
                                                        currentLevel = ach.level,
                                                        currentProgress = ach.currentProgress,
                                                        updatedAt = 0L
                                                    )
                                                )
                                            }

                                            challenges.forEach { ch ->
                                                dbService.saveChallenge(
                                                    UserChallengeDto(
                                                        id = "${u.userId}_${ch.id}",
                                                        userId = u.userId,
                                                        challengeId = ch.id,
                                                        weekCode = "2026-W42",
                                                        currentProgress = ch.currentProgress,
                                                        currentValue = ch.currentValue,
                                                        targetValue = ch.targetValue,
                                                        isCompleted = ch.isCompleted,
                                                        isClaimed = ch.isClaimed
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    currentScreen = Screen.Main
                                    SnackbarHelper.show(polishText = "Sesja zakończona!", englishText = "Workout completed!", icon = Icons.Default.Check)
                                    if (evalResult.bonusPointsEarned > 0) {
                                        evalResult.levelUpMessages.forEach { achMsg ->
                                            SnackbarHelper.show(
                                                text = achMsg,
                                                icon = Icons.Default.EmojiEvents
                                            )
                                        }
                                    }
                                },
                                onCancel = {
                                    currentScreen = Screen.Main
                                }
                            )
                        }

                        is Screen.Main -> {
                            val coroutineScope = rememberCoroutineScope()
                            val pagerState = rememberPagerState(initialPage = currentTab.ordinal) { MainTab.entries.size }

                            LaunchedEffect(pagerState) {
                                snapshotFlow { pagerState.settledPage }.collect { settledPage ->
                                    val selectedTab = MainTab.entries[settledPage]
                                    if (currentTab != selectedTab) {
                                        currentTab = selectedTab
                                    }
                                }
                            }

                            LaunchedEffect(currentTab) {
                                if (pagerState.targetPage != currentTab.ordinal) {
                                    pagerState.animateScrollToPage(currentTab.ordinal)
                                }
                            }

                            Scaffold(
                                bottomBar = {
                                    VoltUpBottomBar(
                                        currentTab = currentTab,
                                        onTabSelected = { currentTab = it },
                                        onQrClick = { currentScreen = Screen.QrScanner() }
                                    )
                                }
                            ) { innerPadding ->
                                HorizontalPager(
                                    state = pagerState,
                                    userScrollEnabled = true,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) { page ->
                                    when (MainTab.entries[page]) {
                                        MainTab.HOME -> {
                                            LaunchedEffect(Unit) {
                                                checkAndAwardAchievements("ach_weekly_challenges", true)
                                            }
                                            DashboardScreen(
                                                stats = userStats,
                                                onOpenSettings = { currentScreen = Screen.Settings },
                                                onGoToMap = { currentTab = MainTab.MAP },
                                                onScanStation = { station -> currentScreen = Screen.QrScanner(initialStation = station) }
                                            )
                                        }

                                        MainTab.MAP -> {
                                            LaunchedEffect(Unit) {
                                                checkAndAwardAchievements("ach_map_navigator", true)
                                            }
                                            StationsMapScreen(
                                                stations = MockData.sampleStations,
                                                onSelectStationToScanQr = { station ->
                                                    currentScreen = Screen.QrScanner(initialStation = station)
                                                }
                                            )
                                        }

                                        MainTab.REWARDS -> {
                                            RewardsScreen(
                                                currentPoints = userStats.points,
                                                redeemedRewards = redeemedRewards,
                                                onTriggerAchievement = { key -> checkAndAwardAchievements(key, true) },
                                                onRedeemReward = { reward ->
                                                    val isRankingTicket = reward.id.contains("RANK")
                                                    if (isRankingTicket) {
                                                        val pointsAfterRedeem = (userStats.points - reward.costPoints).coerceAtLeast(0)
                                                        val newRankingPoints = userStats.rankingPoints + 100
                                                        val newLevel = calculateUserLevel(newRankingPoints)
                                                        val newTitle = calculateLevelTitle(newLevel)
                                                        userStats = userStats.copy(
                                                            points = pointsAfterRedeem,
                                                            voltPoints = pointsAfterRedeem,
                                                            rankingPoints = newRankingPoints,
                                                            level = newLevel,
                                                            levelTitle = newTitle
                                                        )
                                                        currentAuthUser?.let { u ->
                                                            coroutineScope.launch {
                                                                dbService.updateUserStats(
                                                                    userId = u.userId,
                                                                    pointsDelta = -reward.costPoints,
                                                                    whDelta = 0.0,
                                                                    co2Delta = 0.0,
                                                                    newLevel = newLevel,
                                                                    rankingPointsDelta = 100
                                                                )
                                                            }
                                                        }
                                                        val isEnglish = appSettings.language == AppLanguage.ENGLISH
                                                        SnackbarHelper.show(
                                                            if (isEnglish) "Purchased: +100 ranking points!" else "Kupiono: +100 pkt rankingu!",
                                                            icon = Icons.Default.Leaderboard
                                                        )
                                                    } else {
                                                        val pointsAfterRedeem = (userStats.points - reward.costPoints).coerceAtLeast(0)
                                                        val newCouponId = "${reward.id}_${(100000..999999).random()}"
                                                        val uniqueDiscountCode = "${reward.discountCode}-${(1000..9999).random()}"
                                                        val newRedeemed = RedeemedReward(
                                                            id = newCouponId,
                                                            reward = reward,
                                                            discountCode = uniqueDiscountCode,
                                                            redeemedDate = "Dzisiaj",
                                                            isActivated = false,
                                                            validityMinutes = reward.validityMinutes
                                                        )
                                                        val newRedeemedList = listOf(newRedeemed) + redeemedRewards
                                                        redeemedRewards = newRedeemedList

                                                        val evalResult = evaluateAchievements(
                                                            currentAchievements = achievements,
                                                            workouts = userStats.recentWorkouts,
                                                            totalEnergyWh = userStats.totalEnergyWh,
                                                            co2SavedKg = userStats.co2SavedKg,
                                                            streakDays = userStats.streakDays,
                                                            redeemedRewardsCount = newRedeemedList.size,
                                                            isEnglish = appSettings.language == AppLanguage.ENGLISH,
                                                            isDarkTheme = appSettings.themeMode == AppThemeMode.DARK,
                                                            userId = currentAuthUser?.userId.orEmpty()
                                                        )

                                                        achievements = evalResult.updatedAchievements
                                                        val newRankingPoints = userStats.rankingPoints + evalResult.bonusPointsEarned
                                                        val finalPoints = pointsAfterRedeem
                                                        val finalLevel = calculateUserLevel(newRankingPoints)
                                                        val finalTitle = calculateLevelTitle(finalLevel)

                                                        userStats = userStats.copy(
                                                            rankingPoints = newRankingPoints,
                                                            points = finalPoints,
                                                            voltPoints = finalPoints,
                                                            level = finalLevel,
                                                            levelTitle = finalTitle
                                                        )

                                                        if (evalResult.bonusPointsEarned > 0) {
                                                            evalResult.levelUpMessages.forEach { msg ->
                                                                SnackbarHelper.show(
                                                                    text = msg,
                                                                    icon = Icons.Default.EmojiEvents
                                                                )
                                                            }
                                                        }

                                                        currentAuthUser?.let { u ->
                                                            coroutineScope.launch {
                                                                dbService.saveCoupon(
                                                                    UserCouponDto(
                                                                        couponId = newCouponId,
                                                                        userId = u.userId,
                                                                        rewardId = reward.id,
                                                                        discountCode = uniqueDiscountCode,
                                                                        costPoints = reward.costPoints,
                                                                        validityMinutes = reward.validityMinutes,
                                                                        isActivated = false,
                                                                        activatedAt = null,
                                                                        redeemedAt = currentTimeMillis()
                                                                    )
                                                                )

                                                                dbService.updateUserStats(
                                                                    userId = u.userId,
                                                                    pointsDelta = -reward.costPoints,
                                                                    whDelta = 0.0,
                                                                    co2Delta = 0.0,
                                                                    newLevel = finalLevel,
                                                                    rankingPointsDelta = evalResult.bonusPointsEarned
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                                onActivateReward = { target ->
                                                    val now = currentTimeMillis()
                                                    redeemedRewards = redeemedRewards.map {
                                                        if (it.discountCode == target.discountCode || (it.id.isNotBlank() && it.id == target.id)) {
                                                            it.copy(
                                                                isReadyToScan = true,
                                                                isActivated = true,
                                                                activatedTimestamp = now
                                                            )
                                                        } else it
                                                    }

                                                    currentAuthUser?.let { u ->
                                                        coroutineScope.launch {
                                                             dbService.updateCouponActivation(
                                                                 couponId = target.id.ifBlank { target.discountCode },
                                                                 isActivated = true,
                                                                 activatedAtMs = now
                                                             )
                                                        }
                                                    }
                                                },
                                                onSetCouponReadyToScan = { target ->
                                                    redeemedRewards = redeemedRewards.map {
                                                        if (it.discountCode == target.discountCode || (it.id.isNotBlank() && it.id == target.id)) {
                                                            it.copy(isReadyToScan = true)
                                                        } else it
                                                    }
                                                },
                                                onDeleteCoupon = { target ->
                                                    redeemedRewards = redeemedRewards.filterNot {
                                                        (it.id.isNotBlank() && it.id == target.id) || it.discountCode == target.discountCode
                                                    }

                                                    currentAuthUser?.let { u ->
                                                        coroutineScope.launch {
                                                            dbService.deleteCoupon(
                                                                userId = u.userId,
                                                                couponId = target.id.ifBlank { target.discountCode }
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                        }

                                        MainTab.PROFILE -> {
                                            ProfileScreen(
                                                stats = userStats,
                                                challenges = challenges,
                                                achievements = achievements,
                                                onClaimChallenge = { challenge ->
                                                    val newRankingPoints = userStats.rankingPoints + challenge.rankingPointsReward
                                                    val newVoltPoints = userStats.points + challenge.voltPointsReward
                                                    val newLevel = calculateUserLevel(newRankingPoints)
                                                    val newTitle = calculateLevelTitle(newLevel)

                                                    userStats = userStats.copy(
                                                        points = newVoltPoints,
                                                        voltPoints = newVoltPoints,
                                                        rankingPoints = newRankingPoints,
                                                        level = newLevel,
                                                        levelTitle = newTitle
                                                    )
                                                    challenges = challenges.map {
                                                        if (it.id == challenge.id) it.copy(isClaimed = true, isCompleted = true) else it
                                                    }
                                                    currentAuthUser?.let { u ->
                                                        coroutineScope.launch {
                                                            dbService.claimChallenge(u.userId, challenge.id)
                                                            dbService.updateUserStats(
                                                                userId = u.userId,
                                                                pointsDelta = challenge.voltPointsReward,
                                                                whDelta = 0.0,
                                                                co2Delta = 0.0,
                                                                newLevel = newLevel,
                                                                rankingPointsDelta = challenge.rankingPointsReward
                                                            )
                                                        }
                                                    }
                                                    SnackbarHelper.show(polishText = "+${challenge.rankingPointsReward} pkt rankingu, +${challenge.voltPointsReward} VP!", englishText = "+${challenge.rankingPointsReward} rank pts, +${challenge.voltPointsReward} VP!", icon = Icons.Default.EmojiEvents)
                                                },
                                                onUpdateUserProfile = { newName, newAvatarId ->
                                                    userStats = userStats.copy(
                                                        userName = newName,
                                                        avatarId = newAvatarId
                                                    )
                                                    currentAuthUser?.let { u ->
                                                        coroutineScope.launch {
                                                            authService.updateProfile(newName, newAvatarId)
                                                        }
                                                    }
                                                    SnackbarHelper.show(polishText = "Profil zaktualizowany.", englishText = "Profile updated.")
                                                    checkAndAwardAchievements("ach_profile_custom", true)
                                                },
                                                onOpenSettings = {
                                                    currentScreen = Screen.Settings
                                                },
                                                onOpenAchievements = {
                                                    currentScreen = Screen.Achievements
                                                },
                                                onOpenWorkoutHistory = {
                                                    currentScreen = Screen.WorkoutHistory
                                                },
                                                userEmail = currentAuthUser?.email.orEmpty(),
                                                authService = authService,
                                                onSignOut = {
                                                    coroutineScope.launch {
                                                        VoltUpStorage.setBoolean(VoltUpPreferenceKeys.KEY_REMEMBER_ME, false)
                                                        VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_PASSWORD, "")
                                                        authService.signOut()
                                                        resetAppStateToDefaults()
                                                        currentScreen = Screen.Login
                                                        SnackbarHelper.show(polishText = "Wylogowano z konta.", englishText = "Logged out successfully.")
                                                    }
                                                },
                                                onTriggerAchievement = { key ->
                                                    checkAndAwardAchievements(key, true)
                                                },
                                                onNavigateToManageAccount = {
                                                    currentScreen = Screen.ManageAccount
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            VoltUpSnackbarHost(modifier = Modifier.align(Alignment.TopCenter))

            if (isAutoLoggingIn && !isSplashVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp),
                                strokeWidth = 3.5.dp
                            )
                            Text(
                                text = strings.loggingInWait,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = strings.loggingInSync,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (isSplashVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "VoltUp Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Łódź ",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Volt",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Up",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.cityKineticEnergy,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun VoltUpBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onQrClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = BottomBarWithNotchShape(notchRadius = 46.dp, notchDepth = 36.dp, cornerRadius = 20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BottomNavItem(
                    tab = MainTab.HOME,
                    isSelected = currentTab == MainTab.HOME,
                    onClick = { onTabSelected(MainTab.HOME) }
                )
                BottomNavItem(
                    tab = MainTab.MAP,
                    isSelected = currentTab == MainTab.MAP,
                    onClick = { onTabSelected(MainTab.MAP) }
                )

                Spacer(modifier = Modifier.width(76.dp))

                BottomNavItem(
                    tab = MainTab.REWARDS,
                    isSelected = currentTab == MainTab.REWARDS,
                    onClick = { onTabSelected(MainTab.REWARDS) }
                )
                BottomNavItem(
                    tab = MainTab.PROFILE,
                    isSelected = currentTab == MainTab.PROFILE,
                    onClick = { onTabSelected(MainTab.PROFILE) }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clickable(onClick = onQrClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = strings.scanQr,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val tabLabel = when (tab) {
        MainTab.HOME -> strings.tabHome
        MainTab.MAP -> strings.tabMap
        MainTab.REWARDS -> strings.tabRewards
        MainTab.PROFILE -> strings.tabProfile
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tabLabel,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = (lat2 - lat1) * (kotlin.math.PI / 180.0)
    val dLon = (lon2 - lon1) * (kotlin.math.PI / 180.0)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(lat1 * (kotlin.math.PI / 180.0)) *
            kotlin.math.cos(lat2 * (kotlin.math.PI / 180.0)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return r * c
}

@Composable
fun DashboardScreen(
    stats: UserStats,
    onOpenSettings: () -> Unit,
    onGoToMap: () -> Unit,
    onScanStation: (KineticStation) -> Unit
) {
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val strings = strings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Łódź ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Volt",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Up",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = strings.settingsScreenTitle,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = strings.yourWallet,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${stats.points}",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.voltPoints,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DesignerMetricBox(
                        label = strings.caloriesBurned,
                        value = "${stats.effectiveCalories} kcal",
                        modifier = Modifier.weight(1f)
                    )
                    DesignerMetricBox(
                        label = strings.minutesLabel,
                        value = "${stats.effectiveActiveMinutes} min",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        val currentDayIndex = remember {
            getDayOfWeekIndex()
        }

        val weeklyActivityMinutes = remember(stats.recentWorkouts, stats.totalEnergyWh, currentDayIndex) {
            val arr = DoubleArray(7) { 0.0 }
            val now = currentTimeMillis()
            for (w in stats.recentWorkouts) {
                val t = if (w.timestampMs > 0L) w.timestampMs else now
                val dayIdx = getDayOfWeekIndex(t)
                if (dayIdx in 0..6) {
                    val mins = if (w.durationSeconds > 0) w.durationSeconds / 60.0 else w.generatedWh * 0.8
                    arr[dayIdx] += mins
                }
            }
            if (arr.all { it == 0.0 } && stats.totalEnergyWh > 0.0) {
                arr[currentDayIndex] = (stats.totalEnergyWh * 0.1).coerceIn(10.0, 60.0)
            }
            arr.toList()
        }

        SinusoidActivityChartCard(
            weeklyData = weeklyActivityMinutes,
            currentDayIndex = currentDayIndex,
            primaryColor = primaryColor
        )

        val todayCaloriesKcal = remember(stats.recentWorkouts) {
            val now = currentTimeMillis()
            val startOfDay = now - (now % 86400000L)
            val fromWorkouts = stats.recentWorkouts
                .filter { it.timestampMs >= startOfDay }
                .sumOf { it.caloriesKcal }
            if (fromWorkouts > 0) fromWorkouts else (stats.effectiveCalories / 3).coerceAtLeast(60)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.communityGoalTitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            CommunityGoalCard(
                userCalories = todayCaloriesKcal,
                primaryColor = primaryColor
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = strings.nearbyStationsTitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            var currentUserLocation by remember { mutableStateOf(getUserCurrentLocation()) }
            LaunchedEffect(Unit) {
                val loc = getUserCurrentLocation()
                if (loc != null) {
                    currentUserLocation = loc
                }
            }

            var dbDeviceList by remember { mutableStateOf(MockData.sampleStations) }
            val dbService = remember { getDatabaseService() }
            LaunchedEffect(Unit) {
                try {
                    val fromDb = dbService.getDevices()
                    if (fromDb.isNotEmpty()) {
                        dbDeviceList = fromDb
                    }
                } catch (_: Exception) {}
            }

            val nearbyStations = remember(currentUserLocation, dbDeviceList) {
                val realLoc = currentUserLocation
                if (realLoc != null) {
                    val (uLat, uLng) = realLoc
                    dbDeviceList.map { station ->
                        val distMeters = calculateDistanceMeters(uLat, uLng, station.latitude, station.longitude)
                        val distStr = if (distMeters < 1000.0) "${distMeters.toInt()} m" else "${((distMeters / 100.0).roundToInt() / 10.0)} km"
                        Triple(station, distMeters, distStr)
                    }.sortedBy { it.second }.take(3)
                } else {
                    dbDeviceList.take(3).map { station ->
                        Triple(station, 500.0, station.distance)
                    }
                }
            }

            var expandedNearbyStationId by remember { mutableStateOf<String?>(null) }

            nearbyStations.forEach { (station, _, distStr) ->
                val isExpanded = expandedNearbyStationId == station.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            expandedNearbyStationId = if (isExpanded) null else station.id
                        },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = station.type.icon,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = station.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = strings.distanceFromYou(distStr),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = primaryColor.copy(alpha = 0.12f),
                                modifier = Modifier.clickable { onScanStation(station) }
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = strings.scanQr,
                                    tint = primaryColor,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(20.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = station.location,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${strings.districtLabel}: ${station.district}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${strings.maxPowerLabel}: ${station.maxPowerW} W",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (station.isOnline) strings.statusActiveOnline else strings.statusInactive,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (station.isOnline) primaryColor else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DesignerMetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CommunityGoalCard(
    userCalories: Int,
    primaryColor: Color
) {
    val strings = strings
    var isExpanded by remember { mutableStateOf(false) }

    var secondsRemaining by remember {
        val now = currentTimeMillis()
        val elapsedInDay = (now % 86400000L) / 1000L
        mutableStateOf((86400L - elapsedInDay).coerceIn(0L, 86400L))
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            if (secondsRemaining > 0) {
                secondsRemaining--
            } else {
                secondsRemaining = 86400L
            }
        }
    }

    val hours = secondsRemaining / 3600
    val minutes = (secondsRemaining % 3600) / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    val targetKcal = 15000
    val baseCommunityKcal = 8450
    val totalCommunityKcal = (baseCommunityKcal + userCalories).coerceAtMost(targetKcal * 2)
    val progressFraction = (totalCommunityKcal.toFloat() / targetKcal.toFloat()).coerceIn(0f, 1f)
    val percentage = (progressFraction * 100).toInt()
    val isGoalCompleted = totalCommunityKcal >= targetKcal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Park na Zdrowiu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = strings.communityGoalSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$percentage%",
                        color = primaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (strings is VoltUpEnglishStrings) "Community: $totalCommunityKcal / $targetKcal kcal" else "Mieszkańcy Łodzi: $totalCommunityKcal / $targetKcal kcal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (strings is VoltUpEnglishStrings) "Your share: $userCalories kcal" else "Twój wkład: $userCalories kcal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(primaryColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (secondsRemaining < 3600) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (secondsRemaining > 0) strings.stageEndsIn(timeFormatted) else strings.renewalIn4Hours,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isGoalCompleted) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isGoalCompleted) strings.goalCompleted else strings.goalInProgress,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalCompleted) Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (isExpanded) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.communityGoalDescription,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.energyCollectionState,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalCommunityKcal / $targetKcal kcal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.participantReward,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = strings.rankingRewardPoints,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.yourPersonalContribution,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$userCalories kcal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SinusoidActivityChartCard(
    weeklyData: List<Double>,
    currentDayIndex: Int,
    primaryColor: Color
) {
    val strings = strings
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, surfaceVariantColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.activityTitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                val todayVal = weeklyData.getOrElse(currentDayIndex) { 0.0 }
                val prevDaysAvg = if (currentDayIndex > 0) {
                    val sub = weeklyData.take(currentDayIndex)
                    if (sub.any { it > 0 }) sub.filter { it > 0 }.average() else 0.0
                } else 0.0

                val (percentText, percentColor) = when {
                    todayVal > 0 && prevDaysAvg > 0 -> {
                        val pct = ((todayVal - prevDaysAvg) / prevDaysAvg * 100).toInt()
                        if (pct >= 0) "+$pct%" to primaryColor else "$pct%" to MaterialTheme.colorScheme.error
                    }
                    todayVal > 0 -> "+100%" to primaryColor
                    weeklyData.sum() > 0 -> "+${(weeklyData.sum() / 10).toInt().coerceIn(1, 99)}%" to primaryColor
                    else -> "0%" to primaryColor
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = percentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = percentText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = percentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxRaw = (weeklyData.maxOrNull() ?: 0.0).toFloat()
                        val maxVal = if (maxRaw > 0f) maxRaw * 1.15f else 10f

                        val segmentWidth = size.width / weeklyData.size
                        val barWidth = 14.dp.toPx()

                        if (currentDayIndex in 0 until weeklyData.size) {
                            val todayCenterX = (currentDayIndex * segmentWidth) + (segmentWidth / 2f)
                            drawRoundRect(
                                color = primaryColor.copy(alpha = 0.08f),
                                topLeft = Offset(todayCenterX - (segmentWidth * 0.44f), 0f),
                                size = Size(segmentWidth * 0.88f, size.height),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                            )
                        }

                        weeklyData.forEachIndexed { idx, v ->
                            val isToday = idx == currentDayIndex
                            val barHeight = if (v > 0) ((v.toFloat() / maxVal) * (size.height - 24f)).coerceAtLeast(6f) else 0f

                            val centerX = (idx * segmentWidth) + (segmentWidth / 2f)
                            val topLeftX = centerX - (barWidth / 2f)
                            val topLeftY = size.height - barHeight

                            val barColor = if (isToday) primaryColor.copy(alpha = 0.55f) else surfaceVariantColor.copy(alpha = 0.4f)

                            if (barHeight > 0f) {
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }

                        val points = weeklyData.mapIndexed { idx, v ->
                            val x = (idx * segmentWidth) + (segmentWidth / 2f)
                            val barHeight = if (v > 0) ((v.toFloat() / maxVal) * (size.height - 24f)).coerceAtLeast(6f) else 0f
                            val y = size.height - barHeight - 6f
                            Offset(x, y)
                        }

                        if (points.size >= 2) {
                            val path = Path()
                            val fillPath = Path()

                            path.moveTo(points.first().x, points.first().y)
                            fillPath.moveTo(points.first().x, size.height)
                            fillPath.lineTo(points.first().x, points.first().y)

                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                                val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                                path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                                fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                            }

                            fillPath.lineTo(points.last().x, size.height)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                                    startY = 0f,
                                    endY = size.height
                                )
                            )

                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            val todayPoint = points.getOrNull(currentDayIndex) ?: points.last()
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.28f),
                                radius = 11.dp.toPx(),
                                center = todayPoint
                            )
                            drawCircle(
                                color = surfaceColor,
                                radius = 6.dp.toPx(),
                                center = todayPoint
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = todayPoint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val days = strings.daysOfWeekShort
                    days.forEachIndexed { idx, d ->
                        val isToday = idx == currentDayIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isToday) primaryColor.copy(alpha = 0.16f) else Color.Transparent)
                                .then(if (isToday) Modifier.border(1.dp, primaryColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp)) else Modifier)
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = d,
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Int.formatSeconds(): String {
    val mins = this / 60
    val secs = this % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

fun Double.formatOneDecimal(): String {
    val rounded = (this * 10).roundToInt() / 10.0
    return rounded.toString()
}

@Preview
@Composable
fun AppPreview() {
    App()
}
