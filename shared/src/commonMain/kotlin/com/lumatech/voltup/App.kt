package com.lumatech.voltup

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
    var notifications by remember { mutableStateOf(MockData.sampleNotifications) }
    var showQrScannerModal by remember { mutableStateOf(false) }

    // Intercept back button: go to Main or HOME tab instead of closing the app!
    val canGoBack = currentScreen != Screen.Main || currentTab != MainTab.HOME
    CommonBackHandler(enabled = canGoBack) {
        if (currentScreen is Screen.Settings) {
            currentScreen = Screen.Main
        } else if (currentScreen is Screen.ActiveWorkout) {
            // Handled inside WorkoutScreen
        } else if (currentTab != MainTab.HOME) {
            currentTab = MainTab.HOME
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
                                    userStats = userStats.copy(userDistrict = newSettings.homeDistrict)
                                },
                                onNavigateBack = { currentScreen = Screen.Main },
                                onClearHistory = {
                                    userStats = userStats.copy(recentWorkouts = emptyList())
                                },
                                onResetDefaults = {
                                    appSettings = AppSettings()
                                    SnackbarHelper.show("Ustawienia przywrócone do domyślnych.")
                                }
                            )
                        }

                        is Screen.ActiveWorkout -> {
                            WorkoutScreen(
                                station = screen.station,
                                targetWh = appSettings.targetSessionWh,
                                soundEnabled = appSettings.soundEffectsEnabled,
                                hapticEnabled = appSettings.hapticFeedbackEnabled,
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
                                    SnackbarHelper.show("Sesja zapisana! +$earnedPoints VoltPoints")
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
                                        onQrClick = { showQrScannerModal = true }
                                    )
                                }
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                    when (currentTab) {
                                        MainTab.HOME -> {
                                            DashboardScreen(
                                                stats = userStats,
                                                onOpenSettings = { currentScreen = Screen.Settings },
                                                onStartWorkout = { station ->
                                                    currentScreen = Screen.ActiveWorkout(station)
                                                },
                                                onGoToMap = { currentTab = MainTab.MAP }
                                            )
                                        }

                                        MainTab.MAP -> {
                                            StationsMapScreen(
                                                stations = MockData.sampleStations,
                                                onSelectStationToWorkout = { station ->
                                                    currentScreen = Screen.ActiveWorkout(station)
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
                                                        redeemedDate = "Dzisiaj"
                                                    )
                                                    redeemedRewards = listOf(newRedeemed) + redeemedRewards
                                                },
                                                onToggleRewardUsed = { target ->
                                                    redeemedRewards = redeemedRewards.map {
                                                        if (it.discountCode == target.discountCode) it.copy(isUsed = !it.isUsed)
                                                        else it
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
                                                    SnackbarHelper.show("+${challenge.rewardPoints} VoltPoints za wyzwanie!")
                                                },
                                                onUpdateUserProfile = { newName, newDistrict, newAvatarId ->
                                                    userStats = userStats.copy(
                                                        userName = newName,
                                                        userDistrict = newDistrict,
                                                        avatarId = newAvatarId
                                                    )
                                                    appSettings = appSettings.copy(homeDistrict = newDistrict)
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

                // QR Scanner Modal
                if (showQrScannerModal) {
                    MinimalQrScannerModal(
                        onDismiss = { showQrScannerModal = false },
                        onStationDetected = { station ->
                            showQrScannerModal = false
                            currentScreen = Screen.ActiveWorkout(station)
                        }
                    )
                }
            }

            // Floating Custom Snackbar
            VoltUpSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
        }
    }
}

