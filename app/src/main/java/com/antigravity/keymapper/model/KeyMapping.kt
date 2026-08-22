package com.antigravity.keymapper.model

import java.util.UUID

data class KeyMapping(
    val id: String = UUID.randomUUID().toString(),
    var label: String = "Key",
    var type: KeyType = KeyType.SINGLE_KEY,
    var keyCode: Int = 0,             // Android KeyEvent.KEYCODE_...
    var mouseButton: Int = 0,         // MotionEvent.BUTTON_PRIMARY, BUTTON_SECONDARY...
    var xPercent: Float = 0.5f,       // 0.0f to 1.0f (Screen relative X)
    var yPercent: Float = 0.5f,       // 0.0f to 1.0f (Screen relative Y)
    var radiusDp: Float = 28f,        // Button size in dp
    var isContinuous: Boolean = false // If true, holds touch down until key up
)
