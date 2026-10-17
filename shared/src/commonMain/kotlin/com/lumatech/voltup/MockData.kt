package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

object MockData {
    val sampleStations = listOf(
        KineticStation(
            id = "PC-1",
            name = "Rower Kinetyczny",
            location = "Piotrkowska Centrum",
            district = "Śródmieście",
            type = DeviceType.BIKE,
            distance = "450 m",
            activeUsersCount = 3,
            qrCodeId = "LDZ-PC-1",
            mapCoordinatesX = 0.52f,
            mapCoordinatesY = 0.48f,
            latitude = 51.7594,
            longitude = 19.4568,
            isOnline = true,
            maxPowerW = 250
        ),
        KineticStation(
            id = "PP-1",
            name = "Rower Kinetyczny",
            location = "Park Poniatowskiego",
            district = "Polesie",
            type = DeviceType.BIKE,
            distance = "600 m",
            activeUsersCount = 2,
            qrCodeId = "LDZ-PP-1",
            mapCoordinatesX = 0.38f,
            mapCoordinatesY = 0.52f,
            latitude = 51.7538,
            longitude = 19.4445,
            isOnline = true,
            maxPowerW = 180
        ),
        KineticStation(
            id = "PZ-1",
            name = "Orbitrek Parkowy",
            location = "Park na Zdrowiu",
            district = "Polesie",
            type = DeviceType.CROSS_TRAINER,
            distance = "1.1 km",
            activeUsersCount = 4,
            qrCodeId = "LDZ-PZ-1",
            mapCoordinatesX = 0.16f,
            mapCoordinatesY = 0.38f,
            latitude = 51.7656,
            longitude = 19.4182,
            isOnline = true,
            maxPowerW = 220
        ),
        KineticStation(
            id = "MF-1",
            name = "Huśtawka Dynamiczna",
            location = "Manufaktura",
            district = "Bałuty",
            type = DeviceType.SWING,
            distance = "1.8 km",
            activeUsersCount = 2,
            qrCodeId = "LDZ-MF-1",
            mapCoordinatesX = 0.48f,
            mapCoordinatesY = 0.18f,
            latitude = 51.7797,
            longitude = 19.4496,
            isOnline = true,
            maxPowerW = 120
        ),
        KineticStation(
            id = "PZ-2",
            name = "Stepper Miejski",
            location = "Park Źródliska",
            district = "Widzew",
            type = DeviceType.STEPPER,
            distance = "2.3 km",
            activeUsersCount = 1,
            qrCodeId = "LDZ-PZ-2",
            mapCoordinatesX = 0.74f,
            mapCoordinatesY = 0.52f,
            latitude = 51.7575,
            longitude = 19.4754,
            isOnline = true,
            maxPowerW = 160
        ),
        KineticStation(
            id = "SS-1",
            name = "Orbitrek Nadwodny",
            location = "Stawy Stefańskiego",
            district = "Górna",
            type = DeviceType.CROSS_TRAINER,
            distance = "3.8 km",
            activeUsersCount = 2,
            qrCodeId = "LDZ-SS-1",
            mapCoordinatesX = 0.46f,
            mapCoordinatesY = 0.88f,
            latitude = 51.7042,
            longitude = 19.4492,
            isOnline = true,
            maxPowerW = 200
        ),
        KineticStation(
            id = "EC-1",
            name = "Rower Generator",
            location = "EC1 Łódź",
            district = "Śródmieście",
            type = DeviceType.BIKE,
            distance = "1.2 km",
            activeUsersCount = 3,
            qrCodeId = "LDZ-EC-1",
            mapCoordinatesX = 0.70f,
            mapCoordinatesY = 0.36f,
            latitude = 51.7672,
            longitude = 19.4718,
            isOnline = true,
            maxPowerW = 250
        ),
        KineticStation(
            id = "PM-1",
            name = "Stepper Parkowy",
            location = "Park Matejki",
            district = "Śródmieście",
            type = DeviceType.STEPPER,
            distance = "2.0 km",
            activeUsersCount = 1,
            qrCodeId = "LDZ-PM-1",
            mapCoordinatesX = 0.79f,
            mapCoordinatesY = 0.22f,
            latitude = 51.7765,
            longitude = 19.4880,
            isOnline = true,
            maxPowerW = 150
        )
    )

