package com.lumatech.voltup

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun GoogleMapView(
    modifier: Modifier,
    stations: List<KineticStation>,
    selectedStation: KineticStation?,
    onStationSelected: (KineticStation) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val hasApiKey = GOOGLE_MAPS_API_KEY.isNotBlank() && GOOGLE_MAPS_API_KEY != "YOUR_GOOGLE_MAPS_API_KEY_HERE"
    var webViewError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) DarkSurface else LightSurface)
    ) {
        if (hasApiKey && !webViewError) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        webViewClient = object : WebViewClient() {
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    webViewError = true
                                }
                            }
                        }

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onMarkerClick(stationId: String) {
                                post {
                                    stations.find { it.id == stationId }?.let { onStationSelected(it) }
                                }
                            }
                        }, "AndroidBridge")
                    }
                },
                update = { webView ->
                    val markersJs = stations.joinToString(",") { s ->
                        "{ id: '${s.id}', name: '${s.name.replace("'", "\\'")}', lat: ${s.latitude}, lng: ${s.longitude}, type: '${s.type.displayName}' }"
                    }
                    val centerLat = selectedStation?.latitude ?: 51.7592
                    val centerLng = selectedStation?.longitude ?: 19.4560
                    val html = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="utf-8" />
                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                            <script src="https://maps.googleapis.com/maps/api/js?key=$GOOGLE_MAPS_API_KEY"></script>
                            <style>
                                body, html, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: ${if (isDark) "#0E1116" else "#F8FAFC"}; }
                            </style>
                        </head>
                        <body>
                            <div id="map"></div>
                            <script>
                                var map = new google.maps.Map(document.getElementById('map'), {
                                    center: { lat: $centerLat, lng: $centerLng },
                                    zoom: 13,
                                    disableDefaultUI: true,
                                    styles: ${if (isDark) "[{'elementType':'geometry','stylers':[{'color':'#181C23'}]},{'elementType':'labels.text.fill','stylers':[{'color':'#94A3B8'}]}]" else "[]"}
                                });
                                var stations = [$markersJs];
                                stations.forEach(function(s) {
                                    var marker = new google.maps.Marker({
                                        position: { lat: s.lat, lng: s.lng },
                                        map: map,
                                        title: s.name
                                    });
                                    marker.addListener('click', function() {
                                        if (window.AndroidBridge) { window.AndroidBridge.onMarkerClick(s.id); }
                                    });
                                });
                            </script>
                        </body>
                        </html>
                    """.trimIndent()
                    webView.loadDataWithBaseURL("https://maps.google.com", html, "text/html", "UTF-8", null)
                }
            )
        } else {
            // Interaktywna mapa wektorowa z informacją o kluczu API
            InteractiveSchematicMap(
                stations = stations,
                selectedStation = selectedStation,
                onStationSelected = onStationSelected,
                isDark = isDark
            )
        }

        // Komunikat informacyjny w górnym rogu
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
                    text = if (hasApiKey) "Google Maps" else "Brak klucza API (podgląd)",
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

        // Canvas z siatką ulic i parków Łodzi
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = if (isDark) Color(0xFF222731) else Color(0xFFE2E8F0)
            val streetColor = if (isDark) Color(0xFF1E232B) else Color(0xFFEDF2F7)
            val parkColor = if (isDark) Color(0xFF112217) else Color(0xFFE8F5E9)

            // Tereny parków (Zdrowie, Poniatowski, Źródliska)
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

            // Główne arterie (np. Piotrkowska / W-Z)
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

            // Delikatna siatka
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

        // Pinezki stacji
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

        // Pigułka informacyjna o wybranej stacji na dole
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
