package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.NguoiDung;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class SessionService {

    @Autowired
    private HttpServletRequest request;

    private HttpSession getSession() {
        return request.getSession(false);
    }

    public boolean isLoggedIn() {
        HttpSession session = getSession();
        return session != null && session.getAttribute("nguoiDung") != null;
    }

    public NguoiDung getCurrentUser() {
        HttpSession session = getSession();
        if (session == null) {
            return null;
        }
        return (NguoiDung) session.getAttribute("nguoiDung");
    }

    public String getCurrentMaND() {
        HttpSession session = getSession();
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("maND");
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public String getCurrentMaNV() {
        String currentMaND = getCurrentMaND();
        if (currentMaND == null) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject("SELECT MaNV FROM NHANVIEN WHERE MaND = ?", String.class, currentMaND);
        } catch (Exception e) {
            return null;
        }
    }

    public String getCurrentMaKH() {
        HttpSession session = getSession();
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("maKH");
    }

    @SuppressWarnings("unchecked")
    public Set<String> getCurrentRoles() {
        HttpSession session = getSession();
        if (session == null) {
            return Collections.emptySet();
        }
        Set<String> roles = (Set<String>) session.getAttribute("roles");
        return roles != null ? roles : Collections.emptySet();
    }

    public boolean hasRole(String role) {
        return getCurrentRoles().contains(role);
    }

    public boolean hasAnyRole(String... roles) {
        Set<String> currentRoles = getCurrentRoles();
        for (String r : roles) {
            if (currentRoles.contains(r)) {
                return true;
            }
        }
        return false;
    }
}
