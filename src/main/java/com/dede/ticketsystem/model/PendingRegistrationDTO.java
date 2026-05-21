package com.dede.ticketsystem.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PendingRegistrationDTO implements Serializable {

    private String tenTaiKhoan;
    private String matKhauMaHoa;
    private String email;
    private String sdt;
    private String gioiTinh;
    private String hoTenKH;
    private String otp;
    private LocalDateTime otpExpireAt;
    private Integer otpAttempts;
    private LocalDateTime lastOtpSentAt;

    public PendingRegistrationDTO() {
    }

    public String getTenTaiKhoan() {
        return tenTaiKhoan;
    }

    public void setTenTaiKhoan(String tenTaiKhoan) {
        this.tenTaiKhoan = tenTaiKhoan;
    }

    public String getMatKhauMaHoa() {
        return matKhauMaHoa;
    }

    public void setMatKhauMaHoa(String matKhauMaHoa) {
        this.matKhauMaHoa = matKhauMaHoa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getHoTenKH() {
        return hoTenKH;
    }

    public void setHoTenKH(String hoTenKH) {
        this.hoTenKH = hoTenKH;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getOtpExpireAt() {
        return otpExpireAt;
    }

    public void setOtpExpireAt(LocalDateTime otpExpireAt) {
        this.otpExpireAt = otpExpireAt;
    }

    public Integer getOtpAttempts() {
        return otpAttempts;
    }

    public void setOtpAttempts(Integer otpAttempts) {
        this.otpAttempts = otpAttempts;
    }

    public LocalDateTime getLastOtpSentAt() {
        return lastOtpSentAt;
    }

    public void setLastOtpSentAt(LocalDateTime lastOtpSentAt) {
        this.lastOtpSentAt = lastOtpSentAt;
    }
}
