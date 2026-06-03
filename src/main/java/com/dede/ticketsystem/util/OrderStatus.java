package com.dede.ticketsystem.util;

public final class OrderStatus {
    public static final String CHO_THANH_TOAN = "Chờ thanh toán";
    public static final String DA_THANH_TOAN = "Đã thanh toán";
    public static final String DA_HUY = "Đã hủy";
    public static final String HOAN_TIEN = "Hoàn tiền";

    private OrderStatus() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
