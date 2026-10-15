package com.lumatech.voltup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private const val DARK_MAP_STYLE_JSON = """[
  {"elementType": "geometry", "stylers": [{"color": "#181C23"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#94A3B8"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#181C23"}]},
  {"featureType": "administrative", "elementType": "country", "stylers": [{"color": "#94A3B8"}]},
  {"featureType": "poi", "elementType": "labels.text.fill", "stylers": [{"color": "#64748B"}]},
  {"featureType": "poi.park", "elementType": "geometry", "stylers": [{"color": "#112217"}]},
  {"featureType": "poi.park", "elementType": "labels.text.fill", "stylers": [{"color": "#00E676"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#222731"}]},
  {"featureType": "road", "elementType": "labels.text.fill", "stylers": [{"color": "#94A3B8"}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#0E1116"}]}
]"""

@Composable
actual fun GoogleMapView(
    modifier: Modifier,
    stations: List<KineticStation>,
    selectedStation: KineticStation?,
    onStationSelected: (KineticStation) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val hasApiKey = GOOGLE_MAPS_API_KEY.isNotBlank() && GOOGLE_MAPS_API_KEY != "YOUR_GOOGLE_MAPS_API_KEY_HERE"

    val initialLat = selectedStation?.latitude ?: 51.7592
    val initialLng = selectedStation?.longitude ?: 19.4560

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 13f)
    }

    LaunchedEffect(selectedStation) {
        if (selectedStation != null) {
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(selectedStation.latitude, selectedStation.longitude),
                        14f
                    )
                )
            } catch (_: Exception) { }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) DarkSurface else LightSurface)
    ) {
        if (hasApiKey) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = if (isDark) MapStyleOptions(DARK_MAP_STYLE_JSON) else null
                ),
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
                        snippet = "${station.location} • ${station.distance}",
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
                isDark = isDark
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (hasApiKey) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else EnergyYellow.copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (hasApiKey) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else EnergyYellow.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (hasApiKey) Icons.Default.LocationOn else Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = if (hasApiKey) MaterialTheme.colorScheme.primary else EnergyYellow,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (hasApiKey) "Google Maps" else "Siatka interaktywna",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hasApiKey) MaterialTheme.colorScheme.primary else EnergyYellow
                )
            }
        }
    }
}

@Composable
private fun InteractiveSchematicMap(
    stations: List<KineticStation>,
    selectedStation: KineticStation?,
    onStationSelected: (KineticStation) -> Unit,
    isDark: Boolean
) {
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = if (isDark) Color(0xFF222731) else Color(0xFFE2E8F0)
            val streetColor = if (isDark) Color(0xFF1E232B) else Color(0xFFEDF2F7)
            val parkColor = if (isDark) Color(0xFF112217) else Color(0xFFE8F5E9)

            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.12f, size.height * 0.22f),
                size = Size(size.width * 0.26f, size.height * 0.28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.32f, size.height * 0.40f),
                size = Size(size.width * 0.20f, size.height * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.64f, size.height * 0.50f),
                size = Size(size.width * 0.24f, size.height * 0.26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            drawLine(
                color = streetColor,
                start = Offset(size.width * 0.50f, 0f),
                end = Offset(size.width * 0.50f, size.height),
                strokeWidth = 4.dp.toPx()
            )
            drawLine(
                color = streetColor,
                start = Offset(0f, size.height * 0.52f),
                end = Offset(size.width, size.height * 0.52f),
                strokeWidth = 4.dp.toPx()
            )

            val step = 40.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor.copy(alpha = 0.3f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor.copy(alpha = 0.3f), Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }
        }

        stations.forEach { station ->
            val isSelected = station.id == selectedStation?.id
            val posX = width * station.mapCoordinatesX
            val posY = height * station.mapCoordinatesY

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
                            .background(ElectricVoltGreen.copy(alpha = haloAlpha))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(if (isSelected) 18.dp else 12.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ElectricVoltGreen else Color(0xFF00C853))
                        .border(
                            2.dp,
                            if (isDark) DarkSurface else LightSurface,
                            CircleShape
                        )
                )
            }
        }

        if (selectedStation != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
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
                            .background(ElectricVoltGreen)
                    )
                    Text(
                        text = "${selectedStation.name} • ${selectedStation.distance}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
