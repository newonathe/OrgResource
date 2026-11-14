@echo off
setlocal
cd /d "%~dp0"
echo OrgResource - Read-only checks for 5 GET endpoints
echo Keep Java application running in a SEPARATE terminal/window.
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\check-skeleton-demo.ps1"
echo.
pause
