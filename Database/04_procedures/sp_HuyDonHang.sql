-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_HuyDonHang (
    p_MaDonHang IN VARCHAR2,
    p_LyDo      IN VARCHAR2 DEFAULT NULL
) AS
BEGIN
    -- Vô hiệu hóa vé
    UPDATE VE SET TrangThaiVe = 'Đã hủy'
    WHERE MaDonHang = p_MaDonHang;

    -- Giải phóng ghế về trạng thái Trống
    UPDATE GHENGOI SET TrangThaiGhe = 'Trống', ThoiGianKhoaTam = NULL, MaPhienKhoa = NULL
    WHERE MaGhe IN (SELECT MaGhe FROM VE WHERE MaDonHang = p_MaDonHang);

    -- Cập nhật đơn hàng
    UPDATE DONHANG
    SET TrangThaiDonHang = 'Đã hủy', CapNhatLanCuoi = SYSTIMESTAMP
    WHERE MaDonHang = p_MaDonHang;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Đã hủy đơn hàng: ' || p_MaDonHang || ' - ' || NVL(p_LyDo, 'Không có lý do'));
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20400, 'sp_HuyDonHang thất bại: ' || SQLERRM);
END sp_HuyDonHang;
/
