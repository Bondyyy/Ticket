package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.LichSuSoatVe;
import com.dede.ticketsystem.model.SuKien;
import com.dede.ticketsystem.model.ValidationResult;
import com.dede.ticketsystem.repository.LichSuSoatVeRepository;
import com.dede.ticketsystem.service.SessionService;
import com.dede.ticketsystem.service.SuKienService;
import com.dede.ticketsystem.service.TicketValidationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SoatVeController {

    private final SuKienService suKienService;
    private final SessionService sessionService;
    private final TicketValidationService ticketValidationService;
    private final LichSuSoatVeRepository lichSuSoatVeRepository;

    public SoatVeController(SuKienService suKienService, SessionService sessionService,
                            TicketValidationService ticketValidationService, LichSuSoatVeRepository lichSuSoatVeRepository) {
        this.suKienService = suKienService;
        this.sessionService = sessionService;
        this.ticketValidationService = ticketValidationService;
        this.lichSuSoatVeRepository = lichSuSoatVeRepository;
    }

    /**
     * Màn hình soát vé dành cho STAFF hoặc ADMIN
     */
    @GetMapping("/soat-ve")
    public String showSoatVe(Model model) {
        if (!sessionService.isLoggedIn()) {
            return "redirect:/dang-nhap";
        }
        if (!sessionService.hasAnyRole("STAFF", "ADMIN")) {
            return "redirect:/?error=forbidden";
        }
        
        // Lấy thông tin mã nhân viên. SessionService chỉ đọc NHANVIEN theo MaND.
        String maNV = sessionService.getCurrentMaNV();
        if (maNV == null) {
            model.addAttribute("errorMsg", "Tài khoản của bạn chưa được liên kết với hồ sơ Nhân viên (NHANVIEN). Vui lòng chạy seed data hoặc liên hệ Admin để tạo hồ sơ nhân viên.");
        }

        List<SuKien> suKienList = suKienService.layTatCa();
        model.addAttribute("suKienList", suKienList);
        model.addAttribute("maNV", maNV);
        return "SoatVe/soat-ve";
    }

    /**
     * API: Kiểm tra và soát vé online
     */
    @PostMapping("/api/soat-ve/validate")
    @ResponseBody
    public ResponseEntity<?> validateTicket(@RequestParam String qrPayloadOrCode,
                                            @RequestParam String maSK,
                                            @RequestParam(required = false, defaultValue = "Cổng chính") String congSoat) {
        ResponseEntity<?> authError = ensureStaffOrAdminApi("Yêu cầu quyền STAFF hoặc ADMIN để soát vé.");
        if (authError != null) {
            return authError;
        }

        String maNV = sessionService.getCurrentMaNV();
        if (maNV == null) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("message", "Yêu cầu tài khoản nhân viên (STAFF/ADMIN) để thực hiện soát vé.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResp);
        }

        if (maSK == null || maSK.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Vui lòng chọn sự kiện trước khi soát vé."));
        }

        try {
            ValidationResult result = ticketValidationService.validateQr(qrPayloadOrCode, maSK, maNV, congSoat, "Online");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Lỗi soát vé online: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Không thể soát vé lúc này. Vui lòng thử lại."));
        }
    }

    /**
     * API: Đồng bộ danh sách vé quét offline
     */
    @PostMapping("/api/soat-ve/sync")
    @ResponseBody
    public ResponseEntity<?> syncOfflineTickets(@RequestBody List<OfflineScanDTO> pendingScans) {
        ResponseEntity<?> authError = ensureStaffOrAdminApi("Yêu cầu quyền STAFF hoặc ADMIN để đồng bộ soát vé.");
        if (authError != null) {
            return authError;
        }

        String maNV = sessionService.getCurrentMaNV();
        if (maNV == null) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("success", false);
            errorResp.put("message", "Yêu cầu tài khoản nhân viên (STAFF/ADMIN) để thực hiện đồng bộ.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResp);
        }

        if (pendingScans == null || pendingScans.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", true, "syncedCount", 0, "results", new ArrayList<>()));
        }

        List<ValidationResult> results = new ArrayList<>();
        int successCount = 0;
        int processedCount = 0;

        for (OfflineScanDTO scan : pendingScans) {
            try {
                if (scan == null || scan.getMaSK() == null || scan.getMaSK().isBlank()) {
                    ValidationResult failed = new ValidationResult(false, "Chưa chọn sự kiện", null, null, null, null, 0);
                    failed.setMessage("Vui lòng chọn sự kiện trước khi soát vé.");
                    failed.setNguonDuLieu("Offline");
                    results.add(failed);
                    continue;
                }
                Timestamp thoiGianQuet = null;
                if (scan.getThoiGianQuet() != null) {
                    thoiGianQuet = parseScanTimestamp(scan.getThoiGianQuet());
                    if (thoiGianQuet == null) {
                        thoiGianQuet = new Timestamp(System.currentTimeMillis());
                    }
                }

                ValidationResult res = ticketValidationService.validateQr(
                        scan.getQrPayloadOrCode(),
                        scan.getMaSK(),
                        maNV,
                        scan.getCongSoat() != null ? scan.getCongSoat() : "Cổng chính",
                        "Offline",
                        thoiGianQuet
                );

                results.add(res);
                processedCount++;
                if (res.isSuccess()) {
                    successCount++;
                }
            } catch (Exception e) {
                ValidationResult failed = new ValidationResult(false, "Vé không tìm thấy", null, null, null, null, 0);
                failed.setMessage("Không thể đồng bộ lượt quét này: " + e.getMessage());
                failed.setNguonDuLieu("Offline");
                results.add(failed);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("syncedCount", processedCount);
        response.put("successCount", successCount);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    /**
     * API: Lấy 10 lịch sử soát vé gần nhất của nhân viên hiện tại
     */
    @GetMapping("/api/soat-ve/history")
    @ResponseBody
    public ResponseEntity<?> getScanHistory() {
        ResponseEntity<?> authError = ensureStaffOrAdminApi("Yêu cầu quyền STAFF hoặc ADMIN để xem lịch sử soát vé.");
        if (authError != null) {
            return authError;
        }

        String maNV = sessionService.getCurrentMaNV();
        if (maNV == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Không tìm thấy mã nhân viên."));
        }

        List<LichSuSoatVe> history = lichSuSoatVeRepository.findRecentHistoryByStaff(maNV, PageRequest.of(0, 10));
        return ResponseEntity.ok(history);
    }

    /**
     * API: Reset dữ liệu vé và lịch sử để phục vụ kiểm thử tích hợp
     */
    @PostMapping("/api/soat-ve/reset")
    @ResponseBody
    public ResponseEntity<?> resetTestData() {
        ResponseEntity<?> authError = ensureStaffOrAdminApi("Yêu cầu quyền STAFF hoặc ADMIN để reset dữ liệu soát vé.");
        if (authError != null) {
            return authError;
        }
        ticketValidationService.resetTestData();
        return ResponseEntity.ok(Map.of("success", true, "message", "Reset dữ liệu soát vé thành công!"));
    }

    private ResponseEntity<?> ensureStaffOrAdminApi(String message) {
        if (!sessionService.isLoggedIn()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Bạn cần đăng nhập.", "redirect", "/dang-nhap"));
        }
        if (!sessionService.hasAnyRole("STAFF", "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", message));
        }
        return null;
    }

    private Timestamp parseScanTimestamp(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        try {
            String clean = timeStr.trim();
            if (clean.matches("^\\d+$")) {
                return new Timestamp(Long.parseLong(clean));
            }
            return parseTimestamp(clean);
        } catch (Exception e) {
            System.err.println("Không thể parse timestamp: " + timeStr + " - " + e.getMessage());
            return null;
        }
    }

    private Timestamp parseTimestamp(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        try {
            String clean = timeStr.trim()
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

            return Timestamp.valueOf(clean);
        } catch (Exception e) {
            System.err.println("Không thể parse timestamp: " + timeStr + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * DTO đại diện cho một bản ghi soát vé offline gửi lên từ client
     */
    public static class OfflineScanDTO {
        private String qrPayloadOrCode;
        private String maSK;
        private String congSoat;
        private String thoiGianQuet;

        public OfflineScanDTO() {}

        public String getQrPayloadOrCode() {
            return qrPayloadOrCode;
        }

        public void setQrPayloadOrCode(String qrPayloadOrCode) {
            this.qrPayloadOrCode = qrPayloadOrCode;
        }

        public String getMaSK() {
            return maSK;
        }

        public void setMaSK(String maSK) {
            this.maSK = maSK;
        }

        public String getCongSoat() {
            return congSoat;
        }

        public void setCongSoat(String congSoat) {
            this.congSoat = congSoat;
        }

        public String getThoiGianQuet() {
            return thoiGianQuet;
        }

        public void setThoiGianQuet(String thoiGianQuet) {
            this.thoiGianQuet = thoiGianQuet;
        }
    }
}
