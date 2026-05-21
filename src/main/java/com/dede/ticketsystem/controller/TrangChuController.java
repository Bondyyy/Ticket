package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.SuKien;
import com.dede.ticketsystem.model.KhuVuc;
import com.dede.ticketsystem.model.Ghe;
import com.dede.ticketsystem.model.DonHang;
import com.dede.ticketsystem.model.SeatMapDTO;
import com.dede.ticketsystem.service.SuKienService;
import com.dede.ticketsystem.service.SessionService;
import com.dede.ticketsystem.service.BookingService;
import com.dede.ticketsystem.repository.KhuVucRepository;
import com.dede.ticketsystem.repository.GheRepository;
import com.dede.ticketsystem.repository.DonHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class TrangChuController {

    private final SuKienService suKienService;
    private final KhuVucRepository khuVucRepository;
    private final GheRepository gheRepository;
    private final DonHangRepository donHangRepository;
    private final BookingService bookingService;

    @Autowired
    private SessionService sessionService;

    public TrangChuController(SuKienService suKienService, 
                              KhuVucRepository khuVucRepository, 
                              GheRepository gheRepository,
                              DonHangRepository donHangRepository,
                              BookingService bookingService) {
        this.suKienService = suKienService;
        this.khuVucRepository = khuVucRepository;
        this.gheRepository = gheRepository;
        this.donHangRepository = donHangRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/")
    public String trangChu(Model model) {
        return "Public/index";
    }

    @GetMapping("/su-kien")
    public String danhSachSuKien(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String trangThai,
            Model model) {
        List<SuKien> list = suKienService.timKiem(keyword, trangThai);
        model.addAttribute("suKienList", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiFilter", trangThai);
        return "Public/su-kien";
    }

    @Autowired
    private com.dede.ticketsystem.service.LogHanhViService logHanhViService;

    @GetMapping("/su-kien/{maSK}")
    public String chiTietSuKien(@PathVariable String maSK, Model model, jakarta.servlet.http.HttpServletRequest request) {
        SuKien sk = suKienService.timTheoMa(maSK).orElse(null);
        if (sk == null) {
            return "redirect:/";
        }
        
        // Ghi log hành vi XEM_SK
        try {
            String userAgent = request.getHeader("User-Agent");
            String maKH = sessionService.getCurrentMaKH();
            logHanhViService.log("XEM_SK", maSK, maKH, userAgent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<KhuVuc> zones = khuVucRepository.findByMaSK(maSK);
        
        boolean canBuy = false;
        if ("Đang mở bán".equals(sk.getTrangThaiSK())) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            boolean afterMoBan = sk.getThoiGianMoBan() == null || now.after(sk.getThoiGianMoBan());
            boolean beforeDongBan = sk.getThoiGianDongBan() == null || now.before(sk.getThoiGianDongBan());
            if (afterMoBan && beforeDongBan) {
                canBuy = true;
            }
        }
        
        model.addAttribute("suKien", sk);
        model.addAttribute("zones", zones);
        model.addAttribute("canBuy", canBuy);
        return "Public/chi-tiet-su-kien";
    }

    @Autowired
    private com.dede.ticketsystem.service.QueueService queueService;

    @GetMapping("/mua-ve/{maSK}")
    public String chonGhe(@PathVariable String maSK, 
                          @RequestParam(required = false) String queueToken,
                          Model model, 
                          jakarta.servlet.http.HttpServletRequest request) {
        if (!sessionService.isLoggedIn()) {
            String redirectUrl = "/mua-ve/" + maSK;
            if (queueToken != null && !queueToken.trim().isEmpty()) {
                redirectUrl += "?queueToken=" + queueToken;
            }
            return "redirect:/dang-nhap?redirect=" + UriUtils.encode(redirectUrl, StandardCharsets.UTF_8);
        }
        if (!sessionService.hasRole("CUSTOMER")) {
            return "redirect:/";
        }
        
        String maKH = sessionService.getCurrentMaKH();
        if (maKH == null) {
            return "redirect:/";
        }

        // Virtual Queue Check
        if (queueService.shouldQueue(maSK)) {
            if (queueToken == null || !queueService.validateQueueToken(queueToken, maKH, maSK)) {
                return "redirect:/hang-doi/" + maSK;
            }
            model.addAttribute("queueToken", queueToken);
        } else {
            model.addAttribute("queueToken", queueToken != null ? queueToken : "");
        }
        
        // Ghi log hành vi CLICK_DAT_VE
        try {
            String userAgent = request.getHeader("User-Agent");
            logHanhViService.log("CLICK_DAT_VE", maSK, maKH, userAgent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SuKien sk = suKienService.timTheoMa(maSK).orElse(null);
        if (sk == null) {
            return "redirect:/";
        }
        
        SeatMapDTO seatMap = suKienService.getSeatMap(maSK);
        
        model.addAttribute("suKien", sk);
        model.addAttribute("seatMap", seatMap);
        return "Public/chon-ghe";
    }

    @Autowired
    private com.dede.ticketsystem.repository.GiaoDichThanhToanRepository giaoDichThanhToanRepository;

    @GetMapping("/thanh-toan")
    public String thanhToan(@RequestParam(required = false) String orderId, Model model) {
        if (!sessionService.isLoggedIn()) {
            return "redirect:/dang-nhap?redirect=/thanh-toan" + (orderId != null ? "?orderId=" + orderId : "");
        }
        
        String maKH = sessionService.getCurrentMaKH();
        if (!sessionService.hasRole("CUSTOMER") || maKH == null) {
            return "redirect:/";
        }

        if (orderId == null || orderId.trim().isEmpty()) {
            return "redirect:/";
        }

        DonHang dh = donHangRepository.findById(orderId).orElse(null);
        if (dh == null) {
            return "redirect:/";
        }

        // Kiểm tra quyền sở hữu đơn hàng
        if (!dh.getMaKH().equals(maKH)) {
            return "redirect:/";
        }

        // Nếu đơn đã thanh toán thì redirect /ve-cua-toi
        if ("Đã thanh toán".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            return "redirect:/ve-cua-toi";
        }

        // Nếu đơn đã hủy thì redirect /
        if ("Đã hủy".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            return "redirect:/";
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        // Nếu đơn hết hạn thì hủy đơn và redirect /
        if (dh.getThoiGianHetHan() != null && now.after(dh.getThoiGianHetHan())) {
            bookingService.cancelAndReleaseExpiredOrder(orderId);
            return "redirect:/";
        }

        // Lấy số lần thử từ DB
        Integer maxLan = giaoDichThanhToanRepository.findMaxLanThuLaiByMaDonHang(orderId);
        int lanDaThu = (maxLan != null) ? maxLan : 0;
        
        if (lanDaThu >= 3) {
            bookingService.cancelOrder(orderId, maKH);
            return "redirect:/";
        }

        int lanConLai = Math.max(0, 3 - lanDaThu);

        // Lấy lỗi gần nhất nếu có
        String latestError = "";
        List<com.dede.ticketsystem.model.GiaoDichThanhToan> gdList = giaoDichThanhToanRepository.findByMaDonHangOrderByLanThuLaiDesc(orderId);
        for (com.dede.ticketsystem.model.GiaoDichThanhToan gd : gdList) {
            if ("Thất bại".equalsIgnoreCase(gd.getTrangThaiGD())) {
                latestError = gd.getGhiChuLoi();
                break;
            }
        }

        long remainingSeconds = 0;
        if (dh.getThoiGianHetHan() != null) {
            remainingSeconds = (dh.getThoiGianHetHan().getTime() - now.getTime()) / 1000;
        }

        if (remainingSeconds <= 0) {
            bookingService.cancelAndReleaseExpiredOrder(orderId);
            return "redirect:/";
        }

        // Lấy danh sách ghế đang giữ của phiên đơn hàng này
        List<Ghe> gheList = gheRepository.findByMaPhienKhoa(orderId);
        if (gheList.isEmpty()) {
            return "redirect:/";
        }
        
        String maSK = gheList.get(0).getMaSK();
        SuKien sk = suKienService.timTheoMa(maSK).orElse(null);
        
        model.addAttribute("suKien", sk);
        model.addAttribute("gheList", gheList);
        model.addAttribute("tongTien", dh.getThanhTien());
        model.addAttribute("orderId", orderId);
        model.addAttribute("remainingSeconds", remainingSeconds);
        
        model.addAttribute("lanDaThu", lanDaThu);
        model.addAttribute("lanConLai", lanConLai);
        model.addAttribute("latestError", latestError);
        
        return "Public/thanh-toan";
    }
}
