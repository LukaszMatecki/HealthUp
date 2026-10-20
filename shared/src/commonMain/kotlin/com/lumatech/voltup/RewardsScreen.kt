package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

class TicketShape(
    private val cornerRadius: Float = 32f,
    private val cutoutRadius: Float = 24f,
    private val splitRatio: Float = 0.7f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val splitX = size.width * splitRatio

            moveTo(0f, cornerRadius)
            arcTo(
                rect = Rect(0f, 0f, 2 * cornerRadius, 2 * cornerRadius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(splitX - cutoutRadius, 0f)
            arcTo(
                rect = Rect(
                    left = splitX - cutoutRadius,
                    top = -cutoutRadius,
                    right = splitX + cutoutRadius,
                    bottom = cutoutRadius
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(size.width - cornerRadius, 0f)

            arcTo(
                rect = Rect(size.width - 2 * cornerRadius, 0f, size.width, 2 * cornerRadius),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            val rightCutoutY = size.height / 2f
            lineTo(size.width, rightCutoutY - cutoutRadius)
            arcTo(
                rect = Rect(
                    left = size.width - cutoutRadius,
                    top = rightCutoutY - cutoutRadius,
                    right = size.width + cutoutRadius,
                    bottom = rightCutoutY + cutoutRadius
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height - cornerRadius)

            arcTo(
                rect = Rect(size.width - 2 * cornerRadius, size.height - 2 * cornerRadius, size.width, size.height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(splitX + cutoutRadius, size.height)
            arcTo(
                rect = Rect(
                    left = splitX - cutoutRadius,
                    top = size.height - cutoutRadius,
                    right = splitX + cutoutRadius,
                    bottom = size.height + cutoutRadius
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(cornerRadius, size.height)

            arcTo(
                rect = Rect(0f, size.height - 2 * cornerRadius, 2 * cornerRadius, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(0f, cornerRadius)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun CouponCard(
    title: String,
    partner: String,
    subtitle: String,
    mainValue: String,
    valueLabel: String,
    isAffordableOrActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isArchive: Boolean = false,
    isOwned: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val splitRatio = 0.67f
    val isDark = isAppInDarkTheme()

    val backgroundBrush = remember(isArchive, isAffordableOrActive, isOwned, isDark) {
        when {
            isArchive -> {
                if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF13161C), Color(0xFF171B22)),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    )
                }
            }
            isAffordableOrActive -> {
                if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF161A23),
                            Color(0xFF1B2230),
                            Color(0xFF181E2B),
                            Color(0xFF151922)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 300f)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFAFBFC),
                            Color(0xFFFFFFFF)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(450f, 250f)
                    )
                }
            }
            else -> {
                if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF12151B), Color(0xFF161921)),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    )
                }
            }
        }
    }

    val borderBrush = remember(isArchive, isAffordableOrActive, isOwned, isDark) {
        when {
            isArchive -> SolidColor(if (isDark) Color(0xFF94A3B8).copy(alpha = 0.2f) else Color(0xFF94A3B8).copy(alpha = 0.25f))
            isOwned -> {
                if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00C853).copy(alpha = 0.90f),
                            Color(0xFF69F0AE).copy(alpha = 0.65f),
                            Color(0xFF00C853).copy(alpha = 0.95f),
                            Color(0xFFB9F6CA).copy(alpha = 0.55f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 200f)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00A35B).copy(alpha = 0.85f),
                            Color(0xFF2D6A4F).copy(alpha = 0.60f),
                            Color(0xFF00A35B).copy(alpha = 0.90f),
                            Color(0xFF52B788).copy(alpha = 0.75f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 200f)
                    )
                }
            }
            isAffordableOrActive -> {
                if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.65f),
                            Color(0xFF8B5CF6).copy(alpha = 0.40f),
                            Color(0xFF00E5FF).copy(alpha = 0.70f),
                            Color(0xFF38BDF8).copy(alpha = 0.35f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 200f)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00A35B).copy(alpha = 0.60f),
                            Color(0xFF52B788).copy(alpha = 0.35f),
                            Color(0xFF00A35B).copy(alpha = 0.70f),
                            Color(0xFF74C69D).copy(alpha = 0.45f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 200f)
                    )
                }
            }
            else -> SolidColor(if (isDark) Color(0xFF94A3B8).copy(alpha = 0.25f) else Color(0xFFE2E8F0))
        }
    }

    val textColor = if (isArchive) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        else if (isAffordableOrActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    val textLightColor = if (isArchive) (if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8))
        else if (isAffordableOrActive) MaterialTheme.colorScheme.onSurfaceVariant
        else (if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF94A3B8))

    val ticketShape = remember { TicketShape(splitRatio = splitRatio) }

    val shouldShine = isAffordableOrActive && !isArchive
    val infiniteTransition = rememberInfiniteTransition(label = "couponSheenTransition")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1.0f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "couponSheenProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 4.dp)
            .graphicsLayer {
                shape = ticketShape
                clip = true
            }
            .shadow(
                elevation = if (isArchive) 0.dp else if (isOwned) 4.5.dp else if (isAffordableOrActive) 2.5.dp else 1.dp,
                shape = ticketShape,
                spotColor = if (isOwned) (if (isDark) Color(0xFF00C853).copy(alpha = 0.35f) else Color(0xFF00A35B).copy(alpha = 0.20f))
                    else if (isAffordableOrActive && !isArchive) (if (isDark) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.14f))
                    else Color.Transparent,
                ambientColor = if (!isDark && !isArchive) Color(0xFF000000).copy(alpha = 0.04f) else Color.Transparent
            )
            .background(backgroundBrush)
            .border(
                width = if (isOwned && !isArchive) 1.8.dp else if (isAffordableOrActive && !isArchive) 1.4.dp else 1.dp,
                brush = borderBrush,
                shape = ticketShape
            )
            .drawWithContent {
                drawContent()
                if (shouldShine) {
                    val width = size.width
                    val height = size.height
                    val sheenX = width * sheenProgress
                    val sheenBrush = Brush.linearGradient(
                        colors = if (isDark) listOf(
                            Color.Transparent,
                            Color(0x0600E5FF),
                            Color(0x1CE0E7FF),
                            Color(0x08A78BFA),
                            Color.Transparent
                        ) else listOf(
                            Color.Transparent,
                            Color(0x0D64748B),
                            Color(0x2864748B),
                            Color(0x0D64748B),
                            Color.Transparent
                        ),
                        start = Offset(sheenX - width * 0.35f, 0f),
                        end = Offset(sheenX + width * 0.35f, height)
                    )
                    drawRect(brush = sheenBrush)
                }
            }
            .clickable(enabled = (isAffordableOrActive && !isArchive), onClick = onClick)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val splitX = size.width * splitRatio
            drawLine(
                color = if (isDark) textLightColor.copy(alpha = 0.25f)
                    else if (isAffordableOrActive && !isArchive) Color(0xFF00A35B).copy(alpha = 0.25f)
                    else Color(0xFFCBD5E1),
                start = Offset(splitX, 16.dp.toPx()),
                end = Offset(splitX, size.height - 16.dp.toPx()),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(splitRatio)
                    .padding(start = 18.dp, end = 10.dp, top = 14.dp, bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (isArchive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else if (isOwned) (if (isDark) Color(0xFF00C853).copy(alpha = 0.20f) else Color(0xFF00A35B).copy(alpha = 0.15f))
                                else if (isAffordableOrActive) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isArchive) textLightColor
                                else if (isOwned) (if (isDark) Color(0xFF00C853) else Color(0xFF00A35B))
                                else if (isAffordableOrActive) MaterialTheme.colorScheme.primary
                                else textLightColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = partner.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = textLightColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = textLightColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = if (isArchive) 12.dp else 8.dp),
                verticalArrangement = if (isArchive) Arrangement.SpaceBetween else Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isArchive) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = if (valueLabel.isNotBlank()) valueLabel else "UŻYTY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textLightColor,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                } else if (mainValue.isNotEmpty()) {
                    Text(
                        text = mainValue,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isOwned) (if (isDark) Color(0xFF00C853) else Color(0xFF00A35B))
                            else if (isAffordableOrActive) MaterialTheme.colorScheme.primary
                            else textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (valueLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOwned) (if (isDark) Color(0xFF00C853).copy(alpha = 0.20f) else Color(0xFF00A35B).copy(alpha = 0.15f))
                                else if (isAffordableOrActive) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                        ) {
                            Text(
                                text = valueLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOwned) (if (isDark) Color(0xFF00C853) else Color(0xFF00A35B))
                                    else if (isAffordableOrActive) MaterialTheme.colorScheme.primary
                                    else textLightColor,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class CouponTabFilter {
    ACTIVE,
    ARCHIVE
}

@Composable
fun ConfirmTicketActivationDialog(
    ticket: RedeemedReward,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = strings
    val isEnglish = strings is VoltUpEnglishStrings

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(horizontal = 4.dp)
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
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Activate Ticket?" else "Czy na pewno chcesz aktywować bilet?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${ticket.reward.partner} • ${ticket.validityMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = if (isEnglish)
                        "After confirmation, you can validate your ticket by scanning the QR code inside the MPK vehicle."
                    else
                        "Po potwierdzeniu przejdziesz do kasowania biletu poprzez zeskanowanie kodu QR w pojeździe MPK.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape
                    ) {
                        Text(
                            text = if (isEnglish) "Yes, activate" else "Tak, aktywuj",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(strings.cancel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleValidationPromptDialog(
    ticket: RedeemedReward,
    onValidateTicket: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = strings
    val isEnglish = strings is VoltUpEnglishStrings

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(horizontal = 4.dp)
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
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Validate Ticket" else "Skasuj bilet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${ticket.reward.partner} • ${ticket.validityMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = if (isEnglish)
                        "Point your camera at the QR code sticker located inside the vehicle to validate your ticket."
                    else
                        "Skieruj aparat na naklejkę z kodem QR wewnątrz pojazdu MPK, aby skasować bilet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onValidateTicket,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnglish) "Validate ticket" else "Skasuj bilet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(strings.cancel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleScannerFullscreenDialog(
    ticket: RedeemedReward,
    onCodeScanned: () -> Unit,
    onTimeout: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = strings
    val isEnglish = strings is VoltUpEnglishStrings
    var secondsLeft by remember { mutableIntStateOf(30) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000.milliseconds)
            secondsLeft--
        }
        onTimeout()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ScannerLaser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserPosition"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isEnglish) "Vehicle QR Scanner" else "Skaner kodu w pojeździe",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (secondsLeft <= 8) Color(0xFFFF5252).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (secondsLeft <= 8) Color(0xFFFF5252).copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (secondsLeft <= 8) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isEnglish) "Time remaining: ${secondsLeft}s" else "Czas na zeskanowanie: ${secondsLeft}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (secondsLeft <= 8) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onCodeScanned() },
                    contentAlignment = Alignment.Center
                ) {
                    CameraPreview(
                        onQrCodeScanned = { scannedCode ->
                            if (scannedCode.isNotBlank()) {
                                onCodeScanned()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (laserY * 310).dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, MaterialTheme.colorScheme.primary, Color.Transparent)
                                    )
                                )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "MPK ŁÓDŹ • POJAZD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Text(
                    text = if (isEnglish)
                        "Align the camera with any MPK Łódź QR sticker inside the vehicle."
                    else
                        "Skieruj obiektyw na dowolną naklejkę z kodem QR wewnątrz pojazdu MPK Łódź.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(strings.cancel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ValidationTimeoutDialog(
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = strings
    val isEnglish = strings is VoltUpEnglishStrings
    val errorColor = MaterialTheme.colorScheme.error

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(horizontal = 4.dp)
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
                        .background(errorColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TimerOff,
                        contentDescription = null,
                        tint = errorColor,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = if (isEnglish) "Scan Unsuccessful" else "Skanowanie nie powiodło się",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isEnglish)
                        "The 30-second time limit to scan the vehicle QR code has expired. Ticket was not validated."
                    else
                        "Upłynął limit 30 sekund na zeskanowanie kodu QR w pojeździe MPK. Bilet nie został skasowany.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEnglish) "Try again" else "Spróbuj jeszcze raz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = if (isEnglish) "Back" else "Powrót",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicketValidationSuccessDialog(
    ticket: RedeemedReward,
    onDismiss: () -> Unit
) {
    val strings = strings
    val isEnglish = strings is VoltUpEnglishStrings

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(horizontal = 4.dp)
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
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = if (isEnglish) "Ticket validated successfully!" else "Bilet został pomyślnie skasowany!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isEnglish)
                        "Your ${ticket.validityMinutes}-minute ticket is now active. The countdown has started!"
                    else
                        "Twój bilet ${ticket.validityMinutes}-minutowy jest teraz aktywny. Czas ważności został pomyślnie uruchomiony!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (isEnglish) "Confirm" else "Potwierdzam",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RewardsScreen(
    currentPoints: Int,
    redeemedRewards: List<RedeemedReward>,
    onRedeemReward: (Reward) -> Unit,
    onActivateReward: (RedeemedReward) -> Unit = {},
    onDeleteCoupon: (RedeemedReward) -> Unit = {},
    onSetCouponReadyToScan: (RedeemedReward) -> Unit = {},
    onTriggerAchievement: ((String) -> Unit)? = null
) {
    var activeSubTab by remember { mutableIntStateOf(0) }
    var couponFilter by remember { mutableStateOf(CouponTabFilter.ACTIVE) }
    var selectedCategory by remember { mutableStateOf(RewardCategory.ALL) }
    var selectedRewardDetail by remember { mutableStateOf<Reward?>(null) }
    var viewingCouponCodeModal by remember { mutableStateOf<RedeemedReward?>(null) }
    var confirmActivationTicket by remember { mutableStateOf<RedeemedReward?>(null) }
    var promptValidationTicket by remember { mutableStateOf<RedeemedReward?>(null) }
    var scannerVehicleTicket by remember { mutableStateOf<RedeemedReward?>(null) }
    var timeoutValidationTicket by remember { mutableStateOf<RedeemedReward?>(null) }
    var successValidationTicket by remember { mutableStateOf<RedeemedReward?>(null) }
    var showClearArchiveConfirmDialog by remember { mutableStateOf(false) }
    var isViewingOwnedReward by remember { mutableStateOf(false) }
    var nowMs by remember { mutableLongStateOf(currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000.milliseconds)
            nowMs = currentTimeMillis()
        }
    }
    val strings = strings
    val isEnglish = strings is VoltUpEnglishStrings

    val filteredRewards = remember(selectedCategory) {
        MockData.sampleRewards.filter { reward ->
            selectedCategory == RewardCategory.ALL || reward.category == selectedCategory
        }
    }

    val activeCoupons = remember(redeemedRewards, nowMs) {
        redeemedRewards.filter { item ->
            val isTimed = item.reward.category == RewardCategory.TRANSPORT && item.validityMinutes > 0
            if (!isTimed) true
            else {
                if (!item.isActivated) true
                else if (item.activatedTimestamp > 0L) {
                    val elapsedSec = (nowMs - item.activatedTimestamp) / 1000L
                    (item.validityMinutes * 60L - elapsedSec) > 0L
                } else false
            }
        }
    }
    val archiveCoupons = remember(redeemedRewards, nowMs) {
        redeemedRewards.filter { item ->
            val isTimed = item.reward.category == RewardCategory.TRANSPORT && item.validityMinutes > 0
            if (!isTimed) false
            else {
                item.isActivated && (item.activatedTimestamp <= 0L || ((nowMs - item.activatedTimestamp) / 1000L >= item.validityMinutes * 60L))
            }
        }
    }

    val isDark = isAppInDarkTheme()
    val primaryColor = if (isDark) Color(0xFF00C853) else MaterialTheme.colorScheme.primary

    val pagerState = rememberPagerState(initialPage = activeSubTab) { 2 }

    LaunchedEffect(activeSubTab) {
        if (pagerState.currentPage != activeSubTab) {
            pagerState.animateScrollToPage(activeSubTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (activeSubTab != pagerState.currentPage) {
            activeSubTab = pagerState.currentPage
        }
    }

    AnimatedContent(
        targetState = selectedRewardDetail,
        transitionSpec = {
            if (targetState != null) {
                (slideInVertically(tween(350, easing = FastOutSlowInEasing)) { it / 3 } + fadeIn(tween(250)) + scaleIn(tween(350), initialScale = 0.94f))
                    .togetherWith(fadeOut(tween(150)) + scaleOut(tween(200), targetScale = 0.96f))
            } else {
                (fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.96f))
                    .togetherWith(slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it / 3 } + fadeOut(tween(200)) + scaleOut(tween(300), targetScale = 0.94f))
            }
        }, label = "RewardDetailAnimation"
    ) { detailReward ->
        if (detailReward != null) {
            RewardDetailScreen(
                reward = detailReward,
                currentPoints = currentPoints,
                isOwned = isViewingOwnedReward,
                onRedeem = {
                    onRedeemReward(detailReward)
                    selectedRewardDetail = null
                    if (!detailReward.id.contains("RANK")) {
                        activeSubTab = 1
                        SnackbarHelper.show(if (isEnglish) "Coupon added to My Coupons tab." else "Kupon dodany do zakładki Moje Kody.")
                    }
                },
                onBack = { selectedRewardDetail = null }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${strings.tabRewards} ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Volt",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Up",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    val orangeColor = Color(0xFFFF9800)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = orangeColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = orangeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = currentPoints.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = orangeColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    listOf(strings.catalogTab to 0, strings.myCouponsTab to 1).forEach { (label, idx) ->
                        val isActive = activeSubTab == idx
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeSubTab = idx }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (idx == 1 && redeemedRewards.isNotEmpty()) "$label (${redeemedRewards.size})" else label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(3.dp)
                                    .background(
                                        if (isActive) primaryColor else Color.Transparent,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    SleekRewardFilterChip(
                                        text = strings.categoryAll,
                                        isSelected = selectedCategory == RewardCategory.ALL,
                                        onClick = { selectedCategory = RewardCategory.ALL }
                                    )
                                }
                                items(RewardCategory.entries.filter { it != RewardCategory.ALL }) { category ->
                                    SleekRewardFilterChip(
                                        text = category.getDisplayName(strings),
                                        isSelected = selectedCategory == category,
                                        onClick = { selectedCategory = category }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredRewards, key = { it.id }) { reward ->
                                    val canAfford = currentPoints >= reward.costPoints
                                    val isEnglish = strings is VoltUpEnglishStrings
                                    CouponCard(
                                        title = reward.getLocalizedTitle(isEnglish),
                                        partner = reward.partner,
                                        subtitle = "",
                                        mainValue = "${reward.costPoints}",
                                        valueLabel = if (isEnglish) "PTS" else "PKT",
                                        isAffordableOrActive = canAfford,
                                        icon = reward.icon,
                                        onClick = {
                                            selectedRewardDetail = reward
                                            isViewingOwnedReward = false
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(100.dp)) }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val greenPrimary = if (isDark) Color(0xFF00C853) else Color(0xFF00A35B)
                                        listOf(
                                            CouponTabFilter.ACTIVE to "${strings.tabActiveCoupons} (${activeCoupons.size})",
                                            CouponTabFilter.ARCHIVE to "${strings.tabArchiveCoupons} (${archiveCoupons.size})"
                                        ).forEach { (filter, label) ->
                                            val isSelected = couponFilter == filter
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(if (isSelected) greenPrimary else Color.Transparent)
                                                    .clickable {
                                                        couponFilter = filter
                                                        if (filter == CouponTabFilter.ARCHIVE) {
                                                            onTriggerAchievement?.invoke("ach_archive_viewed")
                                                        }
                                                    }
                                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            AnimatedContent(
                                targetState = couponFilter,
                                transitionSpec = {
                                    (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { if (targetState == CouponTabFilter.ARCHIVE) it / 6 else -it / 6 })
                                        .togetherWith(fadeOut(tween(150)) + slideOutHorizontally(tween(200)) { if (targetState == CouponTabFilter.ARCHIVE) -it / 6 else it / 6 })
                                },
                                label = "CouponFilterTransition"
                            ) { activeTabState ->
                                if (activeTabState == CouponTabFilter.ACTIVE) {
                                    if (activeCoupons.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
                                                Text(strings.noActiveCouponsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                Text(strings.noActiveCouponsDesc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(activeCoupons, key = { it.id.ifBlank { "${it.reward.id}_${it.discountCode}" } }) { item ->
                                                val isTimedTicket = item.reward.category == RewardCategory.TRANSPORT && item.validityMinutes > 0
                                                val remainingSeconds = if (!isTimedTicket) 0
                                                    else if (!item.isActivated) item.validityMinutes * 60
                                                    else {
                                                        val elapsedSec = (nowMs - item.activatedTimestamp) / 1000L
                                                        (item.validityMinutes * 60L - elapsedSec).coerceAtLeast(0L).toInt()
                                                    }

                                                val codePrefix = if (isEnglish) "CODE:" else "KOD:"
                                                val cardSubtitle = when {
                                                    !isTimedTicket -> "$codePrefix ${item.discountCode} • ${strings.ticketCodeTapToView}"
                                                    !item.isActivated && !item.isReadyToScan -> if (isEnglish) "Timed ticket • Tap to activate" else "Bilet czasowy • Dotknij, aby aktywować"
                                                    !item.isActivated && item.isReadyToScan -> if (isEnglish) "Ticket ready • Tap to validate" else "Bilet gotowy • Dotknij, aby skasować"
                                                    else -> "$codePrefix ${item.discountCode} • ${strings.ticketCodeTapToView}"
                                                }

                                                val cardMainValue = when {
                                                    !isTimedTicket -> if (item.reward.title.contains("-")) item.reward.title.substringBefore(" ").take(4) else if (isEnglish) "CODE" else "KOD"
                                                    !item.isActivated && !item.isReadyToScan -> if (isEnglish) "ACTIVATE" else "AKTYWUJ"
                                                    !item.isActivated && item.isReadyToScan -> if (isEnglish) "VALIDATE" else "SKASUJ"
                                                    else -> "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"
                                                }

                                                val cardValueLabel = when {
                                                    !isTimedTicket -> strings.statusActive
                                                    !item.isActivated && !item.isReadyToScan -> if (isEnglish) "TICKET" else "BILET"
                                                    !item.isActivated && item.isReadyToScan -> if (isEnglish) "VEHICLE" else "POJAZD"
                                                    else -> strings.statusRemainingTime
                                                }

                                                val cardIcon = when {
                                                    !isTimedTicket -> Icons.Default.ConfirmationNumber
                                                    !item.isActivated && !item.isReadyToScan -> Icons.Default.ConfirmationNumber
                                                    !item.isActivated && item.isReadyToScan -> Icons.Default.QrCodeScanner
                                                    else -> Icons.Default.QrCode
                                                }

                                                CouponCard(
                                                    title = item.reward.getLocalizedTitle(isEnglish),
                                                    partner = item.reward.partner,
                                                    subtitle = cardSubtitle,
                                                    mainValue = cardMainValue,
                                                    valueLabel = cardValueLabel,
                                                    isAffordableOrActive = true,
                                                    icon = cardIcon,
                                                    isArchive = false,
                                                    isOwned = true,
                                                    onClick = {
                                                        when {
                                                            !isTimedTicket -> {
                                                                viewingCouponCodeModal = item
                                                            }
                                                            !item.isActivated && !item.isReadyToScan -> {
                                                                confirmActivationTicket = item
                                                            }
                                                            !item.isActivated && item.isReadyToScan -> {
                                                                promptValidationTicket = item
                                                            }
                                                            else -> {
                                                                viewingCouponCodeModal = item
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            item { Spacer(modifier = Modifier.height(100.dp)) }
                                        }
                                    }
                                } else {
                                    if (archiveCoupons.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Icon(Icons.Default.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
                                                Text(strings.noArchiveCouponsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                Text(strings.noArchiveCouponsDesc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                            }
                                        }
                                    } else {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 24.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (isEnglish) "Archived tickets (${archiveCoupons.size})" else "Bilety w archiwum (${archiveCoupons.size})",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                TextButton(
                                                    onClick = { showClearArchiveConfirmDialog = true },
                                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (isEnglish) "Clear all" else "Wyczyść wszystko",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }

                                            LazyColumn(
                                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                items(archiveCoupons, key = { it.id.ifBlank { "${it.reward.id}_${it.discountCode}" } }) { item ->
                                                    CouponCard(
                                                        title = item.reward.getLocalizedTitle(isEnglish),
                                                        partner = item.reward.partner,
                                                        subtitle = strings.statusExpired,
                                                        mainValue = "",
                                                        valueLabel = if (isEnglish) "USED" else "UŻYTY",
                                                        isAffordableOrActive = false,
                                                        icon = item.reward.icon,
                                                        isArchive = true,
                                                        isOwned = true,
                                                        onDelete = { onDeleteCoupon(item) },
                                                        onClick = {}
                                                    )
                                                }
                                                item { Spacer(modifier = Modifier.height(100.dp)) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            confirmActivationTicket?.let { ticket ->
                ConfirmTicketActivationDialog(
                    ticket = ticket,
                    onConfirm = {
                        confirmActivationTicket = null
                        onSetCouponReadyToScan(ticket)
                        promptValidationTicket = ticket.copy(isReadyToScan = true)
                    },
                    onDismiss = { confirmActivationTicket = null }
                )
            }

            promptValidationTicket?.let { ticket ->
                VehicleValidationPromptDialog(
                    ticket = ticket,
                    onValidateTicket = {
                        promptValidationTicket = null
                        scannerVehicleTicket = ticket
                    },
                    onDismiss = { promptValidationTicket = null }
                )
            }

            scannerVehicleTicket?.let { ticket ->
                VehicleScannerFullscreenDialog(
                    ticket = ticket,
                    onCodeScanned = {
                        scannerVehicleTicket = null
                        successValidationTicket = ticket
                    },
                    onTimeout = {
                        scannerVehicleTicket = null
                        timeoutValidationTicket = ticket
                    },
                    onDismiss = { scannerVehicleTicket = null }
                )
            }

            timeoutValidationTicket?.let { ticket ->
                ValidationTimeoutDialog(
                    onRetry = {
                        timeoutValidationTicket = null
                        scannerVehicleTicket = ticket
                    },
                    onDismiss = { timeoutValidationTicket = null }
                )
            }

            successValidationTicket?.let { ticket ->
                TicketValidationSuccessDialog(
                    ticket = ticket,
                    onDismiss = {
                        successValidationTicket = null
                        onActivateReward(ticket)
                        SnackbarHelper.show(strings.ticketValidatedSuccess, icon = Icons.Default.CheckCircle)
                    }
                )
            }

            viewingCouponCodeModal?.let { coupon ->
                Dialog(
                    onDismissRequest = { viewingCouponCodeModal = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = coupon.reward.partner.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = coupon.reward.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        text = coupon.discountCode,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Text(
                                text = strings.showCodeToCashier,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewingCouponCodeModal = null },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = CircleShape
                            ) {
                                Text(strings.close, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            if (showClearArchiveConfirmDialog) {
                val errorColor = MaterialTheme.colorScheme.error
                Dialog(
                    onDismissRequest = { showClearArchiveConfirmDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(horizontal = 4.dp)
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
                                    .background(errorColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = errorColor,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            Text(
                                text = if (isEnglish) "Clear Archive?" else "Wyczyścić archiwum?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (isEnglish)
                                    "All ${archiveCoupons.size} archived tickets will be permanently removed."
                                else
                                    "Wszystkie zarchiwizowane bilety (${archiveCoupons.size}) zostaną trwale usunięte.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val toDelete = archiveCoupons.toList()
                                        showClearArchiveConfirmDialog = false
                                        toDelete.forEach { onDeleteCoupon(it) }
                                        SnackbarHelper.show(
                                            if (isEnglish) "Archive cleared." else "Archiwum zostało wyczyszczone.",
                                            icon = Icons.Default.DeleteSweep
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = errorColor,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (isEnglish) "Delete all" else "Usuń wszystkie",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Button(
                                    onClick = { showClearArchiveConfirmDialog = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(strings.cancel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RewardDetailScreen(reward: Reward, currentPoints: Int, isOwned: Boolean, onRedeem: () -> Unit, onBack: () -> Unit) {
    val canAfford = currentPoints >= reward.costPoints
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(strings.couponDetailsTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            val isEnglish = strings is VoltUpEnglishStrings
            Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(primaryColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(reward.icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(32.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(reward.partner.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Text(reward.getLocalizedTitle(isEnglish), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(reward.getLocalizedDescription(isEnglish), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.costLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${reward.costPoints} VoltPoints", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.validityLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val validityStr = when {
                        reward.category == RewardCategory.TRANSPORT && reward.validityMinutes > 0 -> strings.minutesValidity(reward.validityMinutes)
                        reward.id.contains("PTS") || reward.id.contains("RANK") -> strings.instantValidity
                        reward.validityMinutes > 0 -> strings.minutesValidity(reward.validityMinutes)
                        else -> strings.daysValidity(30)
                    }
                    Text(validityStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = if (isOwned) onBack else onRedeem,
            enabled = isOwned || canAfford,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            shape = CircleShape
        ) {
            Text(
                text = if (isOwned) strings.closeDetailsBtn else if (canAfford) strings.redeemForPoints(reward.costPoints) else strings.notEnoughPoints,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SleekRewardFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun RewardsScreenLightPreview() {
    VoltUpTheme(AppThemeMode.LIGHT) {
        Surface(color = MaterialTheme.colorScheme.background) {
            RewardsScreen(
                currentPoints = 3500,
                redeemedRewards = emptyList(),
                onRedeemReward = {}
            )
        }
    }
}

@Preview
@Composable
fun RewardsScreenDarkPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        Surface(color = MaterialTheme.colorScheme.background) {
            RewardsScreen(
                currentPoints = 3500,
                redeemedRewards = listOf(
                    RedeemedReward(
                        reward = MockData.sampleRewards.first(),
                        discountCode = "MPK-KINETIC-20",
                        redeemedDate = "Dzisiaj",
                        isActivated = true,
                        validityMinutes = 20
                    )
                ),
                onRedeemReward = {}
            )
        }
    }
}

@Preview
@Composable
fun CouponCardPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            CouponCard(
                title = "Darmowy Bilet 20-minutowy",
                partner = "MPK Łódź",
                subtitle = "Tramwaje i autobusy w strefie 1 Łodzi",
                mainValue = "1500",
                valueLabel = "PKT",
                isAffordableOrActive = true,
                icon = Icons.Default.DirectionsBus,
                onClick = {}
            )
        }
    }
}