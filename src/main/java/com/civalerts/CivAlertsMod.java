package com.civalerts;

import com.civalerts.chat.ChatInterceptor;
import com.civalerts.event.EventManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CivAlertsMod implements ClientModInitializer {
    public static final String MOD_ID = "civ-alerts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static EventManager eventManager;

    @Override
    public void onInitializeClient() {
        LOGGER.info("CivAlerts mod initializing...");

        eventManager = new EventManager();

        ChatInterceptor chatInterceptor = new ChatInterceptor(eventManager);
        chatInterceptor.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            eventManager.cleanOldEvents();
        });

        LOGGER.info("CivAlerts mod initialized.");
    }

    public static EventManager getEventManager() {
        return eventManager;
    }
}
