package com.dede.ticketsystem.model;

import java.io.Serializable;
import java.util.Objects;

public class ChiTietVaiTroID implements Serializable {
    private String maND;
    private String maVaiTro;

    // Default Constructor
    public ChiTietVaiTroID() {
    }

    // All Args Constructor
    public ChiTietVaiTroID(String maND, String maVaiTro) {
        this.maND = maND;
        this.maVaiTro = maVaiTro;
    }

    // Getters and Setters
    public String getMaND() {
        return maND;
    }

    public void setMaND(String maND) {
        this.maND = maND;
    }

    public String getMaVaiTro() {
        return maVaiTro;
    }

    public void setMaVaiTro(String maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    // Thay thế @EqualsAndHashCode của Lombok
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietVaiTroID that = (ChiTietVaiTroID) o;
        return Objects.equals(maND, that.maND) && Objects.equals(maVaiTro, that.maVaiTro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maND, maVaiTro);
    }
}