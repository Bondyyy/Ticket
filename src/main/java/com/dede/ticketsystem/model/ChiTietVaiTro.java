package com.dede.ticketsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CHITIETVAITRO")
@IdClass(ChiTietVaiTroID.class)
public class ChiTietVaiTro {

    @Id
    @Column(name = "MaND", length = 50)
    private String maND;

    @Id
    @Column(name = "MaVaiTro", length = 50)
    private String maVaiTro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaND", insertable = false, updatable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaVaiTro", insertable = false, updatable = false)
    private VaiTro vaiTro;

    public ChiTietVaiTro() {
    }

    public ChiTietVaiTro(String maND, String maVaiTro, NguoiDung nguoiDung, VaiTro vaiTro) {
        this.maND = maND;
        this.maVaiTro = maVaiTro;
        this.nguoiDung = nguoiDung;
        this.vaiTro = vaiTro;
    }

    // Getters and Setters
    public String getMaND() { return maND; }
    public void setMaND(String maND) { this.maND = maND; }

    public String getMaVaiTro() { return maVaiTro; }
    public void setMaVaiTro(String maVaiTro) { this.maVaiTro = maVaiTro; }

    public NguoiDung getNguoiDung() { return nguoiDung; }
    public void setNguoiDung(NguoiDung nguoiDung) { this.nguoiDung = nguoiDung; }

    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }

    // --- MANUAL BUILDER PATTERN ---
    public static ChiTietVaiTroBuilder builder() {
        return new ChiTietVaiTroBuilder();
    }

    public static class ChiTietVaiTroBuilder {
        private String maND;
        private String maVaiTro;
        private NguoiDung nguoiDung;
        private VaiTro vaiTro;

        public ChiTietVaiTroBuilder maND(String maND) { this.maND = maND; return this; }
        public ChiTietVaiTroBuilder maVaiTro(String maVaiTro) { this.maVaiTro = maVaiTro; return this; }
        public ChiTietVaiTroBuilder nguoiDung(NguoiDung nguoiDung) { this.nguoiDung = nguoiDung; return this; }
        public ChiTietVaiTroBuilder vaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; return this; }

        public ChiTietVaiTro build() {
            return new ChiTietVaiTro(maND, maVaiTro, nguoiDung, vaiTro);
        }
    }
}