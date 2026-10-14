package com.lumatech.voltup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onNavigateBack: () -> Unit,
    onClearHistory: () -> Unit,
    onResetDefaults: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDistrictPicker by remember { mutableStateOf(false) }

    val districts = listOf(
        "Polesie (Park na Zdrowiu)",
        "Śródmieście & Piotrkowska",
        "Widzew & Księży Młyn",
        "Bałuty & Manufaktura",
        "Górna & Stawy Stefańskiego"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ustawienia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showResetDialog = true }) {
                        Text(
                            text = "Domyślne",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // SEKCJA 1: Wygląd i Styl
            item {
                SettingsSectionHeader(title = "WYGLĄD I MOTYW", icon = Icons.Default.Palette)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Schemat kolorystyczny aplikacji",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Theme Mode Selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                val isSelected = settings.themeMode == mode
                                val previewColor = when (mode) {
                                    ThemeMode.CYBER_DARK -> ElectricVoltGreen
                                    ThemeMode.NEON_CYAN -> CyanPrimary
                                    ThemeMode.SOLAR_AMBER -> SolarPrimary
                                    ThemeMode.LIGHT_ECO -> EcoLightPrimary
                                    ThemeMode.AMOLED_BLACK -> ElectricVoltGreen
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onSettingsChanged(settings.copy(themeMode = mode))
                                        },
                                    color = if (isSelected) previewColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, previewColor) else null,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(previewColor)
                                            )
                                            Column {
                                                Text(
                                                    text = mode.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = mode.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onSettingsChanged(settings.copy(themeMode = mode)) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = previewColor
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Kinetic Glow Switch
                        SettingsSwitchRow(
                            title = "Efekt kinetycznego blasku (Neon Glow)",
                            subtitle = "Dynamiczne pulsowanie i animacje ładowania sieci miejskiej",
                            checked = settings.kineticGlowEnabled,
                            onCheckedChange = { onSettingsChanged(settings.copy(kineticGlowEnabled = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Compact Cards Switch
                        SettingsSwitchRow(
                            title = "Zwarty układ kafelków",
                            subtitle = "Mniejsze marginesy i gęstsze rozmieszczenie elementów",
                            checked = settings.compactCards,
                            onCheckedChange = { onSettingsChanged(settings.copy(compactCards = it)) }
                        )
                    }
                }
            }

            // SEKCJA 2: Trening i Sensory Kinetyczne
            item {
                SettingsSectionHeader(title = "TRENING I SENSORY KINETYCZNE", icon = Icons.Default.FitnessCenter)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Target Energy per Session
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Domyślny cel sesji treningowej",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${settings.targetSessionWh} Wh",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "Pozwala zasilić ok. ${(settings.targetSessionWh / 50.0 * 60).toInt()} minut miejskiej latarni parkowej LED.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(50, 100, 150, 250, 500).forEach { whOption ->
                                    val isSelected = settings.targetSessionWh == whOption
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSettingsChanged(settings.copy(targetSessionWh = whOption)) },
                                        label = { Text("${whOption}Wh", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsSwitchRow(
                            title = "Wibracje i haptyka",
                            subtitle = "Wyczuwalny impuls w telefonie przy zmianie oporu stacji i co każde wygenerowane 10 Wh",
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = { onSettingsChanged(settings.copy(hapticFeedbackEnabled = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsSwitchRow(
                            title = "Efekty dźwiękowe i doping audio",
                            subtitle = "Sygnały dźwiękowe przy rozpoczęciu, ukończeniu i osiągnięciu celów",
                            checked = settings.soundEffectsEnabled,
                            onCheckedChange = { onSettingsChanged(settings.copy(soundEffectsEnabled = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsSwitchRow(
                            title = "Automatyczna pauza",
                            subtitle = "Wstrzymaj naliczanie czasu, gdy przestaniesz pedałować lub poruszać stepperem",
                            checked = settings.autoPauseWhenIdle,
                            onCheckedChange = { onSettingsChanged(settings.copy(autoPauseWhenIdle = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsSwitchRow(
                            title = "Autopołączenie Bluetooth BLE / NFC",
                            subtitle = "Szybkie łączenie ze stacją kinetyczną po zbliżeniu telefonu",
                            checked = settings.bluetoothAutoConnect,
                            onCheckedChange = { onSettingsChanged(settings.copy(bluetoothAutoConnect = it)) }
                        )
                    }
                }
            }

            // SEKCJA 3: Dzielnica i Cel Miasta
            item {
                SettingsSectionHeader(title = "DZIELNICA I SPOŁECZNOŚĆ ŁODZI", icon = Icons.Default.LocationCity)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Home District selector
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDistrictPicker = true },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Twoja Dzielnica Domowa",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = settings.homeDistrict,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Grid destination
                        Text(
                            text = "Cel przeznaczenia Twojej energii:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        GridDestination.entries.forEach { dest ->
                            val isSelected = settings.gridDestination == dest
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSettingsChanged(settings.copy(gridDestination = dest))
                                    },
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dest.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = dest.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSettingsChanged(settings.copy(gridDestination = dest)) },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsSwitchRow(
                            title = "Anonimowy udział w Lidze Dzielnic",
                            subtitle = "Ukryj swoje imię w publicznych rankingach punktowych Łodzi",
                            checked = settings.anonymousLeaderboard,
                            onCheckedChange = { onSettingsChanged(settings.copy(anonymousLeaderboard = it)) }
                        )
                    }
                }
            }

            // SEKCJA 4: Powiadomienia
            item {
                SettingsSectionHeader(title = "POWIADOMIENIA I ALERTY", icon = Icons.Default.Notifications)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SettingsSwitchRow(
                            title = "Codzienne przypomnienie o aktywności",
                            subtitle = "Krótki impuls motywacyjny o wybranej porze dnia",
                            checked = settings.dailyReminders,
                            onCheckedChange = { onSettingsChanged(settings.copy(dailyReminders = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        SettingsSwitchRow(
                            title = "Alerty Godzin Podwójnych Punktów (x2)",
                            subtitle = "Powiadomienie, gdy w parkach w Łodzi obowiązuje mnożnik VoltPoints",
                            checked = settings.peakHoursAlerts,
                            onCheckedChange = { onSettingsChanged(settings.copy(peakHoursAlerts = it)) }
                        )
                    }
                }
            }

            // SEKCJA 5: Dane i Aplikacja
            item {
                SettingsSectionHeader(title = "PAMIĘĆ I INFORMACJE", icon = Icons.Default.Info)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showClearHistoryDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wyczyść historię sesji treningowych")
                        }

                        Button(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("O Aplikacji VoltUp Łódź (v2.4.0)")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal wyboru dzielnicy
    if (showDistrictPicker) {
        AlertDialog(
            onDismissRequest = { showDistrictPicker = false },
            confirmButton = {
                TextButton(onClick = { showDistrictPicker = false }) {
                    Text("Gotowe", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            title = {
                Text("Wybierz Dzielnicę Domową", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    districts.forEach { dist ->
                        val isSelected = settings.homeDistrict == dist
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSettingsChanged(settings.copy(homeDistrict = dist))
                                    showDistrictPicker = false
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dist,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
        )
    }

    // Modal resetu ustawień
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Zresetować ustawienia?") },
            text = { Text("Wszystkie preferencje wyglądu, dźwięków oraz celów powrócą do wartości fabrycznych.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                ) {
                    Text("Resetuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Modal czyszczenia historii
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Wyczyścić historię treningów?") },
            text = { Text("Lokalna lista odbytych sesji zostanie wyczyszczona. Twoje łączne punkty VoltPoints oraz pozycja w rankingu Łodzi pozostaną nienaruszone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Wyczyść historię")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Modal O Aplikacji
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Zamknij")
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("VoltUp Łódź v2.4.0", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Inteligentna platforma miejskiej energii kinetycznej tworzona w Łodzi.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Każdy ruch na certyfikowanych siłowniach plenerowych i placach zabaw zasila oświetlenie parkowe, huby rowerowe MPK i zielone projekty społeczne.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Partnerzy: Miasto Łódź, MPK-Łódź, EC1 Łódź, Aquapark Fala.",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
