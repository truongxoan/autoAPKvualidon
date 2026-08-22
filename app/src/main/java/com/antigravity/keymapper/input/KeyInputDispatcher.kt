package com.antigravity.keymapper.input

import android.view.KeyEvent
import android.view.MotionEvent
import com.antigravity.keymapper.injection.TouchInjectionEngine
import com.antigravity.keymapper.macro.MacroManager
import com.antigravity.keymapper.model.KeyMapping
import com.antigravity.keymapper.model.KeyType
import com.antigravity.keymapper.model.KeymapProfile

class KeyInputDispatcher(
    private val engine: TouchInjectionEngine
) {
    val wasdController = WasdJoystickController(engine, touchSlot = 0)
    val fpsAimController = FpsAimController(engine, touchSlot = 1)
    val gyroSimulator = GyroscopeSimulator(engine, gyroSlot = 3)
    val macroManager = MacroManager(engine)

    private var activeProfile: KeymapProfile? = null
    private var screenWidth: Int = 1920
    private var screenHeight: Int = 1080
    private var density: Float = 2.0f

    // Active touch slot tracking for multi-finger single keys
    private val keyToSlotMap = mutableMapOf<Int, Int>()
    private var nextAvailableSlot = 4 // 0=WASD, 1=Camera, 3=Gyro

    var isPointerLocked: Boolean = false
    var isStealthMode: Boolean = false
    var onStealthModeChanged: ((Boolean) -> Unit)? = null

    var toggleLockKey: Int = KeyEvent.KEYCODE_GRAVE // `~` key
    var toggleStealthKey: Int = KeyEvent.KEYCODE_B   // Ctrl + B (hoặc F4)
    var toggleStealthKeyAlt: Int = KeyEvent.KEYCODE_F4 // Phím F4 để ẩn/hiện tàng hình nhanh
    var toggleMacroRecordKey: Int = KeyEvent.KEYCODE_F5 // Phím F5 để Bắt đầu/Dừng Ghi Macro
    var toggleMacroPlayKey: Int = KeyEvent.KEYCODE_F6   // Phím F6 để Bật/Dừng Chạy Lặp Vô Hạn

    fun setScreenMetrics(width: Int, height: Int, displayDensity: Float) {
        screenWidth = width
        screenHeight = height
        density = displayDensity
        gyroSimulator.setupGyroArea(width, height)
        rebuildControllers()
    }

    fun setProfile(profile: KeymapProfile) {
        activeProfile = profile
        rebuildControllers()
    }

    private fun rebuildControllers() {
        val profile = activeProfile ?: return
        val wasdMapping = profile.keys.find { it.type == KeyType.WASD_JOYSTICK }
        if (wasdMapping != null) {
            wasdController.setupJoystick(wasdMapping, screenWidth, screenHeight, density)
        }

        val cameraMapping = profile.keys.find { it.type == KeyType.FPS_CAMERA }
        if (cameraMapping != null) {
            fpsAimController.setupCameraArea(cameraMapping, screenWidth, screenHeight, density)
        }
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val isDown = event.action == KeyEvent.ACTION_DOWN
        val profile = activeProfile ?: return false

        // Check toggle pointer lock (`~` or Ctrl)
        if (keyCode == toggleLockKey || keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_ALT_LEFT) {
            if (isDown && event.repeatCount == 0) {
                isPointerLocked = !isPointerLocked
            }
            return true
        }

        // Check Stealth / Ghost Mode toggle (Ctrl + B hoặc F4)
        if ((keyCode == toggleStealthKey && event.isCtrlPressed) || keyCode == toggleStealthKeyAlt) {
            if (isDown && event.repeatCount == 0) {
                isStealthMode = !isStealthMode
                onStealthModeChanged?.invoke(isStealthMode)
            }
            return true
        }

        // Check Macro Record Hotkey (F5)
        if (keyCode == toggleMacroRecordKey) {
            if (isDown && event.repeatCount == 0) {
                if (macroManager.isRecording) {
                    macroManager.stopRecording()
                } else {
                    macroManager.startRecording("Quick Macro", isInfinite = true)
                }
            }
            return true
        }

        // Check Macro Playback Loop Hotkey (F6)
        if (keyCode == toggleMacroPlayKey) {
            if (isDown && event.repeatCount == 0) {
                if (macroManager.isPlaying) {
                    macroManager.stopPlayback()
                } else {
                    macroManager.playMacro(screenWidth = screenWidth, screenHeight = screenHeight)
                }
            }
            return true
        }

        // Smart Cursor Auto-Release for Map (M/T) and Backpack (B/Tab)
        if (keyCode == KeyEvent.KEYCODE_TAB || keyCode == KeyEvent.KEYCODE_B || keyCode == KeyEvent.KEYCODE_M || keyCode == KeyEvent.KEYCODE_T) {
            if (isDown && event.repeatCount == 0) {
                // Toggle pointer lock automatically when opening inventory/map
                isPointerLocked = !isPointerLocked
            }
        }

        // Try WASD
        if (wasdController.handleKeyEvent(keyCode, isDown)) {
            return true
        }

        // Macro Trigger by Key
        val macroMapping = profile.keys.find { it.type == KeyType.MACRO_TRIGGER && it.keyCode == keyCode }
        if (macroMapping != null) {
            if (isDown && event.repeatCount == 0) {
                if (macroManager.isPlaying) {
                    macroManager.stopPlayback()
                } else {
                    macroManager.playMacro(screenWidth = screenWidth, screenHeight = screenHeight)
                }
            }
            return true
        }

        // Single key mappings
        val mapping = profile.keys.find { it.type == KeyType.SINGLE_KEY && it.keyCode == keyCode }
        if (mapping != null) {
            val posX = mapping.xPercent * screenWidth
            val posY = mapping.yPercent * screenHeight

            if (mapping.isContinuous) {
                if (isDown && !keyToSlotMap.containsKey(keyCode)) {
                    val slot = allocateSlot(keyCode)
                    engine.injectTouch(slot, 0, posX, posY) // DOWN
                    macroManager.recordTouch(slot, 0, mapping.xPercent, mapping.yPercent)
                } else if (!isDown && keyToSlotMap.containsKey(keyCode)) {
                    val slot = releaseSlot(keyCode)
                    engine.injectTouch(slot, 1, posX, posY) // UP
                    macroManager.recordTouch(slot, 1, mapping.xPercent, mapping.yPercent)
                }
            } else {
                if (isDown && event.repeatCount == 0) {
                    engine.tap(posX, posY)
                    macroManager.recordTouch(2, 0, mapping.xPercent, mapping.yPercent)
                    macroManager.recordTouch(2, 1, mapping.xPercent, mapping.yPercent)
                }
            }
            return true
        }

        return false
    }

    fun handleMotionEvent(event: MotionEvent): Boolean {
        val profile = activeProfile ?: return false

        // Mouse Look / Camera rotation & Gyroscope
        if (isPointerLocked && event.action == MotionEvent.ACTION_MOVE) {
            val globalSens = activeProfile?.sensitivityX ?: 1.0f
            val dx = event.getAxisValue(MotionEvent.AXIS_RELATIVE_X)
            val dy = event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)
            fpsAimController.onMouseMove(dx, dy, profile.sensitivityX, profile.sensitivityY)
            gyroSimulator.applyGyroDelta(dx, dy)
            return true
        }

        // Mouse Scroll Wheel (AXIS_VSCROLL)
        if (event.action == MotionEvent.ACTION_SCROLL) {
            val vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            if (vscroll > 0f) {
                // Scroll UP -> Weapon 1 / Jump
                val scrollUpMapping = profile.keys.find { it.type == KeyType.MOUSE_SCROLL_UP }
                if (scrollUpMapping != null) {
                    engine.tap(scrollUpMapping.xPercent * screenWidth, scrollUpMapping.yPercent * screenHeight)
                } else {
                    // Default fallback: Weapon 1 (45% X, 85% Y)
                    engine.tap(0.45f * screenWidth, 0.85f * screenHeight)
                }
                return true
            } else if (vscroll < 0f) {
                // Scroll DOWN -> Weapon 2 / Reload
                val scrollDownMapping = profile.keys.find { it.type == KeyType.MOUSE_SCROLL_DOWN }
                if (scrollDownMapping != null) {
                    engine.tap(scrollDownMapping.xPercent * screenWidth, scrollDownMapping.yPercent * screenHeight)
                } else {
                    // Default fallback: Weapon 2 (55% X, 85% Y)
                    engine.tap(0.55f * screenWidth, 0.85f * screenHeight)
                }
                return true
            }
        }

        // Mouse buttons (Left click -> Fire, Right click -> Aim, Middle click -> Ping/Grenade)
        if (event.action == MotionEvent.ACTION_BUTTON_PRESS || event.action == MotionEvent.ACTION_BUTTON_RELEASE) {
            val isPress = event.action == MotionEvent.ACTION_BUTTON_PRESS
            val buttonState = event.buttonState

            // Middle Click (Scroll Click)
            if (event.actionButton == MotionEvent.BUTTON_TERTIARY || buttonState and MotionEvent.BUTTON_TERTIARY != 0) {
                val middleMapping = profile.keys.find { it.type == KeyType.MOUSE_MIDDLE }
                val posX = (middleMapping?.xPercent ?: 0.65f) * screenWidth
                val posY = (middleMapping?.yPercent ?: 0.35f) * screenHeight
                if (isPress) {
                    engine.tap(posX, posY)
                }
                return true
            }

            // Check Fire Button (LMB)
            val fireMapping = profile.keys.find { it.type == KeyType.MOUSE_FIRE }
            if (fireMapping != null && (event.actionButton == MotionEvent.BUTTON_PRIMARY || buttonState and MotionEvent.BUTTON_PRIMARY != 0)) {
                val posX = fireMapping.xPercent * screenWidth
                val posY = fireMapping.yPercent * screenHeight
                val slot = 8 // dedicated fire slot
                fpsAimController.isFiring = isPress
                if (isPress) {
                    engine.injectTouch(slot, 0, posX, posY)
                    macroManager.recordTouch(slot, 0, fireMapping.xPercent, fireMapping.yPercent)
                } else {
                    engine.injectTouch(slot, 1, posX, posY)
                    macroManager.recordTouch(slot, 1, fireMapping.xPercent, fireMapping.yPercent)
                }
                return true
            }

            // Check Aim Button (RMB)
            val aimMapping = profile.keys.find { it.type == KeyType.MOUSE_AIM }
            if (aimMapping != null && (event.actionButton == MotionEvent.BUTTON_SECONDARY || buttonState and MotionEvent.BUTTON_SECONDARY != 0)) {
                val posX = aimMapping.xPercent * screenWidth
                val posY = aimMapping.yPercent * screenHeight
                fpsAimController.isAiming = isPress
                if (isPress) {
                    engine.tap(posX, posY)
                    macroManager.recordTouch(9, 0, aimMapping.xPercent, aimMapping.yPercent)
                    macroManager.recordTouch(9, 1, aimMapping.xPercent, aimMapping.yPercent)
                }
                return true
            }
        }

        return false
    }

    private fun allocateSlot(key: Int): Int {
        val slot = nextAvailableSlot
        keyToSlotMap[key] = slot
        nextAvailableSlot = if (nextAvailableSlot >= 7) 2 else nextAvailableSlot + 1
        return slot
    }

    private fun releaseSlot(key: Int): Int {
        return keyToSlotMap.remove(key) ?: 2
    }

    fun reset() {
        wasdController.release()
        fpsAimController.stopLook()
        keyToSlotMap.clear()
    }
}
