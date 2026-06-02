package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "VE")
public class Ve {

    @Id
    @Column(name = "MaVe", length = 50)
    private String maVe;

    @Column(name = "MaQR", length = 500, unique = true, nullable = false)
    private String maQR;

    @Column(name = "GiaVe", precision = 18, scale = 2)
    private BigDecimal giaVe;

    @Column(name = "TrangThaiVe", length = 50)
    private String trangThaiVe;

    @Column(name = "ThoiGianPhat")
    private Timestamp thoiGianPhat;

    @Column(name = "ThoiGianSuDung")
    private Timestamp thoiGianSuDung;

    @Column(name = "MaDonHang", length = 50)
    private String maDonHang;

    @Column(name = "MaGhe", length = 50)
    private String maGhe;

    @Column(name = "MaKhuVuc", length = 50)
    private String maKhuVuc;

    @Column(name = "MaSK", length = 50)
    private String maSK;

    // Constructors
    public Ve() {
    }

    public Ve(String maVe, String maQR, BigDecimal giaVe, String trangThaiVe, Timestamp thoiGianPhat,
              Timestamp thoiGianSuDung, String maDonHang, String maGhe, String maSK) {
        this.maVe = maVe;
        this.maQR = maQR;
        this.giaVe = giaVe;
        this.trangThaiVe = trangThaiVe;
        this.thoiGianPhat = thoiGianPhat;
        this.thoiGianSuDung = thoiGianSuDung;
        this.maDonHang = maDonHang;
        this.maGhe = maGhe;
        this.maSK = maSK;
    }

    // Getters and Setters
    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public String getMaQR() { return maQR; }
    public void setMaQR(String maQR) { this.maQR = maQR; }

    public BigDecimal getGiaVe() { return giaVe; }
    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }

    public String getTrangThaiVe() { return trangThaiVe; }
    public void setTrangThaiVe(String trangThaiVe) { this.trangThaiVe = trangThaiVe; }

    public Timestamp getThoiGianPhat() { return thoiGianPhat; }
    public void setThoiGianPhat(Timestamp thoiGianPhat) { this.thoiGianPhat = thoiGianPhat; }

    public Timestamp getThoiGianSuDung() { return thoiGianSuDung; }
    public void setThoiGianSuDung(Timestamp thoiGianSuDung) { this.thoiGianSuDung = thoiGianSuDung; }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }

    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }

    public String getMaKhuVuc() { return maKhuVuc; }
    public void setMaKhuVuc(String maKhuVuc) { this.maKhuVuc = maKhuVuc; }

    public String getMaSK() { return maSK; }
    public void setMaSK(String maSK) { this.maSK = maSK; }
}
