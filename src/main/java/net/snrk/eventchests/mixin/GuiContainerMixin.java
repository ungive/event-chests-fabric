/**
 * MIT License
 *
 * Copyright (c) 2022 Jonas van den Berg
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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreens.class)
public class GuiContainerMixin {
    
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(method="open", at=@At("HEAD"), cancellable = true)
    private static void checkChestScreen(ScreenHandlerType type, MinecraftClient client, 
            int any, Text component, CallbackInfo ci) {
        LOGGER.debug("Trying to open container: "+type+" with name "+component.getString());
        if (type == ScreenHandlerType.GENERIC_9X1 
        ||  type == ScreenHandlerType.GENERIC_9X2
        ||  type == ScreenHandlerType.GENERIC_9X3
        ||  type == ScreenHandlerType.GENERIC_9X4
        ||  type == ScreenHandlerType.GENERIC_9X5
        ||  type == ScreenHandlerType.GENERIC_9X6) {
            GenericContainerScreenHandler container = (GenericContainerScreenHandler) type.create(any, client.player.getInventory());
            client.player.currentScreenHandler = container;
            LOGGER.debug("(my chest)");
        } else if (type == ScreenHandlerType.SHULKER_BOX) {
            ShulkerBoxScreenHandler container = ScreenHandlerType.SHULKER_BOX.create(any, client.player.getInventory());
            client.player.currentScreenHandler = container;
            LOGGER.debug("(my shulker)");
        } else {
            LOGGER.debug("(not me)");
        }
    }
}
