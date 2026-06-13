package com.civalerts.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("civ-alerts.json");
    private ConfigData data;
    private boolean needsSave = false;
    private long lastSaveTime = 0;
    private static final long SAVE_DEBOUNCE_MS = 500;

    public ConfigData getData() {
        if (data == null) {
            load();
        }
        return data;
    }

    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                data = GSON.fromJson(json, ConfigData.class);
                if (data == null) data = new ConfigData();
                data.validate();
            } catch (IOException e) {
                data = new ConfigData();
            }
        } else {
            data = new ConfigData();
        }
    }

    public void markDirty() {
        needsSave = true;
    }

    public void tickSave() {
        if (!needsSave) return;
        long now = System.currentTimeMillis();
        if (now - lastSaveTime < SAVE_DEBOUNCE_MS) return;
        save();
    }

    public void save() {
        if (data == null) return;
        data.validate();
        needsSave = false;
        lastSaveTime = System.currentTimeMillis();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            // Silently fail to avoid disrupting gameplay
        }
    }
}
