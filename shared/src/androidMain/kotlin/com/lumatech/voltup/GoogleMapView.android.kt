package com.lumatech.voltup

import androidx.compose.animation.core.*
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

    LaunchedEffect(context) {
        if (hasApiKey) {
            try {
                MapsInitializer.initialize(context)
            } catch (_: Exception) {}
        }
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
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
        if (hasLocationPermission) {
            try {
                val gpsLoc = locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                val netLoc = locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                userLocation = gpsLoc ?: netLoc
            } catch (_: SecurityException) {}
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
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 13f)
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedStation) {
        if (selectedStation != null) {
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(selectedStation.latitude, selectedStation.longitude),
                        15f
                    )
                )
            } catch (_: Exception) { }
        }
    }

    var centerUserTrigger by remember { mutableIntStateOf(0) }
    var centerLodzTrigger by remember { mutableIntStateOf(0) }

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
            val normalStationIcon = remember(context) { createStationMarkerBitmap(context, false) }
            val selectedStationIcon = remember(context) { createStationMarkerBitmap(context, true) }

            val mapProperties = remember(isDark, hasLocationPermission) {
                MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
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
                )
            ) {
                stations.forEach { station ->
                    val markerState = rememberMarkerState(
                        key = station.id,
                        position = LatLng(station.latitude, station.longitude)
                    )
                    Marker(
                        state = markerState,
                        title = station.name,
                        snippet = "${station.location} • Max: ${station.maxPowerW} W",
                        icon = if (station.id == selectedStation?.id) selectedStationIcon else normalStationIcon,
                        anchor = Offset(0.5f, 0.5f),
                        onClick = {
                            onStationSelected(station)
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

        // Przyciski kontrolne mapy (GPS + Reset na Centrum Łodzi)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (hasLocationPermission) {
                Surface(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Przycisk wyśrodkowania na centrum Łodzi (reset widoku)
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
    var panOffset by remember { mutableStateOf(Offset.Zero) }
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
                    panOffset += pan
                    zoomScale = (zoomScale * zoom).coerceIn(0.8f, 2.5f)
                }
            }
    ) {
        val width = maxWidth
        val height = maxHeight

        val panXDp = with(density) { panOffset.x.toDp() }
        val panYDp = with(density) { panOffset.y.toDp() }

        LaunchedEffect(selectedStation) {
            if (selectedStation != null) {
                val targetPanX = (0.5f - selectedStation.mapCoordinatesX) * with(density) { width.toPx() }
                val targetPanY = (0.40f - selectedStation.mapCoordinatesY) * with(density) { height.toPx() }
                panOffset = Offset(targetPanX, targetPanY)
            }
        }

        LaunchedEffect(centerUserTrigger) {
            if (centerUserTrigger > 0) {
                val targetPanX = (0.5f - userNormX) * with(density) { width.toPx() }
                val targetPanY = (0.40f - userNormY) * with(density) { height.toPx() }
                panOffset = Offset(targetPanX, targetPanY)
            }
        }

        LaunchedEffect(centerLodzTrigger) {
            if (centerLodzTrigger > 0) {
                panOffset = Offset.Zero
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

            // Park na Zdrowiu
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.10f + pX, size.height * 0.22f + pY),
                size = Size(size.width * 0.24f, size.height * 0.26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            // Park Poniatowskiego - obejmuje stację PP-1
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.26f + pX, size.height * 0.40f + pY),
                size = Size(size.width * 0.26f, size.height * 0.26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            // Park Źródliska & Księży Młyn
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.62f + pX, size.height * 0.46f + pY),
                size = Size(size.width * 0.24f, size.height * 0.24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            // Park Matejki - stacja PM-1
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

        // Pinezki stacji kinetycznych (zielone punkty)
        stations.forEach { station ->
            val isSelected = station.id == selectedStation?.id
            val posX = width * station.mapCoordinatesX + panXDp
            val posY = height * station.mapCoordinatesY + panYDp

            Box(
                modifier = Modifier
                    .offset(x = posX - 16.dp, y = posY - 16.dp)
                    .size(32.dp)
                    .clickable { onStationSelected(station) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size((haloRadius * 2).dp)
                            .clip(CircleShape)
                            .background(LightPrimary.copy(alpha = haloAlpha))
                    )
                }

                // Zewnętrzny obrys transparentny w środku
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 24.dp else 20.dp)
                        .border(
                            1.6.dp,
                            if (isSelected) LightPrimary else LightPrimary.copy(alpha = 0.9f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Zielone kółko w środku, powiększone bliżej obrysu (pusto między obrysem a kółkiem)
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 15.dp else 12.dp)
                            .clip(CircleShape)
                            .background(LightPrimary)
                    )
                }
            }
        }

        // Pinezka użytkownika (niebieska)
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
        val sizePx = ((if (isSelected) 28 else 22) * density).toInt().coerceAtLeast(1)
        val bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val center = sizePx / 2f

        // Zewnętrzny obrys kółka (transparentne w środku)
        val ringPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#00A35B")
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.8f * density
        }
        val ringRadius = center - (2.5f * density)
        canvas.drawCircle(center, center, ringRadius, ringPaint)

        // Zielone kółko w środku, powiększone bliżej obrysu
        val dotPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#00A35B")
            style = android.graphics.Paint.Style.FILL
        }
        val dotRadius = (if (isSelected) 6.8f else 5.4f) * density
        canvas.drawCircle(center, center, dotRadius, dotPaint)

        com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (_: Exception) {
        null
    }
}
