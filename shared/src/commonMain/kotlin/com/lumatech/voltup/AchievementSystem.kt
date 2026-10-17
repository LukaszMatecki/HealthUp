package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Achievement(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val metricType: MetricType,
    val level: Int = 0,
    val currentProgress: Double = 0.0
) {
    enum class MetricType {
        WORKOUTS_COUNT,
        TOTAL_ENERGY_WH,
        MAX_WATTS,
        CO2_SAVED_G,
        STREAK_DAYS,
        REDEEMED_REWARDS,
        TOTAL_CALORIES_KCAL,
        TOTAL_MINUTES,
        SINGLE_SESSION_MAX_WH,
        NIGHT_WORKOUTS,
        DISTINCT_STATIONS
    }

    fun getLevelConfig(targetLevel: Int): Triple<Double, Int, String> {
        val lvl = if (targetLevel <= 0) 1 else targetLevel
        return when (metricType) {
            MetricType.WORKOUTS_COUNT -> when (lvl) {
                1 -> Triple(1.0, 50, "Ukończ 1 trening na stacji kinetycznej")
                2 -> Triple(5.0, 100, "Ukończ 5 treningów na stacjach kinetycznych")
                3 -> Triple(15.0, 250, "Ukończ 15 treningów na stacjach kinetycznych")
                else -> Triple(15.0 + (lvl - 3) * 15.0, 500, "Ukończ ${15 + (lvl - 3) * 15} treningów")
            }
            MetricType.TOTAL_ENERGY_WH -> when (lvl) {
                1 -> Triple(50.0, 50, "Wygeneruj łącznie 50 Wh energii")
                2 -> Triple(200.0, 100, "Wygeneruj łącznie 200 Wh energii")
                3 -> Triple(1000.0, 250, "Wygeneruj łącznie 1 000 Wh energii")
                else -> Triple(1000.0 + (lvl - 3) * 2000.0, 500, "Wygeneruj ${1 + (lvl - 3) * 2} kWh energii")
            }
            MetricType.MAX_WATTS -> when (lvl) {
                1 -> Triple(50.0, 50, "Osiągnij moc chwilową 50 Watów")
                2 -> Triple(80.0, 100, "Osiągnij moc chwilową 80 Watów")
                3 -> Triple(120.0, 200, "Osiągnij moc chwilową 120 Watów")
                else -> Triple(120.0 + (lvl - 3) * 30.0, 300, "Osiągnij moc ${(120 + (lvl - 3) * 30)} W")
            }
            MetricType.CO2_SAVED_G -> when (lvl) {
                1 -> Triple(100.0, 50, "Zaoszczędź 100 g emisji CO₂")
                2 -> Triple(500.0, 150, "Zaoszczędź 500 g emisji CO₂")
                3 -> Triple(2000.0, 300, "Zaoszczędź 2 000 g emisji CO₂")
                else -> Triple(2000.0 + (lvl - 3) * 2000.0, 500, "Zaoszczędź ${(2 + (lvl - 3) * 2)} kg CO₂")
            }
            MetricType.STREAK_DAYS -> when (lvl) {
                1 -> Triple(2.0, 50, "Utrzymaj serię 2 dni treningowych")
                2 -> Triple(5.0, 100, "Utrzymaj serię 5 dni treningowych")
                else -> Triple(5.0 + (lvl - 2) * 5.0, 250, "Utrzymaj serię ${5 + (lvl - 2) * 5} dni")
            }
            MetricType.REDEEMED_REWARDS -> when (lvl) {
                1 -> Triple(1.0, 50, "Odbierz 1 kupon w sklepie VoltUp")
                2 -> Triple(3.0, 150, "Odbierz 3 kupony w sklepie VoltUp")
                else -> Triple(3.0 + (lvl - 2) * 5.0, 300, "Odbierz ${3 + (lvl - 2) * 5} kuponów")
            }
            MetricType.TOTAL_CALORIES_KCAL -> when (lvl) {
                1 -> Triple(50.0, 40, "Spal łącznie 50 kcal na stacjach kinetycznych")
                2 -> Triple(250.0, 100, "Spal łącznie 250 kcal na stacjach kinetycznych")
                3 -> Triple(1000.0, 250, "Spal łącznie 1 000 kcal na stacjach kinetycznych")
                else -> Triple(1000.0 + (lvl - 3) * 1500.0, 500, "Spal ${1000 + (lvl - 3) * 1500} kcal")
            }
            MetricType.TOTAL_MINUTES -> when (lvl) {
                1 -> Triple(10.0, 40, "Trenuj łącznie przez 10 minut na stacjach")
                2 -> Triple(30.0, 100, "Trenuj łącznie przez 30 minut na stacjach")
                3 -> Triple(120.0, 250, "Trenuj łącznie przez 2 godziny na stacjach")
                else -> Triple(120.0 + (lvl - 3) * 180.0, 500, "Trenuj łącznie przez ${2 + (lvl - 3) * 3} godzin")
            }
            MetricType.SINGLE_SESSION_MAX_WH -> when (lvl) {
                1 -> Triple(25.0, 50, "Wygeneruj 25 Wh podczas jednej pojedynczej sesji")
                2 -> Triple(75.0, 150, "Wygeneruj 75 Wh podczas jednej pojedynczej sesji")
                3 -> Triple(200.0, 350, "Wygeneruj 200 Wh podczas jednej sesji")
                else -> Triple(200.0 + (lvl - 3) * 200.0, 600, "Wygeneruj ${200 + (lvl - 3) * 200} Wh w jednej sesji")
            }
            MetricType.NIGHT_WORKOUTS -> when (lvl) {
                1 -> Triple(1.0, 50, "Ukończ 1 wieczorny trening (po 19:00 lub przed 7:00)")
                2 -> Triple(3.0, 120, "Ukończ 3 wieczorne treningi kinetyczne")
                3 -> Triple(10.0, 300, "Ukończ 10 wieczornych treningów w Łodzi")
                else -> Triple(10.0 + (lvl - 3) * 10.0, 500, "Ukończ ${10 + (lvl - 3) * 10} wieczornych sesji")
            }
            MetricType.DISTINCT_STATIONS -> when (lvl) {
                1 -> Triple(2.0, 60, "Odwiedź i trenuj na 2 różnych stacjach miejskich")
                2 -> Triple(4.0, 150, "Trenuj na 4 różnych stacjach kinetycznych")
                3 -> Triple(8.0, 350, "Trenuj na 8 różnych stacjach w Łodzi")
                else -> Triple(8.0 + (lvl - 3) * 4.0, 500, "Odwiedź ${8 + (lvl - 3) * 4} różnych stacji")
            }
        }
    }

    fun toBadgeModel(): BadgeModel {
        val nextConfig = getLevelConfig(level + 1)
        val reqVal = nextConfig.first.toInt()
        val currVal = currentProgress.toInt().coerceAtMost(reqVal)
        return BadgeModel(
            id = id,
            title = title,
            description = nextConfig.third,
            icon = icon,
            level = level,
            currentProgress = currVal,
            maxProgress = reqVal,
            pointsAwarded = nextConfig.second
        )
    }
}

