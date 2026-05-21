package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.SuKien;
import com.dede.ticketsystem.model.SuKienDTO;
import com.dede.ticketsystem.model.ThietLapSanKhauDTO;
import com.dede.ticketsystem.model.Ve;
import com.dede.ticketsystem.model.KhuVuc;
import com.dede.ticketsystem.model.Ghe;
import com.dede.ticketsystem.model.SeatAreaDTO;
import com.dede.ticketsystem.model.SeatCellDTO;
import com.dede.ticketsystem.model.SeatMapDTO;
import com.dede.ticketsystem.model.SeatRowDTO;
import com.dede.ticketsystem.repository.SuKienRepository;
import com.dede.ticketsystem.repository.VeRepository;
import com.dede.ticketsystem.repository.KhuVucRepository;
import com.dede.ticketsystem.repository.GheRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SuKienService {

    private static final Set<String> TRANG_THAI_HOP_LE = Set.of(
            "Chưa mở bán",
            "Đang mở bán",
            "Đã kết thúc",
            "Đã hủy",
            "Tạm ngưng"
    );

    private static final Set<String> TRANG_THAI_GHE_HOP_LE = Set.of(
            "Trống",
            "Đang chọn",
            "Đã bán",
            "Bảo trì"
    );
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final SuKienRepository suKienRepository;
    private final VeRepository veRepository;
    private final KhuVucRepository khuVucRepository;
    private final GheRepository gheRepository;
    private final IdGeneratorService idGeneratorService;
    private final Path eventUploadDir;

    public SuKienService(SuKienRepository suKienRepository,
                         VeRepository veRepository,
                         KhuVucRepository khuVucRepository,
                         GheRepository gheRepository,
                         IdGeneratorService idGeneratorService,
                         @Value("${app.upload.events-dir:uploads/events}") String eventUploadDir) {
        this.suKienRepository = suKienRepository;
        this.veRepository = veRepository;
        this.khuVucRepository = khuVucRepository;
        this.gheRepository = gheRepository;
        this.idGeneratorService = idGeneratorService;
        this.eventUploadDir = Paths.get(eventUploadDir).toAbsolutePath().normalize();
    }

    public List<SuKien> layTatCa() {
        return suKienRepository.findAll();
    }

    public List<SuKien> timKiem(String keyword, String trangThai) {
        return suKienRepository.search(keyword, trangThai);
    }

    public Optional<SuKien> timTheoMa(String maSK) {
        return suKienRepository.findById(maSK);
    }

    public SeatMapDTO getSeatMap(String maSK) {
        SuKien sk = suKienRepository.findById(maSK)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện: " + maSK));

        List<KhuVuc> khuVucs = new ArrayList<>(khuVucRepository.findByMaSK(maSK));
        khuVucs.sort(Comparator
                .comparing((KhuVuc kv) -> sortValue(kv.getMaKhuVuc()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(kv -> sortValue(kv.getTenKhuVuc()), String.CASE_INSENSITIVE_ORDER));

        Map<String, List<Ghe>> gheTheoKhuVuc = new LinkedHashMap<>();
        for (KhuVuc khuVuc : khuVucs) {
            gheTheoKhuVuc.put(khuVuc.getMaKhuVuc(), new ArrayList<>());
        }

        for (Ghe ghe : gheRepository.findByMaSK(maSK)) {
            List<Ghe> gheCuaKhu = gheTheoKhuVuc.get(ghe.getMaKhuVuc());
            if (gheCuaKhu != null) {
                gheCuaKhu.add(ghe);
            }
        }

        List<SeatAreaDTO> areas = new ArrayList<>();
        for (KhuVuc khuVuc : khuVucs) {
            Map<String, List<Ghe>> gheTheoHang = new LinkedHashMap<>();
            for (Ghe ghe : gheTheoKhuVuc.getOrDefault(khuVuc.getMaKhuVuc(), List.of())) {
                String hangGhe = normalizeNullable(ghe.getHangGhe());
                if (hangGhe == null) {
                    hangGhe = "";
                }
                gheTheoHang.computeIfAbsent(hangGhe, key -> new ArrayList<>()).add(ghe);
            }

            List<String> sortedHang = new ArrayList<>(gheTheoHang.keySet());
            sortedHang.sort(this::compareHangGhe);

            List<SeatRowDTO> rows = new ArrayList<>();
            for (String hangGhe : sortedHang) {
                List<Ghe> gheTrongHang = gheTheoHang.get(hangGhe);
                gheTrongHang.sort(Comparator
                        .comparing((Ghe ghe) -> ghe.getCotGhe() == null ? Integer.MAX_VALUE : ghe.getCotGhe())
                        .thenComparing(ghe -> sortValue(ghe.getTenGhe()), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ghe -> sortValue(ghe.getMaGhe()), String.CASE_INSENSITIVE_ORDER));

                List<SeatCellDTO> cells = new ArrayList<>();
                for (Ghe ghe : gheTrongHang) {
                    cells.add(new SeatCellDTO(
                            ghe.getMaGhe(),
                            ghe.getTenGhe(),
                            ghe.getHangGhe(),
                            ghe.getCotGhe(),
                            normalizeTrangThaiGhe(ghe.getTrangThaiGhe()),
                            ghe.getMaKhuVuc()
                    ));
                }
                rows.add(new SeatRowDTO(hangGhe, cells));
            }

            areas.add(new SeatAreaDTO(
                    khuVuc.getMaKhuVuc(),
                    khuVuc.getTenKhuVuc(),
                    khuVuc.getMauSacHienThi(),
                    khuVuc.getGiaVe(),
                    khuVuc.getSoVeToiDaPerKH(),
                    rows
            ));
        }

        return new SeatMapDTO(sk.getMaSK(), sk.getTenSK(), areas);
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

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }

    private String sortValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeTrangThaiGhe(String trangThaiGhe) {
        String clean = normalizeNullable(trangThaiGhe);
        if (clean != null && TRANG_THAI_GHE_HOP_LE.contains(clean)) {
            return clean;
        }
        return "Trống";
    }

    private int compareHangGhe(String left, String right) {
        int rankCompare = Integer.compare(rowRank(left), rowRank(right));
        if (rankCompare != 0) {
            return rankCompare;
        }
        return sortValue(left).compareToIgnoreCase(sortValue(right));
    }

    private int rowRank(String hangGhe) {
        String clean = sortValue(hangGhe).toUpperCase();
        if (clean.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int split = 0;
        while (split < clean.length() && clean.charAt(split) >= 'A' && clean.charAt(split) <= 'Z') {
            split++;
        }

        if (split == 0) {
            return Integer.MAX_VALUE - 1;
        }

        String letters = clean.substring(0, split);
        String suffix = clean.substring(split);
        int suffixNumber = 0;
        if (!suffix.isEmpty()) {
            try {
                suffixNumber = Integer.parseInt(suffix);
            } catch (NumberFormatException ignored) {
                suffixNumber = 0;
            }
        }

        if (letters.length() == 1) {
            return suffixNumber * 26 + (letters.charAt(0) - 'A');
        }

        int excelRank = 0;
        for (int i = 0; i < letters.length(); i++) {
            excelRank = excelRank * 26 + (letters.charAt(i) - 'A' + 1);
        }
        return suffixNumber * 1000 + excelRank - 1;
    }

    private void validateTrangThai(String trangThai) {
        if (trangThai == null || !TRANG_THAI_HOP_LE.contains(trangThai)) {
            throw new RuntimeException("Trạng thái sự kiện không hợp lệ.");
        }
    }

    private void validateThoiGian(Timestamp batDau, Timestamp ketThuc, Timestamp moBan, Timestamp dongBan) {
        if (batDau != null && ketThuc != null && !ketThuc.after(batDau)) {
            throw new RuntimeException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }

        if (moBan != null && dongBan != null && moBan.after(dongBan)) {
            throw new RuntimeException("Thời gian mở bán phải trước hoặc bằng thời gian đóng bán.");
        }

        if (dongBan != null && batDau != null && dongBan.after(batDau)) {
            throw new RuntimeException("Thời gian đóng bán phải trước hoặc bằng thời gian bắt đầu.");
        }
    }

    private boolean hasUpload(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String saveEventImage(String maSK, String filePrefix, MultipartFile file) {
        validateImageFile(file);

        try {
            Files.createDirectories(eventUploadDir);
            String extension = getImageExtension(file);
            String cleanMaSK = maSK.replaceAll("[^A-Za-z0-9_-]", "_");
            String fileName = filePrefix + "_" + cleanMaSK + "_" + System.currentTimeMillis() + "." + extension;
            Path targetPath = eventUploadDir.resolve(fileName).normalize();
            if (!targetPath.startsWith(eventUploadDir)) {
                throw new RuntimeException("Tên file upload không hợp lệ.");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/events/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file ảnh. Vui lòng thử lại.");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (!hasUpload(file)) {
            return;
        }

        String extension = getImageExtension(file);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Ảnh chỉ hỗ trợ định dạng jpg, jpeg, png, webp.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new RuntimeException("File upload phải là ảnh.");
        }
    }

    private String getImageExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < originalFilename.length() - 1) {
                extension = originalFilename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
            }
        }

        if (extension.isBlank()) {
            String contentType = file.getContentType();
            if ("image/jpeg".equalsIgnoreCase(contentType)) {
                extension = "jpg";
            } else if ("image/png".equalsIgnoreCase(contentType)) {
                extension = "png";
            } else if ("image/webp".equalsIgnoreCase(contentType)) {
                extension = "webp";
            }
        }

        if ("jpeg".equals(extension)) {
            return "jpg";
        }
        return extension;
    }

    public SuKien taoSuKien(SuKienDTO dto) {
        return taoSuKien(dto, null, null);
    }

    public SuKien taoSuKien(SuKienDTO dto, MultipartFile hinhAnhFile, MultipartFile hinhAnhThumbFile) {
        if (dto.getTenSK() == null || dto.getTenSK().trim().isEmpty()) {
            throw new RuntimeException("Tên sự kiện không được để trống.");
        }

        Timestamp batDau = parseTimestampOrThrow("Thời gian bắt đầu", dto.getThoiGianBatDau());
        Timestamp ketThuc = parseTimestampOrThrow("Thời gian kết thúc", dto.getThoiGianKetThuc());
        Timestamp moBan = parseTimestampOrThrow("Thời gian mở bán", dto.getThoiGianMoBan());
        Timestamp dongBan = parseTimestampOrThrow("Thời gian đóng bán", dto.getThoiGianDongBan());
        String trangThai = normalizeNullable(dto.getTrangThaiSK());
        if (trangThai == null) {
            trangThai = "Chưa mở bán";
        }

        validateTrangThai(trangThai);
        validateThoiGian(batDau, ketThuc, moBan, dongBan);
        String maLoaiSK = normalizeNullable(dto.getMaLoaiSK());
        if (maLoaiSK == null) {
            throw new RuntimeException("Loại sự kiện không được để trống.");
        }

        SuKien sk = new SuKien();

        String maSK = normalizeNullable(dto.getMaSK());
        if (maSK == null) {
            sk.setMaSK(idGeneratorService.nextSuKienId());
        } else {
            if (suKienRepository.existsById(maSK)) {
                throw new RuntimeException("Mã sự kiện đã tồn tại!");
            }
            sk.setMaSK(maSK);
        }

        sk.setTenSK(dto.getTenSK().trim());
        sk.setMoTa(dto.getMoTa());
        String hinhAnh = normalizeNullable(dto.getHinhAnh());
        String hinhAnhThumb = normalizeNullable(dto.getHinhAnhThumb());
        if (hasUpload(hinhAnhFile)) {
            hinhAnh = saveEventImage(sk.getMaSK(), "sk", hinhAnhFile);
        }
        if (hasUpload(hinhAnhThumbFile)) {
            hinhAnhThumb = saveEventImage(sk.getMaSK(), "sk_thumb", hinhAnhThumbFile);
        } else if (hinhAnhThumb == null && hinhAnh != null) {
            hinhAnhThumb = hinhAnh;
        }
        sk.setHinhAnh(hinhAnh);
        sk.setHinhAnhThumb(hinhAnhThumb);
        sk.setMoTaNgan(dto.getMoTaNgan());
        sk.setTags(dto.getTags());

        sk.setThoiGianBatDau(batDau);
        sk.setThoiGianKetThuc(ketThuc);
        sk.setThoiGianMoBan(moBan);
        sk.setThoiGianDongBan(dongBan);

        sk.setTongSoVe(dto.getTongSoVe() == null ? 0 : dto.getTongSoVe());
        sk.setSoVeDaBan(0);
        sk.setTrangThaiSK(trangThai);
        sk.setThoiGianTao(new Timestamp(System.currentTimeMillis()));

        sk.setMaLoaiSK(maLoaiSK);
        sk.setMaDiaDiem(normalizeNullable(dto.getMaDiaDiem()));
        sk.setMaNV(normalizeNullable(dto.getMaNV()));

        SuKien savedSk = suKienRepository.save(sk);

        // We no longer auto generate tickets here. It is moved to thietLapSanKhau.

        return savedSk;
    }

    public SuKien capNhatSuKien(String maSK, SuKienDTO dto) {
        return capNhatSuKien(maSK, dto, null, null);
    }

    public SuKien capNhatSuKien(String maSK, SuKienDTO dto, MultipartFile hinhAnhFile, MultipartFile hinhAnhThumbFile) {
        if (dto.getTenSK() == null || dto.getTenSK().trim().isEmpty()) {
            throw new RuntimeException("Tên sự kiện không được để trống.");
        }

        Timestamp batDau = parseTimestampOrThrow("Thời gian bắt đầu", dto.getThoiGianBatDau());
        Timestamp ketThuc = parseTimestampOrThrow("Thời gian kết thúc", dto.getThoiGianKetThuc());
        Timestamp moBan = parseTimestampOrThrow("Thời gian mở bán", dto.getThoiGianMoBan());
        Timestamp dongBan = parseTimestampOrThrow("Thời gian đóng bán", dto.getThoiGianDongBan());
        String trangThai = normalizeNullable(dto.getTrangThaiSK());

        validateTrangThai(trangThai);
        validateThoiGian(batDau, ketThuc, moBan, dongBan);

        SuKien sk = suKienRepository.findById(maSK)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện: " + maSK));

        String dtoMaSK = normalizeNullable(dto.getMaSK());
        if (dtoMaSK != null && !dtoMaSK.equals(maSK)) {
            throw new RuntimeException("Không được thay đổi mã sự kiện.");
        }

        sk.setTenSK(dto.getTenSK().trim());
        sk.setMoTa(dto.getMoTa());

        String hinhAnh = sk.getHinhAnh();
        String dtoHinhAnh = normalizeNullable(dto.getHinhAnh());
        if (dtoHinhAnh != null) {
            hinhAnh = dtoHinhAnh;
        }
        if (hasUpload(hinhAnhFile)) {
            hinhAnh = saveEventImage(maSK, "sk", hinhAnhFile);
        }

        String hinhAnhThumb = sk.getHinhAnhThumb();
        String dtoHinhAnhThumb = normalizeNullable(dto.getHinhAnhThumb());
        if (dtoHinhAnhThumb != null) {
            hinhAnhThumb = dtoHinhAnhThumb;
        }
        if (hasUpload(hinhAnhThumbFile)) {
            hinhAnhThumb = saveEventImage(maSK, "sk_thumb", hinhAnhThumbFile);
        }

        sk.setHinhAnh(hinhAnh);
        sk.setHinhAnhThumb(hinhAnhThumb);
        sk.setMoTaNgan(dto.getMoTaNgan());
        sk.setTags(dto.getTags());

        sk.setThoiGianBatDau(batDau);
        sk.setThoiGianKetThuc(ketThuc);
        sk.setThoiGianMoBan(moBan);
        sk.setThoiGianDongBan(dongBan);

        // Không cập nhật TongSoVe/SoVeDaBan ở route sửa thông tin sự kiện.
        // Hai số này phải đến từ sơ đồ ghế/vé hoặc nghiệp vụ bán vé.
        sk.setTrangThaiSK(trangThai);
        
        sk.setCapNhatLanCuoi(new Timestamp(System.currentTimeMillis()));

        String maLoaiSK = normalizeNullable(dto.getMaLoaiSK());
        if (maLoaiSK == null) {
            maLoaiSK = normalizeNullable(sk.getMaLoaiSK());
        }
        if (maLoaiSK == null) {
            throw new RuntimeException("Loại sự kiện không được để trống.");
        }
        sk.setMaLoaiSK(maLoaiSK);
        sk.setMaDiaDiem(normalizeNullable(dto.getMaDiaDiem()));
        sk.setMaNV(normalizeNullable(dto.getMaNV()));

        return suKienRepository.save(sk);
    }

    public void huySuKien(String maSK) {
        SuKien sk = suKienRepository.findById(maSK)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện: " + maSK));
        
        sk.setTrangThaiSK("Đã hủy");
        sk.setCapNhatLanCuoi(new Timestamp(System.currentTimeMillis()));
        suKienRepository.save(sk);
    }

    @Transactional
    public void thietLapSanKhau(String maSK, ThietLapSanKhauDTO dto) {
        System.out.println("DEBUG: Starting thietLapSanKhau for " + maSK);
        SuKien sk = suKienRepository.findById(maSK)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện: " + maSK));

        List<Ve> oldVe = veRepository.findByMaSK(maSK);
        boolean hasSoldTickets = oldVe.stream().anyMatch(v -> v.getMaDonHang() != null && !v.getMaDonHang().trim().isEmpty());
        if (hasSoldTickets) {
            throw new RuntimeException("Không thể thiết lập lại sơ đồ ghế vì sự kiện đã phát sinh vé bán.");
        }

        try {
            System.out.println("DEBUG: Found " + oldVe.size() + " old tickets (none sold)");
            List<Ghe> oldGhe = gheRepository.findByMaSK(maSK);
            System.out.println("DEBUG: Found " + oldGhe.size() + " old seats");
            List<KhuVuc> oldKhuVuc = khuVucRepository.findByMaSK(maSK);
            System.out.println("DEBUG: Found " + oldKhuVuc.size() + " old zones");

            // Delete in correct order to avoid FK constraint violations: Ve -> Ghe -> KhuVuc
            veRepository.deleteAll(oldVe);
            gheRepository.deleteAll(oldGhe);
            khuVucRepository.deleteAll(oldKhuVuc);
            System.out.println("DEBUG: Deleted old data");
        } catch (Exception e) {
            System.out.println("DEBUG: Error during deletion: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        int totalTickets = 0;

        List<KhuVuc> dsKhuVuc = new ArrayList<>();
        List<Ghe> dsGhe = new ArrayList<>();

        char[] rowLabels = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        if (dto.getDanhSachKhuVuc() != null) {
            for (int k = 0; k < dto.getDanhSachKhuVuc().size(); k++) {
                ThietLapSanKhauDTO.KhuVucDTO kvDto = dto.getDanhSachKhuVuc().get(k);

                String maKhuVuc = idGeneratorService.nextKhuVucId();
                int soGheToiDa = kvDto.getSoHang() * kvDto.getSoGheMoiHang();
                int soVeToiDaPerKH = kvDto.getSoVeToiDaPerKH() != null ? kvDto.getSoVeToiDaPerKH() : 4;
                
                KhuVuc kv = new KhuVuc(maKhuVuc, kvDto.getTenKhuVuc(), kvDto.getMauSacHienThi(), soGheToiDa, 0, soVeToiDaPerKH, kvDto.getGiaVe(), "Đang bán", maSK);
                kv.setSoHang(kvDto.getSoHang());
                kv.setSoGheMoiHang(kvDto.getSoGheMoiHang());
                dsKhuVuc.add(kv);

                for (int i = 0; i < kvDto.getSoHang(); i++) {
                    String rowName = String.valueOf(rowLabels[i % rowLabels.length]);
                    if (i >= rowLabels.length) rowName = rowName + (i / rowLabels.length);

                    for (int j = 1; j <= kvDto.getSoGheMoiHang(); j++) {
                        String tenGhe = rowName + String.format("%02d", j);
                        String maGhe = idGeneratorService.nextGheId();

                        Ghe ghe = new Ghe(maGhe, tenGhe, rowName, j, "Trống", maKhuVuc, maSK);
                        dsGhe.add(ghe);

                        totalTickets++;
                    }
                }
            }
        }

        khuVucRepository.saveAll(dsKhuVuc);
        gheRepository.saveAll(dsGhe);

        sk.setTongSoVe(totalTickets);
        sk.setCapNhatLanCuoi(new Timestamp(System.currentTimeMillis()));
        suKienRepository.save(sk);
        System.out.println("DEBUG: Finished thietLapSanKhau for " + maSK + ". Total tickets: " + totalTickets);
    }
}
