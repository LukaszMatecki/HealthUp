package com.lumatech.voltup

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WorkoutScreen(
    station: KineticStation,
    onFinishWorkout: (earnedWh: Double, earnedPoints: Int, workoutRecord: WorkoutSessionRecord) -> Unit,
    onCancel: () -> Unit
) {
    var secondsElapsed by remember { mutableStateOf(0) }
    var resistanceLevel by remember { mutableStateOf(5) }
    var isPaused by remember { mutableStateOf(false) }
    var currentWatts by remember { mutableStateOf(65) }
    var currentRpm by remember { mutableStateOf(60) }
    var totalGeneratedWh by remember { mutableStateOf(0.0) }
    var showCompletionModal by remember { mutableStateOf(false) }
    var showCancelConfirmModal by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    CommonBackHandler(enabled = true) {
        showCancelConfirmModal = true
    }

    LaunchedEffect(isPaused, resistanceLevel) {
        while (!isPaused) {
            delay(1.seconds)
            secondsElapsed++

            val baseWatts = resistanceLevel * 14
            val randomDelta = (-4..6).random()
            currentWatts = (baseWatts + randomDelta).coerceAtLeast(10)
            currentRpm = (52 + (resistanceLevel * 2) + (-2..3).random()).coerceIn(40, 95)

            val whPerSecond = currentWatts / 3600.0
            totalGeneratedWh += whPerSecond
        }
    }

    val earnedPoints = (totalGeneratedWh * 1.0).roundToInt().coerceAtLeast(1)
    val co2Saved = totalGeneratedWh * 0.0007
    val caloriesBurned = (totalGeneratedWh * 0.7 + (secondsElapsed * 0.08)).roundToInt()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val warningColor = Color(0xFFF59E0B)
    val caloriesColor = Color(0xFFFF6B6B)

    // Płynna wartość Watów dla animacji na wykresach
    val animatedWatts by animateIntAsState(
        targetValue = if (isPaused) 0 else currentWatts,
        animationSpec = tween(800),
        label = "watts"
    )

    // Znormalizowana wartość mocy (0f - 1f) dla fali i łuku
    val powerRatio by animateFloatAsState(
        targetValue = (animatedWatts.toFloat() / 150f).coerceIn(0f, 1f),
        animationSpec = tween(1200),
        label = "powerRatio"
    )

    val sweepAngle = if (isPaused) 10f else (powerRatio * 280f).coerceAtLeast(10f)

    // Zdecydowanie wolniejsza animacja falowania
    val infiniteTransition = rememberInfiniteTransition(label = "energyWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
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
            // Nagłówek
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

                Surface(
                    shape = CircleShape,
                    color = if (isPaused) warningColor.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.15f)
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
                                .background(if (isPaused) warningColor else primaryColor)
                        )
                        Text(
                            text = if (isPaused) "WSTRZYMANO" else "AKTYWNY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPaused) warningColor else primaryColor,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // GŁÓWNY WYKRES MOCY
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    val topLeftOffset = Offset(center.x - radius, center.y - radius)

                    drawArc(
                        color = surfaceVariantColor.copy(alpha = 0.5f),
                        startAngle = 130f,
                        sweepAngle = 280f,
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

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

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isPaused) "PAUZA" else animatedWatts.toString(),
                        fontSize = if (isPaused) 42.sp else 76.sp,
                        fontWeight = FontWeight.Light,
                        color = if (isPaused) warningColor else MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-2).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPaused) "TRENING WSTRZYMANY" else "WATY (MOC)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isPaused) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "$currentRpm RPM",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor
                            )
                        }
                    }
                }
            }

            // DASHBOARD STATYSTYK
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // WIERZ 1: Kalorie + Energia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // KALORIE KARTA
                    Card(
                        modifier = Modifier.weight(1f).aspectRatio(0.9f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(caloriesColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = caloriesColor, modifier = Modifier.size(18.dp))
                                }
                                Text("Kalorie", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "$caloriesBurned",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("kcal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Gęsty wykres słupkowy (Wiele cienkich i kilka grubych kresek)
                            Row(
                                modifier = Modifier.fillMaxWidth().height(28.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val fractions = listOf(
                                    0.3f, 0.15f, 0.2f, 0.1f, 0.45f, 0.2f, 0.25f, 0.15f,
                                    0.6f, 0.3f, 0.25f, 0.2f, 0.75f, 0.35f, 0.4f, 0.25f,
                                    0.85f, 0.4f, 0.5f, 0.3f, 0.95f, 0.45f, 0.55f, 0.35f, 1.0f
                                )
                                fractions.forEachIndexed { idx, frac ->
                                    val isMain = idx % 4 == 0
                                    Box(
                                        modifier = Modifier
                                            .width(if (isMain) 4.dp else 1.5.dp)
                                            .fillMaxHeight(frac)
                                            .clip(CircleShape)
                                            .background(if (idx == fractions.lastIndex) caloriesColor else MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                        }
                    }

                    // ENERGIA KARTA (Zależna od Watów)
                    Card(
                        modifier = Modifier.weight(1f).aspectRatio(0.9f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxSize(),
                                verticalArrangement = Arrangement.Top
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(warningColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = warningColor, modifier = Modifier.size(18.dp))
                                    }
                                    Text("Energia", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = totalGeneratedWh.formatOneDecimal(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("Wh", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Reagująca fala na dole
                            Canvas(modifier = Modifier.fillMaxSize().align(Alignment.BottomCenter)) {
                                val w = size.width
                                val h = size.height

                                // Amplituda i położenie Y zależą od powerRatio (Waty)
                                val amplitude = h * 0.05f + (powerRatio * h * 0.1f)
                                val midY = h * 0.85f - (powerRatio * h * 0.4f)
                                val frequency = (2 * PI / w).toFloat() * 1.5f

                                val wavePath = Path().apply {
                                    moveTo(0f, midY + sin(wavePhase) * amplitude)
                                    for (x in 0..w.toInt() step 5) {
                                        val y = midY + sin(frequency * x + wavePhase) * amplitude
                                        lineTo(x.toFloat(), y)
                                    }
                                }

                                val fillPath = Path().apply {
                                    addPath(wavePath)
                                    lineTo(w, h)
                                    lineTo(0f, h)
                                    close()
                                }

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(warningColor.copy(alpha = 0.35f), Color.Transparent),
                                        startY = midY - amplitude,
                                        endY = h
                                    )
                                )
                                drawPath(
                                    path = wavePath,
                                    color = warningColor,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                }

                // WIERZ 2: Czas i Punkty
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Czas sesji", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(secondsElapsed.formatSeconds(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("VoltPoints", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+$earnedPoints", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                        }
                    }
                }
            }

            // REGULACJA OPORU
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Opór urządzenia",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Poziom $resistanceLevel z 10",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        IconButton(
                            onClick = { if (resistanceLevel > 1) resistanceLevel-- },
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zmniejsz", modifier = Modifier.size(22.dp))
                        }
                        IconButton(
                            onClick = { if (resistanceLevel < 10) resistanceLevel++ },
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zwiększ", modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PRZYCISKI AKCJI (Usunięto wyspę, wrzucone natywnie do scrolla)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { isPaused = !isPaused },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPaused) "Wznów" else "Pauza",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Button(
                        onClick = { showCompletionModal = true },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Zakończ sesję",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Całkowicie czerwony, mocny przycisk anulowania
                Button(
                    onClick = { showCancelConfirmModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Anuluj trening bez zapisu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
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

        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Podsumowanie Sesji", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Świetna robota! Wygenerowana przez Ciebie energia właśnie zasiliła miejską sieć w Łodzi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Wygenerowano:", style = MaterialTheme.typography.bodyMedium)
                                Text("${totalGeneratedWh.formatOneDecimal()} Wh", fontWeight = FontWeight.ExtraBold, color = primaryColor)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zdobyte punkty:", style = MaterialTheme.typography.bodyMedium)
                                Text("+$earnedPoints VoltPoints", fontWeight = FontWeight.ExtraBold, color = warningColor)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Czas sesji:", style = MaterialTheme.typography.bodyMedium)
                                Text(secondsElapsed.formatSeconds(), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Spalone kalorie:", style = MaterialTheme.typography.bodyMedium)
                                Text("$caloriesBurned kcal", fontWeight = FontWeight.Bold)
                            }
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
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                    shape = CircleShape
                ) {
                    Text("Zapisz i odbierz punkty", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        )
    }

    if (showCancelConfirmModal) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmModal = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Anulować trening?", fontWeight = FontWeight.Bold) },
            text = { Text("Cały postęp tej sesji oraz wygenerowana energia zostaną utracone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmModal = false
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Przerwij", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmModal = false }) {
                    Text("Wznów trening", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Preview
@Composable
fun WorkoutScreenPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        WorkoutScreen(
            station = MockData.sampleStations.first(),
            onFinishWorkout = { _, _, _ -> },
            onCancel = {}
        )
    }
}