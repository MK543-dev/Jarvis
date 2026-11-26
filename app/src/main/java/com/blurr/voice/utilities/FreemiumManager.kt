package com.blurr.voice.utilities

class FreemiumManager {
    companion object {
        const val DAILY_TASK_LIMIT = Int.MAX_VALUE
    }

    suspend fun getDeveloperMessage(): String = ""
    suspend fun isUserSubscribed(): Boolean = true
    suspend fun provisionUserIfNeeded() = Unit
    suspend fun getTasksRemaining(): Long = Long.MAX_VALUE
    suspend fun canPerformTask(): Boolean = true
    suspend fun decrementTaskCount() = Unit
}