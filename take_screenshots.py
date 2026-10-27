#!python3.12
"""
HealthUp - Zautomatyzowany generator zrzutów ekranu (Pixel 9 Pro / Bez ramki i Z ramką)
=====================================================================================
Ten skrypt automatycznie:
 1. Wykrywa lub uruchamia emulator telefonu Pixel 9 Pro (AVD: Pixel_9_Pro_API_36 / Pixel_9_Pro_XL).
 2. Włącza nowoczesną nawigację gestami (poziomy pasek gestów, ukrywając 3 przyciski funkcyjne).
 3. Wyłącza systemowe animacje przejścia (likwiduje białą poświatę i rozmycia podczas przełączania).
 4. Aktywuje Android Demo Mode (czysty pasek statusu: 09:41, 100% baterii, pełne 5G i Wi-Fi).
 5. Stosuje spokojniejsze tempo: czeka na pełne załadowanie UI przed zrzutem i chwilę po nim.
 6. Przechodzi przez wszystkie ekrany, stany, podzakładki i animacje aplikacji.
 7. Generuje zrzuty w folderze 'voltup_screens/' w podziale na:
      - bez_ramki/ciemny/
      - bez_ramki/jasny/
      - z_ramka/ciemny/
      - z_ramka/jasny/
    (z wykorzystaniem oficjalnej ramki Google Pixel 9 Pro).

Użycie:
    py take_screenshots.py
    py take_screenshots.py --theme ciemny
    py take_screenshots.py --theme all
    .\run_screenshots.ps1
"""

import argparse
import os
import shutil
import subprocess
import sys
import time
from pathlib import Path
from PIL import Image, ImageDraw


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


