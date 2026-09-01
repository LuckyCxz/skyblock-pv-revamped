package me.owdding.skyblockpv.screens.windowed

import com.mojang.authlib.GameProfile
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.OverlayAlignment
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.utils.State
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.layouts.setPos
import me.owdding.lib.platform.screens.MouseButtonEvent
import me.owdding.lib.platform.screens.mouseClicked
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.CachedApis
import me.owdding.skyblockpv.api.PlayerAPI
import me.owdding.skyblockpv.api.data.SocialEntry
import me.owdding.skyblockpv.api.data.profile.EmptySkyBlockProfile
import me.owdding.skyblockpv.api.data.profile.EmptySkyBlockProfile.Reason
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.screens.windowed.tabs.general.NetworthDisplay
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.ExtraWidgetRenderers
import me.owdding.skyblockpv.utils.PvPageState
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.multiLineDisplay
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvToast
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.theme.PvColors
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import me.owdding.skyblockpv.utils.theme.UiWidgets
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.CommonComponents
import net.minecraft.util.TriState
import net.minecraft.util.Util
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.platform.applyBackgroundBlur
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines

abstract class BaseWindowedPvScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile?) : BasePvScreen(name, gameProfile, profile) {

    protected val sidebarWidth get() = if (width < 500) 108 else 132
    override val uiWidth get() = (width - sidebarWidth - 64).coerceAtLeast(160)
    override val uiHeight get() = (height - 94).coerceAtLeast(100)
    abstract val tab: PvTab

    override fun init() {
        val bg = UiWidgets.background(uiWidth, uiHeight) { ThemeSupport.ui.background }.asWidget()
        bg.setPosition(sidebarWidth + 36, 48)
        bg.applyLayout()
        val sidebar = UiWidgets.background(sidebarWidth, uiHeight + 32) { ThemeSupport.ui.sidebar }.asWidget()
        sidebar.setPosition(12, 16)
        sidebar.applyLayout()
        val avatar = Displays.item(PvTab.MAIN.getIcon(gameProfile), 20, 20).asWidget()
        avatar.setPosition(bg.x, 20)
        avatar.applyLayout()
        addRenderableOnly(PvWidgets.text(gameProfile.name + "  /  " + tabTitle.string)
            .withSize(uiWidth - 28, 20).withPosition(bg.x + 28, 20))
        createTabs().asScrollable(sidebarWidth + 20, uiHeight + 22).applyLayout(12, 22)

        createTopRow(bg).applyLayout(12, height - 26)
        // Appearance remains available even while a profile is loading or unavailable.
        if (tab == PvTab.APPEARANCE) {
            create(bg)
            return
        }

        when (val profile = profile) {
            is EmptySkyBlockProfile if profile.reason == Reason.LOADING -> addLoader()
            is EmptySkyBlockProfile if profile.reason == Reason.NO_PROFILES -> addNoProfiles()
            is EmptySkyBlockProfile if profile.reason == Reason.ERROR -> addParsingError(profile.throwable!!)
        }

        if (!isProfileInitialized()) return
        initedWithProfile = true

        try {
            create(bg)
        } catch (e: Exception) {
            SkyBlockPv.error("Failed to display profile ${this::class.simpleName} from player ${gameProfile.name} (${gameProfile.id}) and profile ${profile.id.name}")

            val errorWidget = PvLayouts.vertical {
                val text = "widgets.error.stacktrace".asTranslated(
                    name,
                    gameProfile.name,
                    gameProfile.id,
                    profile.id.name,
                    e.javaClass.name,
                    e.message,
                    e.getStackTraceString(7),
                )

                SkyBlockPv.error("Failed to build screen ${this.javaClass.simpleName}! ($name - $gameProfile)", e)

                text.splitLines().forEach {
                    widget(PvWidgets.text(it).withCenterAlignment().withSize(uiWidth, 10))
                }
            }
            FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
            errorWidget.applyLayout()
        }

        val searchBox = createSearch(bg)
        searchBox.applyLayout()
        createProfileDropdown(bg).let {
            it.applyLayout()

            if (!Config.socials) return@let
            val availableWidth = searchBox.x - (it.x + it.width) - 5
            val socialsWidth = availableWidth.coerceIn(0, 100)
            if (socialsWidth < 24) return@let
            val button = createSocialDropdown(socialsWidth)
            button.setPosition(it.x + it.width + 5, it.y)
            button.applyLayout()
        }

    }

    private fun addNoProfiles() {
        val errorWidget = PvLayouts.vertical(alignment = 0.5f) {
            display("widgets.error.no_profiles".asTranslated(gameProfile.name).multiLineDisplay(Alignment.CENTER))
        }

        FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
        errorWidget.applyLayout()
    }

