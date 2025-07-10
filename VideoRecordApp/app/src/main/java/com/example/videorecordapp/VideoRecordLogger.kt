package com.example.videorecordapp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

object VideoRecordLogger {
    private val _logFlow = MutableStateFlow<List<String>>(emptyList())
    val logFlow: StateFlow<List<String>> = _logFlow

    fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        _logFlow.value = _logFlow.value + "$timestamp - $message"
        if (_logFlow.value.size > 100) {
            _logFlow.value = _logFlow.value.takeLast(100)
        }
    }
}