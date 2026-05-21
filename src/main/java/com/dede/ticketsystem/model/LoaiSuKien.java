package com.dede.ticketsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "LOAISUKIEN")
public class LoaiSuKien {

    @Id
    @Column(name = "MaLoaiSK", length = 50)
    private String maLoaiSK;

    @Column(name = "TenLoaiSK", length = 100)
    private String tenLoaiSK;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    public LoaiSuKien() {
    }

    public LoaiSuKien(String maLoaiSK, String tenLoaiSK, String moTa) {
        this.maLoaiSK = maLoaiSK;
        this.tenLoaiSK = tenLoaiSK;
        this.moTa = moTa;
    }

    public String getMaLoaiSK() {
        return maLoaiSK;
    }

    public void setMaLoaiSK(String maLoaiSK) {
        this.maLoaiSK = maLoaiSK;
    }

    public String getTenLoaiSK() {
        return tenLoaiSK;
    }

    public void setTenLoaiSK(String tenLoaiSK) {
        this.tenLoaiSK = tenLoaiSK;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