object DefaultAchievements {
    val initialAchievements = listOf(
        Achievement(
            id = "ACH_WORKOUTS",
            title = "Pierwszy Impuls",
            icon = Icons.Default.Bolt,
            metricType = Achievement.MetricType.WORKOUTS_COUNT,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_CALORIES",
            title = "Spalacz Kalorii",
            icon = Icons.Default.Whatshot,
            metricType = Achievement.MetricType.TOTAL_CALORIES_KCAL,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_ENERGY",
            title = "Generator Parkowy",
            icon = Icons.Default.ElectricBolt,
            metricType = Achievement.MetricType.TOTAL_ENERGY_WH,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_DURATION",
            title = "Czasowy Tytan",
            icon = Icons.Default.Timer,
            metricType = Achievement.MetricType.TOTAL_MINUTES,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_WATTS",
            title = "Turbogenerator",
            icon = Icons.Default.Speed,
            metricType = Achievement.MetricType.MAX_WATTS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_SINGLE_WH",
            title = "Siła Jednej Sesji",
            icon = Icons.Default.FitnessCenter,
            metricType = Achievement.MetricType.SINGLE_SESSION_MAX_WH,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_CO2",
            title = "Zielony Obrońca",
            icon = Icons.Default.Eco,
            metricType = Achievement.MetricType.CO2_SAVED_G,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_STREAK",
            title = "Strażnik Aktywności",
            icon = Icons.Default.LocalFireDepartment,
            metricType = Achievement.MetricType.STREAK_DAYS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_EXPLORER",
            title = "Miejski Odkrywca",
            icon = Icons.Default.Explore,
            metricType = Achievement.MetricType.DISTINCT_STATIONS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_NIGHT",
            title = "Nocny Strażnik",
            icon = Icons.Default.Bedtime,
            metricType = Achievement.MetricType.NIGHT_WORKOUTS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_REWARDS",
            title = "Łowca Okazji",
            icon = Icons.Default.CardGiftcard,
            metricType = Achievement.MetricType.REDEEMED_REWARDS,
            level = 0,
            currentProgress = 0.0
        )
    )
}

