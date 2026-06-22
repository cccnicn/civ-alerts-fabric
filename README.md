# CivAlerts Fabric

A Minecraft Fabric client mod for **1.21.11** that intercepts `[Civ]` server notifications (Scout Report, Border Entrance, Border Exit), extracts their HoverEvent data (player names + coordinates), and displays them in a customizable HUD.

## Features

- **Chat Interception**: Automatically detects `[Civ] Scout Report`, `[Civ] Border Entrance Report`, and `[Civ] Border Exit Report` messages via both GAME and CHAT pipelines.
- **HoverEvent Parsing**: Extracts full text from `HoverEvent.ShowText` sealed interface — player names, coordinates, and all details from the tooltip.
- **Categorized HUD**: Three columns — Scout Reports, Border Entrance, Border Exit — in a single overlay window.
- **Drag & Drop**: Drag the header to reposition the HUD anywhere on screen.
- **Resize**: Drag the bottom-right corner handle to resize the window.
- **Toggle Visibility**: Press **F8** to show/hide the HUD.
- **History Screen**: Press **F9** or click the **[H]** button in the HUD header to open a scrollable history of up to 100 events.
- **Coordinate Click-to-Copy**: Click any coordinates in the HUD to copy them to your clipboard. A notification confirms the copy.
- **Timestamps & Age**: Each event shows `[HH:mm:ss] Xs ago` or `Xm ago`.
- **Auto-Cleanup**: Events older than 10 minutes are removed from the active HUD (preserved in history).
- **Config Persistence**: Position, size, scale, opacity, and visibility are saved to `civ-alerts.json` in the config directory. Settings survive game restarts.
- **MC 1.21.11 API**: Uses `Matrix3x2fStack` (2D), `KeyInput.key()`, `KeyBinding.Category`, `HoverEvent.ShowText` pattern matching — all compatible with Minecraft 1.21.11 sealed interfaces and records.

## Requirements

| Dependency | Version |
|------------|---------|
| Minecraft Java Edition | **1.21.11** |
| Fabric Loader | ≥ 0.18.5 |
| Fabric API | 0.141.4+1.21.11 (or compatible) |
| Java | ≥ 21 |

## Installation

1. Download the latest `civ-alerts-fabric-1.0.2.jar` from the [Releases page](../../releases).
2. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11.
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11 and place it in `.minecraft/mods/`.
4. Place `civ-alerts-fabric-1.0.2.jar` in `.minecraft/mods/`.
5. Launch Minecraft with the Fabric profile.

## Usage

### HUD Controls

| Action | Input |
|--------|-------|
| Toggle HUD visibility | **F8** |
| Open History screen | **F9** or click **[H]** in HUD header |
| Close History screen | **ESC** |
| Scroll History | Mouse wheel |
| Drag HUD window | Click & drag the header bar |
| Resize HUD window | Click & drag bottom-right corner □ |
| Copy coordinates | Click any coordinate text in HUD |

> **Tip:** HUD drag/resize works when a screen is open (chat, inventory, etc.) — the cursor is free to interact. When no screen is open, F8 toggles visibility.

### What Gets Intercepted

The mod matches chat messages matching this pattern:

```
[Civ] Scout Report (N)
[Civ] Border Entrance Report (N)
[Civ] Border Exit Report (N)
```

Where `N` is the number of entries. The mod then extracts the `HoverEvent.ShowText` value from the message (the same text you see when hovering over the [Civ] message in chat) and splits it into individual entries displayed in the HUD columns.

Each entry typically contains a player name and coordinates like `PlayerName 100 64 -200`.

## Building from Source

```bash
git clone https://github.com/cccnicn/civ-alerts-fabric.git
cd civ-alerts-fabric
./gradlew build
```

The built JAR will be in `build/libs/`.

### Build Requirements

- Java 21 (JDK)
- Gradle 8.14 (wrapper included)
- Internet access (for downloading Minecraft dependencies)

## Troubleshooting

### Events don't appear in HUD

1. Check the game log (`latest.log`) for `ChatInterceptor received:` messages — if none appear, the server isn't sending [Civ] messages through the detected pipeline.
2. If `ChatInterceptor received: [Civ]...` appears but `Intercepted type=` doesn't, the regex might not match the server's message format.
3. If `Intercepted type=... parsedEntries=0` appears, HoverEvent data wasn't extracted — check `collectHoverText` log lines.

### F9 crashes

Fixed in v1.0.2 — the History screen no longer calls `renderBackground()` and `super.render()` separately (which caused "Can only blur once per frame" in MC 1.21.11).

### HUD can't be dragged

Drag/resize requires the cursor to be ungrabbed — open any screen first (press E for inventory, T for chat), then drag the HUD header.

## Changelog

### v1.0.2

- **Fixed**: HistoryScreen crash ("Can only blur once per frame") — merged renderBackground into super.render()
- **Fixed**: HUD drag/resize blocked when any screen is open — now only blocks when HistoryScreen is open
- **Fixed**: Mouse coordinate calculation — uses `Mouse.getScaledX/getScaledY` for correct screen-space coords
- **Fixed**: HoverEvent parsing — uses `instanceof HoverEvent.ShowText` pattern matching (MC 1.21.11 sealed interface)
- **Fixed**: Matrix operations — `pushMatrix/popMatrix/translate(float,float)/scale(float,float)` for `Matrix3x2fStack`
- **Fixed**: KeyInput API — `key()` instead of `keyCode()`
- **Fixed**: KeyBinding category — `KeyBinding.Category.create(Identifier.of(...))` instead of String
- **Fixed**: HistoryScreen text truncation — binary search on character count instead of pixel-as-index bug
- **Fixed**: HudRenderer text truncation — `textRenderer.getWidth()` instead of `colWidth/6` heuristic
- **Added**: CHAT message pipeline interception (in addition to GAME)
- **Added**: Info-level logging for chat interception and HoverEvent parsing
- **Added**: Mod icon (64×64)
- **Updated**: `fabric.mod.json` minecraft dependency to `~1.21.11`
- **Updated**: Gradle dependencies: yarn 1.21.11+build.1, loader 0.18.5, fabric-api 0.141.4+1.21.11

### v1.0.0

- Initial release
- Scout Reports, Border Entrance Reports, Border Exit Reports support
- Customizable HUD with drag, resize, scale, and opacity
- Event history screen with up to 100 events
- Coordinate click-to-copy
- Config persistence via JSON
- F8 / F9 keybindings

## License

MIT
