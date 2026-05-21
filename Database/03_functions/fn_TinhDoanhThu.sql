-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE FUNCTION fn_TinhDoanhThu (
    p_MaSK          IN VARCHAR2 DEFAULT NULL,
    p_TuNgay        IN DATE     DEFAULT NULL,
    p_DenNgay       IN DATE     DEFAULT NULL
) RETURN NUMBER IS
    v_DoanhThu NUMBER(18,2);
BEGIN
    IF p_MaSK IS NOT NULL THEN
        -- Doanh thu theo sự kiện: Tổng giá vé của các vé đã bán trong sự kiện
        SELECT NVL(SUM(v.GiaVe), 0)
        INTO v_DoanhThu
        FROM VE v
        JOIN DONHANG d ON v.MaDonHang = d.MaDonHang
        WHERE d.TrangThaiDonHang = 'Đã thanh toán'
          AND v.MaSK = p_MaSK
          AND (p_TuNgay IS NULL OR TRUNC(d.ThoiGianDat) >= p_TuNgay)
          AND (p_DenNgay IS NULL OR TRUNC(d.ThoiGianDat) <= p_DenNgay);
    ELSE
        -- Tổng doanh thu hệ thống: Tổng tiền thanh toán thực tế của các đơn hàng đã thanh toán thành công
        SELECT NVL(SUM(d.ThanhTien), 0)
        INTO v_DoanhThu
        FROM DONHANG d
        WHERE d.TrangThaiDonHang = 'Đã thanh toán'
          AND (p_TuNgay IS NULL OR TRUNC(d.ThoiGianDat) >= p_TuNgay)
          AND (p_DenNgay IS NULL OR TRUNC(d.ThoiGianDat) <= p_DenNgay);
    END IF;

    RETURN v_DoanhThu;
END fn_TinhDoanhThu;
/