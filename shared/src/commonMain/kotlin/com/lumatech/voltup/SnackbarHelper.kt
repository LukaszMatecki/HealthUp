package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val type: SnackbarType
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
    modifier: Modifier = Modifier
) {
    val currentMsg = SnackbarHelper.message.value

    LaunchedEffect(currentMsg?.id) {
        if (currentMsg != null) {
            delay(3200)
            if (SnackbarHelper.message.value?.id == currentMsg.id) {
                SnackbarHelper.dismiss()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = currentMsg != null,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            if (currentMsg != null) {
                val accentColor = when (currentMsg.type) {
                    SnackbarType.SUCCESS -> MaterialTheme.colorScheme.primary
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )

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
