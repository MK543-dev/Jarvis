package com.blurr.voice.utilities

import android.content.Context
import com.blurr.voice.BuildConfig
import com.blurr.voice.MyApplication
import java.util.concurrent.atomic.AtomicInteger

object ApiKeyManager {
    private const val PREFS_NAME = "JarvisSettings"
    private const val GEMINI_KEYS_KEY = "gemini_api_keys"
    private val currentIndex = AtomicInteger(0)

    fun saveKeys(context: Context, keys: String) {
        val trimmed = keys.trim()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(GEMINI_KEYS_KEY, trimmed)
            .apply()
        // Preserve compatibility with the previous settings preferences.
        context.getSharedPreferences("BlurrSettings", Context.MODE_PRIVATE)
            .edit()
            .putString(GEMINI_KEYS_KEY, trimmed)
            .apply()
        currentIndex.set(0)
    }

    fun getSavedKeys(context: Context): String {
        val jarvisSaved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(GEMINI_KEYS_KEY, null)?.trim()
        val legacySaved = context.getSharedPreferences("BlurrSettings", Context.MODE_PRIVATE)
            .getString(GEMINI_KEYS_KEY, null)?.trim()
        return when {
            !jarvisSaved.isNullOrEmpty() -> jarvisSaved
            !legacySaved.isNullOrEmpty() -> legacySaved
            else -> BuildConfig.GEMINI_API_KEYS
        }
    }

    fun getKeys(context: Context? = null): List<String> {
        val effectiveContext = context ?: try { MyApplication.appContext } catch (_: Exception) { null }
        val source = if (effectiveContext != null) {
            val jarvisSaved = effectiveContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(GEMINI_KEYS_KEY, null)?.trim()
            val legacySaved = effectiveContext.getSharedPreferences("BlurrSettings", Context.MODE_PRIVATE)
                .getString(GEMINI_KEYS_KEY, null)?.trim()
            when {
                !jarvisSaved.isNullOrEmpty() -> jarvisSaved
                !legacySaved.isNullOrEmpty() -> legacySaved
                else -> BuildConfig.GEMINI_API_KEYS
            }
        } else {
            BuildConfig.GEMINI_API_KEYS
        }
        return source.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    fun getNextKey(context: Context? = null): String {
        val keys = getKeys(context)
        if (keys.isEmpty()) {
            throw IllegalStateException("Gemini API key is not configured. Add a key in Settings.")
        }
        val index = currentIndex.getAndIncrement() % keys.size
        return keys[index]
    }

    fun rotateKey(context: Context? = null): String = getNextKey(context)
}