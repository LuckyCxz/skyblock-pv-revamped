package me.owdding.skyblockpv.screens.windowed.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.skyblockpv.utils.theme.UiWidgets
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import tech.thatgravyboat.skyblockapi.helpers.McClient
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.shorten
import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.SortedEntry
import me.owdding.skyblockpv.data.api.skills.Pet
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class PetScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseWindowedPvScreen("PETS", gameProfile, profile) {

    private var selectedPet: Pet? = profile?.pets?.find { it.active }
    override val tab: PvTab = PvTab.PETS

    private enum class PetSort { RARITY, LEVEL }
    private var sort = PetSort.RARITY

    override fun create(bg: DisplayWidget) {
        val rarityOrder = compareBy<Pet> { SortedEntry.RARITY.list.indexOf(it.tier).let { rank -> if (rank < 0) Int.MAX_VALUE else rank } }
        val ordering = when (sort) {
            PetSort.RARITY -> rarityOrder.thenByDescending { it.level }
            PetSort.LEVEL -> compareByDescending<Pet> { it.level }.then(rarityOrder)
        }.thenByDescending { it.exp }.thenBy { it.type }
        val sortedPets = profile.pets.sortedWith(ordering)
        if (selectedPet !in profile.pets) selectedPet = profile.pets.find { it.active } ?: sortedPets.firstOrNull()
        val stacked = uiWidth < 420
        val detailWidth = if (stacked) uiWidth - 20 else 174
        val gridWidth = if (stacked) uiWidth - 20 else uiWidth - detailWidth - 24

        val content = PvLayouts.vertical(8) {
            horizontal(6) {
                PetSort.entries.forEach { option ->
                    widget(Button().withSize(88, 22).withTexture(null)
                        .withRenderer(UiWidgets.navigation(Text.of("Sort: " + option.name.toTitleCase()), sort == option))
                        .withCallback { sort = option; safelyRebuild() })
                }
            }
            string("${sortedPets.size} pets  /  ${sortedPets.count { it.active }} active")
            if (sortedPets.isEmpty()) {
                string("No pets found for this profile.")
            } else if (stacked) {
                widget(createInfoRow(detailWidth))
                widget(createPetRow(sortedPets, gridWidth))
            } else {
                horizontal(10) {
                    widget(createPetRow(sortedPets, gridWidth).asScrollable(gridWidth, uiHeight - 62))
                    widget(createInfoRow(detailWidth))
                }
            }
        }
        val visible = if (stacked) content.asScrollable(uiWidth, uiHeight) else content
        visible.setPos(bg.x + 8, bg.y + 8).visitWidgets(this::addRenderableWidget)
    }

    private fun createPetRow(pets: List<Pet>, width: Int): Layout {
        val columns = ((width - 20) / 150).coerceAtLeast(1)
        val cardWidth = ((width - 20 - (columns - 1) * 6) / columns).coerceAtLeast(110)
        return PvLayouts.vertical(6) {
            pets.chunked(columns).forEach { row ->
                horizontal(6) { row.forEach { widget(createPetLayout(it, cardWidth)) } }
            }
        }
    }

    private fun createPetLayout(pet: Pet, width: Int): AbstractWidget {
        val icon = PvWidgets.sizedItem(pet.itemStack, 28, null)
        return Button().withSize(width, 48).withTexture(null)
            .withRenderer(WidgetRenderers.layered(
                UiWidgets.navigation(Text.of(""), pet == selectedPet),
                { graphics, context, _ ->
                    icon.extract(graphics, context.x + 6, context.y + 8)
                    val name = McClient.self.font.plainSubstrByWidth(pet.type.toTitleCase(), width - 44)
                    Displays.text(Text.of(name).withColor(pet.rarity.color), shadow = false)
                        .extract(graphics, context.x + 40, context.y + 7)
                    Displays.text("Lv. ${pet.level}", color = { ThemeSupport.ui.text.toUInt() }, shadow = false)
                        .extract(graphics, context.x + 40, context.y + 20)
                    Displays.text(pet.tier.toTitleCase() + if (pet.active) " *" else "",
                        color = { ThemeSupport.ui.muted.toUInt() }, shadow = false)
                        .extract(graphics, context.x + 40, context.y + 33)
                },
            ))
            .withCallback { selectedPet = pet; safelyRebuild() }
    }
    private fun createInfoRow(width: Int) = PvWidgets.label(
        "Selected Pet",
        PvLayouts.vertical(spacing = 2) {
            val colon = ExtraDisplays.grayText(": ")
            val effectiveWidth = width - 20
            fun List<Display>.doesFit() = this.sumOf { it.getWidth() } <= effectiveWidth

            val nameText = ExtraDisplays.grayText("Name")
            val petName = ExtraDisplays.grayText(selectedPet?.type?.toTitleCase() ?: "Unknown")
            if (listOf(nameText, petName, colon).doesFit()) {
                horizontal {
                    display(nameText)
                    display(colon)
                    display(petName)
                }
            } else {
                vertical {
                    spacer(effectiveWidth)
                    display(nameText)
                    indentedDisplay(petName)
                }
            }
            val activePet = selectedPet ?: return@vertical

            string(Text.join("Level: ${activePet.level}"))

            val exp = ExtraDisplays.grayText("Exp: ${activePet.exp.toFormattedString()}")
            if (listOf(exp).doesFit()) {
                display(exp)
            } else {
                display(ExtraDisplays.grayText("Exp: ${activePet.exp.shorten(1)}"))
            }

            if (activePet.progressToMax == 1f) {
                val maxed = ExtraDisplays.grayText("Progress Max: Maxed")
                if (listOf(maxed).doesFit()) {
                    display(maxed)
                } else {
                    vertical {
                        spacer(effectiveWidth)
                        display(ExtraDisplays.grayText("Progress"))
                        indentedDisplay(ExtraDisplays.text(Text.of("MAXED") { this.color = PvColors.RED }, shadow = false))
                    }
                }
            } else {
                val progressNext = ExtraDisplays.grayText("Progress Next: ")
                val progressMax = ExtraDisplays.grayText("Progress Max: ")
                val progressNextPercentage = ExtraDisplays.grayText("${(activePet.progressToNextLevel * 100).round()}%")
                val progressMaxPercentage = ExtraDisplays.grayText("${(activePet.progressToMax * 100).round()}%")

                if (listOf(progressMax, progressMaxPercentage, colon).doesFit() && listOf(progressNext, progressNextPercentage, colon).doesFit()) {
                    vertical {
                        horizontal {
                            display(progressNext)
                            display(colon)
                            display(progressNextPercentage)
                        }
                        horizontal {
                            display(progressMax)
                            display(colon)
                            display(progressMaxPercentage)
                        }
                    }
                } else {
                    vertical {
                        spacer(effectiveWidth)
                        display(ExtraDisplays.grayText("Progress"))
                        indentedHorizonal {
                            string("Next: ")
                            display(progressNextPercentage)
                        }
                        indentedHorizonal {
                            string("Max: ")
                            display(progressMaxPercentage)
                        }
                    }
                }
            }

            activePet.candyUsed.takeIf { it > 0 }?.let {
                val number = ExtraDisplays.grayText(it.toString())
                val candies = ExtraDisplays.grayText("Candy Used")
                if (listOf(number, candies, colon).doesFit()) {
                    horizontal {
                        display(candies)
                        display(colon)
                        display(number)
                    }
                } else {
                    vertical {
                        spacer(effectiveWidth)
                        display(ExtraDisplays.grayText("Candy Used"))
                        indentedHorizonal {
                            display(number)
                            string("/10")
                        }
                    }
                }
            }

            val petItemStack = activePet.heldItem?.let { SkyBlockItemsRepo.getItemStack(it) } ?: return@vertical
            val itemText = ExtraDisplays.grayText("Held Item")
            val itemDisplay = Displays.item(petItemStack, showTooltip = true)
            if (listOf(itemText, itemDisplay, colon).doesFit()) {
                horizontal(alignment = MIDDLE) {
                    display(itemText)
                    display(colon)
                    display(itemDisplay)
                }
            } else {
                horizontal {
                    spacer(effectiveWidth)
                    display(ExtraDisplays.grayText("Held Item"))
                    indentedDisplay(itemDisplay)
                }
            }
        },
        width = width,
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    fun LayoutBuilder.indentedDisplay(display: Display) = this.indented(display.asWidget())

    fun LayoutBuilder.indentedHorizonal(spacing: Int = 0, alignment: Float = 0f, builder: LayoutBuilder.() -> Unit) =
        this.indented(PvLayouts.horizontal(spacing, alignment, builder))

    fun LayoutBuilder.indentedVertical(spacing: Int = 0, alignment: Float = 0f, builder: LayoutBuilder.() -> Unit) =
        this.indented(PvLayouts.vertical(spacing, alignment, builder))

    fun LayoutBuilder.indented(widget: LayoutElement) = horizontal { spacer(2); widget(widget) }


}

