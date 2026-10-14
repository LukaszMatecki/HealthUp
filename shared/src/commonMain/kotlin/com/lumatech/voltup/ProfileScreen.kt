package com.lumatech.voltup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
    badges: List<Badge> = MockData.sampleBadges,
    onClaimChallenge: (Challenge) -> Unit,
    onUpdateUserProfile: (newName: String, newDistrict: String, newAvatarId: Int) -> Unit,
    onOpenSettings: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var selectedBadgeForDialog by remember { mutableStateOf<Badge?>(null) }
    var leaderboardTab by remember { mutableStateOf(0) } // 0 = Monthly, 1 = All-time

    val districtLeaderboard = remember(stats.userDistrict) {
        listOf(
            DistrictRank(1, "Śródmieście & Piotrkowska", 1340.5, isUserDistrict = stats.userDistrict.contains("Śródmieście")),
            DistrictRank(2, "Polesie (Park na Zdrowiu)", 1210.2, isUserDistrict = stats.userDistrict.contains("Polesie")),
            DistrictRank(3, "Widzew & Księży Młyn", 980.0, isUserDistrict = stats.userDistrict.contains("Widzew")),
            DistrictRank(4, "Bałuty & Manufaktura", 890.4, isUserDistrict = stats.userDistrict.contains("Bałuty")),
            DistrictRank(5, "Górna & Stawy Stefańskiego", 720.8, isUserDistrict = stats.userDistrict.contains("Górna"))
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EnergyYellow.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = EnergyYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Seria: ${stats.streakDays} dni 🔥",
                                    color = EnergyYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ustawienia",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.2f)),
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
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stats.userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stats.userDistrict,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                                text = "Poziom ${stats.level}: ${stats.levelTitle}",
                                color = EnergyYellow,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edytuj dane profilu", fontSize = 12.sp)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatNumber(
                            value = "#${stats.cityContributionRank}",
                            label = "Miejsce w Łodzi",
                            valueColor = primaryColor
                        )
                        ProfileStatNumber(
                            value = "${(stats.totalEnergyWh / 1000.0).formatOneDecimal()} kWh",
                            label = "Suma Energii",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        ProfileStatNumber(
                            value = "${stats.co2SavedKg.formatOneDecimal()} kg",
                            label = "Oszczędzone CO₂",
                            valueColor = primaryColor
                        )
                    }
                }
            }
        }

        // Odznaki i Osiągnięcia (Badges)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ODZNAKI I OSIĄGNIĘCIA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${badges.count { it.isUnlocked }} z ${badges.size} odblokowane",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(badges) { badge ->
                        BadgeItem(
                            badge = badge,
                            onClick = { selectedBadgeForDialog = badge }
                        )
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
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Luty 2025",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
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
                                        if (district.isUserDistrict) primaryColor.copy(alpha = 0.15f)
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
                                    Column {
                                        Text(
                                            text = district.name,
                                            fontWeight = if (district.isUserDistrict) FontWeight.Bold else FontWeight.Medium,
                                            color = if (district.isUserDistrict) primaryColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (district.isUserDistrict) {
                                            Text(
                                                text = "Twoja dzielnica domowa",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = primaryColor
                                            )
                                        }
                                    }
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

        // Historia Ostatnich Treningów
        if (stats.recentWorkouts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "OSTATNIE SESJE TRENINGOWE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    stats.recentWorkouts.forEach { workout ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(workout.stationName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${workout.stationLocation} • ${workout.dateText}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("+${workout.earnedPoints} pkt", fontWeight = FontWeight.Black, color = EnergyYellow, fontSize = 14.sp)
                                    Text("${workout.generatedWh.formatOneDecimal()} Wh", style = MaterialTheme.typography.labelSmall, color = primaryColor)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Modal edycji profilu
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(stats.userName) }
        var editDistrict by remember { mutableStateOf(stats.userDistrict) }
        var editAvatar by remember { mutableStateOf(stats.avatarId) }

        val districtOptions = listOf(
            "Polesie (Park na Zdrowiu)",
            "Śródmieście & Piotrkowska",
            "Widzew & Księży Młyn",
            "Bałuty & Manufaktura",
            "Górna & Stawy Stefańskiego"
        )

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edytuj dane profilu", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Twoje imię lub pseudonim") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Wybierz ikonę profilu:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(0 to Icons.Default.Person, 1 to Icons.Default.DirectionsBike, 2 to Icons.Default.ElectricBolt, 3 to Icons.Default.Eco).forEach { (id, icon) ->
                            val isSel = editAvatar == id
                            IconButton(
                                onClick = { editAvatar = id },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSel) primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = if (isSel) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Text("Twoja dzielnica Łodzi:", style = MaterialTheme.typography.labelMedium)
                    districtOptions.forEach { d ->
                        val isSel = editDistrict == d
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { editDistrict = d },
                            color = if (isSel) primaryColor.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Text(
                                text = d,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUserProfile(editName, editDistrict, editAvatar)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black)
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

    // Modal Odznaki
    selectedBadgeForDialog?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadgeForDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (badge.isUnlocked) primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) EnergyYellow else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(badge.title, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(badge.description, style = MaterialTheme.typography.bodyMedium)
                    if (badge.isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "✓ Zdobyto: ${badge.unlockedDate ?: "Niedawno"}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text("Postęp wyzwania: ${(badge.progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = EnergyYellow)
                        LinearProgressIndicator(
                            progress = { badge.progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = EnergyYellow,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedBadgeForDialog = null }) {
                    Text("Zamknij")
                }
            }
        )
    }
}

@Composable
fun BadgeItem(badge: Badge, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (badge.isUnlocked) androidx.compose.foundation.BorderStroke(1.dp, EnergyYellow.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) EnergyYellow.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badge.icon,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) EnergyYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = badge.title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 2,
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileStatNumber(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                        tint = if (challenge.isCompleted) MaterialTheme.colorScheme.primary else EnergyYellow,
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
                            containerColor = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