// Sleek Bottom Navigation Bar with Floating Center QR Button in Circle
@Composable
fun VoltUpBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onQrClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left tabs: Pulpit, Mapa
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceAround) {
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
            }

            // Center Floating Circular QR Button
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .offset(y = (-6).dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(elevation = 10.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onQrClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Skanuj QR urządzenia",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Right tabs: Nagrody, Profil
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceAround) {
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tab.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DashboardScreen(
    stats: UserStats,
    onOpenSettings: () -> Unit,
    onStartWorkout: (KineticStation) -> Unit,
    onGoToMap: () -> Unit
) {
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Minimalist Clean Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VOLTUP ŁÓDŹ",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cześć, ${stats.userName.split(" ").firstOrNull() ?: "Łodzianinie"} 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ustawienia",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Portfel Punktów VoltPoints (Clean minimalist card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PORTFEL PUNKTÓW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${stats.points}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VoltPoints",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EnergyYellow.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "#${stats.cityContributionRank} w Łodzi",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = EnergyYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CleanStatItem(label = "Wygenerowano", value = "${(stats.totalEnergyWh / 1000.0).formatOneDecimal()} kWh")
                    CleanStatItem(label = "CO₂ redukcja", value = "${stats.co2SavedKg.formatOneDecimal()} kg")
                    CleanStatItem(label = "Latarnie", value = "${(stats.totalEnergyWh / 50.0).roundToInt()} godz.")
                }
            }
        }

        // WYKRES STATYSTYKI TYGODNIA (Weekly Activity Bar Chart)
        WeeklyEnergyChartCard(
            weeklyData = stats.weeklyEnergyWh,
            primaryColor = primaryColor
        )

        // Cel Społecznościowy Łodzi (Clean Minimalist)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cel Łodzi: Park na Zdrowiu",
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
                Text(
                    text = "Zbieramy energię na oświetlenie alei w Parku na Zdrowiu (3 900 / 5 000 kWh).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = primaryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Sekcja najbliższych stacji
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EKO-PUNKTY W POBLIŻU",
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
                    text = "Pełna mapa ➔",
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
                    .clickable { onStartWorkout(station) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = station.type.icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(station.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${station.location} • ${station.distance}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text("Ćwicz ➔", fontSize = 12.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

// Minimalist Weekly Energy Activity Bar Chart
@Composable
fun WeeklyEnergyChartCard(
    weeklyData: List<Double>,
    primaryColor: Color
) {
    val days = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd")
    val maxEnergy = (weeklyData.maxOrNull() ?: 150.0).coerceAtLeast(100.0).toFloat()
    val totalThisWeek = weeklyData.sum().roundToInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AKTYWNOŚĆ ENERGETYCZNA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalThisWeek Wh w tym tyg.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+18% vs ub. tydz.",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Canvas Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEachIndexed { index, value ->
                    val isToday = index == 4 // Piątek / Active day
                    val heightFraction = (value / maxEnergy).toFloat().coerceIn(0.1f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(heightFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (isToday) primaryColor else primaryColor.copy(alpha = 0.35f))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = days.getOrElse(index) { "" },
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CleanStatItem(label: String, value: String) {
    Column {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Minimalist Clean QR Scanner Modal
@Composable
fun MinimalQrScannerModal(
    onDismiss: () -> Unit,
    onStationDetected: (KineticStation) -> Unit
) {
    var manualCodeInput by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = primaryColor)
                Text("Połącz ze stacją", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Nakieruj aparat na kod QR na urządzeniu w parku lub wybierz z listy poniżej:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = manualCodeInput,
                    onValueChange = { manualCodeInput = it },
                    placeholder = { Text("Wpisz kod, np. QR-PONT-01", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (manualCodeInput.isNotBlank()) {
                            IconButton(onClick = {
                                val found = MockData.sampleStations.find {
                                    it.qrCodeId.equals(manualCodeInput.trim(), ignoreCase = true)
                                } ?: MockData.sampleStations.first()
                                onStationDetected(found)
                            }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = primaryColor)
                            }
                        }
                    }
                )

                Text("DOSTĘPNE W POBLIŻU:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)

                MockData.sampleStations.take(3).forEach { station ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onStationDetected(station) },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(station.type.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                                Text(station.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Text("Połącz ⚡", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
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
            onStartWorkout = {},
            onGoToMap = {}
        )
    }
}
