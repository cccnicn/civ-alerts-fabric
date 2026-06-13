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

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1.
2. Place the mod JAR in your `.minecraft/mods/` folder.
3. Launch Minecraft with the Fabric profile.

## Building

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

## License

MIT
