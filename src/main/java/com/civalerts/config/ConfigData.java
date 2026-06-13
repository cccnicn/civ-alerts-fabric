package com.civalerts.config;

public class ConfigData {
    public int x = 10;
    public int y = 10;
    public int width = 300;
    public int height = 200;
    public float scale = 1.0f;
    public float opacity = 0.8f;
    public boolean visible = true;

    public ConfigData() {}

    public void validate() {
        if (width < 100) width = 100;
        if (height < 50) height = 50;
        if (scale < 0.5f) scale = 0.5f;
        if (scale > 3.0f) scale = 3.0f;
        if (opacity < 0.1f) opacity = 0.1f;
        if (opacity > 1.0f) opacity = 1.0f;
    }
}
