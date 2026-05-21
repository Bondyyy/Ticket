package com.dede.ticketsystem.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class VeDTO {
    private String maVe;
    private String maQR;
    private BigDecimal giaVe;
    private String trangThaiVe;
    private String thoiGianPhat;
    private String thoiGianSuDung;
    private String maDonHang;
    private String maGhe;
    private String maSK;

    // Getters and Setters
    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }

    public String getMaQR() { return maQR; }
    public void setMaQR(String maQR) { this.maQR = maQR; }

    public BigDecimal getGiaVe() { return giaVe; }
    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }

    public String getTrangThaiVe() { return trangThaiVe; }
    public void setTrangThaiVe(String trangThaiVe) { this.trangThaiVe = trangThaiVe; }

    public String getThoiGianPhat() { return thoiGianPhat; }
    public void setThoiGianPhat(String thoiGianPhat) { this.thoiGianPhat = thoiGianPhat; }

    public String getThoiGianSuDung() { return thoiGianSuDung; }
    public void setThoiGianSuDung(String thoiGianSuDung) { this.thoiGianSuDung = thoiGianSuDung; }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }

    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }

    public String getMaSK() { return maSK; }
    public void setMaSK(String maSK) { this.maSK = maSK; }
}
