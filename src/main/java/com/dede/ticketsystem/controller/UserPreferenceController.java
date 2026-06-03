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
    private static final Set<String> VALID_THEME_MODES = Set.of("light", "dark");

    @PostMapping("/theme")
    public ResponseEntity<?> saveTheme(@RequestBody Map<String, Object> payload, HttpSession session) {
        String themeMode = String.valueOf(payload.getOrDefault("themeMode", "light")).trim();
        if (!VALID_THEME_MODES.contains(themeMode)) {
            themeMode = "light";
        }

        session.setAttribute("themeMode", themeMode);
        return ResponseEntity.ok(Map.of("success", true, "themeMode", themeMode));
    }
}
