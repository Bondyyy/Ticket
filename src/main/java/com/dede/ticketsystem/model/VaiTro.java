package com.dede.ticketsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "VAITRO")
public class VaiTro {

    @Id
    @Column(name = "MaVaiTro", length = 50)
    private String maVaiTro;

    @Column(name = "TenVaiTro", length = 100)
    private String tenVaiTro;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    public VaiTro() {
    }

    public VaiTro(String maVaiTro, String tenVaiTro, String moTa) {
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.moTa = moTa;
    }

    // Getters and Setters
    public String getMaVaiTro() { return maVaiTro; }
    public void setMaVaiTro(String maVaiTro) { this.maVaiTro = maVaiTro; }

    public String getTenVaiTro() { return tenVaiTro; }
    public void setTenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    // --- MANUAL BUILDER PATTERN ---
    public static VaiTroBuilder builder() {
        return new VaiTroBuilder();
    }

    public static class VaiTroBuilder {
        private String maVaiTro;
        private String tenVaiTro;
        private String moTa;

        public VaiTroBuilder maVaiTro(String maVaiTro) { this.maVaiTro = maVaiTro; return this; }
        public VaiTroBuilder tenVaiTro(String tenVaiTro) { this.tenVaiTro = tenVaiTro; return this; }
        public VaiTroBuilder moTa(String moTa) { this.moTa = moTa; return this; }

        public VaiTro build() {
            return new VaiTro(maVaiTro, tenVaiTro, moTa);
        }
    }
}