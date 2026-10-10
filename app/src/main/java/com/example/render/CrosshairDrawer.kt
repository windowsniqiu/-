package com.example.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import kotlin.math.cos
import kotlin.math.sin

/**
 * 统一的高性能准星绘制引擎
 * 采用原生 Android Canvas 与 Paint，零 GC 开销，内存占用极低
 */
object CrosshairDrawer {

    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val path = Path()

    /**
     * 绘制准星
     * @param canvas 画布
     * @param cx 准星中心点 X 坐标
     * @param cy 准星中心点 Y 坐标
     * @param density 屏幕密度，用于把 dp 转为 px
     * @param config 准星配置
     */
    @Synchronized
    fun draw(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        density: Float,
        config: CrosshairConfig
    ) {
        val sizePx = config.sizeDp * density
        val strokeWidthPx = config.strokeWidthDp * density
        val gapPx = config.gapDp * density
        val outlineWidthPx = config.outlineWidthDp * density
        val dotSizePx = config.centerDotSizeDp * density

        val alphaInt = (config.alpha.coerceIn(0.05f, 1.0f) * 255).toInt()
        val baseColor = config.color.toInt()
        val mainColor = Color.argb(
            alphaInt,
            Color.red(baseColor),
            Color.green(baseColor),
            Color.blue(baseColor)
        )
        val outlineBaseColor = config.outlineColor.toInt()
        val outlineColor = Color.argb(
            alphaInt,
            Color.red(outlineBaseColor),
            Color.green(outlineBaseColor),
            Color.blue(outlineBaseColor)
        )

        mainPaint.color = mainColor
        mainPaint.strokeWidth = strokeWidthPx

        outlinePaint.color = outlineColor
        outlinePaint.strokeWidth = strokeWidthPx + 2 * outlineWidthPx

        dotPaint.color = mainColor
        dotOutlinePaint.color = outlineColor

        canvas.save()
        if (config.rotationDeg != 0f) {
            canvas.rotate(config.rotationDeg, cx, cy)
        }

        val halfSize = sizePx / 2f

        when (config.style) {
            CrosshairStyle.CLASSIC_CROSS -> {
                drawCross(canvas, cx, cy, halfSize, gapPx, config.hasOutline)
            }
            CrosshairStyle.CENTER_DOT -> {
                // 实心圆点直接由中心点逻辑绘制
            }
            CrosshairStyle.HALO_CIRCLE -> {
                drawCircle(canvas, cx, cy, halfSize, config.hasOutline)
            }
            CrosshairStyle.CIRCLE_DOT -> {
                drawCircle(canvas, cx, cy, halfSize, config.hasOutline)
            }
            CrosshairStyle.TACTICAL_T -> {
                drawTacticalT(canvas, cx, cy, halfSize, gapPx, config.hasOutline)
            }
            CrosshairStyle.CORNER_BOX -> {
                drawCornerBox(canvas, cx, cy, halfSize, gapPx, config.hasOutline)
            }
            CrosshairStyle.TRI_CHEVRON -> {
                drawTriChevron(canvas, cx, cy, halfSize, gapPx, config.hasOutline)
            }
            CrosshairStyle.ROTATED_X -> {
                drawRotatedX(canvas, cx, cy, halfSize, gapPx, config.hasOutline)
            }
            CrosshairStyle.SNIPER_TICKS -> {
                drawSniperTicks(canvas, cx, cy, halfSize, gapPx, config.hasOutline, density)
            }
            CrosshairStyle.DUAL_RING -> {
                drawCircle(canvas, cx, cy, halfSize, config.hasOutline)
                drawCircle(canvas, cx, cy, halfSize * 0.45f, config.hasOutline)
            }
            CrosshairStyle.DIAMOND -> {
                drawDiamond(canvas, cx, cy, halfSize, config.hasOutline)
            }
            CrosshairStyle.HOLLOW_BOX -> {
                drawHollowBox(canvas, cx, cy, halfSize * 0.7f, config.hasOutline)
            }
            CrosshairStyle.APEX_CHEVRON -> {
                drawApexChevron(canvas, cx, cy, halfSize, gapPx, config.hasOutline)
            }
            CrosshairStyle.MICRO_PIN -> {
                drawMicroPin(canvas, cx, cy, halfSize, density, config.hasOutline)
            }
        }

        // 绘制中心瞄点
        if (config.hasCenterDot || config.style == CrosshairStyle.CENTER_DOT || config.style == CrosshairStyle.CIRCLE_DOT) {
            val radius = dotSizePx / 2f
            if (config.hasOutline) {
                canvas.drawCircle(cx, cy, radius + outlineWidthPx, dotOutlinePaint)
            }
            canvas.drawCircle(cx, cy, radius, dotPaint)
        }

        canvas.restore()
    }

