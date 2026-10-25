#!python3.12
"""
VoltUp - Zautomatyzowany rejestrator wideo (Pixel 9 Pro / Video Walkthrough)
=============================================================================
Ten skrypt automatycznie:
 1. Wykrywa lub uruchamia emulator telefonu Pixel 9 Pro (AVD: Pixel_9_Pro_API_36 / Pixel_9_Pro_XL).
 2. Włącza nowoczesną nawigację gestami (poziomy pasek gestów).
 3. Włącza wskaźniki dotknięć (show_touches), aby w nagraniu było widać eleganckie kółka dotyku.
 4. Aktywuje płynne animacje systemowe (1.0x) oraz Android Demo Mode (czysty pasek stanu 09:41).
 5. Uruchamia natywne nagrywanie wideo w wysokiej rozdzielczości (1080p, wysoki bitrate 16-20 Mbps).
 6. Spokojnie i płynnie przechodzi przez wszystkie aktywności, symulując naturalne korzystanie z aplikacji:
      - Animowany start i Splashscreen
      - Ekrany powitalne (Onboarding) z płynnym przesuwaniem slajdów
      - Logowanie, rejestracja, reset hasła
      - Pulpit (Dashboard) ze statystykami energii, watogodzinami, przewijaniem do rankingu i wyzwań
      - Interaktywna mapa stacji kinetycznych w Łodzi
      - Katalog nagród i vouchery w "Moje Kody" (wraz z archiwum biletów)
      - Profil użytkownika, ranga "Miejski Odkrywca" i bilans CO2
      - Historia treningów
      - Osiągnięcia i odznaki (scroll po 30 odznakach)
      - Aktywny trening z dynamicznym prędkościomierzem i mocą w watach
      - Skaner optyczny QR
      - Centrum pomocy i ustawienia
 7. Zapisuje gotowe nagranie MP4 w folderze 'voltup_recordings/'.

Użycie:
    py record_walkthrough.py
    py record_walkthrough.py --theme ciemny
    py record_walkthrough.py --theme jasny
    py record_walkthrough.py --theme all
    .\run_recording.ps1
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


class VoltUpVideoRecorder:
    def __init__(self, avd_name: str | None = None, theme: str = "ciemny", output_dir: str = "voltup_recordings"):
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
        self.record_proc = None

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
            raise RuntimeError("Nie znaleziono żadnego emulatora AVD. Utwórz Pixel 9 Pro w Android Studio.")

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
        """Włącza poziomy pasek gestów u dołu ekranu (ukrywa 3 przyciski funkcyjne)."""
        print_step("👆", "Konfiguracja paska nawigacji gestami...")
        try:
            self.run_adb("shell", "cmd", "overlay", "disable", "com.android.internal.systemui.navbar.threebutton", check=False)
            self.run_adb("shell", "cmd", "overlay", "disable", "com.android.internal.systemui.navbar.twobutton", check=False)
            self.run_adb("shell", "cmd", "overlay", "enable", "com.android.internal.systemui.navbar.gestural", check=False)
            self.run_adb("shell", "settings", "put", "secure", "navigation_mode", "2", check=False)
            time.sleep(1)
            print_success("Włączono poziomy pasek gestów (Gesture Navigation Bar)!")
        except Exception as e:
            print_warn(f"Nie udało się wymusić nakładki gestów: {e}")

    def setup_recording_environment(self, enable: bool = True):
        """Konfiguruje wskaźniki dotknięć, animacje oraz czysty pasek stanu."""
        try:
            if enable:
                # 1. Pokaż eleganckie punkty dotknięć palca na nagraniu
                self.run_adb("shell", "settings", "put", "system", "show_touches", "1", check=False)

                # 2. Płynne animacje systemowe (1.0x) - aby nagranie miało naturalną dynamikę
                self.run_adb("shell", "settings", "put", "global", "window_animation_scale", "1.0", check=False)
                self.run_adb("shell", "settings", "put", "global", "transition_animation_scale", "1.0", check=False)
                self.run_adb("shell", "settings", "put", "global", "animator_duration_scale", "1.0", check=False)

                # 3. Android Demo Mode (czysty pasek stanu: 09:41, bateria 100%, 5G)
                self.run_adb("shell", "settings", "put", "global", "sysui_demo_allowed", "1", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "enter", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "clock", "-e", "hhmm", "0941", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "battery", "-e", "level", "100", "-e", "plugged", "false", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "network", "-e", "wifi", "show", "-e", "level", "4", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "network", "-e", "mobile", "show", "-e", "datatype", "5g", "-e", "level", "4", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "notifications", "-e", "visible", "false", check=False)
            else:
                self.run_adb("shell", "settings", "put", "system", "show_touches", "0", check=False)
                self.run_adb("shell", "am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", "exit", check=False)
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
        print_step("📦", "Weryfikacja instalacji aplikacji VoltUp...")
        installed = self.run_adb("shell", "pm", "list", "packages", self.package_name, check=False)
        if self.package_name in installed:
            print_success(f"Pakiet {self.package_name} jest już zainstalowany.")
            return

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
            print_info("Kompilacja debug APK...")
            gradlew = self.root_dir / ("gradlew.bat" if os.name == "nt" else "gradlew")
            self.run_cmd([str(gradlew), ":androidApp:assembleDebug"], check=True, capture_output=False)
            apk_to_install = candidate_apks[0]

        print_info(f"Instalowanie {apk_to_install.name}...")
        self.run_adb("install", "-r", "-d", str(apk_to_install))
        print_success("Aplikacja VoltUp zainstalowana!")

    def launch_screen(self, screen: str | None = None, theme_mode: str = "DARK", smooth_pause: float = 2.0):
        """Uruchamia zadany ekran i motyw z płynną pauzą."""
        cmd = ["shell", "am", "start", "-n", f"{self.package_name}/{self.main_activity}"]
        if screen:
            cmd.extend(["--es", "SCREEN", screen])
        if theme_mode:
            cmd.extend(["--es", "THEME", theme_mode])

        self.run_adb(*cmd)
        time.sleep(smooth_pause)

    def swipe(self, x1_ratio: float, y1_ratio: float, x2_ratio: float, y2_ratio: float, duration_ms: int = 500, pause_after: float = 1.5):
        """Wykonuje naturalne przeciągnięcie palcem po ekranie z pauzą."""
        x1 = int(self.screen_width * x1_ratio)
        y1 = int(self.screen_height * y1_ratio)
        x2 = int(self.screen_width * x2_ratio)
        y2 = int(self.screen_height * y2_ratio)
        self.run_adb("shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration_ms))
        time.sleep(pause_after)

    def tap(self, x_ratio: float, y_ratio: float, pause_after: float = 1.5):
        """Dotyka wskazanego punktu na ekranie z pauzą."""
        x = int(self.screen_width * x_ratio)
        y = int(self.screen_height * y_ratio)
        self.run_adb("shell", "input", "tap", str(x), str(y))
        time.sleep(pause_after)

    def start_device_recording(self, remote_video_path: str):
        """Uruchamia w tle proces nagrywania 'screenrecord' na telefonie."""
        # Usuń ewentualny stary plik z /sdcard/
        self.run_adb("shell", "rm", "-f", remote_video_path, check=False)
        time.sleep(0.5)

        record_cmd = [
            self.adb, "shell", "screenrecord",
            "--bit-rate", "16000000",  # 16 Mbps wysoka jakość
            "--time-limit", "180",     # do 3 minut nagrania
            remote_video_path
        ]
        self.record_proc = subprocess.Popen(record_cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        time.sleep(1.0)  # Czas na rozruch kodeka wideo na urządzeniu

    def stop_device_recording(self, remote_video_path: str, local_destination: Path):
        """Zatrzymuje nagrywanie sygnałem SIGINT (zapisanie kontenera MP4) i pobiera plik na dysk."""
        print_info("Zatrzymywanie nagrywania i finalizowanie pliku MP4...")
        # Wysłanie SIGINT (sygnał 2) do procesu screenrecord na telefonie
        self.run_adb("shell", "pkill", "-2", "screenrecord", check=False)
        time.sleep(2.5)  # Kluczowe: czas na zapisanie tablicy klatek (moov atom) w pliku MP4

        if self.record_proc:
            try:
                self.record_proc.terminate()
            except Exception:
                pass

        local_destination.parent.mkdir(parents=True, exist_ok=True)
        print_info(f"Pobieranie wideo z telefonu: {local_destination.name}...")
        self.run_adb("pull", remote_video_path, str(local_destination))
        self.run_adb("shell", "rm", "-f", remote_video_path, check=False)

        if local_destination.is_file():
            size_mb = local_destination.stat().st_size / (1024 * 1024)
            print_success(f"Wideo pomyślnie zapisane -> {local_destination.relative_to(self.root_dir)} ({size_mb:.2f} MB)")
        else:
            print_error(f"Nie udało się pobrać nagrania do: {local_destination}")

    def record_walkthrough_for_theme(self, theme_mode: str):
        theme_tag = "ciemny" if theme_mode == "DARK" else "jasny"
        remote_path = f"/sdcard/voltup_walkthrough_{theme_tag}.mp4"
        local_file = self.output_base / f"VoltUp_Walkthrough_Pixel9Pro_{theme_tag}.mp4"

        print_step("🎬", f"Rozpoczynanie nagrania wideo w motywie: {theme_mode} ({theme_tag})")
        print_info("Scenariusz prezentacji: pełny spacer po aplikacji w naturalnym, spokojnym tempie.")

        # Rozpoczęcie nagrywania
        self.start_device_recording(remote_path)

        try:
            # 1. Start i Splashscreen
            print_info("[1/13] Ekran startowy (Splashscreen & Logo)...")
            self.run_adb("shell", "am", "force-stop", self.package_name, check=False)
            time.sleep(0.8)
            self.run_adb("shell", "am", "start", "-n", f"{self.package_name}/{self.main_activity}", "--es", "THEME", theme_mode)
            time.sleep(3.0)

            # 2. Ekrany powitalne (Onboarding) - płynne przewijanie slajdów
            print_info("[2/13] Prezentacja powitalna (Onboarding 1, 2, 3)...")
            self.launch_screen("ONBOARDING", theme_mode, smooth_pause=2.5)
            self.swipe(0.85, 0.5, 0.15, 0.5, duration_ms=500, pause_after=2.5)  # Slajd 2
            self.swipe(0.85, 0.5, 0.15, 0.5, duration_ms=500, pause_after=2.5)  # Slajd 3

            # 3. Logowanie, rejestracja i reset hasła
            print_info("[3/13] Uwierzytelnianie (Logowanie, Rejestracja, Reset hasła)...")
            self.launch_screen("LOGIN", theme_mode, smooth_pause=3.0)
            self.launch_screen("REGISTER", theme_mode, smooth_pause=3.0)
            self.launch_screen("FORGOT_PASSWORD", theme_mode, smooth_pause=2.5)

            # 4. Pulpit główny (Dashboard) - bilans energii, watogodziny, ranking
            print_info("[4/13] Pulpit główny (Dashboard, statystyki energii i redukcja CO2)...")
            self.launch_screen("HOME", theme_mode, smooth_pause=3.5)
            # Płynne przewinięcie w dół do rankingu i wyzwań tygodniowych
            print_info("       -> Przewijanie do tabeli liderów i wyzwań miejskich...")
            self.swipe(0.5, 0.75, 0.5, 0.35, duration_ms=650, pause_after=3.5)
            # Powrót na górę pulpitu
            self.swipe(0.5, 0.35, 0.5, 0.75, duration_ms=500, pause_after=2.0)

            # 5. Mapa stacji kinetycznych w Łodzi
            print_info("[5/13] Interaktywna mapa łódzkich siłowni plenerowych...")
            self.launch_screen("MAP", theme_mode, smooth_pause=4.5)
            # Delikatne przesunięcie mapy
            self.swipe(0.65, 0.55, 0.35, 0.50, duration_ms=800, pause_after=3.0)

            # 6. Nagrody - Katalog miejski
            print_info("[6/13] Katalog mikronagród (MPK Łódź, Fala, EC1)...")
            self.launch_screen("REWARDS", theme_mode, smooth_pause=3.5)

            # 7. Nagrody - Moje Kody i vouchery
            print_info("[7/13] Zakładka 'Moje Kody' (Aktywne vouchery i kody kreskowe)...")
            self.tap(0.75, 0.16, pause_after=3.0)
            # Przewinięcie do archiwum zużytych biletów
            self.swipe(0.5, 0.75, 0.5, 0.35, duration_ms=600, pause_after=3.0)

            # 8. Profil użytkownika i ranga
            print_info("[8/13] Profil użytkownika (Jan Kowalski, ranga 'Miejski Odkrywca')...")
            self.launch_screen("PROFILE", theme_mode, smooth_pause=3.5)

            # 9. Historia treningów kinetycznych
            print_info("[9/13] Historia zakończonych treningów kardio...")
            self.launch_screen("HISTORY", theme_mode, smooth_pause=3.0)
            self.swipe(0.5, 0.75, 0.5, 0.35, duration_ms=600, pause_after=2.5)

            # 10. Osiągnięcia i odznaki miejskie
            print_info("[10/13] System 30 osiągnięć i poziomów odznak...")
            self.launch_screen("ACHIEVEMENTS", theme_mode, smooth_pause=3.0)
            self.swipe(0.5, 0.75, 0.5, 0.30, duration_ms=700, pause_after=3.0)

            # 11. Aktywny trening kinetyczny (Live Workout Cockpit)
            print_info("[11/13] Aktywny trening kinetyczny (Live telemetria, waty, kadencja)...")
            self.launch_screen("WORKOUT", theme_mode, smooth_pause=4.5)

            # 12. Skaner kodów QR na stacji kinetycznej
            print_info("[12/13] Optyczny skaner QR kodu stacji...")
            self.launch_screen("QR", theme_mode, smooth_pause=3.5)

            # 13. Centrum pomocy & powrót na pulpit
            print_info("[13/13] Przewodnik, FAQ oraz powrót na Pulpit Główny...")
            self.launch_screen("HELP", theme_mode, smooth_pause=3.0)
            self.launch_screen("SETTINGS", theme_mode, smooth_pause=2.5)
            self.launch_screen("HOME", theme_mode, smooth_pause=3.5)

        finally:
            self.stop_device_recording(remote_path, local_file)

    def execute(self):
        try:
            self.ensure_emulator()
            self.enable_gesture_navigation()
            self.update_screen_dimensions()
            self.ensure_app_installed()
            self.setup_recording_environment(True)

            if self.theme in ["ciemny", "dark", "all"]:
                self.record_walkthrough_for_theme("DARK")

            if self.theme in ["jasny", "light", "all"]:
                self.record_walkthrough_for_theme("LIGHT")

            print_step("🎉", "Nagranie wideo zostało pomyślnie zrealizowane!")
            print_info(f"Katalog z nagraniami: {self.output_base.resolve()}")
        finally:
            self.setup_recording_environment(False)


def main():
    parser = argparse.ArgumentParser(description="Automatyczny rejestrator wideo aplikacji VoltUp na Pixel 9 Pro")
    parser.add_argument("--avd", type=str, default="Pixel_9_Pro_API_36", help="Nazwa emulatora AVD (domyślnie: Pixel_9_Pro_API_36)")
    parser.add_argument("--theme", type=str, default="ciemny", choices=["ciemny", "jasny", "all", "dark", "light"], help="Motyw graficzny (ciemny / jasny / all)")
    parser.add_argument("--output", type=str, default="voltup_recordings", help="Katalog wyjściowy dla plików wideo MP4")
    args = parser.parse_args()

    recorder = VoltUpVideoRecorder(avd_name=args.avd, theme=args.theme, output_dir=args.output)
    recorder.execute()


if __name__ == "__main__":
    main()
