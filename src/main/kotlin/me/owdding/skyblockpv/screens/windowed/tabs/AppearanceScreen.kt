package me.owdding.skyblockpv.screens.windowed.tabs

import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.serialization.JsonOps
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.asWidget
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import me.owdding.skyblockpv.utils.theme.UiTheme
import me.owdding.skyblockpv.utils.theme.UiWidgets
import tech.thatgravyboat.skyblockapi.utils.text.Text

/** Edits are staged until Apply; invalid input never replaces the saved theme. */
class AppearanceScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseWindowedPvScreen("APPEARANCE", gameProfile, profile) {
    override val tab = PvTab.APPEARANCE
    private var draft = ThemeSupport.ui
    private val inputs = linkedMapOf<String, String>()
    private var message = "Colors: #RRGGBB. Changes are saved with Apply."
    private var failed = false

    private fun button(label: String, action: () -> Unit) = Button().withSize(((uiWidth - 22) / 2).coerceIn(70, 100), 22).withTexture(null)
        .withRenderer(UiWidgets.navigation(Text.of(label))).withCallback { action() }.withTooltip(Text.of(label))

    override fun create(bg: DisplayWidget) {
        val fields = linkedMapOf(
            "background" to draft.background, "sidebar" to draft.sidebar,
            "surface" to draft.surface, "surfaceAlt" to draft.surfaceAlt,
            "primary" to draft.primary, "secondary" to draft.secondary,
            "text" to draft.text, "muted" to draft.muted, "border" to draft.border,
            "hover" to draft.hover, "selected" to draft.selected,
            "positive" to draft.positive, "warning" to draft.warning, "negative" to draft.negative,
        )
        fields.forEach { (key, color) -> inputs.putIfAbsent(key, "#%06X".format(color and 0xFFFFFF)) }
        inputs.putIfAbsent("panelOpacity", draft.panelOpacity.toString())
        inputs.putIfAbsent("backgroundOpacity", draft.backgroundOpacity.toString())
        inputs.putIfAbsent("cornerRadius", draft.cornerRadius.toString())

        PvLayouts.horizontal(5) {
            widget(button("Apply") {
                val json = JsonObject()
                val invalid = inputs.entries.firstOrNull { (key, value) ->
                    if (key in fields) !value.matches(Regex("#[0-9a-fA-F]{6}"))
                    else value.toIntOrNull()?.let { it !in 0..if (key == "cornerRadius") 12 else 100 } ?: true
                }
                if (invalid != null) {
                    message = "Invalid ${invalid.key}: use " + if (invalid.key in fields) "#RRGGBB" else if (invalid.key == "cornerRadius") "0–12" else "0–100"
                    failed = true
                } else {
                    val effects = JsonObject()
                    inputs.forEach { (key, value) ->
                        if (key in fields) json.addProperty(key, value) else effects.addProperty(key, value.toInt())
                    }
                    effects.addProperty("blur", draft.blur)
                    effects.addProperty("chroma", draft.chroma)
                    json.add("effects", effects)
                    val decoded = SkyBlockPvCodecs.UiThemeCodec.codec().parse(JsonOps.INSTANCE, json).result().orElse(null)
                    if (decoded == null) {
                        message = "Unable to read these colors; your saved theme is unchanged."
                        failed = true
                    } else {
                        ThemeSupport.saveUi(decoded)
                        draft = decoded
                        message = "Appearance saved."
                        failed = false
                    }
                }
                safelyRebuild()
            })
            widget(button("Reset to theme") {
                ThemeSupport.saveUi(null)
                draft = ThemeSupport.ui
                inputs.clear()
                message = "Restored the selected resource-pack theme."
                failed = false
                safelyRebuild()
            })
        }.applyLayout(bg.x + 8, bg.y + 6)

        val statusColor = if (failed) ThemeSupport.ui.negative else if (message == "Appearance saved.") ThemeSupport.ui.positive else ThemeSupport.ui.warning
        addRenderableOnly(PvWidgets.text(Text.of(message).withColor(statusColor)).withSize(uiWidth - 16, 20).withPosition(bg.x + 8, bg.y + 32))

        PvLayouts.vertical(6) {
            widget(PvWidgets.text("Presets (select, then Apply)").withSize(uiWidth - 36, 18))
            UiTheme.presets.forEach { (label, theme) ->
                widget(button(label) {
                    draft = theme
                    inputs.clear()
                    message = "$label selected. Apply to save."
                    failed = false
                    safelyRebuild()
                })
            }
            widget(button("Blur: ${if (draft.blur) "On" else "Off"}") { draft = draft.copy(effects = draft.effects.copy(blur = !draft.blur)); safelyRebuild() })
            widget(button("Chroma: ${if (draft.chroma) "On" else "Off"}") { draft = draft.copy(effects = draft.effects.copy(chroma = !draft.chroma)); safelyRebuild() })
            inputs.toMap().forEach { (key, value) ->
                val label = when (key) {
                    "primary" -> "Primary accent"
                    "secondary" -> "Secondary accent"
                    "text" -> "Primary text"
                    "muted" -> "Muted text"
                    else -> key.replace(Regex("([a-z])([A-Z])"), "$1 $2").replaceFirstChar { it.uppercase() }
                }
                val input = Widgets.textInput(ListenableState.of(value)) { box ->
                    box.withChangeCallback { inputs[key] = it }
                }.withTexture(ExtraConstants.TEXTBOX).withSize(90, 20)
                val swatch = UiWidgets.background(14, 14) {
                    inputs[key]?.removePrefix("#")?.toIntOrNull(16) ?: ThemeSupport.ui.negative
                }.asWidget()
                if (uiWidth < 300) vertical(2) {
                    widget(PvWidgets.text(label).withSize(uiWidth - 40, 14))
                    horizontal(6) { widget(input); if (key in fields) widget(swatch) }
                } else horizontal(6) {
                    widget(PvWidgets.text(label).withSize((uiWidth - 170).coerceIn(100, 150), 20))
                    widget(input)
                    if (key in fields) widget(swatch)
                }
            }
        }.asScrollable(uiWidth - 12, uiHeight - 58).applyLayout(bg.x + 6, bg.y + 56)
    }
}
