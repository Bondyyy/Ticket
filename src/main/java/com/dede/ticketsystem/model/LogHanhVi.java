package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "LOG_HANH_VI")
public class LogHanhVi {

    @Id
    @Column(name = "MaLog", length = 50)
    private String maLog;

    @Column(name = "LoaiHanhDong", length = 50)
    private String loaiHanhDong;

    @Column(name = "MaSK", length = 50)
    private String maSK;

    @Column(name = "ThoiGian")
    private Timestamp thoiGian;

    @Column(name = "MaKH", length = 50)
    private String maKH;

    @Column(name = "ThietBi", length = 20)
    private String thietBi;

    public LogHanhVi() {
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

    public String getThietBi() {
        return thietBi;
    }

    public void setThietBi(String thietBi) {
        this.thietBi = thietBi;
    }
}
