package com.dede.ticketsystem.model;

import java.math.BigDecimal;

public class BaoCaoTongQuanDTO {
    private BigDecimal tongDoanhThu;
    private long tongVeDaBan;
    private long donChoThanhToan;
    private long donDaThanhToan;
    private long donDaHuy;
    private double tyLeThanhToanThanhCong;
    private double tyLeLapDayTB;
    private String suKienBanChayNhat;

    public BaoCaoTongQuanDTO() {
        this.tongDoanhThu = BigDecimal.ZERO;
        this.suKienBanChayNhat = "Chưa có";
    }

    public BigDecimal getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(BigDecimal tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }

    public long getTongVeDaBan() {
        return tongVeDaBan;
    }

    public void setTongVeDaBan(long tongVeDaBan) {
        this.tongVeDaBan = tongVeDaBan;
    }

    public long getDonChoThanhToan() {
        return donChoThanhToan;
    }

    public void setDonChoThanhToan(long donChoThanhToan) {
        this.donChoThanhToan = donChoThanhToan;
    }

    public long getDonDaThanhToan() {
        return donDaThanhToan;
    }

    public void setDonDaThanhToan(long donDaThanhToan) {
        this.donDaThanhToan = donDaThanhToan;
    }

    public long getDonDaHuy() {
        return donDaHuy;
    }

    public void setDonDaHuy(long donDaHuy) {
        this.donDaHuy = donDaHuy;
    }

    public double getTyLeThanhToanThanhCong() {
        return tyLeThanhToanThanhCong;
    }

    public void setTyLeThanhToanThanhCong(double tyLeThanhToanThanhCong) {
        this.tyLeThanhToanThanhCong = tyLeThanhToanThanhCong;
    }

    public double getTyLeLapDayTB() {
        return tyLeLapDayTB;
    }

    public void setTyLeLapDayTB(double tyLeLapDayTB) {
        this.tyLeLapDayTB = tyLeLapDayTB;
    }

    public String getSuKienBanChayNhat() {
        return suKienBanChayNhat;
    }

    public void setSuKienBanChayNhat(String suKienBanChayNhat) {
        this.suKienBanChayNhat = suKienBanChayNhat;
    }
}
