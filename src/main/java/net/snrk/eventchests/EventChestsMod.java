package net.snrk.eventchests;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventChestsMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("event-chests");

    private static final String category = "key.categories.testchestmoves";

    public static KeyBinding keyMoveChest;

    @Override
    public void onInitialize() {
        keyMoveChest = registerKey("movechestcontent", GLFW.GLFW_KEY_I);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // TODO
        });
    }

    private KeyBinding registerKey(String key, int code) {
        KeyBinding result = new KeyBinding("key.easierchests." + key, code, category);
        KeyBindingHelper.registerKeyBinding(result);
        return result;
    }
}
