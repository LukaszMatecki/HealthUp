package com.lumatech.voltup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun RewardsScreen(
    currentPoints: Int,
    redeemedRewards: List<RedeemedReward>,
    onRedeemReward: (Reward) -> Unit,
    onToggleRewardUsed: (RedeemedReward) -> Unit = {}
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Catalog, 1 = My Redeemed Codes
    var selectedCategory by remember { mutableStateOf(RewardCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRewardForDialog by remember { mutableStateOf<Reward?>(null) }

    val filteredRewards = remember(selectedCategory, searchQuery) {
        MockData.sampleRewards.filter { reward ->
            val matchesCategory = selectedCategory == RewardCategory.ALL || reward.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    reward.title.contains(searchQuery, ignoreCase = true) ||
                    reward.partner.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp)
    ) {
        // Minimalist Header
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "KORZYŚCI MIEJSKIE",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nagrody",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$currentPoints pkt",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subtabs
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.Transparent,
            contentColor = primaryColor,
            modifier = Modifier.padding(horizontal = 20.dp),
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) }
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Katalog", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Moje Kody (${redeemedRewards.size})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeSubTab == 0) {
            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(RewardCategory.entries) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rewards List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRewards) { reward ->
                    val canAfford = currentPoints >= reward.costPoints
                    SleekRewardCard(
                        reward = reward,
                        canAfford = canAfford,
                        onClaimClick = { selectedRewardForDialog = reward }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } else {
            // Moje Bony View
            if (redeemedRewards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Brak odebranych bonów",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Wymień wygenerowane punkty na zniżki MPK i wejściówki EC1 w katalogu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(redeemedRewards) { item ->
                        SleekRedeemedCard(
                            redeemed = item,
                            onCopy = { code ->
                                SnackbarHelper.show("Kod $code skopiowany do schowka!")
                            },
                            onToggleUsed = { onToggleRewardUsed(item) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Modal dialog z odbiorem
    selectedRewardForDialog?.let { reward ->
        val canAfford = currentPoints >= reward.costPoints
        AlertDialog(
            onDismissRequest = { selectedRewardForDialog = null },
            title = {
                Text(reward.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Partner: ${reward.partner}", fontWeight = FontWeight.SemiBold, color = primaryColor)
                    Text(reward.description, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Koszt:", style = MaterialTheme.typography.bodySmall)
                        Text("${reward.costPoints} VoltPoints", fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                    if (!canAfford) {
                        Text(
                            text = "Brakuje Ci ${reward.costPoints - currentPoints} pkt do odbioru tej nagrody.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                if (canAfford) {
                    Button(
                        onClick = {
                            onRedeemReward(reward)
                            selectedRewardForDialog = null
                            activeSubTab = 1
                            SnackbarHelper.show("Odebrano ${reward.title}!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Wymień punkty", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRewardForDialog = null }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun SleekRewardCard(
    reward: Reward,
    canAfford: Boolean,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (canAfford) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reward.icon,
                    contentDescription = null,
                    tint = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.partner.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = reward.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${reward.costPoints} pkt",
                    color = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onClaimClick,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (canAfford) "Odbierz" else "Brak pkt",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SleekRedeemedCard(
    redeemed: RedeemedReward,
    onCopy: (String) -> Unit,
    onToggleUsed: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(redeemed.reward.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(redeemed.reward.partner, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (redeemed.isUsed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (redeemed.isUsed) "Wykorzystany" else "Aktywny",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = if (redeemed.isUsed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Minimalist Barcode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidths = listOf(2f, 4f, 1f, 3f, 5f, 2f, 1f, 4f, 2f, 6f, 3f, 1f, 4f, 2f, 3f, 5f, 1f, 2f, 4f, 1f, 5f, 2f, 3f, 1f, 4f, 2f)
                    var currentX = 0f
                    val totalBarsWidth = barWidths.sum()
                    val scale = size.width / (totalBarsWidth * 1.8f)

                    barWidths.forEach { w ->
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(currentX, 0f),
                            size = androidx.compose.ui.geometry.Size(w * scale, size.height)
                        )
                        currentX += (w * scale) + (3f * scale)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = redeemed.discountCode,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = EnergyYellow
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = { onCopy(redeemed.discountCode) }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Kopiuj", modifier = Modifier.size(16.dp))
                    }
                    TextButton(onClick = onToggleUsed, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(if (redeemed.isUsed) "Przywróć" else "Oznacz zużyty", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RewardsScreenPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        RewardsScreen(
            currentPoints = 1240,
            redeemedRewards = emptyList(),
            onRedeemReward = {}
        )
    }
}
