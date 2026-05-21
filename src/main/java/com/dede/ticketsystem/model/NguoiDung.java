package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "NGUOIDUNG")
public class NguoiDung {

    @Id
    @Column(name = "MaND", length = 50)
    private String maND;

    @Column(name = "TenTaiKhoan", length = 50, unique = true)
    private String tenTaiKhoan;

    @Column(name = "MatKhauMaHoa", length = 255)
    private String matKhauMaHoa;

    @Column(name = "AnhDaiDien", length = 500)
    private String anhDaiDien;

    @Column(name = "GioiTinh", length = 10)
    private String gioiTinh;

    @Column(name = "Email", length = 100, unique = true)
    private String email;

    @Column(name = "SDT", length = 20)
    private String sdt;

    @Column(name = "NgaySinh")
    private Date ngaySinh;

    @Column(name = "ThoiGianTao")
    private Timestamp thoiGianTao;

    @Column(name = "CapNhatLanCuoi")
    private Timestamp capNhatLanCuoi;

    @Column(name = "LanCuoiDangNhap")
    private Timestamp lanCuoiDangNhap;

    @Column(name = "TrangThaiND", length = 50)
    private String trangThaiND; // "Đang hoạt động", "Không hoạt động", "Bị khóa"

    @OneToMany(mappedBy = "nguoiDung", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietVaiTro> chiTietVaiTros;

    public NguoiDung() {
    }

    public NguoiDung(String maND, String tenTaiKhoan, String matKhauMaHoa, String anhDaiDien, String gioiTinh,
            String email, String sdt, Date ngaySinh, Timestamp thoiGianTao, Timestamp capNhatLanCuoi,
            Timestamp lanCuoiDangNhap, String trangThaiND, List<ChiTietVaiTro> chiTietVaiTros) {
        this.maND = maND;
        this.tenTaiKhoan = tenTaiKhoan;
        this.matKhauMaHoa = matKhauMaHoa;
        this.anhDaiDien = anhDaiDien;
        this.gioiTinh = gioiTinh;
        this.email = email;
        this.sdt = sdt;
        this.ngaySinh = ngaySinh;
        this.thoiGianTao = thoiGianTao;
        this.capNhatLanCuoi = capNhatLanCuoi;
        this.lanCuoiDangNhap = lanCuoiDangNhap;
        this.trangThaiND = trangThaiND;
        this.chiTietVaiTros = chiTietVaiTros;
    }

    public boolean isAdmin() {
        if (this.chiTietVaiTros == null) {
            return false;
        }
        return this.chiTietVaiTros.stream()
                .anyMatch(ct -> "ADMIN".equals(ct.getMaVaiTro()));
    }

    // Getters and Setters
    public String getMaND() {
        return maND;
    }

    public void setMaND(String maND) {
        this.maND = maND;
    }

    public String getTenTaiKhoan() {
        return tenTaiKhoan;
    }

    public void setTenTaiKhoan(String tenTaiKhoan) {
        this.tenTaiKhoan = tenTaiKhoan;
    }

    public String getMatKhauMaHoa() {
        return matKhauMaHoa;
    }

    public void setMatKhauMaHoa(String matKhauMaHoa) {
        this.matKhauMaHoa = matKhauMaHoa;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
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

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
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

    public Timestamp getLanCuoiDangNhap() {
        return lanCuoiDangNhap;
    }

    public void setLanCuoiDangNhap(Timestamp lanCuoiDangNhap) {
        this.lanCuoiDangNhap = lanCuoiDangNhap;
    }

    public String getTrangThaiND() {
        return trangThaiND;
    }

    public void setTrangThaiND(String trangThaiND) {
        this.trangThaiND = trangThaiND;
    }

    public List<ChiTietVaiTro> getChiTietVaiTros() {
        return chiTietVaiTros;
    }

    public void setChiTietVaiTros(List<ChiTietVaiTro> chiTietVaiTros) {
        this.chiTietVaiTros = chiTietVaiTros;
    }

    // --- MANUAL BUILDER PATTERN ---
    public static NguoiDungBuilder builder() {
        return new NguoiDungBuilder();
    }

    public static class NguoiDungBuilder {
        private String maND;
        private String tenTaiKhoan;
        private String matKhauMaHoa;
        private String anhDaiDien;
        private String gioiTinh;
        private String email;
        private String sdt;
        private Date ngaySinh;
        private Timestamp thoiGianTao;
        private Timestamp capNhatLanCuoi;
        private Timestamp lanCuoiDangNhap;
        private String trangThaiND;
        private List<ChiTietVaiTro> chiTietVaiTros;

        public NguoiDungBuilder maND(String maND) {
            this.maND = maND;
            return this;
        }

        public NguoiDungBuilder tenTaiKhoan(String tenTaiKhoan) {
            this.tenTaiKhoan = tenTaiKhoan;
            return this;
        }

        public NguoiDungBuilder matKhauMaHoa(String matKhauMaHoa) {
            this.matKhauMaHoa = matKhauMaHoa;
            return this;
        }

        public NguoiDungBuilder anhDaiDien(String anhDaiDien) {
            this.anhDaiDien = anhDaiDien;
            return this;
        }

        public NguoiDungBuilder gioiTinh(String gioiTinh) {
            this.gioiTinh = gioiTinh;
            return this;
        }

        public NguoiDungBuilder email(String email) {
            this.email = email;
            return this;
        }

        public NguoiDungBuilder sdt(String sdt) {
            this.sdt = sdt;
            return this;
        }

        public NguoiDungBuilder ngaySinh(Date ngaySinh) {
            this.ngaySinh = ngaySinh;
            return this;
        }

        public NguoiDungBuilder thoiGianTao(Timestamp thoiGianTao) {
            this.thoiGianTao = thoiGianTao;
            return this;
        }

        public NguoiDungBuilder capNhatLanCuoi(Timestamp capNhatLanCuoi) {
            this.capNhatLanCuoi = capNhatLanCuoi;
            return this;
        }

        public NguoiDungBuilder lanCuoiDangNhap(Timestamp lanCuoiDangNhap) {
            this.lanCuoiDangNhap = lanCuoiDangNhap;
            return this;
        }

        public NguoiDungBuilder trangThaiND(String trangThaiND) {
            this.trangThaiND = trangThaiND;
            return this;
        }

        public NguoiDungBuilder chiTietVaiTros(List<ChiTietVaiTro> chiTietVaiTros) {
            this.chiTietVaiTros = chiTietVaiTros;
            return this;
        }

        public NguoiDung build() {
            return new NguoiDung(maND, tenTaiKhoan, matKhauMaHoa, anhDaiDien, gioiTinh, email, sdt, ngaySinh,
                    thoiGianTao, capNhatLanCuoi, lanCuoiDangNhap, trangThaiND, chiTietVaiTros);
        }
    }
}