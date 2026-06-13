package com.civalerts;

import com.civalerts.chat.ChatInterceptor;
import com.civalerts.config.ConfigManager;
import com.civalerts.event.EventManager;
import com.civalerts.hud.HistoryScreen;
import com.civalerts.hud.HudRenderer;
import com.civalerts.keybinding.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CivAlertsMod implements ClientModInitializer {
    public static final String MOD_ID = "civ-alerts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static EventManager eventManager;
    private static ConfigManager configManager;
    private static HudRenderer hudRenderer;

    @Override
    public void onInitializeClient() {
        LOGGER.info("CivAlerts mod initializing...");

        configManager = new ConfigManager();
        eventManager = new EventManager();

        ChatInterceptor chatInterceptor = new ChatInterceptor(eventManager);
        chatInterceptor.register();

        KeyBindings.register();

        hudRenderer = new HudRenderer(configManager, eventManager);
        hudRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            eventManager.cleanOldEvents();

            if (KeyBindings.TOGGLE_HUD.wasPressed()) {
                var config = configManager.getData();
                config.visible = !config.visible;
                configManager.save();
                LOGGER.info("HUD toggled: visible={}", config.visible);
            }

            if (KeyBindings.OPEN_HISTORY.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new HistoryScreen(eventManager));
                LOGGER.info("History screen opened");
            }
        });

        LOGGER.info("CivAlerts mod initialized.");
    }

    public static EventManager getEventManager() {
        return eventManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static HudRenderer getHudRenderer() {
        return hudRenderer;
    }
}
