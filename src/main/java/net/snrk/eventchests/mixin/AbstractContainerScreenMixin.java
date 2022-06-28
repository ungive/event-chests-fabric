/**
 * MIT License
 *
 * Copyright (c) 2022 Snrk
 * Copyright (c) 2020 Guntram Blohm
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.snrk.eventchests.mixin;

import net.snrk.eventchests.ChestContent;
import net.snrk.eventchests.EventChestsMod;
import net.snrk.eventchests.interfaces.SlotClicker;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen implements SlotClicker {

    private static final int PLAYERSLOTS = 36;      // # of slots in player inventory -> so not in container
    private static int PLAYERINVCOLS = 9;           // let's not make those final; maybe we'll need compatibility
    private static int PLAYERINVROWS = 4;           // with some mod at some point which changes these.

    @Shadow protected void onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType) {}
    @Shadow @Final protected ScreenHandler handler;

    protected AbstractContainerScreenMixin() { super(null); }

    @Override
    public void EventChests$onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType) {
        this.onMouseClick(slot, invSlot, button, slotActionType);
    }
    
    @Override
    public int EventChests$getPlayerInventoryStartIndex() {
        if (handler instanceof PlayerScreenHandler) {
            return PLAYERINVCOLS;
        } else {
            return this.handler.slots.size()-PLAYERSLOTS;
        }
    }
    
    @Override
    public int EventChests$playerInventoryIndexFromSlotIndex(int slot) {
        int firstSlot = EventChests$getPlayerInventoryStartIndex();
        if (slot < firstSlot) {
            return -1;
        } else if (slot < firstSlot + (PLAYERSLOTS-PLAYERINVCOLS)) {
            return slot - firstSlot + PLAYERINVCOLS;
        } else {
            return slot - firstSlot - (PLAYERSLOTS-PLAYERINVCOLS);
        }
    }
    
    @Override
    public int EventChests$slotIndexfromPlayerInventoryIndex(int slot) {
        int firstSlot = EventChests$getPlayerInventoryStartIndex();
        if (slot < PLAYERINVCOLS) {
            return slot + firstSlot + (PLAYERSLOTS-PLAYERINVCOLS);
        } else {
            return slot + firstSlot - PLAYERINVCOLS;
        }
    }

    @Inject(method="keyPressed", at=@At("HEAD"), cancellable=true)
    public void EasierChests$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return;
        }

        HandledScreen<?> handledScreen = (HandledScreen<?>)(Screen)this;

        if (EventChestsMod.keyInteract.matchesKey(keyCode, scanCode)) {
            ChestContent.swapInventoryContents(handledScreen);
            cir.setReturnValue(true);
            cir.cancel();
        }

        if (true) {
            return;
        }

        /*if (ExampleMod.keySortPlInv.matchesKey(keyCode, scanCode)) {
            ExtendedGuiChest.sortInventory(this, false, MinecraftClient.getInstance().player.getInventory());
            cir.setReturnValue(true);
            cir.cancel();
        } else if (!isSupportedScreenHandler(handler)) {
            return;
        } else if (ExampleMod.keyMoveToChest.matchesKey(keyCode, scanCode)) {
            ExtendedGuiChest.moveMatchingItems(handledScreen, false);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (ExampleMod.keySortChest.matchesKey(keyCode, scanCode)) {
            ExtendedGuiChest.sortInventory(this, true, handler.getSlot(0).inventory);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (ExampleMod.keyMoveToPlInv.matchesKey(keyCode, scanCode)) {
            ExtendedGuiChest.moveMatchingItems(handledScreen, true);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (ExampleMod.keySearchBox.matchesKey(keyCode, scanCode)) {
            cir.setReturnValue(true);
            cir.cancel();
        }*/
    }

    private boolean loggedScreenHandlerClass = false;
    public boolean isSupportedScreenHandler(ScreenHandler handler) {
        if (handler == null) {      // can this happen? Make IDE happy
            return false;
        }

        if (handler instanceof GenericContainerScreenHandler || handler instanceof ShulkerBoxScreenHandler) {
            return true;
        }
        
        if (!loggedScreenHandlerClass && !handler.getClass().getSimpleName().startsWith("class_")) {    // don't log MC internal classes
            LogManager.getLogger(this.getClass()).info("opening class "+handler.getClass().getSimpleName() + "/" + handler.getClass().getCanonicalName());
            loggedScreenHandlerClass = true;
        }
        return false;
    }
    
    /**
     * Gets the number of inventory rows in the Chest inventory. 
     * This does not include the PLAYERINVROWS rows in the player inventory.
     * @return the number of inventory rows
     */
    
    /*public int getSlotRowCount() {
        int size = handler.slots.size() - PLAYERSLOTS;
        if (false) {
            return size / getSlotColumnCount();
        }
        return Math.min(6, size/PLAYERINVCOLS);
    }*/
    
    /*public int getSlotColumnCount() {
        int size = handler.slots.size() - PLAYERSLOTS;
        if (false) {
            return (size <= 81 ? PLAYERINVCOLS : size/PLAYERINVCOLS);
        }
        return PLAYERINVCOLS;
    }*/
}