fun calculateUserLevel(points: Int): Int {
    return when {
        points < 50 -> 0
        points < 150 -> 1
        points < 300 -> 2
        points < 500 -> 3
        points < 1000 -> 4
        points < 2000 -> 5
        else -> 5 + ((points - 2000) / 1000)
    }
}

fun calculateLevelTitle(level: Int): String {
    return when (level) {
        0 -> "Nowicjusz VoltUp"
        1 -> "Eko-Adept Łódzki"
        2 -> "Początkujący Generator"
        3 -> "Aktywista Parkowy"
        4 -> "Łódzki Aktywista Kinetyczny"
        5 -> "Lider Czystej Energii"
        else -> "Mistrz Sieci Miejskiej"
    }
}

fun getLevelThresholds(level: Int): Pair<Int, Int> {
    return when (level) {
        0 -> Pair(0, 50)
        1 -> Pair(50, 150)
        2 -> Pair(150, 300)
        3 -> Pair(300, 500)
        4 -> Pair(500, 1000)
        5 -> Pair(1000, 2000)
        else -> {
            val base = 2000 + (level - 5) * 1000
            Pair(base, base + 1000)
        }
    }
}

data class AchievementEvaluationResult(
    val updatedAchievements: List<Achievement>,
    val bonusPointsEarned: Int,
    val levelUpMessages: List<String>
)

fun evaluateAchievements(
    currentAchievements: List<Achievement>,
    workouts: List<WorkoutSessionRecord>,
    totalEnergyWh: Double,
    co2SavedKg: Double,
    streakDays: Int,
    redeemedRewardsCount: Int
): AchievementEvaluationResult {
    var bonusPointsEarned = 0
    val messages = mutableListOf<String>()

    val updated = currentAchievements.map { ach ->
        val rawMetric = when (ach.metricType) {
            Achievement.MetricType.WORKOUTS_COUNT -> workouts.size.toDouble()
            Achievement.MetricType.TOTAL_ENERGY_WH -> totalEnergyWh
            Achievement.MetricType.MAX_WATTS -> workouts.maxOfOrNull {
                (it.generatedWh * 3600.0 / it.durationSeconds.coerceAtLeast(1))
            } ?: 0.0
            Achievement.MetricType.CO2_SAVED_G -> co2SavedKg * 1000.0
            Achievement.MetricType.STREAK_DAYS -> streakDays.toDouble()
            Achievement.MetricType.REDEEMED_REWARDS -> redeemedRewardsCount.toDouble()
            Achievement.MetricType.TOTAL_CALORIES_KCAL -> workouts.sumOf { it.caloriesKcal }.toDouble()
            Achievement.MetricType.TOTAL_MINUTES -> (workouts.sumOf { it.durationSeconds } / 60.0)
            Achievement.MetricType.SINGLE_SESSION_MAX_WH -> workouts.maxOfOrNull { it.generatedWh } ?: 0.0
            Achievement.MetricType.NIGHT_WORKOUTS -> workouts.count {
                it.dateText.contains("19:") || it.dateText.contains("20:") || it.dateText.contains("21:") || it.dateText.contains("22:") || it.dateText.contains("23:") || it.dateText.contains("Noc") || (workouts.size >= 2 && it.id.hashCode() % 2 == 0)
            }.toDouble()
            Achievement.MetricType.DISTINCT_STATIONS -> workouts.map { it.stationName }.filter { it.isNotBlank() }.distinct().size.toDouble()
        }

        var curLvl = ach.level
        var newPoints = 0

        while (true) {
            val nextConfig = ach.getLevelConfig(curLvl + 1)
            val reqVal = nextConfig.first
            if (rawMetric >= reqVal) {
                curLvl++
                newPoints += nextConfig.second
                messages.add("🏆 Osiągnięcie '${ach.title}' podniesione do poziomu $curLvl! (+${nextConfig.second} pkt)")
            } else {
                break
            }
        }

        bonusPointsEarned += newPoints
        ach.copy(level = curLvl, currentProgress = rawMetric)
    }

    return AchievementEvaluationResult(updated, bonusPointsEarned, messages)
}
