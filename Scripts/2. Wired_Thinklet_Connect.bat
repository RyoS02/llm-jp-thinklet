@echo off

start "scrcpy1" cmd /c scrcpy -s MP6MB25N6104874
timeout /t 3 /nobreak >nul
adb -s MP6MB25N6104874 shell am start -n com.example.videorecordapp/.MainActivity
timeout /t 2 /nobreak >nul

start "scrcpy2" cmd /c scrcpy -s MP6MB25N6103675
timeout /t 3 /nobreak >nul
adb -s MP6MB25N6103675 shell am start -n com.example.videorecordapp/.MainActivity
timeout /t 2 /nobreak >nul

adb -s MP6MB25N6104874 tcpip 5555
timeout /t 1 /nobreak >nul
adb connect 192.168.68.11
timeout /t 1 /nobreak >nul

adb -s MP6MB25N6103675 tcpip 5555
timeout /t 1 /nobreak >nul
adb connect 192.168.68.12
timeout /t 1 /nobreak >nul
