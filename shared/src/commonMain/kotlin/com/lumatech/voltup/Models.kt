package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class UserStats(
    val points: Int = 1240,
    val totalEnergyWh: Double = 3450.0,
    val cityContributionRank: Int = 42,
    val co2SavedKg: Double = 2.41
)

data class KineticStation(
    val id: String,
    val name: String,
    val location: String,
    val type: DeviceType,
    val currentEnergyRateW: Int = 0,
    val distance: String = "500 m",
    val activeUsersCount: Int = 2,
    val qrCodeId: String = "LDZ-VOLT-QR-001"
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
    val description: String
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
    val discountCode: String = "LODZ-VOLT-2025"
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
    val redeemedDate: String
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
