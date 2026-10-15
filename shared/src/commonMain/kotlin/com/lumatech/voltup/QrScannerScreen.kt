package com.lumatech.voltup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun QrScannerScreen(
    onStationDetected: (KineticStation) -> Unit,
    onDismiss: () -> Unit
) {
    var manualCode by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    // Animacja lasera skanującego (góra - dół)
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    // Pulsowanie ramki skanera
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115)) // Głęboki, nowoczesny ciemny odcień
            .statusBarsPadding()
    ) {
        // Minimalistyczny nagłówek
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Wróć",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Skanuj QR",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.weight(0.2f))

        // Wizjer Skanera z nowoczesną grafiką i symulacją lasera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            // Ciemniejsze tło wewnątrz wizjera imitujące przyciemnienie reszty ekranu
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val len = 50.dp.toPx()      // Długość linii narożnika
                val strokeW = 4.dp.toPx()   // Grubość obramowania
                val r = 24.dp.toPx()        // Promień zaokrąglenia

                // Delikatna pełna ramka w tle
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.05f),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 1.dp.toPx())
                )

                val activeColor = primaryColor.copy(alpha = pulseAlpha)
                val path = Path()

                // Lewy górny róg
                path.moveTo(0f, len)
                path.lineTo(0f, r)
                path.arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
                path.lineTo(len, 0f)

                // Prawy górny róg
                path.moveTo(w - len, 0f)
                path.lineTo(w - r, 0f)
                path.arcTo(Rect(w - 2 * r, 0f, w, 2 * r), 270f, 90f, false)
                path.lineTo(w, len)

                // Prawy dolny róg
                path.moveTo(w, h - len)
                path.lineTo(w, h - r)
                path.arcTo(Rect(w - 2 * r, h - 2 * r, w, h), 0f, 90f, false)
                path.lineTo(w - len, h)

                // Lewy dolny róg
                path.moveTo(len, h)
                path.lineTo(r, h)
                path.arcTo(Rect(0f, h - 2 * r, 2 * r, h), 90f, 90f, false)
                path.lineTo(0f, h - len)

                // Rysowanie zaokrąglonych, pogrubionych narożników
                drawPath(
                    path = path,
                    color = activeColor,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Pozycja linii skanującej z marginesem góra/dół
                val scanY = (h * 0.1f) + ((h * 0.8f) * scanLineProgress)

                // Efekt jarzenia (glow) wokół lasera
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        startY = scanY - 30f,
                        endY = scanY + 30f
                    ),
                    topLeft = Offset(0f, scanY - 30f),
                    size = Size(w, 60f)
                )

                // Główna linia lasera
                drawLine(
                    color = primaryColor,
                    start = Offset(0f, scanY),
                    end = Offset(w, scanY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Skieruj aparat na kod QR na urządzeniu, aby rozpocząć trening.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Pole wpisywania kodu - kształt pigułki (Clean UI)
        OutlinedTextField(
            value = manualCode,
            onValueChange = { manualCode = it },
            placeholder = {
                Text("Wpisz kod ręcznie", fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = CircleShape,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                cursorColor = primaryColor
            ),
            leadingIcon = {
                Icon(
                    Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (manualCode.isNotBlank()) {
                    IconButton(onClick = {
                        val found = MockData.sampleStations.find {
                            it.qrCodeId.equals(manualCode.trim(), ignoreCase = true)
                        } ?: MockData.sampleStations.first()
                        onStationDetected(found)
                    }) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Zatwierdź",
                            tint = primaryColor
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Przycisk pomijania
        TextButton(
            onClick = { onStationDetected(MockData.sampleStations.first()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Symuluj skanowanie (Test) →",
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Preview
@Composable
fun QrScannerScreenPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        QrScannerScreen(
            onStationDetected = {},
            onDismiss = {}
        )
    }
}