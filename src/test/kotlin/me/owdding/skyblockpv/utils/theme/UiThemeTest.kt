package me.owdding.skyblockpv.utils.theme

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import kotlin.test.Test
import kotlin.test.BeforeTest
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiThemeTest {
    @BeforeTest fun bootstrapMinecraftCodecs() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test fun legacyThemeStillDecodes() {
        val theme = PvTheme.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(
            """{"name":"legacy","colors":{"gold":"#123456"},"background_blur":false}"""
        )).getOrThrow()
        assertEquals(0x123456, theme.colors.gold and 0xFFFFFF)
        assertFalse(theme.backgroundBlur)
        assertEquals(UiTheme(), theme.ui)
    }

    @Test fun presetsAndEffectsRoundTrip() {
        val codec = SkyBlockPvCodecs.UiThemeCodec.codec()
        UiTheme.presets.values.forEach { preset ->
            val theme = preset.copy(effects = preset.effects.copy(chroma = true, panelOpacity = 42))
            val encoded = codec.encodeStart(JsonOps.INSTANCE, theme).getOrThrow()
            val decoded = codec.parse(JsonOps.INSTANCE, encoded).getOrThrow()
            // The color codec may normalize the alpha channel; compare serialized forms.
            assertEquals(encoded, codec.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow())
            assertTrue(decoded.chroma)
            assertEquals(42, decoded.panelOpacity)
        }
    }

    @Test fun untrustedEffectValuesAreBounded() {
        val theme = UiTheme(effects = UiEffects(panelOpacity = -50, backgroundOpacity = 900, cornerRadius = 99)).sanitized()
        assertEquals(0, theme.panelOpacity)
        assertEquals(100, theme.backgroundOpacity)
        assertEquals(12, theme.cornerRadius)
    }

    @Test fun standardColorThemesAreAvailableWithoutCustomInput() {
        val names = listOf("Red", "Green", "Blue", "Cyan", "Orange", "Pink", "Purple")
        assertTrue(UiTheme.presets.keys.containsAll(names))
        assertEquals(names.size, names.map { UiTheme.presets.getValue(it).primary }.distinct().size)
    }
}
