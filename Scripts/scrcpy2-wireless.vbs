Set WshShell = CreateObject("WScript.Shell")
WshShell.Run "cmd /k -s 192.168.68.12  --max-size 1024", 0, False
