package com.dede.ticketsystem.model;

public class ZoneTicketRequest {
    private String maKhuVuc;
    private Integer soLuong;

    public ZoneTicketRequest() {
    }

    public ZoneTicketRequest(String maKhuVuc, Integer soLuong) {
        this.maKhuVuc = maKhuVuc;
        this.soLuong = soLuong;
    }

    public String getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(String maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}
