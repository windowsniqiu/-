package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CrosshairRepository
import com.example.model.CrosshairConfig
import com.example.model.CrosshairPreset
import com.example.model.CrosshairStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("极简准星", appName)
    }

    @Test
    fun testConfigSerializationAndDeserialization() {
        val original = CrosshairConfig(
            style = CrosshairStyle.TACTICAL_T,
            color = 0xFF00E5FF,
            sizeDp = 42f,
            strokeWidthDp = 3.2f,
            gapDp = 8f,
            alpha = 0.85f,
            rotationDeg = 45f,
            hasCenterDot = true,
            centerDotSizeDp = 3.5f,
            hasOutline = true,
            offsetX = 12,
            offsetY = -8,
            isTouchPassThrough = true
        )

        val json = original.toJson()
        val parsed = CrosshairConfig.fromJson(json)

        assertEquals(original.style, parsed.style)
        assertEquals(original.color, parsed.color)
        assertEquals(original.sizeDp, parsed.sizeDp, 0.01f)
        assertEquals(original.strokeWidthDp, parsed.strokeWidthDp, 0.01f)
        assertEquals(original.gapDp, parsed.gapDp, 0.01f)
        assertEquals(original.alpha, parsed.alpha, 0.01f)
        assertEquals(original.rotationDeg, parsed.rotationDeg, 0.01f)
        assertEquals(original.hasCenterDot, parsed.hasCenterDot)
        assertEquals(original.centerDotSizeDp, parsed.centerDotSizeDp, 0.01f)
        assertEquals(original.hasOutline, parsed.hasOutline)
        assertEquals(original.offsetX, parsed.offsetX)
        assertEquals(original.offsetY, parsed.offsetY)
        assertEquals(original.isTouchPassThrough, parsed.isTouchPassThrough)
    }

    @Test
    fun testRepositoryInitialConfigAndPresets() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = CrosshairRepository.getInstance(context)

        val config = repository.configFlow.value
        assertNotNull(config)

        val presets = repository.presetsFlow.value
        assertTrue(presets.isNotEmpty())

        val pubg = presets.find { it.id == "preset_pubg_hip" }
        assertNotNull(pubg)
        assertEquals("绝地吃鸡·经典绿十字", pubg?.name)
    }
}
