package com.dede.ticketsystem.model;

import java.util.ArrayList;
import java.util.List;

public class SeatRowDTO {

    private String hangGhe;
    private List<SeatCellDTO> seats = new ArrayList<>();

    public SeatRowDTO() {
    }

    public SeatRowDTO(String hangGhe, List<SeatCellDTO> seats) {
        this.hangGhe = hangGhe;
        this.seats = seats;
    }

    public String getHangGhe() {
        return hangGhe;
    }

    public void setHangGhe(String hangGhe) {
        this.hangGhe = hangGhe;
    }

    public List<SeatCellDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatCellDTO> seats) {
        this.seats = seats;
    }
}
