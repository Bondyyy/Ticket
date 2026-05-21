package com.dede.ticketsystem.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class VeCuaToiDTO {

    private String maVe;
    private String tenSuKien;
    private String tenGhe;
    private String tenKhuVuc;
    private BigDecimal giaVe;
    private String trangThaiVe;
    private Timestamp thoiGianBatDau;
    private String qrPayload;
    private String qrBase64;

    // Constructors
    public VeCuaToiDTO() {
    }

    public VeCuaToiDTO(String maVe, String tenSuKien, String tenGhe, String tenKhuVuc, BigDecimal giaVe, String trangThaiVe, Timestamp thoiGianBatDau, String qrPayload, String qrBase64) {
        this.maVe = maVe;
        this.tenSuKien = tenSuKien;
        this.tenGhe = tenGhe;
        this.tenKhuVuc = tenKhuVuc;
        this.giaVe = giaVe;
        this.trangThaiVe = trangThaiVe;
        this.thoiGianBatDau = thoiGianBatDau;
        this.qrPayload = qrPayload;
        this.qrBase64 = qrBase64;
    }

    // Getters and Setters
    public String getMaVe() {
        return maVe;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public String getTenSuKien() {
        return tenSuKien;
    }

    public void setTenSuKien(String tenSuKien) {
        this.tenSuKien = tenSuKien;
    }

    public String getTenGhe() {
        return tenGhe;
    }

    public void setTenGhe(String tenGhe) {
        this.tenGhe = tenGhe;
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public BigDecimal getGiaVe() {
        return giaVe;
    }

    public void setGiaVe(BigDecimal giaVe) {
        this.giaVe = giaVe;
    }

    public String getTrangThaiVe() {
        return trangThaiVe;
    }

    public void setTrangThaiVe(String trangThaiVe) {
        this.trangThaiVe = trangThaiVe;
    }

    public Timestamp getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Timestamp thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public String getQrPayload() {
        return qrPayload;
    }

    public void setQrPayload(String qrPayload) {
        this.qrPayload = qrPayload;
    }

    public String getQrBase64() {
        return qrBase64;
    }

    public void setQrBase64(String qrBase64) {
        this.qrBase64 = qrBase64;
    }
}
