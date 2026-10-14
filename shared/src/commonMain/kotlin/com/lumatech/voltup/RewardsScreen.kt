package com.lumatech.voltup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun RewardsScreen(
    currentPoints: Int,
    redeemedRewards: List<RedeemedReward>,
    onRedeemReward: (Reward) -> Unit
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Catalog, 1 = My Redeemed Codes
    var selectedCategory by remember { mutableStateOf(RewardCategory.ALL) }
    var selectedRewardForDialog by remember { mutableStateOf<Reward?>(null) }
    var copiedCodeSnackbar by remember { mutableStateOf<String?>(null) }

    val filteredRewards = remember(selectedCategory) {
        if (selectedCategory == RewardCategory.ALL) {
            MockData.sampleRewards
        } else {
            MockData.sampleRewards.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        snackbarHost = {
            copiedCodeSnackbar?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = ElectricVoltGreen,
                    contentColor = Color.Black
                ) {
                    Text(text = msg, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            // Nagłówek
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "KORZYŚCI DLA ŁODZIAN",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricVoltGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Katalog Nagród",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = ElectricVoltGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$currentPoints pkt",
                                fontWeight = FontWeight.Bold,
                                color = ElectricVoltGreen,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Przełącznik: Katalog / Moje Bony
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = Color.Transparent,
                contentColor = ElectricVoltGreen,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Dostępne Nagrody", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Moje Kody (${redeemedRewards.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeSubTab == 0) {
                // Kategorie (Filtry poziome)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(RewardCategory.entries) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricVoltGreen,
                                selectedLabelColor = Color.Black,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lista Nagród
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRewards) { reward ->
                        RewardCard(
                            reward = reward,
                            canAfford = currentPoints >= reward.costPoints,
                            onClaimClick = {
                                selectedRewardForDialog = reward
                            }
                        )
                    }
                }
            } else {
                // Lista Odebranych Kodów
                if (redeemedRewards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Nie masz jeszcze odebranych bonów",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Zbieraj VoltPoints podczas aktywności i odblokowuj bilety MPK, wejściówki do EC1 oraz zniżki w Łodzi!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(redeemedRewards) { redeemed ->
                            RedeemedRewardCard(
                                redeemed = redeemed,
                                onCopyCode = { code ->
                                    copiedCodeSnackbar = "Kod $code został skopiowany do schowka!"
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog z odbiorem nagrody
    selectedRewardForDialog?.let { reward ->
        val canAfford = currentPoints >= reward.costPoints
        AlertDialog(
            onDismissRequest = { selectedRewardForDialog = null },
            confirmButton = {
                if (canAfford) {
                    Button(
                        onClick = {
                            onRedeemReward(reward)
                            selectedRewardForDialog = null
                            activeSubTab = 1 // Switch to My Codes tab!
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricVoltGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("ODBIERZ KOD", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRewardForDialog = null }) {
                    Text("Zamknij", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = reward.icon,
                        contentDescription = null,
                        tint = ElectricVoltGreen
                    )
                    Text(
                        text = reward.title,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Partner: ${reward.partner}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = reward.description)
                    if (canAfford) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Generowany Bilet / Kod:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = reward.discountCode,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = EnergyYellow
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Brakuje Ci jeszcze ${reward.costPoints - currentPoints} VoltPoints! Poćwicz na stacji kinetycznej w Łodzi, aby odblokować.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        )
    }

    LaunchedEffect(copiedCodeSnackbar) {
        if (copiedCodeSnackbar != null) {
            kotlinx.coroutines.delay(3000)
            copiedCodeSnackbar = null
        }
    }
}

@Composable
fun RewardCard(
    reward: Reward,
    canAfford: Boolean,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reward.icon,
                    contentDescription = null,
                    tint = ElectricVoltGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.partner.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reward.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${reward.costPoints} VoltPoints",
                        color = if (canAfford) ElectricVoltGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }

            Button(
                onClick = onClaimClick,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricVoltGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (canAfford) "ODBIERZ" else "BRAK PKT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RedeemedRewardCard(
    redeemed: RedeemedReward,
    onCopyCode: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = redeemed.reward.icon,
                        contentDescription = null,
                        tint = ElectricVoltGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = redeemed.reward.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = redeemed.reward.partner,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricVoltGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "AKTYWNY BIKET",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = ElectricVoltGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surface)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KOD BONU / BILETU",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = redeemed.discountCode,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = EnergyYellow
                    )
                }

                IconButton(
                    onClick = { onCopyCode(redeemed.discountCode) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = ElectricVoltGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Kopiuj kod"
                    )
                }
            }
        }
    }
}
