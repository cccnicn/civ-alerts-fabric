package com.civalerts.hud;

import com.civalerts.config.ConfigData;
import com.civalerts.config.ConfigManager;
import com.civalerts.event.CivEvent;
import com.civalerts.event.CivEventType;
import com.civalerts.event.EventManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HudRenderer {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final int HEADER_HEIGHT = 16;
    private static final int FOOTER_HEIGHT = 4;

    private final ConfigManager configManager;
    private final EventManager eventManager;
    private final List<ClickableZone> clickZones = new ArrayList<>();

    private boolean dragging = false;
    private boolean resizing = false;
    private double dragStartX, dragStartY;
    private int dragStartConfigX, dragStartConfigY;
    private int resizeStartW, resizeStartH;
    private long lastNotificationTime = 0;
    private String notificationText = null;

    public HudRenderer(ConfigManager configManager, EventManager eventManager) {
        this.configManager = configManager;
        this.eventManager = eventManager;
    }

    public void register() {
        HudRenderCallback.EVENT.register(this::render);
    }

    public void tick() {
        configManager.tickSave();
    }

    private void render(DrawContext context, RenderTickCounter tickCounter) {
        ConfigData config = configManager.getData();
        if (!config.visible) return;

        updateInteraction(config);

        context.getMatrices().push();
        context.getMatrices().translate(config.x, config.y, 0);
        context.getMatrices().scale(config.scale, config.scale, 1f);

        int scaledW = (int)(config.width / config.scale);
        int scaledH = (int)(config.height / config.scale);

        int bgAlpha = (int)(255 * config.opacity);
        int bg = (bgAlpha << 24) | 0x222222;
        int headerColor = 0x88001177;

        // Background
        context.fill(0, 0, scaledW, scaledH, bg);

        // Header
        context.fill(0, 0, scaledW, HEADER_HEIGHT, headerColor);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("CivAlerts"), 4, 4, 0xFFFFFF, false);

        // History button in header
        String histLabel = "[H]";
        int histW = MinecraftClient.getInstance().textRenderer.getWidth(histLabel) + 6;
        int histLocalX = scaledW - histW - 2;
        context.fill(histLocalX, 2, histLocalX + histW, HEADER_HEIGHT - 2, 0x44000000);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(histLabel), histLocalX + 3, 4, 0xFFFFFF, false);

        // Content area
        int colWidth = scaledW / 3;

        // Column headers
        for (int col = 0; col < 3; col++) {
            String title = switch (col) {
                case 0 -> "SCOUT";
                case 1 -> "BORDER IN";
                default -> "BORDER OUT";
            };
            int colX = col * colWidth;
            context.fill(colX, HEADER_HEIGHT, colX + colWidth, HEADER_HEIGHT + 12, 0x44000000);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(title), colX + 2, HEADER_HEIGHT + 2, 0xFFFFFF, false);
        }

        // Events
        clickZones.clear();
        List<CivEvent> allActive = eventManager.getActive();
        int lineH = 10;
        int contentStartY = HEADER_HEIGHT + 14;

        for (int col = 0; col < 3; col++) {
            CivEventType type = switch (col) {
                case 0 -> CivEventType.SCOUT;
                case 1 -> CivEventType.BORDER_ENTRANCE;
                default -> CivEventType.BORDER_EXIT;
            };
            int colX = col * colWidth;
            int y = contentStartY;

            for (CivEvent event : allActive) {
                if (event.type() != type) continue;
                if (y + lineH > scaledH - FOOTER_HEIGHT) break;

                String timeStr = TIME_FORMAT.format(Instant.ofEpochMilli(event.timestamp()));
                long ageSec = event.ageSeconds();
                String ageStr = ageSec < 60 ? ageSec + "s ago" : (ageSec / 60) + "m ago";
                String display = "[" + timeStr + "] " + ageStr + " " + event.text();

                // Truncate if too long
                int maxChars = colWidth / 6;
                if (display.length() > maxChars) {
                    display = display.substring(0, maxChars) + "...";
                }

                context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(display), colX + 2, y, 0xFFFFFF, false);

                // Track coordinates for click (in local unscaled space)
                for (String coord : event.coordinates()) {
                    int coordIdx = display.indexOf(coord);
                    if (coordIdx >= 0) {
                        int coordLocalX = colX + 2 + MinecraftClient.getInstance().textRenderer.getWidth(display.substring(0, coordIdx));
                        int coordLocalY = y;
                        int coordLocalW = MinecraftClient.getInstance().textRenderer.getWidth(coord);
                        clickZones.add(new ClickableZone(coordLocalX, coordLocalY, coordLocalX + coordLocalW, coordLocalY + lineH, coord));
                    }
                }

                y += lineH;
            }
        }

        // Resize handle
        context.fill(scaledW - 8, scaledH - 8, scaledW, scaledH, 0xFFAAAAAA);

        context.getMatrices().pop();

        // Notification (drawn outside the scaled matrix, in screen space)
        if (notificationText != null && System.currentTimeMillis() - lastNotificationTime < 2000) {
            int notifW = MinecraftClient.getInstance().textRenderer.getWidth(notificationText) + 8;
            int screenW = context.getScaledWindowWidth();
            int notifX = (screenW - notifW) / 2;
            context.fill(notifX, 2, notifX + notifW, 16, 0xCC000000);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(notificationText), notifX + 4, 5, 0x44FF44, false);
        }
    }

    private void updateInteraction(ConfigData config) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) return;

        double mx = client.mouse.getX() / client.getWindow().getScaleFactor();
        double my = client.mouse.getY() / client.getWindow().getScaleFactor();

        boolean leftDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        int scaledW = (int)(config.width / config.scale);
        int scaledH = (int)(config.height / config.scale);

        boolean inBounds = mx >= config.x && mx <= config.x + scaledW * config.scale && my >= config.y && my <= config.y + scaledH * config.scale;
        boolean inHeader = inBounds && my < config.y + HEADER_HEIGHT * config.scale;
        boolean inResize = inBounds && mx > config.x + (scaledW - 8) * config.scale && my > config.y + (scaledH - 8) * config.scale;

        // History button area (screen-space calculation)
        String histLabel = "[H]";
        int histW = client.textRenderer.getWidth(histLabel) + 6;
        int histScreenX = (int)(config.x + (scaledW - histW - 2) * config.scale);
        int histScreenW = (int)(histW * config.scale);
        int histScreenH = (int)(HEADER_HEIGHT * config.scale);
        boolean inHistoryButton = inHeader && mx >= histScreenX && mx <= histScreenX + histScreenW && my >= config.y && my <= config.y + histScreenH;

        if (leftDown) {
            if (!dragging && !resizing) {
                if (inHistoryButton) {
                    client.execute(() -> client.setScreen(new HistoryScreen(eventManager)));
                    return;
                } else if (inResize) {
                    resizing = true;
                    resizeStartW = config.width;
                    resizeStartH = config.height;
                    dragStartX = mx;
                    dragStartY = my;
                } else if (inHeader) {
                    dragging = true;
                    dragStartX = mx;
                    dragStartY = my;
                    dragStartConfigX = config.x;
                    dragStartConfigY = config.y;
                }
            }

            if (dragging) {
                config.x = (int)(dragStartConfigX + (mx - dragStartX));
                config.y = (int)(dragStartConfigY + (my - dragStartY));
                configManager.markDirty();
            }

            if (resizing) {
                int newW = (int)(resizeStartW + (mx - dragStartX));
                int newH = (int)(resizeStartH + (my - dragStartY));
                config.width = Math.max(100, newW);
                config.height = Math.max(50, newH);
                configManager.markDirty();
            }
        } else {
            if (dragging) {
                dragging = false;
                configManager.save();
            }
            if (resizing) {
                resizing = false;
                configManager.save();
            }

            // Check coordinate click (must account for scale)
            if (inBounds) {
                for (ClickableZone zone : clickZones) {
                    double zoneScreenX1 = config.x + zone.x1 * config.scale;
                    double zoneScreenX2 = config.x + zone.x2 * config.scale;
                    double zoneScreenY1 = config.y + zone.y1 * config.scale;
                    double zoneScreenY2 = config.y + zone.y2 * config.scale;
                    if (mx >= zoneScreenX1 && mx <= zoneScreenX2 && my >= zoneScreenY1 && my <= zoneScreenY2) {
                        copyToClipboard(zone.text);
                        break;
                    }
                }
            }
        }
    }

    private void copyToClipboard(String text) {
        MinecraftClient.getInstance().keyboard.setClipboard(text);
        notificationText = "Coordinates copied: " + text;
        lastNotificationTime = System.currentTimeMillis();
    }

    private record ClickableZone(int x1, int y1, int x2, int y2, String text) {}
}
