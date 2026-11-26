package com.blurr.voice.utilities

import android.content.Context
import androidx.core.content.ContextCompat
import com.blurr.voice.R

/**
 * Utility class for mapping JarvisState values to their corresponding colors
 * and providing state-related information for the delta symbol.
 */
object DeltaStateColorMapper {

    /**
     * Data class representing the visual state of the delta symbol
     */
    data class DeltaVisualState(
        val state: JarvisState,
        val color: Int,
        val statusText: String,
        val colorHex: String
    )

    /**
     * Get the color resource ID for a given JarvisState
     */
    fun getColorResourceId(state: JarvisState): Int {
        return when (state) {
            JarvisState.IDLE -> R.color.delta_idle
            JarvisState.LISTENING -> R.color.delta_listening
            JarvisState.PROCESSING -> R.color.delta_processing
            JarvisState.SPEAKING -> R.color.delta_speaking
            JarvisState.ERROR -> R.color.delta_error
        }
    }

    /**
     * Get the resolved color value for a given JarvisState
     */
    fun getColor(context: Context, state: JarvisState): Int {
        val colorResId = getColorResourceId(state)
        return ContextCompat.getColor(context, colorResId)
    }

    /**
     * Get the status text for a given JarvisState
     */
    fun getStatusText(state: JarvisState): String {
        return when (state) {
            JarvisState.IDLE -> "Ready, tap delta to wake me up!"
            JarvisState.LISTENING -> "Listening..."
            JarvisState.PROCESSING -> "Processing..."
            JarvisState.SPEAKING -> "Speaking..."
            JarvisState.ERROR -> "Error"
        }
    }

    /**
     * Get the hex color string for a given JarvisState (for debugging/logging)
     */
    fun getColorHex(context: Context, state: JarvisState): String {
        val color = getColor(context, state)
        return String.format("#%08X", color)
    }

    /**
     * Get complete visual state information for a given JarvisState
     */
    fun getDeltaVisualState(context: Context, state: JarvisState): DeltaVisualState {
        return DeltaVisualState(
            state = state,
            color = getColor(context, state),
            statusText = getStatusText(state),
            colorHex = getColorHex(context, state)
        )
    }

    /**
     * Get all available states with their visual information
     */
    fun getAllStates(context: Context): List<DeltaVisualState> {
        return JarvisState.values().map { state ->
            getDeltaVisualState(context, state)
        }
    }

    /**
     * Check if a state represents an active operation (not idle or error)
     */
    fun isActiveState(state: JarvisState): Boolean {
        return when (state) {
            JarvisState.LISTENING, JarvisState.PROCESSING, JarvisState.SPEAKING -> true
            JarvisState.IDLE, JarvisState.ERROR -> false
        }
    }

    /**
     * Check if a state represents an error condition
     */
    fun isErrorState(state: JarvisState): Boolean {
        return state == JarvisState.ERROR
    }

    /**
     * Get the priority of a state for determining which state to display
     * when multiple conditions might be true. Higher numbers = higher priority.
     */
    fun getStatePriority(state: JarvisState): Int {
        return when (state) {
            JarvisState.ERROR -> 5      // Highest priority
            JarvisState.SPEAKING -> 4   // High priority
            JarvisState.LISTENING -> 3  // Medium-high priority
            JarvisState.PROCESSING -> 2 // Medium priority
            JarvisState.IDLE -> 1       // Lowest priority
        }
    }
}