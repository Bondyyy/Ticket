package com.dede.ticketsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "DIADIEM")
public class DiaDiem {

    @Id
    @Column(name = "MaDiaDiem", length = 50)
    private String maDiaDiem;

    @Column(name = "TenDiaDiem", length = 200)
    private String tenDiaDiem;

    @Column(name = "DiaChi", length = 500)
    private String diaChi;

    @Column(name = "ThanhPho", length = 100)
    private String thanhPho;

    @Column(name = "SucChuaToiDa")
    private Integer sucChuaToiDa;

    @Column(name = "MoTa", length = 1000)
    private String moTa;

    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    @Column(name = "LinkGoogleMap", length = 1000)
    private String linkGoogleMap;

    public String getMaDiaDiem() {
        return maDiaDiem;
    }

    public void setMaDiaDiem(String maDiaDiem) {
        this.maDiaDiem = maDiaDiem;
    }

    public String getTenDiaDiem() {
        return tenDiaDiem;
    }

    public void setTenDiaDiem(String tenDiaDiem) {
        this.tenDiaDiem = tenDiaDiem;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getThanhPho() {
        return thanhPho;
    }

    public void setThanhPho(String thanhPho) {
        this.thanhPho = thanhPho;
    }

    public Integer getSucChuaToiDa() {
        return sucChuaToiDa;
    }

    public void setSucChuaToiDa(Integer sucChuaToiDa) {
        this.sucChuaToiDa = sucChuaToiDa;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getLinkGoogleMap() {
        return linkGoogleMap;
    }

    public void setLinkGoogleMap(String linkGoogleMap) {
        this.linkGoogleMap = linkGoogleMap;
    }
}
