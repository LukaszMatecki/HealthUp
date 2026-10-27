package com.lumatech.voltup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

val LocalAppLanguage = compositionLocalOf { AppLanguage.POLISH }

@Composable
fun getHealthUpStrings(language: AppLanguage = LocalAppLanguage.current): HealthUpStrings {
    return when (language) {
        AppLanguage.POLISH -> HealthUpPolishStrings
        AppLanguage.ENGLISH -> HealthUpEnglishStrings
    }
}

val strings: HealthUpStrings
    @Composable
    get() = getHealthUpStrings(LocalAppLanguage.current)

@Composable
fun getVoltUpStrings(language: AppLanguage = LocalAppLanguage.current): HealthUpStrings = getHealthUpStrings(language)

typealias VoltUpStrings = HealthUpStrings

interface HealthUpStrings {
    val tabHome: String
    val tabMap: String
    val tabRewards: String
    val tabProfile: String
    val scanQr: String

    val cancel: String
    val confirm: String
    val save: String
    val saveChanges: String
    val close: String
    val back: String
    val ok: String
    val delete: String
    val reset: String
    val retry: String

    val loginHeader: String
    val loginSubtitle: String
    val emailLabel: String
    val emailPlaceholder: String
    val passwordLabel: String
    val passwordPlaceholder: String
    val rememberMe: String
    val forgotPasswordQuestion: String
    val signInBtn: String
    val noAccountQuestion: String
    val signUpLink: String
    val loggingInWait: String
    val loggingInSync: String

    val registerHeader: String
    val registerSubtitle: String
    val fullNameLabel: String
    val fullNamePlaceholder: String
    val confirmPasswordLabel: String
    val signUpBtn: String
    val alreadyHaveAccountQuestion: String
    val signInLink: String

    val forgotPasswordTitle: String
    val forgotPasswordSubtitle: String
    val sendResetLinkBtn: String
    val backToLogin: String
    val resetLinkSentSuccess: String

    val appCityTag: String
    val welcomeBack: String
    val totalEnergyWh: String
    val voltPoints: String
    val co2SavedKg: String
    val userLevel: String
    val quickActionsTitle: String
    val actionScanBike: String
    val actionFindStation: String
    val actionRewardsCatalog: String
    val actionAchievements: String
    val weeklyGoalTitle: String
    val weeklyGoalSubtitle: String
    val recentWorkoutsTitle: String
    val noRecentWorkouts: String
    val viewAll: String
    val rankingPreviewTitle: String
    val fullLeaderboardBtn: String
    val yourWallet: String
    val co2Reduction: String
    val communityGoalTitle: String
    val communityGoalSubtitle: String
    val stageEndsIn: (String) -> String
    val renewalIn4Hours: String
    val goalCompleted: String
    val goalInProgress: String
    val communityGoalDescription: String
    val energyCollectionState: String
    val participantReward: String
    val rankingRewardPoints: String
    val yourPersonalContribution: String
    val activityTitle: String
    val daysOfWeekShort: List<String>
    val nearbyStationsTitle: String
    val distanceFromYou: (String) -> String
    val districtLabel: String
    val maxPowerLabel: String
    val statusActiveOnline: String
    val statusInactive: String

    val minutesLabel: String
    val healthActiveMinutes: String
    val healthHeartZone: String
    val proofOfEffort: String
    val phoneChargingBoost: (Int) -> String
    val whoHealthRecommendation: String

    val workoutActiveTitle: String
    val workoutGenerating: String
    val workoutPaused: String
    val workoutReady: String
    val currentPower: String
    val generatedEnergy: String
    val caloriesBurned: String
    val sessionDuration: String
    val cadenceRpm: String
    val resistanceLevel: String
    val startBtn: String
    val pauseBtn: String
    val resumeBtn: String
    val finishWorkoutBtn: String
    val cancelWorkoutBtn: String
    val workoutSummaryTitle: String
    val workoutSummaryDesc: String
    val saveAndClaimPointsBtn: String
    val continueWorkoutBtn: String
    val cancelConfirmTitle: String
    val cancelConfirmDesc: String
    val discardSessionBtn: String
    val backToWorkoutBtn: String
    val workoutStartAction: String
    val workoutClickToStart: String
    val workoutWattsLabel: String
    val workoutClickToResume: String
    val speedLabel: String
    val distanceLabel: String
    val heartRateLabel: String
    val deviceIdLabel: String

    val rewardsScreenTitle: String
    val catalogTab: String
    val myCouponsTab: String
    val pointsBalance: String
    val categoryAll: String
    val categoryTransport: String
    val categoryFood: String
    val categorySport: String
    val categoryEntertainment: String
    val noActiveCouponsTitle: String
    val noActiveCouponsDesc: String
    val couponDetailsTitle: String
    val costLabel: String
    val validityLabel: String
    val redeemForPoints: (Int) -> String
    val notEnoughPoints: String
    val closeDetailsBtn: String
    val couponCodeDialogTitle: String
    val showCodeToCashier: String
    val statusActive: String
    val statusRemainingTime: String
    val statusActivate: String
    val statusExpired: String
    val ticketTimedTapToActivate: String
    val ticketCodeTapToView: String
    val tabActiveCoupons: String
    val tabArchiveCoupons: String
    val scanVehicleCodeTitle: String
    val scanVehicleCodeDesc: String
    val scanVehicleCodeBtn: String
    val noArchiveCouponsTitle: String
    val noArchiveCouponsDesc: String
    val statusScanCode: String
    val tapToScanVehicleCode: String
    val ticketValidatedSuccess: String

    val mapScreenTitle: String
    val searchStationsPlaceholder: String
    val availableBikesLabel: String
    val stationPowerLabel: String
    val distanceMeters: (Int) -> String

    val profileScreenTitle: String
    val editProfileBtn: String
    val editProfileDialogTitle: String
    val chooseProfileAvatar: String
    val streakDaysLabel: String
    val totalStatsTitle: String
    val weeklyActivityTitle: String
    val weeklyChallengesSection: String
    val allWeeklyChallengesCompleted: String
    val achievementsPreviewTitle: String
    val manageAccountMenu: String
    val settingsMenu: String
    val helpGuideMenu: String
    val authorsMenu: String
    val signOutMenu: String
    val signOutConfirmTitle: String
    val signOutConfirmDesc: String
    val signOutBtn: String

    val settingsScreenTitle: String
    val appearanceSection: String
    val themeSystemTitle: String
    val themeSystemSubtitle: String
    val themeDarkTitle: String
    val themeDarkSubtitle: String
    val themeLightTitle: String
    val themeLightSubtitle: String
    val languageSection: String
    val appLanguageTitle: String
    val notificationsSection: String
    val pushNotificationsTitle: String
    val pushNotificationsSubtitle: String
    val dailyRemindersTitle: String
    val dailyRemindersSubtitle: String
    val soundsHapticsSection: String
    val soundEffectsTitle: String
    val soundEffectsSubtitle: String
    val hapticsTitle: String
    val hapticsSubtitle: String
    val dataAccountSection: String
    val clearHistoryTitle: String
    val clearHistorySubtitle: String
    val clearHistoryConfirmTitle: String
    val clearHistoryConfirmDesc: String
    val deleteDataBtn: String
    val resetDefaultsTitle: String
    val resetDefaultsSubtitle: String
    val resetDefaultsConfirmTitle: String
    val resetDefaultsConfirmDesc: String
    val resetDefaultsBtn: String
    val changePasswordMenuTitle: String
    val changePasswordMenuSubtitle: String
    val rateAppMenuTitle: String
    val rateAppMenuSubtitle: String
    val rateAppDialogTitle: String
    val rateAppDialogDesc: String
    val submitRatingBtn: String
    val maybeLaterBtn: String
    val aboutAppMenuTitle: String
    val aboutAppMenuSubtitle: String
    val aboutAppDialogTitle: String
    val aboutAppDialogDesc: String
    val versionLabel: String
    val copyrightNotice: String
    val awesomeBtn: String

