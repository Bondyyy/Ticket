package com.dede.ticketsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/user-preferences")
public class UserPreferenceController {
    private static final Set<String> VALID_THEME_MODES = Set.of("light", "dark", "system");

    @PostMapping("/theme")
    public ResponseEntity<?> saveTheme(@RequestBody Map<String, Object> payload, HttpSession session) {
        String themeMode = String.valueOf(payload.getOrDefault("themeMode", "system")).trim();
        if (!VALID_THEME_MODES.contains(themeMode)) {
            themeMode = "system";
        }

        int brightness = parseBrightness(payload.get("brightness"));
        session.setAttribute("themeMode", themeMode);
        session.setAttribute("uiBrightness", brightness);
        return ResponseEntity.ok(Map.of("success", true, "themeMode", themeMode, "brightness", brightness));
    }

    private int parseBrightness(Object raw) {
        int value = 100;
        if (raw instanceof Number number) {
            value = number.intValue();
        } else if (raw instanceof String text && !text.isBlank()) {
            try {
                value = Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                value = 100;
            }
        }
        return Math.max(80, Math.min(120, value));
    }
}
