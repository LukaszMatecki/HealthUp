package com.lumatech.voltup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

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
}

@Composable
fun App() {
    VoltUpTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
        var currentTab by remember { mutableStateOf(MainTab.HOME) }
        var userStats by remember { mutableStateOf(UserStats()) }
        var challenges by remember { mutableStateOf(MockData.sampleChallenges) }
        var redeemedRewards by remember { mutableStateOf(emptyList<RedeemedReward>()) }
        var showQrScannerModal by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(targetState = currentScreen) { screen ->
                when (screen) {
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
                                                    tint = if (isSelected) ElectricVoltGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.label,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) ElectricVoltGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = ElectricVoltGreen.copy(alpha = 0.2f)
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
                                            onOpenQrScanner = {
                                                showQrScannerModal = true
                                            },
                                            onStartWorkout = { station ->
                                                currentScreen = Screen.ActiveWorkout(station)
                                            },
                                            onGoToMap = {
                                                currentTab = MainTab.MAP
                                            }
                                        )
                                    }
                                    MainTab.MAP -> {
                                        StationsMapScreen(
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
                                            }
                                        )
                                    }
                                    MainTab.PROFILE -> {
                                        ProfileScreen(
                                            stats = userStats,
                                            challenges = challenges,
                                            onClaimChallenge = { challenge ->
                                                userStats = userStats.copy(
                                                    points = userStats.points + challenge.rewardPoints
                                                )
                                                challenges = challenges.map {
                                                    if (it.id == challenge.id) it.copy(isClaimed = true) else it
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is Screen.ActiveWorkout -> {
                        WorkoutScreen(
                            station = screen.station,
                            onFinishWorkout = { earnedWh, earnedPoints ->
                                userStats = userStats.copy(
                                    points = userStats.points + earnedPoints,
                                    totalEnergyWh = userStats.totalEnergyWh + earnedWh,
                                    co2SavedKg = userStats.co2SavedKg + (earnedWh * 0.0007)
                                )
                                // Update challenge progress
                                challenges = challenges.map { challenge ->
                                    if (!challenge.isCompleted) {
                                        val newProgress = (challenge.currentProgress + (earnedWh / 200.0).toFloat()).coerceAtMost(1.0f)
                                        challenge.copy(
                                            currentProgress = newProgress,
                                            isCompleted = newProgress >= 1.0f
                                        )
                                    } else challenge
                                }
                                currentScreen = Screen.Main
                            },
                            onCancel = {
                                currentScreen = Screen.Main
                            }
                        )
                    }
                }
            }

            // QR Scanner Dialog
            if (showQrScannerModal) {
                QrScannerModal(
                    onDismiss = { showQrScannerModal = false },
                    onStationDetected = { station ->
                        showQrScannerModal = false
                        currentScreen = Screen.ActiveWorkout(station)
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    stats: UserStats,
    onOpenQrScanner: () -> Unit,
    onStartWorkout: (KineticStation) -> Unit,
    onGoToMap: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Nagłówek
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
                        tint = ElectricVoltGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "VOLTUP ŁÓDŹ",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricVoltGreen,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Cześć, Łodzianinie!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${stats.cityContributionRank}",
                    color = EnergyYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
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
                                ElectricVoltGreen.copy(alpha = 0.15f),
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
                                    color = ElectricVoltGreen
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

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EnergyYellow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Zniżki MPK",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = EnergyYellow,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
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
                containerColor = ElectricVoltGreen,
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
                        text = "78%",
                        color = ElectricVoltGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Łodzianie zbierają energię na nocne oświetlenie alei lipowej w Parku na Zdrowiu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElectricVoltGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "3 900 kWh / 5 000 kWh",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                text = "EKO-SIŁOWNIE W POBLIŻU",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onGoToMap) {
                Text(
                    text = "Zobacz mapę ➔",
                    color = ElectricVoltGreen,
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
            tint = ElectricVoltGreen,
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
                    tint = ElectricVoltGreen,
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
                color = ElectricVoltGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun QrScannerModal(
    onDismiss: () -> Unit,
    onStationDetected: (KineticStation) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scannerLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = ElectricVoltGreen
                )
                Text("Skaner QR Urządzeń", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Kamera Viewfinder z animowanym celownikiem laserowym
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 3.dp.toPx()
                        val cornerLen = 24.dp.toPx()

                        // Celownik narozny
                        val color = ElectricVoltGreen

                        // Top Left
                        drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
                        drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), stroke)

                        // Top Right
                        drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), stroke)
                        drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLen), stroke)

                        // Bottom Left
                        drawLine(color, Offset(0f, size.height), Offset(cornerLen, size.height), stroke)
                        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLen), stroke)

                        // Bottom Right
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
                            tint = ElectricVoltGreen.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Nakieruj aparat na kod QR",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Text(
                    text = "LUB WYBIERZ STACJĘ TESTOWĄ:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MockData.sampleStations.forEach { station ->
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
                                        tint = ElectricVoltGreen,
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

