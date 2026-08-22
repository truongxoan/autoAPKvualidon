package com.antigravity.keymapper.storage

import android.content.Context
import android.content.SharedPreferences
import com.antigravity.keymapper.model.KeymapProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class KeymapStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("keymapper_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val KEY_PROFILES_PREFIX = "profile_"
        private const val KEY_SENS_X = "global_sens_x"
        private const val KEY_SENS_Y = "global_sens_y"
        private const val KEY_GLOBAL_SENS = "global_sensitivity"
        private const val KEY_BUTTON_SCALE = "button_scale"
        private const val KEY_BUTTON_OPACITY = "button_opacity"
        private const val KEY_TARGET_GAMES = "target_games_list"
    }

    fun getTargetGames(): Set<String> {
        val defaultGames = setOf(
            "com.dts.freefireth",
            "com.dts.freefiremax",
            "com.vng.pubgmobile",
            "com.tencent.ig",
            "com.garena.game.kgvn",
            "com.vng.codmvn",
            "com.riotgames.league.wildriftvn"
        )
        return prefs.getStringSet(KEY_TARGET_GAMES, defaultGames) ?: defaultGames
    }

    fun setTargetGames(games: Set<String>) {
        prefs.edit().putStringSet(KEY_TARGET_GAMES, games).apply()
    }

    fun addTargetGame(packageName: String) {
        val current = getTargetGames().toMutableSet()
        current.add(packageName)
        setTargetGames(current)
    }

    fun removeTargetGame(packageName: String) {
        val current = getTargetGames().toMutableSet()
        current.remove(packageName)
        setTargetGames(current)
    }

    fun isGameTarget(packageName: String): Boolean {
        return getTargetGames().contains(packageName)
    }

    fun getActiveProfileId(): String {
        return prefs.getString(KEY_ACTIVE_PROFILE_ID, "fps_default") ?: "fps_default"
    }

    fun setActiveProfileId(id: String) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, id).apply()
    }

    fun loadProfile(id: String): KeymapProfile {
        val json = prefs.getString(KEY_PROFILES_PREFIX + id, null)
        if (json != null) {
            try {
                return gson.fromJson(json, KeymapProfile::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Defaults
        return when (id) {
            "moba_default" -> KeymapProfile.createMobaDefault()
            "custom" -> KeymapProfile("custom", "Tùy Chỉnh Cá Nhân")
            else -> KeymapProfile.createFpsDefault()
        }
    }

    fun saveProfile(profile: KeymapProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString(KEY_PROFILES_PREFIX + profile.id, json).apply()
    }

    fun getSensitivityX(): Float = prefs.getFloat(KEY_SENS_X, 1.0f)
    fun setSensitivityX(v: Float) = prefs.edit().putFloat(KEY_SENS_X, v).apply()

    fun getSensitivityY(): Float = prefs.getFloat(KEY_SENS_Y, 1.0f)
    fun setSensitivityY(v: Float) = prefs.edit().putFloat(KEY_SENS_Y, v).apply()

    fun getGlobalSensitivity(): Float = prefs.getFloat(KEY_GLOBAL_SENS, 1.0f)
    fun setGlobalSensitivity(v: Float) = prefs.edit().putFloat(KEY_GLOBAL_SENS, v).apply()

    fun getButtonScale(): Float = prefs.getFloat(KEY_BUTTON_SCALE, 1.0f)
    fun setButtonScale(v: Float) = prefs.edit().putFloat(KEY_BUTTON_SCALE, v).apply()

    fun getButtonOpacity(): Float = prefs.getFloat(KEY_BUTTON_OPACITY, 0.45f)
    fun setButtonOpacity(v: Float) = prefs.edit().putFloat(KEY_BUTTON_OPACITY, v).apply()

    /**
     * Export all keymaps and profiles to a local JSON file
     */
    fun exportToJsonFile(targetFile: java.io.File): Boolean {
        return try {
            val allMap = mutableMapOf<String, Any>()
            allMap["activeProfileId"] = getActiveProfileId()
            allMap["fps_default"] = loadProfile("fps_default")
            allMap["moba_default"] = loadProfile("moba_default")
            allMap["custom"] = loadProfile("custom")
            allMap["sensX"] = getSensitivityX()
            allMap["sensY"] = getSensitivityY()

            targetFile.parentFile?.mkdirs()
            targetFile.writeText(gson.toJson(allMap))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Import keymaps and profiles from a local JSON file
     */
    fun importFromJsonFile(sourceFile: java.io.File): Boolean {
        return try {
            if (!sourceFile.exists()) return false
            val text = sourceFile.readText()
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(text, mapType)

            if (data.containsKey("fps_default")) {
                val fpsProfile = gson.fromJson(gson.toJson(data["fps_default"]), KeymapProfile::class.java)
                saveProfile(fpsProfile)
            }
            if (data.containsKey("moba_default")) {
                val mobaProfile = gson.fromJson(gson.toJson(data["moba_default"]), KeymapProfile::class.java)
                saveProfile(mobaProfile)
            }
            if (data.containsKey("custom")) {
                val customProfile = gson.fromJson(gson.toJson(data["custom"]), KeymapProfile::class.java)
                saveProfile(customProfile)
            }
            if (data.containsKey("activeProfileId")) {
                setActiveProfileId(data["activeProfileId"].toString())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
