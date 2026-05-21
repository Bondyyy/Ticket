package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "HANGDOIAO")
public class HangDoiAo {

    @Id
    @Column(name = "MaHangDoi", length = 50)
    private String maHangDoi;

    @Column(name = "ViTriHang")
    private Long viTriHang;

    @Column(name = "ThoiGianVaoHang")
    private Timestamp thoiGianVaoHang;

    @Column(name = "ThoiGianUocTinh")
    private Timestamp thoiGianUocTinh;

    @Column(name = "TrangThai", length = 50)
    private String trangThai; // 'Đang chờ' | 'Được vào' | 'Hết hạn'

    @Column(name = "TokenHangDoi", length = 200, unique = true)
    private String tokenHangDoi;

    @Column(name = "ThoiGianHetHan")
    private Timestamp thoiGianHetHan;

    @Column(name = "MaKH", length = 50)
    private String maKH;

    @Column(name = "MaSK", length = 50)
    private String maSK;

    // Constructors
    public HangDoiAo() {}

    // Getters and Setters
    public String getMaHangDoi() {
        return maHangDoi;
    }

    public void setMaHangDoi(String maHangDoi) {
        this.maHangDoi = maHangDoi;
    }

    public Long getViTriHang() {
        return viTriHang;
    }

    public void setViTriHang(Long viTriHang) {
        this.viTriHang = viTriHang;
    }

    public Timestamp getThoiGianVaoHang() {
        return thoiGianVaoHang;
    }

    public void setThoiGianVaoHang(Timestamp thoiGianVaoHang) {
        this.thoiGianVaoHang = thoiGianVaoHang;
    }

    public Timestamp getThoiGianUocTinh() {
        return thoiGianUocTinh;
    }

    public void setThoiGianUocTinh(Timestamp thoiGianUocTinh) {
        this.thoiGianUocTinh = thoiGianUocTinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTokenHangDoi() {
        return tokenHangDoi;
    }

    public void setTokenHangDoi(String tokenHangDoi) {
        this.tokenHangDoi = tokenHangDoi;
    }

    public Timestamp getThoiGianHetHan() {
        return thoiGianHetHan;
    }

    public void setThoiGianHetHan(Timestamp thoiGianHetHan) {
        this.thoiGianHetHan = thoiGianHetHan;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getMaSK() {
        return maSK;
    }

    public void setMaSK(String maSK) {
        this.maSK = maSK;
    }
}
