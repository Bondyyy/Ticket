-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_ThuLaiThanhToan (
    p_MaDonHang     IN VARCHAR2,
    p_SoTien        IN NUMBER,
    p_PhuongThuc    IN VARCHAR2,
    p_KetQua        OUT VARCHAR2,
    p_SoLanToiDa    IN NUMBER DEFAULT 3
) AS
    v_Lan   NUMBER := 0;
    v_MaGD  VARCHAR2(50);
BEGIN
    WHILE v_Lan < p_SoLanToiDa LOOP
        v_Lan := v_Lan + 1;
        v_MaGD := 'GD_RETRY' || v_Lan || '_' || TO_CHAR(SYSTIMESTAMP, 'FF3');

        BEGIN
            INSERT INTO GIAODICHTHANHTOAN (MaGiaoDich, SoTienThanhToan, PhuongThucTT, TrangThaiGD, LanThuLai, MaDonHang)
            VALUES (v_MaGD, p_SoTien, p_PhuongThuc, 'Đang xử lý', v_Lan, p_MaDonHang);

            -- [Gọi Gateway thực tế ở đây]
            UPDATE GIAODICHTHANHTOAN SET TrangThaiGD = 'Thành công' WHERE MaGiaoDich = v_MaGD;
            UPDATE DONHANG SET TrangThaiDonHang = 'Đã thanh toán', CapNhatLanCuoi = SYSTIMESTAMP
            WHERE MaDonHang = p_MaDonHang;

            p_KetQua := 'Thành công';
            COMMIT;
            RETURN;
        EXCEPTION
            WHEN OTHERS THEN
                UPDATE GIAODICHTHANHTOAN
                SET TrangThaiGD = 'Thất bại', GhiChuLoi = SQLERRM
                WHERE MaGiaoDich = v_MaGD;
                -- Chờ trước khi retry (1s, 3s, 9s)
                DBMS_LOCK.SLEEP(POWER(3, v_Lan - 1));
        END;
    END LOOP;

    -- Hết lần retry: hủy đơn hàng, giải phóng ghế
    sp_HuyDonHang(p_MaDonHang, 'Hết lần thử lại thanh toán');
    p_KetQua := 'Thất bại sau ' || p_SoLanToiDa || ' lần thử';
END sp_ThuLaiThanhToan;
/
