package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            moveTo(0f, cornerRadius)
            arcTo(
                rect = Rect(0f, 0f, 2 * cornerRadius, 2 * cornerRadius),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

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

            // Prawa krawędź z NOWYM WYCIĘCIEM na środku
            val rightCutoutY = size.height / 2f
            lineTo(size.width, rightCutoutY - cutoutRadius)
            arcTo(
                rect = Rect(
                    left = size.width - cutoutRadius,
                    top = rightCutoutY - cutoutRadius,
                    right = size.width + cutoutRadius,
                    bottom = rightCutoutY + cutoutRadius
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
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
    val splitRatio = 0.72f
    val isDark = isSystemInDarkTheme()

    // Poprawione, solidne kolory zamiast przezroczystych gradientów
    val backgroundColor = if (isAffordableOrActive) {
        MaterialTheme.colorScheme.surface
    } else {
        if (isDark) Color(0xFF13171E) else Color(0xFFF1F5F9)
    }

    val borderColor = if (isAffordableOrActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isAffordableOrActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val textLightColor = if (isAffordableOrActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    val ticketShape = remember { TicketShape(splitRatio = splitRatio) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 4.dp)
            .shadow(
                elevation = if (isAffordableOrActive) 4.dp else 1.dp,
                shape = ticketShape,
                spotColor = if (isAffordableOrActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
            )
            .clip(ticketShape)
            .background(backgroundColor)
            .border(
                width = if (isAffordableOrActive) 1.5.dp else 1.dp,
                color = borderColor,
                shape = ticketShape
            )
            .clickable(enabled = isAffordableOrActive, onClick = onClick)
    ) {
        // Przerywana linia zagięcia
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
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(splitRatio)
                    .padding(start = 20.dp, end = 16.dp, top = 15.dp, bottom = 10.dp)
            ) {
                // Partner (Ikonka + nazwa) na samej górze (5-6dp od krawędzi)
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isAffordableOrActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isAffordableOrActive) MaterialTheme.colorScheme.primary else textLightColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = partner.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = textLightColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Główny tekst (Tytuł i podtytuł) na środku biletu
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = textLightColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isAffordableOrActive) MaterialTheme.colorScheme.primary else textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (valueLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAffordableOrActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Text(
                                text = valueLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAffordableOrActive) MaterialTheme.colorScheme.primary else textLightColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
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
    var viewingCouponCodeModal by remember { mutableStateOf<RedeemedReward?>(null) }
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
                    if (!detailReward.id.contains("RANK")) {
                        activeSubTab = 1
                        SnackbarHelper.show("Kupon dodany do zakładki Moje Kody.")
                    } else {
                        SnackbarHelper.show("+100 pkt rankingu dodane!", icon = Icons.Default.EmojiEvents)
                    }
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
                        color = orangeColor.copy(alpha = 0.15f)
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = orangeColor
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
                                style = MaterialTheme.typography.titleMedium,
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
                                items(redeemedRewards, key = { it.id.ifBlank { "${it.reward.id}_${it.discountCode}" } }) { item ->
                                    val isTransport = item.reward.category == RewardCategory.TRANSPORT
                                    val totalSec = item.reward.validityMinutes * 60
                                    var remainingSeconds by remember(item.isActivated) { mutableStateOf(totalSec) }

                                    LaunchedEffect(item.isActivated) {
                                        if (isTransport && item.isActivated && remainingSeconds > 0) {
                                            while (remainingSeconds > 0) {
                                                delay(1000.milliseconds)
                                                remainingSeconds--
                                            }
                                        }
                                    }
                                    val isExpired = isTransport && item.isActivated && remainingSeconds <= 0

                                    val cardSubtitle = when {
                                        !isTransport -> "KOD: ${item.discountCode} • Kliknij by wyświetlić"
                                        isExpired -> "Bilet wygasł"
                                        item.isActivated -> "KOD: ${item.discountCode}"
                                        else -> "Bilet czasowy • Kliknij by aktywować"
                                    }

                                    val cardMainValue = when {
                                        !isTransport -> if (item.reward.title.contains("-")) item.reward.title.substringBefore(" ").take(4) else "KOD"
                                        isExpired -> "0:00"
                                        item.isActivated -> "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}"
                                        else -> "AKTYWUJ"
                                    }

                                    val cardValueLabel = when {
                                        !isTransport -> "AKTYWNY"
                                        item.isActivated && !isExpired -> "POZOSTAŁO"
                                        !isExpired -> "CZAS"
                                        else -> ""
                                    }

                                    CouponCard(
                                        title = item.reward.title,
                                        partner = item.reward.partner,
                                        subtitle = cardSubtitle,
                                        mainValue = cardMainValue,
                                        valueLabel = cardValueLabel,
                                        isAffordableOrActive = !isExpired,
                                        icon = if (!isTransport) Icons.Default.ConfirmationNumber else if (item.isActivated && !isExpired) Icons.Default.QrCode else item.reward.icon,
                                        onClick = {
                                            if (isTransport && !item.isActivated) {
                                                onActivateReward(item)
                                            } else {
                                                viewingCouponCodeModal = item
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

            if (viewingCouponCodeModal != null) {
                val coupon = viewingCouponCodeModal!!
                AlertDialog(
                    onDismissRequest = { viewingCouponCodeModal = null },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = coupon.reward.partner.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LightPrimary
                            )
                            Text(
                                text = coupon.reward.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, LightPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        text = coupon.discountCode,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp,
                                        color = LightPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Text(
                                text = "Pokaż powyższy kod sprzedawcy przy kasie lub wpisz w aplikacji partnera, aby zrealizować zniżkę.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { viewingCouponCodeModal = null },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                                shape = CircleShape
                            ) {
                                Text("Zamknij", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = null
                )
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
                    Text("${reward.costPoints} VoltPoints", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Czas na wykorzystanie", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${reward.validityMinutes} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun RewardsScreenLightPreview() {
    VoltUpTheme(AppThemeMode.LIGHT) {
        Surface(color = MaterialTheme.colorScheme.background) {
            RewardsScreen(
                currentPoints = 3500,
                redeemedRewards = emptyList(),
                onRedeemReward = {}
            )
        }
    }
}

@Preview
@Composable
fun RewardsScreenDarkPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        Surface(color = MaterialTheme.colorScheme.background) {
            RewardsScreen(
                currentPoints = 3500,
                redeemedRewards = listOf(
                    RedeemedReward(
                        reward = MockData.sampleRewards.first(),
                        discountCode = "MPK-KINETIC-20",
                        redeemedDate = "Dzisiaj",
                        isActivated = true,
                        validityMinutes = 20
                    )
                ),
                onRedeemReward = {}
            )
        }
    }
}

@Preview
@Composable
fun CouponCardPreview() {
    VoltUpTheme(AppThemeMode.DARK) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            CouponCard(
                title = "Darmowy Bilet 20-minutowy",
                partner = "MPK Łódź",
                subtitle = "Tramwaje i autobusy w strefie 1 Łodzi",
                mainValue = "1500",
                valueLabel = "PKT",
                isAffordableOrActive = true,
                icon = Icons.Default.DirectionsBus,
                onClick = {}
            )
        }
    }
}