    private fun drawCross(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        gap: Float,
        hasOutline: Boolean
    ) {
        val g = gap.coerceAtLeast(0f)
        val r = radius.coerceAtLeast(g + 2f)

        fun renderLines(p: Paint) {
            // 左
            canvas.drawLine(cx - r, cy, cx - g, cy, p)
            // 右
            canvas.drawLine(cx + g, cy, cx + r, cy, p)
            // 上
            canvas.drawLine(cx, cy - r, cx, cy - g, p)
            // 下
            canvas.drawLine(cx, cy + g, cx, cy + r, p)
        }

        if (hasOutline) renderLines(outlinePaint)
        renderLines(mainPaint)
    }

    private fun drawTacticalT(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        gap: Float,
        hasOutline: Boolean
    ) {
        val g = gap.coerceAtLeast(0f)
        val r = radius.coerceAtLeast(g + 2f)

        fun renderLines(p: Paint) {
            // 左
            canvas.drawLine(cx - r, cy, cx - g, cy, p)
            // 右
            canvas.drawLine(cx + g, cy, cx + r, cy, p)
            // 下（保留下方指示，上方不画线，扩大爆头视野）
            canvas.drawLine(cx, cy + g, cx, cy + r, p)
        }

        if (hasOutline) renderLines(outlinePaint)
        renderLines(mainPaint)
    }

    private fun drawCircle(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        hasOutline: Boolean
    ) {
        if (hasOutline) {
            canvas.drawCircle(cx, cy, radius, outlinePaint)
        }
        canvas.drawCircle(cx, cy, radius, mainPaint)
    }

    private fun drawRotatedX(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        gap: Float,
        hasOutline: Boolean
    ) {
        val g = (gap * 0.707f).coerceAtLeast(0f)
        val r = (radius * 0.707f).coerceAtLeast(g + 2f)

        fun renderLines(p: Paint) {
            canvas.drawLine(cx - r, cy - r, cx - g, cy - g, p)
            canvas.drawLine(cx + g, cy + g, cx + r, cy + r, p)
            canvas.drawLine(cx + r, cy - r, cx + g, cy - g, p)
            canvas.drawLine(cx - g, cy + g, cx - r, cy + r, p)
        }

        if (hasOutline) renderLines(outlinePaint)
        renderLines(mainPaint)
    }

    private fun drawCornerBox(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        gap: Float,
        hasOutline: Boolean
    ) {
        val cornerLen = (radius * 0.45f).coerceAtLeast(4f)
        val r = radius

        fun renderCorners(p: Paint) {
            // 左上角
            canvas.drawLine(cx - r, cy - r, cx - r + cornerLen, cy - r, p)
            canvas.drawLine(cx - r, cy - r, cx - r, cy - r + cornerLen, p)
            // 右上角
            canvas.drawLine(cx + r, cy - r, cx + r - cornerLen, cy - r, p)
            canvas.drawLine(cx + r, cy - r, cx + r, cy - r + cornerLen, p)
            // 左下角
            canvas.drawLine(cx - r, cy + r, cx - r + cornerLen, cy + r, p)
            canvas.drawLine(cx - r, cy + r, cx - r, cy + r - cornerLen, p)
            // 右下角
            canvas.drawLine(cx + r, cy + r, cx + r - cornerLen, cy + r, p)
            canvas.drawLine(cx + r, cy + r, cx + r, cy + r - cornerLen, p)
        }

        if (hasOutline) renderCorners(outlinePaint)
        renderCorners(mainPaint)
    }

