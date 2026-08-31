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
            "Red" to UiTheme(background = 0x201316, sidebar = 0x2A191D, surface = 0x382126,
                surfaceAlt = 0x492B31, primary = 0xFF8585, secondary = 0xFFB4A3,
                border = 0x674047, hover = 0x593139, selected = 0x703B44),
            "Green" to UiTheme(background = 0x101D17, sidebar = 0x17271E, surface = 0x20372A,
                surfaceAlt = 0x294735, primary = 0x80E5A5, secondary = 0xB7E58A,
                border = 0x3C6049, hover = 0x31553E, selected = 0x3C6B4C),
            "Blue" to UiTheme(),
            "Cyan" to UiTheme(background = 0x101D22, sidebar = 0x162931, surface = 0x203841,
                surfaceAlt = 0x284651, primary = 0x7CE4EF, secondary = 0x9CBFFF,
                border = 0x3D606A, hover = 0x305561, selected = 0x396B78),
            "Orange" to UiTheme(background = 0x211810, sidebar = 0x2D2117, surface = 0x3D2D20,
                surfaceAlt = 0x4C3827, primary = 0xFFB16E, secondary = 0xFFDA89,
                border = 0x6B5139, hover = 0x604229, selected = 0x765034),
            "Pink" to UiTheme(background = 0x211420, sidebar = 0x2D1C2B, surface = 0x3F283B,
                surfaceAlt = 0x50334A, primary = 0xFFA0D5, secondary = 0xD5B2FF,
                border = 0x6B4561, hover = 0x603C55, selected = 0x77476B),
            "Purple" to UiTheme(background = 0x191323, sidebar = 0x221B30, surface = 0x30243F,
                surfaceAlt = 0x3B2F50, primary = 0xC39AFF, secondary = 0xFFAACD,
                border = 0x574568, hover = 0x503966, selected = 0x65477D),
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