    private fun addParsingError(throwable: Throwable) {
        SkyBlockPv.error("Failed to parse profile!", throwable)

        val errorWidget = PvLayouts.vertical {
            val text = "widgets.error.parsing_error".asTranslated(
                name,
                gameProfile.name,
                gameProfile.id,
                McClient.version,
                SkyBlockPv.version,
                throwable.javaClass.name,
                throwable.message,
                throwable.getStackTraceString(7),
            )

            text.splitLines().forEach {
                widget(PvWidgets.text(it).withCenterAlignment().withSize(uiWidth, 10))
            }
        }
        FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
        errorWidget.applyLayout()
    }

    private fun createTopRow(bg: DisplayWidget) = PvLayouts.horizontal(5) {
        createUserRow()
        if (SkyBlockPv.isDevMode) createDevRow(bg)
    }

    private fun LayoutBuilder.createUserRow() = horizontal(5) {
        val settingsButton =
            Button().withSize(20, 20).withRenderer(WidgetRenderers.icon<AbstractWidget>(SkyBlockPv.olympusId("icons/edit")).withColor(MinecraftColors.WHITE))
                .withTexture(null)
                .withCallback { McClient.setScreenAsync { ResourcefulConfigScreen.getFactory(SkyBlockPv.MOD_ID).apply(this@BaseWindowedPvScreen) } }
                .withTooltip(+"widgets.open_settings")

        val themeSwitcher =
            Widgets.button().withRenderer(WidgetRenderers.icon<AbstractWidget>(UIIcons.EYE_DROPPER).withColor(MinecraftColors.WHITE)).withSize(20, 20)
                .withTexture(null).withCallback {
                    ThemeSupport.nextTheme()
                    safelyRebuild()
                    SkyBlockPv.config.save()
                }.withTooltip("widgets.theme_switcher".asTranslated(ThemeSupport.currentTheme.translation))

        widget(settingsButton)
        widget(themeSwitcher)
        widget(Button().withSize(20, 20).withTexture(null)
            .withRenderer(WidgetRenderers.layered(
                UiWidgets.navigation(CommonComponents.EMPTY, tab == PvTab.APPEARANCE),
                WidgetRenderers.center(16, 16) { gr, ctx, _ ->
                    gr.item(PvTab.APPEARANCE.getIcon(gameProfile), ctx.x, ctx.y)
                },
            ))
            .withCallback { if (tab != PvTab.APPEARANCE) Utils.openTab(PvTab.APPEARANCE, gameProfile, profile) }
            .withTooltip(+"tab.appearance"))
    }

    private fun LayoutBuilder.createDevRow(bg: DisplayWidget) = horizontal(5) {
        // Useful for hotswaps
        val refreshButton = Button().withRenderer(WidgetRenderers.text(Text.of("Refresh Screen"))).withSize(60, 20).withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { this@BaseWindowedPvScreen.safelyRebuild() }

        val hoverText = Text.multiline(
            "Screen: ${this@BaseWindowedPvScreen.width}x${this@BaseWindowedPvScreen.height}",
            "UI: ${uiWidth}x${uiHeight}",
            "BG: ${bg.width}x${bg.height}",
        )
        val screenSizeText = Button().withRenderer(WidgetRenderers.text(Text.of("Screen Size"))).withSize(60, 20).withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { McClient.self.keyboardHandler.clipboard = hoverText.stripped }.withTooltip(hoverText)

        val saveButton = Button().withRenderer(WidgetRenderers.text(Text.of("Save Profiles"))).withSize(60, 20).withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { saveProfiles() }

        val clearCache = Button().withRenderer(WidgetRenderers.text(Text.of("Clear Cache"))).withSize(60, 20).withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback(CachedApis::clearCaches)

        val networthDebug = Button().withRenderer(WidgetRenderers.text(Text.of("Networth"))).withSize(60, 20).withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { McClient.clipboard = NetworthDisplay.networthDebug(profile).joinToString("\n") }


        widget(refreshButton)
        widget(screenSizeText)
        widget(saveButton)
        widget(clearCache)
        widget(networthDebug)
    }

