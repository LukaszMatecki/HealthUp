<p align="center">
  <a href="#spis-treści">
    <img src="https://flagcdn.com/w40/pl.png" alt="Wersja Polska" title="Wersja Polska">
  </a>
  &nbsp;
  <a href="#table-of-contents">
    <img src="https://flagcdn.com/w40/gb.png" alt="English Version" title="English Version">
  </a>
</p>

<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=kotlin,androidstudio,apple,firebase,gcp,gradle,git" alt="Technology Stack" />
  </a>
</p>

# HealthUp: Miejski system Zdrowia i ruchu

<a id="wersja-polska"></a>

**HealthUp** to ekosystem wspierający profilaktykę zdrowotną, rozwój zdrowych nawyków i system mikronagród miejskich w Łodzi, tworzony w technologiach **Kotlin Multiplatform** i **Compose Multiplatform**. Projekt wykorzystuje potencjał ponad 120 istniejących siłowni plenerowych w łódzkich parkach, zachęcając mieszkańców do regularnej aktywności fizycznej. Łączy budowanie trwałych nawyków ruchowych, zgodnych z zaleceniami WHO, z systemem natychmiastowych korzyści, obejmującym m.in. darmowe bilety MPK Łódź oraz zniżki na basen Fala i atrakcje EC1. Dzięki temu HealthUp łączy troskę o zdrowie z lokalną infrastrukturą i realnymi korzyściami dla mieszkańców.


---

<a id="spis-treści"></a>

## Spis Treści

