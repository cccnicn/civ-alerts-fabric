# CivAlerts Fabric — Roadmap

## Stage 1 — Project Setup ✅
- [x] Create GitHub店铺 repository
- [x] Initialize Fabric project structure
- [x] Create `README.md`, `.gitignore`, `ROADMAP.md`
- [x] Configure Gradle build files
- [x] Create `fabric.mod.json`
- [x] First commit & push

## Stage 2 — Chat Interception & Parsing
- [ ] Implement `ChatInterceptor`
- [ ] Implement `HoverEventParser`
- [ ] Test HoverEvent extraction

## Stage 3 — Event Management
- [ ] Implement `EventManager`
- [ ] Implement `CivEvent` data model
- [ ] Categorize events (Scout / Border Entrance / Border Exit)

## Stage 4 — HUD & History
- [ ] Implement `HudRenderer` (overlay, drag, resize, scale, opacity)
- [ ] Implement `HistoryScreen` (scrollable, 100 events)
- [ ] Implement coordinate copy
- [ ] Implement auto-cleanup (10 min)
- [ ] Implement keybinding (F8)
- [ ] Implement config persistence

## Stage 5 — Build & Release
- [ ] Build project, fix compilation errors
- [ ] Update `README.md`
- [ ] Create GitHub Release v1.0.0
- [ ] Attach JAR to release
