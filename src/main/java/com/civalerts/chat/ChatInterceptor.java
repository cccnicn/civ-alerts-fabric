package com.civalerts.chat;

import com.civalerts.event.CivEventType;
import com.civalerts.event.EventManager;
import com.civalerts.util.HoverEventParser;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChatInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger("civ-alerts");
    private final EventManager eventManager;

    public ChatInterceptor(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void register() {
        ClientReceiveMessageEvents.GAME.register(this::onGameMessage);
        LOGGER.info("ChatInterceptor registered.");
    }

    private void onGameMessage(Text text, boolean overlay polygon) {
        HoverEventParser.ParsedReport report = HoverEventParser.parse(text);
        if (report == null) {
            return;
        }

        LOGGER.info("Intercepted Civ report type={} reportedCount={} parsedEntries={}",
                report.type(), report.reportedCount(), report.entries().size());

        if (report.entries().isEmpty()) {
            LOGGER.warn("HoverEvent had no entries for report: {}", text.getString());
            return;
        }

        for (String entry : report.entries()) {
            List<String> coords = HoverEventParser.extractCoordinates(entry);
            eventManager.addEvent(report.type(), entry, coords);
        }
    }
}
