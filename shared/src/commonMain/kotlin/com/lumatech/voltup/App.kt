package com.lumatech.voltup

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
            HOME -> Icons.Default.Bolt
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
}

@Composable
fun App() {
    var appSettings by remember { mutableStateOf(AppSettings()) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var userStats by remember {
        mutableStateOf(
            UserStats(
                recentWorkouts = MockData.sampleRecentWorkouts
            )
        )
    }
    var challenges by remember { mutableStateOf(MockData.sampleChallenges) }
    var badges by remember { mutableStateOf(MockData.sampleBadges) }
    var redeemedRewards by remember { mutableStateOf(emptyList<RedeemedReward>()) }

    val canGoBack = currentScreen != Screen.Main || currentTab != MainTab.HOME
    CommonBackHandler(enabled = canGoBack) {
        when (currentScreen) {
            is Screen.Settings -> currentScreen = Screen.Main
            is Screen.QrScanner -> currentScreen = Screen.Main
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
                AnimatedContent(targetState = currentScreen) { screen ->
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

                        is Screen.ActiveWorkout -> {
                            WorkoutScreen(
                                station = screen.station,
                                targetWh = 100,
                                soundEnabled = appSettings.soundEffectsEnabled,
                                onFinishWorkout = { earnedWh, earnedPoints, workoutRecord ->
                                    userStats = userStats.copy(
                                        points = userStats.points + earnedPoints,
                                        totalEnergyWh = userStats.totalEnergyWh + earnedWh,
                                        co2SavedKg = userStats.co2SavedKg + (earnedWh * 0.0007),
                                        recentWorkouts = listOf(workoutRecord) + userStats.recentWorkouts
                                    )
                                    challenges = challenges.map { ch ->
                                        if (!ch.isCompleted) {
                                            val newProg = (ch.currentProgress + (earnedWh / 200.0).toFloat()).coerceAtMost(1f)
                                            ch.copy(currentProgress = newProg, isCompleted = newProg >= 1f)
                                        } else ch
                                    }
                                    currentScreen = Screen.Main
                                    SnackbarHelper.show("Sesja zapisana. +$earnedPoints pkt")
                                },
                                onCancel = {
                                    currentScreen = Screen.Main
                                }
                            )
                        }

                        is Screen.Main -> {
                            Scaffold(
                                bottomBar = {
                                    VoltUpBottomBar(
                                        currentTab = currentTab,
                                        onTabSelected = { currentTab = it },
                                        onQrClick = { currentScreen = Screen.QrScanner }
                                    )
                                }
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                    when (currentTab) {
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
                                                    userStats = userStats.copy(
                                                        points = userStats.points - reward.costPoints
                                                    )
                                                    val newRedeemed = RedeemedReward(
                                                        reward = reward,
                                                        discountCode = reward.discountCode,
                                                        redeemedDate = "Dzisiaj",
                                                        isActivated = false,
                                                        validityMinutes = reward.validityMinutes
                                                    )
                                                    redeemedRewards = listOf(newRedeemed) + redeemedRewards
                                                },
                                                onActivateReward = { target ->
                                                    redeemedRewards = redeemedRewards.map {
                                                        if (it.discountCode == target.discountCode) {
                                                            it.copy(
                                                                isActivated = true,
                                                                activatedTimestamp = System.currentTimeMillis()
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
                                                badges = badges,
                                                onClaimChallenge = { challenge ->
                                                    userStats = userStats.copy(
                                                        points = userStats.points + challenge.rewardPoints
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Column {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceEvenly
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

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onQrClick)
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Skanuj",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Skanuj",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

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
    }
}

@Composable
fun BottomNavItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tab.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Minimalist Header: Just VOLTUP and settings icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VOLTUP",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(38.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ustawienia",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Portfel Punktów VoltPoints (Designerski, minimalistyczny układ)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "PORTFEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${stats.points}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VoltPoints",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EnergyYellow.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Miejsce #${stats.cityContributionRank}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = EnergyYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Rozdzielone designerskie kafelki statystyk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    DesignerMetricBox(
                        label = "Latarnie",
                        value = "${(stats.totalEnergyWh / 50.0).roundToInt()} h",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // WYKRES STATYSTYKI: SINUSOIDA, WIĘCEJ PRZESTRZENI, TYLKO "AKTYWNOŚĆ" I "+5%"
        SinusoidActivityChartCard(
            weeklyData = stats.weeklyEnergyWh,
            primaryColor = primaryColor
        )

        // Cel Społecznościowy (Uproszczony bez zbędnego tekstu)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cel: Park na Zdrowiu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "78%",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                LinearProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = primaryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Stacje w pobliżu (Uproszczone: tylko nazwa, odległość i skanuj kod)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "W POBLIŻU",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            TextButton(
                onClick = onGoToMap,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Mapa ➔",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        MockData.sampleStations.take(2).forEach { station ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onScanStation),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(primaryColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = station.type.icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(station.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(station.distance, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Skanuj QR", tint = primaryColor, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun DesignerMetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// WYKRES: GŁADKA SINUSOIDA Z WIĘKSZĄ PRZESTRZENIĄ, TYLKO "AKTYWNOŚĆ" I PIGUŁKA "+5%"
@Composable
fun SinusoidActivityChartCard(
    weeklyData: List<Double>,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

                // Minimalistyczna pigułka z "+5%"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+5%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Gładka krzywa Canvas z gradientowym wypełnieniem pod sinusoidą
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = (weeklyData.maxOrNull() ?: 100.0).coerceAtLeast(80.0).toFloat()
                    val points = weeklyData.mapIndexed { idx, v ->
                        val x = (idx.toFloat() / (weeklyData.size - 1)) * size.width
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

                        // Gradientowe wypełnienie pod krzywą
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                                startY = 0f,
                                endY = size.height
                            )
                        )

                        // Główna gładka linia sinusoidy
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Punkt na szczycie ostatniej wartości
                        val lastPoint = points.last()
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = lastPoint
                        )
                    }
                }
            }

            // Dni tygodnia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd").forEachIndexed { idx, d ->
                    val isToday = idx == 4
                    Text(
                        text = d,
                        fontSize = 11.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

@Preview
@Composable
fun DashboardScreenPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        DashboardScreen(
            stats = UserStats(),
            onOpenSettings = {},
            onGoToMap = {},
            onScanStation = {}
        )
    }
}
