@echo off

adb -s MP6MB25N6104874 shell reboot -p

timeout /t 2 >nul

adb -s MP6MB25N6103675 shell reboot -p
timeout /t 2 >nul
