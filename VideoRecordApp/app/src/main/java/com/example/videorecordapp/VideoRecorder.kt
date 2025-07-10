package com.example.videorecordapp

import ai.fd.thinklet.camerax.ThinkletMic
import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import android.os.Environment
import ai.fd.thinklet.camerax.mic.ThinkletMics
import ai.fd.thinklet.camerax.mic.multichannel.FiveCh
import ai.fd.thinklet.camerax.mic.multichannel.SixCh
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import ai.fd.thinklet.camerax.mic.xfe.Xfe
import android.media.AudioManager
import androidx.core.content.getSystemService


class VideoRecorder(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context
) {
    private val recorder = Recorder.Builder()
        .setExecutor(Executors.newSingleThreadExecutor())
        .setQualitySelector(QualitySelector.from(Quality.FHD))
//        .setThinkletMic(ThinkletMics.FiveCh)
        .setTargetVideoEncodingBitRate(12_000_000)
//        .setThinkletMic(ThinkletMics.Xfe(checkNotNull(context.getSystemService<AudioManager>())))
        .build()

    private val videoCapture = VideoCapture.withOutput(recorder)
    private var recording: Recording? = null

    val isRecording: Boolean
        get() = recording != null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        if (isRecording) return
        val sdCardDir = getExternalSdCardDir(context)

        val storageDir = sdCardDir ?: context.getExternalFilesDir(null)

        val videoFileName = "thinklet_${System.currentTimeMillis()}.mp4"
        val videoFile = File(storageDir, videoFileName)

        val fileOutputOptions = FileOutputOptions.Builder(videoFile).build()

        recording = videoCapture.output
            .prepareRecording(context, fileOutputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> VideoRecordLogger.addLog("録画開始")
                    is VideoRecordEvent.Finalize -> {
                        recording = null
                        VideoRecordLogger.addLog("録画終了")
                    }
                }
            }
    }

    private fun getExternalSdCardDir(context: Context): File? {
        val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
        return externalDirs.firstOrNull { dir ->
            dir != null && Environment.isExternalStorageRemovable(dir) &&
                    Environment.getExternalStorageState(dir) == Environment.MEDIA_MOUNTED
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        recording?.stop()
    }

    fun getVideoCapture(): VideoCapture<Recorder> = videoCapture
}