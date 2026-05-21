-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_XuLyThanhToan (
    p_MaDonHang     IN VARCHAR2,
    p_SoTien        IN NUMBER,
    p_PhuongThuc    IN VARCHAR2,
    p_KetQua        OUT VARCHAR2
) AS
    v_MaGD      VARCHAR2(50);
BEGIN
    v_MaGD := 'GD_' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');

    -- Ghi log giao dịch
    INSERT INTO GIAODICHTHANHTOAN (MaGiaoDich, SoTienThanhToan, PhuongThucTT, TrangThaiGD, LanThuLai, MaDonHang)
    VALUES (v_MaGD, p_SoTien, p_PhuongThuc, 'Đang xử lý', 0, p_MaDonHang);

    -- [Tích hợp Payment Gateway thực tế qua UTL_HTTP / Java Stored Proc]
    -- Giả lập: cập nhật thành công
    UPDATE GIAODICHTHANHTOAN
    SET TrangThaiGD = 'Thành công'
    WHERE MaGiaoDich = v_MaGD;

    UPDATE DONHANG
    SET TrangThaiDonHang = 'Đã thanh toán', CapNhatLanCuoi = SYSTIMESTAMP
    WHERE MaDonHang = p_MaDonHang;

    p_KetQua := 'Thành công';
EXCEPTION
    WHEN OTHERS THEN
        UPDATE GIAODICHTHANHTOAN
        SET TrangThaiGD = 'Thất bại', GhiChuLoi = SQLERRM
        WHERE MaGiaoDich = v_MaGD;
        p_KetQua := 'Thất bại';
        -- Gọi retry
        sp_ThuLaiThanhToan(p_MaDonHang, p_SoTien, p_PhuongThuc, p_KetQua);
END sp_XuLyThanhToan;
/
