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
            } catch (IOException e) {
                data = new ConfigData();
            }
        } else {
            data = new ConfigData();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
