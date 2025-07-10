import subprocess
import tkinter as tk
from tkinter import ttk, messagebox
import asyncio
from open_gopro import WiredGoPro
from open_gopro.models import constants
from threading import Thread, Event
import time
import os
import json
from datetime import datetime
import shutil
import pygame

# ---------- Configuration (modify here each time) ------------

USE_GOPROS = [
    "C3461326516392", # GoPro3
    "C3461326512607", # GoPro5
    ]

USE_THINKLETS_SERIES = [
    "MP6MB25N6104874", # no.1
    "MP6MB25N6103675", # no.2
]

USE_THINKLETS_IP = [
    "192.168.68.11", # no.1
    "192.168.68.12", # no.2 
    ]

THINKLETS_SDMARK = {
    "192.168.68.11": "68AC-0CDA",
    "192.168.68.12": "30A7-0EBF",
    "MP6MB25N6104874": "68AC-0CDA",
    "MP6MB25N6103675": "30A7-0EBF",
}


USE_THINKLETS = USE_THINKLETS_IP

# -------------------------------------------------------------


class GoProRecorder(Thread):
    def __init__(self, serial, save_dir="records/gopro"):
        super().__init__()
        self.serial = serial
        self.event = Event()
        self.gopro = WiredGoPro(serial=serial)
        self.save_dir = save_dir
        self.start_timestamp = None
        self.saved_filename = None

    def run(self):
        asyncio.run(self.record())

    async def record(self):
        await self.gopro.open()
        await self.gopro.http_setting.video_resolution.set(constants.settings.VideoResolution.NUM_1080)
        await self.gopro.http_setting.frames_per_second.set(constants.settings.FramesPerSecond.NUM_30_0)
        files_before = set((await self.gopro.http_command.get_media_list()).data.files)

        await self.gopro.http_command.set_shutter(shutter=constants.Toggle.ENABLE)
        self.start_timestamp = int(time.time() * 1000)

        self.event.wait()

        await self.gopro.http_command.set_shutter(shutter=constants.Toggle.DISABLE)
        await asyncio.sleep(2)

        files_after = set((await self.gopro.http_command.get_media_list()).data.files)
        new_files = files_after - files_before

        if new_files:
            new_file = new_files.pop()
            self.saved_filename = new_file.filename.split("/")[1]

        await self.gopro.close()


class ThinkletRecorder:
    def __init__(self, ip):
        self.ip = ip

    def start_recording(self):
        subprocess.run(
            f"adb -s {self.ip} shell am startservice -a com.example.videorecordapp.START_RECORDING "
            "-n com.example.videorecordapp/.VideoRecordService",
            shell=True
        )

    def stop_recording(self):
        subprocess.run(
            f"adb -s {self.ip} shell am startservice -a com.example.videorecordapp.STOP_RECORDING "
            "-n com.example.videorecordapp/.VideoRecordService",
            shell=True
        )
    
    def play_beep(self):
         subprocess.run(
            f"adb -s {self.ip} shell am startservice -a com.example.videorecordapp.PLAY_BEEP -n com.example.videorecordapp/.VideoRecordService",
            shell=True
        )


