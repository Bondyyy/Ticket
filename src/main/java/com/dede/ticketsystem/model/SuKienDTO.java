package com.dede.ticketsystem.model;

public class SuKienDTO {
    private String maSK;
    private String tenSK;
    private String moTa;
    private String hinhAnh;
    private String hinhAnhThumb;
    private String moTaNgan;
    private String tags;
    private String thoiGianBatDau; // ISO String from input datetime-local
    private String thoiGianKetThuc;
    private String thoiGianMoBan;
    private String thoiGianDongBan;
    private Integer tongSoVe;
    private Integer soVeDaBan;
    private String trangThaiSK;
    private String maLoaiSK;
    private String maDiaDiem;
    private String maNV;
    private String loaiSoDo;
    private String banToChucJson;

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

    public String getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(String thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public String getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(String thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }

    public String getThoiGianMoBan() { return thoiGianMoBan; }
    public void setThoiGianMoBan(String thoiGianMoBan) { this.thoiGianMoBan = thoiGianMoBan; }

    public String getThoiGianDongBan() { return thoiGianDongBan; }
    public void setThoiGianDongBan(String thoiGianDongBan) { this.thoiGianDongBan = thoiGianDongBan; }

    public Integer getTongSoVe() { return tongSoVe; }
    public void setTongSoVe(Integer tongSoVe) { this.tongSoVe = tongSoVe; }

    public Integer getSoVeDaBan() { return soVeDaBan; }
    public void setSoVeDaBan(Integer soVeDaBan) { this.soVeDaBan = soVeDaBan; }

    public String getTrangThaiSK() { return trangThaiSK; }
    public void setTrangThaiSK(String trangThaiSK) { this.trangThaiSK = trangThaiSK; }

    public String getMaLoaiSK() { return maLoaiSK; }
    public void setMaLoaiSK(String maLoaiSK) { this.maLoaiSK = maLoaiSK; }

    public String getMaDiaDiem() { return maDiaDiem; }
    public void setMaDiaDiem(String maDiaDiem) { this.maDiaDiem = maDiaDiem; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getLoaiSoDo() { return loaiSoDo; }
    public void setLoaiSoDo(String loaiSoDo) { this.loaiSoDo = loaiSoDo; }

    public String getBanToChucJson() { return banToChucJson; }
    public void setBanToChucJson(String banToChucJson) { this.banToChucJson = banToChucJson; }
}
