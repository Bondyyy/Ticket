package com.dede.ticketsystem.model;

public class BanToChucThanhVienDTO {

    private String maNV;
    private String tenNhanVien;
    private String email;
    private String sdt;
    private String vaiTroTrongSuKien;
    private String ghiChu;
    private Boolean laVaiTroChinh;

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
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
}
