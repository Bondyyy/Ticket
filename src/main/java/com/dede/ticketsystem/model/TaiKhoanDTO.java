package com.dede.ticketsystem.model;

import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import java.util.List;

public class TaiKhoanDTO {

    private String maND;
    private String tenTaiKhoan;
    private String matKhau;
    private String email;
    private String sdt;
    private String gioiTinh;
    private String trangThaiND;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngaySinh;

    private List<String> danhSachVaiTro;

    public TaiKhoanDTO() {
    }

    public TaiKhoanDTO(String maND, String tenTaiKhoan, String matKhau, String email, String sdt, String gioiTinh, String trangThaiND, Date ngaySinh, List<String> danhSachVaiTro) {
        this.maND = maND;
        this.tenTaiKhoan = tenTaiKhoan;
        this.matKhau = matKhau;
        this.email = email;
        this.sdt = sdt;
        this.gioiTinh = gioiTinh;
        this.trangThaiND = trangThaiND;
        this.ngaySinh = ngaySinh;
        this.danhSachVaiTro = danhSachVaiTro;
    }

    // Getters and Setters
    public String getMaND() { return maND; }
    public void setMaND(String maND) { this.maND = maND; }

    public String getTenTaiKhoan() { return tenTaiKhoan; }
    public void setTenTaiKhoan(String tenTaiKhoan) { this.tenTaiKhoan = tenTaiKhoan; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getTrangThaiND() { return trangThaiND; }
    public void setTrangThaiND(String trangThaiND) { this.trangThaiND = trangThaiND; }

    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }

    public List<String> getDanhSachVaiTro() { return danhSachVaiTro; }
    public void setDanhSachVaiTro(List<String> danhSachVaiTro) { this.danhSachVaiTro = danhSachVaiTro; }

    // --- MANUAL BUILDER PATTERN ---
    public static TaiKhoanDTOBuilder builder() {
        return new TaiKhoanDTOBuilder();
    }

    public static class TaiKhoanDTOBuilder {
        private String maND;
        private String tenTaiKhoan;
        private String matKhau;
        private String email;
        private String sdt;
        private String gioiTinh;
        private String trangThaiND;
        private Date ngaySinh;
        private List<String> danhSachVaiTro;

        public TaiKhoanDTOBuilder maND(String maND) { this.maND = maND; return this; }
        public TaiKhoanDTOBuilder tenTaiKhoan(String tenTaiKhoan) { this.tenTaiKhoan = tenTaiKhoan; return this; }
        public TaiKhoanDTOBuilder matKhau(String matKhau) { this.matKhau = matKhau; return this; }
        public TaiKhoanDTOBuilder email(String email) { this.email = email; return this; }
        public TaiKhoanDTOBuilder sdt(String sdt) { this.sdt = sdt; return this; }
        public TaiKhoanDTOBuilder gioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; return this; }
        public TaiKhoanDTOBuilder trangThaiND(String trangThaiND) { this.trangThaiND = trangThaiND; return this; }
        public TaiKhoanDTOBuilder ngaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; return this; }
        public TaiKhoanDTOBuilder danhSachVaiTro(List<String> danhSachVaiTro) { this.danhSachVaiTro = danhSachVaiTro; return this; }

        public TaiKhoanDTO build() {
            return new TaiKhoanDTO(maND, tenTaiKhoan, matKhau, email, sdt, gioiTinh, trangThaiND, ngaySinh, danhSachVaiTro);
        }
    }
}