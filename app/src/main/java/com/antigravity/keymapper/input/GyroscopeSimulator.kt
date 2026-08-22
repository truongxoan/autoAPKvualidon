package com.antigravity.keymapper.input

import com.antigravity.keymapper.injection.TouchInjectionEngine

/**
 * Simulates hardware gyroscope motion via micro mouse movements.
 * Provides micro-adjustment and natural recoil dampening.
 */
class GyroscopeSimulator(
    private val engine: TouchInjectionEngine,
    private val gyroSlot: Int = 3
) {
    var isEnabled: Boolean = true
    var gyroSensitivity: Float = 0.5f

    private var isActive = false
    private var currentX = 0f
    private var currentY = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 150f

    fun setupGyroArea(screenW: Int, screenH: Int) {
        centerX = screenW * 0.72f
        centerY = screenH * 0.50f
        currentX = centerX
        currentY = centerY
    }

    fun applyGyroDelta(dx: Float, dy: Float) {
        if (!isEnabled || (dx == 0f && dy == 0f)) return

        val gyroDx = dx * gyroSensitivity * 0.8f
        val gyroDy = dy * gyroSensitivity * 0.8f

        if (!isActive) {
            currentX = centerX
            currentY = centerY
            engine.injectTouch(gyroSlot, 0, currentX, currentY) // DOWN
            isActive = true
        }

        currentX += gyroDx
        currentY += gyroDy

        val distSq = (currentX - centerX) * (currentX - centerX) + (currentY - centerY) * (currentY - centerY)
        if (distSq > radius * radius) {
            engine.injectTouch(gyroSlot, 1, currentX, currentY) // UP
            currentX = centerX
            currentY = centerY
            engine.injectTouch(gyroSlot, 0, currentX, currentY) // DOWN
        } else {
            engine.injectTouch(gyroSlot, 2, currentX, currentY) // MOVE
        }
    }

    fun stopGyro() {
        if (isActive) {
            engine.injectTouch(gyroSlot, 1, currentX, currentY)
            isActive = false
        }
    }
}
