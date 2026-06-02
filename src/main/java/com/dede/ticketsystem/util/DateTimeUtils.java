package com.dede.ticketsystem.util;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateTimeUtils() {
    }

    public static Timestamp truncateToMinute(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        long millis = timestamp.getTime();
        long truncated = millis - Math.floorMod(millis, 60_000L);
        return new Timestamp(truncated);
    }

    public static Timestamp parseDateTimeLocalToMinute(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String clean = value.trim()
                .replace("T", " ")
                .replace("Z", "");

        if (clean.contains(".")) {
            clean = clean.split("\\.")[0];
        }

        if (clean.length() == 16) {
            clean += ":00";
        }

        if (clean.length() > 19) {
            clean = clean.substring(0, 19);
        }

        return truncateToMinute(Timestamp.valueOf(clean));
    }

    public static String formatForDateTimeLocal(Timestamp timestamp) {
        Timestamp truncated = truncateToMinute(timestamp);
        if (truncated == null) {
            return null;
        }
        return truncated.toLocalDateTime().format(INPUT_FORMATTER);
    }

    public static String formatDisplayMinute(Timestamp timestamp) {
        Timestamp truncated = truncateToMinute(timestamp);
        if (truncated == null) {
            return "";
        }
        return truncated.toLocalDateTime().format(DISPLAY_FORMATTER);
    }
}
