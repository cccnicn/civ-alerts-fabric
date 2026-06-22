package com.civalerts.chat;

import com.civalerts.event.CivEvent;
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
        // Register for GAME messages (system/server messages like /tellraw)
        ClientReceiveMessageEvents.GAME.register(this::onGameMessage);

        // Also register for CHAT messages - Civ servers may route [Civ] notifications through player chat
        ClientReceiveMessageEvents.CHAT.register((text, signedMessage, sender, messageType, timestamp) ->
                processMessage(text));

        LOGGER.info("ChatInterceptor registered (GAME + CHAT).");
    }

    private void onGameMessage(Text text, boolean overlay) {
        processMessage(text);
    }

    private void processMessage(Text text) {
        String plain = text.getString();
        LOGGER.info("ChatInterceptor received: {}", plain);

        HoverEventParser.ParsedReport report = HoverEventParser.parse(text);
        if (report == null) {
            return;
        }

        LOGGER.info("Intercepted type={} reportedCount={} parsedEntries={}",
                report.type(), report.reportedCount(), report.entries().size());

        if (report.entries().isEmpty()) {
            LOGGER.warn("HoverEvent had no entries for: {}", plain);
            return;
        }

        int idx = 0;
        for (String entry : report.entries()) {
            List<String> coords = HoverEventParser.extractCoordinates(entry);
            LOGGER.info("Adding event: type={} text='{}' coords={}", report.type(), entry, coords);
            eventManager.addEvent(new CivEvent(report.type(), entry, coords, idx++));
        }
    }
}
