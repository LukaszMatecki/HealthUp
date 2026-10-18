package com.lumatech.voltup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SnackbarType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}

data class SnackbarMessage(
    val id: Long,
    val text: String,
    val type: SnackbarType = SnackbarType.SUCCESS,
    val icon: ImageVector? = null,
)

object SnackbarHelper {
    private val queue = mutableStateListOf<SnackbarMessage>()
    private val _currentMessage = mutableStateOf<SnackbarMessage?>(null)
    val currentMessage: State<SnackbarMessage?> = _currentMessage

    val message: State<SnackbarMessage?> = _currentMessage

    private var messageIdCounter = 0L

    fun show(
        text: String,
        type: SnackbarType = SnackbarType.SUCCESS,
        icon: ImageVector? = null
    ) {
        val msg = SnackbarMessage(++messageIdCounter, text, type, icon)
        if (_currentMessage.value == null) {
            _currentMessage.value = msg
        } else {
            queue.add(msg)
        }
    }

    internal fun onAnimationCompleted() {
        if (queue.isNotEmpty()) {
            _currentMessage.value = queue.removeAt(0)
        } else {
            _currentMessage.value = null
        }
    }

    fun dismiss() {
        onAnimationCompleted()
    }
}

@Composable
fun VoltUpSnackbarHost(
    modifier: Modifier = Modifier,
) {
    val currentMsg = SnackbarHelper.currentMessage.value

    val expansion = remember { Animatable(0f) }
    val offsetY = remember { Animatable(-70f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(currentMsg?.id) {
        if (currentMsg == null) return@LaunchedEffect

        expansion.snapTo(0f)
        offsetY.snapTo(-70f)
        alpha.snapTo(0f)

        coroutineScope {
            launch {
                alpha.animateTo(1f, animationSpec = tween(320, easing = LinearEasing))
            }
            launch {
                offsetY.animateTo(0f, animationSpec = tween(360, easing = FastOutSlowInEasing))
            }
        }

        delay(40.milliseconds)
        expansion.animateTo(1f, animationSpec = tween(380, easing = FastOutSlowInEasing))

        delay(2400.milliseconds)

        expansion.animateTo(0f, animationSpec = tween(340, easing = FastOutSlowInEasing))

        coroutineScope {
            launch {
                alpha.animateTo(0f, animationSpec = tween(280, easing = LinearEasing))
            }
            launch {
                offsetY.animateTo(-70f, animationSpec = tween(300, easing = FastOutLinearInEasing))
            }
        }

        SnackbarHelper.onAnimationCompleted()
    }

    if (currentMsg != null) {
        val accentColor = when (currentMsg.type) {
            SnackbarType.SUCCESS -> LightPrimary
            SnackbarType.INFO -> LightPrimary
            SnackbarType.WARNING -> EnergyYellow
            SnackbarType.ERROR -> MaterialTheme.colorScheme.error
        }

        val displayIcon = currentMsg.icon ?: when (currentMsg.type) {
            SnackbarType.SUCCESS -> Icons.Default.Check
            SnackbarType.INFO -> Icons.Default.Info
            SnackbarType.WARNING -> Icons.Default.Warning
            SnackbarType.ERROR -> Icons.Default.Error
        }

        val baseCircleSize = 48.dp
        val maxPillWidth = 310.dp
        val animatedWidth = baseCircleSize + ((maxPillWidth - baseCircleSize) * expansion.value)
        val dynamicEndPadding = 7.dp + (7.dp * expansion.value)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .offset(y = offsetY.value.dp)
                    .graphicsLayer(alpha = alpha.value)
                    .width(animatedWidth)
                    .height(baseCircleSize)
                    .clip(CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 0.dp,
                border = null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 7.dp, end = dynamicEndPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = displayIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    if (expansion.value > 0.18f) {
                        val textAlpha = ((expansion.value - 0.18f) / 0.82f).coerceIn(0f, 1f)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentMsg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer(alpha = textAlpha)
                        )
                    }
                }
            }
        }
    }
}
