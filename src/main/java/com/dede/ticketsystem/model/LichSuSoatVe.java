package com.dede.ticketsystem.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "LICHSUSOATVE")
public class LichSuSoatVe {

    @Id
    @Column(name = "MaLichSu", length = 50)
    private String maLichSu;

    @Column(name = "ThoiGianQuet")
    private Timestamp thoiGianQuet;

    @Column(name = "KetQuaQuet", length = 50)
    private String ketQuaQuet; // 'Hợp lệ', 'Vé giả', 'Vé đã sử dụng', 'Sai sự kiện', 'Vé không tìm thấy'

    @Column(name = "CongSoat", length = 50)
    private String congSoat;

    @Column(name = "NguonDuLieu", length = 20)
    private String nguonDuLieu; // 'Online', 'Offline'

    @Column(name = "DaDongBo", length = 1)
    private String daDongBo; // 'Y', 'N'

    @Column(name = "ThoiGianDongBo")
    private Timestamp thoiGianDongBo;

    @Column(name = "MaVe", length = 50)
    private String maVe;

    @Column(name = "MaNV", length = 50)
    private String maNV;

    // Constructors
    public LichSuSoatVe() {}

    public LichSuSoatVe(String maLichSu, Timestamp thoiGianQuet, String ketQuaQuet, String congSoat,
                        String nguonDuLieu, String daDongBo, Timestamp thoiGianDongBo, String maVe, String maNV) {
        this.maLichSu = maLichSu;
        this.thoiGianQuet = thoiGianQuet;
        this.ketQuaQuet = ketQuaQuet;
        this.congSoat = congSoat;
        this.nguonDuLieu = nguonDuLieu;
        this.daDongBo = daDongBo;
        this.thoiGianDongBo = thoiGianDongBo;
        this.maVe = maVe;
        this.maNV = maNV;
    }

    // Getters and Setters
    public String getMaLichSu() {
        return maLichSu;
    }

    public void setMaLichSu(String maLichSu) {
        this.maLichSu = maLichSu;
    }

    public Timestamp getThoiGianQuet() {
        return thoiGianQuet;
    }

    public void setThoiGianQuet(Timestamp thoiGianQuet) {
        this.thoiGianQuet = thoiGianQuet;
    }

    public String getKetQuaQuet() {
        return ketQuaQuet;
    }

    public void setKetQuaQuet(String ketQuaQuet) {
        this.ketQuaQuet = ketQuaQuet;
    }

    public String getCongSoat() {
        return congSoat;
    }

    public void setCongSoat(String congSoat) {
        this.congSoat = congSoat;
    }

    public String getNguonDuLieu() {
        return nguonDuLieu;
    }

    public void setNguonDuLieu(String nguonDuLieu) {
        this.nguonDuLieu = nguonDuLieu;
    }

    public String getDaDongBo() {
        return daDongBo;
    }

    public void setDaDongBo(String daDongBo) {
        this.daDongBo = daDongBo;
    }

    public Timestamp getThoiGianDongBo() {
        return thoiGianDongBo;
    }

    public void setThoiGianDongBo(Timestamp thoiGianDongBo) {
        this.thoiGianDongBo = thoiGianDongBo;
    }

    public String getMaVe() {
        return maVe;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }
}
