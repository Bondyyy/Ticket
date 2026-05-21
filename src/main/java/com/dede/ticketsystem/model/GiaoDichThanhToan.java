package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "GIAODICHTHANHTOAN")
public class GiaoDichThanhToan {

    @Id
    @Column(name = "MaGiaoDich", length = 50)
    private String maGiaoDich;

    @Column(name = "SoTienThanhToan", precision = 18, scale = 2)
    private BigDecimal soTienThanhToan;

    @Column(name = "PhuongThucTT", length = 50)
    private String phuongThucTT;

    @Column(name = "TrangThaiGD", length = 50)
    private String trangThaiGD;

    @Column(name = "MaGiaoDichBenThu3", length = 200)
    private String maGiaoDichBenThu3;

    @Column(name = "LanThuLai")
    private Integer lanThuLai = 0;

    @Column(name = "ThoiGianThucHien")
    private Timestamp thoiGianThucHien;

    @Column(name = "GhiChuLoi", length = 1000)
    private String ghiChuLoi;

    @Column(name = "MaDonHang", length = 50)
    private String maDonHang;

    // Getters and Setters
    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public BigDecimal getSoTienThanhToan() {
        return soTienThanhToan;
    }

    public void setSoTienThanhToan(BigDecimal soTienThanhToan) {
        this.soTienThanhToan = soTienThanhToan;
    }

    public String getPhuongThucTT() {
        return phuongThucTT;
    }

    public void setPhuongThucTT(String phuongThucTT) {
        this.phuongThucTT = phuongThucTT;
    }

    public String getTrangThaiGD() {
        return trangThaiGD;
    }

    public void setTrangThaiGD(String trangThaiGD) {
        this.trangThaiGD = trangThaiGD;
    }

    public String getMaGiaoDichBenThu3() {
        return maGiaoDichBenThu3;
    }

    public void setMaGiaoDichBenThu3(String maGiaoDichBenThu3) {
        this.maGiaoDichBenThu3 = maGiaoDichBenThu3;
    }

    public Integer getLanThuLai() {
        return lanThuLai;
    }

    public void setLanThuLai(Integer lanThuLai) {
        this.lanThuLai = lanThuLai;
    }

    public Timestamp getThoiGianThucHien() {
        return thoiGianThucHien;
    }

    public void setThoiGianThucHien(Timestamp thoiGianThucHien) {
        this.thoiGianThucHien = thoiGianThucHien;
    }

    public String getGhiChuLoi() {
        return ghiChuLoi;
    }

    public void setGhiChuLoi(String ghiChuLoi) {
        this.ghiChuLoi = ghiChuLoi;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
    }
}
