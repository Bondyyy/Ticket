package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.DonHang;
import com.dede.ticketsystem.model.KhachHang;
import com.dede.ticketsystem.repository.KhachHangRepository;
import com.dede.ticketsystem.service.DonHangService;
import com.dede.ticketsystem.service.SessionService;
import com.dede.ticketsystem.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/donhang")
public class DonHangController {

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionService sessionService;

    /** Trang danh sách đơn hàng */
    @GetMapping
    public String danhSach(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String trangThai,
            Model model) {
        List<DonHang> list;
        try {
            list = donHangService.timKiem(keyword, trangThai);
        } catch (Exception e) {
            list = new java.util.ArrayList<>();
        }

        Map<String, String> mapKhachHang = new java.util.HashMap<>();
        try {
            List<KhachHang> khachHangs = khachHangRepository.findAll();
            for (KhachHang kh : khachHangs) {
                String displayName = kh.getHoTenKH();
                if (displayName == null || displayName.isBlank()) {
                    displayName = kh.getNguoiDung() != null ? kh.getNguoiDung().getTenTaiKhoan() : kh.getMaKH();
                }
                // Map cả MaKH và MaND sang displayName để giải quyết triệt để lỗi phân giải tên khách hàng
                mapKhachHang.put(kh.getMaKH(), displayName);
                if (kh.getNguoiDung() != null) {
                    mapKhachHang.put(kh.getNguoiDung().getMaND(), displayName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("donHangList", list);
        model.addAttribute("mapKhachHang", mapKhachHang);
        model.addAttribute("orderStats", donHangService.thongKeTongQuan());
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiFilter", trangThai);
        return "QLDH/QLDH";
    }

    /** API: lấy chi tiết 1 đơn hàng theo mã */
    @GetMapping("/api/{maDonHang}")
    @ResponseBody
    public ResponseEntity<?> chiTiet(@PathVariable String maDonHang) {
        return donHangService.timTheoMa(maDonHang)
                .map(dh -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("maDonHang", dh.getMaDonHang());
                    data.put("soDonHang", dh.getSoDonHang());
                    data.put("tongTien", dh.getTongTien());
                    data.put("thanhTien", dh.getThanhTien());
                    data.put("trangThaiDonHang", dh.getTrangThaiDonHang());
                    data.put("thoiGianDat", DateTimeUtils.formatDisplayMinute(dh.getThoiGianDat()));
                    data.put("thoiGianHetHan", DateTimeUtils.formatDisplayMinute(dh.getThoiGianHetHan()));
                    data.put("maKH", dh.getMaKH());
                    data.put("maNV", dh.getMaNV());
                    data.put("maPGG", dh.getMaPGG());
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** API: hủy đơn hàng */
    @PostMapping("/huy/{maDonHang}")
    @ResponseBody
    public ResponseEntity<?> huyDon(@PathVariable String maDonHang) {
        try {
            donHangService.huyDonHang(maDonHang);
            return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng " + maDonHang));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** API: tạo đơn hàng nhanh (test) */
    @PostMapping("/api/tao-nhanh")
    @ResponseBody
    public ResponseEntity<?> taoDonNhanh(@RequestParam String maSK, @RequestParam int soLuong, @RequestParam(required = false) String maKH) {
        try {
            String currentMaKH = (maKH != null && !maKH.isBlank()) ? maKH : sessionService.getCurrentMaKH();
            if (currentMaKH == null) {
                throw new RuntimeException("Vui lòng đăng nhập với tài khoản Khách hàng để đặt đơn hàng!");
            }
            
            String currentMaND = sessionService.getCurrentMaND();
            String maNV = null;
            if (currentMaND != null) {
                try {
                    maNV = jdbcTemplate.queryForObject("SELECT MaNV FROM NHANVIEN WHERE MaND = ?", String.class, currentMaND);
                } catch (Exception e) {
                    // ignore if current user is not a staff/admin
                }
            }

            DonHang dh = donHangService.taoDonHang(maSK, soLuong, currentMaKH, maNV);
            return ResponseEntity.ok(Map.of("message", "Tạo đơn hàng thành công", "maDonHang", dh.getMaDonHang()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
