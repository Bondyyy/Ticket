package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.DonHang;
import com.dede.ticketsystem.model.Ghe;
import com.dede.ticketsystem.model.KhachHang;
import com.dede.ticketsystem.model.LichSuSoatVe;
import com.dede.ticketsystem.model.SuKien;
import com.dede.ticketsystem.model.Ve;
import com.dede.ticketsystem.model.VeDTO;
import com.dede.ticketsystem.model.VeQuanLyDTO;
import com.dede.ticketsystem.repository.DonHangRepository;
import com.dede.ticketsystem.repository.GheRepository;
import com.dede.ticketsystem.repository.KhachHangRepository;
import com.dede.ticketsystem.repository.LichSuSoatVeRepository;
import com.dede.ticketsystem.repository.SuKienRepository;
import com.dede.ticketsystem.repository.VeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class VeService {

    private final VeRepository veRepository;
    private final DonHangRepository donHangRepository;
    private final GheRepository gheRepository;
    private final SuKienRepository suKienRepository;
    private final KhachHangRepository khachHangRepository;
    private final LichSuSoatVeRepository lichSuSoatVeRepository;
    private final IdGeneratorService idGeneratorService;

    public VeService(VeRepository veRepository,
                     DonHangRepository donHangRepository,
                     GheRepository gheRepository,
                     SuKienRepository suKienRepository,
                     KhachHangRepository khachHangRepository,
                     LichSuSoatVeRepository lichSuSoatVeRepository,
                     IdGeneratorService idGeneratorService) {
        this.veRepository = veRepository;
        this.donHangRepository = donHangRepository;
        this.gheRepository = gheRepository;
        this.suKienRepository = suKienRepository;
        this.khachHangRepository = khachHangRepository;
        this.lichSuSoatVeRepository = lichSuSoatVeRepository;
        this.idGeneratorService = idGeneratorService;
    }

    public List<Ve> layTatCa() {
        return veRepository.findAll();
    }

    public List<Ve> timKiem(String keyword, String trangThai) {
        return veRepository.search(keyword, trangThai);
    }

    public List<VeQuanLyDTO> timKiemQuanLy(String keyword, String trangThai) {
        String cleanKeyword = normalize(keyword);
        String cleanTrangThai = normalize(trangThai);
        return veRepository.findAll().stream()
                .map(this::toQuanLyDTO)
                .filter(dto -> cleanTrangThai == null || cleanTrangThai.equals(dto.getTrangThaiVe()))
                .filter(dto -> cleanKeyword == null || matchesKeyword(dto, cleanKeyword))
                .sorted(Comparator.comparing(VeQuanLyDTO::getThoiGianPhat, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public Optional<Ve> timTheoMa(String maVe) {
        return veRepository.findById(maVe);
    }

    public Ve taoVe(VeDTO dto) {
        Ve ve = new Ve();
        
        // Auto-generate MaVe if not provided
        if (dto.getMaVe() == null || dto.getMaVe().trim().isEmpty()) {
            ve.setMaVe(idGeneratorService.nextVeId());
        } else {
            if (veRepository.existsById(dto.getMaVe())) {
                throw new RuntimeException("Mã vé đã tồn tại!");
            }
            ve.setMaVe(dto.getMaVe());
        }

        // Auto-generate QR code if not provided
        if (dto.getMaQR() == null || dto.getMaQR().trim().isEmpty()) {
            ve.setMaQR(buildQrCode(ve.getMaVe()));
        } else {
            if (veRepository.existsByMaQR(dto.getMaQR())) {
                throw new RuntimeException("Mã QR đã tồn tại!");
            }
            ve.setMaQR(dto.getMaQR());
        }

        ve.setGiaVe(dto.getGiaVe());
        ve.setTrangThaiVe(dto.getTrangThaiVe() == null ? "Chưa sử dụng" : dto.getTrangThaiVe());
        ve.setThoiGianPhat(new Timestamp(System.currentTimeMillis()));
        
        if (dto.getThoiGianSuDung() != null && !dto.getThoiGianSuDung().isBlank()) {
            ve.setThoiGianSuDung(parseTimestampOrThrow("Thời gian sử dụng", dto.getThoiGianSuDung()));
        }
        
        ve.setMaDonHang(dto.getMaDonHang() != null && dto.getMaDonHang().isBlank() ? null : dto.getMaDonHang());
        ve.setMaGhe(dto.getMaGhe() != null && dto.getMaGhe().isBlank() ? null : dto.getMaGhe());
        ve.setMaSK(dto.getMaSK() != null && dto.getMaSK().isBlank() ? null : dto.getMaSK());

        return veRepository.save(ve);
    }

    public Ve capNhatVe(String maVe, VeDTO dto) {
        Ve ve = veRepository.findById(maVe)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé: " + maVe));

        if (dto.getGiaVe() != null) ve.setGiaVe(dto.getGiaVe());
        if (dto.getTrangThaiVe() != null) ve.setTrangThaiVe(dto.getTrangThaiVe());
        
        if (dto.getThoiGianSuDung() != null && !dto.getThoiGianSuDung().isBlank()) {
            ve.setThoiGianSuDung(parseTimestampOrThrow("Thời gian sử dụng", dto.getThoiGianSuDung()));
        }
        
        if (dto.getMaDonHang() != null) ve.setMaDonHang(dto.getMaDonHang().isBlank() ? null : dto.getMaDonHang());
        if (dto.getMaGhe() != null) ve.setMaGhe(dto.getMaGhe().isBlank() ? null : dto.getMaGhe());
        if (dto.getMaSK() != null) ve.setMaSK(dto.getMaSK().isBlank() ? null : dto.getMaSK());
        
        // Update QR Code only if explicitly provided and different
        if (dto.getMaQR() != null && !dto.getMaQR().isBlank() && !dto.getMaQR().equals(ve.getMaQR())) {
            if (veRepository.existsByMaQR(dto.getMaQR())) {
                throw new RuntimeException("Mã QR đã tồn tại trên một vé khác!");
            }
            ve.setMaQR(dto.getMaQR());
        }

        return veRepository.save(ve);
    }

    public void huyVe(String maVe) {
        Ve ve = veRepository.findById(maVe)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé: " + maVe));
        
        ve.setTrangThaiVe("Đã hủy");
        veRepository.save(ve);
    }

    public List<Ve> layVeCuaKhachHang(String maKH) {
        return veRepository.findByMaKH(maKH);
    }

    public Optional<Ve> parsePayloadAndFindVe(String payloadOrCode) {
        return parsePayloadAndFindVe(payloadOrCode, false);
    }

    @Transactional
    public String giaLapSuDungVe(String maVe, String maNV) {
        Ve ve = veRepository.findByMaVeWithLock(maVe)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé: " + maVe));

        String trangThai = ve.getTrangThaiVe();
        if ("Đã sử dụng".equals(trangThai)) {
            return "Vé đã được sử dụng trước đó.";
        }
        if ("Đã hủy".equals(trangThai)) {
            throw new RuntimeException("Vé đã hủy, không thể giả lập sử dụng.");
        }
        if (!"Chưa sử dụng".equals(trangThai)) {
            throw new RuntimeException("Chỉ có thể giả lập dùng vé ở trạng thái Chưa sử dụng.");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        ve.setTrangThaiVe("Đã sử dụng");
        ve.setThoiGianSuDung(now);
        veRepository.save(ve);

        LichSuSoatVe history = new LichSuSoatVe();
        history.setMaLichSu(idGeneratorService.nextLichSuSoatVeId());
        history.setThoiGianQuet(now);
        history.setKetQuaQuet("Hợp lệ");
        history.setCongSoat("WEB_ADMIN");
        history.setNguonDuLieu("Online");
        history.setDaDongBo("Y");
        history.setThoiGianDongBo(now);
        history.setMaVe(ve.getMaVe());
        history.setMaNV(normalize(maNV));
        lichSuSoatVeRepository.save(history);

        return "Đã giả lập vé " + maVe + " được sử dụng thành công.";
    }

    public Optional<Ve> parsePayloadAndFindVeWithLock(String payloadOrCode) {
        return parsePayloadAndFindVe(payloadOrCode, true);
    }

    private Optional<Ve> parsePayloadAndFindVe(String payloadOrCode, boolean withLock) {
        if (payloadOrCode == null || payloadOrCode.trim().isEmpty()) {
            return Optional.empty();
        }

        String input = payloadOrCode.trim();

        if (input.startsWith("TICKET|")) {
            String[] parts = input.split("\\|");
            String parsedMaVe = null;
            String parsedMaQR = null;
            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length != 2) {
                    continue;
                }
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if ("maVe".equalsIgnoreCase(key)) {
                    parsedMaVe = value;
                } else if ("maQR".equalsIgnoreCase(key)) {
                    parsedMaQR = value;
                }
            }

            // Full ticket payloads are authoritative: when maVe exists, lock by maVe only.
            // This avoids accepting a forged payload with a fake maVe but a real maQR.
            if (parsedMaVe != null && !parsedMaVe.trim().isEmpty()) {
                return findByMaVe(parsedMaVe.trim(), withLock);
            }

            if (parsedMaQR != null && !parsedMaQR.trim().isEmpty()) {
                return findByMaQR(parsedMaQR.trim(), withLock);
            }

            return Optional.empty();
        }

        Optional<Ve> veByQR = findByMaQR(input, withLock);
        if (veByQR.isPresent()) {
            return veByQR;
        }

        return findByMaVe(input, withLock);
    }

    private Optional<Ve> findByMaVe(String maVe, boolean withLock) {
        return withLock ? veRepository.findByMaVeWithLock(maVe) : veRepository.findById(maVe);
    }

    private Optional<Ve> findByMaQR(String maQR, boolean withLock) {
        return withLock ? veRepository.findByMaQRWithLock(maQR) : veRepository.findByMaQR(maQR);
    }

    private VeQuanLyDTO toQuanLyDTO(Ve ve) {
        VeQuanLyDTO dto = new VeQuanLyDTO();
        dto.setMaVe(ve.getMaVe());
        dto.setMaQR(ve.getMaQR());
        dto.setMaSK(ve.getMaSK());
        dto.setMaGhe(ve.getMaGhe());
        dto.setMaDonHang(ve.getMaDonHang());
        dto.setGiaVe(ve.getGiaVe());
        dto.setTrangThaiVe(ve.getTrangThaiVe());
        dto.setThoiGianPhat(ve.getThoiGianPhat());
        dto.setThoiGianSuDung(ve.getThoiGianSuDung());

        if (ve.getMaSK() != null) {
            suKienRepository.findById(ve.getMaSK()).map(SuKien::getTenSK).ifPresent(dto::setTenSuKien);
        }
        if (ve.getMaGhe() != null) {
            gheRepository.findById(ve.getMaGhe()).map(Ghe::getTenGhe).ifPresent(dto::setTenGhe);
        }
        if (ve.getMaDonHang() != null) {
            donHangRepository.findById(ve.getMaDonHang()).ifPresent(dh -> {
                dto.setSoDonHang(dh.getSoDonHang());
                dto.setMaKH(dh.getMaKH());
                if (dh.getMaKH() != null) {
                    khachHangRepository.findById(dh.getMaKH())
                            .map(KhachHang::getHoTen)
                            .ifPresent(dto::setTenKhachHang);
                }
            });
        }
        return dto;
    }

    private boolean matchesKeyword(VeQuanLyDTO dto, String keyword) {
        String needle = keyword.toLowerCase(Locale.ROOT);
        return contains(dto.getMaVe(), needle)
                || contains(dto.getMaQR(), needle)
                || contains(dto.getMaSK(), needle)
                || contains(dto.getTenSuKien(), needle)
                || contains(dto.getMaGhe(), needle)
                || contains(dto.getTenGhe(), needle)
                || contains(dto.getMaDonHang(), needle)
                || contains(dto.getSoDonHang(), needle)
                || contains(dto.getMaKH(), needle)
                || contains(dto.getTenKhachHang(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }

    private String buildQrCode(String maVe) {
        String base = "QR-" + maVe;
        String candidate = base;
        int suffix = 1;
        while (veRepository.existsByMaQR(candidate)) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        return candidate;
    }

    private Timestamp parseTimestamp(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        try {
            String clean = timeStr.trim()
                    .replace("T", " ")
                    .replace("Z", "");

            if (clean.contains(".")) {
                clean = clean.split("\\.")[0];
            }

            if (clean.length() == 16) {
                clean += ":00";
            }

            if (clean.length() > 19) {
                clean = clean.substring(0, 19);
            }

            return Timestamp.valueOf(clean);
        } catch (Exception e) {
            System.err.println("Không thể parse timestamp: " + timeStr + " - " + e.getMessage());
            return null;
        }
    }

    private Timestamp parseTimestampOrThrow(String fieldName, String timeStr) {
        Timestamp parsed = parseTimestamp(timeStr);
        if (timeStr != null && !timeStr.isBlank() && parsed == null) {
            throw new RuntimeException(fieldName + " không hợp lệ. Vui lòng nhập đúng định dạng ngày giờ.");
        }
        return parsed;
    }
}
