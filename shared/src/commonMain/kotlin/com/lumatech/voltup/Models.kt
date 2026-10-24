package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.roundToInt

data class UserStats(
    val points: Int = 0,
    val voltPoints: Int = 0,
    val rankingPoints: Int = 0,
    val totalEnergyWh: Double = 0.0,
    val weeklyEnergyWh: Double = 0.0,
    val cityContributionRank: Int = 0,
    val co2SavedKg: Double = 0.0,
    val userName: String = "Jan Kowalski",
    val avatarId: Int = 1,
    val level: Int = 0,
    val levelTitle: String = "Nowicjusz VoltUp",
    val streakDays: Int = 1,
    val recentWorkouts: List<WorkoutSessionRecord> = emptyList(),
    val accountCreatedAt: Long = 0L,
    val totalCaloriesBurned: Int = 0,
    val activeCardioMinutes: Int = 0,
) {
    val effectiveCalories: Int
        get() = if (totalCaloriesBurned > 0) totalCaloriesBurned else {
            val fromWorkouts = recentWorkouts.sumOf { it.caloriesKcal }
            if (fromWorkouts > 0) fromWorkouts else (totalEnergyWh * 0.7).roundToInt()
        }

    val effectiveActiveMinutes: Int
        get() = if (activeCardioMinutes > 0) activeCardioMinutes else {
            val fromWorkouts = recentWorkouts.sumOf { it.durationSeconds } / 60
            if (fromWorkouts > 0) fromWorkouts else (totalEnergyWh * 0.12).roundToInt()
        }
}

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
    val maxPowerW: Int = 150,
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
    val timestampMs: Long = 0L,
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

    fun getLocalizedName(strings: VoltUpStrings): String = when (this) {
        BIKE -> strings.deviceBike
        STEPPER -> strings.deviceStepper
        CROSS_TRAINER -> strings.deviceCrossTrainer
        SWING -> strings.deviceSwing
    }
}

fun KineticStation.getLocalizedName(strings: VoltUpStrings): String = type.getLocalizedName(strings)

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
    LOCAL("Kawiarnie i Gastro");

    fun getDisplayName(strings: VoltUpStrings): String = when (this) {
        ALL -> strings.categoryAll
        TRANSPORT -> strings.categoryTransport
        CULTURE -> strings.categoryEntertainment
        LOCAL -> strings.categoryFood
    }
}

data class Reward(
    val id: String,
    val title: String,
    val partner: String,
    val costPoints: Int,
    val category: RewardCategory,
    val description: String,
    val discountCode: String = "LODZ-VOLT-2025",
    val validityMinutes: Int = 0,
    val expiresDays: Int = 30,
) {
    val icon: ImageVector
        get() = if (id.contains("RANK")) {
            Icons.Default.Leaderboard
        } else when (category) {
            RewardCategory.TRANSPORT -> if (id.contains("ROD")) Icons.AutoMirrored.Filled.DirectionsBike else Icons.Default.DirectionsBus
            RewardCategory.CULTURE -> if (id.contains("FALA")) Icons.Default.Pool else Icons.Default.Museum
            RewardCategory.LOCAL -> Icons.Default.LocalCafe
            RewardCategory.ALL -> Icons.Default.ConfirmationNumber
        }

    fun getLocalizedTitle(isEnglish: Boolean): String = if (!isEnglish) title else when (id) {
        "REW-RANK-100" -> "+100 Leaderboard Points"
        "REW-MPK-01" -> "Free 20-minute Ticket"
        "REW-EC1-02" -> "-50% for CNiT Exhibition"
        "REW-ROD-03" -> "30 minutes free bike ride"
        "REW-PIOTR-04" -> "Free Espresso with breakfast"
        "REW-FALA-05" -> "-30% Aquapark Fala ticket"
        "REW-MANU-06" -> "Cinema City Movie Ticket for 15 PLN"
        "REW-VEGE-07" -> "-20% for Healthy Lunch"
        else -> title
    }

    fun getLocalizedDescription(isEnglish: Boolean): String = if (!isEnglish) description else when (id) {
        "REW-RANK-100" -> "Leaderboard booster. Exchange 50 VoltPoints for an instant +100 ranking points on the leaderboard!"
        "REW-MPK-01" -> "Single-ride ticket for all tram and bus routes across zone 1 in Łódź."
        "REW-EC1-02" -> "50% discount on admission to the Science and Technology Centre in the revitalized historic power plant."
        "REW-ROD-03" -> "Free minutes for the Łódź Public Bike system across the entire city."
        "REW-PIOTR-04" -> "Get a complimentary espresso or americano with any breakfast or dessert order."
        "REW-FALA-05" -> "Discount on a 2-hour entry ticket to the pool zone, slides, and saunarium."
        "REW-MANU-06" -> "Discount voucher for any 2D movie screening from Monday to Thursday at Cinema City Manufaktura."
        "REW-VEGE-07" -> "Discount on the entire vegan and vegetarian menu prepared with local eco ingredients."
        else -> description
    }
}

