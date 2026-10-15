package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
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
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Główna karta profilu (Clean UI)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pasek narzędzi karty (Seria i Ustawienia)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EnergyYellow.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = EnergyYellow, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${stats.streakDays} dni",
                                    color = EnergyYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ustawienia",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Awatar i nazwa
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarIcon = when (stats.avatarId) {
                            1 -> Icons.AutoMirrored.Filled.DirectionsBike
                            2 -> Icons.Default.ElectricBolt
                            3 -> Icons.Default.Eco
                            else -> Icons.Default.Person
                        }
                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = "Avatar",
                            tint = primaryColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stats.userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Używamy separatora zamiast przełamania wiersza dla czystszego widoku
                        Text(
                            text = "Poziom ${stats.level} • ${stats.levelTitle}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Przycisk edycji "Soft UI"
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showEditProfileDialog = true },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Edytuj dane profilu", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )

                    // Statystyki
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatNumber(
                            value = "#${stats.cityContributionRank}",
                            label = "Ranking",
                            valueColor = primaryColor
                        )
                        ProfileStatNumber(
                            value = "${(stats.totalEnergyWh / 1000.0).formatOneDecimal()} kWh",
                            label = "Wygenerowano",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        ProfileStatNumber(
                            value = "${stats.co2SavedKg.formatOneDecimal()} kg",
                            label = "Mniej CO2",
                            valueColor = primaryColor
                        )
                    }
                }
            }
        }

        // Karta Osiągnięć (Modern Row)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { showAllBadgesModal = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EnergyYellow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = EnergyYellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Osiągnięcia i Odznaki",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${badges.count { badge -> badge.isUnlocked }} z ${badges.size} odblokowane",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Karta Rankingu
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { showLeaderboardModal = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Ranking Dzielnic Łodzi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Polesie: 2. miejsce",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Nagłówek Wyzwań
        item {
            Text(
                text = "WYZWANIA TYGODNIOWE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 8.dp)
            )
        }

        // Wyzwania Tygodniowe
        val activeChallenges = challenges.filter { !it.isClaimed }
        if (activeChallenges.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Wszystkie wyzwania na ten tydzień ukończone. Dobra robota!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Modal Osiągnięć
    if (showAllBadgesModal) {
        AlertDialog(
            onDismissRequest = { showAllBadgesModal = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Osiągnięcia", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(badges) { badge ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (badge.isUnlocked) EnergyYellow.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, if (badge.isUnlocked) EnergyYellow.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (badge.isUnlocked) EnergyYellow.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = badge.icon,
                                        contentDescription = null,
                                        tint = if (badge.isUnlocked) EnergyYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(badge.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(badge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (badge.isUnlocked) {
                                        Text("Odblokowano", fontSize = 11.sp, color = EnergyYellow, fontWeight = FontWeight.Bold)
                                    } else {
                                        // Naprawiona matematyka (zamiast uciętego operatora)
                                        Text("Postęp: ${(badge.progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = CircleShape
                ) {
                    Text("Zamknij", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }

    // Modal Rankingu Dzielnic
    if (showLeaderboardModal) {
        AlertDialog(
            onDismissRequest = { showLeaderboardModal = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Ranking Dzielnic Łodzi", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    districtLeaderboard.forEach { district ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (district.isUserDistrict) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (district.isUserDistrict) primaryColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${district.rank}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (district.rank == 1) EnergyYellow else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = district.name,
                                        fontWeight = if (district.isUserDistrict) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (district.isUserDistrict) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${district.totalEnergyKWh} kWh",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
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
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = CircleShape
                ) {
                    Text("Zamknij", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }

    // Modal Edycji
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(stats.userName) }
        var newPassword by remember { mutableStateOf("") }
        var editAvatar by remember { mutableStateOf(stats.avatarId) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Edytuj profil", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Imię") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nowe hasło (opcjonalnie)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Wybierz ikonę profilu", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(0 to Icons.Default.Person, 1 to Icons.Default.DirectionsBike, 2 to Icons.Default.ElectricBolt, 3 to Icons.Default.Eco).forEach { (id, icon) ->
                            val isSel = editAvatar == id
                            IconButton(
                                onClick = { editAvatar = id },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
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
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = CircleShape
                ) {
                    Text("Zapisz zmiany", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Anuluj", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

// Czysty, nowoczesny komponent wyzwania
@Composable
fun ModernChallengeCard(
    challenge: Challenge,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = CircleShape,
                    color = EnergyYellow.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+${challenge.rewardPoints} pkt",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = EnergyYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Własny, płynnie zaokrąglony pasek postępu (zamiast kanciastego LinearProgressIndicator)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(challenge.currentProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                if (challenge.isCompleted) {
                    Button(
                        onClick = onClaimClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Odbierz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Naprawiona matematyka (zamiast uciętego operatora)
                    Text(
                        text = "${(challenge.currentProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Czysty komponent liczbowy w głównym nagłówku
@Composable
private fun ProfileStatNumber(
    value: String,
    label: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
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