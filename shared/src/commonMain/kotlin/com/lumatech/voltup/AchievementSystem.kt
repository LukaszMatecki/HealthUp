package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Achievement(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val metricType: MetricType,
    val level: Int = 0,
    val currentProgress: Double = 0.0,
    val maxLevel: Int = 5
) {
    val isMaxLevel: Boolean
        get() = maxLevel in 1..level

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
        DISTINCT_STATIONS,
        MORNING_WORKOUTS,
        WEEKEND_WORKOUTS,
        FAST_SESSIONS,
        COMMUNITY_DAYS,
        LONG_SESSIONS,
        DISTINCT_DISTRICTS,
        CO2_MILESTONES,
        DEVICE_VARIETY,
        DAILY_DOUBLE,
        EXPLORE_HISTORY,
        DARK_THEME_TOGGLE,
        ABOUT_VIEWED,
        PROFILE_CUSTOM,
        ARCHIVE_VIEWED,
        QR_SCANNER,
        MAP_NAVIGATOR,
        WEEKLY_CHALLENGES,
        FIRST_REWARD,
        ECO_CALCULATOR_HERO
    }

    fun getLocalizedTitle(isEnglish: Boolean = false): String = if (!isEnglish) title else when (metricType) {
        MetricType.WORKOUTS_COUNT -> "First Impulse"
        MetricType.TOTAL_ENERGY_WH -> "Generator"
        MetricType.MAX_WATTS -> "Peak Watt Master"
        MetricType.CO2_SAVED_G -> "Eco Hero"
        MetricType.STREAK_DAYS -> "Streak Master"
        MetricType.REDEEMED_REWARDS -> "Perk Hunter"
        MetricType.TOTAL_CALORIES_KCAL -> "Calorie Crusher"
        MetricType.TOTAL_MINUTES -> "Endurance Champion"
        MetricType.SINGLE_SESSION_MAX_WH -> "Energy Burst"
        MetricType.NIGHT_WORKOUTS -> "Night Owl"
        MetricType.DISTINCT_STATIONS -> "City Explorer"
        MetricType.MORNING_WORKOUTS -> "Early Bird"
        MetricType.WEEKEND_WORKOUTS -> "Weekend Pro"
        MetricType.FAST_SESSIONS -> "Sprint Prodigy"
        MetricType.COMMUNITY_DAYS -> "Community Pillar"
        MetricType.LONG_SESSIONS -> "Marathoner"
        MetricType.DISTINCT_DISTRICTS -> "District Conqueror"
        MetricType.CO2_MILESTONES -> "Eco Hero"
        MetricType.DEVICE_VARIETY -> "All-Round Athlete"
        MetricType.DAILY_DOUBLE -> "Double Power"
        MetricType.EXPLORE_HISTORY -> "History Buff"
        MetricType.DARK_THEME_TOGGLE -> "Dark Mode"
        MetricType.ABOUT_VIEWED -> "Aware"
        MetricType.PROFILE_CUSTOM -> "Custom Style"
        MetricType.ARCHIVE_VIEWED -> "Archivist"
        MetricType.QR_SCANNER -> "QR Scanner"
        MetricType.MAP_NAVIGATOR -> "City Navigator"
        MetricType.WEEKLY_CHALLENGES -> "Mission Brief"
        MetricType.FIRST_REWARD -> "First Voucher"
        MetricType.ECO_CALCULATOR_HERO -> "Green Footprint"
    }

    fun getLevelConfig(targetLevel: Int, isEnglish: Boolean = false): Triple<Double, Int, String> {
        val lvl = if (targetLevel <= 0) 1 else targetLevel
        return when (metricType) {
            MetricType.WORKOUTS_COUNT -> when (lvl) {
                1 -> Triple(1.0, 50, if (isEnglish) "Complete 1 workout on a kinetic station" else "Ukończ 1 trening na stacji kinetycznej")
                2 -> Triple(5.0, 100, if (isEnglish) "Complete 5 workouts on kinetic stations" else "Ukończ 5 treningów na stacjach kinetycznych")
                3 -> Triple(15.0, 250, if (isEnglish) "Complete 15 workouts on kinetic stations" else "Ukończ 15 treningów na stacjach kinetycznych")
                else -> Triple(15.0 + (lvl - 3) * 15.0, 500, if (isEnglish) "Complete ${15 + (lvl - 3) * 15} workouts" else "Ukończ ${15 + (lvl - 3) * 15} treningów")
            }
            MetricType.TOTAL_ENERGY_WH -> when (lvl) {
                1 -> Triple(50.0, 50, if (isEnglish) "Generate a total of 50 Wh of energy" else "Wygeneruj łącznie 50 Wh energii")
                2 -> Triple(200.0, 100, if (isEnglish) "Generate a total of 200 Wh of energy" else "Wygeneruj łącznie 200 Wh energii")
                3 -> Triple(1000.0, 250, if (isEnglish) "Generate a total of 1 000 Wh of energy" else "Wygeneruj łącznie 1 000 Wh energii")
                else -> Triple(1000.0 + (lvl - 3) * 2000.0, 500, if (isEnglish) "Generate ${1 + (lvl - 3) * 2} kWh of energy" else "Wygeneruj ${1 + (lvl - 3) * 2} kWh energii")
            }
            MetricType.MAX_WATTS -> when (lvl) {
                1 -> Triple(50.0, 50, if (isEnglish) "Reach a peak power output of 50 Watts" else "Osiągnij moc chwilową 50 Watów")
                2 -> Triple(80.0, 100, if (isEnglish) "Reach a peak power output of 80 Watts" else "Osiągnij moc chwilową 80 Watów")
                3 -> Triple(120.0, 200, if (isEnglish) "Reach a peak power output of 120 Watts" else "Osiągnij moc chwilową 120 Watów")
                else -> Triple(120.0 + (lvl - 3) * 30.0, 300, if (isEnglish) "Reach peak power of ${(120 + (lvl - 3) * 30)} W" else "Osiągnij moc ${(120 + (lvl - 3) * 30)} W")
            }
            MetricType.CO2_SAVED_G -> when (lvl) {
                1 -> Triple(100.0, 50, if (isEnglish) "Save 100 g of CO₂ emissions" else "Zaoszczędź 100 g emisji CO₂")
                2 -> Triple(500.0, 150, if (isEnglish) "Save 500 g of CO₂ emissions" else "Zaoszczędź 500 g emisji CO₂")
                3 -> Triple(2000.0, 300, if (isEnglish) "Save 2,000 g of CO₂ emissions" else "Zaoszczędź 2 000 g emisji CO₂")
                else -> Triple(2000.0 + (lvl - 3) * 2000.0, 500, if (isEnglish) "Save ${(2 + (lvl - 3) * 2)} kg of CO₂" else "Zaoszczędź ${(2 + (lvl - 3) * 2)} kg CO₂")
            }
            MetricType.STREAK_DAYS -> when (lvl) {
                1 -> Triple(2.0, 50, if (isEnglish) "Maintain a 2-day workout streak" else "Utrzymaj serię 2 dni treningowych")
                2 -> Triple(5.0, 100, if (isEnglish) "Maintain a 5-day workout streak" else "Utrzymaj serię 5 dni treningowych")
                else -> Triple(5.0 + (lvl - 2) * 5.0, 250, if (isEnglish) "Maintain a ${5 + (lvl - 2) * 5}-day streak" else "Utrzymaj serię ${5 + (lvl - 2) * 5} dni")
            }
            MetricType.REDEEMED_REWARDS -> when (lvl) {
                1 -> Triple(1.0, 50, if (isEnglish) "Redeem 1 voucher in the VoltUp store" else "Odbierz 1 kupon w sklepie VoltUp")
                2 -> Triple(3.0, 150, if (isEnglish) "Redeem 3 vouchers in the VoltUp store" else "Odbierz 3 kupony w sklepie VoltUp")
                else -> Triple(3.0 + (lvl - 2) * 5.0, 300, if (isEnglish) "Redeem ${3 + (lvl - 2) * 5} vouchers" else "Odbierz ${3 + (lvl - 2) * 5} kuponów")
            }
            MetricType.TOTAL_CALORIES_KCAL -> when (lvl) {
                1 -> Triple(50.0, 40, if (isEnglish) "Burn 50 kcal on kinetic equipment" else "Spal łącznie 50 kcal na stacjach kinetycznych")
                2 -> Triple(250.0, 100, if (isEnglish) "Burn 250 kcal on kinetic equipment" else "Spal łącznie 250 kcal na stacjach kinetycznych")
                3 -> Triple(1000.0, 250, if (isEnglish) "Burn 1,000 kcal on kinetic equipment" else "Spal łącznie 1 000 kcal na stacjach kinetycznych")
                else -> Triple(1000.0 + (lvl - 3) * 1500.0, 500, if (isEnglish) "Burn ${1000 + (lvl - 3) * 1500} kcal" else "Spal ${1000 + (lvl - 3) * 1500} kcal")
            }
            MetricType.TOTAL_MINUTES -> when (lvl) {
                1 -> Triple(10.0, 40, if (isEnglish) "Train for 10 minutes total on stations" else "Trenuj łącznie przez 10 minut na stacjach")
                2 -> Triple(30.0, 100, if (isEnglish) "Train for 30 minutes total on stations" else "Trenuj łącznie przez 30 minut na stacjach")
                3 -> Triple(120.0, 250, if (isEnglish) "Train for 2 hours total on stations" else "Trenuj łącznie przez 2 godziny na stacjach")
                else -> Triple(120.0 + (lvl - 3) * 180.0, 500, if (isEnglish) "Train for ${2 + (lvl - 3) * 3} hours total" else "Trenuj łącznie przez ${2 + (lvl - 3) * 3} godzin")
            }
            MetricType.SINGLE_SESSION_MAX_WH -> when (lvl) {
                1 -> Triple(25.0, 50, if (isEnglish) "Generate 25 Wh in a single session" else "Wygeneruj 25 Wh podczas jednej pojedynczej sesji")
                2 -> Triple(75.0, 150, if (isEnglish) "Generate 75 Wh in a single session" else "Wygeneruj 75 Wh podczas jednej pojedynczej sesji")
                3 -> Triple(200.0, 350, if (isEnglish) "Generate 200 Wh in a single session" else "Wygeneruj 200 Wh podczas jednej sesji")
                else -> Triple(200.0 + (lvl - 3) * 200.0, 600, if (isEnglish) "Generate ${200 + (lvl - 3) * 200} Wh in one session" else "Wygeneruj ${200 + (lvl - 3) * 200} Wh w jednej sesji")
            }
            MetricType.NIGHT_WORKOUTS -> when (lvl) {
                1 -> Triple(1.0, 50, if (isEnglish) "Complete 1 evening workout (after 7 PM)" else "Ukończ 1 wieczorny trening (po 19:00 lub przed 7:00)")
                2 -> Triple(3.0, 120, if (isEnglish) "Complete 3 evening kinetic workouts" else "Ukończ 3 wieczorne treningi kinetyczne")
                3 -> Triple(10.0, 300, if (isEnglish) "Complete 10 evening workouts in Łódź" else "Ukończ 10 wieczornych treningów w Łodzi")
                else -> Triple(10.0 + (lvl - 3) * 10.0, 500, if (isEnglish) "Complete ${10 + (lvl - 3) * 10} evening sessions" else "Ukończ ${10 + (lvl - 3) * 10} wieczornych sesji")
            }
            MetricType.DISTINCT_STATIONS -> when (lvl) {
                1 -> Triple(2.0, 60, if (isEnglish) "Visit and train at 2 different stations" else "Odwiedź i trenuj na 2 różnych stacjach miejskich")
                2 -> Triple(4.0, 150, if (isEnglish) "Train at 4 different kinetic stations" else "Trenuj na 4 różnych stacjach kinetycznych")
                3 -> Triple(8.0, 350, if (isEnglish) "Train at 8 different stations in Łódź" else "Trenuj na 8 różnych stacjach w Łodzi")
                else -> Triple(8.0 + (lvl - 3) * 4.0, 500, if (isEnglish) "Visit ${8 + (lvl - 3) * 4} different stations" else "Odwiedź ${8 + (lvl - 3) * 4} różnych stacji")
            }
            MetricType.MORNING_WORKOUTS -> when (lvl) {
                1 -> Triple(1.0, 50, if (isEnglish) "Start your day with energy: 1 workout before 9 AM" else "Rozpocznij dzień energią: 1 poranny trening przed 9:00")
                2 -> Triple(3.0, 120, if (isEnglish) "Complete 3 morning workout sessions" else "Ukończ 3 poranne sesje treningowe")
                3 -> Triple(8.0, 280, if (isEnglish) "Complete 8 morning workouts in city parks" else "Ukończ 8 porannych rozgrzewek w łódzkich parkach")
                else -> Triple(8.0 + (lvl - 3) * 8.0, 450, if (isEnglish) "Complete ${8 + (lvl - 3) * 8} morning workouts" else "Ukończ ${8 + (lvl - 3) * 8} porannych treningów")
            }
            MetricType.WEEKEND_WORKOUTS -> when (lvl) {
                1 -> Triple(1.0, 40, if (isEnglish) "Complete 1 weekend workout" else "Ukończ 1 weekendowy trening w sobotę lub niedzielę")
                2 -> Triple(4.0, 120, if (isEnglish) "Complete 4 workouts on weekend days" else "Ukończ 4 treningi w dni weekendowe")
                3 -> Triple(12.0, 300, if (isEnglish) "Complete 12 weekend activities in Łódź" else "Ukończ 12 aktywności w łódzkie weekendy")
                else -> Triple(12.0 + (lvl - 3) * 10.0, 450, if (isEnglish) "Complete ${12 + (lvl - 3) * 10} weekend workouts" else "Ukończ ${12 + (lvl - 3) * 10} treningów weekendowych")
            }
            MetricType.FAST_SESSIONS -> when (lvl) {
                1 -> Triple(1.0, 60, if (isEnglish) "Express surge: generate 10 Wh in under 5 minutes" else "Ekspresowy zryw: wygeneruj 10 Wh w czasie poniżej 5 minut")
                2 -> Triple(3.0, 150, if (isEnglish) "Complete 3 high-intensity express sessions" else "Ukończ 3 ekspresowe sesje wysokiej intensywności")
                else -> Triple(3.0 + (lvl - 2) * 5.0, 300, if (isEnglish) "Complete ${3 + (lvl - 2) * 5} express sessions" else "Ukończ ${3 + (lvl - 2) * 5} sesji ekspresowych")
            }
            MetricType.COMMUNITY_DAYS -> when (lvl) {
                1 -> Triple(1.0, 50, if (isEnglish) "Contribute: support the city community goal for 1 day" else "Dołóż swoją cegiełkę: wesprzyj cel społeczności Łodzi przez 1 dzień")
                2 -> Triple(5.0, 180, if (isEnglish) "Support the community goal for 5 days" else "Wspieraj cel społecznościowy przez 5 dni")
                3 -> Triple(15.0, 400, if (isEnglish) "Support the city community goal for 15 days" else "Wspieraj cel miejski przez 15 dni")
                else -> Triple(15.0 + (lvl - 3) * 15.0, 600, if (isEnglish) "Support the community for ${15 + (lvl - 3) * 15} days" else "Bądź filarem społeczności przez ${15 + (lvl - 3) * 15} dni")
            }
            MetricType.LONG_SESSIONS -> when (lvl) {
                1 -> Triple(1.0, 70, if (isEnglish) "Power marathon: maintain a continuous session for 15+ minutes" else "Maraton mocy: utrzymaj ciągły trening przez ponad 15 minut")
                2 -> Triple(5.0, 200, if (isEnglish) "Complete 5 long-distance kinetic sessions" else "Ukończ 5 długodystansowych sesji kinetycznych")
                else -> Triple(5.0 + (lvl - 2) * 5.0, 400, if (isEnglish) "Complete ${5 + (lvl - 2) * 5} kinetic marathons" else "Ukończ ${5 + (lvl - 2) * 5} maratonów kinetycznych")
            }
            MetricType.DISTINCT_DISTRICTS -> when (lvl) {
                1 -> Triple(2.0, 80, if (isEnglish) "Train in 2 different districts of Łódź" else "Trenuj w 2 różnych dzielnicach Łodzi (np. Polesie, Bałuty)")
                2 -> Triple(3.0, 180, if (isEnglish) "Visit stations in 3 different districts" else "Odwiedź stacje w 3 różnych dzielnicach Łodzi")
                3 -> Triple(5.0, 400, if (isEnglish) "Power all 5 main districts of Łódź!" else "Zasil wszystkie 5 głównych dzielnic Łodzi!")
                else -> Triple(5.0, 500, if (isEnglish) "Master of Łódź city districts" else "Mistrz łódzkich dzielnic")
            }
            MetricType.CO2_MILESTONES -> when (lvl) {
                1 -> Triple(1.0, 100, if (isEnglish) "Eco Hero: reduce a total of 1 kg of CO₂ emissions" else "Eko-Bohater: zredukuj łącznie 1 kg emisji CO₂")
                2 -> Triple(5.0, 250, if (isEnglish) "Reduce a total of 5 kg of CO₂ emissions for Łódź" else "Zredukuj łącznie 5 kg emisji CO₂ dla Łodzi")
                3 -> Triple(15.0, 500, if (isEnglish) "Reduce a total of 15 kg of CO₂ emissions!" else "Zredukuj łącznie 15 kg emisji CO₂!")
                else -> Triple(15.0 + (lvl - 3) * 15.0, 700, if (isEnglish) "Reduce ${15 + (lvl - 3) * 15} kg of CO₂" else "Zredukuj ${15 + (lvl - 3) * 15} kg CO₂")
            }
            MetricType.DEVICE_VARIETY -> when (lvl) {
                1 -> Triple(2.0, 70, if (isEnglish) "Multisport: try out 2 different device types" else "Multisport: wypróbuj 2 różne typy urządzeń (rower, orbitrek, stepper...)")
                2 -> Triple(3.0, 160, if (isEnglish) "Try out 3 different types of kinetic stations" else "Wypróbuj 3 różne typy urządzeń kinetycznych")
                3 -> Triple(4.0, 350, if (isEnglish) "Master all 4 types of urban stations!" else "Opanuj wszystkie 4 rodzaje stacji miejskich!")
                else -> Triple(4.0, 450, if (isEnglish) "Master of all station types" else "Kompletny mistrz wszystkich typów stacji")
            }
            MetricType.DAILY_DOUBLE -> when (lvl) {
                1 -> Triple(1.0, 60, if (isEnglish) "Double charge: complete 2 workouts in a single day" else "Podwójna moc: ukończ 2 treningi w ciągu jednego dnia")
                2 -> Triple(3.0, 150, if (isEnglish) "Do double workouts on 3 different days" else "Zrób podwójny trening w 3 różnych dniach")
                else -> Triple(3.0 + (lvl - 2) * 4.0, 350, if (isEnglish) "Do double workouts on ${3 + (lvl - 2) * 4} days" else "Wykonaj podwójny trening w ${3 + (lvl - 2) * 4} dniach")
            }
            MetricType.EXPLORE_HISTORY -> Triple(1.0, 50, if (isEnglish) "Check your past workout history in the profile tab" else "Sprawdź historię swoich treningów w profilu")
            MetricType.DARK_THEME_TOGGLE -> Triple(1.0, 50, if (isEnglish) "Switch the app theme to dark mode" else "Przełącz motyw aplikacji na ciemny")
            MetricType.ABOUT_VIEWED -> Triple(1.0, 50, if (isEnglish) "Read information in the About VoltUp section" else "Przeczytaj informacje w sekcji O VoltUp")
            MetricType.PROFILE_CUSTOM -> Triple(1.0, 50, if (isEnglish) "Customize your avatar or username in your profile" else "Dostosuj swój awatar lub nazwę użytkownika w profilu")
            MetricType.ARCHIVE_VIEWED -> Triple(1.0, 50, if (isEnglish) "View redeemed vouchers in your ticket archive" else "Przejrzyj wykorzystane bilety w archiwum biletów")
            MetricType.QR_SCANNER -> Triple(1.0, 50, if (isEnglish) "Open QR code scanner to locate a station" else "Otwórz skaner kodów QR, aby namierzyć stację")
            MetricType.MAP_NAVIGATOR -> Triple(1.0, 50, if (isEnglish) "Explore the interactive map of kinetic hubs" else "Przeglądaj interaktywną mapę miejskich stacji")
            MetricType.WEEKLY_CHALLENGES -> Triple(1.0, 50, if (isEnglish) "Review weekly challenges on your dashboard" else "Sprawdź wyzwania tygodniowe na pulpicie głównym")
            MetricType.FIRST_REWARD -> Triple(1.0, 60, if (isEnglish) "Redeem your first discount voucher or transit ticket" else "Odbierz swój pierwszy kupon zniżkowy lub bilet MPK")
            MetricType.ECO_CALCULATOR_HERO -> Triple(1.0, 50, if (isEnglish) "Save at least 50 g of CO₂ emissions for Łódź" else "Zaoszczędź co najmniej 50 g emisji CO₂ dla Łodzi")
        }
    }

    fun toBadgeModel(isEnglish: Boolean = false): BadgeModel {
        val atMax = maxLevel in 1..level
        val nextConfig = getLevelConfig(if (atMax) level else (level + 1), isEnglish)
        val reqVal = nextConfig.first.toInt()
        val currVal = if (atMax) reqVal else currentProgress.toInt().coerceAtMost(reqVal)
        val desc = if (atMax) {
            if (isEnglish) "Completed! Maximum level reached." else "Ukończono! Osiągnięto maksymalny poziom."
        } else {
            nextConfig.third
        }
        return BadgeModel(
            id = id,
            title = getLocalizedTitle(isEnglish),
            description = desc,
            icon = icon,
            level = level,
            currentProgress = currVal,
            maxProgress = reqVal,
            pointsAwarded = nextConfig.second,
            isMaxLevel = atMax
        )
    }
}

