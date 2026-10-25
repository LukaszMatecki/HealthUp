# Skrypt pomocniczy do uruchomienia generatora zrzutów ekranu na Pixel 9 Pro
$ErrorActionPreference = "Stop"

$pythonExe = "C:\Users\Lukasz\AppData\Local\Programs\Python\Python312\python.exe"
if (Test-Path $pythonExe) {
    & $pythonExe take_screenshots.py @args
} else {
    py -3.12 take_screenshots.py @args
}
