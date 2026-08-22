package com.antigravity.keymapper.injection

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.antigravity.keymapper.service.FloatingOverlayService

class AccessibilityTouchService : AccessibilityService(), TouchInjectionEngine {

    companion object {
        private const val TAG = "AccessibilityTouch"
        var instance: AccessibilityTouchService? = null
            private set

        fun isServiceRunning(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "AccessibilityTouchService connected.")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && FloatingOverlayService.isRunning) {
            val handled = FloatingOverlayService.instance?.handleGlobalKeyEvent(event) ?: false
            if (handled) return true
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            // Ignore system overlays, keyboards, and transient system UI
            if (pkg != "android" && !pkg.contains("inputmethod") && !pkg.contains("systemui")) {
                com.antigravity.keymapper.service.FloatingOverlayService.instance?.onForegroundAppChanged(pkg)
            }
        }
    }

    override fun onInterrupt() {
        instance = null
    }

    override fun isConnected(): Boolean = instance != null

    override fun injectTouch(slot: Int, action: Int, x: Float, y: Float) {
        // Fallback for simple tap/touch down
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
                lineTo(x + 1, y + 1)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 40)
            val builder = GestureDescription.Builder()
            builder.addStroke(stroke)
            dispatchGesture(builder.build(), null, null)
        }
    }

    override fun tap(x: Float, y: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 30)
            val builder = GestureDescription.Builder()
            builder.addStroke(stroke)
            dispatchGesture(builder.build(), null, null)
        }
    }

    override fun swipe(slot: Int, startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs.coerceAtLeast(20))
            val builder = GestureDescription.Builder()
            builder.addStroke(stroke)
            dispatchGesture(builder.build(), null, null)
        }
    }
}