    val manageAccountTitle: String
    val accountInfoSection: String
    val securitySection: String
    val dangerZoneSection: String
    val deleteAccountTitle: String
    val deleteAccountSubtitle: String
    val deleteAccountConfirmTitle: String
    val deleteAccountConfirmDesc: String
    val deleteAccountPermanentlyBtn: String

    val changePasswordDialogTitle: String
    val oldPasswordLabel: String
    val newPasswordLabel: String
    val confirmNewPasswordLabel: String
    val saveNewPasswordBtn: String
    val changePasswordHelpText: String

    val achievementsScreenTitle: String
    val achievementProgressLabel: String
    val achievementCompleted: String
    val achievementClaimReward: String
    val tabAll: String
    val tabEnergy: String
    val tabDistance: String
    val tabEco: String

    val themesSection: String
    val themesSectionHeader: String
    val accountAndSecuritySection: String
    val manageAccountSubtitleDesc: String
    val workoutAndNotificationsSection: String
    val autoPauseTitle: String
    val autoPauseSubtitle: String
    val keepScreenOnTitle: String
    val keepScreenOnSubtitle: String
    val notificationSoundsTitle: String
    val notificationSoundsSubtitle: String
    val dailyReminderSubtitleShort: String
    val dataAndResetSection: String
    val clearWorkoutHistorySubtitle: String
    val resetDefaultsSubtitleShort: String
    val aboutAppSection: String
    val rateOurAppTitle: String
    val rateOurAppSubtitle: String
    val helpAndGuideTitle: String
    val helpAndGuideSubtitle: String
    val appAuthorsTitle: String
    val appAuthorsSubtitle: String
    val aboutHealthUpAppSubtitle: String
    val aboutVoltUpAppSubtitle: String get() = aboutHealthUpAppSubtitle

    val myAchievementsTitle: String
    val tabAchievements: String
    val tabLeaderboard: String
    val badgeLocked: String
    val badgeLevel: (Int) -> String
    val levelPoints: (Int) -> String
    val badgePointsAwarded: (Int) -> String
    val youLabel: String
    val levelLabel: (Int) -> String
    val rankPosition: (Any) -> String
    val rankOutsideTop: (Int) -> String
    val placeRank: (Int) -> String

    val onboardingWelcomeTitle: String
    val onboardingWelcomeSubtitle: String
    val onboardingWelcomeDesc: String
    val onboardingNotificationsTitle: String
    val onboardingNotificationsSubtitle: String
    val onboardingNotificationsDesc: String
    val onboardingRewardsTitle: String
    val onboardingRewardsSubtitle: String
    val onboardingRewardsDesc: String
    val onboardingNextBtn: String
    val onboardingGetStartedBtn: String
    val onboardingSkipBtn: String
    val onboardingInitializing: String

    val helpGuideTitle: String
    val helpTabFaq: String
    val helpTabSupport: String
    val authorsScreenTitle: String
    val authorsTeamTitle: String
    val authorsTeamDesc: String

    val instantValidity: String
    val daysValidity: (Int) -> String
    val minutesValidity: (Int) -> String

    val deviceBike: String
    val deviceStepper: String
    val deviceCrossTrainer: String
    val deviceSwing: String

    val qrScannerTitle: String
    val qrScannerInstruction: String
    val qrManualCodePlaceholder: String
    val qrSubmitBtn: String
    val qrSimulateBtn: String
    val clearQueryBtn: String

    val statusOnline: String
    val statusPaused: String
    val workoutStart: String
    val workoutPausedShort: String

    val workoutHistorySection: String
    val workoutHistoryEmpty: String
    val workoutDetailsTitle: String
    val workoutDetailsStation: String
    val workoutDetailsDuration: String
    val workoutDetailsEnergy: String
    val workoutDetailsPoints: String
    val workoutDetailsCalories: String
    val workoutDetailsCo2: String
    val workoutDetailsDate: String

    val enableNotificationsBtn: String
    val notificationsEnabledStatus: String
    val notificationPermissionDialogTitle: String
    val notificationPermissionDialogDesc: String
    val allowBtn: String
    val notNowBtn: String

    val workoutHistoryTileTitle: String
    val workoutHistoryTileSubtitle: String
    val workoutHistoryScreenTitle: String
    val workoutDetailsDevice: String
    val workoutDetailsPlace: String
    val cityKineticEnergy: String
    val challengeProgressLabel: String
    val completionRewardsLabel: String
    val claimRewardsBtn: (Int, Int) -> String
    val claimingStatus: String
    val rewardsClaimedStatus: String
    val expandDetails: String
    val collapseDetails: String
    val levelPointsSuffix: String

    val profileUpdated: String
    val welcomeBackUser: (String) -> String
    val accountCreatedUser: (String) -> String
    val defaultSettingsRestored: String
    val signedOutMessage: String
    val accountDeletedMessage: String
    val sessionFinishedMessage: String
    val challengeRewardEarned: (Int, Int) -> String
    val historyClearedMessage: String
    val passwordChangedSuccess: String
    val rateAppThankYou: String
    val nameCannotBeEmpty: String
    val enterCurrentPassword: String
    val passwordMinLength: String
    val passwordsDoNotMatch: String
    val passwordMustBeDifferent: String
    val passwordChangeError: String
    val accountDeleteError: String
    val emailAndPasswordRequired: String
    val invalidEmailFormat: String
    val loginFailedRetry: String
    val enterNameOrNickname: String
    val nameMaxLength: String
    val registrationFailedRetry: String
    val resendLinkSuccess: String
    val resendLinkFailed: String
    val resetLinkFailed: String
    val rateRating5: String
    val rateRating4: String
    val rateRating3: String
    val rateRatingDefault: String
}

typealias VoltUpPolishStrings = HealthUpPolishStrings

object HealthUpPolishStrings : HealthUpStrings {
    override val tabHome = "Główna"
    override val tabMap = "Mapa"
    override val tabRewards = "Nagrody"
    override val tabProfile = "Profil"
    override val scanQr = "Skanuj QR"

    override val cancel = "Anuluj"
    override val confirm = "Potwierdź"
    override val save = "Zapisz"
    override val saveChanges = "Zapisz zmiany"
    override val close = "Zamknij"
    override val back = "Wróć"
    override val ok = "OK"
    override val delete = "Usuń"
    override val reset = "Przywróć"
    override val retry = "Spróbuj ponownie"

    override val loginHeader = "LOGOWANIE"
    override val loginSubtitle = "Zaloguj się do swojego konta zdrowia"
    override val emailLabel = "Adres e-mail"
    override val emailPlaceholder = "np. jan.kowalski@lodz.pl"
    override val passwordLabel = "Hasło"
    override val passwordPlaceholder = "Wpisz swoje hasło"
    override val rememberMe = "Zapamiętaj mnie"
    override val forgotPasswordQuestion = "Nie pamiętasz hasła?"
    override val signInBtn = "Zaloguj się"
    override val noAccountQuestion = "Nie masz jeszcze konta? "
    override val signUpLink = "Zarejestruj się"
    override val loggingInWait = "Logowanie, proszę czekać..."
    override val loggingInSync = "Synchronizacja profilu i punktów..."

    override val registerHeader = "REJESTRACJA"
    override val registerSubtitle = "Ruszaj się dla zdrowia w Łodzi"
    override val fullNameLabel = "Imię i nazwisko"
    override val fullNamePlaceholder = "np. Jan Kowalski"
    override val confirmPasswordLabel = "Powtórz hasło"
    override val signUpBtn = "Zarejestruj się"
    override val alreadyHaveAccountQuestion = "Masz już konto? "
    override val signInLink = "Zaloguj się"

