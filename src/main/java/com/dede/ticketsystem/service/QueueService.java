package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.HangDoiAo;
import com.dede.ticketsystem.repository.HangDoiAoRepository;
import com.dede.ticketsystem.repository.DonHangRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QueueService {

    private final HangDoiAoRepository hangDoiAoRepository;
    private final DonHangRepository donHangRepository;
    private final IdGeneratorService idGeneratorService;

    @Value("${queue.threshold.active-holds:100}")
    private int threshold;

    @Value("${queue.allow-next.limit:10}")
    private int allowLimit;

    @Value("${queue.token-ttl-minutes:5}")
    private int tokenTtlMinutes;

    public QueueService(HangDoiAoRepository hangDoiAoRepository,
                        DonHangRepository donHangRepository,
                        IdGeneratorService idGeneratorService) {
        this.hangDoiAoRepository = hangDoiAoRepository;
        this.donHangRepository = donHangRepository;
        this.idGeneratorService = idGeneratorService;
    }

    /**
     * Checks if queuing is needed for the event.
     * CHUẨN ĐANG DÙNG: pendingOrderCount >= threshold để tránh double-count giữa pending orders và locked seats
     * (vì chúng biểu diễn cùng một lượng hold trên hệ thống).
     */
    public boolean shouldQueue(String maSK) {
        long pendingOrders = donHangRepository.countPendingOrdersByMaSK(maSK);
        return pendingOrders >= threshold;
    }

    /**
     * Users join the queue when shouldQueue is true.
     */
    @Transactional
    public HangDoiAo joinQueue(String maKH, String maSK) {
        // String intern to synchronize on the combination of event and customer, avoiding duplicates
        String lockKey = (maSK + "_" + maKH).intern();
        synchronized (lockKey) {
            Optional<HangDoiAo> existing = hangDoiAoRepository.findByMaKHAndMaSKAndTrangThaiIn(
                    maKH, maSK, List.of("Đang chờ", "Được vào")
            );

            if (existing.isPresent()) {
                HangDoiAo record = existing.get();
                if ("Đang chờ".equals(record.getTrangThai())) {
                    return record;
                }
                if ("Được vào".equals(record.getTrangThai())) {
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    if (record.getThoiGianHetHan() != null && record.getThoiGianHetHan().after(now)) {
                        return record;
                    } else {
                        // Mark expired allowed token as "Hết hạn"
                        record.setTrangThai("Hết hạn");
                        hangDoiAoRepository.save(record);
                    }
                }
            }

            // Create a new queue record
            Long maxViTri = hangDoiAoRepository.findMaxViTriHangByMaSKAndTrangThai(maSK);
            long nextViTri = (maxViTri != null ? maxViTri : 0L) + 1;

            Timestamp now = new Timestamp(System.currentTimeMillis());
            HangDoiAo h = new HangDoiAo();
            h.setMaHangDoi(idGeneratorService.nextHangDoiId());
            h.setViTriHang(nextViTri);
            h.setThoiGianVaoHang(now);
            // Estimated wait: now + position * 30 seconds
            h.setThoiGianUocTinh(new Timestamp(now.getTime() + nextViTri * 30 * 1000L));
            h.setTrangThai("Đang chờ");
            
            // Đảm bảo TokenHangDoi phải unique
            String token;
            do {
                token = "TK-" + UUID.randomUUID().toString().toUpperCase();
            } while (hangDoiAoRepository.findByTokenHangDoi(token).isPresent());
            h.setTokenHangDoi(token);

            // Position wait duration expires in 30 minutes if they never get allowed
            h.setThoiGianHetHan(new Timestamp(now.getTime() + 30 * 60 * 1000L));
            h.setMaKH(maKH);
            h.setMaSK(maSK);

            return hangDoiAoRepository.save(h);
        }
    }

    /**
     * Validates if a token is valid, matches the customer and event, is allowed and not expired.
     */
    @Transactional
    public boolean validateQueueToken(String token, String maKH, String maSK) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        Optional<HangDoiAo> opt = hangDoiAoRepository.findByTokenHangDoi(token);
        if (opt.isEmpty()) {
            return false;
        }
        HangDoiAo h = opt.get();
        if (!h.getMaKH().equals(maKH) || !h.getMaSK().equals(maSK)) {
            return false;
        }
        if (!"Được vào".equals(h.getTrangThai())) {
            return false;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (h.getThoiGianHetHan() == null || h.getThoiGianHetHan().before(now)) {
            h.setTrangThai("Hết hạn");
            hangDoiAoRepository.save(h);
            return false;
        }
        return true;
    }

    /**
     * Consumes (invalidates) a token after successful booking.
     */
    @Transactional
    public void consumeToken(String token, String maKH, String maSK) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }
        Optional<HangDoiAo> opt = hangDoiAoRepository.findByTokenHangDoi(token);
        if (opt.isPresent()) {
            HangDoiAo h = opt.get();
            if (h.getMaKH().equals(maKH) && h.getMaSK().equals(maSK)) {
                h.setTrangThai("Hết hạn");
                hangDoiAoRepository.save(h);
            }
        }
    }

    /**
     * Promotes waitlisted users to `'Được vào'` status.
     */
    @Transactional
    public void allowNextUsers(String maSK, int limit) {
        List<HangDoiAo> waiting = hangDoiAoRepository.findByMaSKAndTrangThaiOrderByViTriHangAsc(maSK, "Đang chờ");
        int count = 0;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expires = new Timestamp(now.getTime() + tokenTtlMinutes * 60 * 1000L);

        for (HangDoiAo h : waiting) {
            if (count >= limit) {
                break;
            }
            // Không allow người đã hết hạn
            if (h.getThoiGianHetHan() != null && h.getThoiGianHetHan().before(now)) {
                h.setTrangThai("Hết hạn");
                hangDoiAoRepository.save(h);
                continue;
            }
            h.setTrangThai("Được vào");
            h.setThoiGianHetHan(expires);
            hangDoiAoRepository.save(h);
            count++;
        }
    }

    /**
     * Expire old queue entries (older than current timestamp or allowed tokens that have passed ttl).
     */
    @Transactional
    public void expireOldQueueTokens() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        List<HangDoiAo> allActive = hangDoiAoRepository.findAll();
        for (HangDoiAo h : allActive) {
            if (("Đang chờ".equals(h.getTrangThai()) || "Được vào".equals(h.getTrangThai()))
                    && h.getThoiGianHetHan() != null && h.getThoiGianHetHan().before(now)) {
                h.setTrangThai("Hết hạn");
                hangDoiAoRepository.save(h);
            }
        }
    }

    /**
     * Helper to find a queue record by its token.
     */
    public Optional<HangDoiAo> findByToken(String token) {
        return hangDoiAoRepository.findByTokenHangDoi(token);
    }

    /**
     * Helper method to calculate relative position in queue.
     */
    public long getPosition(String maSK, Timestamp entryTime) {
        return hangDoiAoRepository.calculateCurrentPosition(maSK, entryTime);
    }

    /**
     * Scheduler running every 30 seconds to clean up expired entries and push queues.
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void runScheduler() {
        // 1. Expire old tokens
        expireOldQueueTokens();

        // 2. Allow next batch of users for each active waitlist event
        List<String> activeEvents = hangDoiAoRepository.findDistinctMaSKDangCho();
        for (String maSK : activeEvents) {
            allowNextUsers(maSK, allowLimit);
        }
    }
}
