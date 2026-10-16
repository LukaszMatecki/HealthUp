package com.lumatech.voltup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class UserRank(
    val rank: Int,
    val name: String,
    val district: String,
    val totalKWh: Double,
    val points: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun RankingScreen(
    userStats: UserStats,
    onNavigateBack: () -> Unit
) {
    val userLeaderboard = remember {
        listOf(
            UserRank(1, "Jan Kowalski", "Śródmieście", 2450.0, 18500),
            UserRank(2, "Anna Nowak", "Polesie", 2120.0, 16200),
            UserRank(3, "Piotr Wiśniewski", "Widzew", 1890.0, 14100),
            UserRank(4, "${userStats.userName} (Ty)", "Polesie", 1340.5, userStats.points, isCurrentUser = true),
            UserRank(5, "Michał Zieliński", "Bałuty", 1100.0, 8900),
            UserRank(6, "Katarzyna Wójcik", "Górna", 950.0, 7400),
            UserRank(7, "Tomasz Kamiński", "Śródmieście", 820.0, 6100),
            UserRank(8, "Magdalena Lewandowska", "Polesie", 760.0, 5800)
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    CommonBackHandler(enabled = true) {
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Wróć",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Ranking Ogólny",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    PodiumCard(userRank = userLeaderboard[1], heightDp = 110, rankColor = Color(0xFFC0C0C0))
                    PodiumCard(userRank = userLeaderboard[0], heightDp = 140, rankColor = EnergyYellow)
                    PodiumCard(userRank = userLeaderboard[2], heightDp = 95, rankColor = Color(0xFFCD7F32))
                }
            }

            item {
                Text(
                    text = "LIDERZY ENERGII ŁODZI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            items(userLeaderboard) { user ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (user.isCurrentUser) primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (user.isCurrentUser) primaryColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${user.rank}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = when (user.rank) {
                                    1 -> EnergyYellow
                                    2 -> Color(0xFF9E9E9E)
                                    3 -> Color(0xFFCD7F32)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Column {
                                Text(
                                    text = user.name,
                                    fontWeight = if (user.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = if (user.isCurrentUser) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = user.district,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${user.totalKWh} kWh",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumCard(
    userRank: UserRank,
    heightDp: Int,
    rankColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = rankColor.copy(alpha = 0.2f),
            border = BorderStroke(2.dp, rankColor)
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${userRank.rank}",
                    fontWeight = FontWeight.ExtraBold,
                    color = rankColor,
                    fontSize = 16.sp
                )
            }
        }

        Text(
            text = userRank.name.split(" ").firstOrNull() ?: userRank.name,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            modifier = Modifier
                .width(80.dp)
                .height(heightDp.dp),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = rankColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, rankColor.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${userRank.totalKWh.toInt()} kWh",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
