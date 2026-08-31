package me.owdding.skyblockpv.utils.theme

import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.accessor.WidgetSpritesAccessor
import me.owdding.skyblockpv.config.Config
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.resources.Identifier
import kotlin.collections.toList
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.SkyBlockPv

object ThemeSupport {

    val currentTheme: PvTheme get() = ThemeHelper.themes[Config.theme] ?: ThemeHelper.fallbackTheme

    val pvColors get() = currentTheme.colors
    val pvTextures get() = currentTheme.textures

    private var cachedOverride = ""
    private var parsedOverride: UiTheme? = null
    val ui: UiTheme
        get() {
            if (cachedOverride != Config.appearanceOverride) {
                cachedOverride = Config.appearanceOverride
                parsedOverride = runCatching {
                    SkyBlockPvCodecs.UiThemeCodec.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(cachedOverride)).getOrThrow().sanitized()
                }.getOrNull()
            }
            return parsedOverride ?: currentTheme.ui.sanitized().let {
                it.copy(effects = it.effects.copy(blur = it.blur && currentTheme.backgroundBlur))
            }
        }

    fun saveUi(theme: UiTheme?) {
        Config.appearanceOverride = theme?.let {
            SkyBlockPvCodecs.UiThemeCodec.codec().encodeStart(JsonOps.INSTANCE, it.sanitized()).getOrThrow().toString()
        } ?: ""
        SkyBlockPv.config.save()
    }

    fun nextTheme() {
        val themes = ThemeHelper.themes.keys.toList()
        if (themes.isEmpty()) return
        Config.theme = themes[(themes.indexOf(Config.theme) + 1) % themes.size]
    }

    fun texture(path: String) = texture(id(path))
    fun texture(path: Identifier) = pvTextures.getOrDefault(path, path)

    fun WidgetSprites.withThemeSupport(): WidgetSprites = also { WidgetSpritesAccessor.withThemeSupport(it) }

    fun ThemedWidgetSprites(
        enabled: Identifier,
        disabled: Identifier,
        enabledFocused: Identifier,
        disabledFocused: Identifier = disabled,
    ) = WidgetSprites(enabled, disabled, enabledFocused, disabledFocused).withThemeSupport()

}
