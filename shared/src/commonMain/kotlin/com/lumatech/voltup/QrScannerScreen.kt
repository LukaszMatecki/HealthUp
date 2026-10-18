package com.lumatech.voltup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CameraPreview(
            onQrCodeScanned = { scannedCode ->
                val station = MockData.sampleStations.find {
                    it.qrCodeId.equals(scannedCode.trim(), ignoreCase = true)
                } ?: MockData.sampleStations.first()
                onStationDetected(station)
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.65f))

            val boxSizePx = 260.dp.toPx()
            val left = (size.width - boxSizePx) / 2f
            val top = (size.height - boxSizePx) / 2.3f
            val cornerRadius = 24.dp.toPx()

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(boxSizePx, boxSizePx),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                blendMode = BlendMode.Clear
            )

            val len = 44.dp.toPx()
            val strokeW = 4.dp.toPx()
            val activeColor = primaryColor.copy(alpha = pulseAlpha)

            val path = Path()

            path.moveTo(left, top + len)
            path.lineTo(left, top + cornerRadius)
            path.arcTo(Rect(left, top, left + 2 * cornerRadius, top + 2 * cornerRadius), 180f, 90f, false)
            path.lineTo(left + len, top)

            path.moveTo(left + boxSizePx - len, top)
            path.lineTo(left + boxSizePx - cornerRadius, top)
            path.arcTo(Rect(left + boxSizePx - 2 * cornerRadius, top, left + boxSizePx, top + 2 * cornerRadius), 270f, 90f, false)
            path.lineTo(left + boxSizePx, top + len)

            path.moveTo(left + boxSizePx, top + boxSizePx - len)
            path.lineTo(left + boxSizePx, top + boxSizePx - cornerRadius)
            path.arcTo(Rect(left + boxSizePx - 2 * cornerRadius, top + boxSizePx - 2 * cornerRadius, left + boxSizePx, top + boxSizePx), 0f, 90f, false)
            path.lineTo(left + boxSizePx - len, top + boxSizePx)

            path.moveTo(left + len, top + boxSizePx)
            path.lineTo(left + cornerRadius, top + boxSizePx)
            path.arcTo(Rect(left, top + boxSizePx - 2 * cornerRadius, left + 2 * cornerRadius, top + boxSizePx), 90f, 90f, false)
            path.lineTo(left, top + boxSizePx - len)

            drawPath(
                path = path,
                color = activeColor,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            val scanY = top + (boxSizePx * 0.1f) + ((boxSizePx * 0.8f) * scanLineProgress)

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryColor.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    startY = scanY - 24f,
                    endY = scanY + 24f
                ),
                topLeft = Offset(left, scanY - 24f),
                size = Size(boxSizePx, 48f)
            )

            drawLine(
                color = primaryColor,
                start = Offset(left, scanY),
                end = Offset(left + boxSizePx, scanY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
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
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = strings.qrScannerTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = strings.qrScannerInstruction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = manualCode,
                onValueChange = { manualCode = it },
                placeholder = {
                    Text(strings.qrManualCodePlaceholder, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
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
                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                    cursorColor = primaryColor
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
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
                                contentDescription = strings.qrSubmitBtn,
                                tint = primaryColor
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onStationDetected(MockData.sampleStations.first()) },
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = strings.qrSimulateBtn,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
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
