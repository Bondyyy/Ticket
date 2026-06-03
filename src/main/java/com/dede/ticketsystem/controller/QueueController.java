package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.HangDoiAo;
import com.dede.ticketsystem.model.SuKien;
import com.dede.ticketsystem.repository.DiaDiemRepository;
import com.dede.ticketsystem.service.QueueService;
import com.dede.ticketsystem.service.SessionService;
import com.dede.ticketsystem.service.SuKienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

@Controller
public class QueueController {

    @Autowired
    private QueueService queueService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SuKienService suKienService;

    @Autowired
    private DiaDiemRepository diaDiemRepository;

    @GetMapping("/hang-doi/{maSK}")
    public String viewQueuePage(@PathVariable String maSK, Model model) {
        if (!sessionService.isLoggedIn()) {
            return "redirect:/dang-nhap?redirect=/hang-doi/" + maSK;
        }
        if (!sessionService.hasRole("CUSTOMER")) {
            return "redirect:/";
        }
        String maKH = sessionService.getCurrentMaKH();
        if (maKH == null) {
            return "redirect:/";
        }

        SuKien sk = suKienService.timTheoMa(maSK).orElse(null);
        if (sk == null) {
            return "redirect:/";
        }

        // If shouldQueue is false, redirect user straight to seat selection
        if (!queueService.shouldQueue(maSK)) {
            return "redirect:/mua-ve/" + maSK;
        }

        // Join queue (returns existing if already waitlisted or allowed, or creates new)
        HangDoiAo h = queueService.joinQueue(maKH, maSK);

        // If user is already allowed and not expired, redirect to seat booking immediately
        if ("Được vào".equals(h.getTrangThai()) && h.getThoiGianHetHan() != null && h.getThoiGianHetHan().after(new Timestamp(System.currentTimeMillis()))) {
            return "redirect:/mua-ve/" + maSK + "?queueToken=" + h.getTokenHangDoi();
        }

        long currentPos = 0;
        if ("Đang chờ".equals(h.getTrangThai())) {
            currentPos = queueService.getPosition(maSK, h.getThoiGianVaoHang());
        }

        model.addAttribute("suKien", sk);
        model.addAttribute("diaDiem", sk.getMaDiaDiem() != null ? diaDiemRepository.findById(sk.getMaDiaDiem()).orElse(null) : null);
        model.addAttribute("queueRecord", h);
        model.addAttribute("currentPosition", currentPos);
        return "Public/hang-doi";
    }

    @GetMapping("/api/hang-doi/status")
    @ResponseBody
    public ResponseEntity<?> getQueueStatus(@RequestParam String token) {
        if (!sessionService.isLoggedIn()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Bạn cần đăng nhập"));
        }
        String maKH = sessionService.getCurrentMaKH();
        if (maKH == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Khách hàng không hợp lệ"));
        }

        Optional<HangDoiAo> opt = queueService.findByToken(token);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Không tìm thấy token hàng đợi"));
        }

        HangDoiAo h = opt.get();
        if (!h.getMaKH().equals(maKH)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Không có quyền truy cập token này"));
        }

        // Automatically expire on poll if past TTL
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if ("Được vào".equals(h.getTrangThai()) && h.getThoiGianHetHan() != null && h.getThoiGianHetHan().before(now)) {
            h.setTrangThai("Hết hạn");
            // Perform silent update
            queueService.expireOldQueueTokens();
        }

        long currentPos = 0;
        if ("Đang chờ".equals(h.getTrangThai())) {
            currentPos = queueService.getPosition(h.getMaSK(), h.getThoiGianVaoHang());
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "trangThai", h.getTrangThai(),
                "viTriHang", currentPos,
                "thoiGianUocTinh", h.getThoiGianUocTinh() != null ? h.getThoiGianUocTinh().getTime() : 0,
                "tokenHangDoi", h.getTokenHangDoi()
        ));
    }

    @PostMapping("/api/hang-doi/allow-next")
    @ResponseBody
    public ResponseEntity<?> allowNext(@RequestParam String maSK, @RequestParam int limit) {
        // Admin security check
        if (!sessionService.isLoggedIn() || (!sessionService.hasRole("ADMIN") && !sessionService.hasRole("ORGANIZER"))) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Không có quyền thực hiện"));
        }

        try {
            queueService.allowNextUsers(maSK, limit);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã thúc đẩy hàng đợi thành công cho " + limit + " người"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