    override val forgotPasswordTitle = "Odzyskaj hasło"
    override val forgotPasswordSubtitle = "Wyślemy link do zresetowania hasła na podany adres e-mail."
    override val sendResetLinkBtn = "Wyślij link resetujący"
    override val backToLogin = "Wróć do logowania"
    override val resetLinkSentSuccess = "Wysłano link do zresetowania hasła na Twój adres e-mail."

    override val appCityTag = "Łódź"
    override val welcomeBack = "Witaj ponownie!"
    override val totalEnergyWh = "Wygenerowano"
    override val voltPoints = "HealthPoints"
    override val co2SavedKg = "Oszczędzone CO₂"
    override val userLevel = "Poziom"
    override val quickActionsTitle = "SZYBKIE AKCJE"
    override val actionScanBike = "Skanuj rower"
    override val actionFindStation = "Znajdź stację"
    override val actionRewardsCatalog = "Odbierz bilet"
    override val actionAchievements = "Osiągnięcia"
    override val weeklyGoalTitle = "CEL TYGODNIOWY"
    override val weeklyGoalSubtitle = "Wygeneruj 150 Wh energii w tym tygodniu"
    override val recentWorkoutsTitle = "OSTATNIE SESJE"
    override val noRecentWorkouts = "Brak zarejestrowanych sesji. Odwiedź stację w parku, aby zacząć!"
    override val viewAll = "Wszystkie"
    override val rankingPreviewTitle = "RANKING ŁODZI"
    override val fullLeaderboardBtn = "Zobacz pełny ranking"
    override val yourWallet = "TWÓJ PORTFEL"
    override val co2Reduction = "CO2 redukcja"
    override val communityGoalTitle = "CEL ZDROWIA ŁODZI"
    override val communityGoalSubtitle = "Wspólne spalanie kalorii w parku"
    override val stageEndsIn: (String) -> String = { "Koniec etapu za: $it" }
    override val renewalIn4Hours = "Odnowienie za 4 godz."
    override val goalCompleted = "Zaliczone ✓"
    override val goalInProgress = "W trakcie"
    override val communityGoalDescription = "Każda spalona kaloria na miejskich rowerach i orbitrekach przybliża mieszkańców do celu. Gdy osiągniemy 15 000 kcal w 24h, wszyscy uczestnicy otrzymują +100 punktów do rankingu!"
    override val energyCollectionState = "Stan zbiórki kalorii:"
    override val participantReward = "Nagroda dla uczestników:"
    override val rankingRewardPoints = "+100 pkt do rankingu"
    override val yourPersonalContribution = "Twój osobisty wkład:"
    override val activityTitle = "AKTYWNOŚĆ"
    override val daysOfWeekShort = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd")
    override val nearbyStationsTitle = "W POBLIŻU"
    override val distanceFromYou: (String) -> String = { "$it od Ciebie" }
    override val districtLabel = "Dzielnica"
    override val maxPowerLabel = "Moc max"
    override val statusActiveOnline = "Status: Aktywna (Online)"
    override val statusInactive = "Status: Nieaktywna"

    override val minutesLabel = "Minuty"
    override val healthActiveMinutes = "Minuty"
    override val healthHeartZone = "Strefa zdrowia"
    override val proofOfEffort = "Energia"
    override val phoneChargingBoost: (Int) -> String = { "+$it% baterii z mięśni" }
    override val whoHealthRecommendation = "Norma WHO (150 min/tydz.)"

    override val workoutActiveTitle = "Aktywny Trening"
    override val workoutGenerating = "Aktywny wysiłek • Energia"
    override val workoutPaused = "Trening wstrzymany"
    override val workoutReady = "Gotowy do treningu"
    override val currentPower = "Moc mięśni"
    override val generatedEnergy = "Energia"
    override val caloriesBurned = "Spalone kalorie"
    override val sessionDuration = "Czas trwania"
    override val cadenceRpm = "Kadencja"
    override val resistanceLevel = "Opór"
    override val startBtn = "Rozpocznij"
    override val pauseBtn = "Wstrzymaj"
    override val resumeBtn = "Wznów"
    override val finishWorkoutBtn = "Zakończ i odbierz punkty"
    override val cancelWorkoutBtn = "Anuluj trening"
    override val workoutSummaryTitle = "Podsumowanie Sesji"
    override val workoutSummaryDesc = "Świetna robota dla Twojego zdrowia! Twój wysiłek zasilił lokalną stację w parku, a zdobyte punkty możesz wymienić na bilety MPK."
    override val saveAndClaimPointsBtn = "Zapisz i odbierz punkty"
    override val continueWorkoutBtn = "Kontynuuj trening"
    override val cancelConfirmTitle = "Anulować trening?"
    override val cancelConfirmDesc = "Cały postęp tej sesji, spalone kalorie i punkty zostaną utracone."
    override val discardSessionBtn = "Przerwij i usuń"
    override val backToWorkoutBtn = "Wróć do treningu"
    override val workoutStartAction = "START"
    override val workoutClickToStart = "KLIKNIJ ABY ROZPOCZĄĆ"
    override val workoutWattsLabel = "WATY (MOC)"
    override val workoutClickToResume = "KLIKNIJ BY WZNOWIĆ"
    override val speedLabel = "Prędkość"
    override val distanceLabel = "Dystans"
    override val heartRateLabel = "Tętno"
    override val deviceIdLabel = "ID URZĄDZENIA"

    override val rewardsScreenTitle = "Nagrody i Bilety"
    override val catalogTab = "Katalog nagród"
    override val myCouponsTab = "Moje kupony"
    override val pointsBalance = "Twoje HealthPoints"
    override val categoryAll = "Wszystkie"
    override val categoryTransport = "Transport MPK"
    override val categoryFood = "Gastronomia"
    override val categorySport = "Sport i Rekreacja"
    override val categoryEntertainment = "Kultura i Kino"
    override val noActiveCouponsTitle = "Brak aktywnych kuponów"
    override val noActiveCouponsDesc = "Odbierz nagrodę z katalogu,\naby mieć do niej dostęp tutaj."
    override val couponDetailsTitle = "Szczegóły kuponu"
    override val costLabel = "Koszt"
    override val validityLabel = "Czas na wykorzystanie"
    override val redeemForPoints: (Int) -> String = { "Odbierz za $it pkt" }
    override val notEnoughPoints = "Brakuje punktów"
    override val closeDetailsBtn = "Zamknij szczegóły"
    override val couponCodeDialogTitle = "Twój Kod Rabatowy"
    override val showCodeToCashier = "Pokaż powyższy kod sprzedawcy przy kasie lub wpisz w aplikacji partnera, aby zrealizować zniżkę."
    override val statusActive = "AKTYWNY"
    override val statusRemainingTime = "POZOSTAŁO"
    override val statusActivate = "AKTYWUJ"
    override val statusExpired = "Bilet wygasł"
    override val ticketTimedTapToActivate = "Bilet czasowy • Kliknij by aktywować"
    override val ticketCodeTapToView = "Kliknij by wyświetlić"
    override val tabActiveCoupons = "Aktywne"
    override val tabArchiveCoupons = "Archiwum"
    override val scanVehicleCodeTitle = "Kasowanie biletu MPK"
    override val scanVehicleCodeDesc = "Skieruj aparat na naklejkę z kodem QR umieszczoną wewnątrz pojazdu (autobusu lub tramwaju), aby skasować bilet."
    override val scanVehicleCodeBtn = "Skasuj bilet (Skanuj kod)"
    override val noArchiveCouponsTitle = "Brak biletów w archiwum"
    override val noArchiveCouponsDesc = "Zużyte i wygasłe bilety pojawią się w tym miejscu."
    override val statusScanCode = "SKANUJ"
    override val tapToScanVehicleCode = "Dotknij, aby zeskanować kod z pojazdu"
    override val ticketValidatedSuccess = "Bilet skasowany!"

    override val mapScreenTitle = "Mapa"
    override val searchStationsPlaceholder = "Szukaj stacji w Łodzi..."
    override val availableBikesLabel = "Dostępne rowery"
    override val stationPowerLabel = "Moc stacji"
    override val distanceMeters: (Int) -> String = { "${it} m od Ciebie" }

