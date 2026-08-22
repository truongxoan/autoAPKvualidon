package com.antigravity.keymapper.vision

enum class VisionTriggerMode {
    OFF,
    AUTO_TAP_TARGET,   // Tap directly on detected image/color position
    AUTO_TRIGGER_FIRE, // Tap Fire Button when target enters crosshair ROI
    AIM_ASSIST         // Pull camera/crosshair towards target center
}
