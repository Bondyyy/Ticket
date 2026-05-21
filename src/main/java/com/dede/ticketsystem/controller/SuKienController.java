package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.SuKien;
import com.dede.ticketsystem.model.SuKienDTO;
import com.dede.ticketsystem.model.ThietLapSanKhauDTO;
import com.dede.ticketsystem.service.SuKienService;
import com.dede.ticketsystem.repository.KhuVucRepository;
import com.dede.ticketsystem.repository.GheRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

@Controller
@RequestMapping("/sukien")
public class SuKienController {

    private final SuKienService suKienService;
    private final JdbcTemplate jdbcTemplate;
    private final KhuVucRepository khuVucRepository;
    private final GheRepository gheRepository;

    public SuKienController(SuKienService suKienService, JdbcTemplate jdbcTemplate, KhuVucRepository khuVucRepository, GheRepository gheRepository) {
        this.suKienService = suKienService;
        this.jdbcTemplate = jdbcTemplate;
        this.khuVucRepository = khuVucRepository;
        this.gheRepository = gheRepository;
    }

    @GetMapping
    public String danhSach(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String trangThai,
            Model model) {
        List<SuKien> list;
        try {
            list = suKienService.timKiem(keyword, trangThai);
        } catch (Exception e) {
            list = new java.util.ArrayList<>();
        }
        
        List<Map<String, Object>> dsLoaiSK = new java.util.ArrayList<>();
        List<Map<String, Object>> dsDiaDiem = new java.util.ArrayList<>();
        List<Map<String, Object>> dsNhanVien = new java.util.ArrayList<>();
        try {
            dsLoaiSK = jdbcTemplate.queryForList("SELECT MaLoaiSK as MALOAISK, TenLoaiSK as TENLOAISK FROM LOAISUKIEN");
            dsDiaDiem = jdbcTemplate.queryForList("SELECT MaDiaDiem as MADIADIEM, TenDiaDiem as TENDIADIEM FROM DIADIEM");
            dsNhanVien = jdbcTemplate.queryForList("SELECT nv.MaNV as MANV, nd.TenTaiKhoan as TENNV FROM NHANVIEN nv JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND");
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("suKienList", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("skMoi", new SuKienDTO());
        model.addAttribute("dsLoaiSK", dsLoaiSK);
        model.addAttribute("dsDiaDiem", dsDiaDiem);
        model.addAttribute("dsNhanVien", dsNhanVien);
        return "QLSK/QLSK";
    }

    @GetMapping("/api/{maSK}")
    @ResponseBody
    public ResponseEntity<?> chiTiet(@PathVariable String maSK) {
        return suKienService.timTheoMa(maSK)
                .map(sk -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("maSK", sk.getMaSK());
                    data.put("tenSK", sk.getTenSK());
                    data.put("moTa", sk.getMoTa());
                    data.put("moTaNgan", sk.getMoTaNgan());
                    data.put("hinhAnh", sk.getHinhAnh());
                    data.put("hinhAnhThumb", sk.getHinhAnhThumb());
                    data.put("tags", sk.getTags());
                    data.put("thoiGianBatDau", sk.getThoiGianBatDau() != null ? sk.getThoiGianBatDau().toString() : null);
                    data.put("thoiGianKetThuc", sk.getThoiGianKetThuc() != null ? sk.getThoiGianKetThuc().toString() : null);
                    data.put("thoiGianMoBan", sk.getThoiGianMoBan() != null ? sk.getThoiGianMoBan().toString() : null);
                    data.put("thoiGianDongBan", sk.getThoiGianDongBan() != null ? sk.getThoiGianDongBan().toString() : null);
                    data.put("tongSoVe", sk.getTongSoVe());
                    data.put("soVeDaBan", sk.getSoVeDaBan());
                    data.put("trangThaiSK", sk.getTrangThaiSK());
                    data.put("maLoaiSK", sk.getMaLoaiSK());
                    data.put("tenLoaiSK", timTenLoaiSK(sk.getMaLoaiSK()));
                    data.put("maDiaDiem", sk.getMaDiaDiem());
                    data.put("maNV", sk.getMaNV());
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tao-moi")
    public String taoMoi(@ModelAttribute SuKienDTO dto,
                         @RequestParam(required = false) MultipartFile hinhAnhFile,
                         @RequestParam(required = false) MultipartFile hinhAnhThumbFile,
                         RedirectAttributes redirectAttributes) {
        try {
            SuKien created = suKienService.taoSuKien(dto, hinhAnhFile, hinhAnhThumbFile);
            redirectAttributes.addFlashAttribute("thanhCong", "Thêm sự kiện " + created.getMaSK() + " thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isBlank()) {
                errorMsg = "Lỗi cơ sở dữ liệu: Vui lòng kiểm tra lại thông tin (Ràng buộc dữ liệu).";
            }
            redirectAttributes.addFlashAttribute("loi", "Lỗi tạo sự kiện: " + errorMsg);
        }
        return "redirect:/sukien";
    }

    @PostMapping("/cap-nhat/{maSK}")
    public String capNhat(@PathVariable String maSK,
                          @ModelAttribute SuKienDTO dto,
                          @RequestParam(required = false) MultipartFile hinhAnhFile,
                          @RequestParam(required = false) MultipartFile hinhAnhThumbFile,
                          RedirectAttributes redirectAttributes) {
        return xuLyCapNhat(maSK, dto, hinhAnhFile, hinhAnhThumbFile, redirectAttributes);
    }

    @PostMapping("/update")
    public String capNhatTuForm(@ModelAttribute SuKienDTO dto,
                                @RequestParam(required = false) MultipartFile hinhAnhFile,
                                @RequestParam(required = false) MultipartFile hinhAnhThumbFile,
                                RedirectAttributes redirectAttributes) {
        if (dto.getMaSK() == null || dto.getMaSK().isBlank()) {
            redirectAttributes.addFlashAttribute("loi", "Lỗi cập nhật: Không xác định được mã sự kiện.");
            return "redirect:/sukien";
        }
        return xuLyCapNhat(dto.getMaSK().trim(), dto, hinhAnhFile, hinhAnhThumbFile, redirectAttributes);
    }

    private String xuLyCapNhat(String maSK,
                              SuKienDTO dto,
                              MultipartFile hinhAnhFile,
                              MultipartFile hinhAnhThumbFile,
                              RedirectAttributes redirectAttributes) {
        try {
            suKienService.capNhatSuKien(maSK, dto, hinhAnhFile, hinhAnhThumbFile);
            redirectAttributes.addFlashAttribute("thanhCong", "Cập nhật sự kiện " + maSK + " thành công!");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isBlank()) {
                errorMsg = "Lỗi dữ liệu: Vui lòng kiểm tra lại thông tin.";
            }
            redirectAttributes.addFlashAttribute("loi", "Lỗi cập nhật: " + errorMsg);
        }
        return "redirect:/sukien";
    }

    private String timTenLoaiSK(String maLoaiSK) {
        if (maLoaiSK == null || maLoaiSK.isBlank()) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT TenLoaiSK FROM LOAISUKIEN WHERE MaLoaiSK = ?",
                    String.class,
                    maLoaiSK
            );
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/xoa/{maSK}")
    public String huySuKien(@PathVariable String maSK, RedirectAttributes redirectAttributes) {
        try {
            suKienService.huySuKien(maSK);
            redirectAttributes.addFlashAttribute("thanhCong", "Đã hủy sự kiện " + maSK + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", "Lỗi hủy sự kiện: " + e.getMessage());
        }
        return "redirect:/sukien";
    }

    @PostMapping("/api/{maSK}/thiet-lap-san-khau")
    @ResponseBody
    public ResponseEntity<?> thietLapSanKhau(@PathVariable String maSK, @RequestBody ThietLapSanKhauDTO dto) {
        System.out.println("DEBUG: Controller received request for maSK: " + maSK);
        if (dto != null && dto.getDanhSachKhuVuc() != null) {
            System.out.println("DEBUG: Zones count: " + dto.getDanhSachKhuVuc().size());
        } else {
            System.out.println("DEBUG: DTO or zones list is NULL");
        }
        try {
            suKienService.thietLapSanKhau(maSK, dto);
            System.out.println("DEBUG: Service call successful for " + maSK);
            return ResponseEntity.ok(Map.of("message", "Thiết lập sân khấu thành công!"));
        } catch (Exception e) {
            System.out.println("DEBUG: Controller caught exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Đã có lỗi hệ thống xảy ra khi lưu cấu hình sân khấu: " + e.getMessage()));
        }
    }

    @GetMapping("/api/{maSK}/so-do-ghe")
    @ResponseBody
    public ResponseEntity<?> laySoDoGhe(@PathVariable String maSK) {
        try {
            return ResponseEntity.ok(suKienService.getSeatMap(maSK));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Đã có lỗi hệ thống xảy ra khi tải dữ liệu. Vui lòng thử lại."));
        }
    }
}
