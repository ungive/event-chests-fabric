package net.snrk.eventchests;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.snrk.eventchests.interfaces.SlotClicker;

public class ChestContent {

    public static final int PLAYER_INVENTORY_START_INDEX = 9;
    public static final int INVENTORY_SWAP_SLOT_COUNT = 27;

    public static void swapInventoryContents(HandledScreen<?> screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        if (!(screen instanceof SlotClicker slotClicker)) {
            return;
        }

        Inventory containerInventory = screen.getScreenHandler().getSlot(0).inventory;
        Inventory playerInventory = client.player.getInventory();

        boolean isEmptyContainer = true;
        for (int i = 0; i < INVENTORY_SWAP_SLOT_COUNT; i++) {
            ItemStack stack = containerInventory.getStack(i);
            if (stack.getCount() != 0) {
                isEmptyContainer = false;
                break;
            }
        }

        if (!isEmptyContainer) {
            for (int i = 0; i < INVENTORY_SWAP_SLOT_COUNT; i++) {
                int playerIndex = getPlayerSwapIndex(i);
                ItemStack stack = playerInventory.getStack(playerIndex);
                if (stack.getCount() > 0) {
                    client.player.sendMessage(new LiteralText("Make sure you have enough space in your inventory."), false);
                    return;
                }
            }
        }

        // NOTE One of the containers is always empty at this point.

        for (int i = 0; i < INVENTORY_SWAP_SLOT_COUNT; i++) {
            int playerIndex = getPlayerSwapIndex(i);
            ItemStack playerStack = playerInventory.getStack(playerIndex);
            ItemStack containerStack = containerInventory.getStack(i);
            playerInventory.setStack(playerIndex, containerStack);
            containerInventory.setStack(i, playerStack);

            int playerSlot = slotClicker.EventChests$slotIndexfromPlayerInventoryIndex(playerIndex);

            if (isEmptyContainer) {
                pickupSlot(slotClicker, playerSlot);
                pickupSlot(slotClicker, i);
            }
            else {
                pickupSlot(slotClicker, i);
                pickupSlot(slotClicker, playerSlot);
            }
        }
    }

    private static void pickupSlot(SlotClicker slotClicker, int index) {
        slotClicker.EventChests$onMouseClick(null, index, 0, SlotActionType.PICKUP);
    }

    private static int getPlayerSwapIndex(int index) {
        return PLAYER_INVENTORY_START_INDEX + index;
    }
}
