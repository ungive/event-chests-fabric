package net.snrk.eventchests;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

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

    private static DyeColor savedSignDyeColor = null;
    private static ArrayList<Line> savedSignContent = null;

    private static final java.lang.Character FORMAT_CHAR = '\u00A7';
    private static final java.lang.Character ALT_FORMAT_CHAR = '&';

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
            // TODO what about back text?
            var signText = sign.getFrontText();

            boolean isEmpty = true;
            for (int i = 0; i < SIGN_LINE_COUNT; i++) {
                int length = signText.getMessage(i, false).getString().length();
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
                        EventChestsModClient.printFeedback("Kann gespeicherten Text nicht anwenden. Das Schild ist bereits beschriftet.");
                    }
                }
            }
            else {
                if (isEmpty) {
                    EventChestsModClient.printFeedback("Kann keinen Text speichern. Das Schild ist unbeschriftet.");
                }
                else {
                    saveSignContentAtCrosshair();
                }
            }

            DyeColor dye = signText.getColor();
            if (dye != DyeColor.BLACK) {
                String dyeId = dye.getName() + "_dye";
                String dyeText = formatForColorName("white") + FORMAT_CHAR + "o" + dyeId;
                EventChestsModClient.printFeedback("Verwendeter Farbstoff: " + dyeText);
                MinecraftClient instance = MinecraftClient.getInstance();
                if (instance.player != null) {
                    if (instance.interactionManager != null && instance.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                        // TODO is getName correct? previously getEntityName
                        var x = instance.player.getName().getLiteralString();
                        EventChestsModClient.sendCommand("give " + instance.player.getName().getLiteralString() + " " + dyeId);
                    }
                    else {
                        EventChestsModClient.printFeedback("Wechsle in den Creative um den Farbstoff automatisch zu erhalten.");
                    }
                }
            }
        }
    }

    public static void saveSignContentAtCrosshair() {
        SignBlockEntity sign = getSignAtCrosshair();
        if (sign != null) {
            recreateSavedSignContent();

            // TODO what about back text?
            var signText = sign.getFrontText();

            savedSignDyeColor = signText.getColor();
            for (int i = 0; i < SIGN_LINE_COUNT; i++) {
                Text text = signText.getMessage(i, false);
                Line line = savedSignContent.get(i);
                line.text = text;
                text.asOrderedText().accept((index, style, codePoint) -> {
                    line.chars.add(new Character(codePoint, style));
                    return true;
                });
            }
        }
    }

    private static String formatForColorName(String colorName) {
        String code = switch (colorName) {
            case "dark_red" -> "4";
            case "red" -> "c";
            case "gold" -> "6";
            case "yellow" -> "e";
            case "dark_green" -> "2";
            case "green" -> "a";
            case "aqua" -> "b";
            case "dark_aqua" -> "3";
            case "dark_blue" -> "1";
            case "blue" -> "9";
            case "light_purple" -> "d";
            case "dark_purple" -> "5";
            case "white" -> "f";
            case "gray" -> "7";
            case "dark_gray" -> "8";
            case "black" -> "0";
            default -> null;
        };
        return code != null ? FORMAT_CHAR + code : "";
    }

    private static String formatPrefixForStyle(Style style) {
        StringBuilder prefixes = new StringBuilder();
        if (style.getColor() != null) {
            String colorName = style.getColor().getName();
            prefixes.append(formatForColorName(colorName));
        }
        if (style.isObfuscated()) prefixes.append(FORMAT_CHAR).append('k');
        if (style.isBold()) prefixes.append(FORMAT_CHAR).append('l');
        if (style.isStrikethrough()) prefixes.append(FORMAT_CHAR).append('m');
        if (style.isUnderlined()) prefixes.append(FORMAT_CHAR).append('n');
        if (style.isItalic()) prefixes.append(FORMAT_CHAR).append('o');
        return prefixes.toString();
    }

    private static String normalizeText(Text text) {
        StringBuilder value = new StringBuilder();
        if (!text.getSiblings().isEmpty()) {
            boolean addedPrefix = false;
            for (Text sibling : text.getSiblings()) {
                if (!sibling.getSiblings().isEmpty()) {
                    EventChestsModClient.LOGGER.error("Unexpected siblings of a sibling.");
                }
                if (!sibling.getString().isEmpty()) {
                    if (!addedPrefix) {
                        // add the potentially unformatted prefix
                        int index = text.getString().indexOf(sibling.getString());
                        if (index >= 0) {
                            String prefix = text.getString().substring(0, index);
                            value.append(prefix);
                        }
                        else {
                            value.append(text.getString());
                        }
                        addedPrefix = true;
                    }
                    Style style = sibling.getStyle();
                    value.append(formatPrefixForStyle(style));
                    value.append(sibling.getString());
                    value.append(FORMAT_CHAR).append('r');
                }
            }
        }
        else if (!text.getStyle().isEmpty() && !text.getString().isEmpty()) {
            value.append(formatPrefixForStyle(text.getStyle()));
            value.append(text.getString());
            value.append(FORMAT_CHAR).append('r');
        }
        else {
            value.append(text.getString());
        }
        return value.toString().replace(FORMAT_CHAR, ALT_FORMAT_CHAR);
    }

    public static void restoreSignContentAtCrosshair() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        for (int i = 0; i < SIGN_LINE_COUNT; i++) {
            Line line = getSavedSignContentLine(i);
            String lineFormatted = normalizeText(line.text);
            if (lineFormatted.length() > 0) {
                String command = String.format("editsign set %d %s", i + 1, lineFormatted);
                EventChestsModClient.LOGGER.info(String.format("Sending command: %s", command));
                EventChestsModClient.sendCommand(command);
            }
        }
        clearSavedSignContent();
    }

    public static boolean hasSavedSignContent() {
        return savedSignContent != null;
    }

    public static DyeColor getSavedSignDyeColor() {
        return savedSignDyeColor;
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
        savedSignDyeColor = null;
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
