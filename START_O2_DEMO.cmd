@echo off
setlocal
cd /d "%~dp0"
echo OrgResource O1 - Real Java skeleton startup
echo This does NOT reset MySQL or modify source code.
echo Use the original Windows computer and locally configured MySQL 3308.
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start-skeleton-demo.ps1"
set "status=%ERRORLEVEL%"
echo.
if not "%status%"=="0" echo Startup or Java execution ended with code %status%.
echo This window will close after you press a key.
pause >nul
exit /b %status%
