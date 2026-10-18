package com.lumatech.voltup

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AchievementTab(val label: String) {
    ACHIEVEMENTS("Osiągnięcia"),
    LEADERBOARD("Ranking")
}

enum class LeaderboardFilter(val limit: Int, val label: String) {
    TOP_10(10, "TOP 10"),
    TOP_50(50, "TOP 50")
}

data class BadgeModel(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val level: Int,
    val currentProgress: Int,
    val maxProgress: Int,
    val pointsAwarded: Int
)

data class LeaderboardUser(
    val id: String,
    val name: String,
    val points: Int,
    val level: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun AchievementsScreen(
    badges: List<BadgeModel> = emptyList(),
    userStats: UserStats,
    onNavigateBack: () -> Unit,
    dbService: VoltUpDatabaseService = remember { getDatabaseService() }
) {
    val strings = strings
    var selectedTab by remember { mutableStateOf(AchievementTab.ACHIEVEMENTS) }
    var selectedFilter by remember { mutableStateOf(LeaderboardFilter.TOP_10) }
    var leaderboardUsers by remember { mutableStateOf<List<LeaderboardUser>>(emptyList()) }
    var isLoadingLeaderboard by remember { mutableStateOf(true) }

    val effectiveUserPoints = userStats.rankingPoints

    LaunchedEffect(selectedTab, selectedFilter, effectiveUserPoints, userStats.level, userStats.userName) {
        isLoadingLeaderboard = true
        try {
            val list = dbService.getLeaderboard(limit = selectedFilter.limit)
            val hasMe = list.any { it.isCurrentUser || it.name.startsWith(userStats.userName) }
            val processed = if (!hasMe) {
                (list + LeaderboardUser("me", "${userStats.userName} ${strings.youLabel}", effectiveUserPoints, userStats.level, isCurrentUser = true))
                    .sortedByDescending { it.points }
            } else {
                list.map {
                    if (it.isCurrentUser || it.name.startsWith(userStats.userName)) {
                        it.copy(name = "${userStats.userName} ${strings.youLabel}", points = effectiveUserPoints, level = userStats.level, isCurrentUser = true)
                    } else it
                }.sortedByDescending { it.points }
            }
            leaderboardUsers = processed
        } catch (_: Exception) {
            leaderboardUsers = listOf(
                LeaderboardUser("me", "${userStats.userName} ${strings.youLabel}", effectiveUserPoints, userStats.level, isCurrentUser = true)
            )
        } finally {
            isLoadingLeaderboard = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopBar(onNavigateBack)

        TabSelector(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { if (targetState == AchievementTab.LEADERBOARD) it / 6 else -it / 6 })
                    .togetherWith(fadeOut(tween(150)) + slideOutHorizontally(tween(200)) { if (targetState == AchievementTab.LEADERBOARD) -it / 6 else it / 6 })
            },
            label = "AchievementTabTransition"
        ) { tab ->
            when (tab) {
                AchievementTab.ACHIEVEMENTS -> BadgesContent(badges)
                AchievementTab.LEADERBOARD -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LeaderboardFilterPill(
                            selectedFilter = selectedFilter,
                            onFilterSelected = { selectedFilter = it }
                        )
                        LeaderboardContent(
                            leaderboard = leaderboardUsers,
                            limit = selectedFilter.limit,
                            userStats = userStats,
                            effectivePoints = effectiveUserPoints
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    val strings = strings
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = strings.back,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = strings.myAchievementsTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TabSelector(
    selectedTab: AchievementTab,
    onTabSelected: (AchievementTab) -> Unit
) {
    val strings = strings
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AchievementTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val tabLabel = when (tab) {
                AchievementTab.ACHIEVEMENTS -> strings.tabAchievements
                AchievementTab.LEADERBOARD -> strings.tabLeaderboard
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabLabel,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun BadgesContent(badges: List<BadgeModel>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(badges, key = { it.id }) { badge ->
            AchievementCard(badge)
        }
    }
}

@Composable
private fun AchievementCard(badge: BadgeModel) {
    val strings = strings
    val isLocked = badge.level == 0
    val isDark = isAppInDarkTheme()
    val tierColor = getAchievementTierColor(badge.level, isDark)

    val accentColor = tierColor.primary
    val iconBgColor = tierColor.container
    val borderColor = tierColor.border
    val textColor = if (isLocked) tierColor.text else MaterialTheme.colorScheme.onSurface
    val iconVector = if (isLocked) Icons.AutoMirrored.Filled.HelpOutline else badge.icon

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else borderColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLocked) strings.badgeLocked else strings.badgeLevel(badge.level),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textColor
                    )

                    if (!isLocked) {
                        Text(
                            text = strings.badgePointsAwarded(badge.pointsAwarded),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = accentColor
                        )
                    }
                }

                Text(
                    text = badge.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val progressFraction = if (badge.maxProgress > 0) {
                        (badge.currentProgress.toFloat() / badge.maxProgress.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${badge.currentProgress}/${badge.maxProgress}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardFilterPill(
    selectedFilter: LeaderboardFilter,
    onFilterSelected: (LeaderboardFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LeaderboardFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onFilterSelected(filter) }
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.label,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardContent(
    leaderboard: List<LeaderboardUser>,
    limit: Int,
    userStats: UserStats,
    effectivePoints: Int
) {
    val strings = strings
    val displayList = leaderboard.take(limit)
    val currentUser = leaderboard.find { it.isCurrentUser }
    val myRank = leaderboard.indexOfFirst { it.isCurrentUser } + 1
    val isMeInDisplayList = displayList.any { it.isCurrentUser }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userStats.userName.take(1).uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = strings.levelPoints(effectivePoints),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${userStats.userName} ${strings.youLabel} • ${strings.levelLabel(userStats.level)} • ${strings.rankPosition(if (myRank > 0) myRank.toString() else "-")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        itemsIndexed(displayList, key = { index, user -> "${user.id}_$index" }) { index, user ->
            LeaderboardItem(rank = index + 1, user = user)
        }

        if (!isMeInDisplayList && myRank > 0) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.rankOutsideTop(limit),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LeaderboardItem(
                        rank = myRank,
                        user = currentUser ?: LeaderboardUser("me", "${userStats.userName} ${strings.youLabel}", effectivePoints, userStats.level, isCurrentUser = true)
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItem(rank: Int, user: LeaderboardUser) {
    val strings = strings
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (user.isCurrentUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (user.isCurrentUser) 2.dp else 1.dp,
            color = if (user.isCurrentUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (rank <= 3) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = strings.placeRank(rank),
                        tint = rankColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "$rank",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = rankColor
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.levelLabel(user.level),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = strings.levelPoints(user.points),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (user.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
