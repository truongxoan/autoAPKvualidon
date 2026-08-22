package com.antigravity.keymapper.input

import android.view.KeyEvent
import com.antigravity.keymapper.injection.TouchInjectionEngine
import com.antigravity.keymapper.model.KeyMapping
import kotlin.math.sqrt

/**
 * Maps W, A, S, D keys to 8-directional analog touch joystick movements.
 */
class WasdJoystickController(
    private val engine: TouchInjectionEngine,
    private val touchSlot: Int = 0 // Dedicated touch slot for joystick
) {
    private var isPressingW = false
    private var isPressingA = false
    private var isPressingS = false
    private var isPressingD = false

    private var isActive = false
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 100f

    fun setupJoystick(mapping: KeyMapping, screenWidth: Int, screenHeight: Int, density: Float) {
        centerX = mapping.xPercent * screenWidth
        centerY = mapping.yPercent * screenHeight
        radius = mapping.radiusDp * density
    }

    fun handleKeyEvent(keyCode: Int, isDown: Boolean): Boolean {
        var changed = false
        when (keyCode) {
            KeyEvent.KEYCODE_W -> { if (isPressingW != isDown) { isPressingW = isDown; changed = true } }
            KeyEvent.KEYCODE_A -> { if (isPressingA != isDown) { isPressingA = isDown; changed = true } }
            KeyEvent.KEYCODE_S -> { if (isPressingS != isDown) { isPressingS = isDown; changed = true } }
            KeyEvent.KEYCODE_D -> { if (isPressingD != isDown) { isPressingD = isDown; changed = true } }
            else -> return false
        }

        if (changed) {
            updateJoystickState()
        }
        return true
    }

    private fun updateJoystickState() {
        var dirX = 0f
        var dirY = 0f

        if (isPressingW) dirY -= 1f
        if (isPressingS) dirY += 1f
        if (isPressingA) dirX -= 1f
        if (isPressingD) dirX += 1f

        if (dirX == 0f && dirY == 0f) {
            if (isActive) {
                engine.injectTouch(touchSlot, 1, centerX, centerY) // UP
                isActive = false
            }
            return
        }

        // Normalize vector for diagonals
        val length = sqrt(dirX * dirX + dirY * dirY)
        val normX = dirX / length
        val normY = dirY / length

        val targetX = centerX + normX * radius * 0.85f
        val targetY = centerY + normY * radius * 0.85f

        if (!isActive) {
            engine.injectTouch(touchSlot, 0, centerX, centerY) // DOWN
            engine.injectTouch(touchSlot, 2, targetX, targetY) // MOVE
            isActive = true
        } else {
            engine.injectTouch(touchSlot, 2, targetX, targetY) // MOVE
        }
    }

    fun release() {
        if (isActive) {
            engine.injectTouch(touchSlot, 1, centerX, centerY)
            isActive = false
        }
        isPressingW = false
        isPressingA = false
        isPressingS = false
        isPressingD = false
    }
}
