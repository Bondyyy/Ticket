package com.dede.ticketsystem.config;

import com.dede.ticketsystem.model.VaiTro;
import com.dede.ticketsystem.repository.VaiTroRepository;
import com.dede.ticketsystem.service.IdGeneratorService;
import com.dede.ticketsystem.util.DateTimeUtils;
import com.dede.ticketsystem.util.ImageUrlUtil;
import com.dede.ticketsystem.util.OrderStatus;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private Environment environment;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.dev.reset-data-on-start:false}")
    private boolean resetDataOnStart;

    @Value("${app.demo.full-reseed-on-start:false}")
    private boolean fullReseedOnStart;

    @Value("${app.demo.seed-purchase-history-on-start:false}")
    private boolean seedPurchaseHistoryOnStart;

    private String demoPasswordHash;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            System.out.println("DataInitializer: app.seed.enabled=false, bỏ qua seed data.");
            return;
        }

        ensureDefaultRoles();
        ensureAdminUser();

        boolean didFullReseed = false;
        if (fullReseedOnStart) {
            didFullReseed = fullReseedDemoDataIfAllowed();
            ensureDefaultRoles();
            ensureAdminUser();
        } else if (resetDataOnStart) {
            resetRuntimeDataOnStartIfAllowed();
        }

        seedDemoUsers();
        seedDemoCatalogAndEvents(didFullReseed);
        seedDemoPurchaseHistory();

        System.out.println("KHỞI TẠO DỮ LIỆU DEMO HOÀN TẤT. Full reseed: " + didFullReseed);
    }

    private void ensureDefaultRoles() {
        List<VaiTro> defaultRoles = Arrays.asList(
                VaiTro.builder().maVaiTro("ADMIN").tenVaiTro("Quản trị viên").moTa("Quản trị viên hệ thống").build(),
                VaiTro.builder().maVaiTro("CUSTOMER").tenVaiTro("Khách hàng").moTa("Khách hàng").build(),
                VaiTro.builder().maVaiTro("STAFF").tenVaiTro("Nhân viên soát vé").moTa("Nhân viên soát vé").build(),
                VaiTro.builder().maVaiTro("ORGANIZER").tenVaiTro("Ban tổ chức").moTa("Ban tổ chức").build()
        );

        for (VaiTro seedRole : defaultRoles) {
            VaiTro role = vaiTroRepository.findById(seedRole.getMaVaiTro()).orElse(seedRole);
            role.setTenVaiTro(seedRole.getTenVaiTro());
            role.setMoTa(seedRole.getMoTa());
            vaiTroRepository.save(role);
        }
    }

    private void ensureAdminUser() {
        upsertDemoUser(
                "admin",
                "ADMIN",
                "ND0001",
                "NV0001",
                "Quản trị viên Dề Dê",
                "admin@dede.test",
                "0900000000"
        );
    }

    private void seedDemoUsers() {
        for (int i = 1; i <= 170; i++) {
            String username = String.format("customer%03d", i);
            upsertDemoUser(
                    username,
                    "CUSTOMER",
                    String.format("ND%04d", i + 1),
                    String.format("KH%04d", i),
                    "Khách hàng demo " + String.format("%03d", i),
                    username + "@dede.test",
                    String.format("09%08d", i)
            );
        }

        for (int i = 1; i <= 15; i++) {
            String username = String.format("staff%03d", i);
            upsertDemoUser(
                    username,
                    "STAFF",
                    String.format("ND%04d", i + 171),
                    String.format("NV%04d", i + 1),
                    "Nhân viên soát vé " + String.format("%03d", i),
                    username + "@dede.test",
                    String.format("08%08d", i)
            );
        }

        for (int i = 1; i <= 15; i++) {
            String username = String.format("organizer%03d", i);
            upsertDemoUser(
                    username,
                    "ORGANIZER",
                    String.format("ND%04d", i + 186),
                    String.format("NV%04d", i + 16),
                    "Ban tổ chức " + String.format("%03d", i),
                    username + "@dede.test",
                    String.format("07%08d", i)
            );
        }
    }

    private void upsertDemoUser(String username, String roleCode, String preferredMaND, String preferredProfileId,
                                String displayName, String email, String sdt) {
        String maND = findMaNDByUsername(username);
        Timestamp now = now();
        String safeEmail = emailAvailableForUser(email, maND) ? email : username + "." + preferredMaND.toLowerCase() + "@dede.test";

        if (maND == null) {
            maND = isIdAvailable("NGUOIDUNG", "MaND", preferredMaND)
                    ? preferredMaND
                    : idGeneratorService.nextNguoiDungId();

            jdbcTemplate.update(
                    "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, Email, SDT, ThoiGianTao, CapNhatLanCuoi, TrangThaiND) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 'Đang hoạt động')",
                    maND,
                    username,
                    demoPasswordHash(),
                    safeEmail,
                    sdt,
                    now,
                    now
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE NGUOIDUNG SET MatKhauMaHoa = ?, Email = ?, SDT = ?, TrangThaiND = 'Đang hoạt động', CapNhatLanCuoi = ? WHERE MaND = ?",
                    demoPasswordHash(),
                    safeEmail,
                    sdt,
                    now,
                    maND
            );
        }

        syncSingleRole(maND, roleCode);
        if ("CUSTOMER".equals(roleCode)) {
            ensureCustomerProfile(maND, preferredProfileId, displayName, now);
        } else {
            ensureEmployeeProfile(maND, preferredProfileId, roleCode, now);
        }
    }

    private void syncSingleRole(String maND, String roleCode) {
        if (!tableExists("CHITIETVAITRO")) {
            return;
        }
        jdbcTemplate.update("DELETE FROM CHITIETVAITRO WHERE MaND = ?", maND);
        jdbcTemplate.update("INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, ?)", maND, roleCode);
    }

    private void ensureCustomerProfile(String maND, String preferredMaKH, String displayName, Timestamp now) {
        if (!tableExists("KHACHHANG")) {
            return;
        }

        List<String> existing = jdbcTemplate.queryForList("SELECT MAKH FROM KHACHHANG WHERE MAND = ?", String.class, maND);
        if (existing.isEmpty()) {
            String maKH = isIdAvailable("KHACHHANG", "MAKH", preferredMaKH)
                    ? preferredMaKH
                    : idGeneratorService.nextKhachHangId();
            jdbcTemplate.update(
                    "INSERT INTO KHACHHANG (MAKH, HOTENKH, TONGCHITIEU, CAPNHATLANCUOI, MAND) VALUES (?, ?, ?, ?, ?)",
                    maKH,
                    displayName,
                    BigDecimal.ZERO,
                    now,
                    maND
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE KHACHHANG SET HOTENKH = ?, CAPNHATLANCUOI = ? WHERE MAND = ?",
                    displayName,
                    now,
                    maND
            );
        }
    }

    private void ensureEmployeeProfile(String maND, String preferredMaNV, String roleCode, Timestamp now) {
        if (!tableExists("NHANVIEN")) {
            return;
        }

        String loaiNV = "Quản lý";
        BigDecimal luongCoBan = BigDecimal.valueOf(15_000_000);
        BigDecimal phuCap = BigDecimal.valueOf(2_000_000);
        if ("STAFF".equals(roleCode)) {
            loaiNV = "Nhân viên soát vé";
            luongCoBan = BigDecimal.valueOf(8_000_000);
            phuCap = BigDecimal.valueOf(500_000);
        } else if ("ORGANIZER".equals(roleCode)) {
            loaiNV = "Ban tổ chức";
            luongCoBan = BigDecimal.valueOf(12_000_000);
            phuCap = BigDecimal.valueOf(1_000_000);
        }

        List<String> existing = jdbcTemplate.queryForList("SELECT MaNV FROM NHANVIEN WHERE MaND = ?", String.class, maND);
        if (existing.isEmpty()) {
            String maNV = isIdAvailable("NHANVIEN", "MaNV", preferredMaNV)
                    ? preferredMaNV
                    : idGeneratorService.nextNhanVienId();
            jdbcTemplate.update(
                    "INSERT INTO NHANVIEN (MaNV, LoaiNV, NgayVaoLam, TrangThaiLamViec, LuongCoBan, PhuCap, MaND) " +
                            "VALUES (?, ?, ?, 'Đang làm việc', ?, ?, ?)",
                    maNV,
                    loaiNV,
                    now,
                    luongCoBan,
                    phuCap,
                    maND
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE NHANVIEN SET LoaiNV = ?, TrangThaiLamViec = 'Đang làm việc', LuongCoBan = ?, PhuCap = ? WHERE MaND = ?",
                    loaiNV,
                    luongCoBan,
                    phuCap,
                    maND
            );
        }
    }

    private void seedDemoCatalogAndEvents(boolean fullReseed) {
        Map<String, String> loaiSuKienIds = seedLoaiSuKien();
        List<String> organizerMaNVs = findEmployeeIdsByRole("ORGANIZER");
        if (organizerMaNVs.isEmpty()) {
            organizerMaNVs = findEmployeeIdsByRole("ADMIN");
        }

        List<DemoEventSeed> events = buildDemoEvents();
        for (int i = 0; i < events.size(); i++) {
            DemoEventSeed event = events.get(i);
            String maLoaiSK = loaiSuKienIds.get(event.loaiSuKien());
            String organizerMaNV = organizerMaNVs.isEmpty() ? null : organizerMaNVs.get(i % organizerMaNVs.size());
            seedDemoEvent(event, i, maLoaiSK, organizerMaNV, fullReseed);
        }
    }

    private Map<String, String> seedLoaiSuKien() {
        Map<String, String> ids = new LinkedHashMap<>();
        ids.put("Concert", ensureLoaiSuKien("LSK0001", "Concert", "Sự kiện âm nhạc trực tiếp"));
        ids.put("Workshop", ensureLoaiSuKien("LSK0002", "Workshop", "Buổi thực hành chia sẻ kỹ năng"));
        ids.put("Hội thảo", ensureLoaiSuKien("LSK0003", "Hội thảo", "Buổi thảo luận chuyên đề"));
        ids.put("Fan Meeting", ensureLoaiSuKien("LSK0004", "Fan Meeting", "Sự kiện gặp gỡ người hâm mộ"));
        ids.put("Seminar", ensureLoaiSuKien("LSK0005", "Seminar", "Buổi chia sẻ chuyên đề quy mô vừa"));
        ids.put("Triển lãm", ensureLoaiSuKien("LSK0006", "Triển lãm", "Sự kiện trưng bày sản phẩm hoặc nghệ thuật"));
        ids.put("Lễ hội", ensureLoaiSuKien("LSK0007", "Lễ hội", "Sự kiện văn hóa, giải trí cộng đồng"));
        ids.put("Thể thao", ensureLoaiSuKien("LSK0008", "Thể thao", "Sự kiện thi đấu hoặc giao lưu thể thao"));
        ids.put("Networking", ensureLoaiSuKien("LSK0009", "Networking", "Sự kiện kết nối cộng đồng hoặc chuyên môn"));
        ids.put("Doanh nghiệp", ensureLoaiSuKien("LSK0010", "Doanh nghiệp", "Sự kiện nội bộ hoặc đối ngoại doanh nghiệp"));
        ids.put("Biểu diễn nghệ thuật", ensureLoaiSuKien("LSK0011", "Biểu diễn nghệ thuật", "Chương trình biểu diễn nghệ thuật"));
        return ids;
    }

    private List<DemoEventSeed> buildDemoEvents() {
        return List.of(
                new DemoEventSeed("SK0001", "Dề Dê Summer Concert 2026", "Concert", "NGOI_THEO_GHE", "OPEN", "Nhà thi đấu Phú Thọ", "01 Lữ Gia, Phường 15, Quận 11", "TP.HCM", 8000, "concert,live,music"),
                new DemoEventSeed("SK0002", "Fan Meeting Ánh Sao Trẻ", "Fan Meeting", "NGOI_THEO_GHE", "OPEN", "Saigon Exhibition Hall", "799 Nguyễn Văn Linh, Quận 7", "TP.HCM", 4500, "fan meeting,giao lưu"),
                new DemoEventSeed("SK0003", "Workshop Thiết kế Sản phẩm Số", "Workshop", "DUNG_THEO_KHU", "OPEN", "Dề Dê Creative Hub", "12 Nguyễn Thị Minh Khai, Quận 1", "TP.HCM", 1200, "workshop,product,ux"),
                new DemoEventSeed("SK0004", "Hội thảo AI trong Doanh nghiệp", "Hội thảo", "NGOI_THEO_GHE", "OPEN", "Trung tâm Hội nghị GEM", "08 Nguyễn Bỉnh Khiêm, Quận 1", "TP.HCM", 2200, "ai,hội thảo,doanh nghiệp"),
                new DemoEventSeed("SK0005", "Seminar Tài chính Cá nhân", "Seminar", "NGOI_THEO_GHE", "OPEN", "Riverside Palace", "360D Bến Vân Đồn, Quận 4", "TP.HCM", 1800, "seminar,tài chính"),
                new DemoEventSeed("SK0006", "Triển lãm Nghệ thuật Đương đại", "Triển lãm", "DUNG_THEO_KHU", "OPEN", "Nhà Văn hóa Thanh Niên", "04 Phạm Ngọc Thạch, Quận 1", "TP.HCM", 3500, "triển lãm,nghệ thuật"),
                new DemoEventSeed("SK0007", "Lễ hội Ẩm thực Đêm Sài Gòn", "Lễ hội", "DUNG_THEO_KHU", "OPEN", "Công viên Lê Văn Tám", "Võ Thị Sáu, Quận 1", "TP.HCM", 6000, "lễ hội,ẩm thực"),
                new DemoEventSeed("SK0008", "Giải Chạy Thành phố Xanh", "Thể thao", "NGOI_THEO_GHE", "OPEN", "Sân vận động Thống Nhất", "138 Đào Duy Từ, Quận 10", "TP.HCM", 12000, "thể thao,running"),
                new DemoEventSeed("SK0009", "Networking Founder Night", "Networking", "DUNG_THEO_KHU", "OPEN", "Dreamplex Auditorium", "195 Điện Biên Phủ, Bình Thạnh", "TP.HCM", 900, "networking,startup"),
                new DemoEventSeed("SK0010", "Ngày hội Tuyển dụng Công nghệ", "Doanh nghiệp", "NGOI_THEO_GHE", "OPEN", "White Palace Phạm Văn Đồng", "108 Phạm Văn Đồng, Thủ Đức", "TP.HCM", 5000, "doanh nghiệp,công nghệ"),
                new DemoEventSeed("SK0011", "Đêm Ballet và Hòa nhạc", "Biểu diễn nghệ thuật", "NGOI_THEO_GHE", "OPEN", "Nhà hát Thành phố", "07 Công Trường Lam Sơn, Quận 1", "TP.HCM", 1000, "ballet,hòa nhạc"),
                new DemoEventSeed("SK0012", "Lễ hội Indie Weekend", "Concert", "DUNG_THEO_KHU", "OPEN", "Sân khấu Lan Anh", "291 Cách Mạng Tháng 8, Quận 10", "TP.HCM", 4200, "indie,festival"),
                new DemoEventSeed("SK0013", "Workshop Nhiếp ảnh Đường phố", "Workshop", "NGOI_THEO_GHE", "OPEN", "Hanoi Creative Space", "01 Lương Yên, Hai Bà Trưng", "Hà Nội", 900, "workshop,nhiếp ảnh"),
                new DemoEventSeed("SK0014", "Hội thảo Giáo dục Tương lai", "Hội thảo", "NGOI_THEO_GHE", "OPEN", "Trung tâm Hội nghị Quốc gia", "57 Phạm Hùng, Nam Từ Liêm", "Hà Nội", 6000, "giáo dục,hội thảo"),
                new DemoEventSeed("SK0015", "Danang Beach Music Fest", "Lễ hội", "DUNG_THEO_KHU", "OPEN", "Công viên Biển Đông", "Võ Nguyên Giáp, Sơn Trà", "Đà Nẵng", 7000, "beach,music,festival"),
                new DemoEventSeed("SK0016", "Seminar Marketing Tăng trưởng", "Seminar", "NGOI_THEO_GHE", "SOON", "Ariyana Convention Centre", "107 Võ Nguyên Giáp, Ngũ Hành Sơn", "Đà Nẵng", 2500, "marketing,seminar"),
                new DemoEventSeed("SK0017", "Triển lãm Startup Mekong", "Triển lãm", "DUNG_THEO_KHU", "SOON", "Cần Thơ Expo", "108A Lê Lợi, Ninh Kiều", "Cần Thơ", 3000, "startup,mekong"),
                new DemoEventSeed("SK0018", "Gala Doanh nghiệp Xanh", "Doanh nghiệp", "NGOI_THEO_GHE", "SOON", "Capella Parkview", "03 Đặng Văn Sâm, Phú Nhuận", "TP.HCM", 1600, "gala,doanh nghiệp"),
                new DemoEventSeed("SK0019", "Đêm Kịch Nghệ thuật Mùa Xuân", "Biểu diễn nghệ thuật", "NGOI_THEO_GHE", "ENDED", "Sân khấu Idecaf", "28 Lê Thánh Tôn, Quận 1", "TP.HCM", 600, "kịch,nghệ thuật"),
                new DemoEventSeed("SK0020", "Giải Bóng rổ Cộng đồng", "Thể thao", "DUNG_THEO_KHU", "ENDED", "Nhà thi đấu Rạch Miễu", "01 Hoa Phượng, Phú Nhuận", "TP.HCM", 2500, "bóng rổ,thể thao")
        );
    }

    private void seedDemoEvent(DemoEventSeed seed, int index, String maLoaiSK, String organizerMaNV, boolean fullReseed) {
        String maDiaDiem = String.format("DD%04d", index + 1);
        ensureDiaDiem(maDiaDiem, seed.tenDiaDiem(), seed.diaChi(), seed.thanhPho(), seed.sucChua());

        LocalDateTime current = LocalDateTime.now().withSecond(0).withNano(0);
        EventTimes times = buildTimes(seed.salePhase(), index, current);
        Timestamp now = now();
        String status = deriveDemoStatus(times.saleStart(), times.saleEnd(), current);
        String posterPath = ImageUrlUtil.normalizeImageUrl(String.format("/images/events/%s-poster.svg", seed.maSK().toLowerCase()));
        String thumbPath = ImageUrlUtil.normalizeImageUrl(String.format("/images/events/%s-thumb.svg", seed.maSK().toLowerCase()));
        boolean exists = recordExists("SUKIEN", "MaSK = ?", seed.maSK());

        if (!exists) {
            jdbcTemplate.update(
                    "INSERT INTO SUKIEN (MaSK, TenSK, MoTa, MoTaNgan, Tags, HinhAnh, HinhAnhThumb, ThoiGianBatDau, ThoiGianKetThuc, " +
                            "ThoiGianMoBan, ThoiGianDongBan, TrangThaiSK, MaLoaiSK, MaDiaDiem, TongSoVe, SoVeDaBan, ThoiGianTao, CapNhatLanCuoi, MaNV, LoaiSoDo) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?, ?)",
                    seed.maSK(),
                    seed.tenSK(),
                    longDescription(seed),
                    shortDescription(seed),
                    seed.tags(),
                    posterPath,
                    thumbPath,
                    Timestamp.valueOf(times.eventStart()),
                    Timestamp.valueOf(times.eventEnd()),
                    Timestamp.valueOf(times.saleStart()),
                    Timestamp.valueOf(times.saleEnd()),
                    status,
                    maLoaiSK,
                    maDiaDiem,
                    now,
                    now,
                    organizerMaNV,
                    seed.loaiSoDo()
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE SUKIEN SET TenSK = ?, MoTa = ?, MoTaNgan = ?, Tags = ?, HinhAnh = ?, HinhAnhThumb = ?, " +
                            "ThoiGianBatDau = ?, ThoiGianKetThuc = ?, ThoiGianMoBan = ?, ThoiGianDongBan = ?, TrangThaiSK = ?, " +
                            "MaLoaiSK = ?, MaDiaDiem = ?, CapNhatLanCuoi = ?, MaNV = ?, LoaiSoDo = ? WHERE MaSK = ?",
                    seed.tenSK(),
                    longDescription(seed),
                    shortDescription(seed),
                    seed.tags(),
                    posterPath,
                    thumbPath,
                    Timestamp.valueOf(times.eventStart()),
                    Timestamp.valueOf(times.eventEnd()),
                    Timestamp.valueOf(times.saleStart()),
                    Timestamp.valueOf(times.saleEnd()),
                    status,
                    maLoaiSK,
                    maDiaDiem,
                    now,
                    organizerMaNV,
                    seed.loaiSoDo(),
                    seed.maSK()
            );
        }

        ensureBanToChuc(seed.maSK(), organizerMaNV, now);

        boolean hasZones = countWhere("KHUVUC", "MaSK = ?", seed.maSK()) > 0;
        int totalTickets = ensureStage(seed, fullReseed || !hasZones);
        jdbcTemplate.update(
                "UPDATE SUKIEN SET TongSoVe = ?, SoVeDaBan = COALESCE(SoVeDaBan, 0), CapNhatLanCuoi = ? WHERE MaSK = ?",
                totalTickets,
                now,
                seed.maSK()
        );
    }

    private EventTimes buildTimes(String salePhase, int index, LocalDateTime now) {
        LocalDateTime eventStart;
        LocalDateTime eventEnd;
        LocalDateTime saleStart;
        LocalDateTime saleEnd;

        if ("SOON".equals(salePhase)) {
            eventStart = now.plusDays(72 + index).withHour(9 + (index % 7)).withMinute(0);
            eventEnd = eventStart.plusHours(4);
            saleStart = now.plusDays(10 + (index % 5)).withHour(9).withMinute(0);
            saleEnd = eventStart.minusDays(2).withHour(23).withMinute(0);
        } else if ("ENDED".equals(salePhase)) {
            eventStart = now.minusDays(18 + index).withHour(19).withMinute(0);
            eventEnd = eventStart.plusHours(3);
            saleStart = eventStart.minusDays(45).withHour(9).withMinute(0);
            saleEnd = eventStart.minusDays(1).withHour(22).withMinute(0);
        } else {
            eventStart = now.plusDays(16 + (index * 3L)).withHour(18 + (index % 3)).withMinute(0);
            eventEnd = eventStart.plusHours(4);
            saleStart = now.minusDays(8 + (index % 6)).withHour(9).withMinute(0);
            saleEnd = eventStart.minusDays(1).withHour(23).withMinute(30);
        }

        return new EventTimes(eventStart, eventEnd, saleStart, saleEnd);
    }

    private String deriveDemoStatus(LocalDateTime saleStart, LocalDateTime saleEnd, LocalDateTime now) {
        if (now.isBefore(saleStart)) {
            return "Chưa mở bán";
        }
        if (!now.isAfter(saleEnd)) {
            return "Đang mở bán";
        }
        return "Đã kết thúc";
    }

    private void ensureBanToChuc(String maSK, String organizerMaNV, Timestamp now) {
        if (organizerMaNV == null || !tableExists("SUKIEN_BANTOCHUC")) {
            return;
        }

        jdbcTemplate.update("DELETE FROM SUKIEN_BANTOCHUC WHERE MaSK = ?", maSK);
        jdbcTemplate.update(
                "INSERT INTO SUKIEN_BANTOCHUC (MaSK, MaNV, VaiTroTrongSuKien, GhiChu, LaVaiTroChinh, ThoiGianTao, CapNhatLanCuoi) " +
                        "VALUES (?, ?, 'Trưởng ban tổ chức', 'Seed demo', true, ?, ?)",
                maSK,
                organizerMaNV,
                now,
                now
        );
    }

    private int ensureStage(DemoEventSeed seed, boolean rebuild) {
        if (!tableExists("KHUVUC")) {
            return 0;
        }

        if (!rebuild) {
            return sumCapacity(seed.maSK());
        }

        deleteWhereIfTableExists("GHENGOI", "MaSK = ?", seed.maSK());
        deleteWhereIfTableExists("KHUVUC", "MaSK = ?", seed.maSK());

        if ("DUNG_THEO_KHU".equals(seed.loaiSoDo())) {
            int total = 0;
            total += insertZone(seed.maSK(), "Early Bird", "#22c55e", 200, 4, BigDecimal.valueOf(150_000));
            total += insertZone(seed.maSK(), "GA Standing", "#3b82f6", 520, 6, BigDecimal.valueOf(350_000));
            total += insertZone(seed.maSK(), "VIP Standing", "#f59e0b", 120, 2, BigDecimal.valueOf(1_250_000));
            return total;
        }

        int total = 0;
        total += insertSeatedZone(seed.maSK(), "VIP", "#f59e0b", 2, 8, 2, BigDecimal.valueOf(2_500_000), "V");
        total += insertSeatedZone(seed.maSK(), "Premium", "#8b5cf6", 4, 10, 4, BigDecimal.valueOf(1_200_000), "P");
        total += insertSeatedZone(seed.maSK(), "Standard", "#2563eb", 6, 12, 6, BigDecimal.valueOf(450_000), "S");
        total += insertSeatedZone(seed.maSK(), "Balcony", "#14b8a6", 4, 10, 6, BigDecimal.valueOf(250_000), "B");
        return total;
    }

    private int insertSeatedZone(String maSK, String tenKhuVuc, String color, int rows, int cols, int maxPerCustomer,
                                 BigDecimal price, String seatPrefix) {
        String maKhuVuc = idGeneratorService.nextKhuVucId();
        int capacity = rows * cols;
        jdbcTemplate.update(
                "INSERT INTO KHUVUC (MaKhuVuc, TenKhuVuc, MauSacHienThi, SoGheToiDa, SoGheDaBan, SoVeToiDaPerKH, GiaVe, TrangThai, MaSK) " +
                        "VALUES (?, ?, ?, ?, 0, ?, ?, 'Đang bán', ?)",
                maKhuVuc,
                tenKhuVuc,
                color,
                capacity,
                maxPerCustomer,
                price,
                maSK
        );

        if (!tableExists("GHENGOI")) {
            return capacity;
        }

        for (int row = 0; row < rows; row++) {
            String rowLabel = seatPrefix + (char) ('A' + row);
            for (int col = 1; col <= cols; col++) {
                String tenGhe = rowLabel + String.format("%02d", col);
                jdbcTemplate.update(
                        "INSERT INTO GHENGOI (MaGhe, TenGhe, HangGhe, CotGhe, TrangThaiGhe, MaKhuVuc, MaSK) VALUES (?, ?, ?, ?, 'Trống', ?, ?)",
                        idGeneratorService.nextGheId(),
                        tenGhe,
                        rowLabel,
                        col,
                        maKhuVuc,
                        maSK
                );
            }
        }
        return capacity;
    }

    private int insertZone(String maSK, String tenKhuVuc, String color, int capacity, int maxPerCustomer, BigDecimal price) {
        jdbcTemplate.update(
                "INSERT INTO KHUVUC (MaKhuVuc, TenKhuVuc, MauSacHienThi, SoGheToiDa, SoGheDaBan, SoVeToiDaPerKH, GiaVe, TrangThai, MaSK) " +
                        "VALUES (?, ?, ?, ?, 0, ?, ?, 'Đang bán', ?)",
                idGeneratorService.nextKhuVucId(),
                tenKhuVuc,
                color,
                capacity,
                maxPerCustomer,
                price,
                maSK
        );
        return capacity;
    }

    private int sumCapacity(String maSK) {
        if (!tableExists("KHUVUC")) {
            return 0;
        }
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(SoGheToiDa), 0) FROM KHUVUC WHERE MaSK = ?",
                Integer.class,
                maSK
        );
        return total == null ? 0 : total;
    }

    private String shortDescription(DemoEventSeed seed) {
        return seed.tenSK() + " tại " + seed.tenDiaDiem() + ", " + seed.thanhPho() + ".";
    }

    private String longDescription(DemoEventSeed seed) {
        return "Sự kiện demo " + seed.tenSK() + " thuộc nhóm " + seed.loaiSuKien()
                + ". Người mua có thể xem chi tiết, vào hàng đợi khi cần, chọn ghế hoặc khu đứng, thanh toán và nhận vé QR trong tài khoản.";
    }

    private void ensureDiaDiem(String maDiaDiem, String tenDiaDiem, String diaChi, String thanhPho, int sucChua) {
        String linkGoogleMap = "https://maps.google.com/?q=" + diaChi.replace(" ", "+") + "," + thanhPho.replace(" ", "+");
        if (!recordExists("DIADIEM", "MaDiaDiem = ?", maDiaDiem)) {
            jdbcTemplate.update(
                    "INSERT INTO DIADIEM (MaDiaDiem, TenDiaDiem, DiaChi, ThanhPho, SucChuaToiDa, MoTa, TrangThai, LinkGoogleMap) " +
                            "VALUES (?, ?, ?, ?, ?, ?, 'Đang hoạt động', ?)",
                    maDiaDiem,
                    tenDiaDiem,
                    diaChi,
                    thanhPho,
                    sucChua,
                    "Địa điểm demo phục vụ dữ liệu Dề Dê Tickets.",
                    linkGoogleMap
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE DIADIEM SET TenDiaDiem = ?, DiaChi = ?, ThanhPho = ?, SucChuaToiDa = ?, TrangThai = 'Đang hoạt động', LinkGoogleMap = ? WHERE MaDiaDiem = ?",
                    tenDiaDiem,
                    diaChi,
                    thanhPho,
                    sucChua,
                    linkGoogleMap,
                    maDiaDiem
            );
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

        String maLoaiSK = isIdAvailable("LOAISUKIEN", "MaLoaiSK", preferredMaLoaiSK)
                ? preferredMaLoaiSK
                : idGeneratorService.nextLoaiSuKienId();

        jdbcTemplate.update(
                "INSERT INTO LOAISUKIEN (MaLoaiSK, TenLoaiSK, MoTa) VALUES (?, ?, ?)",
                maLoaiSK,
                tenLoaiSK,
                moTa
        );
        return maLoaiSK;
    }

    private boolean fullReseedDemoDataIfAllowed() {
        if (!isDevOrLocalProfile()) {
            System.err.println("============================================================");
            System.err.println("CẢNH BÁO: app.demo.full-reseed-on-start=true nhưng profile hiện tại không phải dev/local.");
            System.err.println("DataInitializer bỏ qua full reseed để tránh mất dữ liệu production.");
            System.err.println("============================================================");
            return false;
        }

        System.err.println("============================================================");
        System.err.println("ĐANG FULL RESEED DEMO LOCAL/DEV: xóa dữ liệu demo/runtime và giữ lại user admin.");
        System.err.println("============================================================");

        deleteIfTableExists("LICHSUSOATVE");
        deleteIfTableExists("LICHSUGUI_EMAIL");
        deleteIfTableExists("LICHSUGUIEMAIL");
        deleteIfTableExists("GIAODICHTHANHTOAN");
        deleteIfTableExists("VE");
        deleteIfTableExists("DONHANG_CHITIET");
        deleteIfTableExists("DONHANG");
        deleteIfTableExists("HANGDOIAO");
        deleteIfTableExists("LOG_HANH_VI");
        deleteIfTableExists("GHENGOI");
        deleteIfTableExists("KHUVUC");
        deleteIfTableExists("SUKIEN_BANTOCHUC");
        deleteIfTableExists("SUKIEN");
        deleteIfTableExists("PHIEUGIAMGIA");
        deleteIfTableExists("KHACHHANG");
        deleteWhereIfTableExists("NHANVIEN", "MaND NOT IN (SELECT MaND FROM NGUOIDUNG WHERE LOWER(TenTaiKhoan) = 'admin')");
        deleteWhereIfTableExists("CHITIETVAITRO", "MaND NOT IN (SELECT MaND FROM NGUOIDUNG WHERE LOWER(TenTaiKhoan) = 'admin')");
        deleteWhereIfTableExists("NGUOIDUNG", "LOWER(TenTaiKhoan) <> 'admin' OR TenTaiKhoan IS NULL");

        return true;
    }

    private void resetRuntimeDataOnStartIfAllowed() {
        if (!isDevOrLocalProfile()) {
            System.err.println("============================================================");
            System.err.println("CẢNH BÁO: app.dev.reset-data-on-start=true nhưng profile hiện tại không phải dev/local.");
            System.err.println("DataInitializer bỏ qua reset tự động để tránh mất dữ liệu ngoài ý muốn.");
            System.err.println("============================================================");
            return;
        }

        deleteIfTableExists("LICHSUSOATVE");
        deleteIfTableExists("LICHSUGUI_EMAIL");
        deleteIfTableExists("GIAODICHTHANHTOAN");
        deleteIfTableExists("VE");
        deleteIfTableExists("DONHANG_CHITIET");
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

    private void seedDemoPurchaseHistory() {
        if (!seedPurchaseHistoryOnStart) {
            System.out.println("DataInitializer: app.demo.seed-purchase-history-on-start=false, không seed lịch sử mua demo.");
            return;
        }
        if (!isDevOrLocalProfile()) {
            System.err.println("============================================================");
            System.err.println("CẢNH BÁO: app.demo.seed-purchase-history-on-start=true nhưng profile hiện tại không phải dev/local.");
            System.err.println("DataInitializer bỏ qua seed lịch sử mua demo để tránh ảnh hưởng production.");
            System.err.println("============================================================");
            return;
        }
        if (!tableExists("DONHANG") || !tableExists("GIAODICHTHANHTOAN") || !tableExists("VE")
                || !tableExists("KHUVUC") || !tableExists("SUKIEN") || !tableExists("KHACHHANG")) {
            System.err.println("DataInitializer: thiếu bảng runtime, bỏ qua seed lịch sử mua demo.");
            return;
        }
        if (countDemoPurchaseHistory() > 0) {
            System.out.println("DataInitializer: đã tồn tại batch lịch sử mua DEMO_, bỏ qua để tránh nhân đôi dữ liệu.");
            return;
        }

        List<EventInventory> events = loadDemoEventInventories();
        List<EventInventory> paidEvents = events.stream()
                .filter(event -> !"Chưa mở bán".equalsIgnoreCase(event.trangThaiSK()))
                .filter(EventInventory::hasAvailability)
                .toList();
        List<EventInventory> standingEvents = paidEvents.stream()
                .filter(event -> "DUNG_THEO_KHU".equalsIgnoreCase(event.loaiSoDo()))
                .filter(EventInventory::hasStandingAvailability)
                .toList();
        List<String> customerIds = jdbcTemplate.queryForList("SELECT MaKH FROM KHACHHANG ORDER BY MaKH", String.class);
        List<String> staffIds = findEmployeeIdsByRole("STAFF");

        if (paidEvents.isEmpty() || standingEvents.isEmpty() || customerIds.isEmpty() || staffIds.isEmpty()) {
            System.err.println("DataInitializer: thiếu sự kiện/khách hàng/nhân viên để seed lịch sử mua demo.");
            return;
        }

        Map<String, String> customerEmails = loadCustomerEmails();
        Map<String, Integer> customerZoneCounts = loadExistingPaidTicketLimits();
        PurchaseSeedSummary summary = new PurchaseSeedSummary();
        Random random = new Random(20260603L);
        List<String> statuses = buildDemoOrderStatuses(760, random);

        int sequence = 1;
        for (String status : statuses) {
            boolean created = false;
            for (int attempt = 0; attempt < 80 && !created; attempt++) {
                EventInventory event = OrderStatus.DA_THANH_TOAN.equals(status)
                        ? pickWeightedEvent(paidEvents, random)
                        : pickWeightedEvent(standingEvents, random);
                String customerId = customerIds.get(random.nextInt(customerIds.size()));
                if (OrderStatus.DA_THANH_TOAN.equals(status)) {
                    created = seedPaidOrder(sequence, event, customerId, customerZoneCounts, customerEmails, random, summary);
                } else if (OrderStatus.DA_HUY.equals(status)) {
                    created = seedCancelledOrder(sequence, event, customerId, customerZoneCounts, random, summary);
                } else {
                    created = seedPendingOrder(sequence, event, customerId, customerZoneCounts, random, summary);
                }
            }
            if (created) {
                sequence++;
            }
        }

        updateSoldCounters();
        seedScanHistory(staffIds, random, summary);
        seedBehaviorLogs(customerIds, events, random, summary);
        updateCustomerSpendingAndTier();
        runDemoPurchaseConsistencyCheck();
        logDemoPurchaseSummary(summary);
    }

    private int countDemoPurchaseHistory() {
        if (!tableExists("DONHANG")) {
            return 0;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DONHANG WHERE MaDonHang LIKE 'DEMO\\_DH\\_%' ESCAPE '\\\\'",
                Integer.class
        );
        return count == null ? 0 : count;
    }

    private boolean seedPaidOrder(int sequence, EventInventory event, String maKH, Map<String, Integer> customerZoneCounts,
                                  Map<String, String> customerEmails, Random random, PurchaseSeedSummary summary) {
        TicketSelection selection = selectTicketsForOrder(event, maKH, customerZoneCounts, random, true);
        if (selection == null || selection.quantity() <= 0) {
            return false;
        }

        String maDonHang = demoOrderId(sequence);
        Timestamp orderTime = randomOrderTime(event, random, false);
        Timestamp expiresAt = new Timestamp(orderTime.getTime() + 10L * 60L * 1000L);
        insertDemoOrder(maDonHang, maKH, selection.totalPrice(), OrderStatus.DA_THANH_TOAN, orderTime, expiresAt);
        if ("DUNG_THEO_KHU".equalsIgnoreCase(event.loaiSoDo())) {
            insertOrderDetail(maDonHang, event.maSK(), selection.zone().maKhuVuc(), selection.quantity(), selection.zone().giaVe());
        }

        seedPaymentTransaction(maDonHang, sequence, selection.totalPrice(), "Thành công", randomPaymentMethod(random), orderTime, 1, null);
        for (int i = 0; i < selection.quantity(); i++) {
            String maGhe = selection.seatIds().isEmpty() ? null : selection.seatIds().get(i);
            createTicketForPaidOrder(sequence, i + 1, maDonHang, event.maSK(), selection.zone(), maGhe, orderTime, summary);
        }

        incrementCustomerZoneCount(customerZoneCounts, maKH, event.maSK(), selection.zone().maKhuVuc(), selection.quantity());
        seedEmailLog(maDonHang, customerEmails.get(maKH), random, summary);
        summary.orders++;
        summary.paidOrders++;
        summary.transactions++;
        return true;
    }

    private boolean seedCancelledOrder(int sequence, EventInventory event, String maKH, Map<String, Integer> customerZoneCounts,
                                       Random random, PurchaseSeedSummary summary) {
        TicketSelection selection = selectTicketsForOrder(event, maKH, customerZoneCounts, random, false);
        if (selection == null || selection.quantity() <= 0) {
            return false;
        }

        String maDonHang = demoOrderId(sequence);
        Timestamp orderTime = randomOrderTime(event, random, false);
        Timestamp expiresAt = new Timestamp(orderTime.getTime() + 10L * 60L * 1000L);
        insertDemoOrder(maDonHang, maKH, selection.totalPrice(), OrderStatus.DA_HUY, orderTime, expiresAt);
        insertOrderDetail(maDonHang, event.maSK(), selection.zone().maKhuVuc(), selection.quantity(), selection.zone().giaVe());
        String transactionStatus = random.nextInt(10) < 7 ? "Thất bại" : "Hết thời gian";
        String error = "Hết thời gian".equals(transactionStatus) ? "Đơn hàng demo hết hạn thanh toán" : "Thanh toán demo thất bại";
        seedPaymentTransaction(maDonHang, sequence, selection.totalPrice(), transactionStatus, randomPaymentMethod(random), orderTime, 1, error);

        summary.orders++;
        summary.cancelledOrders++;
        summary.transactions++;
        return true;
    }

    private boolean seedPendingOrder(int sequence, EventInventory event, String maKH, Map<String, Integer> customerZoneCounts,
                                     Random random, PurchaseSeedSummary summary) {
        TicketSelection selection = selectTicketsForOrder(event, maKH, customerZoneCounts, random, false);
        if (selection == null || selection.quantity() <= 0) {
            return false;
        }

        String maDonHang = demoOrderId(sequence);
        Timestamp orderTime = new Timestamp(System.currentTimeMillis() - random.nextInt(8 * 60 * 1000));
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (2L + random.nextInt(9)) * 60L * 1000L);
        insertDemoOrder(maDonHang, maKH, selection.totalPrice(), OrderStatus.CHO_THANH_TOAN, orderTime, expiresAt);
        insertOrderDetail(maDonHang, event.maSK(), selection.zone().maKhuVuc(), selection.quantity(), selection.zone().giaVe());
        String transactionStatus = random.nextInt(10) < 6 ? "Đang xử lý" : "Thất bại";
        String error = "Thất bại".equals(transactionStatus) ? "Thanh toán demo đang chờ thử lại" : null;
        seedPaymentTransaction(maDonHang, sequence, selection.totalPrice(), transactionStatus, randomPaymentMethod(random), orderTime, 1, error);

        summary.orders++;
        summary.pendingOrders++;
        summary.transactions++;
        return true;
    }

    private void createTicketForPaidOrder(int orderSequence, int ticketSequence, String maDonHang, String maSK, ZoneInventory zone,
                                          String maGhe, Timestamp orderTime, PurchaseSeedSummary summary) {
        String maVe = String.format("DEMO_VE_%04d_%02d", orderSequence, ticketSequence);
        String maQR = "DEMO-QR-" + maVe + "-" + maSK + "-" + Math.abs(Objects.hash(maVe, maDonHang));
        Timestamp issuedAt = new Timestamp(orderTime.getTime() + 30_000L + ticketSequence * 1000L);

        jdbcTemplate.update(
                "INSERT INTO VE (MaVe, MaQR, GiaVe, TrangThaiVe, ThoiGianPhat, MaDonHang, MaGhe, MaKhuVuc, MaSK) " +
                        "VALUES (?, ?, ?, 'Chưa sử dụng', ?, ?, ?, ?, ?)",
                maVe,
                maQR,
                zone.giaVe(),
                issuedAt,
                maDonHang,
                maGhe,
                maGhe == null ? zone.maKhuVuc() : null,
                maSK
        );

        if (maGhe != null) {
            jdbcTemplate.update(
                    "UPDATE GHENGOI SET TrangThaiGhe = 'Đã bán', ThoiGianKhoaTam = NULL, MaPhienKhoa = NULL WHERE MaGhe = ?",
                    maGhe
            );
        }
        summary.tickets++;
    }

    private void updateSoldCounters() {
        jdbcTemplate.update(
                "UPDATE SUKIEN sk SET SoVeDaBan = (" +
                        "SELECT COUNT(*) FROM VE v JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang " +
                        "WHERE v.MaSK = sk.MaSK AND dh.TrangThaiDonHang = ?" +
                        ") WHERE sk.MaSK LIKE 'SK00%'",
                OrderStatus.DA_THANH_TOAN
        );

        jdbcTemplate.update(
                "UPDATE KHUVUC kv SET SoGheDaBan = (" +
                        "SELECT COUNT(*) FROM VE v " +
                        "JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang " +
                        "LEFT JOIN GHENGOI g ON g.MaGhe = v.MaGhe " +
                        "WHERE dh.TrangThaiDonHang = ? AND (v.MaKhuVuc = kv.MaKhuVuc OR g.MaKhuVuc = kv.MaKhuVuc)" +
                        ") WHERE kv.MaSK LIKE 'SK00%'",
                OrderStatus.DA_THANH_TOAN
        );

        if (tableExists("GHENGOI")) {
            jdbcTemplate.update(
                    "UPDATE GHENGOI g SET TrangThaiGhe = 'Đã bán', ThoiGianKhoaTam = NULL, MaPhienKhoa = NULL " +
                            "WHERE EXISTS (SELECT 1 FROM VE v JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang " +
                            "WHERE v.MaGhe = g.MaGhe AND dh.TrangThaiDonHang = ?)",
                    OrderStatus.DA_THANH_TOAN
            );
        }
    }

    private void seedPaymentTransaction(String maDonHang, int sequence, BigDecimal amount, String status, String method,
                                        Timestamp orderTime, int attempt, String error) {
        Timestamp paymentTime = new Timestamp(orderTime.getTime() + (2L + sequence % 9) * 60L * 1000L);
        jdbcTemplate.update(
                "INSERT INTO GIAODICHTHANHTOAN (MaGiaoDich, SoTienThanhToan, PhuongThucTT, TrangThaiGD, MaGiaoDichBenThu3, LanThuLai, ThoiGianThucHien, GhiChuLoi, MaDonHang) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                String.format("DEMO_GD_%04d", sequence),
                amount,
                method,
                status,
                "Thành công".equals(status) ? String.format("DEMO_GATEWAY_%04d", sequence) : null,
                attempt,
                paymentTime,
                error,
                maDonHang
        );
    }

    private void seedEmailLog(String maDonHang, String email, Random random, PurchaseSeedSummary summary) {
        if (email == null || email.isBlank() || !tableExists("LICHSUGUI_EMAIL")) {
            return;
        }
        List<Map<String, Object>> tickets = jdbcTemplate.queryForList(
                "SELECT MaVe, ThoiGianPhat FROM VE WHERE MaDonHang = ? ORDER BY MaVe",
                maDonHang
        );
        Timestamp baseTime = tickets.isEmpty() ? now() : (Timestamp) getRowValue(tickets.get(0), "ThoiGianPhat", "THOIGIANPHAT");
        insertEmailLog("DEMO_EMAIL_ORDER_" + maDonHang.substring("DEMO_DH_".length()), "XAC_NHAN_DON_HANG", email,
                random.nextInt(100) < 94 ? "Da_gui" : "That_bai", baseTime, null, maDonHang);
        summary.emailLogs++;

        int index = 1;
        for (Map<String, Object> ticket : tickets) {
            String maVe = String.valueOf(getRowValue(ticket, "MaVe", "MAVE"));
            insertEmailLog(String.format("DEMO_EMAIL_QR_%04d_%02d", extractDemoSequence(maDonHang), index), "QR_CODE", email,
                    random.nextInt(100) < 90 ? "Da_gui" : "That_bai", baseTime, maVe, maDonHang);
            summary.emailLogs++;
            index++;
        }
    }

    private void seedScanHistory(List<String> staffIds, Random random, PurchaseSeedSummary summary) {
        if (!tableExists("LICHSUSOATVE")) {
            return;
        }

        List<Map<String, Object>> tickets = jdbcTemplate.queryForList(
                "SELECT v.MaVe, v.MaSK, sk.TrangThaiSK, sk.ThoiGianBatDau, sk.ThoiGianKetThuc " +
                        "FROM VE v JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang " +
                        "JOIN SUKIEN sk ON sk.MaSK = v.MaSK " +
                        "WHERE dh.MaDonHang LIKE 'DEMO\\_DH\\_%' ESCAPE '\\\\' AND dh.TrangThaiDonHang = ? " +
                        "ORDER BY v.MaSK, v.MaVe",
                OrderStatus.DA_THANH_TOAN
        );

        int sequence = 1;
        Set<String> usedTickets = new HashSet<>();
        for (Map<String, Object> ticket : tickets) {
            String eventStatus = String.valueOf(getRowValue(ticket, "TrangThaiSK", "TRANGTHAISK"));
            boolean ended = "Đã kết thúc".equalsIgnoreCase(eventStatus);
            int threshold = ended ? 78 : 0;
            if (random.nextInt(100) >= threshold) {
                continue;
            }

            String maVe = String.valueOf(getRowValue(ticket, "MaVe", "MAVE"));
            Timestamp scanTime = randomScanTime(ticket, random);
            insertScanLog(String.format("DEMO_SCAN_%05d", sequence++), scanTime, "Hợp lệ",
                    randomGate(random), randomSource(random), randomSyncFlag(random), maVe, randomStaff(staffIds, random));
            jdbcTemplate.update("UPDATE VE SET TrangThaiVe = 'Đã sử dụng', ThoiGianSuDung = ? WHERE MaVe = ?", scanTime, maVe);
            usedTickets.add(maVe);
            summary.scanLogs++;
        }

        List<String> allTickets = tickets.stream()
                .map(row -> String.valueOf(getRowValue(row, "MaVe", "MAVE")))
                .toList();
        int errorScans = Math.min(60, Math.max(15, allTickets.size() / 30));
        for (int i = 0; i < errorScans; i++) {
            String result = switch (i % 3) {
                case 0 -> "Vé đã sử dụng";
                case 1 -> "Sai sự kiện";
                default -> "Vé không tìm thấy";
            };
            String maVe = "Vé không tìm thấy".equals(result) || allTickets.isEmpty()
                    ? null
                    : allTickets.get(random.nextInt(allTickets.size()));
            if ("Vé đã sử dụng".equals(result) && !usedTickets.isEmpty()) {
                maVe = usedTickets.stream().skip(random.nextInt(usedTickets.size())).findFirst().orElse(maVe);
            }
            insertScanLog(String.format("DEMO_SCAN_%05d", sequence++), randomRecentTimestamp(25, random), result,
                    randomGate(random), randomSource(random), randomSyncFlag(random), maVe, randomStaff(staffIds, random));
            summary.scanLogs++;
        }
    }

    private void seedBehaviorLogs(List<String> customerIds, List<EventInventory> events, Random random, PurchaseSeedSummary summary) {
        if (!tableExists("LOG_HANH_VI") || customerIds.isEmpty() || events.isEmpty()) {
            return;
        }
        String[] actions = {"XEM_SK", "CLICK_DAT_VE", "BO_GIO_HANG"};
        String[] devices = {"Web", "Web", "Web", "Mobile"};
        List<EventInventory> weighted = new ArrayList<>(events);
        int sequence = 1;
        for (int i = 0; i < 2400; i++) {
            EventInventory event = pickWeightedEvent(weighted, random);
            int roll = random.nextInt(100);
            String action = roll < 68 ? actions[0] : (roll < 90 ? actions[1] : actions[2]);
            String maKH = customerIds.get(random.nextInt(customerIds.size()));
            jdbcTemplate.update(
                    "INSERT INTO LOG_HANH_VI (MaLog, LoaiHanhDong, MaSK, ThoiGian, MaKH, ThietBi) VALUES (?, ?, ?, ?, ?, ?)",
                    String.format("DEMO_LOG_%05d", sequence++),
                    action,
                    event.maSK(),
                    randomRecentTimestamp(90, random),
                    maKH,
                    devices[random.nextInt(devices.length)]
            );
            summary.behaviorLogs++;
        }
    }

    private void updateCustomerSpendingAndTier() {
        jdbcTemplate.update(
                "UPDATE KHACHHANG kh SET TongChiTieu = (" +
                        "SELECT COALESCE(SUM(dh.ThanhTien), 0) FROM DONHANG dh " +
                        "WHERE dh.MaKH = kh.MaKH AND dh.TrangThaiDonHang = ?" +
                        "), CapNhatLanCuoi = ?",
                OrderStatus.DA_THANH_TOAN,
                now()
        );

        if (!tableExists("HANGTHANHVIEN") || !columnExists("KHACHHANG", "MaHangThanhVien")) {
            return;
        }
        List<Map<String, Object>> customers = jdbcTemplate.queryForList("SELECT MaKH, TongChiTieu FROM KHACHHANG");
        for (Map<String, Object> customer : customers) {
            BigDecimal spending = (BigDecimal) getRowValue(customer, "TongChiTieu", "TONGCHITIEU");
            List<String> tierIds = jdbcTemplate.queryForList(
                    "SELECT MaHangThanhVien FROM HANGTHANHVIEN " +
                            "WHERE COALESCE(ChiTieuToiThieu, 0) <= ? " +
                            "ORDER BY COALESCE(ChiTieuToiThieu, 0) DESC LIMIT 1",
                    String.class,
                    spending != null ? spending : BigDecimal.ZERO
            );
            if (!tierIds.isEmpty()) {
                jdbcTemplate.update("UPDATE KHACHHANG SET MaHangThanhVien = ? WHERE MaKH = ?",
                        tierIds.get(0),
                        getRowValue(customer, "MaKH", "MAKH"));
            }
        }
    }

    private void runDemoPurchaseConsistencyCheck() {
        long eventMismatch = queryLong(
                "SELECT COUNT(*) FROM SUKIEN sk WHERE sk.MaSK LIKE 'SK00%' AND COALESCE(sk.SoVeDaBan, 0) <> (" +
                        "SELECT COUNT(*) FROM VE v JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang " +
                        "WHERE v.MaSK = sk.MaSK AND dh.TrangThaiDonHang = ?)",
                OrderStatus.DA_THANH_TOAN
        );
        long zoneMismatch = queryLong(
                "SELECT COUNT(*) FROM KHUVUC kv WHERE kv.MaSK LIKE 'SK00%' AND COALESCE(kv.SoGheDaBan, 0) <> (" +
                        "SELECT COUNT(*) FROM VE v JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang " +
                        "LEFT JOIN GHENGOI g ON g.MaGhe = v.MaGhe " +
                        "WHERE dh.TrangThaiDonHang = ? AND (v.MaKhuVuc = kv.MaKhuVuc OR g.MaKhuVuc = kv.MaKhuVuc))",
                OrderStatus.DA_THANH_TOAN
        );
        long duplicateSeats = queryLong(
                "SELECT COUNT(*) FROM (SELECT MaGhe FROM VE WHERE MaGhe IS NOT NULL GROUP BY MaGhe HAVING COUNT(*) > 1) duplicated"
        );
        long duplicateQr = queryLong(
                "SELECT COUNT(*) FROM (SELECT MaQR FROM VE GROUP BY MaQR HAVING COUNT(*) > 1) duplicated"
        );
        long unpaidTickets = queryLong(
                "SELECT COUNT(*) FROM VE v JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang WHERE dh.TrangThaiDonHang <> ?",
                OrderStatus.DA_THANH_TOAN
        );

        if (eventMismatch + zoneMismatch + duplicateSeats + duplicateQr + unpaidTickets == 0) {
            System.out.println("DataInitializer consistency: lịch sử mua demo hợp lệ.");
        } else {
            System.err.println("DataInitializer consistency warning: eventMismatch=" + eventMismatch
                    + ", zoneMismatch=" + zoneMismatch
                    + ", duplicateSeats=" + duplicateSeats
                    + ", duplicateQr=" + duplicateQr
                    + ", unpaidTickets=" + unpaidTickets);
        }
    }

    private void logDemoPurchaseSummary(PurchaseSeedSummary summary) {
        System.out.println("============================================================");
        System.out.println("SEED LỊCH SỬ MUA DEMO HOÀN TẤT");
        System.out.println("Số đơn đã tạo: " + summary.orders + " (paid=" + summary.paidOrders
                + ", cancelled=" + summary.cancelledOrders + ", pending=" + summary.pendingOrders + ")");
        System.out.println("Số vé đã tạo: " + summary.tickets);
        System.out.println("Số giao dịch: " + summary.transactions);
        System.out.println("Số lịch sử email: " + summary.emailLogs);
        System.out.println("Số lịch sử quét: " + summary.scanLogs);
        System.out.println("Số log hành vi: " + summary.behaviorLogs);
        System.out.println("Top 5 sự kiện doanh thu cao nhất:");
        List<Map<String, Object>> topEvents = jdbcTemplate.queryForList(
                "SELECT sk.MaSK, sk.TenSK, COALESCE(SUM(v.GiaVe), 0) AS DoanhThu " +
                        "FROM VE v JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang " +
                        "JOIN SUKIEN sk ON sk.MaSK = v.MaSK " +
                        "WHERE dh.MaDonHang LIKE 'DEMO\\_DH\\_%' ESCAPE '\\\\' AND dh.TrangThaiDonHang = ? " +
                        "GROUP BY sk.MaSK, sk.TenSK ORDER BY DoanhThu DESC LIMIT 5",
                OrderStatus.DA_THANH_TOAN
        );
        for (Map<String, Object> row : topEvents) {
            System.out.println("- " + getRowValue(row, "MaSK", "MASK") + " | "
                    + getRowValue(row, "TenSK", "TENSK") + " | "
                    + getRowValue(row, "DoanhThu", "DOANHTHU") + " VND");
        }
        System.out.println("============================================================");
    }

    private List<EventInventory> loadDemoEventInventories() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT MaSK, TenSK, TrangThaiSK, LoaiSoDo, ThoiGianBatDau, ThoiGianKetThuc, ThoiGianMoBan, ThoiGianDongBan " +
                        "FROM SUKIEN WHERE MaSK LIKE 'SK00%' ORDER BY MaSK"
        );
        List<EventInventory> result = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> row : rows) {
            String maSK = String.valueOf(getRowValue(row, "MaSK", "MASK"));
            EventInventory event = new EventInventory(
                    maSK,
                    String.valueOf(getRowValue(row, "TenSK", "TENSK")),
                    String.valueOf(getRowValue(row, "TrangThaiSK", "TRANGTHAISK")),
                    String.valueOf(getRowValue(row, "LoaiSoDo", "LOAISODO")),
                    (Timestamp) getRowValue(row, "ThoiGianBatDau", "THOIGIANBATDAU"),
                    (Timestamp) getRowValue(row, "ThoiGianKetThuc", "THOIGIANKETTHUC"),
                    (Timestamp) getRowValue(row, "ThoiGianMoBan", "THOIGIANMOBAN"),
                    (Timestamp) getRowValue(row, "ThoiGianDongBan", "THOIGIANDONGBAN"),
                    demoEventWeight(index)
            );
            event.zones().addAll(loadZonesForEvent(maSK, event.loaiSoDo()));
            result.add(event);
            index++;
        }
        return result;
    }

    private List<ZoneInventory> loadZonesForEvent(String maSK, String loaiSoDo) {
        List<Map<String, Object>> zones = jdbcTemplate.queryForList(
                "SELECT MaKhuVuc, GiaVe, SoGheToiDa, SoGheDaBan, SoVeToiDaPerKH FROM KHUVUC WHERE MaSK = ? AND TrangThai = 'Đang bán' ORDER BY GiaVe DESC",
                maSK
        );
        List<ZoneInventory> result = new ArrayList<>();
        for (Map<String, Object> row : zones) {
            String maKhuVuc = String.valueOf(getRowValue(row, "MaKhuVuc", "MAKHUVUC"));
            BigDecimal giaVe = (BigDecimal) getRowValue(row, "GiaVe", "GIAVE");
            int capacity = numberValue(getRowValue(row, "SoGheToiDa", "SOGHETOIDA"));
            int sold = numberValue(getRowValue(row, "SoGheDaBan", "SOGHEDABAN"));
            int maxPerCustomer = Math.max(1, numberValue(getRowValue(row, "SoVeToiDaPerKH", "SOVETOIDADAPERKH")));
            List<String> freeSeats = "NGOI_THEO_GHE".equalsIgnoreCase(loaiSoDo)
                    ? jdbcTemplate.queryForList(
                    "SELECT MaGhe FROM GHENGOI WHERE MaSK = ? AND MaKhuVuc = ? AND TrangThaiGhe = 'Trống' ORDER BY MaGhe",
                    String.class,
                    maSK,
                    maKhuVuc
            )
                    : new ArrayList<>();
            result.add(new ZoneInventory(maKhuVuc, giaVe != null ? giaVe : BigDecimal.ZERO,
                    capacity, Math.max(0, capacity - sold), maxPerCustomer, freeSeats));
        }
        return result;
    }

    private TicketSelection selectTicketsForOrder(EventInventory event, String maKH, Map<String, Integer> customerZoneCounts,
                                                  Random random, boolean reserveInventory) {
        List<ZoneInventory> candidates = new ArrayList<>(event.zones());
        candidates.removeIf(zone -> !zone.hasAvailability(event.loaiSoDo()));
        candidates.sort(Comparator.comparing(ZoneInventory::giaVe).reversed());
        if (candidates.isEmpty()) {
            return null;
        }

        for (int attempt = 0; attempt < candidates.size(); attempt++) {
            ZoneInventory zone = candidates.get(random.nextInt(candidates.size()));
            int alreadyBought = customerZoneCounts.getOrDefault(limitKey(maKH, event.maSK(), zone.maKhuVuc()), 0);
            int allowedByCustomer = Math.max(0, zone.maxPerCustomer() - alreadyBought);
            int available = "NGOI_THEO_GHE".equalsIgnoreCase(event.loaiSoDo()) ? zone.freeSeats().size() : zone.remainingStanding();
            int maxQty = Math.min(Math.min(4, allowedByCustomer), available);
            if (maxQty <= 0) {
                candidates.remove(zone);
                continue;
            }

            int quantity = 1 + random.nextInt(maxQty);
            if (random.nextInt(100) < 55) {
                quantity = 1;
            } else if (quantity > 2 && random.nextInt(100) < 70) {
                quantity = 2;
            }

            List<String> seatIds = new ArrayList<>();
            if ("NGOI_THEO_GHE".equalsIgnoreCase(event.loaiSoDo())) {
                if (!reserveInventory) {
                    return null;
                }
                for (int i = 0; i < quantity; i++) {
                    seatIds.add(zone.freeSeats().remove(0));
                }
            } else if (reserveInventory) {
                zone.reduceStanding(quantity);
            }

            return new TicketSelection(zone, quantity, seatIds, zone.giaVe().multiply(BigDecimal.valueOf(quantity)));
        }
        return null;
    }

    private EventInventory pickWeightedEvent(List<EventInventory> events, Random random) {
        int totalWeight = events.stream().mapToInt(EventInventory::weight).sum();
        int point = random.nextInt(Math.max(totalWeight, 1));
        int cursor = 0;
        for (EventInventory event : events) {
            cursor += event.weight();
            if (point < cursor) {
                return event;
            }
        }
        return events.get(events.size() - 1);
    }

    private List<String> buildDemoOrderStatuses(int targetOrders, Random random) {
        List<String> statuses = new ArrayList<>();
        int paid = Math.round(targetOrders * 0.70f);
        int cancelled = Math.round(targetOrders * 0.15f);
        int pending = targetOrders - paid - cancelled;
        for (int i = 0; i < paid; i++) statuses.add(OrderStatus.DA_THANH_TOAN);
        for (int i = 0; i < cancelled; i++) statuses.add(OrderStatus.DA_HUY);
        for (int i = 0; i < pending; i++) statuses.add(OrderStatus.CHO_THANH_TOAN);
        java.util.Collections.shuffle(statuses, random);
        return statuses;
    }

    private void insertDemoOrder(String maDonHang, String maKH, BigDecimal amount, String status, Timestamp orderTime, Timestamp expiresAt) {
        jdbcTemplate.update(
                "INSERT INTO DONHANG (MaDonHang, SoDonHang, TongTien, ThanhTien, TrangThaiDonHang, ThoiGianDat, ThoiGianHetHan, CapNhatLanCuoi, MaKH) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                maDonHang,
                maDonHang,
                amount,
                amount,
                status,
                orderTime,
                expiresAt,
                orderTime,
                maKH
        );
    }

    private void insertOrderDetail(String maDonHang, String maSK, String maKhuVuc, int quantity, BigDecimal price) {
        if (!tableExists("DONHANG_CHITIET")) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO DONHANG_CHITIET (MaDonHang, MaSK, MaKhuVuc, SoLuong, DonGia, ThanhTien) VALUES (?, ?, ?, ?, ?, ?)",
                maDonHang,
                maSK,
                maKhuVuc,
                quantity,
                price,
                price.multiply(BigDecimal.valueOf(quantity))
        );
    }

    private void insertEmailLog(String maEmail, String type, String email, String status, Timestamp time, String maVe, String maDonHang) {
        jdbcTemplate.update(
                "INSERT INTO LICHSUGUI_EMAIL (MaEmail, LoaiEmail, DiaChiNhan, TrangThai, SoLanThu, ThoiGianGui, MaVe, MaDonHang) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                maEmail,
                type,
                email,
                status,
                "That_bai".equals(status) ? 2 : 1,
                time,
                maVe,
                maDonHang
        );
    }

    private void insertScanLog(String maLichSu, Timestamp time, String result, String gate, String source, String synced,
                               String maVe, String maNV) {
        Timestamp syncTime = "Y".equals(synced) ? new Timestamp(time.getTime() + 45_000L) : null;
        jdbcTemplate.update(
                "INSERT INTO LICHSUSOATVE (MaLichSu, ThoiGianQuet, KetQuaQuet, CongSoat, NguonDuLieu, DaDongBo, ThoiGianDongBo, MaVe, MaNV) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                maLichSu,
                time,
                result,
                gate,
                source,
                synced,
                syncTime,
                maVe,
                maNV
        );
    }

    private Map<String, String> loadCustomerEmails() {
        Map<String, String> result = new HashMap<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT kh.MaKH, nd.Email FROM KHACHHANG kh JOIN NGUOIDUNG nd ON nd.MaND = kh.MaND"
        );
        for (Map<String, Object> row : rows) {
            result.put(String.valueOf(getRowValue(row, "MaKH", "MAKH")), String.valueOf(getRowValue(row, "Email", "EMAIL")));
        }
        return result;
    }

    private Map<String, Integer> loadExistingPaidTicketLimits() {
        Map<String, Integer> result = new HashMap<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT dh.MaKH, v.MaSK, COALESCE(v.MaKhuVuc, g.MaKhuVuc) AS MaKhuVuc, COUNT(*) AS SoLuong " +
                        "FROM VE v JOIN DONHANG dh ON dh.MaDonHang = v.MaDonHang " +
                        "LEFT JOIN GHENGOI g ON g.MaGhe = v.MaGhe " +
                        "WHERE dh.TrangThaiDonHang = ? " +
                        "GROUP BY dh.MaKH, v.MaSK, COALESCE(v.MaKhuVuc, g.MaKhuVuc)",
                OrderStatus.DA_THANH_TOAN
        );
        for (Map<String, Object> row : rows) {
            String maKH = String.valueOf(getRowValue(row, "MaKH", "MAKH"));
            String maSK = String.valueOf(getRowValue(row, "MaSK", "MASK"));
            String maKhuVuc = String.valueOf(getRowValue(row, "MaKhuVuc", "MAKHUVUC"));
            result.put(limitKey(maKH, maSK, maKhuVuc), numberValue(getRowValue(row, "SoLuong", "SOLUONG")));
        }
        return result;
    }

    private void incrementCustomerZoneCount(Map<String, Integer> counts, String maKH, String maSK, String maKhuVuc, int quantity) {
        String key = limitKey(maKH, maSK, maKhuVuc);
        counts.put(key, counts.getOrDefault(key, 0) + quantity);
    }

    private String limitKey(String maKH, String maSK, String maKhuVuc) {
        return maKH + "|" + maSK + "|" + maKhuVuc;
    }

    private Timestamp randomOrderTime(EventInventory event, Random random, boolean pending) {
        if (pending) {
            return new Timestamp(System.currentTimeMillis() - random.nextInt(8 * 60 * 1000));
        }
        long nowMillis = System.currentTimeMillis();
        long saleStart = event.saleStart() != null ? event.saleStart().getTime() : nowMillis - 60L * 24L * 60L * 60L * 1000L;
        long saleEnd = event.saleEnd() != null ? event.saleEnd().getTime() : nowMillis;
        long upper = Math.min(saleEnd, nowMillis - 60L * 60L * 1000L);
        if (upper <= saleStart) {
            saleStart = Math.max(0, nowMillis - 9L * 24L * 60L * 60L * 1000L);
            upper = nowMillis - 60L * 60L * 1000L;
        }
        long span = Math.max(1, upper - saleStart);
        long jitter = Math.floorMod(random.nextLong(), span);
        return DateTimeUtils.truncateToMinute(new Timestamp(saleStart + jitter));
    }

    private Timestamp randomScanTime(Map<String, Object> ticket, Random random) {
        Timestamp start = (Timestamp) getRowValue(ticket, "ThoiGianBatDau", "THOIGIANBATDAU");
        Timestamp end = (Timestamp) getRowValue(ticket, "ThoiGianKetThuc", "THOIGIANKETTHUC");
        if (start == null || end == null || end.before(start)) {
            return randomRecentTimestamp(25, random);
        }
        long span = Math.max(1, end.getTime() - start.getTime());
        return DateTimeUtils.truncateToMinute(new Timestamp(start.getTime() + Math.floorMod(random.nextLong(), span)));
    }

    private Timestamp randomRecentTimestamp(int maxDays, Random random) {
        long nowMillis = System.currentTimeMillis();
        long minAge = 30L * 60L * 1000L;
        long maxAge = Math.max(1, maxDays) * 24L * 60L * 60L * 1000L;
        long age = minAge + Math.floorMod(random.nextLong(), maxAge);
        return DateTimeUtils.truncateToMinute(new Timestamp(nowMillis - age));
    }

    private String randomPaymentMethod(Random random) {
        String[] methods = {"Chuyển khoản", "Thẻ tín dụng", "Ví điện tử"};
        return methods[random.nextInt(methods.length)];
    }

    private String randomGate(Random random) {
        String[] gates = {"Cổng A", "Cổng B", "Cổng VIP", "Cổng GA"};
        return gates[random.nextInt(gates.length)];
    }

    private String randomSource(Random random) {
        return random.nextInt(100) < 72 ? "Online" : "Offline";
    }

    private String randomSyncFlag(Random random) {
        return random.nextInt(100) < 88 ? "Y" : "N";
    }

    private String randomStaff(List<String> staffIds, Random random) {
        return staffIds.get(random.nextInt(staffIds.size()));
    }

    private String demoOrderId(int sequence) {
        return String.format("DEMO_DH_%04d", sequence);
    }

    private int extractDemoSequence(String maDonHang) {
        try {
            return Integer.parseInt(maDonHang.substring("DEMO_DH_".length()));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private int demoEventWeight(int zeroBasedIndex) {
        if (zeroBasedIndex < 4) {
            return 18;
        }
        if (zeroBasedIndex < 12) {
            return 9;
        }
        return 3;
    }

    private long queryLong(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private int numberValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Object getRowValue(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            for (String key : keys) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private String findMaNDByUsername(String username) {
        List<String> ids = jdbcTemplate.queryForList(
                "SELECT MaND FROM NGUOIDUNG WHERE LOWER(TenTaiKhoan) = LOWER(?)",
                String.class,
                username
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    private List<String> findEmployeeIdsByRole(String roleCode) {
        if (!tableExists("NHANVIEN") || !tableExists("CHITIETVAITRO")) {
            return List.of();
        }
        return jdbcTemplate.queryForList(
                "SELECT nv.MaNV FROM NHANVIEN nv JOIN CHITIETVAITRO ct ON ct.MaND = nv.MaND " +
                        "WHERE ct.MaVaiTro = ? ORDER BY nv.MaNV",
                String.class,
                roleCode
        );
    }

    private boolean emailAvailableForUser(String email, String currentMaND) {
        if (email == null || email.isBlank()) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM NGUOIDUNG WHERE LOWER(Email) = LOWER(?) AND (? IS NULL OR MaND <> ?)",
                Integer.class,
                email,
                currentMaND,
                currentMaND
        );
        return count == null || count == 0;
    }

    private boolean recordExists(String tableName, String whereSql, Object... args) {
        return countWhere(tableName, whereSql, args) > 0;
    }

    private int countWhere(String tableName, String whereSql, Object... args) {
        if (!tableExists(tableName)) {
            return 0;
        }
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName + " WHERE " + whereSql, Integer.class, args);
        return count == null ? 0 : count;
    }

    private void deleteIfTableExists(String tableName) {
        if (!tableExists(tableName)) {
            return;
        }
        jdbcTemplate.update("DELETE FROM " + tableName);
    }

    private void deleteWhereIfTableExists(String tableName, String whereSql, Object... args) {
        if (!tableExists(tableName)) {
            return;
        }
        jdbcTemplate.update("DELETE FROM " + tableName + " WHERE " + whereSql, args);
    }

    private void updateIfTableExists(String tableName, String sql) {
        if (!tableExists(tableName)) {
            return;
        }
        jdbcTemplate.update(sql);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER(?)",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER(?) AND UPPER(column_name) = UPPER(?)",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private boolean isIdAvailable(String tableName, String columnName, String id) {
        if (id == null || id.isBlank() || !tableExists(tableName)) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?",
                Integer.class,
                id
        );
        return count == null || count == 0;
    }

    private Timestamp now() {
        return DateTimeUtils.truncateToMinute(new Timestamp(System.currentTimeMillis()));
    }

    private String demoPasswordHash() {
        if (demoPasswordHash == null) {
            demoPasswordHash = passwordEncoder.encode("123456");
        }
        return demoPasswordHash;
    }

    private static final class EventInventory {
        private final String maSK;
        private final String tenSK;
        private final String trangThaiSK;
        private final String loaiSoDo;
        private final Timestamp eventStart;
        private final Timestamp eventEnd;
        private final Timestamp saleStart;
        private final Timestamp saleEnd;
        private final int weight;
        private final List<ZoneInventory> zones = new ArrayList<>();

        private EventInventory(String maSK, String tenSK, String trangThaiSK, String loaiSoDo,
                               Timestamp eventStart, Timestamp eventEnd, Timestamp saleStart, Timestamp saleEnd, int weight) {
            this.maSK = maSK;
            this.tenSK = tenSK;
            this.trangThaiSK = trangThaiSK;
            this.loaiSoDo = loaiSoDo == null || loaiSoDo.isBlank() ? "NGOI_THEO_GHE" : loaiSoDo;
            this.eventStart = eventStart;
            this.eventEnd = eventEnd;
            this.saleStart = saleStart;
            this.saleEnd = saleEnd;
            this.weight = weight;
        }

        private String maSK() {
            return maSK;
        }

        private String trangThaiSK() {
            return trangThaiSK;
        }

        private String loaiSoDo() {
            return loaiSoDo;
        }

        private Timestamp saleStart() {
            return saleStart;
        }

        private Timestamp saleEnd() {
            return saleEnd;
        }

        private int weight() {
            return weight;
        }

        private List<ZoneInventory> zones() {
            return zones;
        }

        private boolean hasAvailability() {
            return zones.stream().anyMatch(zone -> zone.hasAvailability(loaiSoDo));
        }

        private boolean hasStandingAvailability() {
            return "DUNG_THEO_KHU".equalsIgnoreCase(loaiSoDo)
                    && zones.stream().anyMatch(zone -> zone.remainingStanding() > 0);
        }
    }

    private static final class ZoneInventory {
        private final String maKhuVuc;
        private final BigDecimal giaVe;
        private final int capacity;
        private int remainingStanding;
        private final int maxPerCustomer;
        private final List<String> freeSeats;

        private ZoneInventory(String maKhuVuc, BigDecimal giaVe, int capacity, int remainingStanding,
                              int maxPerCustomer, List<String> freeSeats) {
            this.maKhuVuc = maKhuVuc;
            this.giaVe = giaVe;
            this.capacity = capacity;
            this.remainingStanding = remainingStanding;
            this.maxPerCustomer = maxPerCustomer;
            this.freeSeats = freeSeats;
        }

        private String maKhuVuc() {
            return maKhuVuc;
        }

        private BigDecimal giaVe() {
            return giaVe;
        }

        private int remainingStanding() {
            return remainingStanding;
        }

        private int maxPerCustomer() {
            return maxPerCustomer;
        }

        private List<String> freeSeats() {
            return freeSeats;
        }

        private boolean hasAvailability(String loaiSoDo) {
            if ("NGOI_THEO_GHE".equalsIgnoreCase(loaiSoDo)) {
                return !freeSeats.isEmpty();
            }
            return remainingStanding > 0 && capacity > 0;
        }

        private void reduceStanding(int quantity) {
            remainingStanding = Math.max(0, remainingStanding - quantity);
        }
    }

    private record TicketSelection(
            ZoneInventory zone,
            int quantity,
            List<String> seatIds,
            BigDecimal totalPrice
    ) {
    }

    private static final class PurchaseSeedSummary {
        private int orders;
        private int paidOrders;
        private int cancelledOrders;
        private int pendingOrders;
        private int tickets;
        private int transactions;
        private int emailLogs;
        private int scanLogs;
        private int behaviorLogs;
    }

    private record DemoEventSeed(
            String maSK,
            String tenSK,
            String loaiSuKien,
            String loaiSoDo,
            String salePhase,
            String tenDiaDiem,
            String diaChi,
            String thanhPho,
            int sucChua,
            String tags
    ) {
    }

    private record EventTimes(
            LocalDateTime eventStart,
            LocalDateTime eventEnd,
            LocalDateTime saleStart,
            LocalDateTime saleEnd
    ) {
    }
}
