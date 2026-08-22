package com.antigravity.keymapper.input

import com.antigravity.keymapper.injection.TouchInjectionEngine
import com.antigravity.keymapper.model.KeyMapping

/**
 * Handles smooth FPS camera look (Aiming/Camera Rotation) using mouse relative movement (dx, dy).
 * Simulates human touch drag gestures across the camera area without abrupt resets.
 */
class FpsAimController(
    private val engine: TouchInjectionEngine,
    private val touchSlot: Int = 1 // Dedicated touch slot for camera
) {
    private var isLooking = false
    private var currentX = 0f
    private var currentY = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 200f

    var isAiming = false
    var isFiring = false
    var recoilStrength = 0.8f // Automatic pull-down intensity
    var adsMultiplier = 0.6f  // Precision slowdown when aiming

    fun setupCameraArea(mapping: KeyMapping, screenWidth: Int, screenHeight: Int, density: Float) {
        centerX = mapping.xPercent * screenWidth
        centerY = mapping.yPercent * screenHeight
        radius = mapping.radiusDp * density * 2.5f
        currentX = centerX
        currentY = centerY
    }

    fun onMouseMove(deltaX: Float, deltaY: Float, sensX: Float, sensY: Float) {
        if (deltaX == 0f && deltaY == 0f && !isFiring) return

        val speedMultiplier = if (isAiming) adsMultiplier else 1.0f
        var scaledDx = deltaX * sensX * 1.5f * speedMultiplier
        var scaledDy = deltaY * sensY * 1.5f * speedMultiplier

        // Add auto recoil compensation (downward pull when firing)
        if (isFiring) {
            scaledDy += recoilStrength * 2.2f
        }

        if (!isLooking) {
            // Touch down at center
            currentX = centerX
            currentY = centerY
            engine.injectTouch(touchSlot, 0, currentX, currentY) // DOWN
            isLooking = true
        }

        // Advance position
        currentX += scaledDx
        currentY += scaledDy

        // Check if exceeded bounds of look circle
        val distSq = (currentX - centerX) * (currentX - centerX) + (currentY - centerY) * (currentY - centerY)
        if (distSq > radius * radius) {
            // Lift finger and reset to center seamlessly
            engine.injectTouch(touchSlot, 1, currentX, currentY) // UP
            currentX = centerX
            currentY = centerY
            engine.injectTouch(touchSlot, 0, currentX, currentY) // DOWN
        } else {
            // Send move event
            engine.injectTouch(touchSlot, 2, currentX, currentY) // MOVE
        }
    }

    fun stopLook() {
        if (isLooking) {
            engine.injectTouch(touchSlot, 1, currentX, currentY) // UP
            isLooking = false
        }
    }
}
