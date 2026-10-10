package com.example.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import com.example.model.CrosshairConfig
import com.example.render.CrosshairDrawer
import kotlin.math.roundToInt

/**
 * 原生极低内存悬浮窗绘制视图
 */
class CrosshairOverlayView(
    context: Context,
    private val onDragOffsetChanged: (dx: Int, dy: Int) -> Unit,
    private val onDragFinished: () -> Unit
) : View(context) {

    private var config: CrosshairConfig = CrosshairConfig()
    private var isDragMode: Boolean = false

    // 辅助提示绘制
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 0, 229, 255)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 0f, 0f, Color.BLACK)
    }

    private val textBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(160, 20, 25, 30)
        style = Paint.Style.FILL
    }

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    fun updateConfig(newConfig: CrosshairConfig, dragMode: Boolean) {
        this.config = newConfig
        this.isDragMode = dragMode
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val density = resources.displayMetrics.density

        // 绘制准星实体
        CrosshairDrawer.draw(canvas, cx, cy, density, config)

        // 若处于拖动校准模式，绘制外围校准虚线圈与坐标信息
        if (isDragMode) {
            val guideRadius = (config.sizeDp * density * 0.75f).coerceAtLeast(50f * density)
            canvas.drawCircle(cx, cy, guideRadius, guidePaint)

            // 绘制坐标微标签
            val coordText = "X:${config.offsetX}  Y:${config.offsetY}"
            val textY = cy + guideRadius + 36f
            val textWidth = textPaint.measureText(coordText)
            canvas.drawRoundRect(
                cx - textWidth / 2f - 16f,
                textY - 30f,
                cx + textWidth / 2f + 16f,
                textY + 12f,
                12f,
                12f,
                textBgPaint
            )
            canvas.drawText(coordText, cx, textY, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDragMode) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = (event.rawX - initialTouchX).roundToInt()
                    val dy = (event.rawY - initialTouchY).roundToInt()
                    if (dx != 0 || dy != 0) {
                        onDragOffsetChanged(dx, dy)
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    onDragFinished()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
