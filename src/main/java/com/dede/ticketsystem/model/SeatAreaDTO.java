package com.dede.ticketsystem.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SeatAreaDTO {

    private String maKhuVuc;
    private String tenKhuVuc;
    private String mauSacHienThi;
    private BigDecimal giaVe;
    private Integer soVeToiDaPerKH;
    private Integer soGheToiDa;
    private Integer soGheDaBan;
    private String trangThai;
    private List<SeatRowDTO> rows = new ArrayList<>();

    public SeatAreaDTO() {
    }

    public SeatAreaDTO(String maKhuVuc, String tenKhuVuc, String mauSacHienThi, BigDecimal giaVe,
                       Integer soVeToiDaPerKH, List<SeatRowDTO> rows) {
        this.maKhuVuc = maKhuVuc;
        this.tenKhuVuc = tenKhuVuc;
        this.mauSacHienThi = mauSacHienThi;
        this.giaVe = giaVe;
        this.soVeToiDaPerKH = soVeToiDaPerKH;
        this.rows = rows;
    }

    public SeatAreaDTO(String maKhuVuc, String tenKhuVuc, String mauSacHienThi, BigDecimal giaVe,
                       Integer soVeToiDaPerKH, Integer soGheToiDa, Integer soGheDaBan,
                       String trangThai, List<SeatRowDTO> rows) {
        this(maKhuVuc, tenKhuVuc, mauSacHienThi, giaVe, soVeToiDaPerKH, rows);
        this.soGheToiDa = soGheToiDa;
        this.soGheDaBan = soGheDaBan;
        this.trangThai = trangThai;
    }

    public String getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(String maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public String getMauSacHienThi() {
        return mauSacHienThi;
    }

    public void setMauSacHienThi(String mauSacHienThi) {
        this.mauSacHienThi = mauSacHienThi;
    }

    public BigDecimal getGiaVe() {
        return giaVe;
    }

    public void setGiaVe(BigDecimal giaVe) {
        this.giaVe = giaVe;
    }

    public Integer getSoVeToiDaPerKH() {
        return soVeToiDaPerKH;
    }

    public void setSoVeToiDaPerKH(Integer soVeToiDaPerKH) {
        this.soVeToiDaPerKH = soVeToiDaPerKH;
    }

    public Integer getSoGheToiDa() {
        return soGheToiDa;
    }

    public void setSoGheToiDa(Integer soGheToiDa) {
        this.soGheToiDa = soGheToiDa;
    }

    public Integer getSoGheDaBan() {
        return soGheDaBan;
    }

    public void setSoGheDaBan(Integer soGheDaBan) {
        this.soGheDaBan = soGheDaBan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<SeatRowDTO> getRows() {
        return rows;
    }

    public void setRows(List<SeatRowDTO> rows) {
        this.rows = rows;
    }
}
