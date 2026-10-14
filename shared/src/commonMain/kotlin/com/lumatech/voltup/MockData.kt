package com.lumatech.voltup

object MockData {
    val sampleStations = listOf(
        KineticStation(
            id = "LDZ-PONT-01",
            name = "Rower Kinetyczny #01",
            location = "Park Poniatowskiego, Łódź",
            type = DeviceType.BIKE,
            distance = "450 m",
            activeUsersCount = 3,
            qrCodeId = "QR-PONT-01"
        ),
        KineticStation(
            id = "LDZ-ZDROWIE-02",
            name = "Orbitrek Parkowy #02",
            location = "Park na Zdrowiu, Łódź",
            type = DeviceType.CROSS_TRAINER,
            distance = "1.1 km",
            activeUsersCount = 5,
            qrCodeId = "QR-ZDROWIE-02"
        ),
        KineticStation(
            id = "LDZ-MANU-03",
            name = "Huśtawka Dynamiczna #03",
            location = "Rynek Włókniarek, Manufaktura",
            type = DeviceType.SWING,
            distance = "1.8 km",
            activeUsersCount = 2,
            qrCodeId = "QR-MANU-03"
        ),
        KineticStation(
            id = "LDZ-ZRODLA-04",
            name = "Stepper Miejski #04",
            location = "Park Źródliska, Łódź",
            type = DeviceType.STEPPER,
            distance = "2.3 km",
            activeUsersCount = 1,
            qrCodeId = "QR-ZRODLA-04"
        )
    )

    val sampleRewards = listOf(
        Reward(
            id = "REW-MPK-01",
            title = "Darmowy Bilet 20-minutowy",
            partner = "MPK Łódź",
            costPoints = 300,
            category = RewardCategory.TRANSPORT,
            description = "Bilet jednoprzejazdowy na wszystkie linie tramwajowe i autobusowe w Łodzi.",
            discountCode = "MPK-KINETIC-20"
        ),
        Reward(
            id = "REW-EC1-02",
            title = "-50% na Wystawę CNiT",
            partner = "EC1 Łódź - Miasto Kultury",
            costPoints = 750,
            category = RewardCategory.CULTURE,
            description = "Zniżka 50% na bilet do Centrum Nauki i Techniki w dawnej elektrociepłowni.",
            discountCode = "EC1-ENERGY-50"
        ),
        Reward(
            id = "REW-ROD-03",
            title = "30 minut darmowej jazdy",
            partner = "Łódzki Rower Publiczny",
            costPoints = 400,
            category = RewardCategory.TRANSPORT,
            description = "Darmowe minuty na stacjach Łódzkiego Roweru Miejskiego.",
            discountCode = "ROD-LODZ-30"
        ),
        Reward(
            id = "REW-PIOTR-04",
            title = "Kawa Espresso gratis do śniadania",
            partner = "Kawiarnia Piotrkowska 102",
            costPoints = 250,
            category = RewardCategory.LOCAL,
            description = "Odbierz darmową kawę przy zamówieniu dowolnego śniadania.",
            discountCode = "PIOTRKOWSKA-EKO"
        ),
        Reward(
            id = "REW-FALA-05",
            title = "-30% na bilet do Aquapark Fala",
            partner = "Aquapark Fala Łódź",
            costPoints = 900,
            category = RewardCategory.CULTURE,
            description = "Zniżka na bilet wstępu do strefy basenowej i saun.",
            discountCode = "FALA-VOLT-30"
        )
    )

    val sampleChallenges = listOf(
        Challenge(
            id = "C1",
            title = "Poranny Zryw w Parku",
            description = "Wygeneruj 100 Wh przed godziną 10:00",
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
            currentProgress = 0.6f,
            isCompleted = false,
            isClaimed = false
        ),
        Challenge(
            id = "C3",
            title = "Eco-Ekspert Tygodnia",
            description = "Załaduj łącznie 500 Wh do sieci miejskiej",
            rewardPoints = 500,
            currentProgress = 0.35f,
            isCompleted = false,
            isClaimed = false
        )
    )
}
