package net.snrk.eventchests;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SignContent {

    public static class Character {
        public int codePoint;
        public Style style;

        public Character(int codePoint, Style style) {
            this.codePoint = codePoint;
            this.style = style;
        }
    }

    public static class Line {
        public Text text;
        public ArrayList<Character> chars;

        public Line(Text text, ArrayList<Character> chars) {
            this.text = text;
            this.chars = chars;
        }
    }

    public static final int SIGN_LINE_COUNT = 4;

    private static ArrayList<Line> savedSignContent = null;

    private static SignBlockEntity getSignAtCrosshair() {
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult hit = client.crosshairTarget;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos blockPos = blockHit.getBlockPos();
            if (client.world == null) {
                return null;
            }
            BlockEntity blockEntity = client.world.getBlockEntity(blockPos);
            if (blockEntity instanceof SignBlockEntity signBlockEntity) {
                return signBlockEntity;
            }
        }
        return null;
    }

    public static boolean isPlayerLookingAtSign() {
        return getSignAtCrosshair() != null;
    }

    public static void handleSignAtCrosshair() {
        SignBlockEntity sign = getSignAtCrosshair();
        if (sign != null) {
            boolean isEmpty = true;
            for (int i = 0; i < SIGN_LINE_COUNT; i++) {
                int length = sign.getTextOnRow(i, false).getString().length();
                if (length > 0) {
                    isEmpty = false;
                    break;
                }
            }

            if (hasSavedSignContent()) {
                if (isEmpty) {
                    restoreSignContentAtCrosshair();
                }
                else {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        EventChestsMod.printFeedback("Das Schild ist bereits beschriftet.");
                    }
                }
            }
            if (!hasSavedSignContent() && !isEmpty) {
                saveSignContentAtCrosshair();
            }
        }
    }

    public static void saveSignContentAtCrosshair() {
        SignBlockEntity sign = getSignAtCrosshair();
        if (sign != null) {
            recreateSavedSignContent();
            for (int i = 0; i < SIGN_LINE_COUNT; i++) {
                Text text = sign.getTextOnRow(i, false);
                Line line = savedSignContent.get(i);
                line.text = text;
                text.asOrderedText().accept((index, style, codePoint) -> {
                    line.chars.add(new Character(codePoint, style));
                    return true;
                });
            }
        }
    }

    public static void restoreSignContentAtCrosshair() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        for (int i = 0; i < SIGN_LINE_COUNT; i++) {
            Line line = getSavedSignContentLine(i);
            String lineFormatted = line.text.getString().replace('\u00A7', '&');
            if (lineFormatted.length() > 0) {
                String message = String.format("/editsign set %d %s", i + 1, lineFormatted);
                EventChestsMod.LOGGER.debug(String.format("Sending command: %s", message));
                client.player.sendChatMessage(message);
            }
        }
        clearSavedSignContent();
    }

    public static boolean hasSavedSignContent() {
        return savedSignContent != null;
    }

    public static Line getSavedSignContentLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= savedSignContent.size()) {
            throw new IndexOutOfBoundsException();
        }
        return savedSignContent.get(lineIndex);
    }

    public static String characterListToString(List<Character> characters) {
        StringBuilder sb = new StringBuilder();
        for (Character c : characters) {
            sb.append(java.lang.Character.toChars(c.codePoint));
        }
        return sb.toString();
    }

    public static void clearSavedSignContent() {
        savedSignContent = null;
    }

    private static void recreateSavedSignContent() {
        clearSavedSignContent();
        savedSignContent = new ArrayList<>(SIGN_LINE_COUNT);
        for (int i = 0; i < SIGN_LINE_COUNT; i++) {
            savedSignContent.add(new Line(null, new ArrayList<>(24)));
        }
    }
}
