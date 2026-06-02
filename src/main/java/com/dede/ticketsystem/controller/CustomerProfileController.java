package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.*;
import com.dede.ticketsystem.repository.*;
import com.dede.ticketsystem.service.SessionService;
import com.dede.ticketsystem.service.VeService;
import com.dede.ticketsystem.service.QRCodeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class CustomerProfileController {

    private final SessionService sessionService;
    private final VeService veService;
    private final QRCodeService qrCodeService;
    private final VeRepository veRepository;
    private final DonHangRepository donHangRepository;
    private final SuKienRepository suKienRepository;
    private final GheRepository gheRepository;
    private final KhuVucRepository khuVucRepository;

    public CustomerProfileController(SessionService sessionService,
                                     VeService veService,
                                     QRCodeService qrCodeService,
                                     VeRepository veRepository,
                                     DonHangRepository donHangRepository,
                                     SuKienRepository suKienRepository,
                                     GheRepository gheRepository,
                                     KhuVucRepository khuVucRepository) {
        this.sessionService = sessionService;
        this.veService = veService;
        this.qrCodeService = qrCodeService;
        this.veRepository = veRepository;
        this.donHangRepository = donHangRepository;
        this.suKienRepository = suKienRepository;
        this.gheRepository = gheRepository;
        this.khuVucRepository = khuVucRepository;
    }

    @GetMapping("/ve-cua-toi")
    public String veCuaToi(Model model) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole("CUSTOMER")) {
            return "redirect:/dang-nhap";
        }

        String maKH = sessionService.getCurrentMaKH();
        if (maKH == null) {
            return "redirect:/dang-nhap";
        }

        List<Ve> listVe = veRepository.findByMaKH(maKH);
        List<VeCuaToiDTO> dtos = new ArrayList<>();

        for (Ve ve : listVe) {
            VeCuaToiDTO dto = mapToVeCuaToiDTO(ve, 150, 150);
            dtos.add(dto);
        }

        model.addAttribute("tickets", dtos);
        return "Public/ve-cua-toi";
    }

    @GetMapping("/ve-cua-toi/{maVe}")
    public String chiTietVe(@PathVariable("maVe") String maVe, Model model, HttpServletResponse response) throws Exception {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole("CUSTOMER")) {
            return "redirect:/dang-nhap";
        }

        String maKH = sessionService.getCurrentMaKH();
        if (maKH == null) {
            return "redirect:/dang-nhap";
        }

        // Truy vấn bảo mật vé thuộc khách hàng hiện tại
        Optional<Ve> veOpt = veRepository.findByMaVeAndMaKH(maVe, maKH);
        if (veOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập vé này hoặc vé không tồn tại.");
            return null;
        }

        Ve ve = veOpt.get();
        VeCuaToiDTO dto = mapToVeCuaToiDTO(ve, 300, 300);

        model.addAttribute("ticket", dto);
        return "Public/chi-tiet-ve";
    }

    @GetMapping("/don-hang-cua-toi")
    public String donHangCuaToi(Model model) {
        if (!sessionService.isLoggedIn() || !sessionService.hasRole("CUSTOMER")) {
            return "redirect:/dang-nhap";
        }

        String maKH = sessionService.getCurrentMaKH();
        if (maKH == null) {
            return "redirect:/dang-nhap";
        }

        List<DonHang> orders = donHangRepository.findByMaKHOrderByThoiGianDatDesc(maKH);
        Map<String, List<VeCuaToiDTO>> orderTicketsMap = new HashMap<>();

        for (DonHang dh : orders) {
            List<Ve> listVe = veRepository.findByMaDonHang(dh.getMaDonHang());
            List<VeCuaToiDTO> dtos = new ArrayList<>();
            for (Ve ve : listVe) {
                // Không cần generate QR code lớn, chỉ cần map thông tin cơ bản
                dtos.add(mapToVeCuaToiDTO(ve, 80, 80));
            }
            orderTicketsMap.put(dh.getMaDonHang(), dtos);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderTicketsMap", orderTicketsMap);
        return "Public/don-hang-cua-toi";
    }

    private VeCuaToiDTO mapToVeCuaToiDTO(Ve ve, int qrWidth, int qrHeight) {
        VeCuaToiDTO dto = new VeCuaToiDTO();
        dto.setMaVe(ve.getMaVe());
        dto.setGiaVe(ve.getGiaVe());
        dto.setTrangThaiVe(ve.getTrangThaiVe());

        // Lấy thông tin Sự kiện
        SuKien sk = suKienRepository.findById(ve.getMaSK()).orElse(null);
        if (sk != null) {
            dto.setTenSuKien(sk.getTenSK());
            dto.setThoiGianBatDau(sk.getThoiGianBatDau());
        } else {
            dto.setTenSuKien("Sự kiện không xác định");
        }

        // Lấy thông tin Ghế và Khu Vực
        Ghe ghe = ve.getMaGhe() != null ? gheRepository.findById(ve.getMaGhe()).orElse(null) : null;
        if (ghe != null) {
            dto.setTenGhe(ghe.getTenGhe());
            KhuVuc kv = khuVucRepository.findById(ghe.getMaKhuVuc()).orElse(null);
            if (kv != null) {
                dto.setTenKhuVuc(kv.getTenKhuVuc());
            } else {
                dto.setTenKhuVuc("Khu vực không xác định");
            }
        } else {
            dto.setTenGhe("Không áp dụng ghế");
            if (ve.getMaKhuVuc() != null) {
                KhuVuc kv = khuVucRepository.findById(ve.getMaKhuVuc()).orElse(null);
                dto.setTenKhuVuc(kv != null ? kv.getTenKhuVuc() : "Khu đứng");
            } else {
                dto.setTenKhuVuc("Khu đứng");
            }
        }

        // Tạo QR Payload và Base64 Image
        String payload = qrCodeService.buildTicketPayload(ve);
        dto.setQrPayload(payload);
        
        String qrBase64 = qrCodeService.generateQRCodeBase64(payload, qrWidth, qrHeight);
        dto.setQrBase64(qrBase64);

        return dto;
    }
}
