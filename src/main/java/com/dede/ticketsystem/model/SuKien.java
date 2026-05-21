package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "SUKIEN")
public class SuKien {

    @Id
    @Column(name = "MaSK", length = 50)
    private String maSK;

    @Column(name = "TenSK", length = 300)
    private String tenSK;

    @Lob
    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "HinhAnh", length = 1000)
    private String hinhAnh;

    @Column(name = "HinhAnhThumb", length = 1000)
    private String hinhAnhThumb;

    @Column(name = "MoTaNgan", length = 500)
    private String moTaNgan;

    @Column(name = "Tags", length = 500)
    private String tags;

    @Column(name = "ThoiGianBatDau")
    private Timestamp thoiGianBatDau;

    @Column(name = "ThoiGianKetThuc")
    private Timestamp thoiGianKetThuc;

    @Column(name = "ThoiGianMoBan")
    private Timestamp thoiGianMoBan;

    @Column(name = "ThoiGianDongBan")
    private Timestamp thoiGianDongBan;

    @Column(name = "TongSoVe")
    private Integer tongSoVe;

    @Column(name = "SoVeDaBan")
    private Integer soVeDaBan;

    @Column(name = "TrangThaiSK", length = 50)
    private String trangThaiSK;

    @Column(name = "ThoiGianTao", updatable = false)
    private Timestamp thoiGianTao;

    @Column(name = "CapNhatLanCuoi")
    private Timestamp capNhatLanCuoi;

    @Column(name = "MaLoaiSK", length = 50)
    private String maLoaiSK;

    @Column(name = "MaDiaDiem", length = 50)
    private String maDiaDiem;

    @Column(name = "MaNV", length = 50)
    private String maNV;

    // Getters and Setters
    public String getMaSK() { return maSK; }
    public void setMaSK(String maSK) { this.maSK = maSK; }

    public String getTenSK() { return tenSK; }
    public void setTenSK(String tenSK) { this.tenSK = tenSK; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getHinhAnhThumb() { return hinhAnhThumb; }
    public void setHinhAnhThumb(String hinhAnhThumb) { this.hinhAnhThumb = hinhAnhThumb; }

    public String getMoTaNgan() { return moTaNgan; }
    public void setMoTaNgan(String moTaNgan) { this.moTaNgan = moTaNgan; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Timestamp getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(Timestamp thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public Timestamp getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(Timestamp thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }

    public Timestamp getThoiGianMoBan() { return thoiGianMoBan; }
    public void setThoiGianMoBan(Timestamp thoiGianMoBan) { this.thoiGianMoBan = thoiGianMoBan; }

    public Timestamp getThoiGianDongBan() { return thoiGianDongBan; }
    public void setThoiGianDongBan(Timestamp thoiGianDongBan) { this.thoiGianDongBan = thoiGianDongBan; }

    public Integer getTongSoVe() { return tongSoVe; }
    public void setTongSoVe(Integer tongSoVe) { this.tongSoVe = tongSoVe; }

    public Integer getSoVeDaBan() { return soVeDaBan; }
    public void setSoVeDaBan(Integer soVeDaBan) { this.soVeDaBan = soVeDaBan; }

    public String getTrangThaiSK() { return trangThaiSK; }
    public void setTrangThaiSK(String trangThaiSK) { this.trangThaiSK = trangThaiSK; }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public Timestamp getCapNhatLanCuoi() { return capNhatLanCuoi; }
    public void setCapNhatLanCuoi(Timestamp capNhatLanCuoi) { this.capNhatLanCuoi = capNhatLanCuoi; }

    public String getMaLoaiSK() { return maLoaiSK; }
    public void setMaLoaiSK(String maLoaiSK) { this.maLoaiSK = maLoaiSK; }

    public String getMaDiaDiem() { return maDiaDiem; }
    public void setMaDiaDiem(String maDiaDiem) { this.maDiaDiem = maDiaDiem; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
}
