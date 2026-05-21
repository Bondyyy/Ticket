package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "LICHSUGUI_EMAIL")
public class LichSuGuiEmail {

    @Id
    @Column(name = "MaEmail", length = 50)
    private String maEmail;

    @Column(name = "LoaiEmail", length = 50)
    private String loaiEmail;

    @Column(name = "DiaChiNhan", length = 100)
    private String diaChiNhan;

    @Column(name = "TrangThai", length = 50)
    private String trangThai; // "Da_gui", "That_bai", "Cho_gui"

    @Column(name = "SoLanThu")
    private Integer soLanThu;

    @Column(name = "ThoiGianGui")
    private Timestamp thoiGianGui;

    @Column(name = "MaVe", length = 50)
    private String maVe;

    @Column(name = "MaDonHang", length = 50)
    private String maDonHang;

    // Constructors
    public LichSuGuiEmail() {
    }

    public LichSuGuiEmail(String maEmail, String loaiEmail, String diaChiNhan, String trangThai, Integer soLanThu, Timestamp thoiGianGui, String maVe, String maDonHang) {
        this.maEmail = maEmail;
        this.loaiEmail = loaiEmail;
        this.diaChiNhan = diaChiNhan;
        this.trangThai = trangThai;
        this.soLanThu = soLanThu;
        this.thoiGianGui = thoiGianGui;
        this.maVe = maVe;
        this.maDonHang = maDonHang;
    }

    // Getters and Setters
    public String getMaEmail() {
        return maEmail;
    }

    public void setMaEmail(String maEmail) {
        this.maEmail = maEmail;
    }

    public String getLoaiEmail() {
        return loaiEmail;
    }

    public void setLoaiEmail(String loaiEmail) {
        this.loaiEmail = loaiEmail;
    }

    public String getDiaChiNhan() {
        return diaChiNhan;
    }

    public void setDiaChiNhan(String diaChiNhan) {
        this.diaChiNhan = diaChiNhan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getSoLanThu() {
        return soLanThu;
    }

    public void setSoLanThu(Integer soLanThu) {
        this.soLanThu = soLanThu;
    }

    public Timestamp getThoiGianGui() {
        return thoiGianGui;
    }

    public void setThoiGianGui(Timestamp thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }

    public String getMaVe() {
        return maVe;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
    }
}