    override val profileScreenTitle = "Mój Profil"
    override val editProfileBtn = "Edytuj profil"
    override val editProfileDialogTitle = "Edytuj profil"
    override val chooseProfileAvatar = "Wybierz ikonę profilu"
    override val streakDaysLabel = "Dni serii"
    override val totalStatsTitle = "STATYSTYKI ŁĄCZNE"
    override val weeklyActivityTitle = "AKTYWNOŚĆ W TYM TYGODNIU"
    override val weeklyChallengesSection = "WYZWANIA TYGODNIOWE"
    override val allWeeklyChallengesCompleted = "Wszystkie wyzwania na ten tydzień ukończone. Dobra robota!"
    override val achievementsPreviewTitle = "ZDOBYTE ODZNAKI"
    override val manageAccountMenu = "Zarządzanie kontem"
    override val settingsMenu = "Ustawienia aplikacji"
    override val helpGuideMenu = "Pomoc i przewodnik"
    override val authorsMenu = "O twórcach projektu"
    override val signOutMenu = "Wyloguj się"
    override val signOutConfirmTitle = "Wylogować się?"
    override val signOutConfirmDesc = "Czy na pewno chcesz zakończyć sesję i wylogować się z konta HealthUp?"
    override val signOutBtn = "Wyloguj się"

    override val settingsScreenTitle = "Ustawienia"
    override val appearanceSection = "WYGLĄD I MOTYW"
    override val themeSystemTitle = "Motyw systemu"
    override val themeSystemSubtitle = "Dopasuj do ustawień urządzenia"
    override val themeDarkTitle = "Ciemny motyw"
    override val themeDarkSubtitle = "Oszczędzaj baterię, ciemne tło"
    override val themeLightTitle = "Jasny motyw"
    override val themeLightSubtitle = "Klasyczny, czytelny w słońcu"
    override val languageSection = "JĘZYK / LANGUAGE"
    override val appLanguageTitle = "Język aplikacji"
    override val notificationsSection = "POWIADOMIENIA"
    override val pushNotificationsTitle = "Powiadomienia push"
    override val pushNotificationsSubtitle = "Informacje o wyzwaniach i stacjach"
    override val dailyRemindersTitle = "Codzienne przypomnienie"
    override val dailyRemindersSubtitle = "Motywacja do regularnego treningu"
    override val soundsHapticsSection = "DŹWIĘKI I WIBRACJE"
    override val soundEffectsTitle = "Efekty dźwiękowe"
    override val soundEffectsSubtitle = "Dźwięki przy zdobywaniu punktów"
    override val hapticsTitle = "Wibracje (Haptyka)"
    override val hapticsSubtitle = "Subtelne wibracje przy interakcji"
    override val dataAccountSection = "DANE I KONTO"
    override val clearHistoryTitle = "Wyczyść historię sesji"
    override val clearHistorySubtitle = "Usuń lokalne zapisy treningów"
    override val clearHistoryConfirmTitle = "Wyczyścić historię?"
    override val clearHistoryConfirmDesc = "Zapisana historia odbytych sesji zostanie trwale usunięta. Twoje HealthPoints pozostaną bezpieczne."
    override val deleteDataBtn = "Usuń dane"
    override val resetDefaultsTitle = "Przywróć domyślne"
    override val resetDefaultsSubtitle = "Zresetuj motyw i powiadomienia"
    override val resetDefaultsConfirmTitle = "Przywrócić domyślne?"
    override val resetDefaultsConfirmDesc = "Wszystkie opcje motywu oraz powiadomień zostaną zresetowane do wartości początkowych."
    override val resetDefaultsBtn = "Przywróć"
    override val changePasswordMenuTitle = "Zmień hasło"
    override val changePasswordMenuSubtitle = "Zaktualizuj hasło do swojego konta"
    override val rateAppMenuTitle = "Oceń HealthUp"
    override val rateAppMenuSubtitle = "Podziel się opinią w Google Play"
    override val rateAppDialogTitle = "Oceń HealthUp"
    override val rateAppDialogDesc = "Twoja opinia pomaga nam rozwijać bezpieczne stacje aktywności w parkach Łodzi!"
    override val submitRatingBtn = "Wyślij ocenę"
    override val maybeLaterBtn = "Może później"
    override val aboutAppMenuTitle = "O aplikacji HealthUp"
    override val aboutAppMenuSubtitle = "Wersja 1.00 • Łódź 2026"
    override val aboutAppDialogTitle = "HealthUp"
    override val aboutAppDialogDesc = "HealthUp to program aktywizacji mieszkańców Łodzi. Zachęcamy do regularnego, bezpiecznego ruchu na świeżym powietrzu – trenuj w parkach, dbaj o serce i wymieniaj minuty aktywności na darmowe bilety MPK i zniżki!"
    override val versionLabel = "Wersja 1.0.0"
    override val copyrightNotice = "© 2026 HealthUp Team. Wszelkie prawa zastrzeżone."
    override val awesomeBtn = "Super"

    override val manageAccountTitle = "Zarządzanie kontem"
    override val accountInfoSection = "INFORMACJE O KONCIE"
    override val securitySection = "BEZPIECZEŃSTWO"
    override val dangerZoneSection = "STREFA ZAGROŻENIA"
    override val deleteAccountTitle = "Usuń konto trwale"
    override val deleteAccountSubtitle = "Trwałe skasowanie konta i danych"
    override val deleteAccountConfirmTitle = "Usunąć konto?"
    override val deleteAccountConfirmDesc = "Ta operacja jest nieodwracalna. Twój profil, punkty HealthPoints oraz historia treningów zostaną trwale skasowane."
    override val deleteAccountPermanentlyBtn = "Usuń trwale konto"

    override val changePasswordDialogTitle = "Zmień hasło"
    override val oldPasswordLabel = "Dotychczasowe hasło"
    override val newPasswordLabel = "Nowe hasło"
    override val confirmNewPasswordLabel = "Powtórz nowe hasło"
    override val saveNewPasswordBtn = "Zapisz nowe hasło"
    override val changePasswordHelpText = "Wprowadź dotychczasowe hasło oraz nowe bezpieczne hasło (minimum 6 znaków)."

    override val achievementsScreenTitle = "Osiągnięcia i Odznaki"
    override val achievementProgressLabel = "Postęp"
    override val achievementCompleted = "Ukończono"
    override val achievementClaimReward = "Odbierz nagrodę"
    override val tabAll = "Wszystkie"
    override val tabEnergy = "Energia"
    override val tabDistance = "Dystans"
    override val tabEco = "Ekologia"

    override val themesSection = "Motywy"
    override val themesSectionHeader = "MOTYWY"
    override val accountAndSecuritySection = "KONTO I BEZPIECZEŃSTWO"
    override val manageAccountSubtitleDesc = "Dane profilu, zmiana hasła i opcje konta"
    override val workoutAndNotificationsSection = "TRENING I POWIADOMIENIA"
    override val autoPauseTitle = "Automatyczna pauza"
    override val autoPauseSubtitle = "Zatrzymuj czas przy braku ruchu"
    override val keepScreenOnTitle = "Nie wygaszaj ekranu"
    override val keepScreenOnSubtitle = "Ekran pozostaje włączony podczas treningu"
    override val notificationSoundsTitle = "Dźwięki powiadomień"
    override val notificationSoundsSubtitle = "Sygnały audio po zakończeniu sesji"
    override val dailyReminderSubtitleShort = "Krótki impuls o aktywności"
    override val dataAndResetSection = "DANE I RESET"
    override val clearWorkoutHistorySubtitle = "Usuwa lokalnie zapisane sesje"
    override val resetDefaultsSubtitleShort = "Resetuje motyw i preferencje"
    override val aboutAppSection = "O APLIKACJI"
    override val rateOurAppTitle = "Oceń naszą aplikację"
    override val rateOurAppSubtitle = "Podziel się opinią o HealthUp"
    override val helpAndGuideTitle = "Pomoc i instrukcja"
    override val helpAndGuideSubtitle = "Jak korzystać ze stacji i zdobywać nagrody"
    override val appAuthorsTitle = "Autorzy Aplikacji"
    override val appAuthorsSubtitle = "Twórcy projektu HealthUp"
    override val aboutHealthUpAppSubtitle = "Wersja, licencje i informacje"
    override val aboutVoltUpAppSubtitle get() = aboutHealthUpAppSubtitle

