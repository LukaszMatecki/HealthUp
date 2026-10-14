package com.lumatech.voltup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

enum class MainTab(val label: String) {
    HOME("Pulpit"),
    MAP("Stacje"),
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
    var cityGoalProgress by remember { mutableStateOf(0.78f) }
    var showQrScannerModal by remember { mutableStateOf(false) }
    var showNotificationsModal by remember { mutableStateOf(false) }

    VoltUpTheme(themeMode = appSettings.themeMode) {
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
                                userStats = userStats.copy(
                                    userDistrict = newSettings.homeDistrict
                                )
                            },
                            onNavigateBack = { currentScreen = Screen.Main },
                            onClearHistory = {
                                userStats = userStats.copy(recentWorkouts = emptyList())
                            },
                            onResetDefaults = {
                                appSettings = AppSettings()
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
                                // Update challenges
                                challenges = challenges.map { challenge ->
                                    if (!challenge.isCompleted) {
                                        val newProgress = (challenge.currentProgress + (earnedWh / 200.0).toFloat()).coerceAtMost(1.0f)
                                        challenge.copy(
                                            currentProgress = newProgress,
                                            isCompleted = newProgress >= 1.0f
                                        )
                                    } else challenge
                                }
                                // Update badges
                                if (earnedWh >= 100) {
                                    badges = badges.map {
                                        if (it.id == "B4") it.copy(isUnlocked = true) else it
                                    }
                                }
                                currentScreen = Screen.Main
                            },
                            onCancel = {
                                currentScreen = Screen.Main
                            }
                        )
                    }

                    is Screen.Main -> {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp
                                ) {
                                    MainTab.entries.forEach { tab ->
                                        val isSelected = currentTab == tab
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.label,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.label,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            )
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (currentTab) {
                                    MainTab.HOME -> {
                                        DashboardScreen(
                                            stats = userStats,
                                            cityGoalProgress = cityGoalProgress,
                                            unreadNotificationsCount = notifications.count { !it.isRead },
                                            onOpenNotifications = { showNotificationsModal = true },
                                            onOpenSettings = { currentScreen = Screen.Settings },
                                            onOpenQrScanner = { showQrScannerModal = true },
                                            onStartWorkout = { station ->
                                                currentScreen = Screen.ActiveWorkout(station)
                                            },
                                            onGoToMap = { currentTab = MainTab.MAP },
                                            onGoToRewards = { currentTab = MainTab.REWARDS },
                                            onContributeToCityGoal = {
                                                cityGoalProgress = (cityGoalProgress + 0.04f).coerceAtMost(1.0f)
                                                userStats = userStats.copy(
                                                    points = (userStats.points + 25)
                                                )
                                            }
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
                                                    redeemedDate = "Dzisiaj, 14:20"
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
                                            },
                                            onUpdateUserProfile = { newName, newDistrict, newAvatarId ->
                                                userStats = userStats.copy(
                                                    userName = newName,
                                                    userDistrict = newDistrict,
                                                    avatarId = newAvatarId
                                                )
                                                appSettings = appSettings.copy(homeDistrict = newDistrict)
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
                QrScannerModal(
                    onDismiss = { showQrScannerModal = false },
                    onStationDetected = { station ->
                        showQrScannerModal = false
                        currentScreen = Screen.ActiveWorkout(station)
                    }
                )
            }

            // Notifications Modal
            if (showNotificationsModal) {
                NotificationsModal(
                    notifications = notifications,
                    onDismiss = { showNotificationsModal = false },
                    onMarkAllAsRead = {
                        notifications = notifications.map { it.copy(isRead = true) }
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    stats: UserStats,
    cityGoalProgress: Float,
    unreadNotificationsCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onStartWorkout: (KineticStation) -> Unit,
    onGoToMap: () -> Unit,
    onGoToRewards: () -> Unit,
    onContributeToCityGoal: () -> Unit
) {
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Górny pasek nawigacyjny
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "VOLTUP ŁÓDŹ",
                        style = MaterialTheme.typography.labelLarge,
                        color = primaryColor,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Cześć, ${stats.userName.split(" ").firstOrNull() ?: "Łodzianinie"}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Notifications icon
                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge(containerColor = EnergyYellow, contentColor = Color.Black) {
                                Text("$unreadNotificationsCount", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onOpenNotifications,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Powiadomienia")
                    }
                }

                // Settings icon
                IconButton(
                    onClick = onOpenSettings,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Ustawienia")
                }
            }
        }

        // Real-Time City Grid Energy Pulse Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SIEĆ MIEJSKA ŁODZI DZISIAJ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "28.4 kWh wygenerowane przez 86 mieszkańców",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "ONLINE ⚡",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = primaryColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Portfel Punktów VoltPoints
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            radius = 600f
                        )
                    )
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "TWÓJ PORTFEL PUNKTÓW",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${stats.points}",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = primaryColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VoltPoints",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onGoToRewards,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EnergyYellow,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("BONY MPK", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Statystyki
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            label = "Wygenerowano",
                            value = "${(stats.totalEnergyWh / 1000.0).formatOneDecimal()} kWh",
                            icon = Icons.Default.FlashOn
                        )
                        StatItem(
                            label = "Oszczędzone CO₂",
                            value = "${stats.co2SavedKg.formatOneDecimal()} kg",
                            icon = Icons.Default.Park
                        )
                        StatItem(
                            label = "Zasilono latarnie",
                            value = "${(stats.totalEnergyWh / 50.0).roundToInt()} h",
                            icon = Icons.Default.Lightbulb
                        )
                    }
                }
            }
        }

        // Przycisk Skanera QR
        Button(
            onClick = onOpenQrScanner,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "SKANUJ KOD QR URZĄDZENIA",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // Cel Społecznościowy Łodzi
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = EnergyYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Cel Łodzi: Park na Zdrowiu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${(cityGoalProgress * 100).toInt()}%",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Łodzianie zbierają energię na nocne oświetlenie alei lipowej w Parku na Zdrowiu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { cityGoalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = primaryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${((cityGoalProgress * 5000)).toInt()} kWh / 5 000 kWh",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = onContributeToCityGoal,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                            Text("Wesprzyj cel (+25 pkt)", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Ostatnia sesja treningowa podgląd
        if (stats.recentWorkouts.isNotEmpty()) {
            val lastWorkout = stats.recentWorkouts.first()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp)
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = primaryColor)
                        }
                        Column {
                            Text("Ostatni Trening", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(lastWorkout.stationName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("+${lastWorkout.earnedPoints} pkt", fontWeight = FontWeight.Black, color = EnergyYellow)
                        Text("${lastWorkout.generatedWh.formatOneDecimal()} Wh", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                    }
                }
            }
        }

        // Sekcja najbliższych stacji
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EKO-SIŁOWNIE W POBLIŻU",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onGoToMap) {
                Text(
                    text = "Zobacz mapę ➔",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        MockData.sampleStations.take(2).forEach { station ->
            StationPreviewItem(
                station = station,
                onSelect = { onStartWorkout(station) }
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StationPreviewItem(
    station: KineticStation,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = station.type.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = station.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${station.location} • ${station.distance}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "ĆWICZ ➔",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun NotificationsModal(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Powiadomienia", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onMarkAllAsRead) {
                    Text("Odczytaj", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                notifications.forEach { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (item.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (item.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(item.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}

@Composable
fun QrScannerModal(
    onDismiss: () -> Unit,
    onStationDetected: (KineticStation) -> Unit
) {
    var manualCodeInput by remember { mutableStateOf("") }
    var isFlashlightOn by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val scannerLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Text("Skaner QR Stacji", fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = { isFlashlightOn = !isFlashlightOn }) {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Latarka",
                        tint = if (isFlashlightOn) EnergyYellow else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Kamera Viewfinder z animowanym celownikiem laserowym
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isFlashlightOn) Color(0xFF333333) else Color.Black)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 3.dp.toPx()
                        val cornerLen = 24.dp.toPx()
                        val color = primaryColor

                        // Celownik narożny
                        drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
                        drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), stroke)

                        drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), stroke)
                        drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLen), stroke)

                        drawLine(color, Offset(0f, size.height), Offset(cornerLen, size.height), stroke)
                        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLen), stroke)

                        drawLine(color, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), stroke)
                        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), stroke)

                        // Laserowy promień skanowania
                        val laserY = scannerLineOffset * size.height
                        drawLine(
                            color = EnergyYellow,
                            start = Offset(0f, laserY),
                            end = Offset(size.width, laserY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = primaryColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (isFlashlightOn) "Latarka aktywna" else "Nakieruj na kod QR stacji",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Ręczny kod QR input
                OutlinedTextField(
                    value = manualCodeInput,
                    onValueChange = { manualCodeInput = it },
                    placeholder = { Text("Wpisz kod ręcznie, np. QR-PONT-01", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (manualCodeInput.isNotBlank()) {
                            IconButton(onClick = {
                                val found = MockData.sampleStations.find {
                                    it.qrCodeId.equals(manualCodeInput.trim(), ignoreCase = true) ||
                                            it.id.equals(manualCodeInput.trim(), ignoreCase = true)
                                } ?: MockData.sampleStations.first()
                                onStationDetected(found)
                            }) {
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = primaryColor)
                            }
                        }
                    }
                )

                Text(
                    text = "LUB WYBIERZ STACJĘ TESTOWĄ:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MockData.sampleStations.take(3).forEach { station ->
                        OutlinedButton(
                            onClick = { onStationDetected(station) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = station.type.icon,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(station.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("POŁĄCZ ⚡", fontSize = 11.sp, color = EnergyYellow, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
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
