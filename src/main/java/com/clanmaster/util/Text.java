package com.clanmaster.util;

import net.md_5.bungee.api.ChatColor;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small text helpers.
 */
public final class Text {

    private Text() {
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    /**
     * Translates ampersand color codes.
     */
    public static String color(String message) {
        if (message == null) return "";
        // Support '&#RRGGBB' shorthand by converting to '#RRGGBB'
        message = message.replace("&#", "#");
        // first convert hex (#RRGGBB) to ChatColor.of codes, then translate legacy '&'.
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    /**
     * Creates a simple left-to-right gradient using start/end hex colors.
     */
    public static String gradient(String startHex, String endHex, String text) {
        startHex = normalizeHex(startHex);
        endHex = normalizeHex(endHex);
        int length = text.length();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length; i++) {
            double ratio = length == 1 ? 0 : (double) i / (length - 1);
            int r = lerp(hex(startHex, 0), hex(endHex, 0), ratio);
            int g = lerp(hex(startHex, 2), hex(endHex, 2), ratio);
            int b = lerp(hex(startHex, 4), hex(endHex, 4), ratio);
            out.append(ChatColor.of(String.format("#%02x%02x%02x", r, g, b))).append(text.charAt(i));
        }
        return out.toString();
    }

    private static int hex(String hex, int idx) {
        return Integer.parseInt(hex.substring(idx, idx + 2), 16);
    }

    private static int lerp(int a, int b, double t) {
        return (int) Math.round(a + (b - a) * t);
    }

    private static String normalizeHex(String hex) {
        hex = hex.toLowerCase(Locale.ROOT);
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6) {
            return "ffffff";
        }
        return hex;
    }
}