    private fun createTabs() = PvLayouts.vertical(3) {
        PvTab.entries.forEach { tab ->
            if (tab == PvTab.APPEARANCE) return@forEach
            if (tab.getTabState(profile) == TriState.FALSE) return@forEach
            if (!tab.canDisplay(profile)) return@forEach

            val categories = tab.subcategories(profile)
            val label = +"tab.${tab.name.lowercase()}"
            if (categories.isNotEmpty()) {
                lateinit var tabButton: Button
                tabButton = Button().withSize(sidebarWidth - 8, 22).withTexture(null)
                    .withRenderer(UiWidgets.navigation(label.copy().append(" >"), tab.isSelected(), tab.getIcon(gameProfile)))
                    .apply { withTooltip(label) }
                    .withCallback {
                        val selectedCategory = categories.firstOrNull { it.isSelected }
                        val menuWidth = minOf(160, width - sidebarWidth - 32)
                        val menuHeight = minOf(categories.size * 26 + 3, height - 48)
                        // Keep the flyout attached to the button that opened it. Center
                        // long menus on that button, then clamp them inside the screen.
                        val menuX = (tabButton.x + tabButton.width + 4).coerceAtMost(width - menuWidth - 4)
                        val menuY = (tabButton.y + (tabButton.height - menuHeight) / 2)
                            .coerceIn(4, (height - menuHeight - 4).coerceAtLeast(4))
                        earth.terrarium.olympus.client.ui.context.ContextMenu.open(
                            menuX, menuY,
                        ) { menu ->
                            menu.withBounds(menuWidth, menuHeight)
                            menu.withTexture(ThemeSupport.texture(SkyBlockPv.id("box/rounded_box_thin")))
                            categories.forEach { category ->
                                menu.add {
                                    Button().withSize(menuWidth - 4, 26).withTexture(null)
                                        .withRenderer(UiWidgets.navigation(Text.of(category.hover), category == selectedCategory, category.icon))
                                        .apply { withTooltip(Text.of(category.hover)) }
                                        .withCallback { Utils.openTab(category, gameProfile, profile) }
                                }
                            }
                        }
                    }
                widget(tabButton)
            } else {
                widget(Button().withSize(sidebarWidth - 8, 22).withTexture(null)
                    .withRenderer(UiWidgets.navigation(label, tab.isSelected(), tab.getIcon(gameProfile)))
                    .withCallback { if (!tab.isSelected()) Utils.openTab(tab, gameProfile, profile) }
                    .apply { withTooltip(label) })
            }
        }
    }


    private var coopDropdownVisible = false
    private fun createSearch(bg: DisplayWidget): LayoutElement {
        var width = if (uiWidth < 240) (uiWidth / 2 - 20).coerceAtLeast(55) else 100

        return LayoutFactory.horizontal {
            val coopDropdown = Button().apply {
                withSize(12, 20)
                withTexture(null)
                withRenderer(
                    WidgetRenderers.padded(
                        4, 0, 4, 0,
                        WidgetRenderers.icon<AbstractWidget>(UIIcons.CHEVRON_UP).withColor(MinecraftColors.WHITE),
                    ),
                )
                withTooltip(+"widgets.coop_search_tooltip")
                withCallback {
                    coopDropdownVisible = !coopDropdownVisible
                    safelyRebuild()
                }
            }

            val usernameState = State.of(gameProfile.name)
            val username = Widgets.autocomplete<String>(usernameState) { box ->
                box.withEnterCallback {
                    Utils.fetchGameProfile(box.value) { profile ->
                        profile?.let { Utils.openPv(it) }
                    }
                }
                box.withTexture(ExtraConstants.TEXTBOX)
            }
            username.withAlwaysShow(true)
            username.withSuggestions { SkyBlockPlayerSuggestionProvider.getSuggestions(it) }
            username.withPlaceholder((+"widgets.username_input").stripped)
            username.withSize(width, 20)


            val coopMemberDropdownState = DropdownState(null, State.of(profile.userId), true)
            val coopMemberDropdown = Widgets.dropdown(
                coopMemberDropdownState,
                profile.coopMembers.keys.toList(),
                { _ -> CommonComponents.EMPTY },
                { button ->
                    button.withSize(width, 20)
                    button.withRenderer(
                        WidgetRenderers.text<Button>(
                            Text.of {
                                color = PvColors.WHITE
                                append("◆ ")
                                append(gameProfile.name)
                            },
                        ).withPadding(4, 6),
                    )
                },
                { builder ->
                    builder.withCallback { coopProfile ->
                        val profile = profile.coopMembers[coopProfile]

                        if (profile == null) {
                            Text.of("Unknown member!") { color = TextColor.RED }.sendWithPrefix()
                            return@withCallback
                        }

                        val gameProfile = profile.getNow(null)
                        if (gameProfile != null) {
                            Utils.preferedProfileId = this@BaseWindowedPvScreen.profile.id.id
                            Utils.openPv(gameProfile)
                        } else if (profile.isCompletedExceptionally) {
                            Text.of("Failed to fetch username!").sendWithPrefix()
                        } else {
                            Text.of("Still fetching!").sendWithPrefix()
                        }

                    }

                    val loadingRenderer = WidgetRenderers.text<Button>(Text.of("Loading...") { this.color = TextColor.RED })
                        .withLeftAlignment()
                        .withPadding(0, 4)
                    val failedToLoad = WidgetRenderers.text<Button>(Text.of("Error!") { this.color = TextColor.RED })
                        .withLeftAlignment()
                        .withPadding(0, 4)
                    builder.withEntryRenderer { id ->
                        val future = profile.coopMembers[id] ?: return@withEntryRenderer WidgetRenderers.text<Button>(
                            Text.of("Unknown!") {
                                this.color = TextColor.DARK_RED
                            },
                        ).withLeftAlignment().withPadding(0, 4)
                        val widgetRenderer = future.thenApply { profile ->
                            if (profile == null) return@thenApply null

                            WidgetRenderers.text<Button>(
                                Text.of {
                                    color = PvColors.WHITE
                                    if (id == profile) {
                                        underlined = true
                                        append("◆ ")
                                    } else {
                                        append("◇ ")
                                    }
                                    append(profile.name.toString())
                                },
                            ).withLeftAlignment().withPadding(0, 4)
                        }

                        ExtraWidgetRenderers.supplied {
                            widgetRenderer.getNow(loadingRenderer) ?: failedToLoad
                        }
                    }
                    builder.withAlignment(OverlayAlignment.TOP_LEFT)
                },
            ).apply {
                withTexture(ExtraConstants.BUTTON_DARK)
                setPosition(bg.x, bg.y + bg.height)
            }

            if (profile.coopMembers.isNotEmpty()) {
                widget(coopDropdown)
                spacer(5)
                width += coopDropdown.width + 5
            }
            if (coopDropdownVisible) {
                widget(coopMemberDropdown)
                McClient.runNextTick {
                    coopMemberDropdown.mouseClicked(MouseButtonEvent(coopMemberDropdown.x + 1.0, coopMemberDropdown.y + 1.0, 1), false)
                }
                coopDropdownVisible = false
            } else widget(username)

        }.setPos(bg.x + bg.width - width, bg.y + bg.height)
    }

