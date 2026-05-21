package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.GiaoDichThanhToan;
import com.dede.ticketsystem.repository.GiaoDichThanhToanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Service
public class PaymentAttemptLogService {

    @Autowired
    private GiaoDichThanhToanRepository giaoDichThanhToanRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GiaoDichThanhToan logAttempt(
        String orderId,
        BigDecimal amount,
        String paymentMethod,
        String status,
        int lanThuLai,
        String ghiChuLoi
    ) {
        GiaoDichThanhToan gd = new GiaoDichThanhToan();
        gd.setMaGiaoDich(idGeneratorService.nextGiaoDichId());
        gd.setSoTienThanhToan(amount);
        gd.setPhuongThucTT(paymentMethod);
        gd.setTrangThaiGD(status);
        gd.setLanThuLai(lanThuLai);
        gd.setThoiGianThucHien(new Timestamp(System.currentTimeMillis()));
        gd.setMaDonHang(orderId);
        if ("Thành công".equalsIgnoreCase(status)) {
            gd.setMaGiaoDichBenThu3("MOCK-SUCCESS-" + UUID.randomUUID().toString().substring(0, 8));
        } else {
            gd.setGhiChuLoi(ghiChuLoi);
        }
        return giaoDichThanhToanRepository.save(gd);
    }
}
