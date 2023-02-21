package net.snrk.eventchests;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.annotation.Debug;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

public class EventChestsMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("event-chests");

    private static final String KEY_CATEGORY = "key.categories.eventchests";

    public static final String OUTPUT_PREFIX = "§f[§cEv§7ent§cCh§7ests§f]§r §d";

    public static KeyBinding keyInteract;

    @Override
    public void onInitialize() {
        keyInteract = registerKey("interact", GLFW.GLFW_KEY_X);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyInteract.wasPressed()) {
                if (SignContent.isPlayerLookingAtSign()) {
                    SignContent.handleSignAtCrosshair();
                }
                else if (SignContent.hasSavedSignContent()) {
                    SignContent.clearSavedSignContent();
                }
                else {
                    printFeedback("Öffne eine Kiste oder betrachte ein Schild.");
                }
            }
        });
    }

    public static void printFeedback(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.of(EventChestsMod.OUTPUT_PREFIX + text), false);
        }
    }

    public static void sendCommand(String command) {
        String modifiedCommand = command.startsWith("/") ? command.substring(1) : command;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            printFeedback("Befehl konnte nicht gesendet werden.");
            return;
        }
        client.player.sendCommand(modifiedCommand);
    }

    private KeyBinding registerKey(String key, int code) {
        KeyBinding result = new KeyBinding("key.eventchests." + key, code, KEY_CATEGORY);
        KeyBindingHelper.registerKeyBinding(result);
        return result;
    }

    /* Useful during debug to make sure the mouse is visible/not captured when a breakpoint is hit.
     */
    @Debug
    @SuppressWarnings("unused")
    public static boolean _debuggerReleaseControl() {
        GLFW.glfwSetInputMode(MinecraftClient.getInstance().getWindow().getHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        return true;
    }
}
