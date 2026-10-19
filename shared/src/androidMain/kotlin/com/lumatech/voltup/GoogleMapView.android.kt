package com.lumatech.voltup

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.launch

private const val DARK_MAP_STYLE_JSON = """[
  {"elementType": "geometry", "stylers": [{"color": "#181C23"}]},
  {"elementType": "labels.icon", "stylers": [{"visibility": "off"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#94A3B8"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#181C23"}]},
  {"featureType": "administrative", "elementType": "geometry", "stylers": [{"color": "#334155"}]},
  {"featureType": "administrative.country", "elementType": "labels.text.fill", "stylers": [{"color": "#94A3B8"}]},
  {"featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{"color": "#CBD5E1"}]},
  {"featureType": "landscape", "elementType": "geometry", "stylers": [{"color": "#14181F"}]},
  {"featureType": "poi", "elementType": "labels.text.fill", "stylers": [{"color": "#64748B"}]},
  {"featureType": "poi.park", "elementType": "geometry", "stylers": [{"color": "#112217"}]},
  {"featureType": "poi.park", "elementType": "labels.text.fill", "stylers": [{"color": "#00E676"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#1E2530"}]},
  {"featureType": "road", "elementType": "geometry.fill", "stylers": [{"color": "#1E2530"}]},
  {"featureType": "road", "elementType": "labels.text.fill", "stylers": [{"color": "#94A3B8"}]},
  {"featureType": "road.arterial", "elementType": "geometry", "stylers": [{"color": "#283140"}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#334155"}]},
  {"featureType": "transit", "elementType": "geometry", "stylers": [{"color": "#1E2530"}]},
  {"featureType": "transit", "elementType": "labels.text.fill", "stylers": [{"color": "#64748B"}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#0B0E14"}]},
  {"featureType": "water", "elementType": "labels.text.fill", "stylers": [{"color": "#475569"}]}
]"""

