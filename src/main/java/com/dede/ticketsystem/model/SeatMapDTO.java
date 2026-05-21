package com.dede.ticketsystem.model;

import java.util.ArrayList;
import java.util.List;

public class SeatMapDTO {

    private String maSK;
    private String tenSK;
    private List<SeatAreaDTO> areas = new ArrayList<>();

    public SeatMapDTO() {
    }

    public SeatMapDTO(String maSK, String tenSK, List<SeatAreaDTO> areas) {
        this.maSK = maSK;
        this.tenSK = tenSK;
        this.areas = areas;
    }

    public String getMaSK() {
        return maSK;
    }

    public void setMaSK(String maSK) {
        this.maSK = maSK;
    }

    public String getTenSK() {
        return tenSK;
    }

    public void setTenSK(String tenSK) {
        this.tenSK = tenSK;
    }

    public List<SeatAreaDTO> getAreas() {
        return areas;
    }

    public void setAreas(List<SeatAreaDTO> areas) {
        this.areas = areas;
    }
}
