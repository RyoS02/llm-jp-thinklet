Set WshShell = CreateObject("WScript.Shell")
WshShell.Run "cmd /k scrcpy -s 192.168.68.11 --max-size 1024", 0, False
