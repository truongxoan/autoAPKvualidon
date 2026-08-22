package com.antigravity.keymapper.helper

import java.util.Random

/**
 * Humanize Touch Helper to evade AI behavioral anti-cheat analysis.
 * Generates natural biological micro-jitter and dynamic press durations.
 */
object HumanizeTouchHelper {
    private val random = Random()

    /**
     * Add subtle biological finger micro-jitter (-1.2px to +1.2px)
     */
    fun applyJitter(value: Float, intensity: Float = 1.0f): Float {
        // Gaussian distribution centered at 0 with standard deviation ~ 0.5
        val jitter = (random.nextGaussian() * 0.6f * intensity).toFloat()
        return value + jitter
    }

    /**
     * Random human-like tap hold duration between 40ms and 75ms
     */
    fun getRandomTapDuration(): Long {
        return (45 + random.nextInt(30)).toLong()
    }

    /**
     * Generate intermediate control point for natural curved swipe (Quadratic Bezier)
     */
    fun getBezierControlPoint(startX: Float, startY: Float, endX: Float, endY: Float): Pair<Float, Float> {
        val midX = (startX + endX) / 2f
        val midY = (startY + endY) / 2f

        val dx = endX - startX
        val dy = endY - startY

        // Perpendicular offset with small random variance
        val curveIntensity = 0.08f + (random.nextFloat() * 0.04f)
        val ctrlX = midX - dy * curveIntensity
        val ctrlY = midY + dx * curveIntensity

        return Pair(ctrlX, ctrlY)
    }

    /**
     * Calculate point on quadratic Bezier curve at t (0.0 .. 1.0)
     */
    fun evaluateBezier(p0: Float, p1: Float, p2: Float, t: Float): Float {
        val u = 1f - t
        return u * u * p0 + 2 * u * t * p1 + t * t * p2
    }
}
