@echo off
set "PYTHON_EXE=%LOCALAPPDATA%\Programs\Python\Python312\python.exe"
if exist "%PYTHON_EXE%" (
    "%PYTHON_EXE%" record_walkthrough.py %*
) else (
    py -3.12 record_walkthrough.py %*
)
