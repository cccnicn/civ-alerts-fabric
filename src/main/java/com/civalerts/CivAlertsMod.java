package com.civalerts;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CivAlertsMod implements ClientModInitializer {
    public static final String MOD_ID = "civ-alerts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("CivAlerts mod initialized.");
        // Stage 2-4 will wire up ChatInterceptor, EventManager, HudRenderer, etc.
    }
}
