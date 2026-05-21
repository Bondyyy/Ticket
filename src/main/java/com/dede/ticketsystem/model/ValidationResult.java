package com.dede.ticketsystem.model;

import java.sql.Timestamp;

public class ValidationResult {
    private boolean success;
    private String status; // "Hợp lệ", "Vé giả", "Vé đã sử dụng", "Sai sự kiện", "Vé không tìm thấy"
    private String maVe;
    private String seatName;
    private String zoneName;
    private String ticketOwner;
    private long durationMs;
    private Timestamp thoiGianQuet;
    private String maSK;
    private String message;

    public ValidationResult() {}

    public ValidationResult(boolean success, String status, String maVe, String seatName, String zoneName, String ticketOwner, long durationMs) {
        this.success = success;
        this.status = status;
        this.maVe = maVe;
        this.seatName = seatName;
        this.zoneName = zoneName;
        this.ticketOwner = ticketOwner;
        this.durationMs = durationMs;
    }

    public ValidationResult(boolean success, String status, String maVe, String seatName, String zoneName,
                            String ticketOwner, long durationMs, Timestamp thoiGianQuet, String maSK) {
        this(success, status, maVe, seatName, zoneName, ticketOwner, durationMs);
        this.thoiGianQuet = thoiGianQuet;
        this.maSK = maSK;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMaVe() {
        return maVe;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getTicketOwner() {
        return ticketOwner;
    }

    public void setTicketOwner(String ticketOwner) {
        this.ticketOwner = ticketOwner;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public Timestamp getThoiGianQuet() {
        return thoiGianQuet;
    }

    public void setThoiGianQuet(Timestamp thoiGianQuet) {
        this.thoiGianQuet = thoiGianQuet;
    }

    public String getMaSK() {
        return maSK;
    }

    public void setMaSK(String maSK) {
        this.maSK = maSK;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
