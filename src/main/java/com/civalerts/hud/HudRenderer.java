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

    private void render(DrawContext context, RenderTickCounter tickCounter) {
        ConfigData config = configManager.getData();
        if (!config.visible) return;

        updateInteraction(config);

        context.getMatrices().push();
        context.getMatrices().translate(config.x, config.y, 0);
        context.getMatrices().scale(config.scale, config.scale, 1f);

        int scaledW = (int)(config.width / config.scale);
        int scaledH = (int)(config.height / config.scale);

        int bg = ((int)(255 * config.opacity) << 24) | 0x222222;
        int headerColor = 0x88001177;
        int borderColor = 0xFF555555;

        // Background
        context.fill(0, 0, scaledW, scaledH, bg);

        // Header
        context.fill(0, 0, scaledW, HEADER_HEIGHT, headerColor);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("CivAlerts"), 4, 4, 0xFFFFFF, false);

        // Content area
        int contentY = HEADER_HEIGHT + 2;
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
                if (display.length() > (colWidth / 6)) {
                    display = display.substring(0, (colWidth / 6)) + "...";
                }

                context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(display), colX + 2, y, 0xFFFFFF, false);

                // Track coordinates for click
                for (String coord : event.coordinates()) {
                    int coordX = colX + 2 + MinecraftClient.getInstance().textRenderer.getWidth(display.substring(0, display.indexOf(coord)));
                    int coordY = y;
                    clickZones.add(new ClickableZone(coordX, coordY, coordX + MinecraftClient.getInstance().textRenderer.getWidth(coord), coordY + lineH, coord));
                }

                y += lineH;
            }
        }

        // Resize handle
        context.fill(scaledW - 8, scaledH - 8, scaledW, scaledH, 0xFFAAAAAA);

        context.getMatrices().pop();

        // Notification
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

        double mx, my;
        // Try to get scaled mouse coordinates
        mx = client.mouse.getX() / client.getWindow().getScaleFactor();
        my = client.mouse.getY() / client.getWindow().getScaleFactor();

        boolean leftDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        int scaledW = (int)(config.width / config.scale);
        int scaledH = (int)(config.height / config.scale);

        boolean inBounds = mx >= config.x && mx <= config.x + scaledW && my >= config.y && my <= config.y + scaledH;
        boolean inHeader = inBounds && my < config.y + HEADER_HEIGHT;
        boolean inResize = inBounds && mx > config.x + scaledW - 8 && my > config.y + scaledH - 8;

        if (leftDown) {
            if (!dragging && !resizing) {
                if (inResize) {
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
                configManager.save();
            }

            if (resizing) {
                int newW = (int)(resizeStartW + (mx - dragStartX) * config.scale);
                int newH = (int)(resizeStartH + (my - dragStartY) * config.scale);
                config.width = Math.max(100, newW);
                config.height = Math.max(50, newH);
                configManager.save();
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

            // Check coordinate click
            if (inBounds) {
                for (ClickableZone zone : clickZones) {
                    if (mx >= config.x + zone.x1 && mx <= config.x + zone.x2 && my >= config.y + zone.y1 && my <= config.y + zone.y2) {
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
