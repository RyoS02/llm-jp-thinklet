@echo off

cd C:\Users\counseling\Documents\counseling_data_collection\ExperimentControl
call .venv\Scripts\python.exe main.py

timeout /t 2 >nul
