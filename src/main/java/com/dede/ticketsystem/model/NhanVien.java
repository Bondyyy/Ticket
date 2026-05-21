package com.dede.ticketsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "NHANVIEN")
public class NhanVien {

    @Id
    @Column(name = "MaNV", length = 50)
    private String maNV;

    @Column(name = "LoaiNV", length = 50)
    private String loaiNV;

    @Column(name = "NgayVaoLam")
    private Timestamp ngayVaoLam;

    @Column(name = "TrangThaiLamViec", length = 50)
    private String trangThaiLamViec;

    @Column(name = "LuongCoBan", precision = 18, scale = 2)
    private BigDecimal luongCoBan;

    @Column(name = "PhuCap", precision = 18, scale = 2)
    private BigDecimal phuCap;

    @Column(name = "MaNQL", length = 50)
    private String maNQL;

    @Column(name = "MaND", length = 50, unique = true)
    private String maND;

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getLoaiNV() {
        return loaiNV;
    }

    public void setLoaiNV(String loaiNV) {
        this.loaiNV = loaiNV;
    }

    public Timestamp getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(Timestamp ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public String getTrangThaiLamViec() {
        return trangThaiLamViec;
    }

    public void setTrangThaiLamViec(String trangThaiLamViec) {
        this.trangThaiLamViec = trangThaiLamViec;
    }

    public BigDecimal getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(BigDecimal luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    public BigDecimal getPhuCap() {
        return phuCap;
    }

    public void setPhuCap(BigDecimal phuCap) {
        this.phuCap = phuCap;
    }

    public String getMaNQL() {
        return maNQL;
    }

    public void setMaNQL(String maNQL) {
        this.maNQL = maNQL;
    }

    public String getMaND() {
        return maND;
    }

    public void setMaND(String maND) {
        this.maND = maND;
    }
}
