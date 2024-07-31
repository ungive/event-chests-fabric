package net.snrk.eventchests.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import net.snrk.eventchests.EventChestsModClient;
import net.snrk.eventchests.SignContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class SignContentMixin {

    private static final int X_OFFSET = 4;
    private static final int Y_OFFSET = 4;
    private static final int PADDING = 4;
    private static final float WIDTH_TO_HEIGHT_RATIO = 2.f; // 2:1
    private static final int COLOR_BLACK = 0;

    @Inject(method = "render", at = @At("RETURN"), cancellable = true)
    public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (SignContent.hasSavedSignContent()) {
            int boxWidth = 0;
            for (int i = 0; i < SignContent.SIGN_LINE_COUNT; i++) {
                SignContent.Line content = SignContent.getSavedSignContentLine(i);
                // FIXME Use .text instead of chars
                String contentString = SignContent.characterListToString(content.chars);
                boxWidth = Math.max(boxWidth, client.textRenderer.getWidth(contentString));
            }

            int boxHeight = PADDING * 2 + client.textRenderer.fontHeight * SignContent.SIGN_LINE_COUNT;
            boxWidth = Math.max((int)(boxHeight * WIDTH_TO_HEIGHT_RATIO), boxWidth);

            context.fill(X_OFFSET, Y_OFFSET, X_OFFSET + boxWidth + 2 * PADDING,
                    Y_OFFSET + SignContent.SIGN_LINE_COUNT * client.textRenderer.fontHeight + 2 * PADDING,
                    0xBBb09b75);

            int baseXOffset = X_OFFSET + PADDING;
            int maxX = baseXOffset + boxWidth;
            for (int i = 0; i < SignContent.SIGN_LINE_COUNT; i++) {
                int xOffset = 0;
                int yOffset = client.textRenderer.fontHeight * i;
                SignContent.Line content = SignContent.getSavedSignContentLine(i);
                int textWidth = client.textRenderer.getWidth(SignContent.characterListToString(content.chars));
                for (SignContent.Character c : content.chars) {
                    String value = new String(Character.toChars(c.codePoint));
                    int x = baseXOffset + (int)((boxWidth - textWidth) / 2d) + xOffset;
                    int y = Y_OFFSET + PADDING + yOffset;
                    int color = SignContent.getSavedSignDyeColor().getMapColor().color;
                    TextColor textColor = c.style.getColor();
                    if (textColor != null) {
                        color = textColor.getRgb();
                    }
                    context.drawText(client.textRenderer, value, x, y, color, false);
                    int charWidth = client.textRenderer.getWidth(value);
                    maxX = Math.max(maxX, x + charWidth);
                    xOffset += charWidth;
                }
            }

            context.drawItem(new ItemStack(Items.OAK_SIGN), maxX + PADDING * 2, Y_OFFSET);
        }
    }
}
