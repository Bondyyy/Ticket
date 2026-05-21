package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "KHACHHANG")
public class KhachHang {

    @Id
    @Column(name = "MAKH", length = 50)
    private String maKH;

    @Column(name = "HOTENKH", length = 100)
    private String hoTenKH;

    @Column(name = "TONGCHITIEU")
    private BigDecimal tongChiTieu;

    @Column(name = "CAPNHATLANCUOI")
    private Timestamp capNhatLanCuoi;

    @Column(name = "MAHANGTHANHVIEN", length = 50)
    private String maHangThanhVien;

    @OneToOne
    @JoinColumn(name = "MAND")
    private NguoiDung nguoiDung;

    // Các field cũ không có trong DB gốc, đánh dấu @Transient để tránh lỗi compile nếu UI dùng
    @Transient
    private String hoTen;

    @Transient
    private String sdt;

    @Transient
    private String email;

    public KhachHang() {
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getHoTenKH() {
        return hoTenKH;
    }

    public void setHoTenKH(String hoTenKH) {
        this.hoTenKH = hoTenKH;
        this.hoTen = hoTenKH; // Đồng bộ sang field cũ
    }

    public BigDecimal getTongChiTieu() {
        return tongChiTieu;
    }

    public void setTongChiTieu(BigDecimal tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }

    public Timestamp getCapNhatLanCuoi() {
        return capNhatLanCuoi;
    }

    public void setCapNhatLanCuoi(Timestamp capNhatLanCuoi) {
        this.capNhatLanCuoi = capNhatLanCuoi;
    }

    public String getMaHangThanhVien() {
        return maHangThanhVien;
    }

    public void setMaHangThanhVien(String maHangThanhVien) {
        this.maHangThanhVien = maHangThanhVien;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    // Tương thích ngược với field cũ
    public String getHoTen() {
        return hoTenKH != null ? hoTenKH : hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
        if (this.hoTenKH == null) {
            this.hoTenKH = hoTen;
        }
    }

    public String getSdt() {
        return sdt != null ? sdt : (nguoiDung != null ? nguoiDung.getSdt() : null);
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email != null ? email : (nguoiDung != null ? nguoiDung.getEmail() : null);
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
