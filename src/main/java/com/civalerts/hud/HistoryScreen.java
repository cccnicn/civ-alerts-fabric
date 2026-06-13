package com.civalerts.hud;

import com.civalerts.event.CivEvent;
import com.civalerts.event.CivEventType;
import com.civalerts.event.EventManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryScreen extends Screen {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final int LINE_HEIGHT = 14;
    private static final int PADDING = 10;
    private static final int HEADER_HEIGHT = 30;
    private static final int EVENT_PADDING = 6;

    private final EventManager eventManager;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private List<CivEvent> history;

    public HistoryScreen(EventManager eventManager) {
        super(Text.literal("CivAlerts History"));
        this.eventManager = eventManager;
        this.history = eventManager.getHistory();
    }

    @Override
    protected void init() {
        super.init();
        refreshHistory();
    }

    private void refreshHistory() {
        this.history = eventManager.getHistory();
        calculateMaxScroll();
    }

    private void calculateMaxScroll() {
        int contentHeight = history.size() * (LINE_HEIGHT + EVENT_PADDING) + HEADER_HEIGHT + PADDING * 2;
        int viewHeight = this.height - PADDING * 2;
        this.maxScroll = Math.max(0, contentHeight - viewHeight);
        this.scrollOffset = Math.min(this.scrollOffset, this.maxScroll);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Background
        int bgColor = 0xDD222222;
        context.fill(PADDING, PADDING, this.width - PADDING, this.height - PADDING, bgColor);

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, PADDING + 6, 0xFFFFFF);

        // Header separator
        context.fill(PADDING, PADDING + HEADER_HEIGHT, this.width - PADDING, PADDING + HEADER_HEIGHT + 1, 0xFF555555);

        // Column headers
        int colY = PADDING + HEADER_HEIGHT + 5;
        int typeColX = PADDING + 10;
        int timeColX = PADDING + 140;
        int textColX = PADDING + 230;

        context.drawTextWithShadow(this.textRenderer, Text.literal("Type"), typeColX, colY, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Timestamp"), timeColX, colY, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Event Details"), textColX, colY, 0xAAAAAA);

        // Events list
        int contentY = colY + LINE_HEIGHT + 4;
        int contentWidth = this.width - PADDING * 2 - 10;
        int eventStartY = contentY - scrollOffset;

        for (int i = 0; i < history.size(); i++) {
            CivEvent event = history.get(i);
            int eventY = eventStartY + i * (LINE_HEIGHT + EVENT_PADDING);

            // Skip if out of visible bounds
            if (eventY + LINE_HEIGHT < PADDING + HEADER_HEIGHT + 5 || eventY > this.height - PADDING) {
                continue;
            }

            // Event type with color
            String typeStr = switch (event.type()) {
                case SCOUT -> "[Scout]";
                case BORDER_ENTRANCE -> "[Border Ent]";
                case BORDER_EXIT -> "[Border Exit]";
                default -> "[Unknown]";
            };
            int typeColor = switch (event.type()) {
                case SCOUT -> 0x44FF44;
                case BORDER_ENTRANCE -> 0xFF4444;
                case BORDER_EXIT -> 0x4444FF;
                default -> 0xFFFFFF;
            };
            context.drawTextWithShadow(this.textRenderer, Text.literal(typeStr).styled(s -> s.withColor(typeColor)), typeColX, eventY, typeColor);

            // Timestamp
            String timeStr = TIME_FORMAT.format(Instant.ofEpochMilli(event.timestamp()));
            context.drawTextWithShadow(this.textRenderer, Text.literal(timeStr), timeColX, eventY, 0xCCCCCC);

            // Full text (truncated if needed)
            String fullText = event.text();
            int maxTextWidth = contentWidth - (textColX - PADDING);
            int textWidth = this.textRenderer.getWidth(fullText);
            if (textWidth > maxTextWidth) {
                // Truncate with ellipsis
                int visibleChars = this.textRenderer.getWidth(fullText);
                while (visibleChars > 0 && this.textRenderer.getWidth(fullText.substring(0, Math.min(visibleChars, fullText.length()))) > maxTextWidth - 15) {
                    visibleChars = Math.max(0, visibleChars - 1);
                }
                fullText = fullText.substring(0, Math.min(visibleChars, fullText.length())) + "...";
            }
            context.drawTextWithShadow(this.textRenderer, Text.literal(fullText), textColX, eventY, 0xEEEEEE);
        }

        // Scrollbar
        if (maxScroll > 0) {
            int scrollbarWidth = 6;
            int scrollbarX = this.width - PADDING - scrollbarWidth;
            int barTop = PADDING + HEADER_HEIGHT + 5;
            int barHeight = this.height - PADDING * 2 - HEADER_HEIGHT - 5;
            int thumbHeight = Math.max(20, barHeight * (this.height - PADDING * 2) / (barHeight + maxScroll));
            int thumbY = barTop + (barHeight - thumbHeight) * scrollOffset / maxScroll;

            context.fill(scrollbarX, barTop, scrollbarX + scrollbarWidth, barTop + barHeight, 0x33666666);
            context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xFF888888);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) {
            scrollOffset -= (int) (verticalAmount * LINE_HEIGHT * 3);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
