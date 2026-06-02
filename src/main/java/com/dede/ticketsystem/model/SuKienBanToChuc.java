package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(
        name = "SUKIEN_BANTOCHUC",
        uniqueConstraints = @UniqueConstraint(name = "UK_SUKIEN_BANTOCHUC_SK_NV", columnNames = {"MaSK", "MaNV"})
)
public class SuKienBanToChuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "MaSK", length = 50, nullable = false)
    private String maSK;

    @Column(name = "MaNV", length = 50, nullable = false)
    private String maNV;

    @Column(name = "VaiTroTrongSuKien", length = 100)
    private String vaiTroTrongSuKien;

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @Column(name = "LaVaiTroChinh")
    private Boolean laVaiTroChinh;

    @Column(name = "ThoiGianTao", updatable = false)
    private Timestamp thoiGianTao;

    @Column(name = "CapNhatLanCuoi")
    private Timestamp capNhatLanCuoi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaSK() {
        return maSK;
    }

    public void setMaSK(String maSK) {
        this.maSK = maSK;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getVaiTroTrongSuKien() {
        return vaiTroTrongSuKien;
    }

    public void setVaiTroTrongSuKien(String vaiTroTrongSuKien) {
        this.vaiTroTrongSuKien = vaiTroTrongSuKien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Boolean getLaVaiTroChinh() {
        return laVaiTroChinh;
    }

    public void setLaVaiTroChinh(Boolean laVaiTroChinh) {
        this.laVaiTroChinh = laVaiTroChinh;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public Timestamp getCapNhatLanCuoi() {
        return capNhatLanCuoi;
    }

    public void setCapNhatLanCuoi(Timestamp capNhatLanCuoi) {
        this.capNhatLanCuoi = capNhatLanCuoi;
    }
}
