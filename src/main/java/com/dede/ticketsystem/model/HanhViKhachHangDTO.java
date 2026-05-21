package com.dede.ticketsystem.model;

import java.sql.Timestamp;

public class HanhViKhachHangDTO {
    private String maLog;
    private String loaiHanhDong;
    private String maSK;
    private String tenSK;
    private Timestamp thoiGian;
    private String maKH;
    private String tenKH;
    private String thietBi;

    public HanhViKhachHangDTO() {
    }

    public HanhViKhachHangDTO(String maLog, String loaiHanhDong, String maSK, String tenSK, Timestamp thoiGian, String maKH, String tenKH, String thietBi) {
        this.maLog = maLog;
        this.loaiHanhDong = loaiHanhDong;
        this.maSK = maSK;
        this.tenSK = tenSK;
        this.thoiGian = thoiGian;
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.thietBi = thietBi;
    }

    public String getMaLog() {
        return maLog;
    }

    public void setMaLog(String maLog) {
        this.maLog = maLog;
    }

    public String getLoaiHanhDong() {
        return loaiHanhDong;
    }

    public void setLoaiHanhDong(String loaiHanhDong) {
        this.loaiHanhDong = loaiHanhDong;
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

    public Timestamp getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(Timestamp thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getThietBi() {
        return thietBi;
    }

    public void setThietBi(String thietBi) {
        this.thietBi = thietBi;
    }
}
