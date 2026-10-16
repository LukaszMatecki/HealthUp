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
import androidx.compose.ui.draw.shadow
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
    private val cornerRadius: Float = 32f,
    private val cutoutRadius: Float = 24f,
    private val splitRatio: Float = 0.7f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val splitX = size.width * splitRatio

            // Lewy górny róg
            moveTo(cornerRadius, 0f)

            // Górna krawędź do wycięcia
            lineTo(splitX - cutoutRadius, 0f)
            // Górne wycięcie
            arcTo(
                rect = Rect(
                    left = splitX - cutoutRadius,
                    top = -cutoutRadius,
                    right = splitX + cutoutRadius,
                    bottom = cutoutRadius
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            // Górna krawędź od wycięcia do prawego górnego rogu
            lineTo(size.width - cornerRadius, 0f)

            // Prawy górny róg
            arcTo(
                rect = Rect(size.width - 2 * cornerRadius, 0f, size.width, 2 * cornerRadius),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Prawa krawędź
            lineTo(size.width, size.height - cornerRadius)

            // Prawy dolny róg
            arcTo(
                rect = Rect(size.width - 2 * cornerRadius, size.height - 2 * cornerRadius, size.width, size.height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Dolna krawędź do wycięcia
            lineTo(splitX + cutoutRadius, size.height)
            // Dolne wycięcie
            arcTo(
                rect = Rect(
                    left = splitX - cutoutRadius,
                    top = size.height - cutoutRadius,
                    right = splitX + cutoutRadius,
                    bottom = size.height + cutoutRadius
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            // Dolna krawędź od wycięcia do lewego dolnego rogu
            lineTo(cornerRadius, size.height)

            // Lewy dolny róg
            arcTo(
                rect = Rect(0f, size.height - 2 * cornerRadius, 2 * cornerRadius, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Lewa krawędź
            lineTo(0f, cornerRadius)

            // Zakończenie lewego górnego rogu
            arcTo(
                rect = Rect(0f, 0f, 2 * cornerRadius, 2 * cornerRadius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        return Outline.Generic(path)
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
    val splitRatio = 0.72f // Przesunięto delikatnie w prawo dla lepszych proporcji

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val ticketBrush = remember(isAffordableOrActive, isDark, surfaceVariant) {
        if (isAffordableOrActive) {
            if (isDark) {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF222838),
                        Color(0xFF141824),
                        Color(0xFF1C2234)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF1F5F9),
                        Color(0xFFE2E8F0)
                    )
                )
            }
        } else {
            Brush.linearGradient(
                colors = listOf(
                    surfaceVariant.copy(alpha = 0.25f),
                    surfaceVariant.copy(alpha = 0.10f)
                )
            )
        }
    }

    val textColor = if (isAffordableOrActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val textLightColor = textColor.copy(alpha = 0.7f)

    val ticketShape = remember { TicketShape(splitRatio = splitRatio) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 4.dp)
            .shadow(
                elevation = if (isAffordableOrActive) 6.dp else 1.dp,
                shape = ticketShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(ticketShape)
            .background(ticketBrush)
            .clickable(enabled = isAffordableOrActive, onClick = onClick)
    ) {
        // Zintegrowana linia przerywana pod spodem - idealnie spasowana z wycięciami
        Canvas(modifier = Modifier.fillMaxSize()) {
            val splitX = size.width * splitRatio
            drawLine(
                color = textLightColor.copy(alpha = 0.3f),
                start = Offset(splitX, 16.dp.toPx()),
                end = Offset(splitX, size.height - 16.dp.toPx()),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Lewa sekcja (Informacje)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(splitRatio)
                    .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = partner.uppercase(),
                        color = textLightColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        lineHeight = 22.sp
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            color = textLightColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Prawa sekcja (Wartość / Akcja)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mainValue.isNotEmpty()) {
                    Text(
                        text = mainValue,
                        color = if (isAffordableOrActive) MaterialTheme.colorScheme.primary else textColor,
                        fontSize = if (mainValue.length > 5) 18.sp else 26.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    if (valueLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAffordableOrActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                        ) {
                            Text(
                                text = valueLabel,
                                color = textLightColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
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
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nagrody ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "VoltUp",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    val orangeColor = Color(0xFFFF9800)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = orangeColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, orangeColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = orangeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = currentPoints.toString(),
                                fontWeight = FontWeight.ExtraBold,
                                color = orangeColor,
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
