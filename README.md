<h1 align="center">
  SkyBlock PV Revamped
</h1>

<div align="center">

[![Discord](https://img.shields.io/discord/1296157888343179264?color=8c03fc&label=Discord&logo=discord&logoColor=white)](https://discord.gg/FsRc2GUwZR)
[![Modrinth](https://img.shields.io/modrinth/dt/8yqXwFLl?style=flat&logo=modrinth)](https://modrinth.com/mod/skyblock-profile-viewer)

</div>

A GUI-focused fork of [Meowdding's SkyBlockPv](https://github.com/meowdding/skyblock-pv).
**Portions of this code are from the SkyBlockPv mod.** The upstream Git history,
license, assets, and attribution are preserved; this is not an official Meowdding release.
See [NOTICE](NOTICE) and the unchanged [LICENSE](LICENSE).

Revamped adds a scrollable left sidebar, a player/page header, denser overview
cards, larger inventory items (with compact-window sizing), and an Appearance editor.
Page navigation uses Minecraft item icons. Overview cards arrange into two or
three columns on wider windows; inventory slots grow to use available width and
height while keeping item counts anchored inside each slot.
The Hypixel/profile/API implementation is unchanged.

### Appearance

Open **Appearance** using the painting button beside Settings and the eyedropper below the sidebar. Click Red, Green, Blue, Cyan, Orange, Pink,
Purple, Midnight, Amethyst, or High contrast to apply and save a complete theme
immediately. No color codes are needed. **Custom colors** reveals the optional
`#RRGGBB` editor; click **Apply** to save custom colors/effects. Invalid values
are rejected without replacing your saved settings. Reset restores the selected
resource-pack theme. Custom UI settings apply globally until reset; cycling legacy
themes still changes their formatting colors and texture mappings.

Inventory pages show one complete page with Previous/Next controls and a page
counter. The clipped neighboring-page previews have been removed. Tall layouts
remain scrollable on small windows.

UI roles: background, sidebar, surface, surfaceAlt, primary, secondary, text,
muted, border, hover, selected, positive, warning, negative. Effects include panel
and background opacity (0–100), blur, corner radius (0–12), and optional chroma
accents. Chroma affects accents only, not item-rarity colors or normal text.

Resource-pack themes can add an optional `ui` object without changing the original
`colors`, `textures`, `name`, or `background_blur` fields:

```json
{
  "name": "My theme",
  "ui": {
    "primary": "#79AAFF",
    "surface": "#1C2638",
    "effects": {
      "panelOpacity": 96,
      "backgroundOpacity": 85,
      "blur": true,
      "cornerRadius": 5,
      "chroma": false
    }
  }
}
```

Legacy `background_blur: false` remains respected unless the user saves a custom
UI override. Existing texture mappings remain active for detailed legacy widgets;
the modern shell and cards use UI roles instead of texture replacements.

### Building this fork

Use JDK **25**, then run `./gradlew build` (Windows: `gradlew.bat build`).
The upstream Stonecutter configuration builds Minecraft **26.1 and 26.2**.
Outputs are under `build/libs`. The build includes theme compatibility and
serialization regression tests. The first build needs network access for Gradle,
Fabric/Minecraft libraries, and upstream's Hypixel museum resource generation.

### Upstream features and historical screenshots

The Profile Viewer can be opened with ``/pv`` for your own Profile, or ``/pv <username>`` for someone else.
<br/>You can switch between tabs using the left sidebar.
<br/>If a tab has multiple categories, you can switch between them using the buttons on the left side of the UI.

> [!WARNING]
> The mod is still in development, design is subject to change.
> Screenshots may be outdated.

## Features

- **Profile Spying**: Adds a button into the Hypixel Profile Viewer to open this Profile Viewer.
- **PronounDB**: Integrates with [PronounDB](https://pronoundb.org/) to display the player's pronouns.
- **Party Finder**: Sends a message to open the Profile Viewer for the player that just joined a party finder party.
- **Replace Social Options**: Replaces Hypixel's "Click to open social options" with "Click to open Profile Viewer" on chat messages.
- **Scaling** (_Experimental_): Automatically scales up some elements based on the player's screen size, to make it more readable on larger screens.
- **and themes...**

### Themes

The original resource themes (Default, Dark, NEU) are retained for compatibility.
Quickly switch between them using the eyedropper button below the sidebar.

<details>
<summary>Theme Showcase</summary>

#### Dark Theme

<img src="./.github/images/dark.png" width="720" alt="" title="Dark Theme">
A dark mode theme for the profile viewer.

#### NEU Theme

<img src="./.github/images/neu.png" width="720" alt="" title="NEU Theme">
A transparent theme for the profile viewer, inspired by NEU's Profile Viewer.

</details>

You can also create your own theme, here's a guide:

<details>
<summary>Creating your own Theme</summary>

This needs to be done in a texture pack.
In the folder ``assets/skyblock-pv/themes/`` create a new file called ``<theme_name>.json``.
The JSON should have this layout:

```json
{
    "name": "<theme_name>",
    "textures": {
        ...
    },
    "colors": {
        ...
    }
}
```

To "replace" a texture, you specify the path to the texture you want to replace as the key, and the path to your new texture as the value.
E.g., this replaces the normal button texture with a custom one:

```json
{
    "name": "MyTheme",
    "textures": {
        "skyblock-pv:buttons/normal": "skyblock-pv:<theme>/buttons/normal"
    },
    "colors": {
        ...
    }
}
```

Some colors might look weird, as they look too similar to the background.
By default, it uses the Minecraft colors.
The colors can either be in hex format or in decimal format.

```json
{
    ...
    "colors": {
        "dark_gray": "#B5B5B5"
    }
}
```

</details>

### Pages

#### 🏠 Home Tab

<img src="./.github/images/home.png" width="720" alt="" title="Home">
Designed to be simple and to show off the main aspects of the player's profile.
<br/>Designed with a purpose to be easy to screenshot and shareable.

#### 📦 Inventory Tab

<img src="./.github/images/backpack.png" width="720" alt="" title="Backpacks">
Switch between EnderChest, Backpack, ... pages using the custom-built carousel or the buttons up top.

#### 📚 Collections Tab

<img src="./.github/images/collection.png" width="720" alt="" title="Collections">

#### 🎣 Fishing Tab

<img src="./.github/images/fishing.png" width="720" alt="" title="Fishing">
All fishing related information, so Essence Upgrades, Trophy Fish, Gear, Stats, ... in one tab.

#### 🔍 And many more tabs...

...we just didn't put them in the ReadMe. Look at them when pv'ing yourself or someone else!

<details>
<summary>All Tabs</summary>

as of 2025-04-16

- Home
- Combat
    - Dungeons
    - Bestiary
  - Crimson Isle
    - Mob Kills
- Inventory
    - Main Inventory
    - Ender Chest
    - Backpacks
    - Wardrobe
    - Accessory
    - Sacks
  - Personal Vault, Potion Bag, Fishing Bag, Quiver, Candy Bag, etc.
- Collections
    - With Minions
- Mining
    - Main Mining
    - Mining Gear
    - HotM
    - Glacite
- Fishing
- Pets
- Farming
    - Main Farming
    - Visitors
    - Crops
    - Composter
- Museum
    - Weapons
    - Armor
- Chocolate Factory
- Rift
    - Main Rift
    - Inventory
    - Ender Chest

</details>

### Mod Compatibility

#### SkyBlocker

- **Issue**: [SkyBlocker](https://github.com/SkyblockerMod/Skyblocker) includes its own Profile Viewer.
- **Resolution**: We override their `/pv` command with ours. If you prefer SkyBlocker's version, use `/skyblocker pv` instead.
- **Note**: We do not provide an option to disable this override, as using our mod implies a preference for our Profile Viewer.
  
## Repository Access Issues

> [!WARNING]
> If you are from Russia or another country where parts of the Internet are blocked, the mod may fail to initialize its external repositories.

### Required Domains

Make sure the following domains are accessible through your VPN, Zapret, or any other bypass solution you are using:

* `https://skyblock-repo.pages.dev/`
* `https://skyblock-api-repo.thatgravyboat.tech/`
* `*.owdding.me/`
* `https://skyblock-pv.thatgravyboat.tech/`

### Configuring Zapret

If you are using Zapret, follow these steps:

1. Open your Zapret installation directory.
2. Navigate to the `lists` folder.
3. Open `list-general.txt`.
4. Add the domains listed above.
5. When adding them, remove everything before `//` (including `//`) and remove the trailing `/` from each URL.

For example:

```
https://skyblock-repo.pages.dev/
```

should become

```
skyblock-repo.pages.dev
```

