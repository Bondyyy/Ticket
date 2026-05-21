package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "KHUVUC")
public class KhuVuc {

    @Id
    @Column(name = "MaKhuVuc", length = 50)
    private String maKhuVuc;

    @Column(name = "TenKhuVuc", length = 100)
    private String tenKhuVuc;

    @Column(name = "MauSacHienThi", length = 20)
    private String mauSacHienThi;

    @Column(name = "SoGheToiDa")
    private Integer soGheToiDa;

    @Column(name = "SoGheDaBan")
    private Integer soGheDaBan;

    @Column(name = "SoVeToiDaPerKH")
    private Integer soVeToiDaPerKH;

    @Column(name = "GiaVe", precision = 18, scale = 2)
    private BigDecimal giaVe;

    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    @Column(name = "MaSK", length = 50)
    private String maSK;

    // Field hỗ trợ UI, không lưu DB
    @Transient
    private Integer soHang;

    @Transient
    private Integer soGheMoiHang;

    // Constructors
    public KhuVuc() {}

    public KhuVuc(String maKhuVuc, String tenKhuVuc, String mauSacHienThi, Integer soGheToiDa, Integer soGheDaBan, Integer soVeToiDaPerKH, BigDecimal giaVe, String trangThai, String maSK) {
        this.maKhuVuc = maKhuVuc;
        this.tenKhuVuc = tenKhuVuc;
        this.mauSacHienThi = mauSacHienThi;
        this.soGheToiDa = soGheToiDa;
        this.soGheDaBan = soGheDaBan;
        this.soVeToiDaPerKH = soVeToiDaPerKH;
        this.giaVe = giaVe;
        this.trangThai = trangThai;
        this.maSK = maSK;
    }

    // Getters and Setters
    public String getMaKhuVuc() { return maKhuVuc; }
    public void setMaKhuVuc(String maKhuVuc) { this.maKhuVuc = maKhuVuc; }

    public String getTenKhuVuc() { return tenKhuVuc; }
    public void setTenKhuVuc(String tenKhuVuc) { this.tenKhuVuc = tenKhuVuc; }

    public String getMauSacHienThi() { return mauSacHienThi; }
    public void setMauSacHienThi(String mauSacHienThi) { this.mauSacHienThi = mauSacHienThi; }

    public Integer getSoGheToiDa() { return soGheToiDa; }
    public void setSoGheToiDa(Integer soGheToiDa) { this.soGheToiDa = soGheToiDa; }

    public Integer getSoGheDaBan() { return soGheDaBan; }
    public void setSoGheDaBan(Integer soGheDaBan) { this.soGheDaBan = soGheDaBan; }

    public Integer getSoVeToiDaPerKH() { return soVeToiDaPerKH; }
    public void setSoVeToiDaPerKH(Integer soVeToiDaPerKH) { this.soVeToiDaPerKH = soVeToiDaPerKH; }

    public BigDecimal getGiaVe() { return giaVe; }
    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getMaSK() { return maSK; }
    public void setMaSK(String maSK) { this.maSK = maSK; }

    public Integer getSoHang() { return soHang; }
    public void setSoHang(Integer soHang) { this.soHang = soHang; }

    public Integer getSoGheMoiHang() { return soGheMoiHang; }
    public void setSoGheMoiHang(Integer soGheMoiHang) { this.soGheMoiHang = soGheMoiHang; }
}
