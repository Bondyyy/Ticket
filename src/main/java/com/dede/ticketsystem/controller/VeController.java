package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.Ve;
import com.dede.ticketsystem.model.VeDTO;
import com.dede.ticketsystem.model.VeQuanLyDTO;
import com.dede.ticketsystem.service.SessionService;
import com.dede.ticketsystem.service.VeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

@Controller
@RequestMapping("/ve")
public class VeController {

    private final VeService veService;
    private final JdbcTemplate jdbcTemplate;
    private final SessionService sessionService;

    public VeController(VeService veService, JdbcTemplate jdbcTemplate, SessionService sessionService) {
        this.veService = veService;
        this.jdbcTemplate = jdbcTemplate;
        this.sessionService = sessionService;
    }

    @GetMapping
    public String danhSach(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String trangThai,
            Model model) {
        List<VeQuanLyDTO> list;
        try {
            list = veService.timKiemQuanLy(keyword, trangThai);
        } catch (Exception e) {
            list = new java.util.ArrayList<>();
        }
        
        List<Map<String, Object>> dsSuKien = new java.util.ArrayList<>();
        List<Map<String, Object>> dsGhe = new java.util.ArrayList<>();
        List<Map<String, Object>> dsDonHang = new java.util.ArrayList<>();
        Map<String, String> mapSuKien = new java.util.HashMap<>();
        Map<String, String> mapDonHang = new java.util.HashMap<>();
        
        try {
            dsSuKien = jdbcTemplate.queryForList("SELECT MaSK, TenSK FROM SUKIEN ORDER BY ThoiGianTao DESC");
            for (Map<String, Object> sk : dsSuKien) {
                mapSuKien.put((String) sk.get("MASK"), (String) sk.get("TENSK"));
            }
            
            dsGhe = jdbcTemplate.queryForList("SELECT MaGhe, MaKhuVuc FROM GHENGOI");
            
            dsDonHang = jdbcTemplate.queryForList("SELECT dh.MaDonHang, dh.SoDonHang, kh.HoTenKH FROM DONHANG dh LEFT JOIN KHACHHANG kh ON dh.MaKH = kh.MaKH");
            for (Map<String, Object> dh : dsDonHang) {
                Object label = dh.get("SODONHANG") != null ? dh.get("SODONHANG") : dh.get("MADONHANG");
                mapDonHang.put((String) dh.get("MADONHANG"), label != null ? label.toString() : "");
            }
        } catch (Exception e) {}

        model.addAttribute("veList", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("veMoi", new VeDTO());
        model.addAttribute("dsSuKien", dsSuKien);
        model.addAttribute("dsGhe", dsGhe);
        model.addAttribute("dsDonHang", dsDonHang);
        model.addAttribute("mapSuKien", mapSuKien);
        model.addAttribute("mapDonHang", mapDonHang);
        return "QLV/QLV";
    }

    @GetMapping("/api/{maVe}")
    @ResponseBody
    public ResponseEntity<?> chiTiet(@PathVariable String maVe) {
        return veService.timTheoMa(maVe)
                .map(v -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("maVe", v.getMaVe());
                    data.put("maQR", v.getMaQR());
                    data.put("giaVe", v.getGiaVe());
                    data.put("trangThaiVe", v.getTrangThaiVe());
                    data.put("thoiGianPhat", v.getThoiGianPhat() != null ? v.getThoiGianPhat().toString() : null);
                    data.put("thoiGianSuDung", v.getThoiGianSuDung() != null ? v.getThoiGianSuDung().toString() : null);
                    data.put("maDonHang", v.getMaDonHang());
                    data.put("maGhe", v.getMaGhe());
                    data.put("maSK", v.getMaSK());
                    if (v.getMaGhe() != null && !v.getMaGhe().isBlank()) {
                        try {
                            List<Map<String, Object>> seats = jdbcTemplate.queryForList(
                                    "SELECT MaKhuVuc as MAKHUVUC, TenGhe as TENGHE FROM GHENGOI WHERE MaGhe = ?",
                                    v.getMaGhe());
                            if (!seats.isEmpty()) {
                                Map<String, Object> seat = seats.get(0);
                                data.put("maKhuVuc", seat.get("MAKHUVUC"));
                                data.put("tenGhe", seat.get("TENGHE"));
                            }
                        } catch (Exception ignored) {}
                    }
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tao-moi")
    public String taoMoi(@ModelAttribute VeDTO dto, RedirectAttributes redirectAttributes) {
        try {
            Ve createdVe = veService.taoVe(dto);
            redirectAttributes.addFlashAttribute("thanhCong", "Thêm vé " + createdVe.getMaVe() + " thành công!");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("ORA-01403")) {
                    errorMsg = "Mã ghế không hợp lệ hoặc không tồn tại trong hệ thống. Vui lòng nhập đúng Mã Ghế hợp lệ.";
                } else if (errorMsg.contains("ORA-20001")) {
                    errorMsg = "Khu vực của ghế này đã hết vé! Không thể tạo thêm vé mới.";
                } else if (errorMsg.contains("ORA-")) {
                    errorMsg = "Lỗi cơ sở dữ liệu: Vui lòng kiểm tra lại thông tin mã ghế, mã sự kiện.";
                }
            }
            redirectAttributes.addFlashAttribute("loi", "Lỗi tạo vé: " + errorMsg);
        }
        return "redirect:/ve";
    }

    @PostMapping("/cap-nhat/{maVe}")
    public String capNhat(@PathVariable String maVe, @ModelAttribute VeDTO dto, RedirectAttributes redirectAttributes) {
        try {
            veService.capNhatVe(maVe, dto);
            redirectAttributes.addFlashAttribute("thanhCong", "Cập nhật vé " + maVe + " thành công!");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("ORA-")) {
                errorMsg = "Lỗi dữ liệu: Vui lòng kiểm tra lại thông tin.";
            }
            redirectAttributes.addFlashAttribute("loi", "Lỗi cập nhật: " + errorMsg);
        }
        return "redirect:/ve";
    }

    @PostMapping("/xoa/{maVe}")
    public String huyVe(@PathVariable String maVe, RedirectAttributes redirectAttributes) {
        try {
            veService.huyVe(maVe);
            redirectAttributes.addFlashAttribute("thanhCong", "Đã hủy vé " + maVe + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", "Lỗi hủy vé: " + e.getMessage());
        }
        return "redirect:/ve";
    }

    @PostMapping("/{maVe}/gia-lap-su-dung")
    public String giaLapSuDung(@PathVariable String maVe, RedirectAttributes redirectAttributes) {
        try {
            String message = veService.giaLapSuDungVe(maVe, sessionService.getCurrentMaNV());
            redirectAttributes.addFlashAttribute("thanhCong", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/ve";
    }

    @GetMapping("/api/khu-vuc/{maSK}")
    @ResponseBody
    public ResponseEntity<?> layKhuVuc(@PathVariable String maSK) {
        try {
            List<Map<String, Object>> zones = jdbcTemplate.queryForList(
                "SELECT MaKhuVuc as MAKHUVUC, TenKhuVuc as TENKHUVUC FROM KHUVUC WHERE MaSK = ?", maSK);
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/ghe/{maKV}")
    @ResponseBody
    public ResponseEntity<?> layGhe(@PathVariable String maKV) {
        try {
            List<Map<String, Object>> seats = jdbcTemplate.queryForList(
                "SELECT MaGhe as MAGHE, TenGhe as TENGHE FROM GHENGOI WHERE MaKhuVuc = ? AND TrangThaiGhe = 'Trống'", maKV);
            return ResponseEntity.ok(seats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
