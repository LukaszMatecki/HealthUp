package com.lumatech.voltup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview

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
    var currentRpm by remember { mutableStateOf(60) }
    var totalGeneratedWh by remember { mutableStateOf(0.0) }
    var showCompletionModal by remember { mutableStateOf(false) }
    var showCancelConfirmModal by remember { mutableStateOf(false) }

    // Intercept back button
    CommonBackHandler(enabled = true) {
        showCancelConfirmModal = true
    }

    // Engine loop
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
    val progress = (totalGeneratedWh / targetWh.toDouble()).toFloat().coerceIn(0f, 1f)

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = station.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPaused) EnergyYellow.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPaused) EnergyYellow else primaryColor)
                        )
                        Text(
                            text = if (isPaused) "WSTRZYMANO" else "AKTYWNY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaused) EnergyYellow else primaryColor
                        )
                    }
                }
            }

            // Power Gauge (Minimalist Arc)
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background Track
                    drawCircle(
                        color = Color(0xFF232A34),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active Arc
                    val sweepAngle = ((currentWatts.toFloat() / 150f) * 280f).coerceIn(10f, 280f)
                    drawArc(
                        color = if (isPaused) EnergyYellow else primaryColor,
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
                        fontSize = if (isPaused) 32.sp else 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaused) EnergyYellow else primaryColor,
                        maxLines = 1
                    )
                    Text(
                        text = if (isPaused) "Trening wstrzymany" else "WATY (MOC)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isPaused) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currentRpm RPM",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stats Row (Clean 4 metrics)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WorkoutMetricItem(label = "Czas", value = secondsElapsed.formatSeconds())
                WorkoutMetricItem(label = "Energia", value = "${totalGeneratedWh.formatOneDecimal()} Wh", highlight = true)
                WorkoutMetricItem(label = "Kalorie", value = "$caloriesBurned kcal")
                WorkoutMetricItem(label = "Punkty", value = "+$earnedPoints")
            }

            // Resistance Adjuster (Minimalist)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Opór urządzenia",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Poziom $resistanceLevel z 10",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = { if (resistanceLevel > 1) resistanceLevel-- },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zmniejsz opór", modifier = Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(
                        onClick = { if (resistanceLevel < 10) resistanceLevel++ },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zwiększ opór", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Bottom Actions
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
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPaused) "Wznów" else "Pauza", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showCompletionModal = true },
                        modifier = Modifier.weight(1.4f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Zakończ sesję ⚡", fontWeight = FontWeight.Bold)
                    }
                }

                // Anuluj with a clear surface background as requested!
                Button(
                    onClick = { showCancelConfirmModal = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Anuluj bez zapisu", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // Modal zakończenia treningu
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
            title = {
                Text("Podsumowanie Treningu", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Świetna robota! Wygenerowana energia zasiliła miejską sieć w Łodzi.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Wygenerowano:", style = MaterialTheme.typography.bodySmall)
                                Text("${totalGeneratedWh.formatOneDecimal()} Wh", fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zdobyte punkty:", style = MaterialTheme.typography.bodySmall)
                                Text("+$earnedPoints VoltPoints", fontWeight = FontWeight.Bold, color = EnergyYellow)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Czas sesji:", style = MaterialTheme.typography.bodySmall)
                                Text(secondsElapsed.formatSeconds(), fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Spalone kalorie:", style = MaterialTheme.typography.bodySmall)
                                Text("$caloriesBurned kcal", fontWeight = FontWeight.Medium)
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
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zapisz i Odbierz Punkty", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal potwierdzenia anulowania
    if (showCancelConfirmModal) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmModal = false },
            title = { Text("Anulować trening?") },
            text = { Text("Postęp tej sesji nie zostanie zapisany.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmModal = false
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Anuluj trening")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmModal = false }) {
                    Text("Kontynuuj")
                }
            }
        )
    }
}

@Composable
fun WorkoutMetricItem(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
