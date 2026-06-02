package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.DonHang;
import com.dede.ticketsystem.repository.DonHangRepository;
import com.dede.ticketsystem.repository.GiaoDichThanhToanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
public class PaymentService {

    @Autowired
    private DonHangRepository donHangRepository;

    @Autowired
    private GiaoDichThanhToanRepository giaoDichThanhToanRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentAttemptLogService paymentAttemptLogService;

    @Transactional
    public PaymentResult processPayment(String orderId, String maKH, String paymentMethod, String simulateResult) {
        // 2. PaymentService phải khóa đơn hàng
        DonHang dh = donHangRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        // Kiểm tra đơn hàng thuộc maKH hiện tại
        if (!dh.getMaKH().equals(maKH)) {
            throw new RuntimeException("Bạn không có quyền thanh toán đơn hàng này!");
        }

        // 3. Quy tắc xử lý trước khi tạo attempt mới
        // Nếu đơn đã "Đã thanh toán"
        if ("Đã thanh toán".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            bookingService.completePaidOrder(orderId);
            return new PaymentResult(true, false, 0, "Thanh toán thành công", "/ve-cua-toi");
        }

        // Nếu đơn đã "Đã hủy"
        if ("Đã hủy".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            throw new RuntimeException("Đơn hàng đã bị hủy và không thể thanh toán!");
        }

        // Nếu đã có giao dịch "Thành công" cho orderId
        if (giaoDichThanhToanRepository.countByMaDonHangAndTrangThaiGD(orderId, "Thành công") > 0) {
            bookingService.completePaidOrder(orderId);
            return new PaymentResult(true, false, 0, "Thanh toán thành công", "/ve-cua-toi");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        // Nếu đơn đã hết hạn
        if (dh.getThoiGianHetHan() != null && now.after(dh.getThoiGianHetHan())) {
            bookingService.cancelAndReleaseExpiredOrder(orderId);
            throw new RuntimeException("Đơn hàng đã hết hạn thanh toán!");
        }

        // Kiểm tra số lần đã thử
        Integer maxLanThuLai = giaoDichThanhToanRepository.findMaxLanThuLaiByMaDonHang(orderId);
        if (maxLanThuLai == null) {
            maxLanThuLai = 0;
        }

        if (maxLanThuLai >= 3) {
            bookingService.cancelOrder(orderId, maKH);
            throw new RuntimeException("Thanh toán thất bại quá số lần cho phép, đơn hàng đã bị hủy.");
        }

        // 4. Quy tắc attempt count
        int nextAttempt = maxLanThuLai + 1;

        if ("SUCCESS".equalsIgnoreCase(simulateResult) || "Thành công".equalsIgnoreCase(simulateResult)) {
            // double-check success
            if (giaoDichThanhToanRepository.countByMaDonHangAndTrangThaiGD(orderId, "Thành công") > 0) {
                bookingService.completePaidOrder(orderId);
                return new PaymentResult(true, false, 0, "Thanh toán thành công", "/ve-cua-toi");
            }

            // Ghi log giao dịch thành công (Propagation.REQUIRES_NEW)
            paymentAttemptLogService.logAttempt(orderId, dh.getThanhTien(), paymentMethod, "Thành công", nextAttempt, null);

            // Hoàn tất đơn hàng
            bookingService.completePaidOrder(orderId);

            return new PaymentResult(true, false, 0, "Thanh toán thành công", "/ve-cua-toi");
        } else {
            // Ghi log giao dịch thất bại (Propagation.REQUIRES_NEW)
            paymentAttemptLogService.logAttempt(orderId, dh.getThanhTien(), paymentMethod, "Thất bại", nextAttempt, "Thanh toán giả lập thất bại");

            int remainingAttempts = 3 - nextAttempt;

            // 5. Khi simulateResult = FAIL
            if (nextAttempt < 3 && (dh.getThoiGianHetHan() == null || !now.after(dh.getThoiGianHetHan()))) {
                // Giữ DONHANG = "Chờ thanh toán", giữ ghế = "Đang chọn"
                return new PaymentResult(false, true, remainingAttempts, "Thanh toán thất bại, bạn còn " + remainingAttempts + " lần thử lại.", null);
            } else {
                // nextAttempt == 3 hoặc đã hết hạn
                bookingService.cancelOrder(orderId, maKH);
                return new PaymentResult(false, false, 0, "Thanh toán thất bại quá số lần cho phép, đơn hàng đã bị hủy.", "/");
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean canRetry(String orderId, String maKH) {
        DonHang dh = donHangRepository.findById(orderId).orElse(null);
        if (dh == null || maKH == null || !maKH.equals(dh.getMaKH())) {
            return false;
        }
        if (!"Chờ thanh toán".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            return false;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (dh.getThoiGianHetHan() != null && now.after(dh.getThoiGianHetHan())) {
            return false;
        }
        return remainingAttempts(orderId) > 0;
    }

    @Transactional(readOnly = true)
    public int remainingAttempts(String orderId) {
        Integer maxLanThuLai = giaoDichThanhToanRepository.findMaxLanThuLaiByMaDonHang(orderId);
        int tried = maxLanThuLai != null ? maxLanThuLai : 0;
        return Math.max(0, 3 - tried);
    }
}
