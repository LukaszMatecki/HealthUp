package com.lumatech.voltup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StationsMapScreen(
    stations: List<KineticStation> = MockData.sampleStations,
    onSelectStationToWorkout: (KineticStation) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDeviceType by remember { mutableStateOf<DeviceType?>(null) }
    var selectedDistrict by remember { mutableStateOf<String?>(null) }
    var highlightedStationId by remember { mutableStateOf(stations.firstOrNull()?.id) }
    var stationForReportDialog by remember { mutableStateOf<KineticStation?>(null) }
    var navigationInfoDialog by remember { mutableStateOf<KineticStation?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val districts = remember(stations) {
        listOf("Wszystkie") + stations.map { it.district }.distinct()
    }

    val filteredStations = remember(searchQuery, selectedDeviceType, selectedDistrict, stations) {
        stations.filter { station ->
            val matchesQuery = searchQuery.isBlank() ||
                    station.name.contains(searchQuery, ignoreCase = true) ||
                    station.location.contains(searchQuery, ignoreCase = true) ||
                    station.district.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedDeviceType == null || station.type == selectedDeviceType
            val matchesDistrict = selectedDistrict == null || selectedDistrict == "Wszystkie" || station.district == selectedDistrict
            matchesQuery && matchesType && matchesDistrict
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Scaffold(
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ) {
                    Text(text = msg, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "INFRASTRUKTURA ŁODZI",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Mapa Eko-Stacji",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${filteredStations.size} stacji",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Szukaj parku, stacji, dzielnicy...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Wyczyść")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtry urządzeń
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedDeviceType == null,
                        onClick = { selectedDeviceType = null },
                        label = { Text("Wszystkie Typy", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
                items(DeviceType.entries) { type ->
                    val isSelected = selectedDeviceType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDeviceType = if (isSelected) null else type },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = type.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(type.displayName, fontSize = 12.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interaktywna Stylizowana Mapa Łodzi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredStations) {
                            detectTapGestures { tapOffset ->
                                // Find closest station to tap
                                val clickedStation = filteredStations.minByOrNull { s ->
                                    val stationPoint = Offset(s.mapCoordinatesX * size.width, s.mapCoordinatesY * size.height)
                                    (stationPoint - tapOffset).getDistance()
                                }
                                if (clickedStation != null) {
                                    highlightedStationId = clickedStation.id
                                }
                            }
                        }
                ) {
                    val step = 28.dp.toPx()
                    for (x in 0..(size.width / step).toInt()) {
                        drawLine(gridColor, Offset(x * step, 0f), Offset(x * step, size.height), 1f)
                    }
                    for (y in 0..(size.height / step).toInt()) {
                        drawLine(gridColor, Offset(0f, y * step), Offset(size.width, y * step), 1f)
                    }

                    // Draw city district boundaries representation
                    drawLine(
                        color = primaryColor.copy(alpha = 0.2f),
                        start = Offset(size.width * 0.45f, 0f),
                        end = Offset(size.width * 0.45f, size.height),
                        strokeWidth = 2f
                    )

                    // Draw station points
                    filteredStations.forEach { station ->
                        val point = Offset(station.mapCoordinatesX * size.width, station.mapCoordinatesY * size.height)
                        val isHighlighted = station.id == highlightedStationId

                        if (isHighlighted) {
                            // Pulsing radar ripple
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.35f),
                                radius = radarPulse.dp.toPx(),
                                center = point
                            )
                        }

                        // Static halo
                        drawCircle(
                            color = if (isHighlighted) primaryColor.copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.2f),
                            radius = if (isHighlighted) 12.dp.toPx() else 8.dp.toPx(),
                            center = point
                        )

                        // Station core pin
                        drawCircle(
                            color = if (isHighlighted) EnergyYellow else primaryColor,
                            radius = if (isHighlighted) 7.dp.toPx() else 5.dp.toPx(),
                            center = point
                        )
                    }
                }

                // Map Overlay Info Pill
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Kliknij punkt na mapie, by podświetlić stację",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista Stacji
            if (filteredStations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Brak stacji spełniających kryteria",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = {
                            searchQuery = ""
                            selectedDeviceType = null
                            selectedDistrict = null
                        }) {
                            Text("Zresetuj filtry", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredStations) { station ->
                        val isHighlighted = station.id == highlightedStationId
                        InteractiveStationCard(
                            station = station,
                            isHighlighted = isHighlighted,
                            onCardClick = { highlightedStationId = station.id },
                            onStartClick = { onSelectStationToWorkout(station) },
                            onReportClick = { stationForReportDialog = station },
                            onNavigateClick = { navigationInfoDialog = station }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Modal Zgłoszenia Usterki
    stationForReportDialog?.let { station ->
        var selectedIssue by remember { mutableStateOf("Brak oporu / opór zacięty") }
        val issues = listOf(
            "Brak oporu / opór zacięty",
            "Ekran / wskaźnik stacji nie działa",
            "Uszkodzone siodełko lub uchwyt",
            "Kod QR starty lub nieczytelny",
            "Inna usterka techniczna"
        )

        AlertDialog(
            onDismissRequest = { stationForReportDialog = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = EnergyYellow
                    )
                    Text("Zgłoś Usterkę Stacji", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${station.name} (${station.location})",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Wybierz problem, aby powiadomić miejski serwis techniczny Łodzi:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        issues.forEach { issue ->
                            val isSelected = selectedIssue == issue
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedIssue = issue },
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedIssue = issue },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Text(issue, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        snackbarMessage = "Zgłoszenie wysłane! Dziękujemy za dbanie o eko-infrastrukturę Łodzi."
                        stationForReportDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Wyślij zgłoszenie", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { stationForReportDialog = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Modal Nawigacji
    navigationInfoDialog?.let { station ->
        AlertDialog(
            onDismissRequest = { navigationInfoDialog = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Nawigacja do Stacji", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = station.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = station.location,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(station.distance, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Odległość", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ok. 6 min", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Pieszo", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${station.activeUsersCount} os.", fontWeight = FontWeight.Bold, color = EnergyYellow)
                            Text("Aktywnych", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(
                        text = "Wskazówka: Wejście od głównej alei parkowej. Stacja oznaczona zielonym pylonem VoltUp z kodem QR.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        navigationInfoDialog = null
                        onSelectStationToWorkout(station)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Połącz na miejscu ⚡", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { navigationInfoDialog = null }) {
                    Text("Zamknij")
                }
            }
        )
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            kotlinx.coroutines.delay(3000)
            snackbarMessage = null
        }
    }
}

@Composable
fun InteractiveStationCard(
    station: KineticStation,
    isHighlighted: Boolean,
    onCardClick: () -> Unit,
    onStartClick: () -> Unit,
    onReportClick: () -> Unit,
    onNavigateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = if (isHighlighted) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = station.type.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = station.name,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = station.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Text(text = station.distance, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(imageVector = Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                            Text(text = "${station.activeUsersCount} ćwiczy teraz", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNavigateClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NearMe, contentDescription = "Nawiguj", modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onReportClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ReportProblem, contentDescription = "Zgłoś problem", modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onStartClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("ROZPOCZNIJ ⚡", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
