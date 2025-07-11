@echo off

adb -s MP6MB25N6102698 shell reboot -p

timeout /t 2 >nul

adb -s MP6MB25N6104820 shell reboot -p
timeout /t 2 >nul
