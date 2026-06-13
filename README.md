# CivAlerts Fabric

A Minecraft Fabric client mod for **1.21.1** that intercepts `[Civ]` server notifications (Scout Report, Border Entrance, Border Exit), extracts their HoverEvent data, and displays them in a customizable HUD.

## Features

- **Chat Interception**: Automatically detects `[Civ] Scout Report`, `[Civ] Border Entrance Report`, and `[Civ] Border Exit Report` messages.
- **HoverEvent Parsing**: Extracts full text from HoverEvents using native Minecraft API.
- **Categorized HUD**: Separate sections for Scout Reports, Border Entrance, and Border Exit.
- **Customizable Interface**: Drag & drop, resize, scale, and adjust opacity.
- **Timestamps & Timers**: Shows event time and live "ago" timer.
- **Coordinate Clicking**: Click coordinates to copy them to clipboard.
- **History**: Stores up to 100 events; viewable via dedicated history screen.
- **Auto-Cleanup**: Events older than 10 minutes are removed from the active HUD (but kept in history).
- **Persistence**: All settings (position, size, scale, opacity, visibility) are saved between sessions.
- **Toggle Key**: Press **F8** to show/hide the HUD.
- **History Key**: Press **F9** to open the event history screen.

## Requirements

- Minecraft Java Edition 1.21.1
- Fabric Loader >= 0.16.0
- Fabric API (bundled)

## Installation

1. Download the latest release JAR from the [Releases page](../../releases).
2. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1 if you haven't already.
3. Place the downloaded `civ-alerts-fabric-1.0.0.jar` into your `.minecraft/mods/` folder.
4. Launch Minecraft with the Fabric profile.

## Usage

### HUD Controls

| Action | Input |
|--------|-------|
| Toggle HUD | Press **F8** |
| Open History | Press **F9** or click **[H]** button in HUD header |
| Drag Window | Click and drag the header |
| Resize Window | Drag the bottom-right corner handle |

### Coordinate Copying

When coordinates appear in a Scout Report, you can click them directly in the HUD to copy them to your clipboard. A notification will appear confirming the copy.

### History Screen

The History Screen (F9) shows all stored events (up to 100) in a scrollable list with:

- Event type (color-coded)
- Timestamp (HH:mm:ss)
- Full event text

Scroll with your mouse wheel to navigate the list.

## Building from Source

```bash
# Clone the repository
git clone https://github.com/cccnicn/civ-alerts-fabric.git
cd civ-alerts-fabric

# Build the project
./gradlew build
```

The built JAR will be in `build/libs/`.

## Changelog

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
