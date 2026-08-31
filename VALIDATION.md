# Verification

- Upstream baseline: `416323d` from `meowdding/skyblock-pv`, with history retained.
- JDK 25; Gradle wrapper builds Minecraft 26.1 and 26.2.
- `gradlew.bat build`: compilation, packaging, and tests for both versions.
- Four regression tests per version: legacy theme decoding, preset/effect
  round-trips, bounds on untrusted effect values, and named color preset availability.
- The original `LICENSE` and the complete API source directory are unchanged.
- JARs include `LICENSE` and `NOTICE` attribution.

## Remaining manual checks

The GUI has not been exercised in a live Minecraft/Hypixel session. Before release,
check mouse/keyboard navigation, scrolling, all inventory tooltips, co-op/profile
switching, resizing/GUI scale, resource reload, and persistence after restarting.
Verify blur with the user's graphics setup and any shader mods.

Appearance editor labels are currently English. Existing translated tabs and
upstream detailed widgets retain their translations. Legacy texture packs still
affect detailed widgets, while the new shell/cards use UI color roles. The old
category-side alignment option is retained in config for compatibility; categories
now appear under their parent in the scrollable sidebar.

Upstream Gradle/Kotlin/dependency deprecation warnings remain; they do not prevent
the supported builds. No credentials or downloaded build tooling are committed.
