package com.antigravity.keymapper.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.antigravity.keymapper.model.KeyMapping
import com.antigravity.keymapper.model.KeyType
import com.antigravity.keymapper.model.KeymapProfile

class KeymapOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var profile: KeymapProfile? = null
        set(value) {
            field = value
            invalidate()
        }

    var isEditMode: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var buttonOpacity: Float = 0.45f // 0.0f (invisible) to 1.0f (solid)
        set(value) {
            field = value.coerceIn(0.05f, 1.0f)
            invalidate()
        }

    var buttonScale: Float = 1.0f // 0.5f (mini) to 2.0f (large)
        set(value) {
            field = value.coerceIn(0.4f, 2.5f)
            invalidate()
        }

    var onKeySelectedListener: ((KeyMapping) -> Unit)? = null

    private var selectedKey: KeyMapping? = null
    private var isDragging = false
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    // Paints
    private val circleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B3161B22")
        style = Paint.Style.FILL
    }

    private val circleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val wasdStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF0055")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8B949E")
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val p = profile ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density

        // Apply alpha based on buttonOpacity
        val alphaInt = (buttonOpacity * 255).toInt().coerceIn(10, 255)
        circleFillPaint.alpha = (alphaInt * 0.7f).toInt()
        circleStrokePaint.alpha = alphaInt
        wasdStrokePaint.alpha = alphaInt
        textPaint.alpha = alphaInt
        subTextPaint.alpha = (alphaInt * 0.8f).toInt()

        for (key in p.keys) {
            val cx = key.xPercent * w
            val cy = key.yPercent * h
            val radius = key.radiusDp * density * buttonScale

            textPaint.textSize = 32f * buttonScale
            subTextPaint.textSize = 22f * buttonScale

            when (key.type) {
                KeyType.WASD_JOYSTICK -> {
                    // Draw joystick outer circle
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    canvas.drawCircle(cx, cy, radius, wasdStrokePaint)

                    // Draw inner WASD cross
                    canvas.drawText("W", cx, cy - radius * 0.45f + 10f, textPaint)
                    canvas.drawText("S", cx, cy + radius * 0.55f + 10f, textPaint)
                    canvas.drawText("A", cx - radius * 0.5f, cy + 10f, textPaint)
                    canvas.drawText("D", cx + radius * 0.5f, cy + 10f, textPaint)
                    canvas.drawText("JOYSTICK", cx, cy + radius + 25f, subTextPaint)
                }

                KeyType.FPS_CAMERA -> {
                    // Draw camera swipe dashed area
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    canvas.drawCircle(cx, cy, radius, circleStrokePaint)
                    canvas.drawText("TÂM XOAY", cx, cy, textPaint)
                    canvas.drawText("Mouse Look", cx, cy + 30f, subTextPaint)
                }

                KeyType.MOUSE_FIRE -> {
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    val fireStroke = Paint(circleStrokePaint).apply { color = Color.parseColor("#FF0055") }
                    canvas.drawCircle(cx, cy, radius, fireStroke)
                    canvas.drawText("BẮN", cx, cy - 5f, textPaint)
                    canvas.drawText("LMB", cx, cy + 25f, subTextPaint)
                }

                KeyType.MOUSE_AIM -> {
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    val aimStroke = Paint(circleStrokePaint).apply { color = Color.parseColor("#00E676") }
                    canvas.drawCircle(cx, cy, radius, aimStroke)
                    canvas.drawText("NGẮM", cx, cy - 5f, textPaint)
                    canvas.drawText("RMB", cx, cy + 25f, subTextPaint)
                }

                KeyType.MOUSE_SCROLL_UP -> {
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    val scrollStroke = Paint(circleStrokePaint).apply { color = Color.parseColor("#FFD600") }
                    canvas.drawCircle(cx, cy, radius, scrollStroke)
                    canvas.drawText("LĂN ▲", cx, cy + 8f, textPaint)
                }

                KeyType.MOUSE_SCROLL_DOWN -> {
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    val scrollStroke = Paint(circleStrokePaint).apply { color = Color.parseColor("#FFD600") }
                    canvas.drawCircle(cx, cy, radius, scrollStroke)
                    canvas.drawText("LĂN ▼", cx, cy + 8f, textPaint)
                }

                KeyType.MOUSE_MIDDLE -> {
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    val midStroke = Paint(circleStrokePaint).apply { color = Color.parseColor("#00E5FF") }
                    canvas.drawCircle(cx, cy, radius, midStroke)
                    canvas.drawText("LĂN ●", cx, cy + 8f, textPaint)
                }

                KeyType.MACRO_TRIGGER -> {
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    val macroStroke = Paint(circleStrokePaint).apply { color = Color.parseColor("#E040FB") }
                    canvas.drawCircle(cx, cy, radius, macroStroke)
                    canvas.drawText(key.label.ifBlank { "MACRO" }, cx, cy - 4f, textPaint)
                    canvas.drawText("🔁 AUTO", cx, cy + 22f, subTextPaint)
                }

                KeyType.SINGLE_KEY -> {
                    canvas.drawCircle(cx, cy, radius, circleFillPaint)
                    canvas.drawCircle(cx, cy, radius, circleStrokePaint)
                    canvas.drawText(key.label, cx, cy + 10f, textPaint)
                }
            }

            if (key == selectedKey && isEditMode) {
                val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#00E676")
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                }
                canvas.drawCircle(cx, cy, radius + 8f, highlightPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEditMode) return false

        val x = event.x
        val y = event.y
        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val p = profile ?: return false
                // Find touched key
                for (key in p.keys.reversed()) {
                    val cx = key.xPercent * w
                    val cy = key.yPercent * h
                    val radius = key.radiusDp * density * buttonScale
                    val distSq = (x - cx) * (x - cx) + (y - cy) * (y - cy)
                    if (distSq <= radius * radius) {
                        selectedKey = key
                        isDragging = true
                        touchOffsetX = x - cx
                        touchOffsetY = y - cy
                        invalidate()
                        return true
                    }
                }
                selectedKey = null
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging && selectedKey != null) {
                    val newCx = (x - touchOffsetX).coerceIn(0f, w)
                    val newCy = (y - touchOffsetY).coerceIn(0f, h)
                    selectedKey!!.xPercent = newCx / w
                    selectedKey!!.yPercent = newCy / h
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isDragging && selectedKey != null) {
                    isDragging = false
                    onKeySelectedListener?.invoke(selectedKey!!)
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
