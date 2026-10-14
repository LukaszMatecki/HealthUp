package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ThemeMode(val title: String, val description: String) {
    CYBER_DARK("Cyber Eko (Ciemny)", "Głęboka zieleń z dynamicznymi akcentami"),
    NEON_CYAN("Neon Cyan", "Chłodny błękit i futurystyczny styl"),
    SOLAR_AMBER("Słoneczny Bursztyn", "Ciepła energia słoneczna i złoto"),
    LIGHT_ECO("Eko Dzień (Jasny)", "Czytelny, jasny motyw parkowy"),
    AMOLED_BLACK("AMOLED True Black", "Maksymalna oszczędność baterii")
}

enum class GridDestination(val title: String, val subtitle: String, val icon: ImageVector) {
    LIGHTING("Oświetlenie Parków", "Latarnie miejskie w Parku na Zdrowiu i Poniatowskim", Icons.Default.Lightbulb),
    BIKE_CHARGING("Stacje Łódzkiego Roweru", "Zasilanie stacji i ładowarek e-rowerów MPK", Icons.Default.ElectricBike),
    SCHOOL_HUBS("Zielone Szkoły Łodzi", "Eko-pracownie i huby edukacyjne", Icons.Default.School)
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
    val themeMode: ThemeMode = ThemeMode.CYBER_DARK,
    val kineticGlowEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val autoPauseWhenIdle: Boolean = true,
    val bluetoothAutoConnect: Boolean = true,
    val targetSessionWh: Int = 100,
    val homeDistrict: String = "Polesie",
    val gridDestination: GridDestination = GridDestination.LIGHTING,
    val dailyReminders: Boolean = true,
    val peakHoursAlerts: Boolean = true,
    val anonymousLeaderboard: Boolean = false,
    val compactCards: Boolean = false
)

data class UserStats(
    val points: Int = 1240,
    val totalEnergyWh: Double = 3450.0,
    val cityContributionRank: Int = 42,
    val co2SavedKg: Double = 2.41,
    val streakDays: Int = 5,
    val userName: String = "Jan Kowalski",
    val userDistrict: String = "Polesie (Park na Zdrowiu)",
    val avatarId: Int = 0,
    val level: Int = 4,
    val levelTitle: String = "Łódzki Aktywista Kinetyczny",
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

data class CityGoal(
    val title: String,
    val targetKWh: Double,
    val currentKWh: Double,
    val description: String,
    val location: String = "Park na Zdrowiu, Łódź"
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
    val isUsed: Boolean = false
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val currentProgress: Float, // 0.0f .. 1.0f
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)

data class DistrictRank(
    val rank: Int,
    val name: String,
    val totalEnergyKWh: Double,
    val isUserDistrict: Boolean = false
)
