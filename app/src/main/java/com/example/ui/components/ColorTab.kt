package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrosshairConfig
import kotlin.math.roundToInt

data class PresetColor(val name: String, val argb: Long)

val PRESET_COLORS = listOf(
    PresetColor("荧光绿", 0xFF00FF66),
    PresetColor("烈焰红", 0xFFFF2A4B),
    PresetColor("电光青", 0xFF00E5FF),
    PresetColor("耀斑黄", 0xFFFFDD00),
    PresetColor("烈阳橙", 0xFFFF6600),
    PresetColor("极光紫", 0xFFDF00FF),
    PresetColor("纯白色", 0xFFFFFFFF),
    PresetColor("暗影黑", 0xFF121212),
    PresetColor("幻彩粉", 0xFFFF66B2)
)

@Composable
fun ColorTab(
    config: CrosshairConfig,
    onUpdateConfig: ((CrosshairConfig) -> CrosshairConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentColor = config.color
    val r = ((currentColor shr 16) and 0xFF).toInt()
    val g = ((currentColor shr 8) and 0xFF).toInt()
    val b = (currentColor and 0xFF).toInt()
    val hexStr = String.format("#%02X%02X%02X", r, g, b)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 预设高对比电竞色卡片
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "竞技高透色彩",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "精选在草地、沙漠、雪地、夜战中高辨识度的防丢色",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PRESET_COLORS.take(5).forEach { preset ->
                        ColorSwatchItem(
                            preset = preset,
                            isSelected = config.color == preset.argb,
                            onClick = {
                                onUpdateConfig { it.copy(color = preset.argb) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PRESET_COLORS.drop(5).forEach { preset ->
                        ColorSwatchItem(
                            preset = preset,
                            isSelected = config.color == preset.argb,
                            onClick = {
                                onUpdateConfig { it.copy(color = preset.argb) }
                            }
                        )
                    }
                    // 占位保持等距
                    Spacer(modifier = Modifier.size(52.dp))
                }
            }
        }

        // RGB 调色板与透明度
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RGB 精准调色",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(currentColor))
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hexStr,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 红色通道 (R)
                RgbSlider(
                    channelName = "红色 (R)",
                    value = r,
                    color = Color.Red,
                    onValueChange = { newR ->
                        val newArgb = (0xFFL shl 24) or (newR.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
                        onUpdateConfig { it.copy(color = newArgb) }
                    }
                )

                // 绿色通道 (G)
                RgbSlider(
                    channelName = "绿色 (G)",
                    value = g,
                    color = Color.Green,
                    onValueChange = { newG ->
                        val newArgb = (0xFFL shl 24) or (r.toLong() shl 16) or (newG.toLong() shl 8) or b.toLong()
                        onUpdateConfig { it.copy(color = newArgb) }
                    }
                )

                // 蓝色通道 (B)
                RgbSlider(
                    channelName = "蓝色 (B)",
                    value = b,
                    color = Color(0xFF00B0FF),
                    onValueChange = { newB ->
                        val newArgb = (0xFFL shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or newB.toLong()
                        onUpdateConfig { it.copy(color = newArgb) }
                    }
                )

                // 不透明度 (Alpha)
                NumericSliderRow(
                    label = "不透明度",
                    valueText = "${(config.alpha * 100).roundToInt()}%",
                    value = config.alpha,
                    range = 0.1f..1.0f,
                    step = 0.05f,
                    onValueChange = { newAlpha ->
                        onUpdateConfig { it.copy(alpha = newAlpha) }
                    }
                )
            }
        }

        // 高反差黑色描边卡片
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "高对比反差描边",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "在准星外轮廓添加黑边，彻底解决在强光/白昼/雪景中失焦隐形的问题",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = config.hasOutline,
                        onCheckedChange = { checked ->
                            onUpdateConfig { it.copy(hasOutline = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("switch_outline")
                    )
                }

                if (config.hasOutline) {
                    NumericSliderRow(
                        label = "描边粗细",
                        valueText = String.format("%.1f dp", config.outlineWidthDp),
                        value = config.outlineWidthDp,
                        range = 0.5f..3.0f,
                        step = 0.5f,
                        onValueChange = { newWidth ->
                            onUpdateConfig { it.copy(outlineWidthDp = newWidth) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatchItem(
    preset: PresetColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(preset.argb))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF475569),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选",
                    tint = if (preset.name == "纯白色" || preset.name == "耀斑黄") Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = preset.name,
            fontSize = 11.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RgbSlider(
    channelName: String,
    value: Int,
    color: Color,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = channelName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
