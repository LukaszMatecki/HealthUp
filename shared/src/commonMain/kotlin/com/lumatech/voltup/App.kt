package com.lumatech.voltup

import androidx.compose.animation.AnimatedContent
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
    data object Main : Screen
    data class ActiveWorkout(val station: KineticStation) : Screen
    data object Settings : Screen
    data object QrScanner : Screen
    data object Achievements : Screen
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
    var appSettings by remember { mutableStateOf(AppSettings()) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var userStats by remember {
        mutableStateOf(
            UserStats(
                points = 0,
                totalEnergyWh = 0.0,
                weeklyEnergyWh = 0.0,
                co2SavedKg = 0.0,
                userName = "Jan Kowalski",
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

    val canGoBack = currentScreen != Screen.Main || currentTab != MainTab.HOME
    CommonBackHandler(enabled = canGoBack) {
        when (currentScreen) {
            is Screen.Settings -> currentScreen = Screen.Main
            is Screen.QrScanner -> currentScreen = Screen.Main
            is Screen.Achievements -> currentScreen = Screen.Main
            is Screen.ActiveWorkout -> { }
            is Screen.Main -> if (currentTab != MainTab.HOME) currentTab = MainTab.HOME
        }
    }

    VoltUpTheme(themeMode = appSettings.themeMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AnimatedContent(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        is Screen.Settings -> {
                            SettingsScreen(
                                settings = appSettings,
                                onSettingsChanged = { newSettings ->
                                    appSettings = newSettings
                                },
                                onNavigateBack = { currentScreen = Screen.Main },
                                onClearHistory = {
                                    userStats = userStats.copy(recentWorkouts = emptyList())
                                },
                                onResetDefaults = {
                                    appSettings = AppSettings()
                                    SnackbarHelper.show("Ustawienia przywrócone.")
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

                                    challenges = challenges.map { ch ->
                                        if (!ch.isCompleted) {
                                            val newProg = (ch.currentProgress + (earnedWh / 200.0).toFloat()).coerceAtMost(1f)
                                            ch.copy(currentProgress = newProg, isCompleted = newProg >= 1f)
                                        } else ch
                                    }

                                    currentScreen = Screen.Main
                                    var snackMsg = "Sesja zapisana. +$earnedPoints pkt"
                                    if (evalResult.bonusPointsEarned > 0) {
                                        snackMsg += " | 🏆 Awans w osiągnięciach (+${evalResult.bonusPointsEarned} pkt)!"
                                    }
                                    SnackbarHelper.show(snackMsg)
                                },
                                onCancel = {
                                    currentScreen = Screen.Main
                                }
                            )
                        }

                        is Screen.Main -> {
                            val pagerState = rememberPagerState(initialPage = currentTab.ordinal) { MainTab.entries.size }

                            LaunchedEffect(currentTab) {
                                if (pagerState.currentPage != currentTab.ordinal) {
                                    pagerState.animateScrollToPage(currentTab.ordinal)
                                }
                            }

                            LaunchedEffect(pagerState.currentPage) {
                                val selectedTab = MainTab.entries[pagerState.currentPage]
                                if (currentTab != selectedTab) {
                                    currentTab = selectedTab
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
                                    userScrollEnabled = currentTab != MainTab.MAP,
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
                                                    val pointsAfterRedeem = userStats.points - reward.costPoints
                                                    val newRedeemedList = listOf(
                                                        RedeemedReward(
                                                            reward = reward,
                                                            discountCode = reward.discountCode,
                                                            redeemedDate = "Dzisiaj",
                                                            isActivated = false,
                                                            validityMinutes = reward.validityMinutes
                                                        )
                                                    ) + redeemedRewards

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
                                                }
                                            )
                                        }

                                        MainTab.PROFILE -> {
                                            ProfileScreen(
                                                stats = userStats,
                                                challenges = challenges,
                                                achievements = achievements,
                                                onClaimChallenge = { challenge ->
                                                    val newPoints = userStats.points + challenge.rewardPoints
                                                    val newLevel = calculateUserLevel(newPoints)
                                                    val newTitle = calculateLevelTitle(newLevel)

                                                    userStats = userStats.copy(
                                                        points = newPoints,
                                                        level = newLevel,
                                                        levelTitle = newTitle
                                                    )
                                                    challenges = challenges.map {
                                                        if (it.id == challenge.id) it.copy(isClaimed = true) else it
                                                    }
                                                    SnackbarHelper.show("+${challenge.rewardPoints} pkt za wyzwanie")
                                                },
                                                onUpdateUserProfile = { newName, newAvatarId ->
                                                    userStats = userStats.copy(
                                                        userName = newName,
                                                        avatarId = newAvatarId
                                                    )
                                                    SnackbarHelper.show("Profil zaktualizowany.")
                                                },
                                                onOpenSettings = {
                                                    currentScreen = Screen.Settings
                                                },
                                                onOpenAchievements = {
                                                    currentScreen = Screen.Achievements
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
                        value = "${(stats.totalEnergyWh / 1000.0).formatOneDecimal()} kWh",
                        modifier = Modifier.weight(1f)
                    )
                    DesignerMetricBox(
                        label = "CO2 redukcja",
                        value = "${stats.co2SavedKg.formatOneDecimal()} kg",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        SinusoidActivityChartCard(
            weeklyData = listOf(80.0, 110.0, 95.0, 140.0, 180.0, 120.0, 115.0),
            primaryColor = primaryColor
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CEL SPOŁECZNOŚCIOWY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Park na Zdrowiu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "78%",
                        color = primaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                }
            }
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

            MockData.sampleStations.take(2).forEach { station ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onScanStation),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
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
                                    text = station.distance,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.1f)
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
fun SinusoidActivityChartCard(
    weeklyData: List<Double>,
    primaryColor: Color
) {
    val currentDayIndex = 4
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

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+5%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = primaryColor,
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
