package com.lumatech.voltup

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onNavigateBack: () -> Unit,
    onClearHistory: () -> Unit,
    onResetDefaults: () -> Unit,
    authService: VoltUpAuthService? = null,
    userEmail: String = "",
    onSignOut: () -> Unit = {},
    onNavigateToManageAccount: () -> Unit = {},
    onNavigateToHelpGuide: () -> Unit = {}
) {
    CommonBackHandler(enabled = true, onBack = onNavigateBack)

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetDefaultsDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showRateAppDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var isThemeExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }

    val strings = strings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.settingsScreenTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsSectionHeader(strings.accountAndSecuritySection)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingActionRow(
                            icon = Icons.Default.ManageAccounts,
                            title = strings.manageAccountTitle,
                            subtitle = strings.manageAccountSubtitleDesc,
                            titleColor = MaterialTheme.colorScheme.onSurface,
                            onClick = onNavigateToManageAccount
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(strings.themesSectionHeader)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .animateContentSize()
                    ) {
                        // Dropdown wyboru motywu
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { isThemeExpanded = !isThemeExpanded }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = strings.themesSection,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = settings.themeMode.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isThemeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isThemeExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AppThemeMode.entries.forEach { mode ->
                                    val isSelected = settings.themeMode == mode
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            onSettingsChanged(settings.copy(themeMode = mode))
                                            VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_THEME, mode.name)
                                            isThemeExpanded = false
                                        }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = mode.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = mode.description,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        // Dropdown wyboru języka
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { isLanguageExpanded = !isLanguageExpanded }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = strings.appLanguageTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = settings.language.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isLanguageExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isLanguageExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AppLanguage.entries.forEach { lang ->
                                    val isSelected = settings.language == lang
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            onSettingsChanged(settings.copy(language = lang))
                                            VoltUpStorage.setString(VoltUpPreferenceKeys.KEY_SAVED_LANGUAGE, lang.name)
                                            isLanguageExpanded = false
                                        }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = lang.displayName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionHeader(strings.workoutAndNotificationsSection)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingSwitchRow(
                            icon = Icons.Default.NotificationsActive,
                            title = strings.notificationSoundsTitle,
                            subtitle = strings.notificationSoundsSubtitle,
                            checked = settings.soundEffectsEnabled,
                            onCheckedChange = { onSettingsChanged(settings.copy(soundEffectsEnabled = it)) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        SettingSwitchRow(
                            icon = Icons.Default.MotionPhotosPause,
                            title = strings.autoPauseTitle,
                            subtitle = strings.autoPauseSubtitle,
                            checked = settings.autoPauseWhenIdle,
                            onCheckedChange = { onSettingsChanged(settings.copy(autoPauseWhenIdle = it)) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        SettingSwitchRow(
                            icon = Icons.Default.Timer,
                            title = strings.dailyRemindersTitle,
                            subtitle = strings.dailyReminderSubtitleShort,
                            checked = settings.dailyReminders,
                            onCheckedChange = { onSettingsChanged(settings.copy(dailyReminders = it)) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        SettingSwitchRow(
                            icon = Icons.Default.BrightnessHigh,
                            title = strings.keepScreenOnTitle,
                            subtitle = strings.keepScreenOnSubtitle,
                            checked = settings.keepScreenOn,
                            onCheckedChange = {
                                onSettingsChanged(settings.copy(keepScreenOn = it))
                                VoltUpStorage.setBoolean(VoltUpPreferenceKeys.KEY_KEEP_SCREEN_ON, it)
                            }
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(strings.dataAndResetSection)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingActionRow(
                            icon = Icons.Default.DeleteOutline,
                            title = strings.clearHistoryTitle,
                            subtitle = strings.clearWorkoutHistorySubtitle,
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = { showClearHistoryDialog = true }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        SettingActionRow(
                            icon = Icons.Default.Restore,
                            title = strings.resetDefaultsTitle,
                            subtitle = strings.resetDefaultsSubtitleShort,
                            titleColor = MaterialTheme.colorScheme.onSurface,
                            onClick = { showResetDefaultsDialog = true }
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(strings.aboutAppSection)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingActionRow(
                            icon = Icons.Default.StarRate,
                            title = strings.rateOurAppTitle,
                            subtitle = strings.rateOurAppSubtitle,
                            titleColor = MaterialTheme.colorScheme.onSurface,
                            onClick = { showRateAppDialog = true }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        SettingActionRow(
                            icon = Icons.Default.HelpOutline,
                            title = strings.helpAndGuideTitle,
                            subtitle = strings.helpAndGuideSubtitle,
                            titleColor = MaterialTheme.colorScheme.onSurface,
                            onClick = onNavigateToHelpGuide
                        )


                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        SettingActionRow(
                            icon = Icons.Default.Info,
                            title = strings.aboutAppMenuTitle,
                            subtitle = strings.aboutVoltUpAppSubtitle,
                            titleColor = MaterialTheme.colorScheme.onSurface,
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showClearHistoryDialog) {
        val errColor = MaterialTheme.colorScheme.error
        Dialog(
            onDismissRequest = { showClearHistoryDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(0.92f).padding(horizontal = 4.dp)
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
                            .background(errColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = errColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Text(
                        text = strings.clearHistoryConfirmTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = strings.clearHistoryConfirmDesc,
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
                                onClearHistory()
                                showClearHistoryDialog = false
                                SnackbarHelper.show("Historia sesji została wyczyszczona.")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = errColor, contentColor = Color.White),
                            shape = CircleShape
                        ) {
                            Text(strings.deleteDataBtn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Button(
                            onClick = { showClearHistoryDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text(strings.cancel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    if (showResetDefaultsDialog) {
        val primaryColor = MaterialTheme.colorScheme.primary
        Dialog(
            onDismissRequest = { showResetDefaultsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(0.92f).padding(horizontal = 4.dp)
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
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Text(
                        text = strings.resetDefaultsConfirmTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = strings.resetDefaultsConfirmDesc,
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
                                onResetDefaults()
                                showResetDefaultsDialog = false
                                SnackbarHelper.show("Przywrócono ustawienia domyślne.")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                            shape = CircleShape
                        ) {
                            Text(strings.resetDefaultsBtn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Button(
                            onClick = { showResetDefaultsDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text(strings.cancel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    if (showChangePasswordDialog && authService != null) {
        ChangePasswordDialog(
            authService = authService,
            onDismiss = { showChangePasswordDialog = false },
            onSuccess = {
                showChangePasswordDialog = false
                SnackbarHelper.show("Hasło zostało pomyślnie zmienione.")
            }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(strings.signOutConfirmTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(strings.signOutConfirmDesc, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                ) {
                    Text(strings.signOutBtn, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text(strings.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (showRateAppDialog) {
        RateAppDialog(onDismiss = { showRateAppDialog = false })
    }

    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun ThemeSelectionRow(
    mode: AppThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mode.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = titleColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = formatVoltUpAnnotated(title, FontWeight.Medium),
                    fontSize = 15.sp,
                    color = titleColor
                )
                Text(
                    text = formatVoltUpAnnotated(subtitle, FontWeight.Normal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun RateAppDialog(onDismiss: () -> Unit) {
    val strings = strings
    var selectedStars by remember { mutableStateOf(5) }
    val uriHandler = LocalUriHandler.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f).padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.StarRate,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val rateWord = if (strings is VoltUpEnglishStrings) "Rate " else "Oceń "
                    Text(
                        text = rateWord,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Volt",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Up",
                        fontWeight = FontWeight.Light,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = strings.rateAppDialogDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { starIndex ->
                        IconButton(
                            onClick = { selectedStars = starIndex },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (starIndex <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$starIndex",
                                tint = if (starIndex <= selectedStars) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (selectedStars) {
                            5 -> "Rewelacja! Dziękujemy za wsparcie!"
                            4 -> "Bardzo dobrze! Cały czas się doskonalimy."
                            3 -> "Dziękujemy, pracujemy nad nowymi funkcjami."
                            else -> "Pracujemy nad tym, aby stacje działały jeszcze lepiej!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri("https://play.google.com/store/apps/details?id=com.lumatech.voltup")
                            } catch (_: Exception) {}
                            onDismiss()
                            SnackbarHelper.show("Dziękujemy za opinię w Google Play!")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(strings.submitRatingBtn, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.maybeLaterBtn, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
    val strings = strings
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f).padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Volt",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Up",
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = strings.versionLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = strings.aboutAppDialogDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Text(
                    text = strings.copyrightNotice,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(strings.close, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        SettingsScreen(
            settings = AppSettings(),
            onSettingsChanged = {},
            onNavigateBack = {},
            onClearHistory = {},
            onResetDefaults = {}
        )
    }
}