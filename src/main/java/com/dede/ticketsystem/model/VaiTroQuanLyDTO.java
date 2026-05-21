package com.dede.ticketsystem.model;

public class VaiTroQuanLyDTO {

    private String maVaiTro;
    private String tenVaiTro;
    private String moTa;
    private long soNguoiDung;
    private String loaiVaiTro;
    private boolean systemRole;
    private boolean legacyRole;

    public String getMaVaiTro() { return maVaiTro; }
    public void setMaVaiTro(String maVaiTro) { this.maVaiTro = maVaiTro; }

    public String getTenVaiTro() { return tenVaiTro; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public long getSoNguoiDung() { return soNguoiDung; }
    public void setSoNguoiDung(long soNguoiDung) { this.soNguoiDung = soNguoiDung; }

    public String getLoaiVaiTro() { return loaiVaiTro; }
    public void setLoaiVaiTro(String loaiVaiTro) { this.loaiVaiTro = loaiVaiTro; }

    public boolean isSystemRole() { return systemRole; }
    public void setSystemRole(boolean systemRole) { this.systemRole = systemRole; }

    public boolean isLegacyRole() { return legacyRole; }
    public void setLegacyRole(boolean legacyRole) { this.legacyRole = legacyRole; }
}
