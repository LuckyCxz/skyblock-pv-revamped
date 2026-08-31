package me.owdding.skyblockpv.screens.windowed.tabs.base

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.extensions.floorToHalf
import me.owdding.lib.layouts.Scalable
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.utils.PvPageState
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack
import kotlin.math.min

abstract class AbstractCategorizedScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseWindowedPvScreen(name, gameProfile, profile) {

    abstract val categories: List<Category>

    abstract fun getLayout(bg: DisplayWidget): Layout

    final override fun create(bg: DisplayWidget) {
        val layout = getLayout(bg)
        val horizontalDelta = ((uiWidth - 20) / layout.width.toDouble()).floorToHalf()
        val verticalDelta = ((uiHeight - 20) / layout.height.toDouble()).floorToHalf()

        if (Config.displayScaling && horizontalDelta > 1 && verticalDelta > 1) {
            if (SkyBlockPv.isDevMode) {
                addRenderableWidget(PvWidgets.text("$verticalDelta x $horizontalDelta").withPosition(0, 100).withSize(100))
            }
            val min = min(horizontalDelta, verticalDelta)
            if (layout is Scalable) {
                layout.scale(min)
            } else {
                layout.arrangeElements()
            }
        } else {
            layout.arrangeElements()
        }
        val content = if (layout.height > uiHeight) layout.asScrollable(uiWidth, uiHeight) else layout
        FrameLayout.centerInRectangle(content, bg.x, bg.y, uiWidth, uiHeight)
        content.visitWidgets(this::addRenderableWidget)


    }

    override fun toTabState(): PvPageState {
        return categories.find { it.isSelected } ?: super.toTabState()
    }
}

interface Category : PvPageState {

    val icon: ItemStack get() = ItemStack.EMPTY
    val isSelected: Boolean get() = false
    val hover: String get() = ""
    val hideOnStranded: Boolean get() = false

    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): BaseWindowedPvScreen

    override fun canDisplay(profile: SkyBlockProfile?): Boolean {
        return !this.hideOnStranded || profile?.onStranded != true
    }

    companion object {
        inline fun <reified T> getCategories(profile: SkyBlockProfile?): List<T> where T : Enum<T>, T : Category {
            return T::class.java.enumConstants.filter { it.canDisplay(profile) }
        }

        inline fun <reified T> getTabState(profile: SkyBlockProfile?): TriState where T : Enum<T>, T : Category {
            val visibleDisplays = getCategories<T>(profile)
            val expectedDisplays = T::class.java.enumConstants.count { !it.hideOnStranded || profile?.onStranded != true }

            return when {
                visibleDisplays.isEmpty() -> TriState.FALSE
                visibleDisplays.size == expectedDisplays -> TriState.TRUE
                else -> TriState.DEFAULT
            }
        }
    }
}
