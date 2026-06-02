package com.dede.ticketsystem.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.dede.ticketsystem.service.ActiveSessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final ActiveSessionRegistry activeSessionRegistry;

    public AuthInterceptor(ActiveSessionRegistry activeSessionRegistry) {
        this.activeSessionRegistry = activeSessionRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativeUri = uri.substring(contextPath.length());

        // Cho phép các static resources đi qua
        if (relativeUri.startsWith("/css/") || 
            relativeUri.startsWith("/js/") || 
            relativeUri.startsWith("/images/") || 
            relativeUri.startsWith("/uploads/") ||
            relativeUri.startsWith("/assets/") || 
            relativeUri.startsWith("/webjars/") || 
            relativeUri.equals("/favicon.ico")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        var nguoiDung = session != null ? session.getAttribute("nguoiDung") : null;
        @SuppressWarnings("unchecked")
        Set<String> roles = session != null ? (Set<String>) session.getAttribute("roles") : null;

        // Quyết định xem request có là AJAX hoặc API
        boolean isApiOrAjax = relativeUri.startsWith("/api/") || 
                              "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (nguoiDung != null && roles != null && roles.contains("ADMIN")) {
            String maND = (String) session.getAttribute("maND");
            if (!activeSessionRegistry.hasActiveSession(maND)) {
                activeSessionRegistry.registerSingleSession(maND, session);
            } else if (!activeSessionRegistry.isActiveSession(maND, session.getId())) {
                try {
                    session.invalidate();
                } catch (IllegalStateException ignored) {
                    // Session may already be invalidated.
                }
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Tài khoản đã đăng nhập ở thiết bị khác. Phiên hiện tại đã hết hạn.", "/dang-nhap?expired=other-device");
                    return false;
                }
                response.sendRedirect(contextPath + "/dang-nhap?expired=other-device");
                return false;
            }
        }

        // 1. Kiểm tra các route của Customer
        if (isAuthenticatedRoute(relativeUri)) {
            if (nguoiDung == null) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập", "/dang-nhap");
                    return false;
                }
                String currentPath = relativeUri;
                if (request.getQueryString() != null) {
                    currentPath += "?" + request.getQueryString();
                }
                response.sendRedirect(contextPath + "/dang-nhap?redirect=" + java.net.URLEncoder.encode(currentPath, java.nio.charset.StandardCharsets.UTF_8));
                return false;
            }
        }

        if (isTicketSimulationRoute(relativeUri)) {
            if (nguoiDung == null) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập", "/dang-nhap");
                    return false;
                }
                response.sendRedirect(contextPath + "/dang-nhap?redirect=" + relativeUri);
                return false;
            }
            if (roles == null || (!roles.contains("ADMIN") && !roles.contains("STAFF") && !roles.contains("ORGANIZER"))) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập", null);
                    return false;
                }
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin, Staff hoặc Organizer mới có quyền giả lập dùng vé.");
                return false;
            }
        }

        if (isTicketManagementRoute(relativeUri)) {
            if (nguoiDung == null) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập", "/dang-nhap");
                    return false;
                }
                response.sendRedirect(contextPath + "/dang-nhap?redirect=" + relativeUri);
                return false;
            }
            if (roles == null || (!roles.contains("ADMIN") && !roles.contains("STAFF") && !roles.contains("ORGANIZER"))) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập", null);
                    return false;
                }
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập quản lý vé.");
                return false;
            }
        }

        if (isCustomerRoute(relativeUri)) {
            if (nguoiDung == null) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập", "/dang-nhap");
                    return false;
                }
                String currentPath = relativeUri;
                if (request.getQueryString() != null) {
                    currentPath += "?" + request.getQueryString();
                }
                response.sendRedirect(contextPath + "/dang-nhap?redirect=" + currentPath);
                return false;
            }
            if (roles == null || !roles.contains("CUSTOMER")) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập", null);
                    return false;
                }
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này.");
                return false;
            }
        }

        if (isAdminOnlyRoute(relativeUri)) {
            if (nguoiDung == null) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập", "/dang-nhap");
                    return false;
                }
                response.sendRedirect(contextPath + "/dang-nhap?redirect=" + relativeUri);
                return false;
            }
            if (roles == null || !roles.contains("ADMIN")) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập", null);
                    return false;
                }
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin mới có quyền vào khu vực này.");
                return false;
            }
        }

        // 2. Kiểm tra các route của Admin/Organizer
        if (isAdminOrganizerRoute(relativeUri)) {
            if (nguoiDung == null) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập", "/dang-nhap");
                    return false;
                }
                response.sendRedirect(contextPath + "/dang-nhap?redirect=" + relativeUri);
                return false;
            }
            if (roles == null || (!roles.contains("ADMIN") && !roles.contains("ORGANIZER"))) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập", null);
                    return false;
                }
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin hoặc Organizer mới có quyền vào khu vực này.");
                return false;
            }
        }

        // 3. Kiểm tra các route của Staff
        if (isStaffRoute(relativeUri)) {
            if (nguoiDung == null) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập", "/dang-nhap");
                    return false;
                }
                response.sendRedirect(contextPath + "/dang-nhap?redirect=" + relativeUri);
                return false;
            }
            if (roles == null || (!roles.contains("STAFF") && !roles.contains("ADMIN"))) {
                if (isApiOrAjax) {
                    sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập", null);
                    return false;
                }
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Staff hoặc Admin mới có quyền vào khu vực này.");
                return false;
            }
        }

        return true;
    }

    private boolean isCustomerRoute(String uri) {
        return uri.equals("/thanh-toan") || 
               uri.startsWith("/api/booking/") ||
               uri.startsWith("/tai-khoan-cua-toi/") ||
               uri.equals("/ve-cua-toi") ||
               uri.startsWith("/ve-cua-toi/") ||
               uri.equals("/don-hang-cua-toi") ||
               uri.startsWith("/don-hang-cua-toi/") ||
               uri.startsWith("/hang-doi/") ||
               uri.startsWith("/api/hang-doi/status");
    }

    private boolean isAuthenticatedRoute(String uri) {
        return false;
    }

    private boolean isTicketSimulationRoute(String uri) {
        return uri.startsWith("/ve/") && uri.endsWith("/gia-lap-su-dung");
    }

    private boolean isTicketManagementRoute(String uri) {
        return uri.equals("/ve") || uri.startsWith("/ve/");
    }

    private boolean isAdminOnlyRoute(String uri) {
        return uri.equals("/taikhoan") || uri.startsWith("/taikhoan/");
    }

    private boolean isAdminOrganizerRoute(String uri) {
        return uri.equals("/sukien") || uri.startsWith("/sukien/") ||
               uri.equals("/donhang") || uri.startsWith("/donhang/") ||
               uri.equals("/baocao") || uri.startsWith("/baocao/") ||
               uri.startsWith("/api/loaisukien/") ||
               uri.startsWith("/api/hang-doi/allow-next");
    }

    private boolean isStaffRoute(String uri) {
        return uri.equals("/soat-ve") || uri.startsWith("/soat-ve/") || 
               uri.startsWith("/api/soat-ve/");
    }

    private void sendJsonError(HttpServletResponse response, int status, String message, String redirect) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        String json;
        if (redirect != null) {
            json = String.format("{\"success\": false, \"message\": \"%s\", \"redirect\": \"%s\"}", message, redirect);
        } else {
            json = String.format("{\"success\": false, \"message\": \"%s\"}", message);
        }
        response.getWriter().write(json);
    }
}
