package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrosshairConfig
import com.example.render.CrosshairDrawer

enum class GameScene(val label: String, val bgColor: Color) {
    TACTICAL_GRID("暗夜战术", Color(0xFF10161D)),
    GRASS_FIELD("绝地草地", Color(0xFF1C3A24)),
    DESERT_WASTE("沙漠荒原", Color(0xFF4C3E29)),
    SNOW_ICE("极地雪原", Color(0xFFD3DFEB)),
    CYBER_NIGHT("霓虹街区", Color(0xFF231336)),
    DEEP_BLACK("极黑纯色", Color(0xFF000000))
}

@Composable
fun CrosshairPreviewBox(
    config: CrosshairConfig,
    isServiceRunning: Boolean,
    onResetCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScene by remember { mutableStateOf(GameScene.TACTICAL_GRID) }
    var isZoomed by remember { mutableStateOf(false) }
    val density = LocalDensity.current.density

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // 顶部工具行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isServiceRunning) Color(0xFF00FF66) else Color(0xFFFF9900))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isServiceRunning) "悬浮窗已开启" else "应用内实时预览",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isServiceRunning) Color(0xFF00FF66) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // 坐标偏移展示
                Text(
                    text = "偏移: [${config.offsetX}, ${config.offsetY}]",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )

                IconButton(
                    onClick = { isZoomed = !isZoomed },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("preview_zoom_button")
                ) {
                    Icon(
                        imageVector = if (isZoomed) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                        contentDescription = "缩放视界",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onResetCenter,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("preview_reset_center_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "居中重置",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 准星靶场预览主区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(selectedScene.bgColor)
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // 背景参考标尺十字准线（浅灰色辅助标线）
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val gridColor = if (selectedScene == GameScene.SNOW_ICE) Color(0x33000000) else Color(0x22FFFFFF)

                // 标尺虚线
                drawLine(
                    color = gridColor,
                    start = Offset(0f, cy),
                    end = Offset(size.width, cy),
                    strokeWidth = 1f
                )
                drawLine(
                    color = gridColor,
                    start = Offset(cx, 0f),
                    end = Offset(cx, size.height),
                    strokeWidth = 1f
                )
            }

            // 准星本体 Canvas 绘制
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("crosshair_preview_canvas")
            ) {
                val effectiveDensity = if (isZoomed) density * 1.6f else density
                val cx = size.width / 2f
                val cy = size.height / 2f

                drawContext.canvas.nativeCanvas.let { nativeCanvas ->
                    CrosshairDrawer.draw(
                        canvas = nativeCanvas,
                        cx = cx,
                        cy = cy,
                        density = effectiveDensity,
                        config = config
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 模拟游戏背景场景切换器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "背景环境:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameScene.values().forEach { scene ->
                    val isSelected = selectedScene == scene
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(scene.bgColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF475569),
                                shape = CircleShape
                            )
                            .clickable { selectedScene = scene }
                            .testTag("scene_${scene.name.lowercase()}")
                    )
                }
            }
        }
    }
}
