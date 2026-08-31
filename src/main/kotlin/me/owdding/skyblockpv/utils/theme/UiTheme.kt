package me.owdding.skyblockpv.utils.theme

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec

/** UI roles are independent of Minecraft's formatting and item-rarity colors. */
@GenerateCodec
data class UiTheme(
    @NamedCodec("theme§color") val background: Int = 0x101522,
    @NamedCodec("theme§color") val sidebar: Int = 0x151C2C,
    @NamedCodec("theme§color") val surface: Int = 0x1C2638,
    @NamedCodec("theme§color") val surfaceAlt: Int = 0x253149,
    @NamedCodec("theme§color") val primary: Int = 0x79AAFF,
    @NamedCodec("theme§color") val secondary: Int = 0xB69CFF,
    @NamedCodec("theme§color") val text: Int = 0xEEF3FF,
    @NamedCodec("theme§color") val muted: Int = 0xA5B3CC,
    @NamedCodec("theme§color") val border: Int = 0x36455F,
    @NamedCodec("theme§color") val hover: Int = 0x304363,
    @NamedCodec("theme§color") val selected: Int = 0x294776,
    @NamedCodec("theme§color") val positive: Int = 0x69D6A3,
    @NamedCodec("theme§color") val warning: Int = 0xF3CB75,
    @NamedCodec("theme§color") val negative: Int = 0xFF8494,
    val effects: UiEffects = UiEffects(),
) {
    val panelOpacity get() = effects.panelOpacity
    val backgroundOpacity get() = effects.backgroundOpacity
    val blur get() = effects.blur
    val cornerRadius get() = effects.cornerRadius
    val chroma get() = effects.chroma
    fun sanitized() = copy(effects = effects.copy(
        panelOpacity = panelOpacity.coerceIn(0, 100), backgroundOpacity = backgroundOpacity.coerceIn(0, 100),
        cornerRadius = cornerRadius.coerceIn(0, 12)))

    companion object {
        val presets: Map<String, UiTheme> = linkedMapOf(
            "Midnight" to UiTheme(),
            "Amethyst" to UiTheme(background = 0x191323, sidebar = 0x221B30, surface = 0x30243F,
                surfaceAlt = 0x3B2F50, primary = 0xC39AFF, secondary = 0xFFAACD,
                border = 0x574568, hover = 0x503966, selected = 0x65477D),
            "High contrast" to UiTheme(background = 0x000000, sidebar = 0x080808, surface = 0x121212,
                surfaceAlt = 0x222222, text = 0xFFFFFF, muted = 0xDDDDDD, border = 0xAAAAAA,
                primary = 0xFFFF66, secondary = 0x66FFFF, hover = 0x444444, selected = 0x333366,
                effects = UiEffects(panelOpacity = 100, backgroundOpacity = 100, blur = false, cornerRadius = 0)),
        )
    }
}

@GenerateCodec
data class UiEffects(
    val panelOpacity: Int = 96,
    val backgroundOpacity: Int = 85,
    val blur: Boolean = true,
    val cornerRadius: Int = 5,
    val chroma: Boolean = false,
)
