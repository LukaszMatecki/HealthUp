package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

enum class SnackbarType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}

data class SnackbarMessage(
    val id: Long,
    val text: String,
    val type: SnackbarType,
)

object SnackbarHelper {
    private val _message = mutableStateOf<SnackbarMessage?>(null)
    val message: State<SnackbarMessage?> = _message
    private var messageIdCounter = 0L

    fun show(text: String, type: SnackbarType = SnackbarType.SUCCESS) {
        _message.value = SnackbarMessage(++messageIdCounter, text, type)
    }

    fun dismiss() {
        _message.value = null
    }
}

@Composable
fun VoltUpSnackbarHost(
    modifier: Modifier = Modifier,
) {
    val currentMsg = SnackbarHelper.message.value

    LaunchedEffect(currentMsg?.id) {
        if (currentMsg != null) {
            delay(3500.milliseconds)
            if (SnackbarHelper.message.value?.id == currentMsg.id) {
                SnackbarHelper.dismiss()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = currentMsg != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(250))
        ) {
            if (currentMsg != null) {
                val accentColor = when (currentMsg.type) {
                    SnackbarType.SUCCESS -> ElectricVoltGreen
                    SnackbarType.INFO -> MaterialTheme.colorScheme.secondary
                    SnackbarType.WARNING -> EnergyYellow
                    SnackbarType.ERROR -> MaterialTheme.colorScheme.error
                }

                val icon = when (currentMsg.type) {
                    SnackbarType.SUCCESS -> Icons.Default.CheckCircle
                    SnackbarType.INFO -> Icons.Default.Info
                    SnackbarType.WARNING -> Icons.Default.Warning
                    SnackbarType.ERROR -> Icons.Default.Error
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Kolorowy pasek akcentowy na krawędzi bez ramki
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = currentMsg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { SnackbarHelper.dismiss() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zamknij",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
