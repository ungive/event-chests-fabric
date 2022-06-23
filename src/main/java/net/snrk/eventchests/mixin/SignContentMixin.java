package net.snrk.eventchests.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.snrk.eventchests.SignContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGameHud.class)
public class SignContentMixin {

    private static final int X_OFFSET = 4;
    private static final int Y_OFFSET = 4;
    private static final int PADDING = 4;

    @Inject(method = "render", at = @At("RETURN"), cancellable = true)
    public void onRender(MatrixStack matrices, float tickDelta, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (SignContent.hasSavedSignContent()) {
            int boxWidth = 0;
            for (int i = 0; i < SignContent.SIGN_LINE_COUNT; i++) {
                SignContent.Line content = SignContent.getSavedSignContentLine(i);
                // FIXME Use .text instead of chars
                String contentString = SignContent.characterListToString(content.chars);
                boxWidth = Math.max(boxWidth, client.textRenderer.getWidth(contentString));
            }

            DrawableHelper.fill(matrices, X_OFFSET, Y_OFFSET, X_OFFSET + boxWidth + 2 * PADDING,
                    Y_OFFSET + SignContent.SIGN_LINE_COUNT * client.textRenderer.fontHeight + 2 * PADDING,
                    0x99FFFFFF);

            int maxX = 0;
            int maxY = 0;
            for (int i = 0; i < SignContent.SIGN_LINE_COUNT; i++) {
                int xOffset = 0;
                int yOffset = client.textRenderer.fontHeight * i;
                SignContent.Line content = SignContent.getSavedSignContentLine(i);
                int textWidth = client.textRenderer.getWidth(SignContent.characterListToString(content.chars));
                for (SignContent.Character c : content.chars) {
                    String value = new String(Character.toChars(c.codePoint));
                    int x = X_OFFSET + PADDING + (int)((boxWidth - textWidth) / 2d) + xOffset;
                    int y = Y_OFFSET + PADDING + yOffset;
                    int color = 0;
                    if (c.style.getColor() != null) {
                        color = c.style.getColor().getRgb();
                    }
                    if (color == 0) {
                        client.textRenderer.draw(matrices, value, x, y, color);
                    }
                    else {
                        client.textRenderer.drawWithShadow(matrices, value, x, y, color);
                    }
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    int charWidth = client.textRenderer.getWidth(value);
                    xOffset += charWidth;
                }
            }

            Item item = Registry.ITEM.get(new Identifier("minecraft", "oak_sign"));
            client.getItemRenderer().renderInGui(new ItemStack(RegistryEntry.of(item)), maxX + PADDING * 3, Y_OFFSET);
        }
    }
}
