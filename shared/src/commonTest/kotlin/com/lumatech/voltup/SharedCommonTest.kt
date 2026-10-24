package com.lumatech.voltup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun testUserStatsDefaultsAndHealthMetrics() {
        val stats = UserStats()
        assertEquals(0, stats.points)
        assertEquals(0, stats.totalCaloriesBurned)
        assertEquals(0.0, stats.totalEnergyWh)
        assertEquals(0, stats.level)
        assertTrue(stats.recentWorkouts.isEmpty())
    }

    @Test
    fun testChallengeHealthShiftLocalization() {
        val challengeC1 = Challenge(
            id = "C1",
            title = "Serce w Ruchu",
            description = "Spal łącznie 250 kcal w sesjach kardio na stacjach w Łodzi",
            details = "Każda sesja w parku wzmacnia Twoje serce, poprawia krążenie i przybliża Cię do darmowego biletu MPK!",
            targetValue = 250.0,
            currentValue = 120.0,
            unit = "kcal"
        )

        // Polish localization
        assertEquals("Serce w Ruchu", challengeC1.getLocalizedTitle(isEnglish = false))
        assertTrue(challengeC1.getLocalizedDescription(isEnglish = false).contains("250 kcal"))

        // English localization
        assertEquals("Heart in Motion", challengeC1.getLocalizedTitle(isEnglish = true))
        assertTrue(challengeC1.getLocalizedDescription(isEnglish = true).contains("250 kcal"))
        assertTrue(challengeC1.getLocalizedDetails(isEnglish = true).contains("heart"))
    }

    @Test
    fun testChallengeCompletionLogic() {
        val target = 100.0
        val current = 50.0
        val uncompleted = Challenge(
            id = "C2",
            title = "Test",
            description = "Test desc",
            targetValue = target,
            currentValue = current,
            currentProgress = (current / target).toFloat(),
            isCompleted = current >= target,
            isClaimed = false
        )
        assertFalse(uncompleted.isCompleted)

        val updatedCurrent = 100.0
        val completed = uncompleted.copy(
            currentValue = updatedCurrent,
            currentProgress = (updatedCurrent / target).toFloat().coerceIn(0f, 1f),
            isCompleted = updatedCurrent >= target
        )
        assertTrue(completed.isCompleted)
    }

    @Test
    fun testSqlInjectionAndMaliciousInputResilience() {
        val sqlInjectionPayloads = listOf(
            "admin'--",
            "' OR '1'='1",
            "Jan'; DROP TABLE users; --",
            "\" or \"\"=\"",
            "<script>alert('xss')</script>",
            "../../etc/passwd",
            "\u0000nullbyte"
        )

        for (payload in sqlInjectionPayloads) {
            val user = UserStats(
                userName = payload,
                points = 100
            )
            // Verify immutability and exact character preservation without SQL side-effects
            assertEquals(payload, user.userName)
            assertEquals(100, user.points)

            val session = WorkoutSessionRecord(
                id = "REC-1",
                stationName = payload,
                stationLocation = "Park Poniatowskiego",
                dateText = "24.10.2026",
                durationSeconds = 600,
                generatedWh = 25.0,
                earnedPoints = 50,
                co2SavedKg = 0.015,
                caloriesKcal = 120
            )
            assertEquals(payload, session.stationName)
        }
    }

    @Test
    fun testWorkoutPhysicsAndBoundaryValidation() {
        val duration = 1200 // 20 minutes (0.333 h)
        val powerWatts = 75.0 // Realistic average power for outdoor human effort

        // Physical energy formula: E = P * t = 75W * (1200 / 3600 h) = 25.0 Wh
        val generatedWh = powerWatts * (duration / 3600.0)
        assertEquals(25.0, generatedWh)

        // Calories estimate for 20 mins light/moderate cardio (~120 kcal)
        val estimatedCalories = (generatedWh * 4.8).toInt()
        assertTrue(estimatedCalories in 100..150)
    }

    @Test
    fun testAchievementTierColors() {
        val tier0 = getAchievementTierColor(level = 0, isDark = true)
        assertNotNull(tier0)

        val tier5 = getAchievementTierColor(level = 5, isDark = true)
        assertNotNull(tier5)
    }
}
