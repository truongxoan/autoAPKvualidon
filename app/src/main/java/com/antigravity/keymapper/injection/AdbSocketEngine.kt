package com.antigravity.keymapper.injection

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * High-performance, 0-latency touch injection engine connecting directly to the
 * ADB daemon running via shell permissions.
 *
 * It sends binary packets: [MAGIC: 4 bytes][SLOT: 1 byte][ACTION: 1 byte][X: 4 bytes float][Y: 4 bytes float]
 */
class AdbSocketEngine(private val port: Int = 62123) : TouchInjectionEngine {

    companion object {
        private const val TAG = "AdbSocketEngine"
        private const val MAGIC = 0x4B45594D // "KEYM"
    }

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var isConnectedState = false

    private val workerThread = HandlerThread("AdbSocketWorker").apply { start() }
    private val workerHandler = Handler(workerThread.looper)

    init {
        connectAsync()
    }

    fun connectAsync() {
        workerHandler.post {
            try {
                if (socket == null || socket?.isClosed == true || !socket!!.isConnected) {
                    socket?.close()
                    socket = Socket()
                    socket?.connect(InetSocketAddress("127.0.0.1", port), 800)
                    socket?.tcpNoDelay = true
                    outputStream = socket?.getOutputStream()
                    isConnectedState = true
                    Log.i(TAG, "Connected successfully to ADB injection daemon on port $port")
                }
            } catch (e: Exception) {
                isConnectedState = false
                Log.d(TAG, "ADB daemon not running on port $port: ${e.message}")
            }
        }
    }

    override fun isConnected(): Boolean = isConnectedState

    var enableHumanize: Boolean = true

    override fun injectTouch(slot: Int, action: Int, x: Float, y: Float) {
        if (!isConnectedState) {
            connectAsync()
            return
        }

        val finalX = if (enableHumanize) com.antigravity.keymapper.helper.HumanizeTouchHelper.applyJitter(x) else x
        val finalY = if (enableHumanize) com.antigravity.keymapper.helper.HumanizeTouchHelper.applyJitter(y) else y

        workerHandler.post {
            try {
                val buffer = ByteBuffer.allocate(14)
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                buffer.putInt(MAGIC)
                buffer.put(slot.toByte())
                buffer.put(action.toByte())
                buffer.putFloat(finalX)
                buffer.putFloat(finalY)

                outputStream?.write(buffer.array())
                outputStream?.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send touch packet", e)
                isConnectedState = false
                socket?.close()
            }
        }
    }

    override fun tap(x: Float, y: Float) {
        injectTouch(0, 0, x, y) // DOWN
        val duration = if (enableHumanize) com.antigravity.keymapper.helper.HumanizeTouchHelper.getRandomTapDuration() else 30L
        workerHandler.postDelayed({
            injectTouch(0, 1, x, y) // UP
        }, duration)
    }

    override fun swipe(slot: Int, startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long) {
        injectTouch(slot, 0, startX, startY) // DOWN
        val steps = 8
        val stepInterval = (durationMs / steps).coerceAtLeast(3)
        val ctrl = com.antigravity.keymapper.helper.HumanizeTouchHelper.getBezierControlPoint(startX, startY, endX, endY)

        for (i in 1..steps) {
            val progress = i.toFloat() / steps
            val currentX = com.antigravity.keymapper.helper.HumanizeTouchHelper.evaluateBezier(startX, ctrl.first, endX, progress)
            val currentY = com.antigravity.keymapper.helper.HumanizeTouchHelper.evaluateBezier(startY, ctrl.second, endY, progress)
            workerHandler.postDelayed({
                injectTouch(slot, 2, currentX, currentY) // MOVE
            }, i * stepInterval)
        }
        workerHandler.postDelayed({
            injectTouch(slot, 1, endX, endY) // UP
        }, durationMs)
    }

    fun release() {
        workerThread.quitSafely()
        try {
            socket?.close()
        } catch (e: Exception) {
            // ignore
        }
    }
}
