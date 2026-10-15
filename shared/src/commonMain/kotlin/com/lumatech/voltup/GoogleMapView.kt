package com.lumatech.voltup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

const val GOOGLE_MAPS_API_KEY = "AIzaSyDv7P1ch_HIMkAk0Dk3Gph7gcn3hAeyJ-o"

@Composable
expect fun GoogleMapView(
    modifier: Modifier,
    stations: List<KineticStation>,
    selectedStation: KineticStation?,
    onStationSelected: (KineticStation) -> Unit
)
