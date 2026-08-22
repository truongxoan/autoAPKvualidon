package com.antigravity.keymapper.model

import android.view.KeyEvent
import android.view.MotionEvent

data class KeymapProfile(
    val id: String,
    var name: String,
    var sensitivityX: Float = 1.0f,
    var sensitivityY: Float = 1.0f,
    var keys: MutableList<KeyMapping> = mutableListOf()
) {
    companion object {
        fun createFpsDefault(): KeymapProfile {
            val profile = KeymapProfile(
                id = "fps_default",
                name = "Game Bắn Súng (FPS)"
            )
            // WASD Joystick (Left side)
            profile.keys.add(
                KeyMapping(
                    label = "WASD",
                    type = KeyType.WASD_JOYSTICK,
                    xPercent = 0.18f,
                    yPercent = 0.72f,
                    radiusDp = 75f
                )
            )
            // Fire button (LMB - Left Click)
            profile.keys.add(
                KeyMapping(
                    label = "BẮN (LMB)",
                    type = KeyType.MOUSE_FIRE,
                    mouseButton = MotionEvent.BUTTON_PRIMARY,
                    xPercent = 0.85f,
                    yPercent = 0.75f,
                    radiusDp = 36f,
                    isContinuous = true
                )
            )
            // Aim button (RMB - Right Click / ADS)
            profile.keys.add(
                KeyMapping(
                    label = "NGẮM (RMB)",
                    type = KeyType.MOUSE_AIM,
                    mouseButton = MotionEvent.BUTTON_SECONDARY,
                    xPercent = 0.90f,
                    yPercent = 0.50f,
                    radiusDp = 32f
                )
            )
            // FPS Camera Look / Swipe Area (Right screen half)
            profile.keys.add(
                KeyMapping(
                    label = "TÂM NHÌN (Camera)",
                    type = KeyType.FPS_CAMERA,
                    xPercent = 0.70f,
                    yPercent = 0.45f,
                    radiusDp = 90f
                )
            )
            // Jump (Space)
            profile.keys.add(
                KeyMapping(
                    label = "SPACE (Nhảy)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_SPACE,
                    xPercent = 0.92f,
                    yPercent = 0.85f,
                    radiusDp = 30f
                )
            )
            // Crouch (C)
            profile.keys.add(
                KeyMapping(
                    label = "C (Ngồi)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_C,
                    xPercent = 0.80f,
                    yPercent = 0.88f,
                    radiusDp = 30f
                )
            )
            // Prone (Z)
            profile.keys.add(
                KeyMapping(
                    label = "Z (Nằm)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_Z,
                    xPercent = 0.73f,
                    yPercent = 0.90f,
                    radiusDp = 28f
                )
            )
            // Reload (R)
            profile.keys.add(
                KeyMapping(
                    label = "R (Nạp đạn)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_R,
                    xPercent = 0.80f,
                    yPercent = 0.60f,
                    radiusDp = 28f
                )
            )
            // Sprint / Run (Shift)
            profile.keys.add(
                KeyMapping(
                    label = "SHIFT (Chạy)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_SHIFT_LEFT,
                    xPercent = 0.25f,
                    yPercent = 0.50f,
                    radiusDp = 28f
                )
            )
            // Interact / Open Door (F)
            profile.keys.add(
                KeyMapping(
                    label = "F (Nhặt đồ)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_F,
                    xPercent = 0.65f,
                    yPercent = 0.35f,
                    radiusDp = 28f
                )
            )
            // Weapon 1 & 2
            profile.keys.add(
                KeyMapping(
                    label = "1 (Súng 1)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_1,
                    xPercent = 0.45f,
                    yPercent = 0.85f,
                    radiusDp = 28f
                )
            )
            profile.keys.add(
                KeyMapping(
                    label = "2 (Súng 2)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_2,
                    xPercent = 0.55f,
                    yPercent = 0.85f,
                    radiusDp = 28f
                )
            )
            // Scroll Wheel UP -> Súng 1
            profile.keys.add(
                KeyMapping(
                    label = "LĂN LÊN (Súng 1)",
                    type = KeyType.MOUSE_SCROLL_UP,
                    xPercent = 0.45f,
                    yPercent = 0.85f,
                    radiusDp = 24f
                )
            )
            // Scroll Wheel DOWN -> Súng 2
            profile.keys.add(
                KeyMapping(
                    label = "LĂN XUỐNG (Súng 2)",
                    type = KeyType.MOUSE_SCROLL_DOWN,
                    xPercent = 0.55f,
                    yPercent = 0.85f,
                    radiusDp = 24f
                )
            )
            // Mouse Middle Click -> Ping Vị Trí / Đồ
            profile.keys.add(
                KeyMapping(
                    label = "CLICK LĂN (Ping)",
                    type = KeyType.MOUSE_MIDDLE,
                    mouseButton = MotionEvent.BUTTON_TERTIARY,
                    xPercent = 0.65f,
                    yPercent = 0.35f,
                    radiusDp = 26f
                )
            )
            return profile
        }

        fun createMobaDefault(): KeymapProfile {
            val profile = KeymapProfile(
                id = "moba_default",
                name = "Game MOBA (Liên Quân/Wild Rift)"
            )
            // WASD Joystick
            profile.keys.add(
                KeyMapping(
                    label = "WASD",
                    type = KeyType.WASD_JOYSTICK,
                    xPercent = 0.18f,
                    yPercent = 0.72f,
                    radiusDp = 75f
                )
            )
            // Basic Attack (Space or LMB)
            profile.keys.add(
                KeyMapping(
                    label = "ĐÁNH (Space)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_SPACE,
                    xPercent = 0.88f,
                    yPercent = 0.80f,
                    radiusDp = 38f
                )
            )
            // Skill 1 (Q)
            profile.keys.add(
                KeyMapping(
                    label = "Chiêu 1 (Q)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_Q,
                    xPercent = 0.76f,
                    yPercent = 0.85f,
                    radiusDp = 30f
                )
            )
            // Skill 2 (W or E)
            profile.keys.add(
                KeyMapping(
                    label = "Chiêu 2 (E)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_E,
                    xPercent = 0.80f,
                    yPercent = 0.68f,
                    radiusDp = 30f
                )
            )
            // Skill 3 / Ulti (R)
            profile.keys.add(
                KeyMapping(
                    label = "Chiêu 3 (R)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_R,
                    xPercent = 0.88f,
                    yPercent = 0.58f,
                    radiusDp = 32f
                )
            )
            // Spell / Bổ trợ (D / F)
            profile.keys.add(
                KeyMapping(
                    label = "Tốc biến (F)",
                    type = KeyType.SINGLE_KEY,
                    keyCode = KeyEvent.KEYCODE_F,
                    xPercent = 0.70f,
                    yPercent = 0.75f,
                    radiusDp = 28f
                )
            )
            return profile
        }
    }
}
