package com.example.videorecordapp

import android.content.Intent
import android.media.MediaPlayer
import androidx.lifecycle.LifecycleService

class VideoRecordService : LifecycleService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_STOP -> {
                sendBroadcast(Intent(intent.action))
            }
            ACTION_PLAY_BEEP -> {
//                VideoRecordLogger.addLog("ビープ音を再生します")
                MediaPlayer.create(this, R.raw.start_record)?.apply {
                    setOnCompletionListener { release() }
                    start()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        const val ACTION_START = "com.example.videorecordapp.START_RECORDING"
        const val ACTION_STOP = "com.example.videorecordapp.STOP_RECORDING"
        const val ACTION_PLAY_BEEP = "com.example.videorecordapp.PLAY_BEEP"
    }
}