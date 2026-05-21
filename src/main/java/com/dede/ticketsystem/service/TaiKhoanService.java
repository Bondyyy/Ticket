package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.*;
import com.dede.ticketsystem.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaiKhoanService {

    private static final Set<String> SYSTEM_ROLE_CODES = Set.of("ADMIN", "CUSTOMER", "STAFF", "ORGANIZER");
    private static final List<String> USER_FORM_ROLE_ORDER = List.of("ADMIN", "CUSTOMER", "STAFF", "ORGANIZER");
    private static final java.util.regex.Pattern ROLE_CODE_PATTERN = java.util.regex.Pattern.compile("^[A-Z0-9_]+$");

    private static final Set<String> TRANG_THAI_NGUOI_DUNG_HOP_LE = Set.of(
            "Đang hoạt động",
            "Không hoạt động",
            "Bị khóa"
    );

    private final NguoiDungRepository nguoiDungRepo;
    private final VaiTroRepository vaiTroRepo;
    private final ChiTietVaiTroRepository chiTietVaiTroRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final IdGeneratorService idGeneratorService;

    // Tự code Constructor thủ công thay cho @RequiredArgsConstructor của Lombok
    public TaiKhoanService(NguoiDungRepository nguoiDungRepo, 
                           VaiTroRepository vaiTroRepo, 
                           ChiTietVaiTroRepository chiTietVaiTroRepo, 
                           BCryptPasswordEncoder passwordEncoder,
                           IdGeneratorService idGeneratorService) {
        this.nguoiDungRepo = nguoiDungRepo;
        this.vaiTroRepo = vaiTroRepo;
        this.chiTietVaiTroRepo = chiTietVaiTroRepo;
        this.passwordEncoder = passwordEncoder;
        this.idGeneratorService = idGeneratorService;
    }

    public List<NguoiDung> getDanhSachTatCa() {
        return nguoiDungRepo.findAll();
    }

    public List<NguoiDung> timKiem(String keyword) {
        if (keyword == null || keyword.isBlank()) return getDanhSachTatCa();
        return nguoiDungRepo.timKiemTheoTenHoacEmail(keyword.trim());
    }

    public List<NguoiDung> locTheoTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return getDanhSachTatCa();
        return nguoiDungRepo.findByTrangThaiND(trangThai);
    }

    public List<VaiTro> getDanhSachVaiTro() {
        Map<String, VaiTro> byCode = vaiTroRepo.findAll().stream()
                .filter(role -> SYSTEM_ROLE_CODES.contains(role.getMaVaiTro()))
                .collect(Collectors.toMap(VaiTro::getMaVaiTro, role -> role, (left, right) -> left));
        return USER_FORM_ROLE_ORDER.stream()
                .map(byCode::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<NguoiDung> getDanhSachNguoiDung() {
        return nguoiDungRepo.findAll();
    }

    public List<VaiTroQuanLyDTO> getDanhSachVaiTroQuanLy() {
        return vaiTroRepo.findAll().stream()
                .sorted(Comparator.comparing(VaiTro::getMaVaiTro, String.CASE_INSENSITIVE_ORDER))
                .map(this::toVaiTroQuanLyDTO)
                .collect(Collectors.toList());
    }

    public Optional<VaiTro> timVaiTro(String maVaiTro) {
        return vaiTroRepo.findById(normalizeRoleCode(maVaiTro));
    }

    public List<NguoiDung> getNguoiDungCoVaiTro(String maVaiTro) {
        String code = normalizeRoleCode(maVaiTro);
        return chiTietVaiTroRepo.findByMaVaiTro(code).stream()
                .map(ChiTietVaiTro::getMaND)
                .map(nguoiDungRepo::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(NguoiDung::getTenTaiKhoan, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Optional<NguoiDung> timTheoMa(String maND) {
        return nguoiDungRepo.findById(maND);
    }

    @Transactional
    public NguoiDung taoTaiKhoan(TaiKhoanDTO dto) {
        validateTaoMoi(dto);

        String maND = idGeneratorService.nextNguoiDungId();

        NguoiDung nd = NguoiDung.builder()
                .maND(maND)
                .tenTaiKhoan(dto.getTenTaiKhoan().trim())
                .matKhauMaHoa(passwordEncoder.encode(dto.getMatKhau()))
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .sdt(dto.getSdt())
                .gioiTinh(dto.getGioiTinh())
                .ngaySinh(dto.getNgaySinh() != null ? new Date(dto.getNgaySinh().getTime()) : null)
                .trangThaiND(normalizeTrangThai(dto.getTrangThaiND()))
                .thoiGianTao(Timestamp.valueOf(LocalDateTime.now()))
                .capNhatLanCuoi(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        nguoiDungRepo.save(nd);

        if (dto.getDanhSachVaiTro() != null && !dto.getDanhSachVaiTro().isEmpty()) {
            ganVaiTro(maND, dto.getDanhSachVaiTro());
        }

        return nd;
    }


    @Transactional
    public NguoiDung capNhatTaiKhoan(String maND, TaiKhoanDTO dto) {
        NguoiDung nd = nguoiDungRepo.findById(maND)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + maND));

        if (!nd.getTenTaiKhoan().equals(dto.getTenTaiKhoan()) &&
            nguoiDungRepo.existsByTenTaiKhoan(dto.getTenTaiKhoan())) {
            throw new RuntimeException("Tên tài khoản đã tồn tại!");
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(nd.getEmail()) &&
            nguoiDungRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        nd.setTenTaiKhoan(dto.getTenTaiKhoan().trim());
        nd.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        nd.setSdt(dto.getSdt());
        nd.setGioiTinh(dto.getGioiTinh());
        nd.setNgaySinh(dto.getNgaySinh() != null ? new Date(dto.getNgaySinh().getTime()) : null);
        nd.setTrangThaiND(normalizeTrangThai(dto.getTrangThaiND()));
        nd.setCapNhatLanCuoi(Timestamp.valueOf(LocalDateTime.now()));

        if (dto.getMatKhau() != null && !dto.getMatKhau().isBlank()) {
            nd.setMatKhauMaHoa(passwordEncoder.encode(dto.getMatKhau()));
        }

        nguoiDungRepo.save(nd);

        for (String systemRole : SYSTEM_ROLE_CODES) {
            chiTietVaiTroRepo.deleteByMaNDAndMaVaiTro(maND, systemRole);
        }
        if (dto.getDanhSachVaiTro() != null && !dto.getDanhSachVaiTro().isEmpty()) {
            ganVaiTro(maND, dto.getDanhSachVaiTro());
        }

        return nd;
    }

    @Transactional
    public void xoaTaiKhoan(String maND) {
        NguoiDung nd = nguoiDungRepo.findById(maND)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + maND));
        nd.setTrangThaiND("Bị khóa");
        nd.setCapNhatLanCuoi(Timestamp.valueOf(LocalDateTime.now()));
        nguoiDungRepo.save(nd);
    }


    private void ganVaiTro(String maND, List<String> danhSachMaVaiTro) {
        List<ChiTietVaiTro> chiTiets = danhSachMaVaiTro.stream()
                .map(this::normalizeRoleCode)
                .filter(Objects::nonNull)
                .filter(SYSTEM_ROLE_CODES::contains)
                .filter(maVaiTro -> vaiTroRepo.existsById(maVaiTro))
                .filter(maVaiTro -> !chiTietVaiTroRepo.existsByMaNDAndMaVaiTro(maND, maVaiTro))
                .map(maVaiTro -> ChiTietVaiTro.builder()
                        .maND(maND)
                        .maVaiTro(maVaiTro)
                        .build())
                .collect(Collectors.toList());
        chiTietVaiTroRepo.saveAll(chiTiets);
    }

    @Transactional
    public VaiTro taoVaiTro(VaiTro dto) {
        String maVaiTro = validateRolePayload(dto, true);
        VaiTro role = new VaiTro();
        role.setMaVaiTro(maVaiTro);
        role.setTenVaiTro(dto.getTenVaiTro().trim());
        role.setMoTa(trimToNull(dto.getMoTa()));
        return vaiTroRepo.save(role);
    }

    @Transactional
    public VaiTro capNhatVaiTro(String maVaiTro, VaiTro dto) {
        String code = normalizeRoleCode(maVaiTro);
        VaiTro role = vaiTroRepo.findById(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + code));

        if (dto.getTenVaiTro() == null || dto.getTenVaiTro().isBlank()) {
            throw new RuntimeException("Tên vai trò không được để trống.");
        }
        role.setTenVaiTro(dto.getTenVaiTro().trim());
        role.setMoTa(trimToNull(dto.getMoTa()));
        return vaiTroRepo.save(role);
    }

    @Transactional
    public void xoaVaiTro(String maVaiTro) {
        String code = normalizeRoleCode(maVaiTro);
        if (SYSTEM_ROLE_CODES.contains(code)) {
            throw new RuntimeException("Không thể xóa vai trò hệ thống.");
        }
        long assigned = chiTietVaiTroRepo.countByMaVaiTro(code);
        if (assigned > 0) {
            throw new RuntimeException("Không thể xóa vai trò đang được gán cho người dùng.");
        }
        vaiTroRepo.deleteById(code);
    }

    @Transactional
    public void ganVaiTroChoNguoiDung(String maVaiTro, String maND) {
        String code = normalizeRoleCode(maVaiTro);
        String userId = trimToNull(maND);
        if (userId == null) {
            throw new RuntimeException("Vui lòng chọn người dùng.");
        }
        if (!vaiTroRepo.existsById(code)) {
            throw new RuntimeException("Vai trò không tồn tại.");
        }
        if (!nguoiDungRepo.existsById(userId)) {
            throw new RuntimeException("Người dùng không tồn tại.");
        }
        if (chiTietVaiTroRepo.existsByMaNDAndMaVaiTro(userId, code)) {
            return;
        }
        chiTietVaiTroRepo.save(ChiTietVaiTro.builder().maND(userId).maVaiTro(code).build());
    }

    @Transactional
    public void goVaiTroKhoiNguoiDung(String maVaiTro, String maND) {
        String code = normalizeRoleCode(maVaiTro);
        String userId = trimToNull(maND);
        if (userId == null) {
            return;
        }
        if ("ADMIN".equals(code) && chiTietVaiTroRepo.countByMaVaiTro("ADMIN") <= 1
                && chiTietVaiTroRepo.existsByMaNDAndMaVaiTro(userId, "ADMIN")) {
            throw new RuntimeException("Không thể gỡ ADMIN khỏi quản trị viên cuối cùng.");
        }
        chiTietVaiTroRepo.deleteByMaNDAndMaVaiTro(userId, code);
    }

    public String getRoleDisplayName(String maVaiTro) {
        String code = normalizeRoleCode(maVaiTro);
        if ("ADMIN".equals(code)) return "Quản trị viên";
        if ("CUSTOMER".equals(code)) return "Khách hàng";
        if ("STAFF".equals(code)) return "Nhân viên soát vé";
        if ("ORGANIZER".equals(code)) return "Ban tổ chức";
        if (isLegacyRole(code)) return "Vai trò cũ";
        return vaiTroRepo.findById(code)
                .map(VaiTro::getTenVaiTro)
                .filter(name -> name != null && !name.isBlank())
                .orElse(code);
    }

    private void validateTaoMoi(TaiKhoanDTO dto) {
        if (dto.getTenTaiKhoan() == null || dto.getTenTaiKhoan().isBlank()) {
            throw new RuntimeException("Tên tài khoản không được để trống!");
        }
        if (nguoiDungRepo.existsByTenTaiKhoan(dto.getTenTaiKhoan().trim())) {
            throw new RuntimeException("Tên tài khoản đã tồn tại!");
        }
        if (dto.getMatKhau() == null || dto.getMatKhau().isBlank()) {
            throw new RuntimeException("Mật khẩu không được để trống!");
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() &&
            nguoiDungRepo.existsByEmail(dto.getEmail().trim())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
    }

    private VaiTroQuanLyDTO toVaiTroQuanLyDTO(VaiTro role) {
        VaiTroQuanLyDTO dto = new VaiTroQuanLyDTO();
        dto.setMaVaiTro(role.getMaVaiTro());
        dto.setTenVaiTro(getRoleDisplayName(role.getMaVaiTro()));
        dto.setMoTa(role.getMoTa());
        dto.setSoNguoiDung(chiTietVaiTroRepo.countByMaVaiTro(role.getMaVaiTro()));
        dto.setSystemRole(SYSTEM_ROLE_CODES.contains(role.getMaVaiTro()));
        dto.setLegacyRole(isLegacyRole(role.getMaVaiTro()));
        dto.setLoaiVaiTro(dto.isSystemRole() ? "Hệ thống" : (dto.isLegacyRole() ? "Legacy" : "Tùy chỉnh"));
        return dto;
    }

    private String validateRolePayload(VaiTro dto, boolean creating) {
        String maVaiTro = normalizeRoleCode(dto.getMaVaiTro());
        if (maVaiTro == null) {
            if (creating) {
                maVaiTro = idGeneratorService.nextVaiTroCustomId();
            } else {
                throw new RuntimeException("Mã vai trò không được để trống.");
            }
        }
        if (!ROLE_CODE_PATTERN.matcher(maVaiTro).matches()) {
            throw new RuntimeException("Mã vai trò chỉ được dùng chữ in hoa, số và dấu gạch dưới.");
        }
        if (dto.getTenVaiTro() == null || dto.getTenVaiTro().isBlank()) {
            throw new RuntimeException("Tên vai trò không được để trống.");
        }
        if (creating && vaiTroRepo.existsById(maVaiTro)) {
            throw new RuntimeException("Mã vai trò đã tồn tại.");
        }
        return maVaiTro;
    }

    private boolean isLegacyRole(String maVaiTro) {
        return maVaiTro != null && maVaiTro.matches("^VT\\d+$");
    }

    private String normalizeRoleCode(String maVaiTro) {
        String clean = trimToNull(maVaiTro);
        return clean == null ? null : clean.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }

    private String normalizeTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) {
            return "Đang hoạt động";
        }
        String clean = trangThai.trim();
        if ("Hoạt động".equalsIgnoreCase(clean) || "Active".equalsIgnoreCase(clean) || "Hoat_dong".equalsIgnoreCase(clean)) {
            return "Đang hoạt động";
        }
        if ("Inactive".equalsIgnoreCase(clean)) {
            return "Không hoạt động";
        }
        if ("Locked".equalsIgnoreCase(clean)) {
            return "Bị khóa";
        }
        if (!TRANG_THAI_NGUOI_DUNG_HOP_LE.contains(clean)) {
            throw new RuntimeException("Trạng thái tài khoản không hợp lệ.");
        }
        return clean;
    }
}