1. [Architektura KMP i współdzielenie kodu](#architektura-kmp-i-współdzielenie-kodu)
2. [Przepływ danych](#przepływ-danych)
3. [Główne moduły aplikacji](#główne-moduły-aplikacji)
4. [Prezentacja wizualna (zrzuty ekranu)](#prezentacja-wizualna)
5. [Struktura projektu](#struktura-projektu)
6. [Wykorzystane technologie i biblioteki](#wykorzystane-technologie-i-biblioteki)
7. [Uruchomienie projektu i testy](#uruchomienie-projektu-i-testy)
8. [Pobieranie aplikacji i wydania (GitHub Releases)](#pobieranie-aplikacji-i-wydania)
9. [Usługi chmurowe Firebase](#usługi-chmurowe-firebase)
10. [Licencja](#licencja)
11. [Executive Summary (English)](#table-of-contents)

---

## Architektura KMP i współdzielenie kodu

Projekt został zbudowany z wykorzystaniem architektury **Kotlin Multiplatform (KMP)** oraz **Compose Multiplatform**. Całość logiki biznesowej, stanu, kalkulacji energetycznych oraz warstwy widoków UI jest w pełni współdzielona pomiędzy platformami Android i iOS, bez konieczności dublowania kodu.

### Struktura modułów współdzielonych

* **Moduł `/shared/src/commonMain`**: zawiera cały współdzielony interfejs w Compose Multiplatform, modele domenowe, stan aplikacji (MVI/MVVM), kalkulator redukcji emisji dwutlenku węgla oraz silnik osiągnięć i wyzwań tygodniowych.
* **Moduł `/shared/src/androidMain`**: implementuje platformowe mechanizmy systemu Android za pomocą wzorca `expect` / `actual`. Obejmuje integrację z Google Maps SDK for Android (`GoogleMapView.android.kt`), natywną obsługę kamery Jetpack CameraX (`CameraPreview.android.kt`) oraz klienta usług Google Play Services i Firebase Android SDK.
* **Moduł `/shared/src/iosMain`**: dostarcza mosty integracyjne dla systemu iOS, w tym kontroler `MainViewController.kt` osadzający drzewo komponentów Compose w kontrolerze UIViewController.
* **Katalog `/iosApp`**: reprezentuje natywny projekt Xcode dla systemu iOS, stanowiący powłokę wejściową dla aplikacji na urządzeniach Apple (`iOSApp.swift`, `ContentView.swift`).

---

## Przepływ danych

```mermaid
flowchart LR
    A["Stacja QR"] -->|Moc| B["Aplikacja HealthUp"]
    B -->|Telemetria| C["Kalkulator"]
    C -->|Punkty VP| D["Katalog Nagród"]
    C -->|Ranking| E["Poziomy"]
```

* **Autoryzacja stacji:** Zeskanowanie kodu QR urządzenia za pomocą kamery weryfikuje fizyczną obecność na stacji.
* **Rejestracja sesji:** Aplikacja mierzy moc chwilową, zlicza wytworzoną energię i estymuje redukcję emisji CO2.
* **Grywalizacja i nagrody:** Zakończona sesja zwiększa punkty w rankingu i zasila portfel użytkownika o zdobyte HealthPoints, które można wymienić na kody rabatowe i zniżki.

---

## Główne moduły aplikacji

### Pulpit główny i metryki
Główny ekran prezentuje bieżący poziom użytkownika, liczbę punktów rankingowych, zaoszczędzone CO2 oraz dzienny rozkład energii na dynamicznym wykresie. Moduł FusedLocationProvider automatycznie wylicza odległość w metrach do najbliższej stacji w Łodzi.

### Interaktywna mapa stacji w Łodzi
Oparta o Google Maps Compose z customowymi znacznikami stacji i wskaźnikami statusu. Kliknięcie stacji automatycznie animuje kamerę ze zbliżeniem i synchronizuje kartę urządzenia na liście pod mapą.

### Skaner kodów QR
Wykorzystuje Jetpack CameraX do błyskawicznego odczytu identyfikatora stacji i dodatkowo zawiera wbudowany symulator sprzętowy na potrzeby prezentacji.

### Monitor treningu w czasie rzeczywistym
Przejrzysty interfejs z animowanym wskaźnikiem generowanej mocy, zliczaniem watogodzin i spalonych kalorii oraz pauzą w przypadku braku aktywności lub przerwy.

### System grywalizacji, wyzwań i nagród
Trzydzieści osiągnięć z wieloma poziomami zaawansowania, odnawialne wyzwania tygodniowe oraz katalog kuponów zniżkowych (np. bilety MPK Łódź, zniżki w sklepach parnerskich, gadżety sportowe) generowanych w postaci unikalnych kodów.

---

## Logo aplikacji

<p align="center">
  <img src="voltup_app_icon.png" width="128" height="128" alt="Logo aplikacji HealthUp" />
</p>
<p align="center">
  <em>Oficjalna ikona aplikacji HealthUp</em>
</p>

---

## Prezentacja wizualna

Wszystkie ekrany zostały zaprojektowane zgodnie z wytycznymi Material Design 3 z pełną obsługą trybu ciemnego i jasnego, nowoczesną nawigacją gestami (poziomy pasek gestów Pixel 9 Pro) oraz czystym paskiem stanu (Android Demo Mode).

### Pulpit główny, ranking miejski i mapa stacji (Tryb ciemny)

<p align="center">
  <img src="voltup_screens/z_ramka/ciemny/08_Pulpit_Glowny_ciemny_ramka.png" width="31%" alt="Pulpit Główny" />
  <img src="voltup_screens/z_ramka/ciemny/09_Pulpit_Ranking_I_Wyzwania_ciemny_ramka.png" width="31%" alt="Ranking i Wyzwania" />
  <img src="voltup_screens/z_ramka/ciemny/10_Mapa_Stacji_ciemny_ramka.png" width="31%" alt="Mapa Stacji" />
</p>
<p align="center">
  <em>Pulpit główny z bilansem energii &bull; Miejska tabela liderów i wyzwania tygodniowe &bull; Interaktywna mapa łódzkich siłowni plenerowych</em>
</p>

### Trening kinetyczny na żywo, skaner i osiągnięcia

<p align="center">
  <img src="voltup_screens/z_ramka/ciemny/18_Aktywny_Trening_Live_ciemny_ramka.png" width="31%" alt="Aktywny Trening Live" />
  <img src="voltup_screens/z_ramka/ciemny/19_Skaner_QR_ciemny_ramka.png" width="31%" alt="Skaner QR" />
  <img src="voltup_screens/z_ramka/ciemny/16_Osiagniecia_I_Odznaki_ciemny_ramka.png" width="31%" alt="Osiągnięcia i Odznaki" />
</p>
<p align="center">
  <em>Dynamiczny kokpit treningu z mocą w watach i prędkościomierzem &bull; Skaner optyczny QR stacji &bull; System 30 odznak miejskich i progres</em>
</p>

### Katalog mikronagród, aktywne vouchery i profil

<p align="center">
  <img src="voltup_screens/z_ramka/ciemny/11_Katalog_Nagrod_ciemny_ramka.png" width="31%" alt="Katalog Nagród" />
  <img src="voltup_screens/z_ramka/ciemny/13_Nagrody_Archiwum_I_Uzyte_ciemny_ramka.png" width="31%" alt="Moje Kody i Vouchery" />
  <img src="voltup_screens/z_ramka/ciemny/14_Profil_Uzytkownika_ciemny_ramka.png" width="31%" alt="Profil Użytkownika" />
</p>
<p align="center">
  <em>Katalog nagród (MPK Łódź, Fala, EC1) &bull; Moje Kody i aktywne bilety z kodami kreskowymi &bull; Profil użytkownika i ranga Miejski Odkrywca</em>
</p>

### Historia treningów, przewodnik FAQ i ustawienia

<p align="center">
  <img src="voltup_screens/z_ramka/ciemny/15_Historia_Treningow_ciemny_ramka.png" width="31%" alt="Historia Treningów" />
  <img src="voltup_screens/z_ramka/ciemny/20_Przewodnik_I_FAQ_ciemny_ramka.png" width="31%" alt="Przewodnik i FAQ" />
  <img src="voltup_screens/z_ramka/ciemny/21_Ustawienia_Aplikacji_ciemny_ramka.png" width="31%" alt="Ustawienia Aplikacji" />
</p>
<p align="center">
  <em>Historia sesji kinetycznych &bull; Baza wiedzy FAQ i zasady generowania prądu &bull; Ustawienia motywu i języka</em>
</p>

### Onboarding i uwierzytelnianie (Tryb jasny)

<p align="center">
  <img src="voltup_screens/z_ramka/jasny/02_Onboarding_Krok1_jasny_ramka.png" width="31%" alt="Onboarding Powitanie" />
  <img src="voltup_screens/z_ramka/jasny/05_Logowanie_jasny_ramka.png" width="31%" alt="Ekran Logowania" />
  <img src="voltup_screens/z_ramka/jasny/06_Rejestracja_jasny_ramka.png" width="31%" alt="Ekran Rejestracji" />
</p>
<p align="center">
  <em>Wprowadzenie do aplikacji &bull; Formularz logowania &bull; Rejestracja nowego mieszkańca</em>
</p>

> Kompletny zestaw 88 zrzutów ekranu w ultrawysokiej rozdzielczości (Google Pixel 9 Pro z paskiem gestów, w wersjach bez ramki oraz z oficjalną ramką w motywach ciemnym i jasnym) znajduje się w katalogu `voltup_screens/`.

---

## Struktura projektu

```text
VoltUp/
├── androidApp/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── res/
│   └── google-services.json
│
├── iosApp/
│   ├── iosApp/
│   │   ├── iOSApp.swift
│   │   └── ContentView.swift
│   └── iosApp.xcodeproj/
│
├── voltup_screens/
│   ├── bez_ramki/
│   │   ├── ciemny/
│   │   └── jasny/
│   └── z_ramka/
│       ├── ciemny/
│       └── jasny/
│
└── shared/
    └── src/
        ├── commonMain/kotlin/com/lumatech/voltup/
        │   ├── App.kt
        │   ├── Models.kt
        │   ├── FirebaseModels.kt
        │   ├── VoltUpAuthService.kt
        │   ├── VoltUpDatabaseService.kt
        │   ├── AchievementSystem.kt
        │   ├── StationsMapScreen.kt
        │   ├── WorkoutScreen.kt
        │   ├── QrScannerScreen.kt
        │   ├── RewardsScreen.kt
        │   ├── ProfileScreen.kt
        │   ├── SettingsScreen.kt
        │   ├── AuthScreens.kt
        │   ├── Localization.kt
        │   └── Theme.kt
        │
        ├── androidMain/kotlin/com/lumatech/voltup/
        │   ├── GoogleMapView.android.kt
        │   ├── CameraPreview.android.kt
        │   └── FirebaseServices.android.kt
        │
        └── iosMain/kotlin/com/lumatech/voltup/
            ├── MainViewController.kt
            ├── GoogleMapView.ios.kt
            └── FirebaseServices.ios.kt
```

---

## Wykorzystane technologie i biblioteki

* **Język bazowy:** Kotlin 2.0 (logika domenowa, coroutines, współdzielony kod dla Androida i iOS)
* **Interfejs graficzny:** Compose Multiplatform (deklaratywne UI, wspólny design system Material Design 3)
* **Usługi chmurowe:** Google Firebase (Firebase Authentication, Cloud Firestore NoSQL)
* **Mapy i geolokalizacja:** Google Maps Compose SDK for Android oraz FusedLocationProviderClient
* **Aparat i optyka:** Jetpack CameraX (analiza kodów QR w czasie rzeczywistym)
* **Zarządzanie stanem:** StateFlow, MutableStateFlow i architektura reaktywna
* **Typografia i zasoby:** Rodzina czcionek Outfit zintegrowana przez Compose Resources

---

## Uruchomienie projektu i testy

### Wymagania wstępne
* Android Studio (wersja Iguana, Jellyfish lub nowsza)
* Java Development Kit (JDK 17 lub JDK 21)
* Android SDK z API level minimum 26 (Android 8.0 Oreo)
* Do kompilacji wersji na iOS: macOS z zainstalowanym środowiskiem Xcode 15+

### Kompilacja i uruchomienie (Android)

System Windows (PowerShell):
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
./gradlew.bat :androidApp:assembleDebug
./gradlew.bat :androidApp:installDebug
```

System macOS lub Linux:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

### Uruchomienie wersji na iOS
Otwórz katalog `/iosApp` w środowisku Xcode i uruchom projekt na symulatorze lub fizycznym urządzeniu iPhone.

### Testy jednostkowe
Uruchomienie zestawu testów współdzielonych:

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

---

<a id="pobieranie-aplikacji-i-wydania"></a>

## Pobieranie aplikacji i wydania (GitHub Releases)

Wszystkie oficjalne wydania binarne aplikacji HealthUp są automatycznie budowane i publikowane w sekcji [GitHub Releases](https://github.com/LukaszMatecki/VoltUp/releases).

Każde wydanie zawiera kompletny zestaw gotowych pakietów instalacyjnych:

| Nazwa pliku | Platforma | Format | Przeznaczenie |
| :--- | :--- | :--- | :--- |
| **`HealthUp-Android-debug.apk`** | Android | APK | **Zalecany do testów:** Gotowy do natychmiastowej instalacji na smartfonie Android (podpisany kluczem debugującym). |
| **`HealthUp-Android-release-unsigned.apk`** | Android | APK | Zoptymalizowany pakiet Release pod kątem wydajności i minimalnego rozmiaru. |
| **`HealthUp-Android-release.aab`** | Android | AAB | Oficjalny format Android App Bundle gotowy do publikacji w Google Play Console. |
| **`HealthUp-iOS-Device.ipa`** | iOS | IPA | Paczka instalacyjna dla fizycznych urządzeń iPhone (instalacja przez AltStore, Sideloadly lub Apple Configurator). |
| **`HealthUp-iOS-Simulator.app.zip`** | iOS Simulator | ZIP / .app | Paczka dla symulatorów Xcode (instalacja metodą drag-and-drop na okno symulatora). |

### Jak publikować nowe wydania?
Repozytorium posiada w pełni zautomatyzowany potok CI/CD oparty na GitHub Actions ([`.github/workflows/release.yml`](.github/workflows/release.yml)):

* **Opcja 1 (Automatyczna przez Git Tag):**
  Utwórz i wyślij tag wersji (np. `v1.0.0`):
  ```bash
  git tag -a v1.0.0 -m "Release v1.0.0"
  git push origin v1.0.0
  ```
  GitHub Actions automatycznie skompiluje pakiety dla Androida (APK, AAB) oraz iOS (IPA, Simulator), wygeneruje changelog na podstawie commitów i utworzy oficjalny GitHub Release z załączonymi plikami.

* **Opcja 2 (Ręczna przez interfejs GitHub Actions):**
  1. Przejdź do zakładki **Actions** w repozytorium na GitHubie.
  2. Z menu bocznego wybierz workflow **Publish Release**.
  3. Kliknij przycisk **Run workflow**, podaj numer wersji (np. `v1.0.0`) i zatwierdź.

---

## Usługi chmurowe Firebase

### Kolekcje w Cloud Firestore
* `devices`: lista stacji kinetycznych w Łodzi (współrzędne GPS, nazwa, stan techniczny, moc nominalna).
* `users`: profile użytkowników, punkty rankingowe, saldo HealthPoints, stopień doświadczenia i serie treningowe.
* `workouts`: archiwum zakończonych treningów (czas trwania, średnia moc, watogodziny, ekwiwalent CO2).
* `user_achievements`: stany postępu oraz odblokowane odznaki powiązane z identyfikatorem użytkownika.

### Konfiguracja Google Maps API
Klucz Google Maps API jest zdefiniowany w konfiguracji manifestu oraz modułu mapowego. Wymaga włączonej usługi Maps SDK for Android w konsoli Google Cloud Platform.

---

## Licencja

Projekt HealthUp jest udostępniany na warunkach licencji **MIT**. Szczegóły znajdują się w pliku [LICENSE](LICENSE).

---
---

<a id="table-of-contents"></a>
<a id="english-version"></a>

# HealthUp: Urban Health and Movement System

HealthUp is a cross-platform mobile solution built with **Kotlin Multiplatform** and **Compose Multiplatform**. It bridges urban outdoor workout infrastructure with preventive cardio health, gamification, and municipal micro-rewards across the Lodz metropolitan area.

The system empowers citizens to activate underutilized outdoor fitness stations, track calories and cardio intensity via hardware-backed Proof-of-Effort, charge their mobile devices on-site, and redeem earned reward points for public transit tickets (MPK Łódź) and cultural attractions.

---

## Table of Contents

1. [Key Architectural Highlights](#key-architectural-highlights)
2. [Visual Presentation and UI Showcase](#visual-presentation-and-ui-showcase)
3. [Technology Stack](#technology-stack-1)
4. [Quick Start (Building and Testing)](#quick-start-building-and-testing)
5. [Releases & Downloads](#releases--downloads)
6. [License](#license-1)

---

## Key Architectural Highlights

* **100% Shared UI and Business Logic:** Built with Compose Multiplatform, delivering an identical native look and feel on Android and iOS from a single common codebase (`/shared/src/commonMain`).
* **Platform Integrations via expect/actual:** Hardware-specific capabilities such as Jetpack CameraX optical scanning, Google Maps SDK, and location providers are seamlessly plugged into the shared presentation layer.
* **Real-Time Kinetic Telemetry Engine:** Live tracking of instantaneous power (Watts), cumulative electrical energy (Wh), and carbon offset estimation.
* **Gamification and Micro-Economy:** Thirty progressive achievements, weekly challenges, and a HealthPoints wallet redeemable for partner discount vouchers.
* **Cloud Architecture:** Powered by Google Firebase (Firebase Authentication, Cloud Firestore NoSQL).

---

## Visual Presentation and UI Showcase

> To explore the complete visual showcase of the application in both Dark and Light themes, please refer to the [Prezentacja wizualna (zrzuty ekranu)](#prezentacja-wizualna) section above.
All high-resolution screenshots are organized in the `voltup_screens/` directory.

---

## Technology Stack

* **Core Language:** Kotlin 2.0 (Coroutines, Flow, Multiplatform)
* **UI Framework:** Compose Multiplatform with Material Design 3
* **Cloud Backend:** Google Firebase (Auth, Cloud Firestore)
* **Cartography & Geolocation:** Google Maps SDK Compose and FusedLocationProviderClient
* **Camera & Scanner:** Jetpack CameraX with real-time barcode analysis
* **Typography:** Outfit font family integrated via Compose Resources

---

## Quick Start (Building and Testing)

### Android Build
```bash
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

### iOS Execution
Open the `/iosApp` project in Xcode 15+ and run on an iOS simulator or physical device.

### Running Shared Unit Tests
```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

---

<a id="releases--downloads"></a>

## Releases & Downloads

Binary packages for Android and iOS are automatically compiled and published with every release under [GitHub Releases](https://github.com/LukaszMatecki/VoltUp/releases).

| Asset | Platform | Type | Note |
| :--- | :--- | :--- | :--- |
| **`HealthUp-Android-debug.apk`** | Android | APK | **Recommended for quick testing:** Pre-signed with debug key, installs directly on devices. |
| **`HealthUp-Android-release-unsigned.apk`** | Android | APK | Production release binary optimized for performance. |
| **`HealthUp-Android-release.aab`** | Android | AAB | Official Android App Bundle ready for Google Play Store upload. |
| **`HealthUp-iOS-Device.ipa`** | iOS | IPA | Packaged application for physical iOS devices (AltStore / Sideloadly / TestFlight). |
| **`HealthUp-iOS-Simulator.app.zip`** | iOS Simulator | ZIP / .app | Packaged application for Xcode Simulator drag-and-drop installation. |

### Publishing New Releases
1. **Via Git Tag:**
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```
2. **Via GitHub Actions UI:** Navigate to **Actions** -> **Publish Release** -> **Run workflow** and specify the tag version.

---

## License

HealthUp is distributed under the open-source **MIT License**. See the [LICENSE](LICENSE) file for complete details.
