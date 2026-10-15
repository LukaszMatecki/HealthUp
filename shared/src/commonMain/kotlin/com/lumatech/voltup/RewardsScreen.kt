package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

class TicketShape(
    private val cornerRadius: Float = 40f,
    private val cutoutRadius: Float = 24f,
    private val splitRatio: Float = 0.7f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val r = cornerRadius
            val cutout = cutoutRadius
            val x = size.width * splitRatio

            moveTo(r, 0f)
            lineTo(x - cutout, 0f)
            arcTo(Rect(x - cutout, -cutout, x + cutout, cutout), 180f, -180f, false)
            lineTo(size.width - r, 0f)
            arcTo(Rect(size.width - 2 * r, 0f, size.width, 2 * r), 270f, 90f, false)

            val rightCutout = cutout * 0.8f
            lineTo(size.width, (size.height / 2f) - rightCutout)
            arcTo(Rect(size.width - rightCutout, (size.height / 2f) - rightCutout, size.width + rightCutout, (size.height / 2f) + rightCutout), 270f, -180f, false)

            lineTo(size.width, size.height - r)
            arcTo(Rect(size.width - 2 * r, size.height - 2 * r, size.width, size.height), 0f, 90f, false)
            lineTo(x + cutout, size.height)
            arcTo(Rect(x - cutout, size.height - cutout, x + cutout, size.height + cutout), 0f, -180f, false)
            lineTo(r, size.height)
            arcTo(Rect(0f, size.height - 2 * r, 2 * r, size.height), 90f, 90f, false)
            lineTo(0f, r)
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun RewardsScreen(
    currentPoints: Int,
    redeemedRewards: List<RedeemedReward>,
    onRedeemReward: (Reward) -> Unit,
    onActivateReward: (RedeemedReward) -> Unit = {}
) {
    var activeSubTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(RewardCategory.ALL) }
    var selectedRewardDetail by remember { mutableStateOf<Reward?>(null) }
    var isViewingOwnedReward by remember { mutableStateOf(false) }

    val filteredRewards = remember(selectedCategory) {
        MockData.sampleRewards.filter { reward ->
            selectedCategory == RewardCategory.ALL || reward.category == selectedCategory
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    val pagerState = rememberPagerState(initialPage = activeSubTab) { 2 }

    LaunchedEffect(activeSubTab) {
        if (pagerState.currentPage != activeSubTab) {
            pagerState.animateScrollToPage(activeSubTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (activeSubTab != pagerState.currentPage) {
            activeSubTab = pagerState.currentPage
        }
    }

    AnimatedContent(
        targetState = selectedRewardDetail,
        transitionSpec = {
            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
        }, label = "RewardDetailAnimation"
    ) { detailReward ->
        if (detailReward != null) {
            RewardDetailScreen(
                reward = detailReward,
                currentPoints = currentPoints,
                isOwned = isViewingOwnedReward,
                onRedeem = {
                    onRedeemReward(detailReward)
                    selectedRewardDetail = null
                    activeSubTab = 1
                    SnackbarHelper.show("Kupon dodany do zakładki Moje Kody.")
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
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nagrody VoltUp",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = currentPoints.toString(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    listOf("Katalog" to 0, "Moje Kody" to 1).forEach { (label, idx) ->
                        val isActive = activeSubTab == idx
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeSubTab = idx }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (idx == 1 && redeemedRewards.isNotEmpty()) "$label (${redeemedRewards.size})" else label,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(3.dp)
                                    .background(
                                        if (isActive) primaryColor else Color.Transparent,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    SleekRewardFilterChip(
                                        text = "Wszystkie",
                                        isSelected = selectedCategory == RewardCategory.ALL,
                                        onClick = { selectedCategory = RewardCategory.ALL }
                                    )
                                }
                                items(RewardCategory.entries.filter { it != RewardCategory.ALL }) { category ->
                                    SleekRewardFilterChip(
                                        text = category.displayName,
                                        isSelected = selectedCategory == category,
                                        onClick = { selectedCategory = category }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredRewards, key = { it.id }) { reward ->
                                    val canAfford = currentPoints >= reward.costPoints
                                    CouponCard(
                                        title = reward.title,
                                        partner = reward.partner,
                                        subtitle = "",
                                        mainValue = "${reward.costPoints}",
                                        valueLabel = "PKT",
                                        isAffordableOrActive = canAfford,
                                        icon = reward.icon,
                                        onClick = {
                                            selectedRewardDetail = reward
                                            isViewingOwnedReward = false
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(100.dp)) }
                            }
                        }
                    } else {
                        if (redeemedRewards.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
                                    Text("Brak aktywnych kuponów", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Odbierz nagrodę z katalogu,\naby mieć do niej dostęp tutaj.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(redeemedRewards, key = { it.discountCode }) { item ->
                                    val totalSec = item.reward.validityMinutes * 60
                                    var remainingSeconds by remember(item.isActivated) { mutableStateOf(totalSec) }

                                    LaunchedEffect(item.isActivated) {
                                        if (item.isActivated && remainingSeconds > 0) {
                                            while (remainingSeconds > 0) {
                                                delay(1000.milliseconds)
                                                remainingSeconds--
                                            }
                                        }
                                    }
                                    val isExpired = item.isActivated && remainingSeconds <= 0

                                    CouponCard(
                                        title = item.reward.title,
                                        partner = item.reward.partner,
                                        subtitle = if (isExpired) "Wygasły" else if (item.isActivated) "KOD: ${item.discountCode}" else "Kliknij na bilet by aktywować",
                                        mainValue = when {
                                            isExpired -> "0:00"
                                            item.isActivated -> "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"
                                            else -> "AKTYWUJ"
                                        },
                                        valueLabel = if (item.isActivated && !isExpired) "POZOSTAŁO" else "",
                                        isAffordableOrActive = !isExpired,
                                        icon = if (item.isActivated && !isExpired) Icons.Default.QrCode else item.reward.icon,
                                        onClick = {
                                            if (!item.isActivated) {
                                                onActivateReward(item)
                                            } else {
                                                selectedRewardDetail = item.reward
                                                isViewingOwnedReward = true
                                            }
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(100.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CouponCard(
    title: String,
    partner: String,
    subtitle: String,
    mainValue: String,
    valueLabel: String,
    isAffordableOrActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val gradientColor1 = if (isAffordableOrActive) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
    val gradientColor2 = if (isAffordableOrActive) Color(0xFFC8E6C9) else Color(0xFFE0E0E0)

    val textColor = Color(0xFF1A1D20)
    val textLightColor = Color(0xFF4A4D50)

    val splitRatio = 0.7f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(TicketShape(splitRatio = splitRatio))
            .background(Brush.linearGradient(listOf(gradientColor1, gradientColor2)))
            .clickable(enabled = isAffordableOrActive, onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(splitRatio)
                    .padding(start = 24.dp, top = 20.dp, bottom = 20.dp, end = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = partner.uppercase(),
                        color = textLightColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            color = textLightColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxHeight().width(2.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxHeight()) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.15f),
                        start = Offset(0f, 16.dp.toPx()),
                        end = Offset(0f, size.height - 16.dp.toPx()),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f),
                        strokeWidth = 4f
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(start = 12.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mainValue.isNotEmpty()) {
                    Text(
                        text = mainValue,
                        color = textColor,
                        fontSize = if (mainValue.length > 5) 16.sp else 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    if (valueLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = valueLabel,
                            color = textLightColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardDetailScreen(reward: Reward, currentPoints: Int, isOwned: Boolean, onRedeem: () -> Unit, onBack: () -> Unit) {
    val canAfford = currentPoints >= reward.costPoints
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Szczegóły kuponu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(primaryColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(reward.icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(32.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(reward.partner.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Text(reward.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(reward.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Koszt", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${reward.costPoints} VoltPoints", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = primaryColor)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Czas na wykorzystanie", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${reward.validityMinutes} min", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = if (isOwned) onBack else onRedeem,
            enabled = isOwned || canAfford,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            shape = CircleShape
        ) {
            Text(
                text = if (isOwned) "Zamknij szczegóły" else if (canAfford) "Odbierz za ${reward.costPoints} pkt" else "Brakuje punktów",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SleekRewardFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
fun RewardsScreenPreview() {
    VoltUpTheme(AppThemeMode.LIGHT) {
        RewardsScreen(currentPoints = 1240, redeemedRewards = emptyList(), onRedeemReward = {})
    }
}
