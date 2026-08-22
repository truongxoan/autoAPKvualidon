package com.antigravity.keymapper.vision

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.antigravity.keymapper.injection.TouchInjectionEngine

/**
 * High-performance real-time image & color detection engine.
 * Scans Region of Interest (ROI) and triggers automatic actions (Auto-Tap, Auto-Fire, Aim Assist).
 */
class VisualDetector(
    private val engine: TouchInjectionEngine
) {
    companion object {
        private const val TAG = "VisualDetector"
    }

    var mode: VisionTriggerMode = VisionTriggerMode.OFF
    var targetColor: Int = Color.RED // Default red target/marker
    var colorTolerance: Float = 0.35f
    var roiRect: Rect = Rect(860, 440, 1060, 640) // Default center crosshair area (200x200)

    private val visionThread = HandlerThread("VisionWorkerThread").apply { start() }
    private val visionHandler = Handler(visionThread.looper)
    private var isProcessing = false

    var onTargetFoundListener: ((targetX: Float, targetY: Float) -> Unit)? = null

    /**
     * Process a captured frame bitmap
     */
    fun processFrame(bitmap: Bitmap, screenWidth: Int, screenHeight: Int, fireButtonX: Float, fireButtonY: Float) {
        if (mode == VisionTriggerMode.OFF || isProcessing) return
        isProcessing = true

        visionHandler.post {
            try {
                val foundPoint = scanForColorTarget(bitmap)
                if (foundPoint != null) {
                    val (tx, ty) = foundPoint
                    onTargetFoundListener?.invoke(tx, ty)

                    when (mode) {
                        VisionTriggerMode.AUTO_TAP_TARGET -> {
                            // Tap directly on the target's coordinates
                            engine.tap(tx, ty)
                            Log.d(TAG, "Auto-tapped target at ($tx, $ty)")
                        }

                        VisionTriggerMode.AUTO_TRIGGER_FIRE -> {
                            // Tap fire button (LMB mapped coordinate)
                            engine.tap(fireButtonX, fireButtonY)
                            Log.d(TAG, "Auto-triggered fire button at ($fireButtonX, $fireButtonY)")
                        }

                        VisionTriggerMode.AIM_ASSIST -> {
                            // Gently pull crosshair towards target
                            val centerX = screenWidth / 2f
                            val centerY = screenHeight / 2f
                            val deltaX = (tx - centerX) * 0.4f
                            val deltaY = (ty - centerY) * 0.4f

                            val cameraSlot = 1
                            engine.injectTouch(cameraSlot, 0, centerX, centerY)
                            engine.injectTouch(cameraSlot, 2, centerX + deltaX, centerY + deltaY)
                            engine.injectTouch(cameraSlot, 1, centerX + deltaX, centerY + deltaY)
                        }

                        VisionTriggerMode.OFF -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in vision processing", e)
            } finally {
                isProcessing = false
            }
        }
    }

    /**
     * Fast pixel scan for target color within Region of Interest
     */
    private fun scanForColorTarget(bitmap: Bitmap): Pair<Float, Float>? {
        val left = roiRect.left.coerceIn(0, bitmap.width - 1)
        val top = roiRect.top.coerceIn(0, bitmap.height - 1)
        val right = roiRect.right.coerceIn(left + 1, bitmap.width)
        val bottom = roiRect.bottom.coerceIn(top + 1, bitmap.height)

        var matchedCount = 0
        var sumX = 0L
        var sumY = 0L

        // Step by 4 pixels for extreme speed (real-time 60fps capability)
        val step = 4

        val targetHsv = FloatArray(3)
        Color.colorToHSV(targetColor, targetHsv)

        val pixelHsv = FloatArray(3)

        for (y in top until bottom step step) {
            for (x in left until right step step) {
                val pixel = bitmap.getPixel(x, y)
                Color.colorToHSV(pixel, pixelHsv)

                // Compare Hue and Saturation
                val hueDiff = Math.abs(targetHsv[0] - pixelHsv[0])
                val isHueMatch = (hueDiff < 25f || hueDiff > 335f) // Circular hue comparison (especially for red)
                val isSatMatch = pixelHsv[1] > 0.4f && pixelHsv[2] > 0.4f // Avoid dark/gray noise

                if (isHueMatch && isSatMatch) {
                    matchedCount++
                    sumX += x
                    sumY += y
                }
            }
        }

        // Require at least a small cluster of matching pixels (avoid single noise dots)
        if (matchedCount >= 8) {
            val avgX = (sumX / matchedCount).toFloat()
            val avgY = (sumY / matchedCount).toFloat()
            return Pair(avgX, avgY)
        }

        return null
    }

    fun release() {
        visionHandler.removeCallbacksAndMessages(null)
        visionThread.quitSafely()
    }
}
