package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StreakPill(streakDays: Int) {
    val isDark = isAppInDarkTheme()
    val containerColor = if (isDark) Color(0xFF4A1200) else Color(0xFFFFEAE0)
    val contentColor = if (isDark) Color(0xFFFF7043) else Color(0xFFD84315)

    Surface(
        shape = CircleShape,
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$streakDays dni",
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ProfileScreen(
    stats: UserStats,
    challenges: List<Challenge>,
    achievements: List<Achievement> = emptyList(),
    onClaimChallenge: (Challenge) -> Unit,
    onUpdateUserProfile: (newName: String, newAvatarId: Int) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAchievements: () -> Unit = {},
    onOpenWorkoutHistory: () -> Unit = {},
    userEmail: String = "",
    authService: VoltUpAuthService? = null,
    onSignOut: () -> Unit = {}
) {
    val strings = strings
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var selectedWorkoutDetail by remember { mutableStateOf<WorkoutSessionRecord?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = isAppInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isEnglish = strings is VoltUpEnglishStrings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isEnglish) "Profile " else "Profil ",
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
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StreakPill(streakDays = stats.streakDays)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(22.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = strings.editProfileBtn,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = strings.settingsMenu,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = { showSignOutDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = strings.signOutMenu,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarIcon = when (stats.avatarId) {
                            1 -> Icons.AutoMirrored.Filled.DirectionsBike
                            2 -> Icons.Default.ElectricBolt
                            3 -> Icons.Default.Eco
                            else -> Icons.Default.Person
                        }
                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = "Avatar",
                            tint = primaryColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stats.userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = strings.levelLabel(stats.level),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }

                        val effectiveLevelPts = stats.rankingPoints
                        val (minPts, maxPts) = getLevelThresholds(stats.level)
                        val currentLvlPts = (effectiveLevelPts - minPts).coerceAtLeast(0)
                        val reqLvlPts = (maxPts - minPts).coerceAtLeast(1)
                        val progressFraction = (currentLvlPts.toFloat() / reqLvlPts.toFloat()).coerceIn(0f, 1f)

                        val animatedProgressFraction by animateFloatAsState(
                            targetValue = progressFraction,
                            animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
                            label = "profileLevelProgress"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedProgressFraction.coerceIn(0.02f, 1f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(primaryColor)
                                )
                            }

                            Text(
                                text = "$effectiveLevelPts/$maxPts ${strings.levelPointsSuffix}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatNumber(
                            value = formatEnergySmart(stats.totalEnergyWh),
                            label = strings.generatedEnergy,
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        ProfileStatNumber(
                            value = formatCo2Smart(stats.co2SavedKg),
                            label = strings.co2Reduction,
                            valueColor = primaryColor
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenAchievements() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val achBgColor = if (isDark) Color(0xFF4A3400) else Color(0xFFFFF3E0)
                        val achIconColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFFF9800)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(achBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = achIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = strings.achievementsScreenTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val unlockedText = if (strings is VoltUpEnglishStrings) "${achievements.count { it.level > 0 }} of ${achievements.size} unlocked" else "${achievements.count { it.level > 0 }} z ${achievements.size} odblokowane"
                            Text(
                                text = unlockedText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenWorkoutHistory() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val histBgColor = if (isDark) Color(0xFF1E3A5F) else Color(0xFFE3F2FD)
                        val histIconColor = if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(histBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = histIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = strings.workoutHistoryTileTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.workoutHistoryTileSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        item {
            Text(
                text = strings.weeklyChallengesSection,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp, start = 8.dp)
            )
        }

        val activeChallenges = challenges.filter { !it.isClaimed }
        if (activeChallenges.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = strings.allWeeklyChallengesCompleted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeChallenges, key = { it.id }) { challenge ->
                AnimatedVisibility(
                    visible = !challenge.isClaimed,
                    exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut() + shrinkVertically()
                ) {
                    ModernChallengeCard(
                        challenge = challenge,
                        onClaimClick = { onClaimChallenge(challenge) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(stats.userName) }
        var editAvatar by remember { mutableStateOf(stats.avatarId) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = strings.editProfileDialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(strings.fullNameLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Text(
                        text = strings.chooseProfileAvatar,
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(
                            0 to Icons.Default.Person,
                            1 to Icons.AutoMirrored.Filled.DirectionsBike,
                            2 to Icons.Default.ElectricBolt,
                            3 to Icons.Default.Eco
                        ).forEach { (id, icon) ->
                            val isSel = editAvatar == id
                            IconButton(
                                onClick = { editAvatar = id },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (editName.isBlank()) {
                                SnackbarHelper.show("Imię nie może być puste.")
                                return@Button
                            }
                            onUpdateUserProfile(editName.trim(), editAvatar)
                            showEditProfileDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = CircleShape
                    ) {
                        Text(strings.saveChanges, fontWeight = FontWeight.ExtraBold)
                    }

                    TextButton(
                        onClick = { showEditProfileDialog = false },
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(strings.cancel, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            dismissButton = null
        )
    }

    if (showSignOutDialog) {
        Dialog(
            onDismissRequest = { showSignOutDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val warnColor = Color(0xFFFFA000)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
                            .background(warnColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = warnColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = strings.signOutConfirmTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = strings.signOutConfirmDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                showSignOutDialog = false
                                onSignOut()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = strings.signOutBtn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Button(
                            onClick = { showSignOutDialog = false },
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
                                text = strings.cancel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    selectedWorkoutDetail?.let { detail ->
        WorkoutDetailsDialog(
            workout = detail,
            onDismiss = { selectedWorkoutDetail = null }
        )
    }
}

@Composable
fun WorkoutDetailsDialog(
    workout: WorkoutSessionRecord,
    onDismiss: () -> Unit
) {
    val strings = strings
    val primaryColor = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = strings.workoutDetailsTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val displayDate = if (workout.dateText.isNotBlank() && workout.dateText != "Ostatnio") {
                                workout.dateText
                            } else if (workout.timestampMs > 0L) {
                                formatSessionDate(workout.timestampMs)
                            } else ""
                            if (displayDate.isNotBlank()) {
                                Text(
                                    text = displayDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.close,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WorkoutDetailRow(label = strings.workoutDetailsDevice, value = workout.stationName)
                    WorkoutDetailRow(label = strings.workoutDetailsPlace, value = workout.stationLocation)
                    WorkoutDetailRow(label = strings.workoutDetailsDuration, value = "${workout.durationSeconds / 60} min ${workout.durationSeconds % 60} s")
                    WorkoutDetailRow(label = strings.workoutDetailsEnergy, value = "${workout.generatedWh.roundToInt()} Wh", valueColor = primaryColor)
                    WorkoutDetailRow(label = strings.workoutDetailsPoints, value = "+${workout.earnedPoints} VP", valueColor = EnergyYellow)
                    WorkoutDetailRow(label = strings.workoutDetailsCalories, value = "${workout.caloriesKcal} kcal")
                    val co2Grams = (workout.co2SavedKg * 1000).roundToInt()
                    WorkoutDetailRow(label = strings.workoutDetailsCo2, value = "$co2Grams g CO₂", valueColor = Color(0xFF10B981))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(strings.close, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WorkoutDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun ModernChallengeCard(
    challenge: Challenge,
    onClaimClick: () -> Unit
) {
    val strings = strings
    val isEnglish = strings is VoltUpEnglishStrings
    var isExpanded by remember { mutableStateOf(false) }
    var isClaiming by remember { mutableStateOf(false) }

    val lineProgress by animateFloatAsState(
        targetValue = if (isClaiming) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "strikethrough"
    )

    LaunchedEffect(isClaiming) {
        if (isClaiming) {
            delay(450.milliseconds)
            onClaimClick()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = isAppInDarkTheme()
    val warningYellow = if (isDark) Color(0xFFFFB74D) else Color(0xFFFF9800)

    val localizedTitle = challenge.getLocalizedTitle(isEnglish)
    val localizedDescription = challenge.getLocalizedDescription(isEnglish)
    val localizedDetails = challenge.getLocalizedDetails(isEnglish)
    val localizedUnit = challenge.getLocalizedUnit(isEnglish)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) strings.collapseDetails else strings.expandDetails,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    Box(contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = localizedTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isClaiming || challenge.isClaimed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (isClaiming || challenge.isClaimed) TextDecoration.LineThrough else TextDecoration.None
                        )

                        if (lineProgress > 0f) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val strokeW = 2.dp.toPx()
                                val startY = size.height / 2f
                                drawLine(
                                    color = warningYellow,
                                    start = Offset(0f, startY),
                                    end = Offset(size.width * lineProgress, startY),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "+${challenge.rankingPointsReward}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = primaryColor
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = warningYellow.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = warningYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "+${challenge.voltPointsReward} VP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = warningYellow
                            )
                        }
                    }
                }
            }

            Text(
                text = localizedDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (isExpanded) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (localizedDetails.isNotBlank()) {
                            Text(
                                text = localizedDetails,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.challengeProgressLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val currFormatted = if (challenge.currentValue % 1.0 == 0.0) "${challenge.currentValue.toInt()}" else "${(challenge.currentValue * 10).toInt() / 10.0}"
                            val targetFormatted = if (challenge.targetValue % 1.0 == 0.0) "${challenge.targetValue.toInt()}" else "${challenge.targetValue}"

                            Text(
                                text = "$currFormatted / $targetFormatted $localizedUnit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.completionRewardsLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "+${challenge.rankingPointsReward} pkt",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                                Text(
                                    text = "&",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = warningYellow,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "+${challenge.voltPointsReward} VP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = warningYellow
                                )
                            }
                        }
                    }
                }
            }

            if (challenge.isCompleted && !challenge.isClaimed) {
                Button(
                    onClick = { if (!isClaiming) isClaiming = true },
                    enabled = !isClaiming,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(
                        text = if (isClaiming) strings.claimingStatus else strings.claimRewardsBtn(challenge.rankingPointsReward, challenge.voltPointsReward),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (challenge.isClaimed) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = primaryColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.rewardsClaimedStatus,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(challenge.currentProgress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                    }
                    Text(
                        text = "${(challenge.currentProgress * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatNumber(
    value: String,
    label: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}