package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class UserStats(
    val points: Int = 1240,
    val totalEnergyWh: Double = 3450.0,
    val weeklyEnergyWh: Double = 840.0,
    val cityContributionRank: Int = 42,
    val co2SavedKg: Double = 2.41,
    val userName: String = "Jan Kowalski",
    val avatarId: Int = 1,
    val level: Int = 4,
    val levelTitle: String = "Łódzki Aktywista Kinetyczny",
    val streakDays: Int = 5,
    val recentWorkouts: List<WorkoutSessionRecord> = emptyList(),
)

data class KineticStation(
    val id: String,
    val name: String,
    val location: String,
    val type: DeviceType,
    val currentEnergyRateW: Int = 0,
    val distance: String = "500 m",
    val activeUsersCount: Int = 2,
    val qrCodeId: String = "LDZ-VOLT-QR-001",
    val isFavorite: Boolean = false,
    val district: String = "Polesie",
    val latitude: Double = 51.7592,
    val longitude: Double = 19.4560,
    val mapCoordinatesX: Float = 0.5f,
    val mapCoordinatesY: Float = 0.5f,
    val isOnline: Boolean = true,
)

data class WorkoutSessionRecord(
    val id: String,
    val stationName: String,
    val stationLocation: String,
    val dateText: String,
    val durationSeconds: Int,
    val generatedWh: Double,
    val earnedPoints: Int,
    val co2SavedKg: Double,
    val caloriesKcal: Int,
)

enum class DeviceType(val displayName: String) {
    BIKE("Rower Kinetyczny"),
    STEPPER("Stepper Miejski"),
    CROSS_TRAINER("Orbitrek Parkowy"),
    SWING("Huśtawka Dynamiczna");

    val icon: ImageVector
        get() = when (this) {
            BIKE -> Icons.AutoMirrored.Filled.DirectionsBike
            STEPPER -> Icons.AutoMirrored.Filled.DirectionsWalk
            CROSS_TRAINER -> Icons.Default.FitnessCenter
            SWING -> Icons.Default.Attractions
        }
}

data class CityGoal(
    val title: String,
    val targetKWh: Double,
    val currentKWh: Double,
    val description: String,
)

enum class RewardCategory(val displayName: String) {
    ALL("Wszystkie"),
    TRANSPORT("Transport MPK"),
    CULTURE("Kultura i EC1"),
    LOCAL("Kawiarnie i Gastro")
}

data class Reward(
    val id: String,
    val title: String,
    val partner: String,
    val costPoints: Int,
    val category: RewardCategory,
    val description: String,
    val discountCode: String = "LODZ-VOLT-2025",
    val validityMinutes: Int = 20,
    val expiresDays: Int = 30,
) {
    val icon: ImageVector
        get() = when (category) {
            RewardCategory.TRANSPORT -> if (id.contains("ROD")) Icons.AutoMirrored.Filled.DirectionsBike else Icons.Default.DirectionsBus
            RewardCategory.CULTURE -> if (id.contains("FALA")) Icons.Default.Pool else Icons.Default.Museum
            RewardCategory.LOCAL -> Icons.Default.LocalCafe
            RewardCategory.ALL -> Icons.Default.ConfirmationNumber
        }
}

data class RedeemedReward(
    val reward: Reward,
    val discountCode: String,
    val redeemedDate: String,
    val isActivated: Boolean = false,
    val activatedTimestamp: Long = 0L,
    val validityMinutes: Int = 20,
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val currentProgress: Float, // 0.0f .. 1.0f
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
)

data class DistrictRank(
    val rank: Int,
    val name: String,
    val totalEnergyKWh: Double,
    val isUserDistrict: Boolean = false,
)

data class UserBadge(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val icon: ImageVector = Icons.Default.EmojiEvents,
    val progress: Float = 1.0f,
    val unlockedDate: String = "12 Lut",
)

typealias Badge = UserBadge

enum class AppThemeMode(val displayName: String, val description: String) {
    SYSTEM("Systemowy", "Dostosowuje się automatycznie do ustawień urządzenia"),
    DARK("Ciemny VoltUp", "Oszczędza baterię, wysoki kontrast i zielone akcenty"),
    LIGHT("Jasny Eko", "Klasyczny jasny wygląd z kontrastowym tekstem");

    val title: String get() = displayName
    val subtitle: String get() = description
}

data class AppSettings(
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val autoPauseWhenIdle: Boolean = true,
    val dailyReminders: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String = "",
    val body: String = "",
    val timeAgo: String = "",
    val timestampText: String = "",
    val isRead: Boolean = false,
    val icon: ImageVector = Icons.Default.Notifications,
)
