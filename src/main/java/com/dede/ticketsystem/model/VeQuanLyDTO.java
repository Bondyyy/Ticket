package com.dede.ticketsystem.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class VeQuanLyDTO {

    private String maVe;
    private String maQR;
    private String maSK;
    private String tenSuKien;
    private String maGhe;
    private String tenGhe;
    private String maDonHang;
    private String soDonHang;
    private String maKH;
    private String tenKhachHang;
    private BigDecimal giaVe;
    private String trangThaiVe;
    private Timestamp thoiGianPhat;
    private Timestamp thoiGianSuDung;

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public String getMaQR() { return maQR; }
    public void setMaQR(String maQR) { this.maQR = maQR; }

    public String getMaSK() { return maSK; }
    public void setMaSK(String maSK) { this.maSK = maSK; }

    public String getTenSuKien() { return tenSuKien; }
    public void setTenSuKien(String tenSuKien) { this.tenSuKien = tenSuKien; }

    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }

    public String getTenGhe() { return tenGhe; }
    public void setTenGhe(String tenGhe) { this.tenGhe = tenGhe; }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }

    public String getSoDonHang() { return soDonHang; }
    public void setSoDonHang(String soDonHang) { this.soDonHang = soDonHang; }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public BigDecimal getGiaVe() { return giaVe; }
    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }

    public String getTrangThaiVe() { return trangThaiVe; }
    public void setTrangThaiVe(String trangThaiVe) { this.trangThaiVe = trangThaiVe; }

    public Timestamp getThoiGianPhat() { return thoiGianPhat; }
    public void setThoiGianPhat(Timestamp thoiGianPhat) { this.thoiGianPhat = thoiGianPhat; }

    public Timestamp getThoiGianSuDung() { return thoiGianSuDung; }
    public void setThoiGianSuDung(Timestamp thoiGianSuDung) { this.thoiGianSuDung = thoiGianSuDung; }
}