class RecorderController:
    def __init__(self):
        self.gopro_threads = []
        self.thinklets = [ThinkletRecorder(ip) for ip in USE_THINKLETS]
        self.thinklet_files_before = {}
        record_file = "records/record_info.json"
        if os.path.exists(record_file):
            with open(record_file, "r") as f:
                self.records_info = json.load(f)
        else:
            self.records_info = []

    def list_thinklet_files(self, ip):
        remote_path = f"/storage/{THINKLETS_SDMARK[ip]}/Android/data/com.example.videorecordapp/files"
        result = subprocess.run(
            f"adb -s {ip} shell ls {remote_path}",
            shell=True,
            capture_output=True,
            text=True
        )
        return set(
            file.strip() for file in result.stdout.strip().split('\n')
            if file.strip() and file.strip() != 'files'
        )

    def start_all(self):
        t_start = time.time()
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        os.makedirs("records", exist_ok=True)

        self.thinklet_files_before = {
            ip: self.list_thinklet_files(ip) for ip in USE_THINKLETS
        }

        for thinklet in self.thinklets:
            thinklet.start_recording()

        self.gopro_threads = [GoProRecorder(serial) for serial in USE_GOPROS]
        for gopro_thread in self.gopro_threads:
            gopro_thread.start()

        self.records_info.append({"timestamp": timestamp, "gopros": [], "thinklets": []})

        elapsed = time.time() - t_start
        remaining = 10 - elapsed
        if remaining > 0:
            time.sleep(remaining)

        pygame.mixer.init()
        pygame.mixer.music.load("Ding.mp3")
        pygame.mixer.music.play()

        # for thinklet in self.thinklets:
        #     thinklet.play_beep()

    def stop_all(self):
        
        pygame.mixer.init()
        pygame.mixer.music.load("Ding.mp3")
        pygame.mixer.music.play()
    
        for thinklet in self.thinklets:
            thinklet.stop_recording()

        for gopro_thread in self.gopro_threads:
            gopro_thread.event.set()
            gopro_thread.join()
            self.records_info[-1]["gopros"].append({
                "serial": gopro_thread.serial,
                "filename": gopro_thread.saved_filename,
                "start_timestamp": gopro_thread.start_timestamp
            })

        for ip in USE_THINKLETS:
            files_after = self.list_thinklet_files(ip)
            files_before = self.thinklet_files_before.get(ip, set())
            new_files = list(files_after - files_before)
            self.records_info[-1]["thinklets"].append({
                "ip": ip,
                "new_files": new_files
            })

        with open("records/record_info.json", "w") as f:
            json.dump(self.records_info, f, indent=4)

    def pull_gopro_files(self):

        for serial in USE_GOPROS:
            local_path = os.path.join("records", "gopro", serial)
            os.makedirs(local_path, exist_ok=True)

            gopro = WiredGoPro(serial=serial)
            asyncio.run(self._pull_gopro(gopro, local_path))


    async def _pull_gopro(self, gopro, local_path):
        await gopro.open()
        media_list = (await gopro.http_command.get_media_list()).data.files
        remote_files = set([file.filename.split("/")[1] for file in media_list])
        local_files = set(os.listdir(local_path))

        new_files = remote_files - local_files
        for file in media_list:
            short_name = file.filename.split("/")[1]
            if short_name in new_files:
                save_path = os.path.join(local_path, short_name)
                await gopro.http_command.download_file(
                    camera_file=file.filename,
                    local_file=save_path
                )

        await gopro.close()



    def pull_task(self, ip):
        remote_path = f"/storage/{THINKLETS_SDMARK[ip]}/Android/data/com.example.videorecordapp/files"
        local_path = f"records/thinklet/{ip}"
        os.makedirs(local_path, exist_ok=True)
        result = subprocess.run(f"adb -s {ip} shell ls {remote_path}", shell=True, capture_output=True, text=True)

        remote_files = set(
            file.strip() for file in result.stdout.strip().split('\n')
            if file.strip() and file.strip() != 'files'
        )

        local_files = set(os.listdir(local_path))

        files_to_pull = remote_files - local_files

        for file in files_to_pull:
            subprocess.run(f"adb -s {ip} pull {remote_path}/{file} {local_path}", shell=True)

    def pull_files(self):
        for series in USE_THINKLETS_SERIES:
            self.pull_task(series)

    def clear_files(self):
        if messagebox.askyesno("確認", "全ファイルを削除しますか？"):
            for ip in USE_THINKLETS:
                subprocess.run(f"adb -s {ip} shell \"rm -rf /storage/{THINKLETS_SDMARK[ip]}/Android/data/com.example.videorecordapp/files/*\"", shell=True)


# ------------------ GUI Setup ---------------------# 
controller = RecorderController()

class ModernRecorderApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Modern Recorder")
        self.root.geometry("350x200")
        self.root.resizable(False, False)

        self.style = ttk.Style()
        self.style.configure('Start.TButton', font=('Segoe UI', 20), background='#006400', foreground='black')
        self.style.configure('Stop.TButton', font=('Segoe UI', 20), background='#8B0000', foreground='black')
        self.style.configure('Disabled.TButton', font=('Segoe UI', 20), background='gray', foreground='black')

        self.recording = False
        self.blinking = False

        self.main_button = ttk.Button(
            self.root, text="Start", style='Start.TButton', command=self.toggle_recording
        )
        self.main_button.pack(expand=True, pady=20)

        menu_bar = tk.Menu(self.root)
        self.root.config(menu=menu_bar)

        action_menu = tk.Menu(menu_bar, tearoff=0)
        action_menu.add_command(label="Pull GoPro Files", command=self.pull_gopro_files)
        action_menu.add_command(label="Pull Thinklet Files", command=self.pull_thinklet_files)
        action_menu.add_separator()
        action_menu.add_command(label="Clear Thinklet Files", command=self.clear_files)

        menu_bar.add_cascade(label="Actions", menu=action_menu)

    def toggle_recording(self):
        self.main_button.config(state='disabled', style='Disabled.TButton')
        if not self.recording:
            Thread(target=self.start_recording).start()
        else:
            Thread(target=self.stop_recording).start()

    def start_recording(self):
        self.recording = True
        controller.start_all()
        self.main_button.config(text="Stop", style='Stop.TButton', state='normal')
        self.blinking = True
        self.blink_button()

    def stop_recording(self):
        self.recording = False
        controller.stop_all()
        self.blinking = False
        self.main_button.config(text="Start", style='Start.TButton', state='normal')

    def blink_button(self):
        if self.blinking:
            current_bg = self.main_button.cget("style")
            new_style = 'Disabled.TButton' if current_bg == 'Stop.TButton' else 'Stop.TButton'
            self.main_button.config(style=new_style)
            self.root.after(500, self.blink_button)
        else:
            self.main_button.config(style='Start.TButton')

    def pull_gopro_files(self):
        Thread(target=self._pull_gopro_files_task).start()

    def _pull_gopro_files_task(self):
        controller.pull_gopro_files()
        messagebox.showinfo("Info", "GoPro files pulled successfully.")

    def pull_thinklet_files(self):
        Thread(target=self._pull_thinklet_files_task).start()

    def _pull_thinklet_files_task(self):
        controller.pull_files()
        messagebox.showinfo("Info", "Thinklet files pulled successfully.")

    def clear_files(self):
        controller.clear_files()
        messagebox.showinfo("Info", "Thinklet files cleared.")

if __name__ == "__main__":
    root = tk.Tk()
    app = ModernRecorderApp(root)
    root.mainloop()
