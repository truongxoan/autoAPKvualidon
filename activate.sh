#!/bin/bash

echo "================================================================"
echo "          ANTIGRAVITY KEYMAPPER - KICH HOAT ADB DAEMON"
echo "================================================================"

adb get-state > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "[LOI] Khong tim thay thiet bi Android ket noi qua ADB!"
    exit 1
fi

echo "[1/2] Day tap tin len thiet bi..."
adb push app/build/outputs/apk/debug/app-debug.apk /data/local/tmp/antigravity.apk

echo "[2/2] Khoi chay Server Daemon..."
adb shell "nohup app_process -Djava.class.path=/data/local/tmp/antigravity.apk /data/local/tmp com.antigravity.keymapper.daemon.ServerDaemon > /data/local/tmp/keymapper.log 2>&1 &"

echo "================================================================"
echo "[THANH CONG] Daemon da khoi chay xong!"
echo "================================================================"
