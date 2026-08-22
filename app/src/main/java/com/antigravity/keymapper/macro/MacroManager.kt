package com.antigravity.keymapper.macro

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import com.antigravity.keymapper.injection.TouchInjectionEngine

class MacroManager(
    private val engine: TouchInjectionEngine
) {
    companion object {
        private const val TAG = "MacroManager"
    }

    var isRecording = false
        private set
    var isPlaying = false
        private set

    private var currentRecording: MacroProfile? = null
    private var lastRecordedTimeMs: Long = 0

    private val playbackThread = HandlerThread("MacroPlaybackThread").apply { start() }
    private val playbackHandler = Handler(playbackThread.looper)

    var onStatusChangedListener: ((isRecording: Boolean, isPlaying: Boolean) -> Unit)? = null
    var activeMacro: MacroProfile? = null

    fun startRecording(name: String = "Macro Record", isInfinite: Boolean = false) {
        if (isPlaying) stopPlayback()
        currentRecording = MacroProfile(
            name = name,
            isInfiniteLoop = isInfinite,
            loopCount = if (isInfinite) 0 else 1
        )
        lastRecordedTimeMs = SystemClock.uptimeMillis()
        isRecording = true
        onStatusChangedListener?.invoke(true, false)
        Log.i(TAG, "Started recording macro: $name")
    }

    fun recordTouch(slot: Int, action: Int, xPercent: Float, yPercent: Float) {
        if (!isRecording) return
        val recording = currentRecording ?: return
        val now = SystemClock.uptimeMillis()
        val delay = (now - lastRecordedTimeMs).coerceAtLeast(0)
        lastRecordedTimeMs = now

        recording.steps.add(
            MacroStep(
                slot = slot,
                action = action,
                xPercent = xPercent,
                yPercent = yPercent,
                delayMsAfter = delay
            )
        )
    }

    fun stopRecording(): MacroProfile? {
        if (!isRecording) return null
        isRecording = false
        val result = currentRecording
        currentRecording = null
        activeMacro = result
        onStatusChangedListener?.invoke(false, false)
        Log.i(TAG, "Stopped recording. Total steps: ${result?.steps?.size ?: 0}")
        return result
    }

    fun playMacro(
        targetMacro: MacroProfile? = null,
        screenWidth: Int,
        screenHeight: Int
    ) {
        val macro = targetMacro ?: activeMacro ?: return
        if (isRecording) stopRecording()
        if (isPlaying) stopPlayback()
        if (macro.steps.isEmpty()) return

        isPlaying = true
        onStatusChangedListener?.invoke(false, true)

        playbackHandler.post {
            executeMacroLoop(macro, screenWidth, screenHeight, currentLoop = 0)
        }
    }

    private fun executeMacroLoop(
        macro: MacroProfile,
        screenWidth: Int,
        screenHeight: Int,
        currentLoop: Int
    ) {
        if (!isPlaying) return

        var cumulativeDelay = 0L
        val steps = ArrayList(macro.steps)

        for (i in steps.indices) {
            val step = steps[i]
            cumulativeDelay += step.delayMsAfter

            playbackHandler.postDelayed({
                if (isPlaying) {
                    val x = step.xPercent * screenWidth
                    val y = step.yPercent * screenHeight
                    engine.injectTouch(step.slot, step.action, x, y)
                }
            }, cumulativeDelay)
        }

        // Loop execution check
        playbackHandler.postDelayed({
            if (!isPlaying) return@postDelayed

            val nextLoop = currentLoop + 1
            if (macro.isInfiniteLoop || (macro.loopCount > 0 && nextLoop < macro.loopCount)) {
                // Next loop iteration
                executeMacroLoop(macro, screenWidth, screenHeight, nextLoop)
            } else {
                // Done
                isPlaying = false
                onStatusChangedListener?.invoke(false, false)
            }
        }, cumulativeDelay + macro.intervalBetweenLoopsMs)
    }

    fun stopPlayback() {
        if (!isPlaying) return
        isPlaying = false
        playbackHandler.removeCallbacksAndMessages(null)
        onStatusChangedListener?.invoke(false, false)
        Log.i(TAG, "Macro playback stopped.")
    }

    fun release() {
        stopRecording()
        stopPlayback()
        playbackThread.quitSafely()
    }
}