    override val myAchievementsTitle = "Moje Osiągnięcia"
    override val tabAchievements = "Osiągnięcia"
    override val tabLeaderboard = "Ranking"
    override val badgeLocked = "BLOKADA"
    override val badgeLevel: (Int) -> String = { "POZIOM $it" }
    override val levelPoints: (Int) -> String = { "$it pkt poziomu" }
    override val badgePointsAwarded: (Int) -> String = { "+$it pkt" }
    override val youLabel = "(Ty)"
    override val levelLabel: (Int) -> String = { "Poziom $it" }
    override val rankPosition: (Any) -> String = { "Pozycja: #$it" }
    override val rankOutsideTop: (Int) -> String = { "Twoja pozycja poza TOP $it:" }
    override val placeRank: (Int) -> String = { "Miejsce $it" }

    override val onboardingWelcomeTitle = "Witaj w HealthUp!"
    override val onboardingWelcomeSubtitle = "Ruch i zdrowie w Łodzi"
    override val onboardingWelcomeDesc = "Krótki spacer lub 15 minut na maszynie w parku poprawia kondycję serca i samopoczucie. Ćwicz bezpiecznie na świeżym powietrzu!"
    override val onboardingNotificationsTitle = "Powiadomienia i cele"
    override val onboardingNotificationsSubtitle = "Codzienna dawka ruchu"
    override val onboardingNotificationsDesc = "Otrzymuj delikatne przypomnienia o spacerach, śledź codzienne postępy i bierz udział w wyzwaniach parkowych Łodzi."
    override val onboardingRewardsTitle = "Odbieraj realne nagrody"
    override val onboardingRewardsSubtitle = "Aktywność, która się opłaca"
    override val onboardingRewardsDesc = "Zbieraj HealthPoints za każdą minutę ruchu i wymieniaj je na darmowe bilety MPK-Łódź, wejściówki do Falii czy zniżki!"
    override val onboardingNextBtn = "Dalej"
    override val onboardingGetStartedBtn = "Zaczynamy!"
    override val onboardingSkipBtn = "Pomiń"
    override val onboardingInitializing = "Przygotowywanie Twojego profilu..."

    override val helpGuideTitle = "Pomoc i Instrukcja"
    override val helpTabFaq = "Instrukcja & FAQ"
    override val helpTabSupport = "Dział Pomocy & Kontakt"
    override val authorsScreenTitle = "Autorzy Aplikacji"
    override val authorsTeamTitle = "Ekipa Twórców HealthUp"
    override val authorsTeamDesc = "Projekt HealthUp powstał z pasji do nowoczesnych technologii, aktywności fizycznej oraz zrównoważonego rozwoju miasta Łódź."

    override val instantValidity = "Natychmiastowo"
    override val daysValidity: (Int) -> String = { "$it dni" }
    override val minutesValidity: (Int) -> String = { "$it min" }

    override val deviceBike = "Rower Kinetyczny"
    override val deviceStepper = "Stepper Miejski"
    override val deviceCrossTrainer = "Orbitrek Parkowy"
    override val deviceSwing = "Huśtawka Dynamiczna"

    override val qrScannerTitle = "Skanuj QR"
    override val qrScannerInstruction = "Skieruj aparat na kod QR na urządzeniu, aby rozpocząć trening."
    override val qrManualCodePlaceholder = "Wpisz kod ręcznie"
    override val qrSubmitBtn = "Zatwierdź"
    override val qrSimulateBtn = "Symuluj"
    override val clearQueryBtn = "Wyczyść"

    override val statusOnline = "ONLINE"
    override val statusPaused = "PAUZA"
    override val workoutStart = "START"
    override val workoutPausedShort = "PAUZA"

    override val workoutHistorySection = "HISTORIA TRENINGÓW"
    override val workoutHistoryEmpty = "Brak zarejestrowanych treningów. Rozpocznij pierwszą sesję w parku!"
    override val workoutDetailsTitle = "Szczegóły Treningu"
    override val workoutDetailsStation = "Stacja"
    override val workoutDetailsDuration = "Czas trwania"
    override val workoutDetailsEnergy = "Energia wygenerowana"
    override val workoutDetailsPoints = "Zdobyte HealthPoints"
    override val workoutDetailsCalories = "Spalone kalorie"
    override val workoutDetailsCo2 = "Zredukowany CO₂"
    override val workoutDetailsDate = "Data i godzina"

    override val enableNotificationsBtn = "Włącz powiadomienia"
    override val notificationsEnabledStatus = "Powiadomienia włączone"
    override val notificationPermissionDialogTitle = "Zezwolić na powiadomienia?"
    override val notificationPermissionDialogDesc = "Aplikacja HealthUp będzie wysyłać powiadomienia o postępach treningowych, celach dzielnicowych i zniżkach na bilety."
    override val allowBtn = "Zezwól"
    override val notNowBtn = "Nie teraz"

    override val workoutHistoryTileTitle = "Historia treningów"
    override val workoutHistoryTileSubtitle = "Sprawdź historię swoich treningów"
    override val workoutHistoryScreenTitle = "Historia treningów"
    override val workoutDetailsDevice = "Urządzenie"
    override val workoutDetailsPlace = "Miejsce"
    override val cityKineticEnergy = "Zdrowie i Aktywność Łodzi"
    override val challengeProgressLabel = "Postęp wyzwania:"
    override val completionRewardsLabel = "Nagrody za ukończenie:"
    override val claimRewardsBtn: (Int, Int) -> String = { rankPts, hp -> "Odbierz nagrody (+$rankPts pkt | +$hp HP)" }
    override val claimingStatus = "Odbieranie..."
    override val rewardsClaimedStatus = "Nagrody odebrane"
    override val expandDetails = "Rozwiń"
    override val collapseDetails = "Zwiń"
    override val levelPointsSuffix = "pkt poziomu"

    override val profileUpdated = "Profil zaktualizowany."
    override val welcomeBackUser: (String) -> String = { "Witaj ponownie w HealthUp, $it!" }
    override val accountCreatedUser: (String) -> String = { "Konto utworzone! Witaj w HealthUp, $it!" }
    override val defaultSettingsRestored = "Przywrócono ustawienia domyślne."
    override val signedOutMessage = "Wylogowano z konta."
    override val accountDeletedMessage = "Twoje konto zostało usunięte."
    override val sessionFinishedMessage = "Sesja zakończona!"
    override val challengeRewardEarned: (Int, Int) -> String = { rank, hp -> "+$rank pkt rankingu, +$hp HP!" }
    override val historyClearedMessage = "Historia sesji została wyczyszczona."
    override val passwordChangedSuccess = "Hasło zostało pomyślnie zmienione."
    override val rateAppThankYou = "Dziękujemy za opinię w Google Play!"
    override val nameCannotBeEmpty = "Imię nie może być puste."
    override val enterCurrentPassword = "Wprowadź dotychczasowe hasło."
    override val passwordMinLength = "Nowe hasło musi mieć co najmniej 6 znaków."
    override val passwordsDoNotMatch = "Nowe hasła nie są identyczne."
    override val passwordMustBeDifferent = "Nowe hasło musi być inne niż dotychczasowe."
    override val passwordChangeError = "Błąd zmiany hasła."
    override val accountDeleteError = "Błąd usuwania konta."
    override val emailAndPasswordRequired = "Podaj adres e-mail oraz hasło."
    override val invalidEmailFormat = "Podaj poprawny format adresu e-mail."
    override val loginFailedRetry = "Błąd logowania. Spróbuj ponownie."
    override val enterNameOrNickname = "Wpisz swoje imię lub pseudonim."
    override val nameMaxLength = "Imię lub pseudonim może mieć maksymalnie 30 znaków."
    override val registrationFailedRetry = "Błąd rejestracji. Spróbuj ponownie."
    override val resendLinkSuccess = "Wysłano ponownie! Sprawdź skrzynkę i SPAM."
    override val resendLinkFailed = "Nie udało się ponowić wysyłki."
    override val resetLinkFailed = "Nie udało się wysłać linku. Sprawdź e-mail."
    override val rateRating5 = "Rewelacja! Dziękujemy za wsparcie!"
    override val rateRating4 = "Bardzo dobrze! Cały czas się doskonalimy."
    override val rateRating3 = "Dziękujemy, pracujemy nad nowymi funkcjami."
    override val rateRatingDefault = "Pracujemy nad tym, aby stacje działały jeszcze lepiej!"
}

