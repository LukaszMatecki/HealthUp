package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun StationsMapScreen(
    stations: List<KineticStation> = MockData.sampleStations,
    onSelectStationToScanQr: (KineticStation) -> Unit,
    dbService: VoltUpDatabaseService = remember { getDatabaseService() }
) {
    var deviceList by remember { mutableStateOf(stations) }

    LaunchedEffect(Unit) {
        try {
            val fromDb = dbService.getDevices()
            if (fromDb.isNotEmpty()) {
                deviceList = fromDb
            }
        } catch (_: Exception) {}
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDeviceType by remember { mutableStateOf<DeviceType?>(null) }
    var selectedStation by remember { mutableStateOf<KineticStation?>(null) }
    var expandedStationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(deviceList) {
        if (selectedStation == null && deviceList.isNotEmpty()) {
            selectedStation = deviceList.firstOrNull()
        }
    }

    val currentStrings = strings
    val filteredStations = remember(searchQuery, selectedDeviceType, deviceList, currentStrings) {
        deviceList.filter { station ->
            val matchesQuery = searchQuery.isBlank() ||
                    station.name.contains(searchQuery, ignoreCase = true) ||
                    station.getLocalizedName(currentStrings).contains(searchQuery, ignoreCase = true) ||
                    station.location.contains(searchQuery, ignoreCase = true) ||
                    station.id.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedDeviceType == null || station.type == selectedDeviceType
            matchesQuery && matchesType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${strings.mapScreenTitle} ",
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

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                // BLOKADA PRZECHWYTYWANIA SWIPE PRZEZ RODZICA (np. HorizontalPager)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            // Dziecko (mapa) przetwarza zdarzenie pierwsze, następnie to my blokujemy przekazanie go dalej
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            event.changes.forEach { change ->
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            GoogleMapView(
                modifier = Modifier.fillMaxSize(),
                stations = deviceList,
                selectedStation = selectedStation,
                onStationSelected = { station ->
                    selectedStation = station
                    expandedStationId = station.id
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            placeholder = { Text(strings.searchStationsPlaceholder, fontSize = 15.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = strings.clearQueryBtn, modifier = Modifier.size(18.dp))
                    }
                }
            },
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val isAllSelected = selectedDeviceType == null
                SleekFilterChip(
                    text = strings.categoryAll,
                    isSelected = isAllSelected,
                    onClick = { selectedDeviceType = null }
                )
            }
            items(DeviceType.entries) { type ->
                val isSelected = selectedDeviceType == type
                SleekFilterChip(
                    text = type.getLocalizedName(strings),
                    isSelected = isSelected,
                    onClick = { selectedDeviceType = if (isSelected) null else type }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredStations, key = { it.id }) { station ->
                val isSelected = station.id == selectedStation?.id
                val isExpanded = station.id == expandedStationId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        .clickable {
                            selectedStation = station
                            expandedStationId = if (isExpanded) null else station.id
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = station.type.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = station.getLocalizedName(strings),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = station.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                modifier = Modifier.clickable { onSelectStationToScanQr(station) },
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = strings.scanQr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    thickness = 1.dp
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = station.location,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${strings.districtLabel}: ${station.district}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${strings.maxPowerLabel}: ${station.maxPowerW} W",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (station.isOnline) strings.statusActiveOnline else strings.statusInactive,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (station.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
fun SleekFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun StationsMapScreenPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        StationsMapScreen(
            stations = MockData.sampleStations,
            onSelectStationToScanQr = {}
        )
    }
}