    val sampleRewards = listOf(
        Reward(
            id = "REW-RANK-100",
            title = "+100 Punktów do Rankingu",
            partner = "VoltUp Ranking",
            costPoints = 50,
            category = RewardCategory.ALL,
            description = "Bilecik rankingowy. Wymień 50 zgromadzonych VoltPoints na natychmiastowe +100 punktów w tabeli liderów!",
            discountCode = "RANK-BOOST-100",
            expiresDays = 365
        ),
        Reward(
            id = "REW-MPK-01",
            title = "Darmowy Bilet 20-minutowy",
            partner = "MPK Łódź",
            costPoints = 1500,
            category = RewardCategory.TRANSPORT,
            description = "Bilet jednoprzejazdowy na wszystkie linie tramwajowe i autobusowe w strefie 1 Łodzi.",
            discountCode = "MPK-KINETIC-20",
            expiresDays = 14
        ),
        Reward(
            id = "REW-EC1-02",
            title = "-50% na Wystawę CNiT",
            partner = "EC1 Łódź - Miasto Kultury",
            costPoints = 4000,
            category = RewardCategory.CULTURE,
            description = "Zniżka 50% na bilet do Centrum Nauki i Techniki w zrewitalizowanej elektrociepłowni.",
            discountCode = "EC1-ENERGY-50",
            expiresDays = 30
        ),
        Reward(
            id = "REW-ROD-03",
            title = "30 minut darmowej jazdy",
            partner = "Łódzki Rower Publiczny",
            costPoints = 2000,
            category = RewardCategory.TRANSPORT,
            description = "Darmowe minuty na stacjach Łódzkiego Roweru Miejskiego w całym mieście.",
            discountCode = "ROD-LODZ-30",
            expiresDays = 21
        ),
        Reward(
            id = "REW-PIOTR-04",
            title = "Kawa Espresso gratis do śniadania",
            partner = "Kawiarnia Piotrkowska 102",
            costPoints = 1200,
            category = RewardCategory.LOCAL,
            description = "Odbierz darmowe espresso lub americano przy zamówieniu dowolnego śniadania lub ciasta.",
            discountCode = "PIOTRKOWSKA-EKO",
            expiresDays = 7
        ),
        Reward(
            id = "REW-FALA-05",
            title = "-30% na bilet do Aquapark Fala",
            partner = "Aquapark Fala Łódź",
            costPoints = 6000,
            category = RewardCategory.CULTURE,
            description = "Zniżka na 2-godzinny bilet wstępu do strefy basenowej, zjeżdżalni i saunarium.",
            discountCode = "FALA-VOLT-30",
            expiresDays = 60
        ),
        Reward(
            id = "REW-MANU-06",
            title = "Bilet do Kina Cinema City za 15 zł",
            partner = "Manufaktura Łódź",
            costPoints = 3500,
            category = RewardCategory.CULTURE,
            description = "Kupon rabatowy na dowolny seans 2D od poniedziałku do czwartku w Cinema City Manufaktura.",
            discountCode = "MANU-KINO-15",
            expiresDays = 45
        ),
        Reward(
            id = "REW-VEGE-07",
            title = "-20% na Zdrowy Lunch",
            partner = "Zielona Restauracja Manufaktura",
            costPoints = 1800,
            category = RewardCategory.LOCAL,
            description = "Zniżka na całe menu wegańskie i wegetariańskie przygotowane z lokalnych eko-składników.",
            discountCode = "EKO-LUNCH-20",
            expiresDays = 14
        )
    )

    val sampleChallenges = listOf(
        Challenge(
            id = "C1",
            title = "Ładowanie Miasta",
            description = "Wygeneruj łącznie 150 Wh energii na dowolnych stacjach w Łodzi",
            details = "Każda sesja treningowa na miejskich rowerach lub orbitrekach sumuje się do tego wyzwania. Zasil lokalną sieć oświetlenia miejskiego!",
            targetValue = 150.0,
            currentValue = 0.0,
            unit = "Wh",
            rankingPointsReward = 120,
            voltPointsReward = 10,
            currentProgress = 0.0f,
            isCompleted = false,
            isClaimed = false
        ),
        Challenge(
            id = "C2",
            title = "Potrójny Trening",
            description = "Ukończ 3 pełne sesje treningowe na stacjach kinetycznych",
            details = "Odwiedź stacje w dowolnym parku (np. Park Poniatowskiego, Park na Zdrowiu) i ukończ 3 niezależne treningi.",
            targetValue = 3.0,
            currentValue = 0.0,
            unit = "sesji",
            rankingPointsReward = 80,
            voltPointsReward = 8,
            currentProgress = 0.0f,
            isCompleted = false,
            isClaimed = false
        ),
        Challenge(
            id = "C3",
            title = "Mocny Impuls",
            description = "Osiągnij chwilową moc szczytową co najmniej 80 W",
            details = "Daj z siebie wszystko w krótkim sprincie na rowerze kinetycznym, aby rozwinąć moc co najmniej 80 Watów.",
            targetValue = 80.0,
            currentValue = 0.0,
            unit = "W",
            rankingPointsReward = 150,
            voltPointsReward = 10,
            currentProgress = 0.0f,
            isCompleted = false,
            isClaimed = false
        )
    )

