package com.civalerts.keybinding;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding TOGGLE_HUD;
    public static KeyBinding OPEN_HISTORY;

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("civalerts", "main"));

    public static void register() {
        TOGGLE_HUD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.civalerts.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                CATEGORY
        ));

        OPEN_HISTORY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.civalerts.open_history",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                CATEGORY
        ));
    }
}
