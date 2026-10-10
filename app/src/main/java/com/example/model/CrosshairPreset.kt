package com.example.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * 准星预设方案模型
 */
data class CrosshairPreset(
    val id: String,
    val name: String,
    val isBuiltIn: Boolean,
    val config: CrosshairConfig
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("isBuiltIn", isBuiltIn)
        obj.put("config", config.toJson())
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): CrosshairPreset {
            return CrosshairPreset(
                id = obj.getString("id"),
                name = obj.getString("name"),
                isBuiltIn = obj.optBoolean("isBuiltIn", false),
                config = CrosshairConfig.fromJson(obj.getString("config"))
            )
        }

        /**
         * 官方精心调配的内置预设方案
         */
        val BUILT_IN_PRESETS = listOf(
            CrosshairPreset(
                id = "preset_pubg_hip",
                name = "绝地吃鸡·经典绿十字",
                isBuiltIn = true,
                config = CrosshairConfig(
                    style = CrosshairStyle.CLASSIC_CROSS,
                    color = 0xFF00FF66,
                    sizeDp = 34f,
                    strokeWidthDp = 2.5f,
                    gapDp = 6f,
                    hasCenterDot = true,
                    centerDotSizeDp = 2.5f,
                    hasOutline = true
                )
            ),
            CrosshairPreset(
                id = "preset_sniper_red",
                name = "极速狙击·高亮红点",
                isBuiltIn = true,
                config = CrosshairConfig(
                    style = CrosshairStyle.CENTER_DOT,
                    color = 0xFFFF2A4B,
                    sizeDp = 20f,
                    centerDotSizeDp = 4.5f,
                    hasCenterDot = true,
                    hasOutline = true
                )
            ),
            CrosshairPreset(
                id = "preset_tactical_t",
                name = "战术竞技·无遮T型",
                isBuiltIn = true,
                config = CrosshairConfig(
                    style = CrosshairStyle.TACTICAL_T,
                    color = 0xFF00E5FF,
                    sizeDp = 36f,
                    strokeWidthDp = 2.2f,
                    gapDp = 5f,
                    hasCenterDot = true,
                    centerDotSizeDp = 2.5f,
                    hasOutline = true
                )
            ),
            CrosshairPreset(
                id = "preset_shotgun_circle",
                name = "近战霰弹·金黄光环",
                isBuiltIn = true,
                config = CrosshairConfig(
                    style = CrosshairStyle.HALO_CIRCLE,
                    color = 0xFFFFDD00,
                    sizeDp = 48f,
                    strokeWidthDp = 2.8f,
                    hasCenterDot = true,
                    centerDotSizeDp = 3.0f,
                    hasOutline = true
                )
            ),
            CrosshairPreset(
                id = "preset_cs_white",
                name = "反恐精英·纯白空心",
                isBuiltIn = true,
                config = CrosshairConfig(
                    style = CrosshairStyle.CLASSIC_CROSS,
                    color = 0xFFFFFFFF,
                    sizeDp = 32f,
                    strokeWidthDp = 2.0f,
                    gapDp = 7f,
                    hasCenterDot = false,
                    hasOutline = true
                )
            ),
            CrosshairPreset(
                id = "preset_apex_chevron",
                name = "突击步枪·烈焰倒V",
                isBuiltIn = true,
                config = CrosshairConfig(
                    style = CrosshairStyle.APEX_CHEVRON,
                    color = 0xFFFF6600,
                    sizeDp = 38f,
                    strokeWidthDp = 2.8f,
                    gapDp = 4f,
                    hasCenterDot = true,
                    centerDotSizeDp = 3.0f,
                    hasOutline = true
                )
            ),
            CrosshairPreset(
                id = "preset_sniper_ticks",
                name = "重狙密位·战术刻度",
                isBuiltIn = true,
                config = CrosshairConfig(
                    style = CrosshairStyle.SNIPER_TICKS,
                    color = 0xFF00FF66,
                    sizeDp = 50f,
                    strokeWidthDp = 2.0f,
                    gapDp = 5f,
                    hasCenterDot = true,
                    centerDotSizeDp = 2.5f,
                    hasOutline = true
                )
            )
        )
    }
}
