package com.lumatech.voltup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StationsMapScreen(
    onSelectStationToWorkout: (KineticStation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "INFRASTRUKTURA ŁODZI",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricVoltGreen,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interaktywny podgląd mapy Łodzi (Stylizowany widok siatki miejskiej)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color(0xFF2C3E50).copy(alpha = 0.4f)
                val step = 30.dp.toPx()
                for (x in 0..(size.width / step).toInt()) {
                    drawLine(gridColor, Offset(x * step, 0f), Offset(x * step, size.height), 1.5f)
                }
                for (y in 0..(size.height / step).toInt()) {
                    drawLine(gridColor, Offset(0f, y * step), Offset(size.width, y * step), 1.5f)
                }

                // Punkty stacji w Łodzi
                val points = listOf(
                    Offset(size.width * 0.35f, size.height * 0.4f),
                    Offset(size.width * 0.65f, size.height * 0.3f),
                    Offset(size.width * 0.50f, size.height * 0.7f),
                    Offset(size.width * 0.25f, size.height * 0.75f)
                )

                points.forEach { point ->
                    // Fala
                    drawCircle(
                        color = ElectricVoltGreen.copy(alpha = 0.25f),
                        radius = 18.dp.toPx(),
                        center = point
                    )
                    // Marker
                    drawCircle(
                        color = ElectricVoltGreen,
                        radius = 7.dp.toPx(),
                        center = point
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                color = DarkEnergyBackground.copy(alpha = 0.85f),
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
                            .background(ElectricVoltGreen)
                    )
                    Text(
                        text = "4 aktywne huby kinetyczne w Łodzi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DOSTĘPNE PUNKTY I URZĄDZENIA",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Lista stacji
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(MockData.sampleStations) { station ->
                StationCard(
                    station = station,
                    onStartClick = { onSelectStationToWorkout(station) }
                )
            }
        }
    }
}

@Composable
fun StationCard(
    station: KineticStation,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = station.type.icon,
                    contentDescription = null,
                    tint = ElectricVoltGreen,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Text(
                    text = station.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = ElectricVoltGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = station.distance,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricVoltGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${station.activeUsersCount} os. ćwiczy teraz",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricVoltGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("POŁĄCZ", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
