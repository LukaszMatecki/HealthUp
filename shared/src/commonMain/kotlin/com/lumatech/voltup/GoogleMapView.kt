package com.lumatech.voltup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Miejsce na Twój klucz Google Maps API
const val GOOGLE_MAPS_API_KEY = "YOUR_GOOGLE_MAPS_API_KEY_HERE"

@Composable
expect fun GoogleMapView(
    modifier: Modifier,
    stations: List<KineticStation>,
    selectedStation: KineticStation?,
    onStationSelected: (KineticStation) -> Unit
)
