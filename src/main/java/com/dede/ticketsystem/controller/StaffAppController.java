package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.NguoiDung;
import com.dede.ticketsystem.service.SessionService;
import com.dede.ticketsystem.service.SuKienService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Controller
public class StaffAppController {

    private final SessionService sessionService;
    private final SuKienService suKienService;
    private final JdbcTemplate jdbcTemplate;

    public StaffAppController(SessionService sessionService, SuKienService suKienService, JdbcTemplate jdbcTemplate) {
        this.sessionService = sessionService;
        this.suKienService = suKienService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/staff-app")
    public String showStaffApp(Model model) {
        if (!sessionService.isLoggedIn()) {
            return "redirect:/dang-nhap?redirect=/staff-app";
        }
        if (!sessionService.hasAnyRole("STAFF", "ADMIN")) {
            return "redirect:/?error=forbidden";
        }

        String maNV = sessionService.getCurrentMaNV();
        NguoiDung currentUser = sessionService.getCurrentUser();

        model.addAttribute("maNV", maNV);
        model.addAttribute("tenTaiKhoan", currentUser != null ? currentUser.getTenTaiKhoan() : "Nhân viên");
        model.addAttribute("staffProfileError", maNV == null
                ? "Tài khoản của bạn chưa được liên kết với hồ sơ nhân viên, vui lòng liên hệ quản trị viên."
                : null);
        model.addAttribute("events", loadStaffEvents());
        return "StaffApp/staff-app";
    }

    @GetMapping("/api/staff-app/events")
    @ResponseBody
    public ResponseEntity<?> getStaffAppEvents() {
        ResponseEntity<?> authError = ensureStaffOrAdminApi();
        if (authError != null) {
            return authError;
        }
        return ResponseEntity.ok(Map.of("success", true, "events", loadStaffEvents()));
    }

    private ResponseEntity<?> ensureStaffOrAdminApi() {
        if (!sessionService.isLoggedIn()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Phiên đăng nhập đã hết hạn.", "redirect", "/dang-nhap?redirect=/staff-app"));
        }
        if (!sessionService.hasAnyRole("STAFF", "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Yêu cầu quyền STAFF hoặc ADMIN."));
        }
        if (sessionService.getCurrentMaNV() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Tài khoản của bạn chưa được liên kết với hồ sơ nhân viên, vui lòng liên hệ quản trị viên."));
        }
        return null;
    }

    private List<StaffEventView> loadStaffEvents() {
        suKienService.layTatCa();
        return jdbcTemplate.query(
                "SELECT sk.MaSK, sk.TenSK, sk.TrangThaiSK, sk.ThoiGianBatDau, sk.ThoiGianKetThuc, " +
                        "dd.TenDiaDiem, dd.DiaChi, dd.ThanhPho " +
                        "FROM SUKIEN sk LEFT JOIN DIADIEM dd ON dd.MaDiaDiem = sk.MaDiaDiem " +
                        "WHERE sk.TrangThaiSK <> 'Đã hủy' OR sk.TrangThaiSK IS NULL " +
                        "ORDER BY sk.ThoiGianBatDau DESC, sk.TenSK ASC",
                (rs, rowNum) -> new StaffEventView(
                        rs.getString("MaSK"),
                        rs.getString("TenSK"),
                        rs.getString("TrangThaiSK"),
                        rs.getTimestamp("ThoiGianBatDau"),
                        rs.getTimestamp("ThoiGianKetThuc"),
                        rs.getString("TenDiaDiem"),
                        rs.getString("DiaChi"),
                        rs.getString("ThanhPho")
                )
        );
    }

    public record StaffEventView(
            String maSK,
            String tenSK,
            String trangThaiSK,
            Timestamp thoiGianBatDau,
            Timestamp thoiGianKetThuc,
            String tenDiaDiem,
            String diaChi,
            String thanhPho
    ) {
    }
}