@Composable
fun WorkoutScreen(
    station: KineticStation,
    onFinishWorkout: (earnedWh: Double, earnedPoints: Int) -> Unit,
    onCancel: () -> Unit
) {
    var secondsElapsed by remember { mutableStateOf(0) }
    var resistanceLevel by remember { mutableStateOf(5) } // Opór 1 - 10
    var isPaused by remember { mutableStateOf(false) }
    var currentWatts by remember { mutableStateOf(65) }
    var totalGeneratedWh by remember { mutableStateOf(0.0) }

    LaunchedEffect(isPaused, resistanceLevel) {
        while (!isPaused) {
            delay(1.seconds)
            secondsElapsed++
            // Waty zależą od oporu!
            val baseWatts = resistanceLevel * 14
            currentWatts = (baseWatts - 8..baseWatts + 12).random().coerceAtLeast(10)
            val whPerSecond = currentWatts / 3600.0
            totalGeneratedWh += whPerSecond
        }
    }

    val earnedPoints = (totalGeneratedWh * 10).roundToInt()

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Górny status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElectricVoltGreen.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricVoltGreen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (isPaused) EnergyYellow else ElectricVoltGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isPaused) "WSTRZYMANO: ${station.name.uppercase()}" else "SESJA AKTYWNA: ${station.name.uppercase()}",
                        color = ElectricVoltGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = station.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Wskaźnik zegarowy (Canvas)
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                drawCircle(
                    color = Color(0xFF1E2D35),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                val sweepAngle = ((currentWatts.toFloat() / 150f) * 280f).coerceIn(10f, 280f)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ElectricVoltGreen, EnergyYellow, ElectricVoltGreen)
                    ),
                    startAngle = 130f,
                    sweepAngle = if (isPaused) 10f else sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isPaused) "PAUZA" else "$currentWatts",
                    fontSize = if (isPaused) 32.sp else (52 * pulseScale).sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricVoltGreen
                )
                Text(
                    text = "MOC GENEROWANA (W)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Kontrola oporu / Poziomu trudności
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = EnergyYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Opór Urządzenia: Lvl $resistanceLevel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { if (resistanceLevel > 1) resistanceLevel-- },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Zmniejsz opór")
                    }

                    IconButton(
                        onClick = { if (resistanceLevel < 10) resistanceLevel++ },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = ElectricVoltGreen, contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Zwiększ opór")
                    }
                }
            }
        }

        // Statystyki
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            WorkoutStat(
                label = "Czas trwania",
                value = secondsElapsed.formatSeconds(),
                icon = Icons.Default.Timer
            )
            WorkoutStat(
                label = "Energia",
                value = "${totalGeneratedWh.formatOneDecimal()} Wh",
                icon = Icons.Default.FlashOn
            )
            WorkoutStat(
                label = "VoltPoints",
                value = "+$earnedPoints",
                icon = Icons.Default.Stars
            )
        }

        // Przyciski akcji
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isPaused) "WZNÓW" else "PAUZA", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onFinishWorkout(totalGeneratedWh, earnedPoints)
                    },
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricVoltGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "ZAKOŃCZ I ODBIERZ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Anuluj sesję",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WorkoutStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricVoltGreen,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