data class AchievementTierColor(
    val primary: androidx.compose.ui.graphics.Color,
    val container: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val text: androidx.compose.ui.graphics.Color
)

fun getAchievementTierColor(level: Int, isDark: Boolean = true): AchievementTierColor {
    return when (level) {
        0 -> {
            AchievementTierColor(
                primary = androidx.compose.ui.graphics.Color(0xFF9E9E9E),
                container = if (isDark) androidx.compose.ui.graphics.Color(0xFF1E232B) else androidx.compose.ui.graphics.Color(0xFFF1F5F9),
                border = if (isDark) androidx.compose.ui.graphics.Color(0xFF334155) else androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                text = if (isDark) androidx.compose.ui.graphics.Color(0xFF94A3B8) else androidx.compose.ui.graphics.Color(0xFF64748B)
            )
        }
        1 -> {
            AchievementTierColor(
                primary = androidx.compose.ui.graphics.Color(0xFF22C55E),
                container = if (isDark) androidx.compose.ui.graphics.Color(0xFF0D2818) else androidx.compose.ui.graphics.Color(0xFFDCFCE7),
                border = if (isDark) androidx.compose.ui.graphics.Color(0xFF166534) else androidx.compose.ui.graphics.Color(0xFF86EFAC),
                text = if (isDark) androidx.compose.ui.graphics.Color(0xFF4ADE80) else androidx.compose.ui.graphics.Color(0xFF15803D)
            )
        }
        2 -> {
            AchievementTierColor(
                primary = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                container = if (isDark) androidx.compose.ui.graphics.Color(0xFF0E2246) else androidx.compose.ui.graphics.Color(0xFFDBEAFE),
                border = if (isDark) androidx.compose.ui.graphics.Color(0xFF1D4ED8) else androidx.compose.ui.graphics.Color(0xFF93C5FD),
                text = if (isDark) androidx.compose.ui.graphics.Color(0xFF60A5FA) else androidx.compose.ui.graphics.Color(0xFF1D4ED8)
            )
        }
        3 -> {
            AchievementTierColor(
                primary = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                container = if (isDark) androidx.compose.ui.graphics.Color(0xFF3B2506) else androidx.compose.ui.graphics.Color(0xFFFEF3C7),
                border = if (isDark) androidx.compose.ui.graphics.Color(0xFFB45309) else androidx.compose.ui.graphics.Color(0xFFFCD34D),
                text = if (isDark) androidx.compose.ui.graphics.Color(0xFFFBBF24) else androidx.compose.ui.graphics.Color(0xFFB45309)
            )
        }
        4 -> {
            AchievementTierColor(
                primary = androidx.compose.ui.graphics.Color(0xFFE11D48),
                container = if (isDark) androidx.compose.ui.graphics.Color(0xFF3D0C1A) else androidx.compose.ui.graphics.Color(0xFFFFE4E6),
                border = if (isDark) androidx.compose.ui.graphics.Color(0xFFBE123C) else androidx.compose.ui.graphics.Color(0xFFFDA4AF),
                text = if (isDark) androidx.compose.ui.graphics.Color(0xFFFB7185) else androidx.compose.ui.graphics.Color(0xFFBE123C)
            )
        }
        else -> {
            AchievementTierColor(
                primary = androidx.compose.ui.graphics.Color(0xFFA855F7),
                container = if (isDark) androidx.compose.ui.graphics.Color(0xFF2E1065) else androidx.compose.ui.graphics.Color(0xFFF3E8FF),
                border = if (isDark) androidx.compose.ui.graphics.Color(0xFF7E22CE) else androidx.compose.ui.graphics.Color(0xFFD8B4FE),
                text = if (isDark) androidx.compose.ui.graphics.Color(0xFFC084FC) else androidx.compose.ui.graphics.Color(0xFF7E22CE)
            )
        }
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
            title = "Generator",
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
        ),
        Achievement(
            id = "ACH_MORNING",
            title = "Poranny Błysk",
            icon = Icons.Default.WbSunny,
            metricType = Achievement.MetricType.MORNING_WORKOUTS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_WEEKEND",
            title = "Weekendowiec",
            icon = Icons.Default.DateRange,
            metricType = Achievement.MetricType.WEEKEND_WORKOUTS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_FAST",
            title = "Ekspres Kinetyczny",
            icon = Icons.Default.FlashOn,
            metricType = Achievement.MetricType.FAST_SESSIONS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_COMMUNITY",
            title = "Filar Społeczności",
            icon = Icons.Default.Groups,
            metricType = Achievement.MetricType.COMMUNITY_DAYS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_MARATHON",
            title = "Maratończyk",
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            metricType = Achievement.MetricType.LONG_SESSIONS,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_DISTRICTS",
            title = "Zdobywca Dzielnic",
            icon = Icons.Default.LocationCity,
            metricType = Achievement.MetricType.DISTINCT_DISTRICTS,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 5
        ),
        Achievement(
            id = "ACH_CO2_MILESTONE",
            title = "Eko-Bohater",
            icon = Icons.Default.Park,
            metricType = Achievement.MetricType.CO2_MILESTONES,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_DEVICE_VARIETY",
            title = "Multisportowiec",
            icon = Icons.Default.SportsScore,
            metricType = Achievement.MetricType.DEVICE_VARIETY,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 4
        ),
        Achievement(
            id = "ACH_DAILY_DOUBLE",
            title = "Podwójna Moc",
            icon = Icons.Default.AllInclusive,
            metricType = Achievement.MetricType.DAILY_DOUBLE,
            level = 0,
            currentProgress = 0.0
        ),
        Achievement(
            id = "ACH_EXPLORE_HISTORY",
            title = "Kronikarz Mocy",
            icon = Icons.Default.History,
            metricType = Achievement.MetricType.EXPLORE_HISTORY,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_DARK_THEME",
            title = "Władca Ciemności",
            icon = Icons.Default.DarkMode,
            metricType = Achievement.MetricType.DARK_THEME_TOGGLE,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_ABOUT_VIEWED",
            title = "Świadomy",
            icon = Icons.Default.Info,
            metricType = Achievement.MetricType.ABOUT_VIEWED,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_PROFILE_CUSTOM",
            title = "Własny Styl",
            icon = Icons.Default.Face,
            metricType = Achievement.MetricType.PROFILE_CUSTOM,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_ARCHIVE_VIEWED",
            title = "Archiwista",
            icon = Icons.Default.Archive,
            metricType = Achievement.MetricType.ARCHIVE_VIEWED,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_QR_SCANNER",
            title = "Radar Kinetyczny",
            icon = Icons.Default.QrCodeScanner,
            metricType = Achievement.MetricType.QR_SCANNER,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_MAP_NAVIGATOR",
            title = "Nawigator Łodzi",
            icon = Icons.Default.Navigation,
            metricType = Achievement.MetricType.MAP_NAVIGATOR,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_WEEKLY_CHALLENGES",
            title = "Plan Działania",
            icon = Icons.Default.AssignmentTurnedIn,
            metricType = Achievement.MetricType.WEEKLY_CHALLENGES,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_FIRST_REWARD",
            title = "Pierwszy Bilet",
            icon = Icons.Default.ConfirmationNumber,
            metricType = Achievement.MetricType.FIRST_REWARD,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
        ),
        Achievement(
            id = "ACH_ECO_CALC",
            title = "Czysty Ślad",
            icon = Icons.Default.Spa,
            metricType = Achievement.MetricType.ECO_CALCULATOR_HERO,
            level = 0,
            currentProgress = 0.0,
            maxLevel = 1
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
    redeemedRewardsCount: Int,
    isEnglish: Boolean = false,
    isDarkTheme: Boolean = false,
    userId: String = ""
): AchievementEvaluationResult {
    var bonusPointsEarned = 0
    val messages = mutableListOf<String>()

    val allAchievements = if (currentAchievements.size < DefaultAchievements.initialAchievements.size) {
        val existingIds = currentAchievements.map { it.id }.toSet()
        currentAchievements + DefaultAchievements.initialAchievements.filter { it.id !in existingIds }
    } else {
        currentAchievements
    }

    val updated = allAchievements.map { ach ->
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
            Achievement.MetricType.MORNING_WORKOUTS -> workouts.count {
                it.dateText.contains("07:") || it.dateText.contains("08:") || it.dateText.contains("Rano") || (workouts.isNotEmpty() && it.id.hashCode() % 3 == 0)
            }.toDouble()
            Achievement.MetricType.WEEKEND_WORKOUTS -> workouts.count {
                val dow = if (it.timestampMs > 0L) getDayOfWeekIndex(it.timestampMs) else -1
                val isWeekendDay = dow == 5 || dow == 6
                val textMatch = it.dateText.contains("Sob") || it.dateText.contains("Niedz") || it.dateText.contains("Sat") || it.dateText.contains("Sun")
                val isTodayWeekend = getDayOfWeekIndex(currentTimeMillis()).let { d -> d == 5 || d == 6 }
                isWeekendDay || textMatch || (it.timestampMs <= 0L && isTodayWeekend)
            }.toDouble()
            Achievement.MetricType.FAST_SESSIONS -> workouts.count {
                it.durationSeconds in 1..300 && it.generatedWh >= 5.0
            }.toDouble()
            Achievement.MetricType.COMMUNITY_DAYS -> (workouts.size / 2.0).coerceAtLeast(if (workouts.isNotEmpty()) 1.0 else 0.0)
            Achievement.MetricType.LONG_SESSIONS -> workouts.count { it.durationSeconds >= 900 }.toDouble()
            Achievement.MetricType.DISTINCT_DISTRICTS -> workouts.map { it.stationLocation }.filter { it.isNotBlank() }.distinct().size.toDouble()
            Achievement.MetricType.CO2_MILESTONES -> co2SavedKg
            Achievement.MetricType.DEVICE_VARIETY -> (workouts.map { it.stationName }.filter { it.isNotBlank() }.distinct().size).coerceAtMost(4).toDouble()
            Achievement.MetricType.DAILY_DOUBLE -> (workouts.size / 2).toDouble()
            Achievement.MetricType.EXPLORE_HISTORY -> if (ach.level > 0 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_explore_history", false))) 1.0 else 0.0
            Achievement.MetricType.DARK_THEME_TOGGLE -> if (ach.level > 0 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_dark_theme", false))) 1.0 else 0.0
            Achievement.MetricType.ABOUT_VIEWED -> if (ach.level > 0 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_about_viewed", false))) 1.0 else 0.0
            Achievement.MetricType.PROFILE_CUSTOM -> if (ach.level > 0 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_profile_custom", false))) 1.0 else 0.0
            Achievement.MetricType.ARCHIVE_VIEWED -> if (ach.level > 0 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_archive_viewed", false))) 1.0 else 0.0
            Achievement.MetricType.QR_SCANNER -> if (ach.level > 0 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_qr_scanner", false))) 1.0 else 0.0
            Achievement.MetricType.MAP_NAVIGATOR -> if (ach.level > 0 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_map_navigator", false))) 1.0 else 0.0
            Achievement.MetricType.WEEKLY_CHALLENGES -> if (ach.level > 0 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_weekly_challenges", false))) 1.0 else 0.0
            Achievement.MetricType.FIRST_REWARD -> if (ach.level > 0 || redeemedRewardsCount >= 1 || (userId.isNotBlank() && VoltUpStorage.getBoolean("${userId}_ach_first_reward", false))) 1.0 else 0.0
            Achievement.MetricType.ECO_CALCULATOR_HERO -> if (ach.level > 0 || co2SavedKg >= 0.05) 1.0 else 0.0
        }

        var curLvl = ach.level
        var newPoints = 0

        while (true) {
            val atMax = ach.maxLevel in 1..curLvl
            if (atMax) break
            val nextConfig = ach.getLevelConfig(curLvl + 1, isEnglish = isEnglish)
            val reqVal = nextConfig.first
            if (rawMetric >= reqVal) {
                curLvl++
                newPoints += nextConfig.second
                val titleStr = ach.getLocalizedTitle(isEnglish)
                val msg = if (ach.maxLevel == 1 || curLvl == 1) {
                    if (isEnglish) {
                        when {
                            titleStr.length >= 17 -> "Achievement! $titleStr"
                            titleStr.length >= 12 -> "New Achievement! $titleStr"
                            else -> "Achievement unlocked! $titleStr"
                        }
                    } else {
                        when {
                            titleStr.length >= 17 -> "Osiągnięcie! $titleStr"
                            titleStr.length >= 12 -> "Nowe Osiągnięcie! $titleStr"
                            else -> "Odblokowano osiągnięcie! $titleStr"
                        }
                    }
                } else {
                    if (isEnglish) {
                        if (titleStr.length >= 15) "Level $curLvl! $titleStr"
                        else "Level $curLvl unlocked! $titleStr"
                    } else {
                        if (titleStr.length >= 15) "Poziom $curLvl! $titleStr"
                        else "Odblokowano poziom $curLvl! $titleStr"
                    }
                }
                messages.add(msg)
            } else {
                break
            }
        }

        bonusPointsEarned += newPoints
        ach.copy(level = curLvl, currentProgress = rawMetric)
    }

    return AchievementEvaluationResult(updated, bonusPointsEarned, messages)
}
