package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
fun RewardsScreen(
    currentPoints: Int,
    redeemedRewards: List<RedeemedReward>,
    onRedeemReward: (Reward) -> Unit,
    onActivateReward: (RedeemedReward) -> Unit = {}
) {
    var activeSubTab by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf(RewardCategory.ALL) }
    var selectedRewardDetail by remember { mutableStateOf<Reward?>(null) }

    val filteredRewards = remember(selectedCategory) {
        MockData.sampleRewards.filter { reward ->
            selectedCategory == RewardCategory.ALL || reward.category == selectedCategory
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    AnimatedContent(
        targetState = selectedRewardDetail,
        transitionSpec = {
            slideInVertically { it } + fadeIn() togetherWith
                    slideOutVertically { -it } + fadeOut()
        }
    ) { detailReward ->
        if (detailReward != null) {
            RewardDetailScreen(
                reward = detailReward,
                currentPoints = currentPoints,
                onRedeem = {
                    onRedeemReward(detailReward)
                    selectedRewardDetail = null
                    activeSubTab = 1
                    SnackbarHelper.show("Kod dodany do Moje Kody.")
                },
                onBack = { selectedRewardDetail = null }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
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
                        shape = RoundedCornerShape(20.dp),
                        color = primaryColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                                text = "$currentPoints",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    listOf("Katalog" to 0, "Moje Kody" to 1).forEach { (label, idx) ->
                        val isActive = activeSubTab == idx
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeSubTab = idx }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (idx == 1 && redeemedRewards.isNotEmpty()) "$label (${redeemedRewards.size})" else label,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(2.dp)
                                    .background(
                                        if (isActive) primaryColor else Color.Transparent,
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(12.dp))

                if (activeSubTab == 0) {
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
                                        category.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.Black,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = if (!isSelected) FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ) else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredRewards, key = { it.id }) { reward ->
                            val canAfford = currentPoints >= reward.costPoints
                            RewardCard(
                                reward = reward,
                                canAfford = canAfford,
                                onClick = { selectedRewardDetail = reward }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                } else {
                    if (redeemedRewards.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Brak aktywnych kodów",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Odbierz nagrodę z katalogu,\naby aktywować ją w pojeździe.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(redeemedRewards, key = { it.discountCode }) { item ->
                                TicketVoucherCard(
                                    redeemed = item,
                                    onActivateClick = { onActivateReward(item) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RewardCard(
    reward: Reward,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (canAfford) primaryColor.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reward.icon,
                    contentDescription = null,
                    tint = if (canAfford) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.partner.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reward.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${reward.costPoints}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (canAfford) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "pkt",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RewardDetailScreen(
    reward: Reward,
    currentPoints: Int,
    onRedeem: () -> Unit,
    onBack: () -> Unit
) {
    val canAfford = currentPoints >= reward.costPoints
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Wróć",
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Szczegóły nagrody",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = reward.icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = reward.partner.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Koszt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${reward.costPoints} VoltPoints",
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ważność po aktywacji",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${reward.validityMinutes} min",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Po odebraniu kod trafi do zakładki Moje Kody. Czas ważności rozpocznie się po aktywacji w pojeździe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onRedeem,
            enabled = canAfford,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (canAfford) "Odbierz za ${reward.costPoints} pkt" else "Za mało punktów",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TicketVoucherCard(
    redeemed: RedeemedReward,
    onActivateClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var remainingSeconds by remember(redeemed.activatedTimestamp) {
        val elapsedSec = if (redeemed.isActivated && redeemed.activatedTimestamp != null) {
            ((System.currentTimeMillis() - redeemed.activatedTimestamp) / 1000).toInt()
        } else 0
        val totalSec = redeemed.validityMinutes * 60
        mutableStateOf((totalSec - elapsedSec).coerceAtLeast(0))
    }

    LaunchedEffect(redeemed.isActivated, redeemed.activatedTimestamp) {
        if (redeemed.isActivated && remainingSeconds > 0) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
        }
    }

    val isExpired = redeemed.isActivated && remainingSeconds <= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (redeemed.isActivated && !isExpired) primaryColor.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        redeemed.reward.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        redeemed.reward.partner,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isExpired -> MaterialTheme.colorScheme.surfaceVariant
                        redeemed.isActivated -> primaryColor.copy(alpha = 0.12f)
                        else -> EnergyYellow.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = when {
                            isExpired -> "Wygasły"
                            redeemed.isActivated -> "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"
                            else -> "Do aktywacji"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = when {
                            isExpired -> MaterialTheme.colorScheme.onSurfaceVariant
                            redeemed.isActivated -> primaryColor
                            else -> EnergyYellow
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dashed separator
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            ) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }

            if (redeemed.isActivated && !isExpired) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(6.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cells = 7
                            val cellSize = size.width / cells
                            val pattern = listOf(
                                true, true, true, false, true, true, true,
                                true, false, true, false, true, false, true,
                                true, true, true, true, true, true, true,
                                false, false, true, false, true, false, false,
                                true, true, true, false, true, true, true,
                                true, false, true, true, true, false, true,
                                true, true, true, false, true, true, true
                            )
                            pattern.forEachIndexed { i, filled ->
                                if (filled) {
                                    val r = i / cells
                                    val c = i % cells
                                    drawRect(
                                        Color.Black,
                                        Offset(c * cellSize, r * cellSize),
                                        Size(cellSize - 1f, cellSize - 1f)
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "KOD KONTROLI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            redeemed.discountCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Aktywny",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor
                        )
                    }
                }
            } else if (!redeemed.isActivated) {
                Button(
                    onClick = onActivateClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Aktywuj w pojeździe",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
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