    val sampleBadges = listOf(
        Badge(
            id = "B1",
            title = "Pierwszy Impuls",
            description = "Ukończono pierwszy trening na miejskiej stacji kinetycznej w Łodzi.",
            icon = Icons.Default.Bolt,
            isUnlocked = true,
            unlockedDate = "12 Lutego 2025"
        ),
        Badge(
            id = "B2",
            title = "Mistrz Parku na Zdrowiu",
            description = "Wygenerowano ponad 1 000 Wh na terenie Parku na Zdrowiu.",
            icon = Icons.Default.Park,
            isUnlocked = true,
            unlockedDate = "18 Lutego 2025"
        ),
        Badge(
            id = "B3",
            title = "Nocny Strażnik Łodzi",
            description = "Wykonano trening po zmroku, zasilając latarnie parkowe.",
            icon = Icons.Default.NightlightRound,
            isUnlocked = true,
            unlockedDate = "22 Lutego 2025"
        ),
        Badge(
            id = "B4",
            title = "Turbogenerator 100W",
            description = "Osiągnięto chwilową moc powyżej 100 Watów na stacji kinetycznej.",
            icon = Icons.Default.Speed,
            isUnlocked = true,
            unlockedDate = "1 Marca 2025"
        ),
        Badge(
            id = "B5",
            title = "Zielony Łodzianin (5 kg CO₂)",
            description = "Zaoszczędź 5 kg emisji CO₂ dzięki generowaniu czystej energii mięśni.",
            icon = Icons.Default.Eco,
            isUnlocked = false,
            progress = 0.48f
        ),
        Badge(
            id = "B6",
            title = "Klub 10 kWh Łodzi",
            description = "Dostarcz łącznie 10 000 Wh do miejskiej sieci energetycznej.",
            icon = Icons.Default.EmojiEvents,
            isUnlocked = false,
            progress = 0.35f
        )
    )

    val sampleRecentWorkouts = listOf(
        WorkoutSessionRecord(
            id = "W-01",
            stationName = "Rower Kinetyczny",
            stationLocation = "Park Poniatowskiego",
            dateText = "Wczoraj, 18:30",
            durationSeconds = 720,
            generatedWh = 142.0,
            earnedPoints = 142,
            co2SavedKg = 0.10,
            caloriesKcal = 95
        ),
        WorkoutSessionRecord(
            id = "W-02",
            stationName = "Orbitrek Parkowy",
            stationLocation = "Park na Zdrowiu",
            dateText = "3 dni temu, 10:15",
            durationSeconds = 1200,
            generatedWh = 210.0,
            earnedPoints = 210,
            co2SavedKg = 0.15,
            caloriesKcal = 160
        ),
        WorkoutSessionRecord(
            id = "W-03",
            stationName = "Huśtawka Dynamiczna",
            stationLocation = "Manufaktura",
            dateText = "5 dni temu, 16:45",
            durationSeconds = 900,
            generatedWh = 118.0,
            earnedPoints = 118,
            co2SavedKg = 0.08,
            caloriesKcal = 78
        )
    )

    val sampleNotifications = listOf(
        NotificationItem(
            id = "N-1",
            title = "⚡ Godzina Podwójnej Energii!",
            message = "W Parku Poniatowskiego do godziny 20:00 każdy wygenerowany Wh liczy się podwójnie (+2x VoltPoints).",
            timeAgo = "10 min temu",
            isRead = false,
            icon = Icons.Default.FlashOn
        ),
        NotificationItem(
            id = "N-2",
            title = "🌱 Osiągnięcie 80% Celu Miasta!",
            message = "Aleja Lipowa w Parku na Zdrowiu już prawie oświetlona! Dziękujemy za Twój wkład.",
            timeAgo = "2 godz. temu",
            isRead = false,
            icon = Icons.Default.Park
        ),
        NotificationItem(
            id = "N-3",
            title = "🏆 Awans w Rankingu Ogólnym!",
            message = "Twój wkład w energię miejską Łodzi plasuje Cię w ścisłej czołówce aktywistów VoltUp!",
            timeAgo = "Wczoraj",
            isRead = true,
            icon = Icons.Default.EmojiEvents
        ),
        NotificationItem(
            id = "N-4",
            title = "🎟️ Nowy Partner: Kino Cinema City",
            message = "Wymieniaj punkty kinetyczne na bilety ulgowe w Manufakturze.",
            timeAgo = "3 dni temu",
            isRead = true,
            icon = Icons.Default.CardGiftcard
        )
    )
}
