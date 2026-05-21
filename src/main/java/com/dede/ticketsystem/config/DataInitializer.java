package com.dede.ticketsystem.config;

import com.dede.ticketsystem.model.ChiTietVaiTro;
import com.dede.ticketsystem.model.NguoiDung;
import com.dede.ticketsystem.model.VaiTro;
import com.dede.ticketsystem.model.KhachHang;
import com.dede.ticketsystem.repository.NguoiDungRepository;
import com.dede.ticketsystem.repository.VaiTroRepository;
import com.dede.ticketsystem.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private Environment environment;

    @Autowired
    private com.dede.ticketsystem.service.IdGeneratorService idGeneratorService;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.dev.reset-data-on-start:false}")
    private boolean resetDataOnStart;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (resetDataOnStart) {
            resetRuntimeDataOnStartIfAllowed();
        }

        if (!seedEnabled) {
            System.out.println("DataInitializer: app.seed.enabled=false, bỏ qua seed data.");
            return;
        }

        // 1. Tạo các vai trò mặc định
        List<VaiTro> defaultRoles = Arrays.asList(
            VaiTro.builder().maVaiTro("ADMIN").tenVaiTro("Quản trị viên").moTa("Quản trị viên hệ thống").build(),
            VaiTro.builder().maVaiTro("CUSTOMER").tenVaiTro("Khách hàng").moTa("Khách hàng").build(),
            VaiTro.builder().maVaiTro("STAFF").tenVaiTro("Nhân viên soát vé").moTa("Nhân viên soát vé").build(),
            VaiTro.builder().maVaiTro("ORGANIZER").tenVaiTro("Ban tổ chức").moTa("Ban tổ chức").build()
        );

        for (VaiTro r : defaultRoles) {
            VaiTro role = vaiTroRepository.findById(r.getMaVaiTro()).orElse(r);
            role.setTenVaiTro(r.getTenVaiTro());
            role.setMoTa(r.getMoTa());
            vaiTroRepository.save(role);
        }

        // 2. Khởi tạo các user demo
        initUser("admin", "123456", "ADMIN", "ND0001", "NV0001");
        initUser("customer", "123456", "CUSTOMER", "ND0002", "KH0001");
        initUser("staff", "123456", "STAFF", "ND0003", "NV0002");
        initUser("organizer", "123456", "ORGANIZER", "ND0004", "NV0003");

        initTestData();

        System.out.println("KHỞI TẠO DỮ LIỆU BAN ĐẦU HOÀN TẤT VÀ IDEMPOTENT.");
    }

    private void resetRuntimeDataOnStartIfAllowed() {
        if (!isDevOrLocalProfile()) {
            System.err.println("============================================================");
            System.err.println("CẢNH BÁO: app.dev.reset-data-on-start=true nhưng profile hiện tại không phải dev/local.");
            System.err.println("DataInitializer bỏ qua reset tự động để tránh mất dữ liệu ngoài ý muốn.");
            System.err.println("============================================================");
            return;
        }

        System.err.println("============================================================");
        System.err.println("CẢNH BÁO LỚN: ĐANG RESET DỮ LIỆU RUNTIME DEMO KHI START APP.");
        System.err.println("Chỉ chạy vì app.dev.reset-data-on-start=true và profile là dev/local.");
        System.err.println("============================================================");

        deleteIfTableExists("LICHSUSOATVE");
        deleteIfTableExists("LICHSUGUI_EMAIL");
        deleteIfTableExists("GIAODICHTHANHTOAN");
        deleteIfTableExists("VE");
        deleteIfTableExists("DONHANG");
        deleteIfTableExists("HANGDOIAO");
        deleteIfTableExists("LOG_HANH_VI");

        updateIfTableExists("GHENGOI", "UPDATE GHENGOI SET TrangThaiGhe = 'Trống', ThoiGianKhoaTam = NULL, MaPhienKhoa = NULL");
        updateIfTableExists("KHUVUC", "UPDATE KHUVUC SET SoGheDaBan = 0");
        updateIfTableExists("SUKIEN", "UPDATE SUKIEN SET SoVeDaBan = 0");
    }

    private boolean isDevOrLocalProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles == null || activeProfiles.length == 0) {
            return false;
        }
        for (String profile : activeProfiles) {
            if ("dev".equalsIgnoreCase(profile) || "local".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private void deleteIfTableExists(String tableName) {
        if (!tableExists(tableName)) {
            System.out.println("Reset runtime: bỏ qua bảng không tồn tại " + tableName);
            return;
        }
        jdbcTemplate.update("DELETE FROM " + tableName);
        System.out.println("Reset runtime: đã xóa dữ liệu " + tableName);
    }

    private void updateIfTableExists(String tableName, String sql) {
        if (!tableExists(tableName)) {
            System.out.println("Reset runtime: bỏ qua bảng không tồn tại " + tableName);
            return;
        }
        jdbcTemplate.update(sql);
        System.out.println("Reset runtime: đã reset " + tableName);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER(?)",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private void initTestData() {
        System.out.println("Bắt đầu khởi tạo dữ liệu mẫu cho Vé và Sự kiện...");
        try {
            final String sampleMaSK = "SK0001";
            final String vipMaKhuVuc = "KV0001";
            final String standardMaKhuVuc = "KV0002";

            // 1. DIADIEM
            Integer countDiaDiem = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM DIADIEM WHERE MaDiaDiem = 'DD001'", Integer.class);
            if (countDiaDiem == null || countDiaDiem == 0) {
                jdbcTemplate.update("INSERT INTO DIADIEM (MaDiaDiem, TenDiaDiem, DiaChi, ThanhPho, SucChuaToiDa, MoTa, TrangThai) VALUES ('DD001', 'Nhà thi đấu Phú Thọ', '01 Lữ Gia, Phường 15, Quận 11', 'TP.HCM', 8000, 'Nhà thi đấu đa năng', 'Đang hoạt động')");
                System.out.println("- Đã khởi tạo DIADIEM: DD001");
            }

            // 2. LOAISUKIEN
            String concertLoaiSK = ensureLoaiSuKien("LSK001", "Concert", "Sự kiện âm nhạc trực tiếp");
            ensureLoaiSuKien("LSK002", "Workshop", "Buổi thực hành chia sẻ kỹ năng");
            ensureLoaiSuKien("LSK003", "Hội thảo", "Buổi thảo luận học thuật chuyên đề");
            ensureLoaiSuKien(null, "Talkshow", "Chương trình trò chuyện giao lưu");
            ensureLoaiSuKien(null, "Triển lãm", "Sự kiện trưng bày, giới thiệu sản phẩm hoặc nghệ thuật");
            ensureLoaiSuKien(null, "Seminar", "Buổi chia sẻ chuyên đề quy mô vừa và nhỏ");
            ensureLoaiSuKien(null, "Hội nghị", "Sự kiện hội nghị, gặp mặt chuyên môn");
            ensureLoaiSuKien(null, "Fan Meeting", "Sự kiện gặp gỡ người hâm mộ");
            ensureLoaiSuKien(null, "Liveshow", "Chương trình biểu diễn trực tiếp");
            ensureLoaiSuKien(null, "Sự kiện thể thao", "Sự kiện thi đấu hoặc giao lưu thể thao");
            ensureLoaiSuKien(null, "Sự kiện doanh nghiệp", "Sự kiện nội bộ hoặc đối ngoại của doanh nghiệp");
            ensureLoaiSuKien(null, "Networking", "Sự kiện kết nối cộng đồng hoặc chuyên môn");
            ensureLoaiSuKien(null, "Đào tạo", "Sự kiện đào tạo, training, nâng cao kỹ năng");
            ensureLoaiSuKien(null, "Lễ hội", "Sự kiện lễ hội, văn hóa, giải trí cộng đồng");
            ensureLoaiSuKien(null, "Biểu diễn nghệ thuật", "Sự kiện biểu diễn nghệ thuật");

            // 3. SUKIEN
            Timestamp eventStart = Timestamp.valueOf("2026-07-15 19:00:00");
            Timestamp eventEnd = Timestamp.valueOf("2026-07-15 23:00:00");
            Timestamp saleStart = Timestamp.valueOf("2026-05-01 10:00:00");
            Timestamp saleEnd = Timestamp.valueOf("2026-07-14 23:59:59");
            Timestamp now = new Timestamp(System.currentTimeMillis());

            Integer countSK = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM SUKIEN WHERE MaSK = ?", Integer.class, sampleMaSK);
            if (countSK == null || countSK == 0) {
                jdbcTemplate.update("INSERT INTO SUKIEN (MaSK, TenSK, MoTa, ThoiGianBatDau, ThoiGianKetThuc, ThoiGianMoBan, ThoiGianDongBan, TrangThaiSK, MaLoaiSK, MaDiaDiem, TongSoVe, SoVeDaBan, ThoiGianTao, CapNhatLanCuoi) " +
                        "VALUES (?, 'Dề Dê Summer Concert 2026', 'Đêm nhạc hoành tráng mùa hè 2026', ?, ?, ?, ?, 'Đang mở bán', ?, 'DD001', 65, 0, ?, ?)",
                        sampleMaSK, eventStart, eventEnd, saleStart, saleEnd, concertLoaiSK, now, now);
                System.out.println("- Đã khởi tạo SUKIEN: " + sampleMaSK);
            } else {
                jdbcTemplate.update("UPDATE SUKIEN SET TrangThaiSK = 'Đang mở bán', TongSoVe = 65, " +
                        "ThoiGianBatDau = ?, " +
                        "ThoiGianKetThuc = ?, " +
                        "ThoiGianMoBan = ?, " +
                        "ThoiGianDongBan = ?, " +
                        "MaLoaiSK = ?, CapNhatLanCuoi = ? WHERE MaSK = ?",
                        eventStart, eventEnd, saleStart, saleEnd, concertLoaiSK, now, sampleMaSK);
            }

            // 4. KHUVUC
            Integer countKVVIP = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM KHUVUC WHERE MaKhuVuc = ?", Integer.class, vipMaKhuVuc);
            if (countKVVIP == null || countKVVIP == 0) {
                jdbcTemplate.update("INSERT INTO KHUVUC (MaKhuVuc, TenKhuVuc, MauSacHienThi, SoGheToiDa, SoGheDaBan, SoVeToiDaPerKH, GiaVe, TrangThai, MaSK) VALUES (?, 'Khu VIP', '#FFD700', 15, 0, 2, 1000000, 'Đang bán', ?)",
                        vipMaKhuVuc, sampleMaSK);
            } else {
                jdbcTemplate.update("UPDATE KHUVUC SET TenKhuVuc = 'Khu VIP', GiaVe = 1000000, SoVeToiDaPerKH = 2, MaSK = ? WHERE MaKhuVuc = ?",
                        sampleMaSK, vipMaKhuVuc);
            }

            Integer countKVStandard = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM KHUVUC WHERE MaKhuVuc = ?", Integer.class, standardMaKhuVuc);
            if (countKVStandard == null || countKVStandard == 0) {
                jdbcTemplate.update("INSERT INTO KHUVUC (MaKhuVuc, TenKhuVuc, MauSacHienThi, SoGheToiDa, SoGheDaBan, SoVeToiDaPerKH, GiaVe, TrangThai, MaSK) VALUES (?, 'Khu Standard', '#FF6B6B', 50, 0, 4, 300000, 'Đang bán', ?)",
                        standardMaKhuVuc, sampleMaSK);
            } else {
                jdbcTemplate.update("UPDATE KHUVUC SET TenKhuVuc = 'Khu Standard', GiaVe = 300000, SoVeToiDaPerKH = 4, MaSK = ? WHERE MaKhuVuc = ?",
                        sampleMaSK, standardMaKhuVuc);
            }
            System.out.println("- Đã khởi tạo KHUVUC cho " + sampleMaSK);

            // 5. GHENGOI
            // VIP: 3 rows x 5 columns = 15 seats (HangGhe: V, W, X; CotGhe: 1..5)
            String[] vipRows = {"V", "W", "X"};
            int seedSeatNumber = 1;
            for (String r : vipRows) {
                for (int c = 1; c <= 5; c++) {
                    String maGhe = String.format("GHE%04d", seedSeatNumber++);
                    String tenGhe = r + String.format("%02d", c);
                    
                    Integer countGhe = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM GHENGOI WHERE MaGhe = ?", Integer.class, maGhe);
                    if (countGhe == null || countGhe == 0) {
                        jdbcTemplate.update("INSERT INTO GHENGOI (MaGhe, TenGhe, HangGhe, CotGhe, TrangThaiGhe, MaKhuVuc, MaSK) VALUES (?, ?, ?, ?, 'Trống', ?, ?)",
                                maGhe, tenGhe, r, c, vipMaKhuVuc, sampleMaSK);
                    }
                }
            }

            // Standard: 5 rows x 10 columns = 50 seats (HangGhe: A, B, C, D, E; CotGhe: 1..10)
            String[] stdRows = {"A", "B", "C", "D", "E"};
            for (String r : stdRows) {
                for (int c = 1; c <= 10; c++) {
                    String maGhe = String.format("GHE%04d", seedSeatNumber++);
                    String tenGhe = r + String.format("%02d", c);
                    
                    Integer countGhe = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM GHENGOI WHERE MaGhe = ?", Integer.class, maGhe);
                    if (countGhe == null || countGhe == 0) {
                        jdbcTemplate.update("INSERT INTO GHENGOI (MaGhe, TenGhe, HangGhe, CotGhe, TrangThaiGhe, MaKhuVuc, MaSK) VALUES (?, ?, ?, ?, 'Trống', ?, ?)",
                                maGhe, tenGhe, r, c, standardMaKhuVuc, sampleMaSK);
                    }
                }
            }
            System.out.println("- Đã khởi tạo GHENGOI");
            System.out.println("- Seed master data sạch: không tạo DONHANG/VE runtime; vé sẽ phát sinh qua flow mua vé.");
        } catch (Exception e) {
            System.err.println("Lỗi khi khởi tạo dữ liệu mẫu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initUser(String username, String rawPassword, String roleCode, String preferredMaND, String preferredProfileId) {
        Optional<NguoiDung> existing = nguoiDungRepository.findByTenTaiKhoan(username);
        NguoiDung user;
        
        if (existing.isEmpty()) {
            VaiTro role = vaiTroRepository.findById(roleCode).orElse(null);
            if (role == null) return;

            String maND = isIdAvailable("NGUOIDUNG", "MaND", preferredMaND)
                    ? preferredMaND
                    : idGeneratorService.nextNguoiDungId();

            user = NguoiDung.builder()
                    .maND(maND)
                    .tenTaiKhoan(username)
                    .matKhauMaHoa(passwordEncoder.encode(rawPassword))
                    .trangThaiND("Đang hoạt động")
                    .thoiGianTao(new Timestamp(System.currentTimeMillis()))
                    .chiTietVaiTros(new ArrayList<>())
                    .email(username + "@ticketsystem.com")
                    .sdt("090" + String.format("%07d", (int)(Math.random() * 10000000)))
                    .build();

            ChiTietVaiTro chiTiet = new ChiTietVaiTro();
            chiTiet.setNguoiDung(user);
            chiTiet.setVaiTro(role);
            chiTiet.setMaND(user.getMaND());
            chiTiet.setMaVaiTro(roleCode);

            user.getChiTietVaiTros().add(chiTiet);
            user = nguoiDungRepository.saveAndFlush(user);
            System.out.println("Tạo thành công user demo: " + username);
        } else {
            user = existing.get();
            boolean changed = false;

            if (user.getChiTietVaiTros() == null) {
                user.setChiTietVaiTros(new ArrayList<>());
                changed = true;
            }

            if (!"Đang hoạt động".equals(user.getTrangThaiND())) {
                user.setTrangThaiND("Đang hoạt động");
                changed = true;
            }

            if (user.getMatKhauMaHoa() == null || !passwordEncoder.matches(rawPassword, user.getMatKhauMaHoa())) {
                user.setMatKhauMaHoa(passwordEncoder.encode(rawPassword));
                changed = true;
            }

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                user.setEmail(username + "@ticketsystem.com");
                changed = true;
            }

            boolean hasRole = user.getChiTietVaiTros().stream()
                    .anyMatch(ct -> roleCode.equals(ct.getMaVaiTro()));
            if (!hasRole) {
                VaiTro role = vaiTroRepository.findById(roleCode).orElse(null);
                if (role != null) {
                    ChiTietVaiTro chiTiet = new ChiTietVaiTro();
                    chiTiet.setNguoiDung(user);
                    chiTiet.setVaiTro(role);
                    chiTiet.setMaND(user.getMaND());
                    chiTiet.setMaVaiTro(roleCode);
                    user.getChiTietVaiTros().add(chiTiet);
                    changed = true;
                }
            }

            if (changed) {
                user.setCapNhatLanCuoi(new Timestamp(System.currentTimeMillis()));
                user = nguoiDungRepository.saveAndFlush(user);
                System.out.println("Đã chuẩn hóa user demo: " + username);
            }
        }

        // 3. Khởi tạo KHACHHANG hoặc NHANVIEN tương ứng
        if ("CUSTOMER".equals(roleCode)) {
            // Đảm bảo có bản ghi KHACHHANG
            Optional<KhachHang> khOpt = khachHangRepository.findByMaND(user.getMaND());
            if (khOpt.isEmpty()) {
                String maKH = isIdAvailable("KHACHHANG", "MaKH", preferredProfileId)
                        ? preferredProfileId
                        : idGeneratorService.nextKhachHangId();
                
                KhachHang kh = new KhachHang();
                kh.setMaKH(maKH);
                kh.setHoTenKH("Khách hàng " + user.getTenTaiKhoan());
                kh.setTongChiTieu(BigDecimal.ZERO);
                kh.setCapNhatLanCuoi(new Timestamp(System.currentTimeMillis()));
                kh.setNguoiDung(user);
                kh.setMaHangThanhVien(null); // để null để tránh foreign key constraint nếu chưa có dữ liệu HANGTHANHVIEN

                khachHangRepository.save(kh);
                System.out.println("Khởi tạo KHACHHANG cho user customer thành công: " + maKH);
            }
        } else {
            // Đảm bảo có bản ghi NHANVIEN
            String checkSql = "SELECT COUNT(*) FROM NHANVIEN WHERE MaND = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user.getMaND());
            if (count == null || count == 0) {
                String maNV = isIdAvailable("NHANVIEN", "MaNV", preferredProfileId)
                        ? preferredProfileId
                        : idGeneratorService.nextNhanVienId();

                String loaiNV = "Quản lý";
                double luongCB = 15000000;
                double phuCap = 2000000;

                if ("STAFF".equals(roleCode)) {
                    loaiNV = "Nhân viên soát vé";
                    luongCB = 8000000;
                    phuCap = 500000;
                } else if ("ORGANIZER".equals(roleCode)) {
                    loaiNV = "Ban tổ chức";
                    luongCB = 12000000;
                    phuCap = 1000000;
                }

                String insertSql = "INSERT INTO NHANVIEN (MaNV, LoaiNV, NgayVaoLam, TrangThaiLamViec, LuongCoBan, PhuCap, MaND) " +
                                   "VALUES (?, ?, ?, 'Đang làm việc', ?, ?, ?)";
                jdbcTemplate.update(insertSql, maNV, loaiNV, new Timestamp(System.currentTimeMillis()), luongCB, phuCap, user.getMaND());
                System.out.println("Khởi tạo NHANVIEN cho user " + username + " thành công: " + maNV);
            }
        }
    }

    private String ensureLoaiSuKien(String preferredMaLoaiSK, String tenLoaiSK, String moTa) {
        List<String> existingByName = jdbcTemplate.queryForList(
                "SELECT MaLoaiSK FROM LOAISUKIEN WHERE LOWER(TRIM(TenLoaiSK)) = LOWER(TRIM(?))",
                String.class,
                tenLoaiSK
        );
        if (!existingByName.isEmpty()) {
            return existingByName.get(0);
        }

        String maLoaiSK = preferredMaLoaiSK != null && isIdAvailable("LOAISUKIEN", "MaLoaiSK", preferredMaLoaiSK)
                ? preferredMaLoaiSK
                : idGeneratorService.nextLoaiSuKienId();

        jdbcTemplate.update(
                "INSERT INTO LOAISUKIEN (MaLoaiSK, TenLoaiSK, MoTa) VALUES (?, ?, ?)",
                maLoaiSK,
                tenLoaiSK,
                moTa
        );
        System.out.println("- Đã khởi tạo LOAISUKIEN: " + tenLoaiSK);
        return maLoaiSK;
    }

    private boolean isIdAvailable(String tableName, String columnName, String id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?",
                Integer.class,
                id
        );
        return count == null || count == 0;
    }
}
