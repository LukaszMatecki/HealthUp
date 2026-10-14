package com.lumatech.voltup

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
fun WorkoutScreen(
    station: KineticStation,
    targetWh: Int = 100,
    soundEnabled: Boolean = true,
    hapticEnabled: Boolean = true,
    onFinishWorkout: (earnedWh: Double, earnedPoints: Int, workoutRecord: WorkoutSessionRecord) -> Unit,
    onCancel: () -> Unit
) {
    var secondsElapsed by remember { mutableStateOf(0) }
    var resistanceLevel by remember { mutableStateOf(5) } // Opór 1 - 10
    var isPaused by remember { mutableStateOf(false) }
    var currentWatts by remember { mutableStateOf(65) }
    var currentRpm by remember { mutableStateOf(62) }
    var totalGeneratedWh by remember { mutableStateOf(0.0) }
    var boostActiveSeconds by remember { mutableStateOf(0) }
    var recentWattsHistory by remember { mutableStateOf(listOf(40, 48, 55, 60, 65)) }
    var showCompletionModal by remember { mutableStateOf(false) }
    var snackbarFeedback by remember { mutableStateOf<String?>(null) }

    // Live Workout Engine Loop
    LaunchedEffect(isPaused, resistanceLevel, boostActiveSeconds) {
        while (!isPaused) {
            delay(1.seconds)
            secondsElapsed++

            val baseWatts = resistanceLevel * 14
            val boostBonus = if (boostActiveSeconds > 0) 50 else 0
            if (boostActiveSeconds > 0) boostActiveSeconds--

            val randomDelta = (-6..8).random()
            currentWatts = (baseWatts + boostBonus + randomDelta).coerceAtLeast(12)
            currentRpm = (50 + (resistanceLevel * 2.5) + (-3..4).random()).roundToInt().coerceIn(40, 110)

            val whPerSecond = currentWatts / 3600.0
            totalGeneratedWh += whPerSecond

            recentWattsHistory = (recentWattsHistory.takeLast(15) + currentWatts)

            // Milestone cues
            val currentWhInt = totalGeneratedWh.toInt()
            if (currentWhInt > 0 && currentWhInt % 25 == 0 && secondsElapsed % 5 == 0) {
                if (soundEnabled || hapticEnabled) {
                    snackbarFeedback = "⚡ Świetne tempo! Wygenerowano już $currentWhInt Wh do sieci Łodzi!"
                }
            }
        }
    }

    val earnedPoints = (totalGeneratedWh * 1.0).roundToInt().coerceAtLeast(1)
    val co2Saved = totalGeneratedWh * 0.0007
    val caloriesBurned = (totalGeneratedWh * 0.75 + (secondsElapsed * 0.1)).roundToInt()
    val progressToGoal = (totalGeneratedWh / targetWh.toDouble()).toFloat().coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        snackbarHost = {
            snackbarFeedback?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = primaryColor,
                    contentColor = Color.Black
                ) {
                    Text(text = msg, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPaused) EnergyYellow.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPaused) EnergyYellow else primaryColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isPaused) EnergyYellow else primaryColor)
                        )
                        Text(
                            text = if (isPaused) "PAUZA" else "GENEROWANIE ENERGII",
                            color = if (isPaused) EnergyYellow else primaryColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = station.type.icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Target Session Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cel sesji: $targetWh Wh",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(progressToGoal * 100).toInt()}%",
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressToGoal },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = primaryColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // GAUGE CIRCULAR CANVAS
            Box(
                modifier = Modifier.size(230.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background track
                    drawCircle(
                        color = Color(0xFF1E2D35).copy(alpha = 0.8f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Arc indicating power level
                    val sweepAngle = ((currentWatts.toFloat() / 160f) * 280f).coerceIn(10f, 280f)
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(primaryColor, EnergyYellow, primaryColor)
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
                    if (boostActiveSeconds > 0) {
                        Surface(
                            color = EnergyYellow,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TURBO BOOST (${boostActiveSeconds}s)",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = if (isPaused) "PAUZA" else "$currentWatts",
                        fontSize = if (isPaused) 34.sp else (54 * pulseScale).sp,
                        fontWeight = FontWeight.Black,
                        color = if (boostActiveSeconds > 0) EnergyYellow else primaryColor
                    )
                    Text(
                        text = "MOC CHWILOWA (WATY)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currentRpm RPM • Kadencja",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Power Waveform Mini-Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Wykres generacji mocy (W)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Śr. ${(recentWattsHistory.average()).toInt()} W",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (recentWattsHistory.size >= 2) {
                            val maxW = (recentWattsHistory.maxOrNull() ?: 120).coerceAtLeast(100).toFloat()
                            val stepX = size.width / (recentWattsHistory.size - 1)
                            val path = Path()

                            recentWattsHistory.forEachIndexed { index, watts ->
                                val x = index * stepX
                                val y = size.height - (watts / maxW * size.height).coerceIn(4f, size.height - 4f)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            // Resistance Level Controller
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = EnergyYellow,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Opór Urządzenia: Poziom $resistanceLevel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    if (resistanceLevel > 1) {
                                        resistanceLevel--
                                        if (hapticEnabled) snackbarFeedback = "Opór zmniejszony do poziomu $resistanceLevel"
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Zmniejsz opór")
                            }

                            IconButton(
                                onClick = {
                                    if (resistanceLevel < 10) {
                                        resistanceLevel++
                                        if (hapticEnabled) snackbarFeedback = "Opór zwiększony do poziomu $resistanceLevel"
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = primaryColor, contentColor = Color.Black)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Zwiększ opór")
                            }
                        }
                    }

                    // Resistance Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(2 to "Lekki", 5 to "Średni", 8 to "Mocny", 10 to "Beast").forEach { (level, name) ->
                            val isCurrent = resistanceLevel == level
                            OutlinedButton(
                                onClick = { resistanceLevel = level },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isCurrent) primaryColor.copy(alpha = 0.2f) else Color.Transparent
                                ),
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor) else null
                            ) {
                                Text(name, fontSize = 11.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            // Stats Dashboard Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WorkoutStatItem(
                    label = "Czas trwania",
                    value = secondsElapsed.formatSeconds(),
                    icon = Icons.Default.Timer
                )
                WorkoutStatItem(
                    label = "Energia",
                    value = "${totalGeneratedWh.formatOneDecimal()} Wh",
                    icon = Icons.Default.FlashOn
                )
                WorkoutStatItem(
                    label = "Kalorie",
                    value = "$caloriesBurned kcal",
                    icon = Icons.Default.LocalFireDepartment
                )
                WorkoutStatItem(
                    label = "VoltPoints",
                    value = "+$earnedPoints",
                    icon = Icons.Default.Stars
                )
            }

            // Interactive simulation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        boostActiveSeconds = 12
                        snackbarFeedback = "⚡ TURBO SPRINT AKTYWOWANY (+50W na 12 sek.)!"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EnergyYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SPRINT +50W", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                OutlinedButton(
                    onClick = {
                        secondsElapsed += 120
                        val addedWh = (currentWatts * (120.0 / 3600.0))
                        totalGeneratedWh += addedWh
                        snackbarFeedback = "+2 minuty dodane do sesji!"
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+2 MINUTY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bottom Action Buttons
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
                            showCompletionModal = true
                        },
                        modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "ZAKOŃCZ TRENING ⚡",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Anuluj sesję bez zapisu",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal Zakończenia Treningu i Podsumowania
    if (showCompletionModal) {
        val record = WorkoutSessionRecord(
            id = "W-${System.currentTimeMillis().toString().takeLast(6)}",
            stationName = station.name,
            stationLocation = station.location,
            dateText = "Dzisiaj",
            durationSeconds = secondsElapsed,
            generatedWh = totalGeneratedWh,
            earnedPoints = earnedPoints,
            co2SavedKg = co2Saved,
            caloriesKcal = caloriesBurned
        )

        AlertDialog(
            onDismissRequest = { /* Require explicit confirmation */ },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = EnergyYellow,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wspaniały Trening!", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Energia trafiła do sieci Łodzi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wygenerowano energii:", style = MaterialTheme.typography.bodySmall)
                                Text("${totalGeneratedWh.formatOneDecimal()} Wh", fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Zdobyte VoltPoints:", style = MaterialTheme.typography.bodySmall)
                                Text("+$earnedPoints pkt", fontWeight = FontWeight.Bold, color = EnergyYellow)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Czas aktywności:", style = MaterialTheme.typography.bodySmall)
                                Text(secondsElapsed.formatSeconds(), fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Spalone kalorie:", style = MaterialTheme.typography.bodySmall)
                                Text("$caloriesBurned kcal", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Oszczędzone CO₂:", style = MaterialTheme.typography.bodySmall)
                                Text("${co2Saved.formatOneDecimal()} kg", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Eko-ekwiwalent
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = primaryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Zasiliłeś ${(totalGeneratedWh / 50.0 * 60).roundToInt().coerceAtLeast(1)} minut oświetlenia alei parkowej w Łodzi!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompletionModal = false
                        onFinishWorkout(totalGeneratedWh, earnedPoints, record)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ODBIERZ PUNKTY I WRÓĆ", fontWeight = FontWeight.Black)
                }
            }
        )
    }

    LaunchedEffect(snackbarFeedback) {
        if (snackbarFeedback != null) {
            delay(3000)
            snackbarFeedback = null
        }
    }
}

@Composable
fun WorkoutStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
