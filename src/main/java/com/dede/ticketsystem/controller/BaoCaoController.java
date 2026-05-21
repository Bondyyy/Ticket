package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.BaoCaoSuKienDTO;
import com.dede.ticketsystem.model.BaoCaoTongQuanDTO;
import com.dede.ticketsystem.model.HanhViKhachHangDTO;
import com.dede.ticketsystem.model.SuKien;
import com.dede.ticketsystem.repository.SuKienRepository;
import com.dede.ticketsystem.service.BaoCaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class BaoCaoController {

    private static final Set<String> TRANG_THAI_DON_HANG_HOP_LE = Set.of(
            "Chờ thanh toán",
            "Đã thanh toán",
            "Đã hủy",
            "Hoàn tiền"
    );

    private final BaoCaoService baoCaoService;
    private final SuKienRepository suKienRepository;

    public BaoCaoController(BaoCaoService baoCaoService, SuKienRepository suKienRepository) {
        this.baoCaoService = baoCaoService;
        this.suKienRepository = suKienRepository;
    }

    @GetMapping("/baocao")
    public String viewDashboard(
            @RequestParam(value = "maSK", required = false) String maSK,
            @RequestParam(value = "tuNgay", required = false) String tuNgay,
            @RequestParam(value = "denNgay", required = false) String denNgay,
            @RequestParam(value = "trangThaiDonHang", required = false) String trangThaiDonHang,
            Model model) {

        List<String> errors = new ArrayList<>();
        Timestamp startTimestamp = parseDateStart(tuNgay, "Từ ngày", errors);
        Timestamp endTimestamp = parseDateEnd(denNgay, "Đến ngày", errors);

        String selectedMaSK = normalize(maSK);
        String selectedTrangThai = normalize(trangThaiDonHang);
        if (selectedTrangThai != null && !TRANG_THAI_DON_HANG_HOP_LE.contains(selectedTrangThai)) {
            errors.add("Trạng thái đơn hàng không hợp lệ, hệ thống đã bỏ qua bộ lọc trạng thái.");
            selectedTrangThai = null;
        }

        List<SuKien> suKienList = Collections.emptyList();
        BaoCaoTongQuanDTO tongQuan = new BaoCaoTongQuanDTO();
        List<BaoCaoSuKienDTO> baoCaoSuKienList = Collections.emptyList();
        List<HanhViKhachHangDTO> hanhViList = Collections.emptyList();

        try {
            suKienList = suKienRepository.findAll();
            tongQuan = baoCaoService.getBaoCaoTongQuan(selectedMaSK, startTimestamp, endTimestamp, selectedTrangThai);
            baoCaoSuKienList = baoCaoService.getBaoCaoSuKien(selectedMaSK, startTimestamp, endTimestamp, selectedTrangThai);
            hanhViList = baoCaoService.getRecentHanhViKhachHang(selectedMaSK, startTimestamp, endTimestamp);
        } catch (Exception e) {
            errors.add("Không thể tải dữ liệu báo cáo lúc này. Vui lòng kiểm tra dữ liệu hoặc kết nối CSDL.");
            System.err.println("Lỗi tải báo cáo /baocao: " + e.getMessage());
        }

        model.addAttribute("tongQuan", tongQuan);
        model.addAttribute("baoCaoSuKienList", baoCaoSuKienList);
        model.addAttribute("hanhViList", hanhViList);
        model.addAttribute("recentHanhViList", hanhViList);
        model.addAttribute("suKienList", suKienList);
        model.addAttribute("suKiens", suKienList);

        model.addAttribute("chartLabels", baoCaoSuKienList.stream()
                .map(row -> row.getTenSK() != null ? row.getTenSK() : row.getMaSK())
                .collect(Collectors.toList()));
        model.addAttribute("chartRevenueData", baoCaoSuKienList.stream()
                .map(row -> row.getDoanhThu() != null ? row.getDoanhThu() : BigDecimal.ZERO)
                .collect(Collectors.toList()));
        model.addAttribute("chartSoldTicketsData", baoCaoSuKienList.stream()
                .map(BaoCaoSuKienDTO::getSoVeDaBan)
                .collect(Collectors.toList()));

        model.addAttribute("maSKSelected", selectedMaSK);
        model.addAttribute("tuNgaySelected", tuNgay);
        model.addAttribute("denNgaySelected", denNgay);
        model.addAttribute("trangThaiSelected", selectedTrangThai);
        model.addAttribute("selectedMaSK", selectedMaSK);
        model.addAttribute("selectedTuNgay", tuNgay);
        model.addAttribute("selectedDenNgay", denNgay);
        model.addAttribute("selectedTrangThaiDonHang", selectedTrangThai);
        model.addAttribute("errorMessage", errors.isEmpty() ? null : String.join(" ", errors));

        return "BaoCao/dashboard";
    }

    private Timestamp parseDateStart(String value, String label, List<String> errors) {
        LocalDate date = parseDate(value, label, errors);
        return date == null ? null : Timestamp.valueOf(date.atStartOfDay());
    }

    private Timestamp parseDateEnd(String value, String label, List<String> errors) {
        LocalDate date = parseDate(value, label, errors);
        return date == null ? null : Timestamp.valueOf(date.atTime(23, 59, 59));
    }

    private LocalDate parseDate(String value, String label, List<String> errors) {
        String clean = normalize(value);
        if (clean == null) {
            return null;
        }
        try {
            return LocalDate.parse(clean);
        } catch (DateTimeParseException e) {
            errors.add(label + " sai định dạng yyyy-MM-dd, hệ thống đã bỏ qua bộ lọc này.");
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }
}
