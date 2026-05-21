package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.DonHang;
import com.dede.ticketsystem.model.Ve;
import com.dede.ticketsystem.model.LichSuGuiEmail;
import com.dede.ticketsystem.repository.LichSuGuiEmailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
public class EmailService {

    private final LichSuGuiEmailRepository lichSuGuiEmailRepository;
    private final IdGeneratorService idGeneratorService;

    public EmailService(LichSuGuiEmailRepository lichSuGuiEmailRepository, IdGeneratorService idGeneratorService) {
        this.lichSuGuiEmailRepository = lichSuGuiEmailRepository;
        this.idGeneratorService = idGeneratorService;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void sendOrderConfirmationEmail(String email, DonHang donHang) {
        if (donHang == null || email == null || email.trim().isEmpty()) {
            return;
        }

        // Kiểm tra trùng: XAC_NHAN_DON_HANG mỗi đơn hàng chỉ ghi một lần
        if (lichSuGuiEmailRepository.existsByLoaiEmailAndMaDonHang("XAC_NHAN_DON_HANG", donHang.getMaDonHang())) {
            System.out.println("Email log trùng: Đã tồn tại log XAC_NHAN_DON_HANG cho đơn hàng " + donHang.getMaDonHang());
            return;
        }

        LichSuGuiEmail log = new LichSuGuiEmail();
        log.setMaEmail(idGeneratorService.nextEmailLogId());
        log.setLoaiEmail("XAC_NHAN_DON_HANG");
        log.setDiaChiNhan(email);
        log.setTrangThai("Da_gui");
        log.setSoLanThu(1);
        log.setThoiGianGui(new Timestamp(System.currentTimeMillis()));
        log.setMaDonHang(donHang.getMaDonHang());

        lichSuGuiEmailRepository.save(log);

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[EMAIL SIMULATION - XAC_NHAN_DON_HANG]");
        System.out.println("Đã gửi email xác nhận thành công cho: " + email);
        System.out.println("Nội dung: Chúc mừng quý khách đã thanh toán thành công đơn hàng " + donHang.getMaDonHang());
        System.out.println("Tổng tiền: " + donHang.getThanhTien() + " VND");
        System.out.println("Ghi bản ghi vào bảng LICHSUGUI_EMAIL thành công với ID: " + log.getMaEmail());
        System.out.println("--------------------------------------------------------------------------------");
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void sendTicketQREmail(String email, Ve ve) {
        if (ve == null || email == null || email.trim().isEmpty()) {
            return;
        }

        // Kiểm tra trùng: QR_CODE mỗi vé chỉ ghi một lần
        if (lichSuGuiEmailRepository.existsByLoaiEmailAndMaVe("QR_CODE", ve.getMaVe())) {
            System.out.println("Email log trùng: Đã tồn tại log QR_CODE cho vé " + ve.getMaVe());
            return;
        }

        LichSuGuiEmail log = new LichSuGuiEmail();
        log.setMaEmail(idGeneratorService.nextEmailLogId());
        log.setLoaiEmail("QR_CODE");
        log.setDiaChiNhan(email);
        log.setTrangThai("Da_gui");
        log.setSoLanThu(1);
        log.setThoiGianGui(new Timestamp(System.currentTimeMillis()));
        log.setMaVe(ve.getMaVe());
        log.setMaDonHang(ve.getMaDonHang());

        lichSuGuiEmailRepository.save(log);

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[EMAIL SIMULATION - QR_CODE]");
        System.out.println("Đã gửi email QR Code thành công cho: " + email);
        System.out.println("Nội dung: Chi tiết vé của quý khách là: " + ve.getMaVe() + ". Hãy dùng mã QR đính kèm để soát vé!");
        System.out.println("Ghi bản ghi vào bảng LICHSUGUI_EMAIL thành công với ID: " + log.getMaEmail());
        System.out.println("--------------------------------------------------------------------------------");
    }
}
