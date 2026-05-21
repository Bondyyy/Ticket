package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.NguoiDung;
import com.dede.ticketsystem.model.KhachHang;
import com.dede.ticketsystem.model.PendingRegistrationDTO;
import com.dede.ticketsystem.model.VaiTro;
import com.dede.ticketsystem.model.ChiTietVaiTro;
import com.dede.ticketsystem.repository.NguoiDungRepository;
import com.dede.ticketsystem.repository.KhachHangRepository;
import com.dede.ticketsystem.repository.VaiTroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Transactional
    public NguoiDung dangNhap(String tenTaiKhoan, String matKhau) {
        NguoiDung nguoiDung = nguoiDungRepository
                .findByTenTaiKhoan(tenTaiKhoan)
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập không tồn tại."));

        if ("Bị khóa".equals(nguoiDung.getTrangThaiND())) {
            throw new RuntimeException("Tài khoản đã bị khóa. Vui lòng liên hệ hỗ trợ.");
        }

        if (!passwordEncoder.matches(matKhau, nguoiDung.getMatKhauMaHoa())) {
            throw new RuntimeException("Mật khẩu không chính xác.");
        }

        // Cập nhật LanCuoiDangNhap = now
        nguoiDung.setLanCuoiDangNhap(new Timestamp(System.currentTimeMillis()));
        return nguoiDungRepository.save(nguoiDung);
    }

    @Transactional
    public void dangKy(String tenTaiKhoan, String email, String sdt, String matKhau) {
        validateDangKyCustomer(tenTaiKhoan, email, sdt, matKhau);
        createCustomerAccount(tenTaiKhoan.trim(), email.trim(), normalizeBlank(sdt), passwordEncoder.encode(matKhau), tenTaiKhoan.trim());
    }

    public void validateDangKyCustomer(String tenTaiKhoan, String email, String sdt, String matKhau) {
        String cleanUsername = normalizeBlank(tenTaiKhoan);
        String cleanEmail = normalizeBlank(email);

        if (cleanUsername == null) {
            throw new RuntimeException("Tên đăng nhập không được để trống.");
        }
        if (cleanEmail == null) {
            throw new RuntimeException("Email không được để trống.");
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new RuntimeException("Email không hợp lệ.");
        }
        if (matKhau == null || matKhau.isBlank()) {
            throw new RuntimeException("Mật khẩu không được để trống.");
        }
        if (nguoiDungRepository.findByTenTaiKhoan(cleanUsername).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }
        if (nguoiDungRepository.findByEmail(cleanEmail).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng.");
        }
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public void dangKyDaXacThuc(PendingRegistrationDTO pending) {
        if (pending == null) {
            throw new RuntimeException("Phiên đăng ký không tồn tại.");
        }
        String cleanUsername = normalizeBlank(pending.getTenTaiKhoan());
        String cleanEmail = normalizeBlank(pending.getEmail());

        if (cleanUsername == null || cleanEmail == null || pending.getMatKhauMaHoa() == null || pending.getMatKhauMaHoa().isBlank()) {
            throw new RuntimeException("Thông tin đăng ký không hợp lệ.");
        }
        if (nguoiDungRepository.findByTenTaiKhoan(cleanUsername).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        }
        if (nguoiDungRepository.findByEmail(cleanEmail).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng.");
        }

        String hoTenKH = normalizeBlank(pending.getHoTenKH());
        createCustomerAccount(cleanUsername, cleanEmail, normalizeBlank(pending.getSdt()), pending.getMatKhauMaHoa(),
                hoTenKH != null ? hoTenKH : cleanUsername);
    }

    private void createCustomerAccount(String tenTaiKhoan, String email, String sdt, String matKhauMaHoa, String hoTenKH) {
        String newMaND = idGeneratorService.nextNguoiDungId();
        NguoiDung newUser = NguoiDung.builder()
                .maND(newMaND)
                .tenTaiKhoan(tenTaiKhoan)
                .email(email)
                .sdt(sdt)
                .matKhauMaHoa(matKhauMaHoa)
                .trangThaiND("Đang hoạt động")
                .thoiGianTao(new Timestamp(System.currentTimeMillis()))
                .chiTietVaiTros(new ArrayList<>())
                .build();
        
        // 2. Gán role CUSTOMER
        // Đảm bảo role CUSTOMER đã tồn tại
        VaiTro customerRole = vaiTroRepository.findById("CUSTOMER")
                .orElseGet(() -> {
                    VaiTro r = VaiTro.builder().maVaiTro("CUSTOMER").tenVaiTro("CUSTOMER").moTa("Khách hàng").build();
                    return vaiTroRepository.save(r);
                });
                
        ChiTietVaiTro chiTiet = new ChiTietVaiTro();
        chiTiet.setNguoiDung(newUser);
        chiTiet.setVaiTro(customerRole);
        chiTiet.setMaND(newUser.getMaND());
        chiTiet.setMaVaiTro("CUSTOMER");
        
        newUser.getChiTietVaiTros().add(chiTiet);
        
        // Lưu NGUOIDUNG
        NguoiDung savedUser = nguoiDungRepository.save(newUser);
        
        // 3. Tạo KHACHHANG tương ứng
        String newMaKH = idGeneratorService.nextKhachHangId();
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKH(newMaKH);
        khachHang.setHoTenKH(hoTenKH);
        khachHang.setTongChiTieu(BigDecimal.ZERO);
        khachHang.setCapNhatLanCuoi(new Timestamp(System.currentTimeMillis()));
        khachHang.setNguoiDung(savedUser);
        
        khachHangRepository.save(khachHang);
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }
}
