package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CrosshairConfig
import com.example.model.CrosshairPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * 准星配置持久化与全局状态管理仓库
 * 采用轻量 SharedPreferences，无数据库守护线程，极低内存占用
 */
class CrosshairRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<CrosshairConfig> = _configFlow.asStateFlow()

    private val _presetsFlow = MutableStateFlow(loadPresets())
    val presetsFlow: StateFlow<List<CrosshairPreset>> = _presetsFlow.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _isDragCalibrationMode = MutableStateFlow(false)
    val isDragCalibrationMode: StateFlow<Boolean> = _isDragCalibrationMode.asStateFlow()

    private fun loadConfig(): CrosshairConfig {
        val json = prefs.getString(KEY_CONFIG, null)
        return if (!json.isNullOrBlank()) {
            CrosshairConfig.fromJson(json)
        } else {
            CrosshairConfig()
        }
    }

    private fun loadPresets(): List<CrosshairPreset> {
        val result = mutableListOf<CrosshairPreset>()
        // 加入内置官方方案
        result.addAll(CrosshairPreset.BUILT_IN_PRESETS)

        // 加入用户自定义方案
        val customJson = prefs.getString(KEY_CUSTOM_PRESETS, null)
        if (!customJson.isNullOrBlank()) {
            try {
                val array = JSONArray(customJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(CrosshairPreset.fromJson(obj))
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
        return result
    }

    fun updateConfig(transform: (CrosshairConfig) -> CrosshairConfig) {
        val newConfig = transform(_configFlow.value)
        _configFlow.value = newConfig
        prefs.edit().putString(KEY_CONFIG, newConfig.toJson()).apply()
    }

    fun setConfig(config: CrosshairConfig) {
        _configFlow.value = config
        prefs.edit().putString(KEY_CONFIG, config.toJson()).apply()
    }

    fun saveCustomPreset(name: String): Boolean {
        if (name.isBlank()) return false
        val newPreset = CrosshairPreset(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            isBuiltIn = false,
            config = _configFlow.value
        )
        val currentList = _presetsFlow.value.toMutableList()
        currentList.add(newPreset)
        _presetsFlow.value = currentList
        persistCustomPresets(currentList)
        return true
    }

    fun deleteCustomPreset(id: String) {
        val currentList = _presetsFlow.value.filter { it.id != id || it.isBuiltIn }
        _presetsFlow.value = currentList
        persistCustomPresets(currentList)
    }

    private fun persistCustomPresets(list: List<CrosshairPreset>) {
        val customList = list.filter { !it.isBuiltIn }
        val array = JSONArray()
        for (item in customList) {
            array.put(item.toJson())
        }
        prefs.edit().putString(KEY_CUSTOM_PRESETS, array.toString()).apply()
    }

    fun resetPosition() {
        updateConfig { it.copy(offsetX = 0, offsetY = 0) }
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun setDragCalibrationMode(isDrag: Boolean) {
        _isDragCalibrationMode.value = isDrag
    }

    companion object {
        private const val PREFS_NAME = "pure_crosshair_prefs"
        private const val KEY_CONFIG = "key_current_config"
        private const val KEY_CUSTOM_PRESETS = "key_custom_presets"

        @Volatile
        private var instance: CrosshairRepository? = null

        fun getInstance(context: Context): CrosshairRepository {
            return instance ?: synchronized(this) {
                instance ?: CrosshairRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
