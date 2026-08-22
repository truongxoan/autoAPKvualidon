package com.antigravity.keymapper.service

import android.content.Context
import android.util.Log

object AutoGameDetector {
    private const val TAG = "AutoGameDetector"

    private val gameToProfileMap = mapOf(
        // Free Fire
        "com.dts.freefireth" to "fps_default",
        "com.dts.freefiremax" to "fps_default",
        // PUBG Mobile
        "com.vng.pubgmobile" to "fps_default",
        "com.tencent.ig" to "fps_default",
        "com.pubg.krmobile" to "fps_default",
        "com.pubg.imobile" to "fps_default",
        // COD Mobile
        "com.vng.codmvn" to "fps_default",
        "com.activision.callofduty.shooter" to "fps_default",
        // MOBA Games
        "com.garena.game.kgvn" to "moba_default", // Liên Quân
        "com.riotgames.league.wildriftvn" to "moba_default", // Tốc Chiến
        "com.riotgames.league.wildrift" to "moba_default",
        "com.mobile.legends" to "moba_default",
        // Other Action Games
        "com.miHoYo.GenshinImpact" to "custom",
        "com.HoYoverse.Nap" to "custom"
    )

    fun checkAndGetProfile(packageName: String): String? {
        val matched = gameToProfileMap[packageName]
        if (matched != null) {
            Log.i(TAG, "Auto-detected game: $packageName -> Auto switching to profile: $matched")
        }
        return matched
    }
}