typealias VoltUpEnglishStrings = HealthUpEnglishStrings

object HealthUpEnglishStrings : HealthUpStrings {
    override val tabHome = "Home"
    override val tabMap = "Map"
    override val tabRewards = "Rewards"
    override val tabProfile = "Profile"
    override val scanQr = "Scan QR"

    override val cancel = "Cancel"
    override val confirm = "Confirm"
    override val save = "Save"
    override val saveChanges = "Save changes"
    override val close = "Close"
    override val back = "Back"
    override val ok = "OK"
    override val delete = "Delete"
    override val reset = "Reset"
    override val retry = "Try again"

    override val loginHeader = "LOGIN"
    override val loginSubtitle = "Sign in to your health & activity account"
    override val emailLabel = "Email address"
    override val emailPlaceholder = "e.g. john.doe@lodz.pl"
    override val passwordLabel = "Password"
    override val passwordPlaceholder = "Enter your password"
    override val rememberMe = "Remember me"
    override val forgotPasswordQuestion = "Forgot password?"
    override val signInBtn = "Log in"
    override val noAccountQuestion = "Don't have an account yet? "
    override val signUpLink = "Sign up"
    override val loggingInWait = "Logging in, please wait..."
    override val loggingInSync = "Synchronizing your profile and points..."

    override val registerHeader = "REGISTRATION"
    override val registerSubtitle = "Stay active and healthy in Łódź"
    override val fullNameLabel = "Full name"
    override val fullNamePlaceholder = "e.g. John Doe"
    override val confirmPasswordLabel = "Confirm password"
    override val signUpBtn = "Sign up"
    override val alreadyHaveAccountQuestion = "Already have an account? "
    override val signInLink = "Log in"

    override val forgotPasswordTitle = "Reset password"
    override val forgotPasswordSubtitle = "We will send a password reset link to your email address."
    override val sendResetLinkBtn = "Send reset link"
    override val backToLogin = "Back to login"
    override val resetLinkSentSuccess = "A password reset link has been sent to your email."

    override val appCityTag = "Łódź"
    override val welcomeBack = "Welcome back!"
    override val totalEnergyWh = "Generated"
    override val voltPoints = "HealthPoints"
    override val co2SavedKg = "CO₂ Saved"
    override val userLevel = "Level"
    override val quickActionsTitle = "QUICK ACTIONS"
    override val actionScanBike = "Scan bike"
    override val actionFindStation = "Find station"
    override val actionRewardsCatalog = "Claim ticket"
    override val actionAchievements = "Achievements"
    override val weeklyGoalTitle = "WEEKLY GOAL"
    override val weeklyGoalSubtitle = "Generate 150 Wh of energy this week"
    override val recentWorkoutsTitle = "RECENT SESSIONS"
    override val noRecentWorkouts = "No sessions recorded yet. Visit a park station to begin!"
    override val viewAll = "All"
    override val rankingPreviewTitle = "ŁÓDŹ LEADERBOARD"
    override val fullLeaderboardBtn = "View full leaderboard"
    override val yourWallet = "YOUR WALLET"
    override val co2Reduction = "CO2 reduction"
    override val communityGoalTitle = "ŁÓDŹ HEALTH GOAL"
    override val communityGoalSubtitle = "Community calories goal in park"
    override val stageEndsIn: (String) -> String = { "Stage ends in: $it" }
    override val renewalIn4Hours = "Renews in 4 hrs"
    override val goalCompleted = "Completed ✓"
    override val goalInProgress = "In progress"
    override val communityGoalDescription = "Every calorie burned on urban kinetic bikes and steppers brings the community closer to the health goal. Reach 15,000 kcal in 24h to unlock +100 ranking points for all contributors!"
    override val energyCollectionState = "Calories goal progress:"
    override val participantReward = "Participant reward:"
    override val rankingRewardPoints = "+100 pts to ranking"
    override val yourPersonalContribution = "Your contribution:"
    override val activityTitle = "ACTIVITY"
    override val daysOfWeekShort = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    override val nearbyStationsTitle = "NEARBY"
    override val distanceFromYou: (String) -> String = { "$it away" }
    override val districtLabel = "District"
    override val maxPowerLabel = "Max power"
    override val statusActiveOnline = "Status: Active (Online)"
    override val statusInactive = "Status: Inactive"

    override val minutesLabel = "Minutes"
    override val healthActiveMinutes = "Minutes"
    override val healthHeartZone = "Health Heart Zone"
    override val proofOfEffort = "Energy"
    override val phoneChargingBoost: (Int) -> String = { "+$it% phone battery" }
    override val whoHealthRecommendation = "WHO Goal (150 min/wk)"

    override val workoutActiveTitle = "Active Workout"
    override val workoutGenerating = "Active effort • Energy"
    override val workoutPaused = "Workout paused"
    override val workoutReady = "Ready for workout"
    override val currentPower = "Muscle Power"
    override val generatedEnergy = "Energy"
    override val caloriesBurned = "Calories burned"
    override val sessionDuration = "Duration"
    override val cadenceRpm = "Cadence"
    override val resistanceLevel = "Resistance"
    override val startBtn = "Start"
    override val pauseBtn = "Pause"
    override val resumeBtn = "Resume"
    override val finishWorkoutBtn = "Finish & claim points"
    override val cancelWorkoutBtn = "Cancel workout"
    override val workoutSummaryTitle = "Session Summary"
    override val workoutSummaryDesc = "Great job for your health! Your physical effort powered the park station, and earned points can be redeemed for public transit tickets."
    override val saveAndClaimPointsBtn = "Save & claim points"
    override val continueWorkoutBtn = "Continue workout"
    override val cancelConfirmTitle = "Cancel workout?"
    override val cancelConfirmDesc = "All progress from this session, burned calories and points will be lost."
    override val discardSessionBtn = "Discard session"
    override val backToWorkoutBtn = "Back to workout"
    override val workoutStartAction = "START"
    override val workoutClickToStart = "CLICK TO START"
    override val workoutWattsLabel = "WATTS (POWER)"
    override val workoutClickToResume = "CLICK TO RESUME"
    override val speedLabel = "Speed"
    override val distanceLabel = "Distance"
    override val heartRateLabel = "Heart Rate"
    override val deviceIdLabel = "DEVICE ID"

