@echo off
title AntiGravity Keymapper - ADB Activator (Zero Detection)
color 0b

echo ================================================================
echo           ANTIGRAVITY KEYMAPPER - KICH HOAT ADB DAEMON
echo ================================================================
echo.

echo [1/3] Kiem tra ket noi thiet bi qua ADB...
adb get-state >nul 2>&1
if %errorlevel% neq 0 (
    echo [LOI] Khong tim thay thiet bi Android!
    echo Vui long:
    echo   1. Bat 'Tuy chon nha phat trien' tren dien thoai.
    echo   2. Bat 'Go loi USB' (USB Debugging).
    echo   3. Cam cap vao PC va bam 'Cho phep' tren dien thoai.
    echo.
    pause
    exit /b 1
)

echo [2/3] Day tep kich hoat len thiet bi...
adb push app/build/outputs/apk/debug/app-debug.apk /data/local/tmp/antigravity.apk >nul 2>&1

echo [3/3] Khoi chay Server Daemon...
adb shell "nohup app_process -Djava.class.path=/data/local/tmp/antigravity.apk /data/local/tmp com.antigravity.keymapper.daemon.ServerDaemon > /data/local/tmp/keymapper.log 2>&1 &"

echo.
echo ================================================================
echo [THANH CONG] Daemon da duoc kich hoat thanh cong tren dien thoai!
echo Ban co the rut cap hoac de nguyen va mo ung dung de choi game.
echo ================================================================
pause
