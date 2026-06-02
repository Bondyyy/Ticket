package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.NguoiDung;
import com.dede.ticketsystem.model.PendingRegistrationDTO;
import com.dede.ticketsystem.repository.KhachHangRepository;
import com.dede.ticketsystem.service.AuthService;
import com.dede.ticketsystem.service.ActiveSessionRegistry;
import com.dede.ticketsystem.service.OtpMailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    private static final String PENDING_REGISTRATION_SESSION_KEY = "pendingRegistration";

    @Autowired
    private AuthService authService;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private OtpMailService otpMailService;

    @Autowired
    private ActiveSessionRegistry activeSessionRegistry;

    @Value("${app.mail.otp.expire-minutes:5}")
    private long otpExpireMinutes;

    @Value("${app.mail.otp.max-attempts:5}")
    private int otpMaxAttempts;

    @GetMapping("/dang-nhap")
    public String showLogin(
            @RequestParam(required = false) String redirect,
            @RequestParam(required = false) String redirectUrl,
            @RequestParam(required = false) String expired,
            HttpSession session,
            Model model) {
        if (session.getAttribute("nguoiDung") != null) {
            return "redirect:/";
        }
        String target = (redirect != null && !redirect.isBlank()) ? redirect : redirectUrl;
        model.addAttribute("redirectUrl", target);
        if ("other-device".equals(expired)) {
            model.addAttribute("error", "Tài khoản đã đăng nhập ở thiết bị khác. Phiên hiện tại đã hết hạn.");
        }
        model.addAttribute("showNavbar", false);
        return "auth/login";
    }

    @PostMapping("/dang-nhap")
    public String processLogin(
            @RequestParam String tenTaiKhoan,
            @RequestParam String matKhau,
            @RequestParam(required = false) String redirect,
            @RequestParam(required = false) String redirectUrl,
            HttpSession session,
            Model model) {
        try {
            NguoiDung nguoiDung = authService.dangNhap(tenTaiKhoan, matKhau);

            // Lưu vào HttpSession
            session.setAttribute("nguoiDung", nguoiDung);
            session.setAttribute("maND", nguoiDung.getMaND());
            session.setAttribute("tenTaiKhoan", nguoiDung.getTenTaiKhoan());

            // Lấy danh sách role của user dạng Set<String>
            Set<String> roles = nguoiDung.getChiTietVaiTros().stream()
                    .map(ct -> ct.getMaVaiTro())
                    .collect(Collectors.toSet());
            session.setAttribute("roles", roles);

            if (roles.contains("ADMIN")) {
                activeSessionRegistry.registerSingleSession(nguoiDung.getMaND(), session);
            }

            // Nếu user là CUSTOMER thì lấy maKH
            if (roles.contains("CUSTOMER")) {
                khachHangRepository.findByMaND(nguoiDung.getMaND())
                        .ifPresent(kh -> session.setAttribute("maKH", kh.getMaKH()));
            }

            session.setMaxInactiveInterval(30 * 60);

            // Hỗ trợ redirect sau đăng nhập
            String targetRedirect = (redirect != null && !redirect.isBlank()) ? redirect : redirectUrl;
            if (isValidLocalRedirect(targetRedirect)) {
                return "redirect:" + targetRedirect;
            }

            // Redirect mặc định dựa trên role
            if (roles.contains("ADMIN") || roles.contains("ORGANIZER")) {
                return "redirect:/sukien";
            } else if (roles.contains("STAFF")) {
                return "redirect:/soat-ve";
            } else {
                return "redirect:/";
            }

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tenTaiKhoanCu", tenTaiKhoan);
            String targetRedirect = (redirect != null && !redirect.isBlank()) ? redirect : redirectUrl;
            model.addAttribute("redirectUrl", targetRedirect);
            model.addAttribute("showNavbar", false);
            return "auth/login";
        }
    }

    @GetMapping("/dang-ky")
    public String showRegister(HttpSession session, Model model) {
        if (session.getAttribute("nguoiDung") != null) {
            return "redirect:/";
        }
        model.addAttribute("showNavbar", false);
        return "auth/register";
    }

    @PostMapping({"/dang-ky", "/dang-ky/send-otp"})
    public String sendRegistrationOtp(
            @RequestParam String tenTaiKhoan,
            @RequestParam String email,
            @RequestParam String sdt,
            @RequestParam String matKhau,
            @RequestParam(required = false) String xacNhanMatKhau,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            if (xacNhanMatKhau != null && !xacNhanMatKhau.isBlank() && !matKhau.equals(xacNhanMatKhau)) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp.");
            }

            authService.validateDangKyCustomer(tenTaiKhoan, email, sdt, matKhau);

            String otp = otpMailService.generateOtp();
            boolean sent = otpMailService.sendOtpEmail(email.trim(), otp);
            if (!sent) {
                throw new RuntimeException("Không gửi được OTP. Vui lòng thử lại sau.");
            }

            PendingRegistrationDTO pending = new PendingRegistrationDTO();
            pending.setTenTaiKhoan(tenTaiKhoan.trim());
            pending.setEmail(email.trim());
            pending.setSdt(sdt != null ? sdt.trim() : null);
            pending.setMatKhauMaHoa(authService.encodePassword(matKhau));
            pending.setHoTenKH(tenTaiKhoan.trim());
            pending.setOtp(otp);
            pending.setOtpExpireAt(LocalDateTime.now().plusMinutes(otpExpireMinutes));
            pending.setOtpAttempts(0);
            pending.setLastOtpSentAt(LocalDateTime.now());
            session.setAttribute(PENDING_REGISTRATION_SESSION_KEY, pending);

            redirectAttributes.addFlashAttribute("success", "Mã OTP đã được gửi tới email " + email.trim() + ".");
            return "redirect:/dang-ky/verify";
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.contains("JDBC exception") || errorMessage.contains("SQL")) {
                errorMessage = "Lỗi hệ thống khi đăng ký. Vui lòng thử lại sau.";
            }
            model.addAttribute("error", errorMessage);
            model.addAttribute("tenTaiKhoan", tenTaiKhoan);
            model.addAttribute("email", email);
            model.addAttribute("sdt", sdt);
            model.addAttribute("showNavbar", false);
            return "auth/register";
        }
    }

    @GetMapping("/dang-ky/verify")
    public String showVerifyOtp(HttpSession session, Model model) {
        PendingRegistrationDTO pending = getPendingRegistration(session);
        if (pending == null) {
            return "redirect:/dang-ky";
        }
        populateOtpModel(model, pending);
        return "auth/register";
    }

    @PostMapping("/dang-ky/verify")
    public String verifyRegistrationOtp(
            @RequestParam String otp,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        PendingRegistrationDTO pending = getPendingRegistration(session);
        if (pending == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/dang-ky";
        }

        int attempts = pending.getOtpAttempts() != null ? pending.getOtpAttempts() : 0;
        if (attempts >= otpMaxAttempts) {
            session.removeAttribute(PENDING_REGISTRATION_SESSION_KEY);
            model.addAttribute("error", "Bạn đã nhập sai OTP quá số lần cho phép. Vui lòng đăng ký lại.");
            model.addAttribute("showNavbar", false);
            return "auth/register";
        }

        if (pending.getOtpExpireAt() == null || LocalDateTime.now().isAfter(pending.getOtpExpireAt())) {
            populateOtpModel(model, pending);
            model.addAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại OTP.");
            return "auth/register";
        }

        if (otp == null || !pending.getOtp().equals(otp.trim())) {
            pending.setOtpAttempts(attempts + 1);
            session.setAttribute(PENDING_REGISTRATION_SESSION_KEY, pending);
            populateOtpModel(model, pending);
            model.addAttribute("error", "Mã OTP không đúng. Bạn còn " + Math.max(0, otpMaxAttempts - pending.getOtpAttempts()) + " lần thử.");
            return "auth/register";
        }

        try {
            authService.dangKyDaXacThuc(pending);
            session.removeAttribute(PENDING_REGISTRATION_SESSION_KEY);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/dang-nhap?registered=success";
        } catch (Exception e) {
            session.removeAttribute(PENDING_REGISTRATION_SESSION_KEY);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("showNavbar", false);
            return "auth/register";
        }
    }

    @PostMapping("/dang-ky/resend-otp")
    public String resendRegistrationOtp(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        PendingRegistrationDTO pending = getPendingRegistration(session);
        if (pending == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/dang-ky";
        }

        LocalDateTime now = LocalDateTime.now();
        if (pending.getLastOtpSentAt() != null && now.isBefore(pending.getLastOtpSentAt().plusSeconds(60))) {
            populateOtpModel(model, pending);
            model.addAttribute("error", "Vui lòng chờ 60 giây trước khi gửi lại OTP.");
            return "auth/register";
        }

        try {
            String otp = otpMailService.generateOtp();
            boolean sent = otpMailService.sendOtpEmail(pending.getEmail(), otp);
            if (!sent) {
                throw new RuntimeException("Không gửi được OTP. Vui lòng thử lại sau.");
            }
            pending.setOtp(otp);
            pending.setOtpExpireAt(now.plusMinutes(otpExpireMinutes));
            pending.setOtpAttempts(0);
            pending.setLastOtpSentAt(now);
            session.setAttribute(PENDING_REGISTRATION_SESSION_KEY, pending);
            populateOtpModel(model, pending);
            model.addAttribute("success", "Mã OTP mới đã được gửi tới email " + pending.getEmail() + ".");
            return "auth/register";
        } catch (Exception e) {
            populateOtpModel(model, pending);
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/dang-xuat")
    public String logoutGet(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đã đăng xuất thành công.");
        return "redirect:/";
    }

    @PostMapping("/dang-xuat")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đã đăng xuất thành công.");
        return "redirect:/dang-nhap";
    }

    private boolean isValidLocalRedirect(String path) {
        return path != null && path.startsWith("/") && !path.startsWith("//");
    }

    private PendingRegistrationDTO getPendingRegistration(HttpSession session) {
        Object pending = session.getAttribute(PENDING_REGISTRATION_SESSION_KEY);
        return pending instanceof PendingRegistrationDTO ? (PendingRegistrationDTO) pending : null;
    }

    private void populateOtpModel(Model model, PendingRegistrationDTO pending) {
        model.addAttribute("otpStep", true);
        model.addAttribute("pendingEmail", pending.getEmail());
        model.addAttribute("otpExpireAt", pending.getOtpExpireAt());
        model.addAttribute("otpMaxAttempts", otpMaxAttempts);
        model.addAttribute("showNavbar", false);
    }
}