    override val rewardsScreenTitle = "Rewards & Tickets"
    override val catalogTab = "Rewards catalog"
    override val myCouponsTab = "My coupons"
    override val pointsBalance = "Your HealthPoints"
    override val categoryAll = "All"
    override val categoryTransport = "MPK Transport"
    override val categoryFood = "Food & Drink"
    override val categorySport = "Sport & Leisure"
    override val categoryEntertainment = "Cinema & Culture"
    override val noActiveCouponsTitle = "No active coupons"
    override val noActiveCouponsDesc = "Redeem a reward from the catalog\nto access it here."
    override val couponDetailsTitle = "Coupon details"
    override val costLabel = "Cost"
    override val validityLabel = "Validity period"
    override val redeemForPoints: (Int) -> String = { "Redeem for $it pts" }
    override val notEnoughPoints = "Not enough points"
    override val closeDetailsBtn = "Close details"
    override val couponCodeDialogTitle = "Your Discount Code"
    override val showCodeToCashier = "Show this code to the cashier or enter it in partner app to claim your discount."
    override val statusActive = "ACTIVE"
    override val statusRemainingTime = "REMAINING"
    override val statusActivate = "ACTIVATE"
    override val statusExpired = "Ticket expired"
    override val ticketTimedTapToActivate = "Timed ticket • Tap to activate"
    override val ticketCodeTapToView = "Tap to view code"
    override val tabActiveCoupons = "Active"
    override val tabArchiveCoupons = "Archive"
    override val scanVehicleCodeTitle = "Validate MPK Ticket"
    override val scanVehicleCodeDesc = "Point camera at the QR code sticker placed inside the vehicle (bus or tram) to validate your ticket."
    override val scanVehicleCodeBtn = "Validate Ticket (Scan QR)"
    override val noArchiveCouponsTitle = "No archived tickets"
    override val noArchiveCouponsDesc = "Used and expired tickets will appear here."
    override val statusScanCode = "SCAN"
    override val tapToScanVehicleCode = "Tap to scan vehicle QR code"
    override val ticketValidatedSuccess = "Validation successful!"

    override val mapScreenTitle = "Map"
    override val searchStationsPlaceholder = "Search stations in Łódź..."
    override val availableBikesLabel = "Available bikes"
    override val stationPowerLabel = "Station power"
    override val distanceMeters: (Int) -> String = { "${it} m away" }

    override val profileScreenTitle = "My Profile"
    override val editProfileBtn = "Edit profile"
    override val editProfileDialogTitle = "Edit profile"
    override val chooseProfileAvatar = "Choose profile icon"
    override val streakDaysLabel = "Streak days"
    override val totalStatsTitle = "LIFETIME STATISTICS"
    override val weeklyActivityTitle = "ACTIVITY THIS WEEK"
    override val weeklyChallengesSection = "WEEKLY CHALLENGES"
    override val allWeeklyChallengesCompleted = "All challenges for this week completed. Good job!"
    override val achievementsPreviewTitle = "EARNED BADGES"
    override val manageAccountMenu = "Manage account"
    override val settingsMenu = "App settings"
    override val helpGuideMenu = "Help & user guide"
    override val authorsMenu = "Project authors"
    override val signOutMenu = "Log out"
    override val signOutConfirmTitle = "Log out?"
    override val signOutConfirmDesc = "Are you sure you want to end your session and log out of HealthUp?"
    override val signOutBtn = "Log out"

    override val settingsScreenTitle = "Settings"
    override val appearanceSection = "APPEARANCE & THEME"
    override val themeSystemTitle = "System theme"
    override val themeSystemSubtitle = "Match device settings"
    override val themeDarkTitle = "Dark theme"
    override val themeDarkSubtitle = "Save battery, dark background"
    override val themeLightTitle = "Light theme"
    override val themeLightSubtitle = "Classic, high contrast in sunlight"
    override val languageSection = "LANGUAGE / JĘZYK"
    override val appLanguageTitle = "App language"
    override val notificationsSection = "NOTIFICATIONS"
    override val pushNotificationsTitle = "Push notifications"
    override val pushNotificationsSubtitle = "Alerts about challenges and stations"
    override val dailyRemindersTitle = "Daily reminder"
    override val dailyRemindersSubtitle = "Motivation for regular workouts"
    override val soundsHapticsSection = "SOUND & HAPTICS"
    override val soundEffectsTitle = "Sound effects"
    override val soundEffectsSubtitle = "Sounds when earning points"
    override val hapticsTitle = "Haptic feedback"
    override val hapticsSubtitle = "Subtle vibration on interaction"
    override val dataAccountSection = "DATA & ACCOUNT"
    override val clearHistoryTitle = "Clear workout history"
    override val clearHistorySubtitle = "Remove local session logs"
    override val clearHistoryConfirmTitle = "Clear history?"
    override val clearHistoryConfirmDesc = "Your saved session history will be permanently deleted. Your HealthPoints will remain safe."
    override val deleteDataBtn = "Delete data"
    override val resetDefaultsTitle = "Reset to defaults"
    override val resetDefaultsSubtitle = "Reset theme and notification options"
    override val resetDefaultsConfirmTitle = "Reset to defaults?"
    override val resetDefaultsConfirmDesc = "All theme and notification preferences will be reset to their initial values."
    override val resetDefaultsBtn = "Reset"
    override val changePasswordMenuTitle = "Change password"
    override val changePasswordMenuSubtitle = "Update your account password"
    override val rateAppMenuTitle = "Rate HealthUp"
    override val rateAppMenuSubtitle = "Share your review on Google Play"
    override val rateAppDialogTitle = "Rate HealthUp"
    override val rateAppDialogDesc = "Your feedback helps us develop the urban kinetic station network in Łódź!"
    override val submitRatingBtn = "Submit rating"
    override val maybeLaterBtn = "Maybe later"
    override val aboutAppMenuTitle = "About HealthUp"
    override val aboutAppMenuSubtitle = "Version 1.00 • Łódź 2026"
    override val aboutAppDialogTitle = "HealthUp"
    override val aboutAppDialogDesc = "HealthUp is an urban health program designed for Łódź residents. We inspire regular, safe outdoor exercise — move in city parks, improve cardiovascular wellness, and exchange active minutes for free MPK transit tickets and city perks!"
    override val versionLabel = "Version 1.0.0"
    override val copyrightNotice = "© 2026 HealthUp Team. All rights reserved."
    override val awesomeBtn = "Awesome"

    override val manageAccountTitle = "Manage Account"
    override val accountInfoSection = "ACCOUNT INFORMATION"
    override val securitySection = "SECURITY"
    override val dangerZoneSection = "DANGER ZONE"
    override val deleteAccountTitle = "Delete account permanently"
    override val deleteAccountSubtitle = "Permanently remove account and data"
    override val deleteAccountConfirmTitle = "Delete account?"
    override val deleteAccountConfirmDesc = "This operation is irreversible. Your profile, HealthPoints, and workout history will be permanently deleted."
    override val deleteAccountPermanentlyBtn = "Delete account permanently"

    override val changePasswordDialogTitle = "Change Password"
    override val oldPasswordLabel = "Current password"
    override val newPasswordLabel = "New password"
    override val confirmNewPasswordLabel = "Confirm new password"
    override val saveNewPasswordBtn = "Save new password"
    override val changePasswordHelpText = "Enter your current password and a new secure password (minimum 6 characters)."

    override val achievementsScreenTitle = "Achievements & Badges"
    override val achievementProgressLabel = "Progress"
    override val achievementCompleted = "Completed"
    override val achievementClaimReward = "Claim reward"
    override val tabAll = "All"
    override val tabEnergy = "Energy"
    override val tabDistance = "Distance"
    override val tabEco = "Eco"

