package com.blurr.voice.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TaskHistoryItem(
    val task: String,
    val status: String,
    val startedAt: Date? = null,
    val completedAt: Date? = null,
    val success: Boolean? = null,
    val errorMessage: String? = null
) {
    fun getStatusEmoji(): String = if (success == true) "?" else if (status.lowercase() == "failed") "?" else "??"
    fun getFormattedStartTime(): String = startedAt?.let { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()).format(it) } ?: "Unknown"
    fun getFormattedCompletionTime(): String = completedAt?.let { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()).format(it) } ?: "Not completed"
}