package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.DonHang;
import com.dede.ticketsystem.repository.DonHangRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Component
public class SeatLockCleanupTask {

    private final DonHangRepository donHangRepository;
    private final BookingService bookingService;

    public SeatLockCleanupTask(DonHangRepository donHangRepository, BookingService bookingService) {
        this.donHangRepository = donHangRepository;
        this.bookingService = bookingService;
    }

    // Chạy mỗi 60 giây (60000 ms)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredOrders() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        // Tìm các đơn hàng "Chờ thanh toán" đã quá thời gian hết hạn
        List<DonHang> expiredOrders = donHangRepository.findExpiredPendingOrders(now);
        
        if (!expiredOrders.isEmpty()) {
            for (DonHang dh : expiredOrders) {
                try {
                    bookingService.cancelAndReleaseExpiredOrder(dh.getMaDonHang());
                    System.out.println("[SeatLockCleanupTask] Đã hủy đơn hàng quá hạn: " + dh.getMaDonHang());
                } catch (Exception e) {
                    System.err.println("[SeatLockCleanupTask] Lỗi khi hủy đơn hàng quá hạn " + dh.getMaDonHang() + ": " + e.getMessage());
                }
            }
        }
    }
}
