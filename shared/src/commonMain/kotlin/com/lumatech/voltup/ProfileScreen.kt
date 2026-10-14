package com.lumatech.voltup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    stats: UserStats,
    challenges: List<Challenge>,
    onClaimChallenge: (Challenge) -> Unit
) {
    val districtLeaderboard = remember {
        listOf(
            DistrictRank(1, "Śródmieście & Piotrkowska", 1240.5),
            DistrictRank(2, "Polesie (Park na Zdrowiu)", 1110.2, isUserDistrict = true),
            DistrictRank(3, "Widzew & Księży Młyn", 980.0),
            DistrictRank(4, "Bałuty & Manufaktura", 890.4),
            DistrictRank(5, "Górna & Stawy Stefańskiego", 720.8)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Nagłówek profilu
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ElectricVoltGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = ElectricVoltGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Jan Kowalski",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = EnergyYellow.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = EnergyYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Poziom 4: Łódzki Aktywista Kinetyczny",
                                color = EnergyYellow,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "#${stats.cityContributionRank}",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = ElectricVoltGreen
                            )
                            Text(
                                text = "Miejsce w Łodzi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(stats.totalEnergyWh / 1000.0).formatOneDecimal()} kWh",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Suma Energii",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Polesie",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Twoja Dzielnica",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Tygodniowe Wyzwania
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WYZWANIA TYGODNIOWE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = EnergyYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Zbieraj VoltPoints",
                            style = MaterialTheme.typography.labelSmall,
                            color = EnergyYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                challenges.forEach { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onClaimClick = { onClaimChallenge(challenge) }
                    )
                }
            }
        }

        // Liga Dzielnic Łodzi
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIGA DZIELNIC ŁODZI",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = ElectricVoltGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Luty 2025",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricVoltGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        districtLeaderboard.forEach { district ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (district.isUserDistrict) ElectricVoltGreen.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${district.rank}",
                                        fontWeight = FontWeight.Black,
                                        color = if (district.rank == 1) EnergyYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = district.name,
                                        fontWeight = if (district.isUserDistrict) FontWeight.Bold else FontWeight.Medium,
                                        color = if (district.isUserDistrict) ElectricVoltGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${district.totalEnergyKWh} kWh",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (challenge.isCompleted) Icons.Default.TaskAlt else Icons.Default.Star,
                        contentDescription = null,
                        tint = if (challenge.isCompleted) ElectricVoltGreen else EnergyYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = challenge.title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (challenge.isClaimed) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "ODEBRANE ✓",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else if (challenge.isCompleted) {
                    Button(
                        onClick = onClaimClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricVoltGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ODBIERZ +${challenge.rewardPoints} PKT",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "+${challenge.rewardPoints} pkt",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = EnergyYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { challenge.currentProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ElectricVoltGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
