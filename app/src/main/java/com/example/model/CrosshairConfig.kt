package com.example.model

import org.json.JSONObject

/**
 * 准星配置数据模型
 */
data class CrosshairConfig(
    val style: CrosshairStyle = CrosshairStyle.CLASSIC_CROSS,
    val color: Long = 0xFF00FF66, // 荧光绿 (ARGB)
    val sizeDp: Float = 36f,      // 准星总尺寸 (dp)
    val strokeWidthDp: Float = 2.5f, // 线条粗细 (dp)
    val gapDp: Float = 6f,        // 中心空隙距离 (dp)
    val alpha: Float = 1.0f,      // 不透明度 0.1f ~ 1.0f
    val rotationDeg: Float = 0f,  // 旋转角度 0 ~ 180度
    val hasCenterDot: Boolean = true, // 是否显示中心点
    val centerDotSizeDp: Float = 3.0f, // 中心点尺寸 (dp)
    val hasOutline: Boolean = true, // 是否有高对比黑色轮廓
    val outlineColor: Long = 0xFF000000, // 轮廓颜色 (ARGB)
    val outlineWidthDp: Float = 1.0f, // 轮廓线粗细 (dp)
    val offsetX: Int = 0,         // X轴像素偏移（以屏幕中心为0）
    val offsetY: Int = 0,         // Y轴像素偏移（以屏幕中心为0）
    val isTouchPassThrough: Boolean = true, // 是否穿透点击 (true=不拦截手势，游戏中正常射击)
    val isLockPosition: Boolean = false,    // 是否锁定位置
    val isHapticEnabled: Boolean = true     // 微调时是否震动反馈
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("style", style.name)
        obj.put("color", color)
        obj.put("sizeDp", sizeDp.toDouble())
        obj.put("strokeWidthDp", strokeWidthDp.toDouble())
        obj.put("gapDp", gapDp.toDouble())
        obj.put("alpha", alpha.toDouble())
        obj.put("rotationDeg", rotationDeg.toDouble())
        obj.put("hasCenterDot", hasCenterDot)
        obj.put("centerDotSizeDp", centerDotSizeDp.toDouble())
        obj.put("hasOutline", hasOutline)
        obj.put("outlineColor", outlineColor)
        obj.put("outlineWidthDp", outlineWidthDp.toDouble())
        obj.put("offsetX", offsetX)
        obj.put("offsetY", offsetY)
        obj.put("isTouchPassThrough", isTouchPassThrough)
        obj.put("isLockPosition", isLockPosition)
        obj.put("isHapticEnabled", isHapticEnabled)
        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): CrosshairConfig {
            return try {
                val obj = JSONObject(jsonStr)
                CrosshairConfig(
                    style = CrosshairStyle.valueOf(obj.optString("style", CrosshairStyle.CLASSIC_CROSS.name)),
                    color = obj.optLong("color", 0xFF00FF66),
                    sizeDp = obj.optDouble("sizeDp", 36.0).toFloat(),
                    strokeWidthDp = obj.optDouble("strokeWidthDp", 2.5).toFloat(),
                    gapDp = obj.optDouble("gapDp", 6.0).toFloat(),
                    alpha = obj.optDouble("alpha", 1.0).toFloat(),
                    rotationDeg = obj.optDouble("rotationDeg", 0.0).toFloat(),
                    hasCenterDot = obj.optBoolean("hasCenterDot", true),
                    centerDotSizeDp = obj.optDouble("centerDotSizeDp", 3.0).toFloat(),
                    hasOutline = obj.optBoolean("hasOutline", true),
                    outlineColor = obj.optLong("outlineColor", 0xFF000000),
                    outlineWidthDp = obj.optDouble("outlineWidthDp", 1.0).toFloat(),
                    offsetX = obj.optInt("offsetX", 0),
                    offsetY = obj.optInt("offsetY", 0),
                    isTouchPassThrough = obj.optBoolean("isTouchPassThrough", true),
                    isLockPosition = obj.optBoolean("isLockPosition", false),
                    isHapticEnabled = obj.optBoolean("isHapticEnabled", true)
                )
            } catch (e: Exception) {
                CrosshairConfig()
            }
        }
    }
}
