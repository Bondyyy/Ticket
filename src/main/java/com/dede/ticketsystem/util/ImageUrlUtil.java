package com.dede.ticketsystem.util;

import java.util.Locale;
import java.util.Set;

public final class ImageUrlUtil {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif", "svg");
    private static final String STATIC_ROOT = "src/main/resources/static/";

    private ImageUrlUtil() {
    }

    public static String normalizeImageUrl(String raw) {
        if (raw == null) {
            return null;
        }

        String clean = raw.trim();
        if (clean.isEmpty()) {
            return null;
        }

        clean = trimWrappingQuotes(clean).replace('\\', '/');
        String lower = clean.toLowerCase(Locale.ROOT);

        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("data:")) {
            return clean;
        }
        if (lower.startsWith("/images/") || lower.startsWith("/uploads/")) {
            return clean;
        }
        if (lower.startsWith("images/") || lower.startsWith("uploads/")) {
            return "/" + clean;
        }
        if (lower.startsWith("/static/images/")) {
            return clean.substring("/static".length());
        }
        if (lower.startsWith("static/images/")) {
            return "/" + clean.substring("static/".length());
        }

        int staticRootIndex = lower.indexOf(STATIC_ROOT);
        if (staticRootIndex >= 0) {
            return ensureLeadingSlash(clean.substring(staticRootIndex + "src/main/resources/static".length()));
        }

        int imageIndex = lower.indexOf("/images/");
        if (imageIndex >= 0) {
            return clean.substring(imageIndex);
        }

        int uploadIndex = lower.indexOf("/uploads/");
        if (uploadIndex >= 0) {
            return clean.substring(uploadIndex);
        }

        if (!clean.contains("/") && hasImageExtension(clean)) {
            return "/images/events/" + clean;
        }

        return clean;
    }

    private static String ensureLeadingSlash(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    private static String trimWrappingQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }

    private static boolean hasImageExtension(String value) {
        int dot = value.lastIndexOf('.');
        if (dot < 0 || dot == value.length() - 1) {
            return false;
        }
        String extension = value.substring(dot + 1).toLowerCase(Locale.ROOT);
        return IMAGE_EXTENSIONS.contains(extension);
    }
}
