package net.areofy.fyconnect;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Text {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private Text() {
    }

    public static String color(String text) {
        if (text == null) return "";
        return text.replace('&', '\u00A7');
    }

    public static net.kyori.adventure.text.Component component(String text) {
        return LEGACY.deserialize(text == null ? "" : text);
    }
}
