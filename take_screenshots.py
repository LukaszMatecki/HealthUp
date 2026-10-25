#!python3.12
"""
VoltUp - Zautomatyzowany generator zrzutów ekranu (Pixel 9 Pro / Gesture Bar)
=============================================================================
Ten skrypt automatycznie:
 1. Wykrywa lub uruchamia emulator telefonu Pixel 9 Pro (AVD: Pixel_9_Pro_API_36 / Pixel_9_Pro_XL).
 2. Włącza nowoczesną nawigację gestami (poziomy pasek gestów, ukrywając stare przyciski funkcyjne).
 3. Weryfikuje i instaluje aplikację VoltUp (debug APK).
 4. Aktywuje Android Demo Mode (czysty pasek statusu: 09:41, 100% baterii, pełne Wi-Fi i 5G).
 5. Przechodzi przez wszystkie ekrany, stany, podzakładki i animacje aplikacji.
 6. Zapisuje wysokiej jakości zrzuty ekranu (.png) dla wybranego motywu (Dark / Light / All).

Użycie:
    py take_screenshots.py
    py -3.12 take_screenshots.py
    .\run_screenshots.ps1
"""

import argparse
import os
import shutil
import subprocess
import sys
import time
from pathlib import Path


def print_step(icon: str, text: str):
    print(f"\n{icon} \033[1;36m{text}\033[0m")


def print_success(text: str):
    print(f"   \033[32m✔ {text}\033[0m")


def print_info(text: str):
    print(f"   \033[90mℹ {text}\033[0m")


def print_warn(text: str):
    print(f"   \033[33m⚠ {text}\033[0m")


def print_error(text: str):
    print(f"   \033[31m✖ {text}\033[0m")


def find_android_tool(tool_name: str) -> str:
    """Wyszukuje adb lub emulator w PATH lub domyślnych katalogach Android SDK."""
    found = shutil.which(tool_name)
    if found:
        return found

    ext = ".exe" if os.name == "nt" else ""
    local_app_data = os.environ.get("LOCALAPPDATA", "")
    candidates = [
        os.path.join(local_app_data, "Android", "Sdk", "platform-tools", f"{tool_name}{ext}"),
        os.path.join(local_app_data, "Android", "Sdk", "emulator", f"{tool_name}{ext}"),
        os.path.expanduser(f"~/Library/Android/sdk/platform-tools/{tool_name}{ext}"),
        os.path.expanduser(f"~/Library/Android/sdk/emulator/{tool_name}{ext}"),
        os.path.expanduser(f"~/Android/Sdk/platform-tools/{tool_name}{ext}"),
        os.path.expanduser(f"~/Android/Sdk/emulator/{tool_name}{ext}"),
    ]
    for c in candidates:
        if os.path.isfile(c):
            return c

    raise FileNotFoundError(f"Nie odnaleziono narzędzia '{tool_name}'. Upewnij się, że Android SDK jest zainstalowane.")


