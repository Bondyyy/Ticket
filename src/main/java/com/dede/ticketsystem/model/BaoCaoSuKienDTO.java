package com.dede.ticketsystem.model;

import java.math.BigDecimal;

public class BaoCaoSuKienDTO {
    private String maSK;
    private String tenSK;
    private long tongSoVe;
    private long soVeDaBan;
    private long soVeConLai;
    private BigDecimal doanhThu;
    private double tyLeLapDay;
    private long luotXem;
    private long luotClick;
    private long luotBoGioHang;
    private double tyLeChuyenDoi;

    public BaoCaoSuKienDTO() {
        this.doanhThu = BigDecimal.ZERO;
    }

    public String getMaSK() {
        return maSK;
    }

    public void setMaSK(String maSK) {
        this.maSK = maSK;
    }

    public String getTenSK() {
        return tenSK;
    }

    public void setTenSK(String tenSK) {
        this.tenSK = tenSK;
    }

    public long getTongSoVe() {
        return tongSoVe;
    }

    public void setTongSoVe(long tongSoVe) {
        this.tongSoVe = tongSoVe;
    }

    public long getSoVeDaBan() {
        return soVeDaBan;
    }

    public void setSoVeDaBan(long soVeDaBan) {
        this.soVeDaBan = soVeDaBan;
    }

    public long getSoVeConLai() {
        return soVeConLai;
    }

    public void setSoVeConLai(long soVeConLai) {
        this.soVeConLai = soVeConLai;
    }

    public BigDecimal getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(BigDecimal doanhThu) {
        this.doanhThu = doanhThu;
    }

    public double getTyLeLapDay() {
        return tyLeLapDay;
    }

    public void setTyLeLapDay(double tyLeLapDay) {
        this.tyLeLapDay = tyLeLapDay;
    }

    public long getLuotXem() {
        return luotXem;
    }

    public void setLuotXem(long luotXem) {
        this.luotXem = luotXem;
    }

    public long getLuotClick() {
        return luotClick;
    }

    public void setLuotClick(long luotClick) {
        this.luotClick = luotClick;
    }

    public long getLuotBoGioHang() {
        return luotBoGioHang;
    }

    public void setLuotBoGioHang(long luotBoGioHang) {
        this.luotBoGioHang = luotBoGioHang;
    }

    public double getTyLeChuyenDoi() {
        return tyLeChuyenDoi;
    }

    public void setTyLeChuyenDoi(double tyLeChuyenDoi) {
        this.tyLeChuyenDoi = tyLeChuyenDoi;
    }
}
