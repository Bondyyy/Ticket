package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "GHENGOI")
public class Ghe {

    @Id
    @Column(name = "MaGhe", length = 50)
    private String maGhe;

    @Column(name = "TenGhe", length = 50)
    private String tenGhe;

    @Column(name = "HangGhe", length = 10)
    private String hangGhe;

    @Column(name = "CotGhe")
    private Integer cotGhe;

    @Column(name = "TrangThaiGhe", length = 50)
    private String trangThaiGhe; // Trống, Đang chọn, Đã bán, Bảo trì

    @Column(name = "MaKhuVuc", length = 50)
    private String maKhuVuc;

    @Column(name = "MaSK", length = 50)
    private String maSK;

    @Column(name = "ThoiGianKhoaTam")
    private Timestamp thoiGianKhoaTam;

    @Column(name = "MaPhienKhoa", length = 50)
    private String maPhienKhoa;

    // Constructors
    public Ghe() {}

    public Ghe(String maGhe, String tenGhe, String hangGhe, Integer cotGhe, String trangThaiGhe, String maKhuVuc, String maSK) {
        this.maGhe = maGhe;
        this.tenGhe = tenGhe;
        this.hangGhe = hangGhe;
        this.cotGhe = cotGhe;
        this.trangThaiGhe = trangThaiGhe;
        this.maKhuVuc = maKhuVuc;
        this.maSK = maSK;
    }

    // Getters and Setters
    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }

    public String getTenGhe() { return tenGhe; }
    public void setTenGhe(String tenGhe) { this.tenGhe = tenGhe; }

    public String getHangGhe() { return hangGhe; }
    public void setHangGhe(String hangGhe) { this.hangGhe = hangGhe; }

    public Integer getCotGhe() { return cotGhe; }
    public void setCotGhe(Integer cotGhe) { this.cotGhe = cotGhe; }

    public String getTrangThaiGhe() { return trangThaiGhe; }
    public void setTrangThaiGhe(String trangThaiGhe) { this.trangThaiGhe = trangThaiGhe; }

    public String getMaKhuVuc() { return maKhuVuc; }
    public void setMaKhuVuc(String maKhuVuc) { this.maKhuVuc = maKhuVuc; }

    public String getMaSK() { return maSK; }
    public void setMaSK(String maSK) { this.maSK = maSK; }

    public Timestamp getThoiGianKhoaTam() { return thoiGianKhoaTam; }
    public void setThoiGianKhoaTam(Timestamp thoiGianKhoaTam) { this.thoiGianKhoaTam = thoiGianKhoaTam; }

    public String getMaPhienKhoa() { return maPhienKhoa; }
    public void setMaPhienKhoa(String maPhienKhoa) { this.maPhienKhoa = maPhienKhoa; }
}
