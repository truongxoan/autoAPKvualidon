package com.antigravity.keymapper.injection

interface TouchInjectionEngine {
    /**
     * Send touch event to system.
     * @param slot Touch pointer index (0-9 for multi-touch support)
     * @param action 0 = DOWN, 1 = UP, 2 = MOVE
     * @param x Screen coordinate X
     * @param y Screen coordinate Y
     */
    fun injectTouch(slot: Int, action: Int, x: Float, y: Float)

    /**
     * Perform an instantaneous tap
     */
    fun tap(x: Float, y: Float)

    /**
     * Perform continuous swipe between points (used for smooth FPS look)
     */
    fun swipe(slot: Int, startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long)

    /**
     * Check if this engine backend is connected and ready
     */
    fun isConnected(): Boolean
}