data class RedeemedReward(
    val id: String = "",
    val reward: Reward,
    val discountCode: String,
    val redeemedDate: String,
    val isReadyToScan: Boolean = false,
    val isActivated: Boolean = false,
    val activatedTimestamp: Long = 0L,
    val validityMinutes: Int = 0,
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val details: String = "",
    val targetValue: Double = 100.0,
    val currentValue: Double = 0.0,
    val unit: String = "Wh",
    val rankingPointsReward: Int = 100,
    val voltPointsReward: Int = 5,
    val currentProgress: Float = 0.0f, // 0.0f .. 1.0f
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
) {
    val rewardPoints: Int get() = voltPointsReward

    fun getLocalizedTitle(isEnglish: Boolean): String = if (!isEnglish) title else when (id) {
        "C1" -> "Heart in Motion"
        "C2" -> "Healthy Habit"
        "C3" -> "Energy Pulse"
        else -> title
    }

    fun getLocalizedDescription(isEnglish: Boolean): String = if (!isEnglish) description else when (id) {
        "C1" -> "Burn a total of 250 kcal in cardio sessions at park stations in Łódź"
        "C2" -> "Complete 3 cardio sessions this week on your way from work or university"
        "C3" -> "Reach a momentary power of at least 80 W on a park kinetic station"
        else -> description
    }

    fun getLocalizedDetails(isEnglish: Boolean): String = if (!isEnglish) details else when (id) {
        "C1" -> "Every park session strengthens your heart, improves blood circulation, and brings you closer to a free MPK transit ticket!"
        "C2" -> "Visit outdoor stations in any park (e.g. Poniatowski Park, Park na Zdrowiu) and build a lasting habit of daily movement."
        "C3" -> "Power your movement: reach 80 W during training and earn leaderboard ranking points!"
        else -> details
    }

    fun getLocalizedUnit(isEnglish: Boolean): String = if (!isEnglish) unit else when (unit) {
        "sesji" -> "sessions"
        else -> unit
    }
}



enum class AppThemeMode(val displayName: String, val description: String) {
    SYSTEM("Systemowy", "Dostosowuje się automatycznie do ustawień urządzenia"),
    DARK("Ciemny VoltUp", "Oszczędza baterię, wysoki kontrast i zielone akcenty"),
    LIGHT("Jasny Eko", "Klasyczny jasny wygląd z kontrastowym tekstem");

    val title: String get() = displayName
    val subtitle: String get() = description
}

enum class AppLanguage(val code: String, val displayName: String) {
    POLISH("pl", "Polski (PL)"),
    ENGLISH("en", "English (EN)");

    val title: String get() = displayName
}

data class AppSettings(
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val autoPauseWhenIdle: Boolean = true,
    val dailyReminders: Boolean = true,
    val keepScreenOn: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.POLISH,
)

fun formatEnergySmart(wh: Double): String {
    return if (wh >= 1000.0) {
        "${(wh / 1000.0).formatOneDecimal()} kWh"
    } else if (wh > 0.0 && wh < 10.0) {
        "${wh.formatOneDecimal()} Wh"
    } else {
        "${wh.roundToInt()} Wh"
    }
}

fun formatCo2Smart(co2Kg: Double): String {
    return if (co2Kg >= 1.0) {
        "${co2Kg.formatOneDecimal()} kg"
    } else {
        val grams = co2Kg * 1000.0
        if (grams > 0.0 && grams < 10.0) {
            "${grams.formatOneDecimal()} g"
        } else {
            "${grams.roundToInt()} g"
        }
    }
}

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