    private fun drawTriChevron(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        gap: Float,
        hasOutline: Boolean
    ) {
        val angles = doubleArrayOf(90.0, 210.0, 330.0) // 下方、左上方、右上方

        fun renderLines(p: Paint) {
            for (deg in angles) {
                val rad = Math.toRadians(deg)
                val cosV = cos(rad).toFloat()
                val sinV = sin(rad).toFloat()
                val x1 = cx + gap * cosV
                val y1 = cy + gap * sinV
                val x2 = cx + radius * cosV
                val y2 = cy + radius * sinV
                canvas.drawLine(x1, y1, x2, y2, p)
            }
        }

        if (hasOutline) renderLines(outlinePaint)
        renderLines(mainPaint)
    }

    private fun drawSniperTicks(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        gap: Float,
        hasOutline: Boolean,
        density: Float
    ) {
        val g = gap.coerceAtLeast(0f)
        val r = radius.coerceAtLeast(g + 2f)
        val tickHalf = 3.5f * density

        fun renderLines(p: Paint) {
            // 主十字
            canvas.drawLine(cx - r, cy, cx - g, cy, p)
            canvas.drawLine(cx + g, cy, cx + r, cy, p)
            canvas.drawLine(cx, cy - r, cx, cy - g, p)
            canvas.drawLine(cx, cy + g, cx, cy + r, p)

            // 下方密位刻度线（3道辅助横线）
            val step = (r - g) / 4f
            for (i in 1..3) {
                val ty = cy + g + step * i
                canvas.drawLine(cx - tickHalf, ty, cx + tickHalf, ty, p)
            }
        }

        if (hasOutline) renderLines(outlinePaint)
        renderLines(mainPaint)
    }

    private fun drawDiamond(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        hasOutline: Boolean
    ) {
        path.reset()
        path.moveTo(cx, cy - radius)
        path.lineTo(cx + radius, cy)
        path.lineTo(cx, cy + radius)
        path.lineTo(cx - radius, cy)
        path.close()

        if (hasOutline) canvas.drawPath(path, outlinePaint)
        canvas.drawPath(path, mainPaint)
    }

    private fun drawHollowBox(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        halfSide: Float,
        hasOutline: Boolean
    ) {
        val left = cx - halfSide
        val top = cy - halfSide
        val right = cx + halfSide
        val bottom = cy + halfSide

        if (hasOutline) {
            canvas.drawRect(left, top, right, bottom, outlinePaint)
        }
        canvas.drawRect(left, top, right, bottom, mainPaint)
    }

    private fun drawApexChevron(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        gap: Float,
        hasOutline: Boolean
    ) {
        val topY = cy - radius
        val bottomY = cy - gap
        val wingX = radius * 0.7f

        path.reset()
        // 倒V型尖头
        path.moveTo(cx - wingX, bottomY)
        path.lineTo(cx, topY)
        path.lineTo(cx + wingX, bottomY)

        if (hasOutline) {
            canvas.drawPath(path, outlinePaint)
            // 下方垂直指示线
            canvas.drawLine(cx, cy + gap, cx, cy + radius * 0.8f, outlinePaint)
        }
        canvas.drawPath(path, mainPaint)
        canvas.drawLine(cx, cy + gap, cx, cy + radius * 0.8f, mainPaint)
    }

    private fun drawMicroPin(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        density: Float,
        hasOutline: Boolean
    ) {
        val microGap = 4f * density
        val microLen = 8f * density

        fun renderMicro(p: Paint) {
            canvas.drawLine(cx - microGap - microLen, cy, cx - microGap, cy, p)
            canvas.drawLine(cx + microGap, cy, cx + microGap + microLen, cy, p)
            canvas.drawLine(cx, cy - microGap - microLen, cx, cy - microGap, p)
            canvas.drawLine(cx, cy + microGap, cx, cy + microGap + microLen, p)
        }

        if (hasOutline) renderMicro(outlinePaint)
        renderMicro(mainPaint)
    }
}
