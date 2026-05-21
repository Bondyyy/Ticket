package com.dede.ticketsystem.model;

public class SeatCellDTO {

    private String maGhe;
    private String tenGhe;
    private String hangGhe;
    private Integer cotGhe;
    private String trangThaiGhe;
    private String maKhuVuc;

    public SeatCellDTO() {
    }

    public SeatCellDTO(String maGhe, String tenGhe, String hangGhe, Integer cotGhe, String trangThaiGhe, String maKhuVuc) {
        this.maGhe = maGhe;
        this.tenGhe = tenGhe;
        this.hangGhe = hangGhe;
        this.cotGhe = cotGhe;
        this.trangThaiGhe = trangThaiGhe;
        this.maKhuVuc = maKhuVuc;
    }

    public String getMaGhe() {
        return maGhe;
    }

    public void setMaGhe(String maGhe) {
        this.maGhe = maGhe;
    }

    public String getTenGhe() {
        return tenGhe;
    }

    public void setTenGhe(String tenGhe) {
        this.tenGhe = tenGhe;
    }

    public String getHangGhe() {
        return hangGhe;
    }

    public void setHangGhe(String hangGhe) {
        this.hangGhe = hangGhe;
    }

    public Integer getCotGhe() {
        return cotGhe;
    }

    public void setCotGhe(Integer cotGhe) {
        this.cotGhe = cotGhe;
    }

    public String getTrangThaiGhe() {
        return trangThaiGhe;
    }

    public void setTrangThaiGhe(String trangThaiGhe) {
        this.trangThaiGhe = trangThaiGhe;
    }

    public String getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(String maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }
}
