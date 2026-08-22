package com.antigravity.keymapper.model

enum class KeyType {
    SINGLE_KEY,        // Normal single button (e.g. R, F, Space, 1, 2)
    WASD_JOYSTICK,     // 4-way / 8-way movement dpad
    MOUSE_FIRE,        // Left Mouse Button -> Tap/Hold on fire button
    MOUSE_AIM,         // Right Mouse Button -> Tap on aim button / ADS
    FPS_CAMERA,        // Mouse look swipe area
    MOUSE_SCROLL_UP,   // Con lăn chuột lên (Đổi súng 1 / Nhảy)
    MOUSE_SCROLL_DOWN, // Con lăn chuột xuống (Đổi súng 2 / Nạp đạn)
    MOUSE_MIDDLE,      // Nhấn con lăn chuột (Ping / Ném lựu đạn)
    MACRO_TRIGGER      // On-screen or Key trigger to run a recorded Macro sequence
}
