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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrosshairConfig
import com.example.model.CrosshairStyle
import com.example.render.CrosshairDrawer
import kotlin.math.roundToInt

@Composable
fun StyleTab(
    config: CrosshairConfig,
    onUpdateConfig: ((CrosshairConfig) -> CrosshairConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 样式选择卡片
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "准星样式库 (${CrosshairStyle.values().size}款)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "点击切换战术瞄准样式，适配各类枪械手感",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 网格展示样式
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CrosshairStyle.values()) { style ->
                        val isSelected = config.style == style
                        val miniConfig = config.copy(
                            style = style,
                            sizeDp = 24f,
                            gapDp = 4f,
                            strokeWidthDp = 2.0f,
                            centerDotSizeDp = 2.2f,
                            rotationDeg = 0f
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF334155),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateConfig { it.copy(style = style) }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .testTag("style_item_${style.name.lowercase()}")
                        ) {
                            // 小尺寸实时准星图标
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F141A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawContext.canvas.nativeCanvas.let { canvas ->
                                        CrosshairDrawer.draw(
                                            canvas = canvas,
                                            cx = size.width / 2f,
                                            cy = size.height / 2f,
                                            density = density * 0.9f,
                                            config = miniConfig
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = style.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 几何参数微调卡片
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "几何形态微调",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 尺寸调节
                NumericSliderRow(
                    label = "准星尺寸",
                    valueText = "${config.sizeDp.roundToInt()} dp",
                    value = config.sizeDp,
                    range = 12f..90f,
                    step = 1f,
                    onValueChange = { newVal ->
                        onUpdateConfig { it.copy(sizeDp = newVal) }
                    }
                )

                // 线条粗细
                NumericSliderRow(
                    label = "线条粗细",
                    valueText = String.format("%.1f dp", config.strokeWidthDp),
                    value = config.strokeWidthDp,
                    range = 1.0f..7.0f,
                    step = 0.2f,
                    onValueChange = { newVal ->
                        onUpdateConfig { it.copy(strokeWidthDp = newVal) }
                    }
                )

                // 中心空隙
                NumericSliderRow(
                    label = "中心间隙",
                    valueText = "${config.gapDp.roundToInt()} dp",
                    value = config.gapDp,
                    range = 0f..25f,
                    step = 1f,
                    onValueChange = { newVal ->
                        onUpdateConfig { it.copy(gapDp = newVal) }
                    }
                )

                // 旋转角度
                NumericSliderRow(
                    label = "旋转角度",
                    valueText = "${config.rotationDeg.roundToInt()}°",
                    value = config.rotationDeg,
                    range = 0f..180f,
                    step = 5f,
                    onValueChange = { newVal ->
                        onUpdateConfig { it.copy(rotationDeg = newVal) }
                    }
                )

                // 中心点开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "中心瞄准点",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "在十字或圆环中心增加实心瞄点",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = config.hasCenterDot,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(hasCenterDot = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("switch_center_dot")
                    )
                }

                if (config.hasCenterDot) {
                    NumericSliderRow(
                        label = "瞄点直径",
                        valueText = String.format("%.1f dp", config.centerDotSizeDp),
                        value = config.centerDotSizeDp,
                        range = 1.0f..8.0f,
                        step = 0.5f,
                        onValueChange = { newVal ->
                            onUpdateConfig { it.copy(centerDotSizeDp = newVal) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NumericSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val prev = (value - step).coerceIn(range.start, range.endInclusive)
                        onValueChange(prev)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "减少",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = {
                        val next = (value + step).coerceIn(range.start, range.endInclusive)
                        onValueChange(next)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "增加",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
