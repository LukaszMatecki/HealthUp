package com.lumatech.voltup

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()

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
                "{ id: '${s.id}', name: '${s.name.replace("'", "\\'")}', lat: ${s.latitude}, lng: ${s.longitude}, type: '${s.type.displayName}', users: ${s.activeUsersCount} }"
            }

            val tileLayer = if (isDark) {
                "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
            } else {
                "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
            }

            val centerLat = selectedStation?.latitude ?: 51.7592
            val centerLng = selectedStation?.longitude ?: 19.4560

            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                    <style>
                        body, html, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: ${if (isDark) "#0E1116" else "#F8FAFC"}; }
                        .leaflet-popup-content-wrapper { background: ${if (isDark) "#181C23" else "#FFFFFF"}; color: ${if (isDark) "#FFFFFF" else "#000000"}; border-radius: 12px; font-family: sans-serif; }
                        .leaflet-popup-tip { background: ${if (isDark) "#181C23" else "#FFFFFF"}; }
                        .custom-pin {
                            background: #00E676;
                            border: 3px solid #FFFFFF;
                            border-radius: 50%;
                            width: 16px;
                            height: 16px;
                            box-shadow: 0 0 12px rgba(0, 230, 118, 0.8);
                        }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                    <script>
                        // Google Maps API Key Slot: $GOOGLE_MAPS_API_KEY
                        var map = L.map('map', { zoomControl: false }).setView([$centerLat, $centerLng], 13);
                        L.tileLayer('$tileLayer', {
                            maxZoom: 19,
                            attribution: '© Google / OpenStreetMap'
                        }).addTo(map);

                        var stations = [$markersJs];
                        stations.forEach(function(s) {
                            var icon = L.divIcon({ className: 'custom-pin', iconSize: [16, 16] });
                            var marker = L.marker([s.lat, s.lng], { icon: icon }).addTo(map);
                            marker.bindPopup('<b>' + s.name + '</b><br><small>' + s.type + ' • ' + s.users + ' os. ćwiczy</small>');
                            marker.on('click', function() {
                                if (window.AndroidBridge) {
                                    window.AndroidBridge.onMarkerClick(s.id);
                                }
                            });
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()

            webView.loadDataWithBaseURL("https://maps.google.com", html, "text/html", "UTF-8", null)
        }
    )
}
