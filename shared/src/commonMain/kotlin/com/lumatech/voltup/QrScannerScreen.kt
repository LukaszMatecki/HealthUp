package com.lumatech.voltup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
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
    val primaryColor = ElectricVoltGreen

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0D12))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Wstecz",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Skanuj kod QR",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cornerLen = 40.dp.toPx()
                val strokeW = 3.dp.toPx()
                val r = 16.dp.toPx()

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.08f),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 1.dp.toPx())
                )

                val green = primaryColor.copy(alpha = pulseAlpha)

                drawLine(green, Offset(0f, r), Offset(0f, cornerLen), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(green, Offset(0f, 0f + r / 2), Offset(cornerLen, 0f + r / 2), strokeWidth = strokeW, cap = StrokeCap.Round)

                drawLine(green, Offset(size.width, r), Offset(size.width, cornerLen), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(green, Offset(size.width, r / 2), Offset(size.width - cornerLen, r / 2), strokeWidth = strokeW, cap = StrokeCap.Round)

                drawLine(green, Offset(0f, size.height - r), Offset(0f, size.height - cornerLen), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(green, Offset(0f, size.height - r / 2), Offset(cornerLen, size.height - r / 2), strokeWidth = strokeW, cap = StrokeCap.Round)

                drawLine(green, Offset(size.width, size.height - r), Offset(size.width, size.height - cornerLen), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(green, Offset(size.width, size.height - r / 2), Offset(size.width - cornerLen, size.height - r / 2), strokeWidth = strokeW, cap = StrokeCap.Round)

                val scanY = size.height * 0.1f + (size.height * 0.8f) * scanLineProgress
                drawLine(
                    color = primaryColor.copy(alpha = 0.8f),
                    start = Offset(20.dp.toPx(), scanY),
                    end = Offset(size.width - 20.dp.toPx(), scanY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Skieruj kamere na kod QR urzadzenia",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = manualCode,
            onValueChange = { manualCode = it },
            placeholder = {
                Text("Wpisz kod recznie", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(14.dp),
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
            trailingIcon = {
                if (manualCode.isNotBlank()) {
                    IconButton(onClick = {
                        val found = MockData.sampleStations.find {
                            it.qrCodeId.equals(manualCode.trim(), ignoreCase = true)
                        } ?: MockData.sampleStations.first()
                        onStationDetected(found)
                    }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = primaryColor
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = {
                onStationDetected(MockData.sampleStations.first())
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Pomin",
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium,
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
