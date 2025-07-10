package com.example.videorecordapp

import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ExperimentScreen(previewView: PreviewView) {
    val logs by VideoRecordLogger.logFlow.collectAsState()
    val scrollState = rememberScrollState()
    var isExperimentRunning by remember { mutableStateOf(false) }

    LaunchedEffect(logs) {
        val latestLog = logs.lastOrNull().orEmpty()
        isExperimentRunning = latestLog.contains("録画開始") && !latestLog.contains("録画終了")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ExperimentStatusLabel(isRunning = isExperimentRunning)
        }

        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { previewView.apply { scaleType = PreviewView.ScaleType.FIT_CENTER } },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = false)
            )
        }

        LaunchedEffect(logs.size) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Bottom
        ) {
            logs.takeLast(100).forEach { log ->
                Text(
                    text = log,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 24.sp)
                )
            }
        }
    }
}

@Composable
fun ExperimentStatusLabel(isRunning: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = if (isRunning) Color.Red else Color.Gray,
        animationSpec = if (isRunning)
            infiniteRepeatable(tween(500), RepeatMode.Reverse)
        else
            tween(500)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        color = bgColor,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (isRunning) "録画中" else "録画終了",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}