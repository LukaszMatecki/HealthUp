package com.lumatech.voltup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

object MockData {
    val sampleStations = listOf(
        KineticStation(
            id = "LDZ-PONT-01",
            name = "Rower Kinetyczny #01",
            location = "Park Poniatowskiego, Łódź",
            district = "Polesie",
            type = DeviceType.BIKE,
            distance = "450 m",
            activeUsersCount = 3,
            qrCodeId = "QR-PONT-01",
            mapCoordinatesX = 0.38f,
            mapCoordinatesY = 0.45f,
            isOnline = true
        ),
        KineticStation(
            id = "LDZ-ZDROWIE-02",
            name = "Orbitrek Parkowy #02",
            location = "Park na Zdrowiu (przy Aquapark Fala)",
            district = "Polesie",
            type = DeviceType.CROSS_TRAINER,
            distance = "1.1 km",
            activeUsersCount = 5,
            qrCodeId = "QR-ZDROWIE-02",
            mapCoordinatesX = 0.22f,
            mapCoordinatesY = 0.35f,
            isOnline = true
        ),
        KineticStation(
            id = "LDZ-MANU-03",
            name = "Huśtawka Dynamiczna #03",
            location = "Rynek Włókniarek, Manufaktura",
            district = "Bałuty",
            type = DeviceType.SWING,
            distance = "1.8 km",
            activeUsersCount = 2,
            qrCodeId = "QR-MANU-03",
            mapCoordinatesX = 0.52f,
            mapCoordinatesY = 0.25f,
            isOnline = true
        ),
        KineticStation(
            id = "LDZ-ZRODLA-04",
            name = "Stepper Miejski #04",
            location = "Park Źródliska & Palmiarnia",
            district = "Widzew",
            type = DeviceType.STEPPER,
            distance = "2.3 km",
            activeUsersCount = 1,
            qrCodeId = "QR-ZRODLA-04",
            mapCoordinatesX = 0.70f,
            mapCoordinatesY = 0.58f,
            isOnline = true
        ),
        KineticStation(
            id = "LDZ-PIOTR-05",
            name = "Rower Turbogenerator #05",
            location = "Piotrkowska Center (Off Piotrkowska)",
            district = "Śródmieście",
            type = DeviceType.BIKE,
            distance = "1.4 km",
            activeUsersCount = 4,
            qrCodeId = "QR-PIOTR-05",
            mapCoordinatesX = 0.50f,
            mapCoordinatesY = 0.50f,
            isOnline = true
        ),
        KineticStation(
            id = "LDZ-STEFAN-06",
            name = "Orbitrek Nadwodny #06",
            location = "Stawy Stefańskiego, Łódź",
            district = "Górna",
            type = DeviceType.CROSS_TRAINER,
            distance = "3.8 km",
            activeUsersCount = 2,
            qrCodeId = "QR-STEFAN-06",
            mapCoordinatesX = 0.45f,
            mapCoordinatesY = 0.82f,
            isOnline = true
        )
    )

    val sampleRewards = listOf(
        Reward(
            id = "REW-MPK-01",
            title = "Darmowy Bilet 20-minutowy",
            partner = "MPK Łódź",
            costPoints = 300,
            category = RewardCategory.TRANSPORT,
            description = "Bilet jednoprzejazdowy na wszystkie linie tramwajowe i autobusowe w strefie 1 Łodzi.",
            discountCode = "MPK-KINETIC-20",
            expiresDays = 14
        ),
        Reward(
            id = "REW-EC1-02",
            title = "-50% na Wystawę CNiT",
            partner = "EC1 Łódź - Miasto Kultury",
            costPoints = 750,
            category = RewardCategory.CULTURE,
            description = "Zniżka 50% na bilet do Centrum Nauki i Techniki w zrewitalizowanej elektrociepłowni.",
            discountCode = "EC1-ENERGY-50",
            expiresDays = 30
        ),
        Reward(
            id = "REW-ROD-03",
            title = "30 minut darmowej jazdy",
            partner = "Łódzki Rower Publiczny",
            costPoints = 400,
            category = RewardCategory.TRANSPORT,
            description = "Darmowe minuty na stacjach Łódzkiego Roweru Miejskiego w całym mieście.",
            discountCode = "ROD-LODZ-30",
            expiresDays = 21
        ),
        Reward(
            id = "REW-PIOTR-04",
            title = "Kawa Espresso gratis do śniadania",
            partner = "Kawiarnia Piotrkowska 102",
            costPoints = 250,
            category = RewardCategory.LOCAL,
            description = "Odbierz darmowe espresso lub americano przy zamówieniu dowolnego śniadania lub ciasta.",
            discountCode = "PIOTRKOWSKA-EKO",
            expiresDays = 7
        ),
        Reward(
            id = "REW-FALA-05",
            title = "-30% na bilet do Aquapark Fala",
            partner = "Aquapark Fala Łódź",
            costPoints = 900,
            category = RewardCategory.CULTURE,
            description = "Zniżka na 2-godzinny bilet wstępu do strefy basenowej, zjeżdżalni i saunarium.",
            discountCode = "FALA-VOLT-30",
            expiresDays = 60
        ),
        Reward(
            id = "REW-MANU-06",
            title = "Bilet do Kina Cinema City za 15 zł",
            partner = "Manufaktura Łódź",
            costPoints = 600,
            category = RewardCategory.CULTURE,
            description = "Kupon rabatowy na dowolny seans 2D od poniedziałku do czwartku w Cinema City Manufaktura.",
            discountCode = "MANU-KINO-15",
            expiresDays = 45
        ),
        Reward(
            id = "REW-VEGE-07",
            title = "-20% na Zdrowy Lunch",
            partner = "Zielona Restauracja Manufaktura",
            costPoints = 350,
            category = RewardCategory.LOCAL,
            description = "Zniżka na całe menu wegańskie i wegetariańskie przygotowane z lokalnych eko-składników.",
            discountCode = "EKO-LUNCH-20",
            expiresDays = 14
        )
    )

    val sampleChallenges = listOf(
        Challenge(
            id = "C1",
            title = "Poranny Zryw w Parku",
            description = "Wygeneruj 100 Wh przed godziną 10:00 w dowolnym parku Łodzi",
            rewardPoints = 150,
            currentProgress = 1.0f,
            isCompleted = true,
            isClaimed = false
        ),
        Challenge(
            id = "C2",
            title = "Oświetl Park na Zdrowiu",
            description = "Wygeneruj 200 Wh na stacji w Parku na Zdrowiu",
            rewardPoints = 300,
            currentProgress = 0.65f,
            isCompleted = false,
            isClaimed = false
        ),
        Challenge(
            id = "C3",
            title = "Eco-Ekspert Tygodnia",
            description = "Załaduj łącznie 500 Wh do sieci miejskiej w tym tygodniu",
            rewardPoints = 500,
            currentProgress = 0.40f,
            isCompleted = false,
            isClaimed = false
        ),
        Challenge(
            id = "C4",
            title = "Dzielnicowy Lider Polesia",
            description = "Wykonaj 3 sesje treningowe w swojej dzielnicy domowej",
            rewardPoints = 250,
            currentProgress = 0.66f,
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
            stationName = "Rower Kinetyczny #01",
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
            stationName = "Orbitrek Parkowy #02",
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
            stationName = "Huśtawka Dynamiczna #03",
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
            title = "🏆 Awans w Lidze Dzielnic!",
            message = "Polesie zbliża się do 1. miejsca w klasyfikacji miejskiej Łodzi. Śródmieście ma tylko 130 kWh przewagi!",
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
