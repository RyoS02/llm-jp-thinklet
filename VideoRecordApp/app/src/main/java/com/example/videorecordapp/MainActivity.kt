package com.example.videorecordapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.videorecordapp.ui.theme.CameraRecorderTheme

private const val TAG = "VideoRecorder"

class MainActivity : ComponentActivity() {

    private lateinit var videoRecorder: VideoRecorder
    private lateinit var previewView: PreviewView

    private val recordReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.RECORD_AUDIO)
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                VideoRecordService.ACTION_START -> {
                    videoRecorder.startRecording()
                }
                VideoRecordService.ACTION_STOP -> {
                    videoRecorder.stopRecording()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CameraXPatch.apply()
        previewView = PreviewView(this)
        videoRecorder = VideoRecorder(this, this)

        val filter = IntentFilter().apply {
            addAction(VideoRecordService.ACTION_START)
            addAction(VideoRecordService.ACTION_STOP)
        }
        registerReceiver(recordReceiver, filter, RECEIVER_NOT_EXPORTED)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            bindCamera(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            CameraRecorderTheme {
                ExperimentScreen(previewView = previewView)
            }
        }
    }

    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val videoCapture = videoRecorder.getVideoCapture()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            videoCapture
        )

        VideoRecordLogger.addLog("カメラ初期化完了")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(recordReceiver)
    }
}