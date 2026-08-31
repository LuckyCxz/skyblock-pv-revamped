package me.owdding.skyblockpv.utils.theme

import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.buttons.Button
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McFont
import java.awt.Color
import kotlin.math.sqrt

object UiWidgets {
    fun alpha(color: Int, opacity: Int = ThemeSupport.ui.panelOpacity): Int =
        ((opacity.coerceIn(0, 100) * 255 / 100) shl 24) or (color and 0xFFFFFF)

    fun accent(secondary: Boolean = false): Int {
        val ui = ThemeSupport.ui
        if (!ui.chroma) return if (secondary) ui.secondary else ui.primary
        val hue = (System.currentTimeMillis() % 12000L) / 12000f
        return Color.HSBtoRGB((hue + if (secondary) 0.2f else 0f) % 1f, 0.5f, 1f)
    }

    // Horizontal strips keep translucent corners from overlapping/darkening.
    fun panel(gr: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, color: Int,
              opacity: Int = ThemeSupport.ui.panelOpacity) {
        if (width <= 0 || height <= 0) return
        val radius = ThemeSupport.ui.cornerRadius.coerceIn(0, minOf(width, height) / 2)
        val argb = alpha(color, opacity)
        val outline = alpha(ThemeSupport.ui.border, opacity)
        fun cap(row: Int) {
            val dy = when {
                row < radius -> radius - row - 0.5
                row >= height - radius -> row - (height - radius) + 0.5
                else -> 0.0
            }
            val inset = if (dy == 0.0) 0 else (radius - sqrt(radius * radius - dy * dy)).toInt()
            val left = x + inset
            val right = x + width - inset
            if (row == 0 || row == height - 1) gr.fill(left, y + row, right, y + row + 1, outline)
            else {
                gr.fill(left, y + row, left + 1, y + row + 1, outline)
                gr.fill(left + 1, y + row, right - 1, y + row + 1, argb)
                gr.fill(right - 1, y + row, right, y + row + 1, outline)
            }
        }
        val capHeight = maxOf(1, radius)
        for (row in 0 until capHeight) cap(row)
        for (row in maxOf(capHeight, height - capHeight) until height) cap(row)
        if (height > capHeight * 2) {
            gr.fill(x, y + capHeight, x + 1, y + height - capHeight, outline)
            gr.fill(x + 1, y + capHeight, x + width - 1, y + height - capHeight, argb)
            gr.fill(x + width - 1, y + capHeight, x + width, y + height - capHeight, outline)
        }
    }

    fun background(width: Int, height: Int, color: () -> Int = { ThemeSupport.ui.surface }): Display = object : Display {
        override fun getWidth() = width
        override fun getHeight() = height
        override fun extract(graphics: GuiGraphicsExtractor) = panel(graphics, 0, 0, width, height, color())
    }

    fun navigation(label: Component, selected: Boolean = false, icon: ItemStack? = null): WidgetRenderer<Button> = WidgetRenderer { gr, ctx, _ ->
        val button = ctx.widget
        val ui = ThemeSupport.ui
        val hovered = button.isHoveredOrFocused
        panel(gr, ctx.x, ctx.y, button.width, button.height,
            if (selected) ui.selected else if (hovered) ui.hover else ui.sidebar)
        if (selected || hovered) gr.fill(ctx.x, ctx.y + 3, ctx.x + 2, ctx.y + button.height - 3, alpha(accent(), 100))
        val textOffset = if (icon != null && !icon.isEmpty) 26 else 7
        if (icon != null && !icon.isEmpty) gr.item(icon, ctx.x + 6, ctx.y + (button.height - 16) / 2)
        Displays.text(McFont.self.plainSubstrByWidth(label.string, button.width - textOffset - 5),
            color = { (if (selected) accent() else ui.text).toUInt() }, shadow = false)
            .extract(gr, ctx.x + textOffset, ctx.y + (button.height - 9) / 2)
    }

}
