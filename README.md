# thinklet-dialogue-collection-platform

# 概要
- Thinklet 2台とGoPro 2台を同期して収録するためのプログラム
  - PC側のプログラムからThinkletとGoProを制御し、録画の開始・停止を行う
  - ThinkletはAndroid端末であり、有線または無線で制御する
  - GoProはUSB接続によりPCと接続し、有線で制御する
- 本プログラムは「ExperimentControl」、「VideoRecordAPP」、「Scripts」の3つの部分から構成される
  - 「ExperimentControl」はPC側のプログラムであり、ThinkletとGoProを制御する. 特に録画の開始・停止、ThinkletとGoProのビデオをPCへ転送する機能を有する
  - 「VideoRecordAPP」はThinklet側のアプリであり、PC側からの指示を受けて録画の開始・停止を行う
  - 「Scripts」はThinkletの事前設定およびPC側プログラム「ExperimentControl」の実行を支援するスクリプト群である

## A. ExperimentControl
### 说明
このプログラムは、tkinterを使用したGUIを通じてThinkletおよびGoProを制御します. 以下の機能があります
- Start/Stopボタンを押すことで、ThinkletおよびGoProの録画開始・停止を制御
- メニュー機能
  - **Pull GoPro Files**：GoProの録画ファイルをPCへ転送
  - **Pull Thinklet Files**：Thinkletの録画ファイルをPCへ転送
  - **Clear Thinklet Files**：Thinklet内部ストレージの録画ファイルを削除

注意事項：
- Thinkletの録画ファイルは無線でもPCへの転送が可能ですが、転送に時間がかかるため、USB接続を推奨します
- GoProの録画ファイル削除については、GoPro本体の「ユーザー設定」→「リセット」→「SDカードをフォーマット」を実行してください. 

### Installation
Python 3.11が必要です. 

``` bash
cd ExperimentControl
python -m venv .venv/
. .venv/Scripts/activate
python.exe -m pip install --upgrade pip
pip install -r requirements.txt
```

### Configuration
- main.py 内の以下の変数を設定する必要がある

``` python
USE_GOPROS = [
    "C3461326516392",
    "C3461326512607" 
    ]

USE_THINKLETS_SERIES = [
    "MP6MB25N6104874",
    "MP6MB25N6103675"
]

USE_THINKLETS_IP = [
    "192.168.68.11", # Thinklet MP6MB25N6104874
    "192.168.68.12", # Thinklet MP6MB25N6103675
    ]

THINKLETS_SDMARK = {
    "192.168.68.11": "68AC-0CDA",
    "192.168.68.12": "30A7-0EBF",
    "MP6MB25N6104874": "68AC-0CDA",
    "MP6MB25N6103675": "30A7-0EBF",

    "192.168.68.13": "F75D-08D9",
    "192.168.68.14": "86FA-08D9",
}
```

- `USE_GOPROS`：使用するGoProのシリアル番号を指定します. シリアル番号は、GoPro本体の「ユーザー設定」→「バージョン情報」→「カメラ情報」→「シリアル番号」から確認できます. 
- `USE_THINKLETS_SERIES`：使用するThinkletのシリーズ番号を指定してください. ThinkletをUSB接続し、以下のコマンドで確認できます.
``` bash
adb devices
```
- `USE_THINKLETS_IP`：使用するThinkletのIPアドレスを指定してください. 以下のコマンドで確認できます.
``` bash
adb shell "ip addr show wlan0"
```
- `THINKLETS_SDMARK`：ThinkletのSDカード識別子（SDカードマーク）を指定します. 以下のコマンドで確認できます.「86FA-08D9」のような形式の識別子を、Thinkletのシリーズ番号およびIPアドレスと紐付けて設定してください. 
``` bash
adb shell "ls /storage"
```

### 実行
``` bash
python main.py
```

