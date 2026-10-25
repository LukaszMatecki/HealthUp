@echo off
set "PYTHON_EXE=%LOCALAPPDATA%\Programs\Python\Python312\python.exe"
if exist "%PYTHON_EXE%" (
    "%PYTHON_EXE%" take_screenshots.py %*
) else (
    py -3.12 take_screenshots.py %*
)
