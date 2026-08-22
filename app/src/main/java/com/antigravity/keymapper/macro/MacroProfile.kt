package com.antigravity.keymapper.macro

import java.util.UUID

data class MacroStep(
    val slot: Int = 0,
    val action: Int, // 0 = DOWN, 1 = UP, 2 = MOVE
    val xPercent: Float,
    val yPercent: Float,
    val delayMsAfter: Long
)

data class MacroProfile(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Macro Record",
    var triggerKeyCode: Int = 0, // e.g. KeyEvent.KEYCODE_F7 or mouse button
    var isInfiniteLoop: Boolean = false,
    var loopCount: Int = 1, // 0 = Infinite, >0 = specific count
    var intervalBetweenLoopsMs: Long = 100,
    val steps: MutableList<MacroStep> = mutableListOf()
)