注意事項：
- scrcpyとadbがよく使うため，「ThinkletTools/scrcpy-win64-v3.2」を環境変数（グローバルパス）に設定しておくのは推薦です. 
- Thinkletの画面は，scrcpyを使用してPCに表示されます. 複数の設備を接続すれば，Thinkletの指定が必要です. 例:
   ``` bash
   scrcpy　ーs MP6MB25N6104874
   ```
- ThinkletのWiFiをIPアドレスを固定すれば，WiFiを接続するとき，Advanced optionsに IP settingsを「Static」に設定し、IPアドレスを指定してください. 
- GoProをUSB接続する際は、GoPro本体の画面に「USBで接続済み」と表示されていることを確認してください. 表示が出ない場合は、USBケーブルを一度抜き差ししてください. なお、接続ケーブルはType-C – Type-Cの使用を推奨します
- **GoProを使わない場合，USE_GOPROSに空のリスト `[]` を設定してください. **
- Scriptsフォルダ内の「3. Start_Record_Program.bat」はExperimentControl/main.pyを起動するスクリプトです. 
- 現在のmain.pyはデフォルトでThinkletを無線接続で起動する設定になっているため、PCとThinkletを事前に無線接続しておく必要があります. 
Thinkletを有線接続で起動する場合は、main.pyの40行目を以下のように変更してください. 
``` python
USE_THINKLETS = USE_THINKLETS_IP  # 無線接続（デフォルト）
↓
USE_THINKLETS = USE_THINKLETS_SERIES  # 有線接続
```
- Thinkletを無線で起動する場合は、以下のコマンドを実行してください. 事前にThinkletのIPアドレスを確認しておく必要があります（例：IPが192.168.68.12の場合）:
``` bash
adb tcpip 5555
adb connect 192.168.100.12
```

## B. VideoRecordAPP
### 说明
このアプリはThinklet上で動作し、PCからの指示に従って録画を開始・停止します. 以下の機能を備えています.
- 録画開始・停止の制御.
- 録画ファイルの保存先は、ThinkletのSDストレージ.
- UIは録画開始・停止の状態表示、Thinkletカメラのプレビュー表示、または録画開始・停止などの命令受信ログ表示.

### Thinkletの事前設定
- scrcpyを使用してThinkletの画面をPCに表示します.
   - ThinkletをUSB接続し、以下のコマンドを実行します.
   ``` bash
   scrcpy
   ```
- Developer Optionsを有効化します.
   - Thinkletの設定画面から「Settings」→「System」→ 「About phone」に，「Build number」を7回タップして開発者オプションを有効にします.
   - 次に「Settings」→「System」→「Developer options」に移動し、「USB debugging」を有効にします.

### アプリのビルドとThinkletへのインストール
- Android Studioをインストールして、VideoRecordAPPのプロジェクトを開きます. プロジェクトを開くと、自動的にビルドが始まるため、画面右下に表示される進捗バーが完了するまで待つ必要があります.
- GitHubにログインし、「Settings」→「Developer settings」→「Personal access tokens」→「Tokens (classic)」→「Generate new token (classic)」の順にクリックして、必要な権限（スコープ）を選択し，新しいアクセストークンを生成してください. 取得したアクセストークンを、プロジェクト内のlocal.propertiesファイルに以下のように追加します：
``` properties
TOKEN=<github token>
USERNAME=<github username>
```
- ビルドが完了したら、以下の手順でThinkletにアプリをインストールします.
  - ThinkletをUSB接続し、Android Studioの画面上のデバイスをThinkletを選択し，「Run app」ボタンをクリックします.
  - エラーが発生した場合は、生成したアクセストークンの権限が不足している可能性があります. その場合はトークンの権限（スコープ）を確認し、必要な権限を付与した上で再度トークンを生成してください.

### 権限の追加
- 初めてVideoRecordAPPを起動すると、カメラとマイクの権限が要求されます. 以下のコマンドを実行して権限を許可してください.
``` bash
adb shell pm grant com.example.videorecordapp android.permission.RECORD_AUDIO
adb shell pm grant com.example.videorecordapp android.permission.CAMERA
```
- Thinklet上でVideoRecordAPPのプレビュー画面が正常に表示されれば、権限が正しく許可されていることが確認できます.

