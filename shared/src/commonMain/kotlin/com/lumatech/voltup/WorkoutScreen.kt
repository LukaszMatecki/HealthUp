package com.lumatech.voltup

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

enum class StatPattern {
    CIRCLES, DIAGONALS, WAVES
}

@Composable
fun WorkoutScreen(
    station: KineticStation,
    onFinishWorkout: (earnedWh: Double, earnedPoints: Int, workoutRecord: WorkoutSessionRecord) -> Unit,
    onCancel: () -> Unit
) {
    var hasStarted by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var resistanceLevel by remember { mutableIntStateOf(5) }
    var isPaused by remember { mutableStateOf(false) }
    var currentWatts by remember { mutableIntStateOf(0) }
    var currentRpm by remember { mutableIntStateOf(0) }
    var currentHeartRate by remember { mutableIntStateOf(75) }
    var totalGeneratedWh by remember { mutableDoubleStateOf(0.0) }

    var showCompletionModal by remember { mutableStateOf(false) }
    var showCancelConfirmModal by remember { mutableStateOf(false) }

    val energyHistory = remember { mutableStateListOf<Float>(0f) }
    val scrollState = rememberScrollState()

    CommonBackHandler(enabled = true) {
        showCancelConfirmModal = true
    }

    LaunchedEffect(hasStarted, isPaused, showCompletionModal, showCancelConfirmModal, resistanceLevel) {
        if (hasStarted && !isPaused && !showCompletionModal && !showCancelConfirmModal) {
            while (hasStarted && !isPaused && !showCompletionModal && !showCancelConfirmModal) {
                delay(1.seconds)
                secondsElapsed++

                val baseWatts = resistanceLevel * 16
                val randomDelta = (-5..8).random()
                currentWatts = (baseWatts + randomDelta).coerceAtLeast(10)
                currentRpm = (50 + (resistanceLevel * 2) + (-3..4).random()).coerceIn(40, 100)

                val targetHr = 80 + (resistanceLevel * 6) + (currentRpm / 2)
                currentHeartRate = (targetHr + (-2..2).random()).coerceIn(70, 185)

                val whPerSecond = currentWatts / 3600.0
                totalGeneratedWh += whPerSecond

                energyHistory.add(totalGeneratedWh.toFloat())
                if (energyHistory.size > 60) {
                    energyHistory.removeAt(0)
                }
            }
        }
    }

    LaunchedEffect(isPaused, showCompletionModal, showCancelConfirmModal) {
        if (hasStarted && (isPaused || showCompletionModal || showCancelConfirmModal)) {
            currentWatts = 0
            currentRpm = 0
        }
    }

    val earnedPoints = (totalGeneratedWh * 0.2).toInt()
    val co2Saved = totalGeneratedWh * 0.0007
    val caloriesBurned = (totalGeneratedWh * 0.7 + (secondsElapsed * 0.08)).roundToInt()
    val distanceKm = (secondsElapsed * 0.0065) + (totalGeneratedWh * 0.012)

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val warningColor = Color(0xFFF59E0B)
    val cancelRedColor = Color(0xFFEF4444)
    val caloriesColor = Color(0xFFFF4757)

    val energyGradient = listOf(Color(0xFFFFB75E), Color(0xFFED8F03))
    val resistanceGradient = listOf(Color(0xFF38EF7D), Color(0xFF11998E))
    val rpmGradient = listOf(Color(0xFF56CCF2), Color(0xFF2F80ED))

    val caloriesPillGradient = listOf(Color(0xFFFFE4E6), Color(0xFFFFD1D1))
    val timePillGradient = listOf(Color(0xFFE0E7FF), Color(0xFFD4E4FF))

    val animatedWatts by animateIntAsState(
        targetValue = if (hasStarted && !isPaused && !showCompletionModal && !showCancelConfirmModal) currentWatts else 0,
        animationSpec = tween(800),
        label = "watts"
    )
    val sweepAngle by animateFloatAsState(
        targetValue = if (!hasStarted) 0f else if (isPaused || showCompletionModal) 10f else ((animatedWatts.toFloat() / 150f) * 280f).coerceIn(10f, 280f),
        animationSpec = tween(800),
        label = "sweep"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = station.location,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val statusText = when {
                    !hasStarted -> "GOTOWY"
                    isPaused -> "WSTRZYMANO"
                    else -> "AKTYWNY"
                }
                val statusColor = when {
                    !hasStarted -> primaryColor
                    isPaused -> warningColor
                    else -> primaryColor
                }

                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 16.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2f
                        val center = Offset(size.width / 2, size.height / 2)
                        val topLeftOffset = Offset(center.x - radius, center.y - radius)

                        drawArc(
                            color = surfaceVariantColor.copy(alpha = 0.4f),
                            startAngle = 130f,
                            sweepAngle = 280f,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        if (hasStarted) {
                            drawArc(
                                color = if (isPaused) warningColor else primaryColor,
                                startAngle = 130f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeftOffset,
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!hasStarted) {
                            Text(
                                text = "START",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = primaryColor,
                                letterSpacing = (-1).sp,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    hasStarted = true
                                }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "KLIKNIJ ABY ROZPOCZĄĆ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = if (isPaused) "PAUZA" else animatedWatts.toString(),
                                fontSize = if (isPaused) 36.sp else 64.sp,
                                fontWeight = FontWeight.Light,
                                color = if (isPaused) warningColor else MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-2).sp,
                                modifier = if (isPaused) Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isPaused = false } else Modifier
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPaused) "KLIKNIJ BY WZNOWIĆ" else "WATY (MOC)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable {
                                if (!hasStarted) {
                                    hasStarted = true
                                } else {
                                    isPaused = !isPaused
                                }
                            },
                        shape = CircleShape,
                        color = if (isPaused || !hasStarted) warningColor.copy(alpha = 0.2f) else surfaceVariantColor.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, if (isPaused || !hasStarted) warningColor else surfaceVariantColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (!hasStarted || isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (!hasStarted || isPaused) "Start/Wznów" else "Pauza",
                                tint = if (!hasStarted || isPaused) warningColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable {
                                if (hasStarted) {
                                    showCompletionModal = true
                                }
                            },
                        shape = CircleShape,
                        color = if (hasStarted) primaryColor else primaryColor.copy(alpha = 0.4f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Zakończ sesję",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { showCancelConfirmModal = true },
                        shape = CircleShape,
                        color = cancelRedColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, cancelRedColor.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Anuluj",
                                tint = cancelRedColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatPill(
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = caloriesColor,
                    text = "$caloriesBurned Kcal",
                    textColor = Color(0xFF9F1239),
                    gradientColors = caloriesPillGradient,
                    pattern = StatPattern.CIRCLES,
                    modifier = Modifier.weight(1f)
                )

                StatPill(
                    icon = Icons.Default.Schedule,
                    iconTint = Color(0xFF2563EB),
                    text = secondsElapsed.formatSeconds(),
                    textColor = Color(0xFF1E40AF),
                    gradientColors = timePillGradient,
                    pattern = StatPattern.WAVES,
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, surfaceVariantColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniRingGauge(
                        progress = (totalGeneratedWh / 5.0).toFloat().coerceIn(0f, 1f),
                        gradientColors = energyGradient,
                        title = "Energia"
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = totalGeneratedWh.formatOneDecimal(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Wh", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    var dragAccumulator by remember { mutableFloatStateOf(0f) }

                    MiniRingGauge(
                        progress = resistanceLevel / 10f,
                        gradientColors = resistanceGradient,
                        title = "Opór"
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures(
                                        onDragEnd = { dragAccumulator = 0f }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        dragAccumulator += dragAmount
                                        if (dragAccumulator > 30f) {
                                            if (resistanceLevel > 1) resistanceLevel--
                                            dragAccumulator = 0f
                                        } else if (dragAccumulator < -30f) {
                                            if (resistanceLevel < 10) resistanceLevel++
                                            dragAccumulator = 0f
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$resistanceLevel",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    MiniRingGauge(
                        progress = (currentRpm / 100f).coerceIn(0f, 1f),
                        gradientColors = rpmGradient,
                        title = "Prędkość"
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = currentRpm.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "RPM", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, surfaceVariantColor.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(warningColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = warningColor, modifier = Modifier.size(22.dp))
                            }
                            Text(
                                text = "+$earnedPoints",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor,
                                letterSpacing = (-1).sp
                            )
                        }
                        Text(
                            text = "VoltPoints",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, surfaceVariantColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.Center) {
                                Text("Dystans", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${distanceKm.formatOneDecimal()} km", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Icon(Icons.Default.DirectionsBike, contentDescription = null, tint = rpmGradient[0], modifier = Modifier.size(24.dp))
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, surfaceVariantColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.Center) {
                                Text("Tętno", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$currentHeartRate BPM", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = caloriesColor, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }

    if (showCompletionModal) {
        val record = WorkoutSessionRecord(
            id = "W-${(100000..999999).random()}",
            stationName = station.name,
            stationLocation = station.location,
            dateText = "Dzisiaj",
            durationSeconds = secondsElapsed,
            generatedWh = totalGeneratedWh,
            earnedPoints = earnedPoints,
            co2SavedKg = co2Saved,
            caloriesKcal = caloriesBurned
        )

        Dialog(onDismissRequest = { showCompletionModal = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Podsumowanie Sesji",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "Świetna robota! Wygenerowana przez Ciebie energia właśnie zasiliła miejską sieć w Łodzi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Wygenerowano:", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                Text("${totalGeneratedWh.formatOneDecimal()} Wh", fontWeight = FontWeight.ExtraBold, color = primaryColor)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zdobyte punkty:", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                Text("+$earnedPoints VoltPoints", fontWeight = FontWeight.ExtraBold, color = warningColor)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Czas sesji:", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                Text(secondsElapsed.formatSeconds(), fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Spalone kalorie:", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                Text("$caloriesBurned kcal", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                showCompletionModal = false
                                onFinishWorkout(totalGeneratedWh, earnedPoints, record)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                            shape = CircleShape
                        ) {
                            Text("Zapisz i odbierz punkty", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Button(
                            onClick = { showCompletionModal = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color.DarkGray)
                        ) {
                            Text("Kontynuuj trening", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCancelConfirmModal) {
        Dialog(onDismissRequest = { showCancelConfirmModal = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(cancelRedColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = cancelRedColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Anulować trening?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "Cały postęp tej sesji oraz wygenerowana energia zostaną utracone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                showCancelConfirmModal = false
                                onCancel()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = cancelRedColor, contentColor = Color.White),
                            shape = CircleShape
                        ) {
                            Text("Przerwij", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Button(
                            onClick = { showCancelConfirmModal = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color.DarkGray)
                        ) {
                            Text("Wznów trening", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    textColor: Color,
    gradientColors: List<Color>,
    pattern: StatPattern,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (pattern) {
                StatPattern.CIRCLES -> {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = size.height * 0.7f,
                        center = Offset(0f, 0f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = size.height * 0.4f,
                        center = Offset(size.width, size.height)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = size.height * 0.3f,
                        center = Offset(size.width * 0.75f, size.height * 0.2f)
                    )
                }
                StatPattern.DIAGONALS -> {
                    val stripeWidth = size.height * 0.3f
                    val step = (size.height * 0.8f).toInt()
                    for (i in -size.height.toInt()..(size.width + size.height).toInt() step step) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.25f),
                            start = Offset(i.toFloat(), 0f),
                            end = Offset(i.toFloat() - size.height, size.height),
                            strokeWidth = stripeWidth
                        )
                    }
                }
                StatPattern.WAVES -> {
                    val w = size.width
                    val h = size.height

                    val path1 = Path().apply {
                        moveTo(0f, h * 0.5f)
                        cubicTo(w * 0.3f, h * 0.3f, w * 0.7f, h * 0.7f, w, h * 0.4f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path1, color = Color.White.copy(alpha = 0.15f))

                    val path2 = Path().apply {
                        moveTo(0f, h * 0.7f)
                        cubicTo(w * 0.4f, h * 1.0f, w * 0.6f, h * 0.4f, w, h * 0.6f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path2, color = Color.White.copy(alpha = 0.2f))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

@Composable
fun MiniRingGauge(
    progress: Float,
    gradientColors: List<Color>,
    title: String,
    centerContent: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 7.dp.toPx()
                drawArc(
                    color = gradientColors.last().copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.linearGradient(colors = gradientColors),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            centerContent()
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
fun WorkoutScreenPreview() {
    VoltUpTheme(AppThemeMode.LIGHT) {
        WorkoutScreen(
            station = MockData.sampleStations.first(),
            onFinishWorkout = { _, _, _ -> },
            onCancel = {}
        )
    }
}