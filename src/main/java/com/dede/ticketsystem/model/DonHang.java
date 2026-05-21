package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "DONHANG")
public class DonHang {

    @Id
    @Column(name = "MaDonHang", length = 50)
    private String maDonHang;

    @Column(name = "SoDonHang", length = 50)
    private String soDonHang;

    @Column(name = "TongTien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "ThanhTien", precision = 18, scale = 2)
    private BigDecimal thanhTien;

    @Column(name = "TrangThaiDonHang", length = 50)
    private String trangThaiDonHang;

    @Column(name = "ThoiGianDat")
    private Timestamp thoiGianDat;

    @Column(name = "ThoiGianHetHan")
    private Timestamp thoiGianHetHan;

    @Column(name = "CapNhatLanCuoi")
    private Timestamp capNhatLanCuoi;

    @Column(name = "MaKH", length = 50)
    private String maKH;

    @Column(name = "MaPGG", length = 50)
    private String maPGG;

    @Column(name = "MaNV", length = 50)
    private String maNV;

    // Getters and Setters
    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }

    public String getSoDonHang() { return soDonHang; }
    public void setSoDonHang(String soDonHang) { this.soDonHang = soDonHang; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }

    public String getTrangThaiDonHang() { return trangThaiDonHang; }
    public void setTrangThaiDonHang(String trangThaiDonHang) { this.trangThaiDonHang = trangThaiDonHang; }

    public Timestamp getThoiGianDat() { return thoiGianDat; }
    public void setThoiGianDat(Timestamp thoiGianDat) { this.thoiGianDat = thoiGianDat; }

    public Timestamp getThoiGianHetHan() { return thoiGianHetHan; }
    public void setThoiGianHetHan(Timestamp thoiGianHetHan) { this.thoiGianHetHan = thoiGianHetHan; }

    public Timestamp getCapNhatLanCuoi() { return capNhatLanCuoi; }
    public void setCapNhatLanCuoi(Timestamp capNhatLanCuoi) { this.capNhatLanCuoi = capNhatLanCuoi; }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getMaPGG() { return maPGG; }
    public void setMaPGG(String maPGG) { this.maPGG = maPGG; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
}