    private fun createProfileDropdown(bg: DisplayWidget): LayoutElement {
        val width = (uiWidth / 2 - 16).coerceIn(55, 100)

        val dropdownState = DropdownState<SkyBlockProfile>.of(profile)
        val dropdown = Widgets.dropdown(
            dropdownState,
            profiles,
            { profile ->
                Text.of {
                    color = PvColors.WHITE
                    if (profile.selected) {
                        underlined = true
                        append("◆ ")
                    } else {
                        append("◇ ")
                    }
                    append(profile.id.name)
                    append(
                        when (profile.profileType) {
                            ProfileType.NORMAL -> ""
                            ProfileType.BINGO -> " §9Ⓑ"
                            ProfileType.IRONMAN -> " ♻"
                            ProfileType.STRANDED -> " §a☀"
                            ProfileType.UNKNOWN -> " §c§ka"
                        },
                    )
                }
            },
            { button -> button.withSize(width, 20) },
            { builder ->
                builder.withCallback { profile ->
                    this.profile = profile ?: return@withCallback
                    this.onProfileSwitch(profile)
                    this.safelyRebuild()
                }
                builder.withAlignment(OverlayAlignment.TOP_LEFT)
            },
        ).apply {
            withTexture(ExtraConstants.BUTTON_DARK)
            setPosition(bg.x, bg.y + bg.height)
        }

        return dropdown
    }

    private fun createSocialDropdown(dropdownWidth: Int): LayoutElement {
        val source = "?utm_source=SkyBlockPv"
        val entries = listOf(
            SocialEntry("SkyCrypt", "https://sky.shiiyu.moe/stats/${gameProfile.name}/${profile.id.name}$source"),
            SocialEntry("EliteSkyBlock", "https://eliteskyblock.com/@${gameProfile.name}/${profile.id.name}$source"),
            *PlayerAPI.getCached(gameProfile.id)?.socials?.map { it.key.toEntry(it.value) }.orEmpty().toTypedArray(),
        )

        val button = Widgets.dropdown(
            DropdownState<String>.empty(),
            entries,
            { Text.of(it.name) },
            { button ->
                button.withSize(dropdownWidth, 20)
                button.withRenderer(WidgetRenderers.text(+"widgets.socials"))
            },
            { builder ->
                builder.withCallback {
                    if (it == null) return@withCallback
                    if (it.shouldCopy) {
                        McClient.clipboard = it.url
                        PvToast.addSocialsCopiedToast(it.url)
                    } else {
                        Util.getPlatform().openUri(it.url)
                    }
                }
                builder.withAlignment(OverlayAlignment.TOP_LEFT)
            },
        ).apply {
            withTexture(ExtraConstants.BUTTON_DARK)
        }

        return button
    }

    override fun extractBackground(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (ThemeSupport.ui.blur) {
            guiGraphics.applyBackgroundBlur()
        }
        guiGraphics.fill(0, 0, width, height, UiWidgets.alpha(ThemeSupport.ui.background, ThemeSupport.ui.backgroundOpacity))
    }

    open fun toTabState(): PvPageState = this.tab
}
