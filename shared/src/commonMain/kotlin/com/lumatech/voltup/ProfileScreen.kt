package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(
    stats: UserStats,
    challenges: List<Challenge>,
    badges: List<Badge> = MockData.sampleBadges,
    onClaimChallenge: (Challenge) -> Unit,
    onUpdateUserProfile: (newName: String, newAvatarId: Int) -> Unit,
    onOpenSettings: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAllBadgesModal by remember { mutableStateOf(false) }
    var showLeaderboardModal by remember { mutableStateOf(false) }

    val districtLeaderboard = remember {
        listOf(
            DistrictRank(1, "Śródmieście & Piotrkowska", 1340.5, isUserDistrict = false),
            DistrictRank(2, "Polesie", 1210.2, isUserDistrict = true),
            DistrictRank(3, "Widzew", 980.0, isUserDistrict = false),
            DistrictRank(4, "Bałuty", 890.4, isUserDistrict = false),
            DistrictRank(5, "Górna", 720.8, isUserDistrict = false)
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EnergyYellow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Seria: ${stats.streakDays} dni",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = EnergyYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ustawienia",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarIcon = when (stats.avatarId) {
                            1 -> Icons.Default.DirectionsBike
                            2 -> Icons.Default.ElectricBolt
                            3 -> Icons.Default.Eco
                            else -> Icons.Default.Person
                        }
                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = "Avatar",
                            tint = primaryColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stats.userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Poziom ${stats.level} • ${stats.levelTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edytuj dane", fontSize = 12.sp)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatNumber(
                            value = "#${stats.cityContributionRank}",
                            label = "Ranking Łodzi",
                            valueColor = primaryColor
                        )
                        ProfileStatNumber(
                            value = "${(stats.totalEnergyWh / 1000.0).formatOneDecimal()} kWh",
                            label = "Suma Energii",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        ProfileStatNumber(
                            value = "${stats.co2SavedKg.formatOneDecimal()} kg",
                            label = "CO2 redukcja",
                            valueColor = primaryColor
                        )
                    }
                }
            }
        }

        // Achievements Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAllBadgesModal = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EnergyYellow.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = EnergyYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Osiągnięcia i Odznaki",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${badges.count { it.isUnlocked }} z ${badges.size} odblokowane",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Zobacz ➔",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
        }

        // Leaderboard Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLeaderboardModal = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Ranking Dzielnic Łodzi",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Polesie: 2. miejsce",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Tabela ➔",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
        }

        // Weekly Challenges Header
        item {
            Text(
                text = "WYZWANIA TYGODNIOWE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        // Animated disappearing challenges on claim!
        val activeChallenges = challenges.filter { !it.isClaimed }
        if (activeChallenges.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Wszystkie wyzwania na ten tydzień ukończone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activeChallenges, key = { it.id }) { challenge ->
                AnimatedVisibility(
                    visible = !challenge.isClaimed,
                    exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut() + shrinkVertically()
                ) {
                    ModernChallengeCard(
                        challenge = challenge,
                        onClaimClick = { onClaimChallenge(challenge) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal Wszystkich Osiągnięć (Modern)
    if (showAllBadgesModal) {
        AlertDialog(
            onDismissRequest = { showAllBadgesModal = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Osiągnięcia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(badges) { badge ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (badge.isUnlocked) EnergyYellow.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = badge.icon,
                                        contentDescription = null,
                                        tint = if (badge.isUnlocked) EnergyYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(badge.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(badge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (badge.isUnlocked) {
                                        Text("Odblokowano", fontSize = 11.sp, color = primaryColor, fontWeight = FontWeight.SemiBold)
                                    } else {
                                        Text("Postęp: ${(badge.progress * 100).toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAllBadgesModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zamknij")
                }
            }
        )
    }

    // Modal Rankingu Dzielnic (Modern)
    if (showLeaderboardModal) {
        AlertDialog(
            onDismissRequest = { showLeaderboardModal = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Ranking Dzielnic Łodzi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    districtLeaderboard.forEach { district ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (district.isUserDistrict) primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${district.rank}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (district.rank == 1) EnergyYellow else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = district.name,
                                        fontWeight = if (district.isUserDistrict) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (district.isUserDistrict) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${district.totalEnergyKWh} kWh",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLeaderboardModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zamknij")
                }
            }
        )
    }

    // Modal Edycji Danych (Imię, Hasło, Awatar)
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(stats.userName) }
        var newPassword by remember { mutableStateOf("") }
        var editAvatar by remember { mutableStateOf(stats.avatarId) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Edytuj dane profilu", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Imię") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nowe hasło (opcjonalnie)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text("Wybierz ikonę profilu:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(0 to Icons.Default.Person, 1 to Icons.Default.DirectionsBike, 2 to Icons.Default.ElectricBolt, 3 to Icons.Default.Eco).forEach { (id, icon) ->
                            val isSel = editAvatar == id
                            IconButton(
                                onClick = { editAvatar = id },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = if (isSel) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUserProfile(editName, editAvatar)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zapisz", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun ModernChallengeCard(
    challenge: Challenge,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EnergyYellow.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+${challenge.rewardPoints} pkt",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = EnergyYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { challenge.currentProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (challenge.isCompleted) {
                    Button(
                        onClick = onClaimClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Odbierz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "${(challenge.currentProgress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatNumber(
    value: String,
    label: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        ProfileScreen(
            stats = UserStats(),
            challenges = MockData.sampleChallenges,
            badges = MockData.sampleBadges,
            onClaimChallenge = {},
            onUpdateUserProfile = { _, _ -> },
            onOpenSettings = {}
        )
    }
}
