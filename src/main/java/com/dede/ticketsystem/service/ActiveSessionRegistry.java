package com.dede.ticketsystem.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveSessionRegistry {

    private final Map<String, String> activeSessionByMaND = new ConcurrentHashMap<>();
    private final Map<String, HttpSession> sessionById = new ConcurrentHashMap<>();
    private final Map<String, String> maNDBySessionId = new ConcurrentHashMap<>();

    public void registerSingleSession(String maND, HttpSession session) {
        if (maND == null || session == null) {
            return;
        }

        String newSessionId = session.getId();
        String oldSessionId = activeSessionByMaND.get(maND);
        if (oldSessionId != null && !oldSessionId.equals(newSessionId)) {
            HttpSession oldSession = sessionById.get(oldSessionId);
            if (oldSession != null) {
                try {
                    oldSession.invalidate();
                } catch (IllegalStateException ignored) {
                    // Already invalidated by the container.
                }
            }
        }

        activeSessionByMaND.put(maND, newSessionId);
        sessionById.put(newSessionId, session);
        maNDBySessionId.put(newSessionId, maND);
    }

    public boolean hasActiveSession(String maND) {
        return maND != null && activeSessionByMaND.containsKey(maND);
    }

    public boolean isActiveSession(String maND, String sessionId) {
        if (maND == null || sessionId == null) {
            return false;
        }
        return sessionId.equals(activeSessionByMaND.get(maND));
    }

    public void unregisterSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        sessionById.remove(sessionId);
        String maND = maNDBySessionId.remove(sessionId);
        if (maND != null && sessionId.equals(activeSessionByMaND.get(maND))) {
            activeSessionByMaND.remove(maND);
        }
    }
}
