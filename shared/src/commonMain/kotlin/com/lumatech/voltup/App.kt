package com.lumatech.voltup

import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    data object Main : Screen
    data class ActiveWorkout(val station: KineticStation) : Screen
    data object Settings : Screen
    data object QrScanner : Screen
    data object Achievements : Screen
    data object ManageAccount : Screen
    data object HelpGuide : Screen
    data object Authors : Screen
}

class BottomBarWithNotchShape(
    private val notchRadius: Dp = 40.dp,
    private val notchDepth: Dp = 32.dp,
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
fun App() {
    val authService = remember { getAuthService() }
    val dbService = remember { getDatabaseService() }
    val currentAuthUser by authService.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var appSettings by remember { mutableStateOf(AppSettings()) }
    var isSplashVisible by remember { mutableStateOf(true) }
    var isAutoLoggingIn by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<Screen>(if (currentAuthUser != null) Screen.Main else Screen.Login) }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var userStats by remember {
        mutableStateOf(
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
        )
    }
    var achievements by remember { mutableStateOf(DefaultAchievements.initialAchievements) }
    var challenges by remember { mutableStateOf(MockData.sampleChallenges) }
    var redeemedRewards by remember { mutableStateOf(emptyList<RedeemedReward>()) }

    LaunchedEffect(Unit) {
        delay(1200.milliseconds)
        isSplashVisible = false
    }

    LaunchedEffect(currentAuthUser) {
        val user = currentAuthUser
        if (user == null) {
            isAutoLoggingIn = false
            if (currentScreen != Screen.Login && currentScreen != Screen.Register && currentScreen != Screen.ForgotPassword) {
                currentScreen = Screen.Login
            }
        } else {
            isAutoLoggingIn = true
            delay(600.milliseconds)
            isAutoLoggingIn = false
            if (currentScreen == Screen.Login || currentScreen == Screen.Register || currentScreen == Screen.ForgotPassword) {
                currentScreen = Screen.Main
            }
        }
    }

    LaunchedEffect(currentAuthUser?.userId) {
        val u = currentAuthUser
        if (u != null) {
            val freshUser = dbService.getUser(u.userId) ?: u
            val workouts = dbService.getWorkoutSessions(u.userId)
            val loadedSettings = dbService.getUserSettings(u.userId)
            val loadedAchievements = dbService.getUserAchievements(u.userId)
            val loadedChallenges = dbService.getUserChallenges(u.userId)
            val loadedCoupons = dbService.getUserCoupons(u.userId)

            userStats = userStats.copy(
                points = freshUser.voltPoints,
                totalEnergyWh = freshUser.totalEnergyWh,
                co2SavedKg = freshUser.co2SavedKg,
                userName = freshUser.userName.ifBlank { "Użytkownik VoltUp" },
                avatarId = freshUser.avatarId,
                level = freshUser.userLevel,
                levelTitle = calculateLevelTitle(freshUser.userLevel),
                streakDays = freshUser.streakDays,
                recentWorkouts = workouts.map { w ->
                    WorkoutSessionRecord(
                        id = w.sessionId,
                        stationName = w.stationName,
                        stationLocation = w.stationLocation,
                        dateText = "Ostatnio",
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
                appSettings = appSettings.copy(
                    themeMode = themeModeEnum,
                    notificationsEnabled = loadedSettings.notificationsEnabled,
                    soundEffectsEnabled = loadedSettings.soundEffectsEnabled,
                    hapticFeedbackEnabled = loadedSettings.hapticFeedbackEnabled
                )
            }

            if (loadedAchievements.isNotEmpty()) {
                achievements = achievements.map { ach ->
                    val match = loadedAchievements.firstOrNull { it.achievementId == ach.id }
                    if (match != null) {
                        ach.copy(
                            level = match.currentLevel,
                            currentProgress = match.currentProgress
                        )
                    } else ach
                }
            }

            if (loadedChallenges.isNotEmpty()) {
                challenges = challenges.map { ch ->
                    val match = loadedChallenges.firstOrNull { it.challengeId == ch.id }
                    if (match != null) {
                        val currVal = if (match.currentValue > 0) match.currentValue else (match.currentProgress * ch.targetValue).toDouble()
                        ch.copy(
                            currentValue = currVal,
                            currentProgress = match.currentProgress,
                            isCompleted = match.isCompleted,
                            isClaimed = match.isClaimed
                        )
                    } else ch
                }
            }

            if (loadedCoupons.isNotEmpty()) {
                redeemedRewards = loadedCoupons.filter { !it.rewardId.contains("RANK") }.map { c ->
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
                        isActivated = c.isActivated,
                        activatedTimestamp = c.activatedAt ?: 0L,
                        validityMinutes = c.validityMinutes
                    )
                }
            }
        }
    }

    val canGoBack = when (currentScreen) {
        Screen.Login -> false
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
            is Screen.Authors -> currentScreen = Screen.Settings
            is Screen.QrScanner -> currentScreen = Screen.Main
            is Screen.Achievements -> currentScreen = Screen.Main
            is Screen.ActiveWorkout -> { }
            is Screen.Main -> if (currentTab != MainTab.HOME) currentTab = MainTab.HOME
            is Screen.Login -> { }
        }
    }

    VoltUpTheme(themeMode = appSettings.themeMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) +
                        slideInVertically(animationSpec = androidx.compose.animation.core.tween(250)) { it / 10 } togetherWith
                        fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
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
                                    SnackbarHelper.show("Witaj ponownie w VoltUp, ${user.userName}!")
                                }
                            )
                        }

                        is Screen.Register -> {
                            RegisterScreen(
                                authService = authService,
                                onNavigateToLogin = { currentScreen = Screen.Login },
                                onRegisterSuccess = { user ->
                                    currentScreen = Screen.Main
                                    SnackbarHelper.show("Konto utworzone! Witaj w VoltUp, ${user.userName}!")
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
                                    appSettings = AppSettings()
                                    SnackbarHelper.show("Ustawienia przywrócone.")
                                },
                                authService = authService,
                                userEmail = currentAuthUser?.email.orEmpty(),
                                onSignOut = {
                                    coroutineScope.launch {
                                        authService.signOut()
                                        currentScreen = Screen.Login
                                        SnackbarHelper.show("Wylogowano z konta.")
                                    }
                                },
                                onNavigateToManageAccount = {
                                    currentScreen = Screen.ManageAccount
                                },
                                onNavigateToHelpGuide = {
                                    currentScreen = Screen.HelpGuide
                                },
                                onNavigateToAuthors = {
                                    currentScreen = Screen.Authors
                                }
                            )
                        }

                        is Screen.HelpGuide -> {
                            HelpGuideScreen(
                                onNavigateBack = { currentScreen = Screen.Settings }
                            )
                        }

                        is Screen.Authors -> {
                            AuthorsScreen(
                                onNavigateBack = { currentScreen = Screen.Settings }
                            )
                        }

                        is Screen.ManageAccount -> {
                            ManageAccountScreen(
                                userStats = userStats,
                                userEmail = currentAuthUser?.email.orEmpty(),
                                authService = authService,
                                onNavigateBack = { currentScreen = Screen.Settings },
                                onSignOut = {
                                    coroutineScope.launch {
                                        authService.signOut()
                                        currentScreen = Screen.Login
                                        SnackbarHelper.show("Wylogowano z konta.")
                                    }
                                },
                                onAccountDeleted = {
                                    currentScreen = Screen.Login
                                    SnackbarHelper.show("Twoje konto zostało usunięte.")
                                }
                            )
                        }

                        is Screen.QrScanner -> {
                            QrScannerScreen(
                                onStationDetected = { station ->
                                    currentScreen = Screen.ActiveWorkout(station)
                                },
                                onDismiss = { currentScreen = Screen.Main }
                            )
                        }

                        is Screen.Achievements -> {
                            AchievementsScreen(
                                badges = achievements.map { it.toBadgeModel() },
                                userStats = userStats,
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
                                        redeemedRewardsCount = redeemedRewards.size
                                    )

                                    achievements = evalResult.updatedAchievements
                                    val totalPoints = userStats.points + earnedPoints + evalResult.bonusPointsEarned
                                    val newLevel = calculateUserLevel(totalPoints)
                                    val newTitle = calculateLevelTitle(newLevel)

                                    userStats = userStats.copy(
                                        points = totalPoints,
                                        totalEnergyWh = newTotalWh,
                                        co2SavedKg = newCo2,
                                        level = newLevel,
                                        levelTitle = newTitle,
                                        recentWorkouts = newWorkouts
                                    )

                                    val sessionPeakWatts = workoutRecord.generatedWh * 3600.0 / workoutRecord.durationSeconds.coerceAtLeast(1)
                                    challenges = challenges.map { ch ->
                                        if (!ch.isCompleted) {
                                            val newCurrVal = when (ch.id) {
                                                "C1" -> ch.currentValue + earnedWh
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
                                            // Tabela 2: workout_sessions
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

                                            // Tabela 1: users (aktualizacja punktów, energii, CO2, poziomu)
                                            dbService.updateUserStats(
                                                userId = u.userId,
                                                pointsDelta = earnedPoints + evalResult.bonusPointsEarned,
                                                whDelta = earnedWh,
                                                co2Delta = earnedWh * 0.0007,
                                                newLevel = newLevel
                                            )

                                            // Tabela 3: user_achievements
                                            achievements.forEach { ach ->
                                                dbService.saveAchievement(
                                                    UserAchievementDto(
                                                        userId = u.userId,
                                                        achievementId = ach.id,
                                                        currentLevel = ach.level,
                                                        currentProgress = ach.currentProgress.toDouble(),
                                                        updatedAt = 0L
                                                    )
                                                )
                                            }

                                            // Tabela 4: user_challenges
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
                                    SnackbarHelper.show("Sesja zakończona!", icon = Icons.Default.Check)
                                    if (evalResult.bonusPointsEarned > 0) {
                                        val achMsg = evalResult.levelUpMessages.firstOrNull() ?: "Zdobyto nowe osiągnięcie!"
                                        SnackbarHelper.show(
                                            text = achMsg,
                                            icon = Icons.Default.EmojiEvents
                                        )
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
                                        onQrClick = { currentScreen = Screen.QrScanner }
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
                                            DashboardScreen(
                                                stats = userStats,
                                                onOpenSettings = { currentScreen = Screen.Settings },
                                                onGoToMap = { currentTab = MainTab.MAP },
                                                onScanStation = { currentScreen = Screen.QrScanner }
                                            )
                                        }

                                        MainTab.MAP -> {
                                            StationsMapScreen(
                                                stations = MockData.sampleStations,
                                                onSelectStationToScanQr = {
                                                    currentScreen = Screen.QrScanner
                                                }
                                            )
                                        }

                                        MainTab.REWARDS -> {
                                            RewardsScreen(
                                                currentPoints = userStats.points,
                                                redeemedRewards = redeemedRewards,
                                                onRedeemReward = { reward ->
                                                    val isRankingTicket = reward.id.contains("RANK")
                                                    if (isRankingTicket) {
                                                        val pointsAfterRedeem = (userStats.points - reward.costPoints).coerceAtLeast(0)
                                                        userStats = userStats.copy(
                                                            points = pointsAfterRedeem
                                                        )
                                                        currentAuthUser?.let { u ->
                                                            coroutineScope.launch {
                                                                dbService.updateUserStats(
                                                                    userId = u.userId,
                                                                    pointsDelta = 100 - reward.costPoints,
                                                                    whDelta = 0.0,
                                                                    co2Delta = 0.0,
                                                                    newLevel = userStats.level
                                                                )
                                                            }
                                                        }
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
                                                            redeemedRewardsCount = newRedeemedList.size
                                                        )

                                                        achievements = evalResult.updatedAchievements
                                                        val finalPoints = pointsAfterRedeem + evalResult.bonusPointsEarned
                                                        val finalLevel = calculateUserLevel(finalPoints)
                                                        val finalTitle = calculateLevelTitle(finalLevel)

                                                        userStats = userStats.copy(
                                                            points = finalPoints,
                                                            level = finalLevel,
                                                            levelTitle = finalTitle
                                                        )

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
                                                                    pointsDelta = -reward.costPoints + evalResult.bonusPointsEarned,
                                                                    whDelta = 0.0,
                                                                    co2Delta = 0.0,
                                                                    newLevel = finalLevel
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                                onActivateReward = { target ->
                                                    redeemedRewards = redeemedRewards.map {
                                                        if (it.discountCode == target.discountCode) {
                                                            it.copy(
                                                                isActivated = true,
                                                                activatedTimestamp = 1L
                                                            )
                                                        } else it
                                                    }

                                                    currentAuthUser?.let { u ->
                                                        coroutineScope.launch {
                                                            dbService.updateCouponActivation(
                                                                couponId = target.discountCode,
                                                                isActivated = true,
                                                                activatedAtMs = 1L
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
                                                    val newRankingPoints = userStats.points + challenge.rankingPointsReward
                                                    val newVoltPoints = userStats.voltPoints + challenge.voltPointsReward
                                                    val newLevel = calculateUserLevel(newRankingPoints)
                                                    val newTitle = calculateLevelTitle(newLevel)

                                                    userStats = userStats.copy(
                                                        points = newRankingPoints,
                                                        voltPoints = newVoltPoints,
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
                                                                pointsDelta = challenge.rankingPointsReward,
                                                                whDelta = 0.0,
                                                                co2Delta = 0.0,
                                                                newLevel = newLevel
                                                            )
                                                        }
                                                    }
                                                    SnackbarHelper.show("+${challenge.rankingPointsReward} pkt rankingu, +${challenge.voltPointsReward} VP!", icon = Icons.Default.EmojiEvents)
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
                                                    SnackbarHelper.show("Profil zaktualizowany.")
                                                },
                                                onOpenSettings = {
                                                    currentScreen = Screen.Settings
                                                },
                                                onOpenAchievements = {
                                                    currentScreen = Screen.Achievements
                                                },
                                                userEmail = currentAuthUser?.email.orEmpty(),
                                                authService = authService,
                                                onSignOut = {
                                                    coroutineScope.launch {
                                                        authService.signOut()
                                                        currentScreen = Screen.Login
                                                        SnackbarHelper.show("Wylogowano z konta.")
                                                    }
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
                AlertDialog(
                    onDismissRequest = {},
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Text(
                            text = "Logowanie do konta...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = LightPrimary,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Witaj ponownie! Trwa wczytywanie Twojego profilu i postępów w VoltUp.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = null
                )
            }

            if (isSplashVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0E1116)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = LightPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(2.dp, LightPrimary.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "VoltUp Logo",
                                    tint = LightPrimary,
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
                                    color = Color.White
                                )
                                Text(
                                    text = "VoltUp",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Light,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kinetyczna Energia Miasta",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.65f),
                                letterSpacing = 1.sp
                            )
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
                .height(70.dp),
            shape = BottomBarWithNotchShape(notchRadius = 40.dp, notchDepth = 32.dp, cornerRadius = 20.dp),
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

                Spacer(modifier = Modifier.width(64.dp))

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
                .offset(y = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(54.dp)
                    .clickable(onClick = onQrClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Skanuj QR",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
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

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
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
    onScanStation: () -> Unit
) {
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary

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
                    text = "VoltUp",
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
                    contentDescription = "Ustawienia",
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
                            text = "TWÓJ PORTFEL",
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
                                text = "VoltPoints",
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
                        label = "Wygenerowano",
                        value = formatEnergySmart(stats.totalEnergyWh),
                        modifier = Modifier.weight(1f)
                    )
                    DesignerMetricBox(
                        label = "CO2 redukcja",
                        value = formatCo2Smart(stats.co2SavedKg),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        val currentDayIndex = remember {
            getDayOfWeekIndex()
        }

        val weeklyActivityWh = remember(stats.recentWorkouts, stats.totalEnergyWh, currentDayIndex) {
            val arr = DoubleArray(7) { 0.0 }
            val now = currentTimeMillis()
            for (w in stats.recentWorkouts) {
                val t = if (w.timestampMs > 0L) w.timestampMs else now
                val dayIdx = getDayOfWeekIndex(t)
                if (dayIdx in 0..6) {
                    arr[dayIdx] += w.generatedWh
                }
            }
            if (arr.all { it == 0.0 } && stats.totalEnergyWh > 0.0) {
                arr[currentDayIndex] = stats.totalEnergyWh.coerceAtMost(500.0)
            }
            arr.toList()
        }

        SinusoidActivityChartCard(
            weeklyData = weeklyActivityWh,
            currentDayIndex = currentDayIndex,
            primaryColor = primaryColor
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "CEL SPOŁECZNOŚCI",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            CommunityGoalCard(
                userWh = stats.totalEnergyWh,
                primaryColor = primaryColor
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "W POBLIŻU",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            val userLat = 51.7592
            val userLng = 19.4560
            val nearbyStations = remember {
                MockData.sampleStations.map { station ->
                    val distMeters = calculateDistanceMeters(userLat, userLng, station.latitude, station.longitude)
                    val distStr = if (distMeters < 1000.0) "${distMeters.toInt()} m" else "${((distMeters / 100.0).roundToInt() / 10.0)} km"
                    Triple(station, distMeters, distStr)
                }.sortedBy { it.second }.take(3)
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
                                        text = "$distStr od Ciebie",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = primaryColor.copy(alpha = 0.12f),
                                modifier = Modifier.clickable { onScanStation() }
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "Skanuj QR",
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
                                        text = "Dzielnica: ${station.district}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Moc max: ${station.maxPowerW} W",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (station.isOnline) "Status: Aktywna (Online)" else "Status: Nieaktywna",
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
    userWh: Double,
    primaryColor: Color
) {
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

    val targetWh = 1000.0
    val baseCommunityWh = 0.0
    val totalCommunityWh = (baseCommunityWh + userWh).coerceAtMost(targetWh * 1.5)
    val progressFraction = (totalCommunityWh / targetWh).toFloat().coerceIn(0f, 1f)
    val percentage = (progressFraction * 100).toInt()
    val isGoalCompleted = totalCommunityWh >= targetWh

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
                        text = "Wspólne zasilanie oświetlenia alejek",
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
                        text = if (secondsRemaining > 0) "Koniec etapu za: $timeFormatted" else "Odnowienie za 4 godz.",
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
                        text = if (isGoalCompleted) "Zaliczone ✓" else "W trakcie",
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
                            text = "Każdy wat energii wygenerowany na miejskich rowerach, stepperach i orbitrekach w Łodzi zasila wspólny akumulator parkowy. Gdy cel zostanie osiągnięty w ciągu 24h, wszyscy uczestnicy otrzymują +100 punktów do rankingu!",
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
                                text = "Stan zbiórki energii:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${totalCommunityWh.toInt()} / ${targetWh.toInt()} Wh",
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
                                text = "Nagroda dla uczestników:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+100 pkt do rankingu",
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
                                text = "Twój osobisty wkład:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatEnergySmart(userWh),
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
                    text = "AKTYWNOŚĆ",
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
                        val maxVal = (weeklyData.maxOrNull() ?: 100.0).coerceAtLeast(80.0).toFloat()

                        val segmentWidth = size.width / weeklyData.size
                        val barWidth = 14.dp.toPx()

                        weeklyData.forEachIndexed { idx, v ->
                            val isToday = idx == currentDayIndex
                            val barHeight = (v.toFloat() / maxVal * (size.height - 20f))

                            val centerX = (idx * segmentWidth) + (segmentWidth / 2f)
                            val topLeftX = centerX - (barWidth / 2f)
                            val topLeftY = size.height - barHeight

                            val barColor = if (isToday) primaryColor.copy(alpha = 0.3f) else surfaceVariantColor.copy(alpha = 0.4f)

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(topLeftX, topLeftY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }

                        val points = weeklyData.mapIndexed { idx, v ->
                            val x = (idx * segmentWidth) + (segmentWidth / 2f)
                            val y = size.height - (v.toFloat() / maxVal * (size.height - 20f)) - 10f
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

                            val todayPoint = points[currentDayIndex]
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

                Row(modifier = Modifier.fillMaxWidth()) {
                    val days = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd")
                    days.forEachIndexed { idx, d ->
                        val isToday = idx == currentDayIndex
                        Text(
                            text = d,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