class Pixel9ProFramer:
    """Odpowiada za osadzenie surowego zrzutu ekranu w oficjalnej ramce urządzenia Pixel 9 Pro."""

    def __init__(self):
        self.frame_dir = self._find_device_art_dir()
        self.back_img = None
        self.mask_img = None
        if self.frame_dir:
            try:
                self.back_img = Image.open(os.path.join(self.frame_dir, "back.webp")).convert("RGBA")
                self.mask_img = Image.open(os.path.join(self.frame_dir, "mask.webp")).convert("RGBA")
            except Exception as e:
                print_warn(f"Nie udało się załadować grafiki ramki Pixel 9 Pro: {e}")

    def _find_device_art_dir(self) -> str | None:
        candidates = [
            r"C:\Program Files\Android\Android Studio\plugins\android\resources\device-art-resources\pixel_9_pro",
            r"C:\Program Files\Android\Android Studio\plugins\android\resources\device-art-resources\pixel_9",
            r"C:\Program Files\Android\Android Studio\plugins\android\resources\device-art-resources\pixel_8_pro",
        ]
        for c in candidates:
            if os.path.isdir(c) and os.path.isfile(os.path.join(c, "back.webp")):
                return c
        return None

    def frame(self, screenshot_path: Path, output_path: Path):
        output_path.parent.mkdir(parents=True, exist_ok=True)
        screenshot = Image.open(screenshot_path).convert("RGBA")

        if self.back_img and self.mask_img:
            # Oficjalne wymiary Pixel 9 Pro:
            # Rozmiar całkowity: 1408 x 2974 px
            # Obszar wyświetlacza: 1280 x 2856 px na pozycji (60, 61) z promieniami narożników r=109
            target_w, target_h = 1280, 2856
            resized_screen = screenshot.resize((target_w, target_h), Image.Resampling.LANCZOS)

            # Maska zaokrąglenia narożników
            corner_mask = Image.new("L", (target_w, target_h), 0)
            draw = ImageDraw.Draw(corner_mask)
            draw.rounded_rectangle([(0, 0), (target_w, target_h)], radius=109, fill=255)

            screen_canvas = Image.new("RGBA", (target_w, target_h))
            screen_canvas.paste(resized_screen, (0, 0), corner_mask)
            # Nałożenie maski z oczkiem aparatu (punch hole)
            screen_canvas = Image.alpha_composite(screen_canvas, self.mask_img)

            # Składanie z obudową telefonu
            final_img = Image.new("RGBA", (1408, 2974), (0, 0, 0, 0))
            final_img.paste(self.back_img, (0, 0))
            final_img.paste(screen_canvas, (60, 61), screen_canvas)
            final_img.save(output_path, "PNG")
        else:
            # Elegancka ramka wektorowa w przypadku braku zasobów Android Studio
            sw, sh = screenshot.size
            border = 36
            corner = 60
            cw = sw + border * 2
            ch = sh + border * 2

            final_img = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
            draw = ImageDraw.Draw(final_img)
            draw.rounded_rectangle([(0, 0), (cw, ch)], radius=corner, fill=(24, 24, 27, 255), outline=(70, 70, 75, 255), width=3)

            screen_mask = Image.new("L", (sw, sh), 0)
            s_draw = ImageDraw.Draw(screen_mask)
            s_draw.rounded_rectangle([(0, 0), (sw, sh)], radius=corner - 8, fill=255)

            screen_canvas = Image.new("RGBA", (sw, sh))
            screen_canvas.paste(screenshot, (0, 0), screen_mask)
            final_img.paste(screen_canvas, (border, border), screen_canvas)

            # Punch hole aparatu
            cam_radius = 16
            draw.ellipse([(cw // 2 - cam_radius, border + 30 - cam_radius), (cw // 2 + cam_radius, border + 30 + cam_radius)], fill=(10, 10, 10, 255))
            final_img.save(output_path, "PNG")


class VoltUpScreenshotRunner:
    def __init__(self, avd_name: str | None = None, theme: str = "all", output_dir: str = "voltup_screens"):
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
        self.framer = Pixel9ProFramer()

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
        print_step("👆", "Konfiguracja paska nawigacji (poziomy pasek gestów bez przycisków)...")
        try:
            self.run_adb("shell", "cmd", "overlay", "disable", "com.android.internal.systemui.navbar.threebutton", check=False)
            self.run_adb("shell", "cmd", "overlay", "disable", "com.android.internal.systemui.navbar.twobutton", check=False)
            self.run_adb("shell", "cmd", "overlay", "enable", "com.android.internal.systemui.navbar.gestural", check=False)
            self.run_adb("shell", "settings", "put", "secure", "navigation_mode", "2", check=False)
            time.sleep(1)
            print_success("Włączono poziomy pasek gestów (Gesture Navigation Bar)!")
        except Exception as e:
            print_warn(f"Nie udało się wymusić nakładki gestów: {e}")

    def set_animations_enabled(self, enable: bool):
        """Wyłącza animacje przejścia okien w systemie, likwidując białą poświatę i rozmycia."""
        scale = "1.0" if enable else "0"
        try:
            self.run_adb("shell", "settings", "put", "global", "window_animation_scale", scale, check=False)
            self.run_adb("shell", "settings", "put", "global", "transition_animation_scale", scale, check=False)
            self.run_adb("shell", "settings", "put", "global", "animator_duration_scale", scale, check=False)
        except Exception:
            pass

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
        print_step("📦", "Weryfikacja instalacji aplikacji HealthUp...")
        installed = self.run_adb("shell", "pm", "list", "packages", self.package_name, check=False)
        if self.package_name in installed:
            print_success(f"Pakiet {self.package_name} jest już zainstalowany.")
            return

        print_info("Aplikacja nie jest zainstalowana. Poszukiwanie pliku APK...")
        candidate_apks = [
            self.root_dir / "androidApp" / "build" / "outputs" / "apk" / "debug" / "androidApp-debug.apk",
            self.root_dir / "HealthUp_Builds" / "HealthUp-Android-debug.apk",
            self.root_dir / "HealthUp_Builds" / "HealthUp-Android-release.apk",
            self.root_dir / "VoltUp_Builds" / "VoltUp-debug.apk",
            self.root_dir / "VoltUp_Builds" / "VoltUp-release-unsigned.apk",
        ]
        apk_to_install = None
        for apk in candidate_apks:
            if apk.is_file():
                apk_to_install = apk
                break

        if not apk_to_install:
            print_info("Brak pliku APK. Rozpoczynanie kompilacji debug APK...")
            gradlew = self.root_dir / ("gradlew.bat" if os.name == "nt" else "gradlew")
            self.run_cmd([str(gradlew), ":androidApp:assembleDebug"], check=True, capture_output=False)
            apk_to_install = candidate_apks[0]

        print_info(f"Instalowanie {apk_to_install.name} na emulatorze...")
        self.run_adb("install", "-r", "-d", str(apk_to_install))
        print_success("Aplikacja HealthUp pomyślnie zainstalowana!")

    def setup_demo_mode(self, enable: bool = True):
        """Włącza czysty pasek stanu (godzina 09:41, bateria 100%, pełny zasięg 5G/Wi-Fi)."""
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
        # Odczekaj po zamknięciu aplikacji, by system zwolnił bufor graficzny
        time.sleep(0.8)

        cmd = ["shell", "am", "start", "-n", f"{self.package_name}/{self.main_activity}"]
        if screen:
            cmd.extend(["--es", "SCREEN", screen])
        if theme_mode:
            cmd.extend(["--es", "THEME", theme_mode])

        self.run_adb(*cmd)

    def swipe(self, x1_ratio: float, y1_ratio: float, x2_ratio: float, y2_ratio: float, duration_ms: int = 500):
        x1 = int(self.screen_width * x1_ratio)
        y1 = int(self.screen_height * y1_ratio)
        x2 = int(self.screen_width * x2_ratio)
        y2 = int(self.screen_height * y2_ratio)
        self.run_adb("shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration_ms))

    def tap(self, x_ratio: float, y_ratio: float):
        x = int(self.screen_width * x_ratio)
        y = int(self.screen_height * y_ratio)
        self.run_adb("shell", "input", "tap", str(x), str(y))

    def take_raw_screenshot(self, target_path: Path):
        target_path.parent.mkdir(parents=True, exist_ok=True)
        with open(target_path, "wb") as f:
            cmd = [self.adb, "exec-out", "screencap", "-p"]
            subprocess.run(cmd, check=True, stdout=f)

    def capture_suite_for_theme(self, theme_mode: str, theme_folder_name: str):
        dir_bez_ramki = self.output_base / "bez_ramki" / theme_folder_name
        dir_z_ramka = self.output_base / "z_ramka" / theme_folder_name

        dir_bez_ramki.mkdir(parents=True, exist_ok=True)
        dir_z_ramka.mkdir(parents=True, exist_ok=True)

        theme_tag = "ciemny" if theme_mode == "DARK" else "jasny"
        print_step("📸", f"Rozpoczynanie sesji dla motywu: {theme_mode} ({theme_tag})")
        print_info(f"Katalog bez ramki: {dir_bez_ramki.relative_to(self.root_dir)}")
        print_info(f"Katalog z ramką:   {dir_z_ramka.relative_to(self.root_dir)}")

        # Każdy krok ma wydłużony czas oczekiwania na ustabilizowanie widoku
        # i dodatkową pauzę po wykonaniu zrzutu, by nie było żadnych poświat.
        screenshots = [
            # 1. Splashscreen
            {
                "base_name": f"01_Splashscreen_{theme_tag}",
                "label": "Splashscreen (Logo i animacja startowa)",
                "action": lambda: (self.launch_screen(None, theme_mode), time.sleep(0.8)),
            },
            # 2. Ekrany powitalne (Onboarding - Krok 1)
            {
                "base_name": f"02_Onboarding_Krok1_{theme_tag}",
                "label": "Ekrany powitalne (Onboarding - Krok 1/3)",
                "action": lambda: (self.launch_screen("ONBOARDING", theme_mode), time.sleep(2.5)),
            },
            # 3. Onboarding - Krok 2 (przewinięcie)
            {
                "base_name": f"03_Onboarding_Krok2_{theme_tag}",
                "label": "Ekrany powitalne (Onboarding - Krok 2/3)",
                "action": lambda: (self.swipe(0.85, 0.5, 0.15, 0.5, 450), time.sleep(1.5)),
            },
            # 4. Onboarding - Krok 3 (przewinięcie)
            {
                "base_name": f"04_Onboarding_Krok3_{theme_tag}",
                "label": "Ekrany powitalne (Onboarding - Krok 3/3)",
                "action": lambda: (self.swipe(0.85, 0.5, 0.15, 0.5, 450), time.sleep(1.5)),
            },
            # 5. Logowanie
            {
                "base_name": f"05_Logowanie_{theme_tag}",
                "label": "Ekran logowania",
                "action": lambda: (self.launch_screen("LOGIN", theme_mode), time.sleep(2.5)),
            },
            # 6. Rejestracja
            {
                "base_name": f"06_Rejestracja_{theme_tag}",
                "label": "Ekran rejestracji",
                "action": lambda: (self.launch_screen("REGISTER", theme_mode), time.sleep(2.5)),
            },
            # 7. Reset hasła
            {
                "base_name": f"07_Reset_Hasla_{theme_tag}",
                "label": "Ekran resetu hasła",
                "action": lambda: (self.launch_screen("FORGOT_PASSWORD", theme_mode), time.sleep(2.5)),
            },
            # 8. Pulpit główny (Dashboard)
            {
                "base_name": f"08_Pulpit_Glowny_{theme_tag}",
                "label": "Pulpit główny (Dashboard, bilans energii, watogodziny)",
                "action": lambda: (self.launch_screen("HOME", theme_mode), time.sleep(2.5)),
            },
            # 9. Pulpit - Ranking i Wyzwania tygodniowe (przewinięcie)
            {
                "base_name": f"09_Pulpit_Ranking_I_Wyzwania_{theme_tag}",
                "label": "Pulpit - Tabela rankingu miejskiego i wyzwania tygodniowe",
                "action": lambda: (self.swipe(0.5, 0.75, 0.5, 0.35, 550), time.sleep(1.5)),
            },
            # 10. Mapa stacji kinetycznych (dłuższe czekanie na kafle mapy)
            {
                "base_name": f"10_Mapa_Stacji_{theme_tag}",
                "label": "Mapa siłowni plenerowych i stacji ładowania w Łodzi",
                "action": lambda: (self.launch_screen("MAP", theme_mode), time.sleep(3.5)),
            },
            # 11. Nagrody - Katalog miejski
            {
                "base_name": f"11_Katalog_Nagrod_{theme_tag}",
                "label": "Katalog nagród (bilety MPK Łódź, Fala, EC1)",
                "action": lambda: (self.launch_screen("REWARDS", theme_mode), time.sleep(2.5)),
            },
            # 12. Nagrody - Moje Kody / Aktywne kupony
            {
                "base_name": f"12_Nagrody_Moje_Kody_Aktywne_{theme_tag}",
                "label": "Nagrody - Moje Kody i aktywne vouchery",
                "action": lambda: (self.tap(0.75, 0.16), time.sleep(1.5)),
            },
            # 13. Nagrody - Archiwum biletów i zużyte
            {
                "base_name": f"13_Nagrody_Archiwum_I_Uzyte_{theme_tag}",
                "label": "Nagrody - Archiwum zrealizowanych biletów",
                "action": lambda: (self.swipe(0.5, 0.75, 0.5, 0.35, 550), time.sleep(1.5)),
            },
            # 14. Profil użytkownika
            {
                "base_name": f"14_Profil_Uzytkownika_{theme_tag}",
                "label": "Profil użytkownika (poziom Miejski Odkrywca, bilans CO2, serie)",
                "action": lambda: (self.launch_screen("PROFILE", theme_mode), time.sleep(2.5)),
            },
            # 15. Historia treningów
            {
                "base_name": f"15_Historia_Treningow_{theme_tag}",
                "label": "Historia treningów (spalone kalorie, watogodziny, czas trwania)",
                "action": lambda: (self.launch_screen("HISTORY", theme_mode), time.sleep(2.5)),
            },
            # 16. Osiągnięcia i odznaki (Góra)
            {
                "base_name": f"16_Osiagniecia_I_Odznaki_{theme_tag}",
                "label": "Osiągnięcia i odznaki miejskie (poziomy odblokowania)",
                "action": lambda: (self.launch_screen("ACHIEVEMENTS", theme_mode), time.sleep(2.5)),
            },
            # 17. Osiągnięcia - Dalsze poziomy (przewinięcie)
            {
                "base_name": f"17_Osiagniecia_Dalsze_Odznaki_{theme_tag}",
                "label": "Osiągnięcia - Zaawansowane odznaki i postęp",
                "action": lambda: (self.swipe(0.5, 0.75, 0.5, 0.3, 550), time.sleep(1.5)),
            },
            # 18. Aktywny trening kinetyczny (Live Workout)
            {
                "base_name": f"18_Aktywny_Trening_Live_{theme_tag}",
                "label": "Aktywny trening kinetyczny (telemetria, waty, kalorie, prędkość)",
                "action": lambda: (self.launch_screen("WORKOUT", theme_mode), time.sleep(2.5)),
            },
            # 19. Skaner kodów QR na stacji (dłuższe czekanie na aparat)
            {
                "base_name": f"19_Skaner_QR_{theme_tag}",
                "label": "Skaner optyczny QR na stacji kinetycznej",
                "action": lambda: (self.launch_screen("QR", theme_mode), time.sleep(3.0)),
            },
            # 20. Przewodnik i FAQ
            {
                "base_name": f"20_Przewodnik_I_FAQ_{theme_tag}",
                "label": "Przewodnik, zasady generowania prądu i FAQ",
                "action": lambda: (self.launch_screen("HELP", theme_mode), time.sleep(2.5)),
            },
            # 21. Ustawienia aplikacji
            {
                "base_name": f"21_Ustawienia_Aplikacji_{theme_tag}",
                "label": "Ustawienia aplikacji (motyw, język, powiadomienia)",
                "action": lambda: (self.launch_screen("SETTINGS", theme_mode), time.sleep(2.5)),
            },
            # 22. Zarządzanie kontem
            {
                "base_name": f"22_Zarzadzanie_Kontem_{theme_tag}",
                "label": "Zarządzanie kontem i bezpieczeństwo",
                "action": lambda: (self.launch_screen("MANAGE_ACCOUNT", theme_mode), time.sleep(2.5)),
            },
        ]

        total = len(screenshots)
        for idx, item in enumerate(screenshots, start=1):
            raw_file = dir_bez_ramki / f"{item['base_name']}.png"
            framed_file = dir_z_ramka / f"{item['base_name']}_ramka.png"

            print(f"[{idx:02d}/{total:02d}] 📸 {item['label']}...")
            try:
                # 1. Wykonaj akcję i odczekaj na stabilny widok
                item["action"]()
                # 2. Zapisz surowy zrzut (bez ramki)
                self.take_raw_screenshot(raw_file)
                # 3. Wygeneruj wersję z oficjalną ramką Pixel 9 Pro
                self.framer.frame(raw_file, framed_file)
                print_success(f"Zapisano (bez ramki): {raw_file.relative_to(self.root_dir)}")
                print_success(f"Zapisano (z ramką):   {framed_file.relative_to(self.root_dir)}")

                # Dodatkowa pauza przed przejściem do następnego ekranu
                time.sleep(1.2)
            except Exception as e:
                print_error(f"Błąd podczas przechwytywania: {e}")

        print_step("✨", f"Zakończono pomyślnie generowanie {total} zrzutów dla motywu {theme_mode}!")

    def execute(self):
        try:
            self.ensure_emulator()
            self.enable_gesture_navigation()
            # Wyłączenie animacji systemowych - całkowity brak białej poświaty/rozmyć
            self.set_animations_enabled(False)
            self.update_screen_dimensions()
            self.ensure_app_installed()
            self.setup_demo_mode(True)

            if self.theme in ["ciemny", "dark", "all"]:
                self.capture_suite_for_theme("DARK", "ciemny")

            if self.theme in ["jasny", "light", "all"]:
                self.capture_suite_for_theme("LIGHT", "jasny")

            print_step("🎉", "Wszystkie zrzuty (bez ramki i z ramką) zostały wygenerowane!")
            print_info(f"Katalog główny: {self.output_base.resolve()}")
        finally:
            # Przywrócenie animacji i demo mode
            self.set_animations_enabled(True)
            self.setup_demo_mode(False)


def main():
    parser = argparse.ArgumentParser(description="Automatyczny generator zrzutów ekranu HealthUp na emulatorze Pixel 9 Pro")
    parser.add_argument("--avd", type=str, default="Pixel_9_Pro_API_36", help="Nazwa emulatora AVD (domyślnie: Pixel_9_Pro_API_36)")
    parser.add_argument("--theme", type=str, default="all", choices=["ciemny", "jasny", "all", "dark", "light"], help="Motyw graficzny (domyślnie: all - zarówno ciemny jak i jasny)")
    parser.add_argument("--output", type=str, default="voltup_screens", help="Katalog wyjściowy dla zrzutów ekranu (domyślnie: voltup_screens)")
    args = parser.parse_args()

    runner = VoltUpScreenshotRunner(avd_name=args.avd, theme=args.theme, output_dir=args.output)
    runner.execute()


if __name__ == "__main__":
    main()