## C. Scripts
### 说明
実験の自動化には、以下のScriptsを使用することを推奨します.
- **1. Wired_Thinklet_Connect - No Settings.bat**: ThinkletをUSB接続し、PCに画面を表示します.
- **2. Wired_Thinklet_Connect.bat**: ThinkletをUSB接続し、「VideoRecordAPP」を起動して無線接続設定を行います.
- **3. Wireless_Thinklet_Connect.bat**: Thinkletを無線接続し、PCに画面を常時表示します.
- **4. Start_Experiment_Control.bat**: ExperimentControlを起動し、ThinkletとGoProの録画開始・終了やデータ転送を制御します.
- **5. Shutdown_Thinklet-wired.bat**: Thinkletをシャットダウンします.

### 実行
- Scriptsでは、scrcpyとadbをグローバルパスから実行します. そのため、「ThinkletTools/scrcpy-win64-v3.2」のパスを事前に環境変数へ設定しておく必要があります.
- 実験開始前にGoProとThinkletをそれぞれUSB接続し、起動してください. GoProの画面に「USBで接続済み」と表示されていることを必ず確認してください.
- **各batファイル内のThinkletのシリーズ番号およびIPアドレスを正しく設定してください. （必須）**
以下の手順で順番に実行します（batファイルをDouble Clickして実行する）
- **1. Wired_Thinklet_Connect - No Settings.bat**
   - ThinkletをUSB接続し、PCに画面を常に表示します.
   - 注意：2台のThinkletが表示されない場合は、Thinkletが完全に起動するまで待ってから再度実行してください.
   - 二つのThinkletを両方表示されったら，Thinkletの起動が完了とした. 二つの画面を消してください.
- **2. Wired_Thinklet_Connect.bat**
   - 自動的に「VideoRecordAPP」を起動して無線設定をします.
   - 起動後、自動で終了します.
- **3. Wireless_Thinklet_Connect.bat**
   - Thinkletを無線接続し、PCに画面を常に表示します.
- **4. Start_Experiment_Control.bat**
   - ExperimentControlプログラムを起動します (ExperimentControl/main.pyのパスを正しいものに修正してください).
   - GUIが表示され、ThinkletとGoProの録画開始・停止を制御できます.
   - メニューの「Pull GoPro Files」や「Pull Thinklet Files」を使用して、録画ファイルをPCへ転送できます.
   - メニューの「Clear Thinklet Files」を使用して、Thinklet内部ストレージの録画ファイルを削除できます.
   - GoProの録画を開始しったら，本体は赤点滅している. Thinkletの録画を開始しったら、Thinkletの画面に「録画中」と表示される. 
   - cmd画面に制御情報が表示されます.GoProが制御できない場合は、USBケーブルを差し直してください.


実験終了
- **5. Shutdown_Thinklet-wired.bat**
   - Thinkletをシャットダウンします.

注意事項
- ThinkletのSDカードのフォーマット制限により、1回の録画での最大ファイルサイズは4GB（約35分）です.その以上録画してはデータが破損する可能性があります. 
- GoProのSDカードのフォーマット制限により、1回の録画での最大ファイルサイズは4GB（約30分）です. それ以上録画すると、新しいファイルが自動的に生成されます. 
  
### データの保存
- GoProの録画ファイルは、ExperimentControlの「Pull GoPro Files」でPCへ転送できます. 
- Thinkletの録画ファイルは、ExperimentControlの「Pull Thinklet Files」でPCへ転送できます. 
- 保存データの場所：
  1. **ExperimentControl/records/thinklet/**: Thinkletの録画ファイル
  2. **ExperimentControl/records/gopro/**: GoProの録画ファイル
  3. **ExperimentControl/records/record_info.json**: GoProとThinkletの録画ファイル、シリーズ番号やタイムスタンプの情報
   