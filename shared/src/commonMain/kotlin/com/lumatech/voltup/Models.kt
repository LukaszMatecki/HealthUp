package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppThemeMode(val title: String, val subtitle: String) {
    SYSTEM("Systemowy", "Dostosowuje się do ustawień urządzenia"),
    DARK("Ciemny", "Minimalistyczny ciemny motyw"),
    LIGHT("Jasny", "Przejrzysty jasny motyw")
}

data class WorkoutSessionRecord(
    val id: String,
    val stationName: String,
    val stationLocation: String,
    val dateText: String,
    val durationSeconds: Int,
    val generatedWh: Double,
    val earnedPoints: Int,
    val co2SavedKg: Double,
    val caloriesKcal: Int
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val progress: Float = 1.0f
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val icon: ImageVector = Icons.Default.Bolt
)

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val soundEffectsEnabled: Boolean = true,
    val autoPauseWhenIdle: Boolean = true,
    val dailyReminders: Boolean = true
)

data class UserStats(
    val points: Int = 1240,
    val totalEnergyWh: Double = 3450.0,
    val cityContributionRank: Int = 42,
    val co2SavedKg: Double = 2.41,
    val streakDays: Int = 5,
    val userName: String = "Jan Kowalski",
    val userDistrict: String = "Polesie",
    val avatarId: Int = 0,
    val level: Int = 4,
    val levelTitle: String = "Aktywista Kinetyczny",
    val weeklyEnergyWh: List<Double> = listOf(45.0, 75.0, 60.0, 110.0, 145.0, 85.0, 95.0),
    val recentWorkouts: List<WorkoutSessionRecord> = emptyList()
)

data class KineticStation(
    val id: String,
    val name: String,
    val location: String,
    val district: String = "Polesie",
    val type: DeviceType,
    val currentEnergyRateW: Int = 0,
    val distance: String = "450 m",
    val activeUsersCount: Int = 2,
    val qrCodeId: String = "LDZ-VOLT-QR-001",
    val mapCoordinatesX: Float = 0.5f,
    val mapCoordinatesY: Float = 0.5f,
    val latitude: Double = 51.7592,
    val longitude: Double = 19.4560,
    val isOnline: Boolean = true
)

enum class DeviceType(val displayName: String) {
    BIKE("Rower Kinetyczny"),
    STEPPER("Stepper Miejski"),
    CROSS_TRAINER("Orbitrek Parkowy"),
    SWING("Huśtawka Dynamiczna");

    val icon: ImageVector
        get() = when (this) {
            BIKE -> Icons.Default.DirectionsBike
            STEPPER -> Icons.Default.DirectionsWalk
            CROSS_TRAINER -> Icons.Default.FitnessCenter
            SWING -> Icons.Default.Attractions
        }
}

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
    val expiresDays: Int = 30
) {
    val icon: ImageVector
        get() = when (category) {
            RewardCategory.TRANSPORT -> if (id.contains("ROD")) Icons.Default.DirectionsBike else Icons.Default.DirectionsBus
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
    val activatedTimestamp: Long? = null,
    val validityMinutes: Int = 20,
    val isUsed: Boolean = false
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val currentProgress: Float,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)

data class DistrictRank(
    val rank: Int,
    val name: String,
    val totalEnergyKWh: Double,
    val isUserDistrict: Boolean = false
)