    override val themesSection = "Themes"
    override val themesSectionHeader = "THEMES"
    override val accountAndSecuritySection = "ACCOUNT & SECURITY"
    override val manageAccountSubtitleDesc = "Profile data, password change, and account options"
    override val workoutAndNotificationsSection = "WORKOUT & NOTIFICATIONS"
    override val autoPauseTitle = "Auto-pause"
    override val autoPauseSubtitle = "Pause timer when motion stops"
    override val keepScreenOnTitle = "Keep screen on"
    override val keepScreenOnSubtitle = "Keep screen awake during workouts"
    override val notificationSoundsTitle = "Notification sounds"
    override val notificationSoundsSubtitle = "Audio cue upon session completion"
    override val dailyReminderSubtitleShort = "Gentle activity reminder"
    override val dataAndResetSection = "DATA & RESET"
    override val clearWorkoutHistorySubtitle = "Deletes locally stored sessions"
    override val resetDefaultsSubtitleShort = "Resets theme and preferences"
    override val aboutAppSection = "ABOUT THE APP"
    override val rateOurAppTitle = "Rate our app"
    override val rateOurAppSubtitle = "Share your review about HealthUp"
    override val helpAndGuideTitle = "Help & guide"
    override val helpAndGuideSubtitle = "How to use stations and earn rewards"
    override val appAuthorsTitle = "App Authors"
    override val appAuthorsSubtitle = "Creators of HealthUp"
    override val aboutHealthUpAppSubtitle = "Version, licenses, and info"
    override val aboutVoltUpAppSubtitle get() = aboutHealthUpAppSubtitle

    override val myAchievementsTitle = "My Achievements"
    override val tabAchievements = "Achievements"
    override val tabLeaderboard = "Leaderboard"
    override val badgeLocked = "LOCKED"
    override val badgeLevel: (Int) -> String = { "LEVEL $it" }
    override val levelPoints: (Int) -> String = { "$it level pts" }
    override val badgePointsAwarded: (Int) -> String = { "+$it pts" }
    override val youLabel = "(You)"
    override val levelLabel: (Int) -> String = { "Level $it" }
    override val rankPosition: (Any) -> String = { "Rank: #$it" }
    override val rankOutsideTop: (Int) -> String = { "Your rank outside TOP $it:" }
    override val placeRank: (Int) -> String = { "Rank $it" }

    override val onboardingWelcomeTitle = "Welcome to HealthUp!"
    override val onboardingWelcomeSubtitle = "Health & Movement in Łódź"
    override val onboardingWelcomeDesc = "A brisk walk or 15 minutes on park equipment strengthens your heart and lifts your mood. Exercise safely in the fresh air!"
    override val onboardingNotificationsTitle = "Notifications & Goals"
    override val onboardingNotificationsSubtitle = "Your Daily Movement Dose"
    override val onboardingNotificationsDesc = "Get gentle activity reminders, track your weekly habits, and participate in friendly community park challenges."
    override val onboardingRewardsTitle = "Earn Real Rewards"
    override val onboardingRewardsSubtitle = "Movement That Truly Pays Off"
    override val onboardingRewardsDesc = "Earn HealthPoints for every active minute and exchange them for free MPK-Łódź transit tickets, pool passes, and local perks!"
    override val onboardingNextBtn = "Next"
    override val onboardingGetStartedBtn = "Get Started!"
    override val onboardingSkipBtn = "Skip"
    override val onboardingInitializing = "Preparing your health profile..."

    override val helpGuideTitle = "Help & Guide"
    override val helpTabFaq = "Guide & FAQ"
    override val helpTabSupport = "Support & Contact"
    override val authorsScreenTitle = "App Authors"
    override val authorsTeamTitle = "The HealthUp Team"
    override val authorsTeamDesc = "HealthUp was created from a passion for modern technology, physical activity, and sustainable urban development in Łódź."

    override val instantValidity = "Instant"
    override val daysValidity: (Int) -> String = { "$it days" }
    override val minutesValidity: (Int) -> String = { "$it min" }

    override val deviceBike = "Kinetic Bike"
    override val deviceStepper = "City Stepper"
    override val deviceCrossTrainer = "Park Elliptical"
    override val deviceSwing = "Kinetic Swing"

    override val qrScannerTitle = "Scan QR"
    override val qrScannerInstruction = "Point camera at QR code on the equipment to start your workout."
    override val qrManualCodePlaceholder = "Enter code manually"
    override val qrSubmitBtn = "Submit"
    override val qrSimulateBtn = "Simulate"
    override val clearQueryBtn = "Clear"

    override val statusOnline = "ONLINE"
    override val statusPaused = "PAUSED"
    override val workoutStart = "START"
    override val workoutPausedShort = "PAUSED"

    override val workoutHistorySection = "WORKOUT HISTORY"
    override val workoutHistoryEmpty = "No workouts recorded yet. Start your first session in the park!"
    override val workoutDetailsTitle = "Workout Details"
    override val workoutDetailsStation = "Station"
    override val workoutDetailsDuration = "Duration"
    override val workoutDetailsEnergy = "Generated Energy"
    override val workoutDetailsPoints = "HealthPoints Earned"
    override val workoutDetailsCalories = "Calories Burned"
    override val workoutDetailsCo2 = "CO₂ Reduced"
    override val workoutDetailsDate = "Date & Time"

    override val enableNotificationsBtn = "Enable notifications"
    override val notificationsEnabledStatus = "Notifications enabled"
    override val notificationPermissionDialogTitle = "Allow notifications?"
    override val notificationPermissionDialogDesc = "HealthUp will send notifications about workout milestones, city challenges, and transit ticket discounts."
    override val allowBtn = "Allow"
    override val notNowBtn = "Not now"

    override val workoutHistoryTileTitle = "Workout History"
    override val workoutHistoryTileSubtitle = "Check your workout history"
    override val workoutHistoryScreenTitle = "Workout History"
    override val workoutDetailsDevice = "Device"
    override val workoutDetailsPlace = "Place"
    override val cityKineticEnergy = "Health & Outdoor Movement in Łódź"
    override val challengeProgressLabel = "Challenge progress:"
    override val completionRewardsLabel = "Completion rewards:"
    override val claimRewardsBtn: (Int, Int) -> String = { rankPts, hp -> "Claim rewards (+$rankPts pts | +$hp HP)" }
    override val claimingStatus = "Claiming..."
    override val rewardsClaimedStatus = "Rewards claimed"
    override val expandDetails = "Expand"
    override val collapseDetails = "Collapse"
    override val levelPointsSuffix = "level pts"

    override val profileUpdated = "Profile updated."
    override val welcomeBackUser: (String) -> String = { "Welcome back to HealthUp, $it!" }
    override val accountCreatedUser: (String) -> String = { "Account created! Welcome to HealthUp, $it!" }
    override val defaultSettingsRestored = "Default settings restored."
    override val signedOutMessage = "Logged out successfully."
    override val accountDeletedMessage = "Your account has been deleted."
    override val sessionFinishedMessage = "Workout completed!"
    override val challengeRewardEarned: (Int, Int) -> String = { rank, hp -> "+$rank rank pts, +$hp HP!" }
    override val historyClearedMessage = "Workout history cleared."
    override val passwordChangedSuccess = "Password changed successfully."
    override val rateAppThankYou = "Thank you for your rating on Google Play!"
    override val nameCannotBeEmpty = "Name cannot be empty."
    override val enterCurrentPassword = "Enter current password."
    override val passwordMinLength = "New password must be at least 6 characters."
    override val passwordsDoNotMatch = "New passwords do not match."
    override val passwordMustBeDifferent = "New password must be different from current password."
    override val passwordChangeError = "Failed to change password."
    override val accountDeleteError = "Failed to delete account."
    override val emailAndPasswordRequired = "Please enter your email and password."
    override val invalidEmailFormat = "Please enter a valid email address."
    override val loginFailedRetry = "Login failed. Please try again."
    override val enterNameOrNickname = "Please enter your name or nickname."
    override val nameMaxLength = "Name or nickname can have at most 30 characters."
    override val registrationFailedRetry = "Registration failed. Please try again."
    override val resendLinkSuccess = "Sent again! Check your inbox and SPAM."
    override val resendLinkFailed = "Failed to resend. Please try again."
    override val resetLinkFailed = "Failed to send reset link. Check your email."
    override val rateRating5 = "Awesome! Thank you for your support!"
    override val rateRating4 = "Very good! We are constantly improving."
    override val rateRating3 = "Thank you! We are working on new features."
    override val rateRatingDefault = "We are working to make stations work even better!"
}
