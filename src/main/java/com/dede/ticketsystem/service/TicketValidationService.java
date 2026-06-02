package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.*;
import com.dede.ticketsystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class TicketValidationService {

    private final VeService veService;
    private final VeRepository veRepository;
    private final LichSuSoatVeRepository lichSuSoatVeRepository;
    private final GheRepository gheRepository;
    private final KhuVucRepository khuVucRepository;
    private final DonHangRepository donHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final IdGeneratorService idGeneratorService;

    public TicketValidationService(VeService veService, VeRepository veRepository,
                                   LichSuSoatVeRepository lichSuSoatVeRepository, GheRepository gheRepository,
                                   KhuVucRepository khuVucRepository, DonHangRepository donHangRepository,
                                   KhachHangRepository khachHangRepository,
                                   IdGeneratorService idGeneratorService) {
        this.veService = veService;
        this.veRepository = veRepository;
        this.lichSuSoatVeRepository = lichSuSoatVeRepository;
        this.gheRepository = gheRepository;
        this.khuVucRepository = khuVucRepository;
        this.donHangRepository = donHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.idGeneratorService = idGeneratorService;
    }

    @Transactional
    public ValidationResult validateQr(String qrPayloadOrCode, String maSK, String maNV, String congSoat, String nguonDuLieu) {
        return validateQr(qrPayloadOrCode, maSK, maNV, congSoat, nguonDuLieu, null);
    }

    @Transactional
    public ValidationResult validateQr(String qrPayloadOrCode, String maSK, String maNV, String congSoat, String nguonDuLieu, Timestamp thoiGianQuetParam) {
        long startTime = System.currentTimeMillis();
        Timestamp thoiGianQuet = thoiGianQuetParam != null ? thoiGianQuetParam : new Timestamp(startTime);
        String normalizedMaSK = normalize(maSK);
        String normalizedCongSoat = normalize(congSoat) != null ? normalize(congSoat) : "Cổng chính";
        String normalizedNguon = "Offline".equalsIgnoreCase(normalize(nguonDuLieu)) ? "Offline" : "Online";

        // Tìm và khóa vé ngay từ bước lookup để chống hai request cùng hợp lệ.
        Optional<Ve> veOpt = veService.parsePayloadAndFindVeWithLock(qrPayloadOrCode);

        if (veOpt.isEmpty()) {
            String status = "Vé không tìm thấy";
            saveScanHistory(status, normalizedCongSoat, normalizedNguon, thoiGianQuet, null, maNV);
            long duration = System.currentTimeMillis() - startTime;
            ValidationResult result = new ValidationResult(false, status, null, null, null, null, duration, thoiGianQuet, normalizedMaSK);
            result.setNguonDuLieu(normalizedNguon);
            return result;
        }

        Ve lockedVe = veOpt.get();

        // 3. Lấy thông tin chi tiết (ghế, khu vực, người mua)
        String seatName = null;
        String zoneName = null;
        String ticketOwner = null;

        if (lockedVe.getMaGhe() != null) {
            Ghe ghe = gheRepository.findById(lockedVe.getMaGhe()).orElse(null);
            if (ghe != null) {
                seatName = ghe.getTenGhe();
                KhuVuc kv = khuVucRepository.findById(ghe.getMaKhuVuc()).orElse(null);
                if (kv != null) {
                    zoneName = kv.getTenKhuVuc();
                }
            }
        } else if (lockedVe.getMaKhuVuc() != null) {
            KhuVuc kv = khuVucRepository.findById(lockedVe.getMaKhuVuc()).orElse(null);
            if (kv != null) {
                seatName = "Không áp dụng ghế";
                zoneName = kv.getTenKhuVuc();
            }
        }

        if (lockedVe.getMaDonHang() != null) {
            DonHang dh = donHangRepository.findById(lockedVe.getMaDonHang()).orElse(null);
            if (dh != null && dh.getMaKH() != null) {
                KhachHang kh = khachHangRepository.findById(dh.getMaKH()).orElse(null);
                if (kh != null) {
                    ticketOwner = kh.getHoTen();
                }
            }
        }

        // 4. Các bước logic xác thực
        String status;
        boolean success = false;

        String ticketStatus = normalize(lockedVe.getTrangThaiVe());
        if (lockedVe.getMaSK() == null || !lockedVe.getMaSK().equals(normalizedMaSK)) {
            status = "Sai sự kiện";
        } else if ("Đã sử dụng".equals(ticketStatus)) {
            status = "Vé đã sử dụng";
        } else if ("Đã hủy".equals(ticketStatus)) {
            status = "Vé giả";
        } else if ("Chưa sử dụng".equals(ticketStatus)) {
            status = "Hợp lệ";
            success = true;

            lockedVe.setTrangThaiVe("Đã sử dụng");
            lockedVe.setThoiGianSuDung(thoiGianQuet);
            veRepository.save(lockedVe);
        } else {
            status = "Vé giả";
        }

        saveScanHistory(status, normalizedCongSoat, normalizedNguon, thoiGianQuet, lockedVe.getMaVe(), maNV);

        long duration = System.currentTimeMillis() - startTime;
        ValidationResult result = new ValidationResult(success, status, lockedVe.getMaVe(), seatName, zoneName, ticketOwner, duration, thoiGianQuet, lockedVe.getMaSK());
        result.setNguonDuLieu(normalizedNguon);
        return result;
    }

    private void saveScanHistory(String status, String congSoat, String nguonDuLieu, Timestamp thoiGianQuet, String maVe, String maNV) {
        LichSuSoatVe l = new LichSuSoatVe();
        l.setMaLichSu(idGeneratorService.nextLichSuSoatVeId());
        l.setThoiGianQuet(thoiGianQuet);
        l.setKetQuaQuet(status);
        l.setCongSoat(congSoat);
        l.setNguonDuLieu(nguonDuLieu);
        l.setDaDongBo("Y");
        l.setThoiGianDongBo(new Timestamp(System.currentTimeMillis()));
        l.setMaVe(maVe);
        l.setMaNV(normalize(maNV));

        lichSuSoatVeRepository.save(l);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }

    @Transactional
    public void resetTestData() {
        // Reset VE_SK001_A01_001
        veRepository.findById("VE_SK001_A01_001").ifPresent(v -> {
            v.setTrangThaiVe("Chưa sử dụng");
            v.setThoiGianSuDung(null);
            veRepository.save(v);
        });
        // Reset VE_SK001_VIP01_001
        veRepository.findById("VE_SK001_VIP01_001").ifPresent(v -> {
            v.setTrangThaiVe("Chưa sử dụng");
            v.setThoiGianSuDung(null);
            veRepository.save(v);
        });
        // Xóa lịch sử soát vé
        lichSuSoatVeRepository.deleteAll();
    }
}