@Composable
actual fun GoogleMapView(
    modifier: Modifier,
    stations: List<KineticStation>,
    selectedStation: KineticStation?,
    onStationSelected: (KineticStation) -> Unit
) {
    val isDark = isAppInDarkTheme()
    val hasApiKey = GOOGLE_MAPS_API_KEY.isNotBlank() && GOOGLE_MAPS_API_KEY != "YOUR_GOOGLE_MAPS_API_KEY_HERE"

    val context = androidx.compose.ui.platform.LocalContext.current

    var isMapInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(context) {
        if (hasApiKey) {
            try {
                MapsInitializer.initialize(context)
                isMapInitialized = true
            } catch (_: Exception) {}
        }
    }

    fun isLocationGranted(): Boolean {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    var hasLocationPermission by remember {
        mutableStateOf(isLocationGranted())
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted || isLocationGranted()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val granted = isLocationGranted()
                hasLocationPermission = granted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val locationManager = remember {
        context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
    }
    var userLocation by remember { mutableStateOf<android.location.Location?>(null) }

    fun refreshLocation() {
        if (isLocationGranted()) {
            try {
                val gpsLoc = locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                val netLoc = locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                userLocation = gpsLoc ?: netLoc
            } catch (_: SecurityException) {} catch (_: Exception) {}
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            refreshLocation()
        }
    }

    val userNormX = remember(userLocation) {
        userLocation?.let { loc ->
            val norm = ((loc.longitude - 19.38) / (19.54 - 19.38)).toFloat()
            (0.10f + norm.coerceIn(0f, 1f) * 0.80f)
        } ?: 0.50f
    }
    val userNormY = remember(userLocation) {
        userLocation?.let { loc ->
            val norm = ((51.82 - loc.latitude) / (51.82 - 51.70)).toFloat()
            (0.10f + norm.coerceIn(0f, 1f) * 0.80f)
        } ?: 0.50f
    }

    val initialLat = selectedStation?.latitude ?: 51.7592
    val initialLng = selectedStation?.longitude ?: 19.4560

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 14f)
    }

    var centerUserTrigger by remember { mutableIntStateOf(0) }
    var centerLodzTrigger by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedStation?.id) {
        val st = selectedStation ?: return@LaunchedEffect
        try {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(st.latitude, st.longitude),
                    15.5f
                ),
                durationMs = 600
            )
        } catch (_: Exception) {
            try {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(st.latitude, st.longitude),
                        15.5f
                    )
                )
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(centerUserTrigger) {
        val uLoc = userLocation
        if (centerUserTrigger > 0 && uLoc != null) {
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(uLoc.latitude, uLoc.longitude),
                        15.5f
                    ),
                    durationMs = 600
                )
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(centerLodzTrigger) {
        if (centerLodzTrigger > 0) {
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(51.7592, 19.4560),
                        13.5f
                    ),
                    durationMs = 600
                )
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) DarkSurface else LightSurface)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        if (event.changes.any { it.pressed }) {
                            event.changes.forEach { change ->
                                val delta = change.position - change.previousPosition
                                if (delta != Offset.Zero) {
                                    change.consume()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        if (hasApiKey) {
            val normalStationIcon = remember(context, isMapInitialized) { createStationMarkerBitmap(context, false) }
            val selectedStationIcon = remember(context, isMapInitialized) { createStationMarkerBitmap(context, true) }

            val actualGranted = isLocationGranted()
            val mapProperties = remember(isDark, hasLocationPermission, actualGranted) {
                MapProperties(
                    isMyLocationEnabled = hasLocationPermission && actualGranted,
                    mapStyleOptions = if (isDark) MapStyleOptions(DARK_MAP_STYLE_JSON) else null
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false,
                    mapToolbarEnabled = false
                ),
                onMapLoaded = {
                    val targetLat = selectedStation?.latitude ?: 51.7592
                    val targetLng = selectedStation?.longitude ?: 19.4560
                    try {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(targetLat, targetLng),
                                15.5f
                            )
                        )
                    } catch (_: Exception) {}
                }
            ) {
                stations.forEach { station ->
                    val isSelected = station.id == selectedStation?.id
                    val markerState = rememberMarkerState(
                        key = "${station.id}_${station.latitude}_${station.longitude}",
                        position = LatLng(station.latitude, station.longitude)
                    )
                    LaunchedEffect(station.latitude, station.longitude) {
                        markerState.position = LatLng(station.latitude, station.longitude)
                    }
                    Marker(
                        state = markerState,
                        title = station.name,
                        snippet = "${station.location} • Max: ${station.maxPowerW} W",
                        icon = if (isSelected) selectedStationIcon else normalStationIcon,
                        anchor = Offset(0.5f, 0.90f),
                        zIndex = if (isSelected) 10f else 1f,
                        onClick = {
                            onStationSelected(station)
                            coroutineScope.launch {
                                try {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(station.latitude, station.longitude),
                                            15.5f
                                        ),
                                        durationMs = 600
                                    )
                                } catch (_: Exception) {}
                            }
                            true
                        }
                    )
                }
            }
        } else {
            InteractiveSchematicMap(
                stations = stations,
                selectedStation = selectedStation,
                onStationSelected = onStationSelected,
                isDark = isDark,
                hasLocationPermission = hasLocationPermission,
                userNormX = userNormX,
                userNormY = userNormY,
                centerUserTrigger = centerUserTrigger,
                centerLodzTrigger = centerLodzTrigger
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (!isLocationGranted()) {
                            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            hasLocationPermission = true
                            refreshLocation()
                            centerUserTrigger++
                            if (hasApiKey) {
                                coroutineScope.launch {
                                    try {
                                        val targetLat = userLocation?.latitude ?: 51.7592
                                        val targetLng = userLocation?.longitude ?: 19.4560
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(targetLat, targetLng),
                                                15f
                                            )
                                        )
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    },
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = CircleShape,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Wyśrodkuj na mojej lokalizacji",
                        tint = if (hasLocationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable {
                        centerLodzTrigger++
                        if (hasApiKey) {
                            coroutineScope.launch {
                                try {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(51.7592, 19.4560),
                                            13.5f
                                        )
                                    )
                                } catch (_: Exception) {}
                            }
                        }
                    },
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = CircleShape,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Wyśrodkuj na Centrum Łodzi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveSchematicMap(
    stations: List<KineticStation>,
    selectedStation: KineticStation?,
    onStationSelected: (KineticStation) -> Unit,
    isDark: Boolean,
    hasLocationPermission: Boolean = false,
    userNormX: Float = 0.50f,
    userNormY: Float = 0.50f,
    centerUserTrigger: Int = 0,
    centerLodzTrigger: Int = 0
) {
    val coroutineScope = rememberCoroutineScope()
    val panAnimatable = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val panOffset = panAnimatable.value
    var zoomScale by remember { mutableFloatStateOf(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val haloRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAlpha"
    )

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    coroutineScope.launch {
                        panAnimatable.snapTo(panAnimatable.value + pan)
                    }
                    zoomScale = (zoomScale * zoom).coerceIn(0.8f, 2.5f)
                }
            }
    ) {
        val width = maxWidth
        val height = maxHeight

        val panXDp = with(density) { panOffset.x.toDp() }
        val panYDp = with(density) { panOffset.y.toDp() }

        LaunchedEffect(selectedStation?.id) {
            val st = selectedStation ?: return@LaunchedEffect
            val targetPanX = (0.5f - st.mapCoordinatesX) * with(density) { width.toPx() }
            val targetPanY = (0.40f - st.mapCoordinatesY) * with(density) { height.toPx() }
            panAnimatable.animateTo(
                targetValue = Offset(targetPanX, targetPanY),
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        }

        LaunchedEffect(centerUserTrigger) {
            if (centerUserTrigger > 0) {
                val targetPanX = (0.5f - userNormX) * with(density) { width.toPx() }
                val targetPanY = (0.40f - userNormY) * with(density) { height.toPx() }
                panAnimatable.animateTo(
                    targetValue = Offset(targetPanX, targetPanY),
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
            }
        }

        LaunchedEffect(centerLodzTrigger) {
            if (centerLodzTrigger > 0) {
                panAnimatable.animateTo(
                    targetValue = Offset.Zero,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseBg = if (isDark) Color(0xFF14181F) else Color(0xFFF8FAFC)
            drawRect(color = baseBg)

            val gridColor = if (isDark) Color(0xFF222731) else Color(0xFFE2E8F0)
            val streetColor = if (isDark) Color(0xFF1E232B) else Color(0xFFEDF2F7)
            val parkColor = if (isDark) Color(0xFF112217) else Color(0xFFE8F5E9)

            val pX = panOffset.x
            val pY = panOffset.y

            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.10f + pX, size.height * 0.22f + pY),
                size = Size(size.width * 0.24f, size.height * 0.26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.26f + pX, size.height * 0.40f + pY),
                size = Size(size.width * 0.26f, size.height * 0.26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.62f + pX, size.height * 0.46f + pY),
                size = Size(size.width * 0.24f, size.height * 0.24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.70f + pX, size.height * 0.14f + pY),
                size = Size(size.width * 0.18f, size.height * 0.16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            drawLine(
                color = streetColor,
                start = Offset(size.width * 0.50f + pX, 0f),
                end = Offset(size.width * 0.50f + pX, size.height),
                strokeWidth = 4.dp.toPx()
            )
            drawLine(
                color = streetColor,
                start = Offset(0f, size.height * 0.52f + pY),
                end = Offset(size.width, size.height * 0.52f + pY),
                strokeWidth = 4.dp.toPx()
            )

            val step = 40.dp.toPx() * zoomScale
            var x = (pX % step)
            while (x < size.width) {
                if (x >= 0f) {
                    drawLine(gridColor.copy(alpha = 0.3f), Offset(x, 0f), Offset(x, size.height), 1f)
                }
                x += step
            }
            var y = (pY % step)
            while (y < size.height) {
                if (y >= 0f) {
                    drawLine(gridColor.copy(alpha = 0.3f), Offset(0f, y), Offset(size.width, y), 1f)
                }
                y += step
            }
        }

        stations.sortedBy { it.id == selectedStation?.id }.forEach { station ->
            val isSelected = station.id == selectedStation?.id
            val posX = width * station.mapCoordinatesX + panXDp
            val posY = height * station.mapCoordinatesY + panYDp

            Box(
                modifier = Modifier
                    .offset(x = posX - 20.dp, y = posY - 34.dp)
                    .size(40.dp)
                    .zIndex(if (isSelected) 10f else 1f)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        onStationSelected(station)
                        coroutineScope.launch {
                            val targetPanX = (0.5f - station.mapCoordinatesX) * with(density) { width.toPx() }
                            val targetPanY = (0.40f - station.mapCoordinatesY) * with(density) { height.toPx() }
                            panAnimatable.animateTo(
                                targetValue = Offset(targetPanX, targetPanY),
                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Drop shadow underneath pin
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                        .size(width = if (isSelected) 22.dp else 16.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = if (isDark) 0.55f else 0.30f))
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 6.dp)
                            .size((haloRadius * 2.2f).dp)
                            .clip(CircleShape)
                            .background(LightPrimary.copy(alpha = haloAlpha))
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 6.dp)
                        .size(if (isSelected) 32.dp else 26.dp),
                    shape = CircleShape,
                    color = if (isSelected) LightPrimary else Color(0xFF1E824C),
                    border = BorderStroke(if (isSelected) 2.5.dp else 2.dp, Color.White),
                    shadowElevation = if (isSelected) 6.dp else 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (isSelected) 18.dp else 14.dp)
                        )
                    }
                }
            }
        }

        if (hasLocationPermission) {
            val userPosX = width * userNormX + panXDp
            val userPosY = height * userNormY + panYDp
            Box(
                modifier = Modifier
                    .offset(x = userPosX - 12.dp, y = userPosY - 12.dp)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((haloRadius * 1.4f).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3).copy(alpha = haloAlpha))
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }

        if (selectedStation != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (selectedStation.isOnline) LightPrimary else Color.Gray)
                    )
                    Column {
                        Text(
                            text = "${selectedStation.name} (${selectedStation.id})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${selectedStation.location} • Max: ${selectedStation.maxPowerW} W • ${if (selectedStation.isOnline) "Aktywna" else "Offline"}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun createStationMarkerBitmap(
    context: android.content.Context,
    isSelected: Boolean
): com.google.android.gms.maps.model.BitmapDescriptor? {
    return try {
        MapsInitializer.initialize(context)
        val density = context.resources.displayMetrics.density
        val widthDp = if (isSelected) 42 else 34
        val heightDp = if (isSelected) 52 else 42
        val widthPx = (widthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (heightDp * density).toInt().coerceAtLeast(1)

        val bitmap = android.graphics.Bitmap.createBitmap(widthPx, heightPx, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val centerX = widthPx / 2f

        // Shadow at bottom
        val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(85, 0, 0, 0)
            style = android.graphics.Paint.Style.FILL
        }
        val shadowW = (if (isSelected) 22f else 16f) * density
        val shadowH = (if (isSelected) 6.5f else 5f) * density
        val shadowBottom = heightPx - (1.5f * density)
        val shadowRect = android.graphics.RectF(
            centerX - shadowW / 2f,
            shadowBottom - shadowH,
            centerX + shadowW / 2f,
            shadowBottom
        )
        canvas.drawOval(shadowRect, shadowPaint)

        // Pin Teardrop/Marker Path
        val headRadius = (if (isSelected) 17f else 14f) * density
        val headCenterY = headRadius + (2.5f * density)
        val tipY = heightPx - (5.5f * density)

        val pinPath = android.graphics.Path().apply {
            moveTo(centerX, tipY)
            quadTo(
                centerX - headRadius * 1.05f,
                headCenterY + headRadius * 0.7f,
                centerX - headRadius,
                headCenterY
            )
            arcTo(
                android.graphics.RectF(
                    centerX - headRadius,
                    headCenterY - headRadius,
                    centerX + headRadius,
                    headCenterY + headRadius
                ),
                180f,
                180f
            )
            quadTo(
                centerX + headRadius * 1.05f,
                headCenterY + headRadius * 0.7f,
                centerX,
                tipY
            )
            close()
        }

        // Fill for pin
        val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor(if (isSelected) "#00A35B" else "#1E824C")
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawPath(pinPath, fillPaint)

        // White border for pin
        val borderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = (if (isSelected) 2.8f else 2.2f) * density
        }
        canvas.drawPath(pinPath, borderPaint)

        // Inner white circle
        val innerCirclePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.FILL
        }
        val innerRadius = (if (isSelected) 8f else 6.5f) * density
        canvas.drawCircle(centerX, headCenterY, innerRadius, innerCirclePaint)

        // Inner green dot
        val dotPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor(if (isSelected) "#00A35B" else "#1E824C")
            style = android.graphics.Paint.Style.FILL
        }
        val dotRadius = (if (isSelected) 4.2f else 3.4f) * density
        canvas.drawCircle(centerX, headCenterY, dotRadius, dotPaint)

        com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (_: Exception) {
        null
    }
}
