package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.service.BookingService;
import com.dede.ticketsystem.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private com.dede.ticketsystem.service.QueueService queueService;

    @Autowired
    private com.dede.ticketsystem.service.RateLimitService rateLimitService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/lock-seats")
    public ResponseEntity<?> lockSeats(@RequestBody Map<String, Object> payload, jakarta.servlet.http.HttpServletRequest request) {
        try {
            String maKH = sessionService.getCurrentMaKH();
            
            // 1. Rate Limit Check
            String rateLimitKey = maKH != null ? maKH : request.getRemoteAddr();
            if (!rateLimitService.isAllowed(rateLimitKey)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("success", false, "message", "Bạn thao tác quá nhanh, vui lòng thử lại sau."));
            }

            if (!sessionService.isLoggedIn()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để đặt vé", "redirect", "/dang-nhap"));
            }
            if (!sessionService.hasRole("CUSTOMER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền truy cập chức năng này"));
            }

            if (maKH == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Tài khoản của bạn chưa có hồ sơ Khách hàng. Vui lòng liên hệ Admin.", "redirect", "/dang-nhap"));
            }

            List<String> maGheList = (List<String>) payload.get("maGheList");
            String maSK = (String) payload.get("maSK");
            String queueToken = (String) payload.get("queueToken");

            if (maSK == null || maSK.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã sự kiện không được trống!"));
            }
            if (maGheList == null || maGheList.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Danh sách ghế chọn không được trống!"));
            }

            // 2. Virtual Queue Verification
            if (queueService.shouldQueue(maSK)) {
                if (queueToken == null || !queueService.validateQueueToken(queueToken, maKH, maSK)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("success", false, "message", "Bạn cần vào hàng đợi trước khi đặt vé."));
                }
            }

            // Gọi logic giữ ghế
            String orderId = bookingService.lockSeats(maGheList, maSK, maKH, queueToken);

            // 3. Consume token on success
            if (queueService.shouldQueue(maSK) && queueToken != null) {
                queueService.consumeToken(queueToken, maKH, maSK);
            }
            
            return ResponseEntity.ok(Map.of("message", "Đã giữ ghế thành công", "redirect", "/thanh-toan?orderId=" + orderId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/lock-zone-tickets")
    public ResponseEntity<?> lockZoneTickets(@RequestBody Map<String, Object> payload, jakarta.servlet.http.HttpServletRequest request) {
        try {
            String maKH = sessionService.getCurrentMaKH();
            String rateLimitKey = maKH != null ? maKH : request.getRemoteAddr();
            if (!rateLimitService.isAllowed(rateLimitKey)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("success", false, "message", "Bạn thao tác quá nhanh, vui lòng thử lại sau."));
            }

            if (!sessionService.isLoggedIn()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Bạn cần đăng nhập để đặt vé", "redirect", "/dang-nhap"));
            }
            if (!sessionService.hasRole("CUSTOMER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn không có quyền truy cập chức năng này"));
            }
            if (maKH == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Tài khoản của bạn chưa có hồ sơ Khách hàng. Vui lòng liên hệ Admin.", "redirect", "/dang-nhap"));
            }

            String maSK = (String) payload.get("maSK");
            String maKhuVuc = (String) payload.get("maKhuVuc");
            String queueToken = (String) payload.get("queueToken");
            int soLuong = parsePositiveInt(payload.get("soLuong"));

            if (maSK == null || maSK.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã sự kiện không được trống!"));
            }
            if (maKhuVuc == null || maKhuVuc.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã khu vực không được trống!"));
            }

            if (queueService.shouldQueue(maSK)) {
                if (queueToken == null || !queueService.validateQueueToken(queueToken, maKH, maSK)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("success", false, "message", "Bạn cần vào hàng đợi trước khi đặt vé."));
                }
            }

            String orderId = bookingService.lockZoneTickets(maSK, maKhuVuc, soLuong, maKH, queueToken);

            if (queueService.shouldQueue(maSK) && queueToken != null) {
                queueService.consumeToken(queueToken, maKH, maSK);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Đã giữ vé khu đứng thành công", "redirect", "/thanh-toan?orderId=" + orderId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Autowired
    private com.dede.ticketsystem.service.PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> payload) {
        try {
            if (!sessionService.isLoggedIn()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để thực hiện thanh toán", "redirect", "/dang-nhap"));
            }
            if (!sessionService.hasRole("CUSTOMER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền truy cập chức năng này"));
            }

            String maKH = sessionService.getCurrentMaKH();
            if (maKH == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Tài khoản của bạn chưa có hồ sơ Khách hàng", "redirect", "/dang-nhap"));
            }

            String orderId = (String) payload.get("orderId");
            if (orderId == null) {
                orderId = (String) payload.get("seats");
            }
            
            String simulateResult = (String) payload.get("simulateResult");
            if (simulateResult == null) {
                Object successObj = payload.get("success");
                boolean success = false;
                if (successObj instanceof Boolean) {
                    success = (Boolean) successObj;
                } else if (successObj instanceof String) {
                    success = Boolean.parseBoolean((String) successObj);
                }
                simulateResult = success ? "Thành công" : "Thất bại";
            }

            String paymentMethod = (String) payload.get("paymentMethod");
            if (paymentMethod == null) {
                paymentMethod = "Chuyển khoản";
            }

            com.dede.ticketsystem.service.PaymentResult result = paymentService.processPayment(orderId, maKH, paymentMethod, simulateResult);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel-order")
    public ResponseEntity<?> cancelOrder(@RequestBody Map<String, Object> payload) {
        try {
            if (!sessionService.isLoggedIn()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để thực hiện", "redirect", "/dang-nhap"));
            }

            String maKH = sessionService.getCurrentMaKH();
            if (maKH == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Tài khoản của bạn chưa có hồ sơ Khách hàng", "redirect", "/dang-nhap"));
            }

            String orderId = (String) payload.get("orderId");
            if (orderId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu mã đơn hàng!"));
            }

            bookingService.cancelOrder(orderId, maKH);
            return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng và giải phóng ghế thành công.", "redirect", "/"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private int parsePositiveInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text.trim());
        }
        throw new IllegalArgumentException("Số lượng vé phải lớn hơn 0!");
    }
}
