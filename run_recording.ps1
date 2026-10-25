# Skrypt pomocniczy do uruchomienia automatycznego nagrania wideo na Pixel 9 Pro
$ErrorActionPreference = "Stop"

$pythonExe = "C:\Users\Lukasz\AppData\Local\Programs\Python\Python312\python.exe"
if (Test-Path $pythonExe) {
    & $pythonExe record_walkthrough.py @args
} else {
    py -3.12 record_walkthrough.py @args
}
