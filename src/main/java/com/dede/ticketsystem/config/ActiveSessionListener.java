package com.dede.ticketsystem.config;

import com.dede.ticketsystem.service.ActiveSessionRegistry;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

@Component
public class ActiveSessionListener implements HttpSessionListener {

    private final ActiveSessionRegistry activeSessionRegistry;

    public ActiveSessionListener(ActiveSessionRegistry activeSessionRegistry) {
        this.activeSessionRegistry = activeSessionRegistry;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessionRegistry.unregisterSession(se.getSession().getId());
    }
}
