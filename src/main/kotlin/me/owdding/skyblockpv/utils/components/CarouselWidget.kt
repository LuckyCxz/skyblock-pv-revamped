package me.owdding.skyblockpv.utils.components

import com.mojang.blaze3d.platform.cursor.CursorTypes
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.platform.screens.BaseWidget
import me.owdding.lib.platform.screens.MouseButtonEvent
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.CarouselPageState
import me.owdding.skyblockpv.utils.ExtraWidgetRenderers
import me.owdding.skyblockpv.utils.PvPageState
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutSettings
import kotlin.collections.mapIndexed


class CarouselWidget(
    private val displays: List<Display>,
    var index: Int = 0,
    width: Int,
) : BaseWidget() {

    private val pageHeight = displays.maxOfOrNull(Display::getHeight) ?: 0
    private var pageState: PvPageState? = null

    init {
        // Reserve the complete tallest page plus navigation. Never crop neighboring
        // inventories to the active page's height or draw them outside this widget.
        this.height = pageHeight + if (displays.size > 1) 26 else 0
        this.width = maxOf(width, displays.maxOfOrNull(Display::getWidth) ?: 0)
        this.index = index.coerceIn(0, (displays.size - 1).coerceAtLeast(0))
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val current = displays.getOrNull(index) ?: return
        current.extract(graphics, x + width / 2, y, alignmentX = 0.5f, alignmentY = 0f)
        if (displays.size < 2) return

        val controlsY = y + (displays.getOrNull(index)?.getHeight() ?: 0) + 6
        val buttonWidth = minOf(64, width / 3)
        fun control(left: Int, label: String) {
            val hovered = mouseX in left until left + buttonWidth && mouseY in controlsY until controlsY + 20
            me.owdding.skyblockpv.utils.theme.UiWidgets.panel(graphics, left, controlsY, buttonWidth, 20,
                if (hovered) me.owdding.skyblockpv.utils.theme.ThemeSupport.ui.hover
                else me.owdding.skyblockpv.utils.theme.ThemeSupport.ui.surfaceAlt)
            Displays.text(label, color = { me.owdding.skyblockpv.utils.theme.ThemeSupport.ui.text.toUInt() }, shadow = false)
                .extract(graphics, left + buttonWidth / 2, controlsY + 6, alignmentX = 0.5f)
            if (hovered) graphics.requestCursor(CursorTypes.POINTING_HAND)
        }
        control(x, "< Previous")
        control(x + width - buttonWidth, "Next >")
        Displays.text("${index + 1} / ${displays.size}",
            color = { me.owdding.skyblockpv.utils.theme.ThemeSupport.ui.muted.toUInt() }, shadow = false)
            .extract(graphics, x + width / 2, controlsY + 6, alignmentX = 0.5f)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (displays.size < 2) return
        val (mouseX, mouseY) = event
        val controlsY = y + (displays.getOrNull(index)?.getHeight() ?: 0) + 6
        if (mouseY < controlsY || mouseY >= controlsY + 20) return
        val buttonWidth = minOf(64, width / 3)
        val direction = when {
            mouseX >= x && mouseX < x + buttonWidth -> -1
            mouseX >= x + width - buttonWidth && mouseX < x + width -> 1
            else -> return
        }
        index = Math.floorMod(index + direction, displays.size)
        pageState?.let { Utils.lastTab = CarouselPageState(it, index) }
    }
    fun getIcons(perRow: Int = 9, page: PvPageState, displays: () -> List<Display>): Layout {
        pageState = page
        val buttons = displays.invoke().mapIndexed { index, display ->
            Button()
                .withSize(32, 32)
                .withTexture(null)
                .withRenderer(
                    WidgetRenderers.layered(
                        ExtraWidgetRenderers.conditional(
                            WidgetRenderers.sprite(ExtraConstants.BUTTON_PRIMARY_OPAQUE),
                            WidgetRenderers.sprite(ExtraConstants.BUTTON_DARK_OPAQUE),
                        ) { this.index == index },
                        WidgetRenderers.center(32, 32, WidgetRenderers.padded(4, 4, 4, 4, DisplayWidget.displayRenderer(display))),
                    ),
                ).withCallback {
                    this.index = index
                    Utils.lastTab = CarouselPageState(page, index)
                }
        }

        val windowWidth = tech.thatgravyboat.skyblockapi.helpers.McClient.self.window.guiScaledWidth
        val availableWidth = windowWidth - (if (windowWidth < 500) 108 else 132) - 88
        val columns = minOf(perRow, (availableWidth / 33).coerceAtLeast(1))
        val rows = buttons.chunked(columns).map { PvLayouts.horizontal(1) { widget(it) } }
        return PvLayouts.vertical(1) {
            rows.forEach {
                widget(it, LayoutSettings::alignHorizontallyCenter)
            }
        }
    }
}
