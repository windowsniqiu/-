package com.example

import com.example.model.CrosshairPreset
import com.example.model.CrosshairStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBuiltInPresetsLoaded() {
        val presets = CrosshairPreset.BUILT_IN_PRESETS
        assertTrue(presets.isNotEmpty())
        assertEquals(7, presets.size)

        val pubgPreset = presets.find { it.id == "preset_pubg_hip" }
        assertNotNull(pubgPreset)
        assertEquals("绝地吃鸡·经典绿十字", pubgPreset?.name)
        assertTrue(pubgPreset?.isBuiltIn == true)
    }

    @Test
    fun testAllStylesHaveChineseTitles() {
        CrosshairStyle.values().forEach { style ->
            assertTrue(style.title.isNotBlank())
            assertTrue(style.description.isNotBlank())
        }
    }
}