class VoltUpScreenshotRunner:
    def __init__(self, avd_name: str | None = None, theme: str = "ciemny", output_dir: str = "VoltUp_Screenshots"):
        self.adb = find_android_tool("adb")
        self.emulator = find_android_tool("emulator")
        self.requested_avd = avd_name
        self.theme = theme.lower()
        self.root_dir = Path(__file__).resolve().parent
        self.output_base = self.root_dir / output_dir
        self.package_name = "com.lumatech.voltup"
        self.main_activity = "com.lumatech.voltup.MainActivity"
        self.screen_width = 1080
        self.screen_height = 2400

    def run_cmd(self, cmd: list[str], check: bool = True, capture_output: bool = True) -> str:
        res = subprocess.run(cmd, check=check, capture_output=capture_output, text=True)
        return res.stdout.strip() if capture_output else ""

    def run_adb(self, *args: str, check: bool = True) -> str:
        cmd = [self.adb] + list(args)
        return self.run_cmd(cmd, check=check)

    def is_device_running(self) -> bool:
        out = self.run_adb("devices")
        lines = [line.strip() for line in out.splitlines() if line.strip() and not line.startswith("List of")]
        for l in lines:
            parts = l.split()
            if len(parts) >= 2 and parts[1] == "device":
                return True
        return False

    def list_avds(self) -> list[str]:
        out = self.run_cmd([self.emulator, "-list-avds"])
        return [l.strip() for l in out.splitlines() if l.strip()]

    def ensure_emulator(self):
        print_step("📱", "Weryfikacja emulatora Android (Pixel 9 Pro)...")
        if self.is_device_running():
            print_success("Aktywne urządzenie/emulator jest już podłączone i gotowe.")
            return

        avds = self.list_avds()
        if not avds:
            raise RuntimeError("Nie znaleziono żadnego emulatora AVD w systemie. Utwórz Pixel 9 Pro w Android Studio Device Manager.")

        chosen_avd = self.requested_avd
        if not chosen_avd or chosen_avd not in avds:
            # Domyślnie preferujemy Pixel 9 Pro
            priority = ["Pixel_9_Pro_API_36", "Pixel_9_Pro_XL", "Pixel_8", "Pixel_5"]
            for p in priority:
                if p in avds:
                    chosen_avd = p
                    break
            if not chosen_avd:
                chosen_avd = avds[0]

        print_info(f"Uruchamianie emulatora: {chosen_avd}...")
        emulator_cmd = [
            self.emulator,
            "-avd", chosen_avd,
            "-no-boot-anim",
        ]
        subprocess.Popen(emulator_cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        print_info("Oczekiwanie na rozruch emulatora (wait-for-device)...")
        self.run_adb("wait-for-device")

        print_info("Czekanie na pełne uruchomienie systemu Android...")
        timeout = 180
        start_time = time.time()
        while time.time() - start_time < timeout:
            boot = self.run_adb("shell", "getprop", "sys.boot_completed", check=False)
            anim = self.run_adb("shell", "getprop", "init.svc.bootanim", check=False)
            if boot == "1" and (anim == "stopped" or anim == ""):
                print_success(f"Emulator {chosen_avd} pomyślnie uruchomiony i gotowy!")
                time.sleep(3)
                return
            time.sleep(2)

        raise TimeoutError("Przekroczono limit czasu oczekiwania na uruchomienie emulatora.")

    def enable_gesture_navigation(self):
        """Wymusza tryb nawigacji gestami (poziomy pasek gestów, ukrywa 3 przyciski funkcyjne)."""
        print_step("👆", "Konfiguracja paska nawigacji (tryb gestów zamiast przycisków funkcyjnych)...")
        try:
            # 1. Wyłącz tradycyjne przyciski nawigacyjne (3-button / 2-button)
            self.run_adb("shell", "cmd", "overlay", "disable", "com.android.internal.systemui.navbar.threebutton", check=False)
            self.run_adb("shell", "cmd", "overlay", "disable", "com.android.internal.systemui.navbar.twobutton", check=False)
            
            # 2. Włącz nakładkę nawigacji gestami (pozioma belka u dołu)
            self.run_adb("shell", "cmd", "overlay", "enable", "com.android.internal.systemui.navbar.gestural", check=False)
            
            # 3. Ustawienie systemowe navigation_mode = 2 (0 = 3 przyciski, 1 = 2 przyciski, 2 = gesty)
            self.run_adb("shell", "settings", "put", "secure", "navigation_mode", "2", check=False)
            
            time.sleep(1)
            print_success("Włączono poziomy pasek gestów (Gesture Navigation Bar)!")
        except Exception as e:
            print_warn(f"Nie udało się wymusić nakładki gestów (możliwe, że jest już aktywna): {e}")

    def update_screen_dimensions(self):
        try:
            out = self.run_adb("shell", "wm", "size")
            if "size:" in out:
                dim = out.split("size:")[-1].strip().split("x")
                self.screen_width = int(dim[0])
                self.screen_height = int(dim[1])
                print_info(f"Rozdzielczość ekranu Pixel 9 Pro: {self.screen_width} x {self.screen_height} px")
        except Exception:
            pass

    def ensure_app_installed(self):
        print_step("📦", "Weryfikacja instalacji aplikacji VoltUp...")
        installed = self.run_adb("shell", "pm", "list", "packages", self.package_name, check=False)
        if self.package_name in installed:
            print_success(f"Pakiet {self.package_name} jest już zainstalowany.")
            return

        print_info("Aplikacja nie jest zainstalowana. Poszukiwanie pliku APK...")
        candidate_apks = [
            self.root_dir / "androidApp" / "build" / "outputs" / "apk" / "debug" / "androidApp-debug.apk",
            self.root_dir / "VoltUp_Builds" / "VoltUp-debug.apk",
            self.root_dir / "VoltUp_Builds" / "VoltUp-release-unsigned.apk",
        ]
        apk_to_install = None
        for apk in candidate_apks:
            if apk.is_file():
                apk_to_install = apk
                break

        if not apk_to_install:
            print_info("Brak prekompilowanego pliku APK. Rozpoczynanie kompilacji debug APK...")
            gradlew = self.root_dir / ("gradlew.bat" if os.name == "nt" else "gradlew")
            self.run_cmd([str(gradlew), ":androidApp:assembleDebug"], check=True, capture_output=False)
            apk_to_install = candidate_apks[0]

        print_info(f"Instalowanie {apk_to_install.name} na emulatorze...")
        self.run_adb("install", "-r", "-d", str(apk_to_install))
        print_success("Aplikacja VoltUp pomyślnie zainstalowana!")

    def setup_demo_mode(self, enable: bool = True):
        """Włącza czysty pasek stanu (godzina 09:41, bateria 100%, pełny sygnał sieci bez zbędnych ikon)."""
        try:
            if enable:
                self.run_adb("shell", "settings", "put", "global", "sysui_demo_allowed", "1", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "enter", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "clock", "-e", "hhmm", "0941", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "battery", "-e", "level", "100", "-e", "plugged", "false", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "network", "-e", "wifi", "show", "-e", "level", "4", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "network", "-e", "mobile", "show", "-e", "datatype", "5g", "-e", "level", "4", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "notifications", "-e", "visible", "false", check=False)
            else:
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "exit", check=False)
        except Exception:
            pass

    def launch_screen(self, screen: str | None = None, theme_mode: str = "DARK"):
        """Uruchamia MainActivity ze wskazanym ekranem i motywem poprzez Intent extras."""
        self.run_adb("shell", "am", "force-stop", self.package_name, check=False)
        time.sleep(0.4)

        cmd = ["shell", "am", "start", "-n", f"{self.package_name}/{self.main_activity}"]
        if screen:
            cmd.extend(["--es", "SCREEN", screen])
        if theme_mode:
            cmd.extend(["--es", "THEME", theme_mode])

        self.run_adb(*cmd)

    def swipe(self, x1_ratio: float, y1_ratio: float, x2_ratio: float, y2_ratio: float, duration_ms: int = 400):
        x1 = int(self.screen_width * x1_ratio)
        y1 = int(self.screen_height * y1_ratio)
        x2 = int(self.screen_width * x2_ratio)
        y2 = int(self.screen_height * y2_ratio)
        self.run_adb("shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration_ms))

    def tap(self, x_ratio: float, y_ratio: float):
        x = int(self.screen_width * x_ratio)
        y = int(self.screen_height * y_ratio)
        self.run_adb("shell", "input", "tap", str(x), str(y))

    def take_screenshot(self, target_path: Path):
        target_path.parent.mkdir(parents=True, exist_ok=True)
        with open(target_path, "wb") as f:
            cmd = [self.adb, "exec-out", "screencap", "-p"]
            subprocess.run(cmd, check=True, stdout=f)

    def capture_suite_for_theme(self, theme_mode: str, folder_name: str):
        target_dir = self.output_base / folder_name
        target_dir.mkdir(parents=True, exist_ok=True)

        theme_tag = "ciemny" if theme_mode == "DARK" else "jasny"
        print_step("📸", f"Rozpoczynanie sesji zrzutów ekranu dla motywu: {theme_mode} -> {folder_name}/")

        screenshots = [
            # 1. Splashscreen (ładowanie początkowe)
            {
                "file": f"01_Splashscreen_{theme_tag}.png",
                "label": "Splashscreen (Logo i animacja startowa)",
                "action": lambda: (self.launch_screen(None, theme_mode), time.sleep(0.45)),
            },
            # 2. Ekrany powitalne (Onboarding - Krok 1)
            {
                "file": f"02_Onboarding_Krok1_{theme_tag}.png",
                "label": "Ekrany powitalne (Onboarding - Krok 1/3)",
                "action": lambda: (self.launch_screen("ONBOARDING", theme_mode), time.sleep(1.5)),
            },
            # 3. Onboarding - Krok 2 (przewinięcie)
            {
                "file": f"03_Onboarding_Krok2_{theme_tag}.png",
                "label": "Ekrany powitalne (Onboarding - Krok 2/3)",
                "action": lambda: (self.swipe(0.85, 0.5, 0.15, 0.5, 350), time.sleep(0.8)),
            },
            # 4. Onboarding - Krok 3 (przewinięcie)
            {
                "file": f"04_Onboarding_Krok3_{theme_tag}.png",
                "label": "Ekrany powitalne (Onboarding - Krok 3/3)",
                "action": lambda: (self.swipe(0.85, 0.5, 0.15, 0.5, 350), time.sleep(0.8)),
            },
            # 5. Logowanie
            {
                "file": f"05_Logowanie_{theme_tag}.png",
                "label": "Ekran logowania",
                "action": lambda: (self.launch_screen("LOGIN", theme_mode), time.sleep(1.2)),
            },
            # 6. Rejestracja
            {
                "file": f"06_Rejestracja_{theme_tag}.png",
                "label": "Ekran rejestracji",
                "action": lambda: (self.launch_screen("REGISTER", theme_mode), time.sleep(1.2)),
            },
            # 7. Reset hasła
            {
                "file": f"07_Reset_Hasla_{theme_tag}.png",
                "label": "Ekran resetu hasła",
                "action": lambda: (self.launch_screen("FORGOT_PASSWORD", theme_mode), time.sleep(1.2)),
            },
            # 8. Pulpit główny (Dashboard)
            {
                "file": f"08_Pulpit_Glowny_{theme_tag}.png",
                "label": "Pulpit główny (Dashboard, statystyki energii, watogodziny)",
                "action": lambda: (self.launch_screen("HOME", theme_mode), time.sleep(1.5)),
            },
            # 9. Pulpit - Ranking i Wyzwania tygodniowe (przewinięcie)
            {
                "file": f"09_Pulpit_Ranking_I_Wyzwania_{theme_tag}.png",
                "label": "Pulpit - Tabela rankingu miejskiego i wyzwania tygodniowe",
                "action": lambda: (self.swipe(0.5, 0.75, 0.5, 0.35, 500), time.sleep(1.0)),
            },
            # 10. Mapa stacji kinetycznych
            {
                "file": f"10_Mapa_Stacji_{theme_tag}.png",
                "label": "Mapa siłowni plenerowych i stacji ładowania w Łodzi",
                "action": lambda: (self.launch_screen("MAP", theme_mode), time.sleep(2.0)),
            },
            # 11. Nagrody - Katalog miejski
            {
                "file": f"11_Katalog_Nagrod_{theme_tag}.png",
                "label": "Katalog nagród (bilety MPK Łódź, Fala, EC1)",
                "action": lambda: (self.launch_screen("REWARDS", theme_mode), time.sleep(1.5)),
            },
            # 12. Nagrody - Moje Kody / Aktywne kupony
            {
                "file": f"12_Nagrody_Moje_Kody_Aktywne_{theme_tag}.png",
                "label": "Nagrody - Moje Kody i aktywne vouchery",
                "action": lambda: (self.tap(0.75, 0.16), time.sleep(1.0)),
            },
            # 13. Nagrody - Archiwum biletów i zużyte
            {
                "file": f"13_Nagrody_Archiwum_I_Uzyte_{theme_tag}.png",
                "label": "Nagrody - Archiwum zrealizowanych biletów",
                "action": lambda: (self.swipe(0.5, 0.75, 0.5, 0.35, 500), time.sleep(1.0)),
            },
            # 14. Profil użytkownika
            {
                "file": f"14_Profil_Uzytkownika_{theme_tag}.png",
                "label": "Profil użytkownika (poziom Miejski Odkrywca, bilans CO2, serie)",
                "action": lambda: (self.launch_screen("PROFILE", theme_mode), time.sleep(1.5)),
            },
            # 15. Historia treningów
            {
                "file": f"15_Historia_Treningow_{theme_tag}.png",
                "label": "Historia treningów (spalone kalorie, watogodziny, czas trwania)",
                "action": lambda: (self.launch_screen("HISTORY", theme_mode), time.sleep(1.5)),
            },
            # 16. Osiągnięcia i odznaki (Góra)
            {
                "file": f"16_Osiagniecia_I_Odznaki_{theme_tag}.png",
                "label": "Osiągnięcia i odznaki miejskie (poziomy odblokowania)",
                "action": lambda: (self.launch_screen("ACHIEVEMENTS", theme_mode), time.sleep(1.5)),
            },
            # 17. Osiągnięcia - Dalsze poziomy (przewinięcie)
            {
                "file": f"17_Osiagniecia_Dalsze_Odznaki_{theme_tag}.png",
                "label": "Osiągnięcia - Zaawansowane odznaki i postęp",
                "action": lambda: (self.swipe(0.5, 0.75, 0.5, 0.3, 500), time.sleep(1.0)),
            },
            # 18. Aktywny trening kinetyczny (Live Workout)
            {
                "file": f"18_Aktywny_Trening_Live_{theme_tag}.png",
                "label": "Aktywny trening kinetyczny (telemetria, waty, kalorie, prędkość)",
                "action": lambda: (self.launch_screen("WORKOUT", theme_mode), time.sleep(1.5)),
            },
            # 19. Skaner kodów QR na stacji
            {
                "file": f"19_Skaner_QR_{theme_tag}.png",
                "label": "Skaner optyczny QR na stacji kinetycznej",
                "action": lambda: (self.launch_screen("QR", theme_mode), time.sleep(1.5)),
            },
            # 20. Przewodnik i FAQ
            {
                "file": f"20_Przewodnik_I_FAQ_{theme_tag}.png",
                "label": "Przewodnik, zasady generowania prądu i FAQ",
                "action": lambda: (self.launch_screen("HELP", theme_mode), time.sleep(1.5)),
            },
            # 21. Ustawienia aplikacji
            {
                "file": f"21_Ustawienia_Aplikacji_{theme_tag}.png",
                "label": "Ustawienia aplikacji (motyw, język, powiadomienia)",
                "action": lambda: (self.launch_screen("SETTINGS", theme_mode), time.sleep(1.5)),
            },
            # 22. Zarządzanie kontem
            {
                "file": f"22_Zarzadzanie_Kontem_{theme_tag}.png",
                "label": "Zarządzanie kontem i bezpieczeństwo",
                "action": lambda: (self.launch_screen("MANAGE_ACCOUNT", theme_mode), time.sleep(1.5)),
            },
        ]

        total = len(screenshots)
        for idx, item in enumerate(screenshots, start=1):
            out_file = target_dir / item["file"]
            print(f"[{idx:02d}/{total:02d}] 📸 {item['label']}...")
            try:
                item["action"]()
                self.take_screenshot(out_file)
                print_success(f"Zapisano -> {out_file.relative_to(self.root_dir)}")
            except Exception as e:
                print_error(f"Błąd podczas przechwytywania: {e}")

        print_step("✨", f"Zakończono generowanie {total} zrzutów dla motywu {theme_mode}!")
        print_info(f"Lokalizacja plików: {target_dir}")

    def execute(self):
        try:
            self.ensure_emulator()
            self.enable_gesture_navigation()
            self.update_screen_dimensions()
            self.ensure_app_installed()
            self.setup_demo_mode(True)

            if self.theme in ["ciemny", "dark", "all"]:
                self.capture_suite_for_theme("DARK", "ciemny")

            if self.theme in ["jasny", "light", "all"]:
                self.capture_suite_for_theme("LIGHT", "jasny")

            print_step("🎉", "Wszystkie zrzuty ekranu zostały pomyślnie wygenerowane!")
            print_info(f"Katalog bazowy: {self.output_base}")
        finally:
            self.setup_demo_mode(False)


def main():
    parser = argparse.ArgumentParser(description="Automatyczny generator zrzutów ekranu VoltUp na emulatorze Pixel 9 Pro")
    parser.add_argument("--avd", type=str, default="Pixel_9_Pro_API_36", help="Nazwa emulatora AVD (domyślnie: Pixel_9_Pro_API_36)")
    parser.add_argument("--theme", type=str, default="ciemny", choices=["ciemny", "jasny", "all", "dark", "light"], help="Motyw graficzny (ciemny / jasny / all)")
    parser.add_argument("--output", type=str, default="VoltUp_Screenshots", help="Katalog wyjściowy dla zrzutów ekranu")
    args = parser.parse_args()

    runner = VoltUpScreenshotRunner(avd_name=args.avd, theme=args.theme, output_dir=args.output)
    runner.execute()


if __name__ == "__main__":
    main()
