CREATE OR REPLACE FUNCTION fn_KiemTraGioVang (
    p_MaSK          IN VARCHAR2,
    p_NguongHangDoi IN NUMBER DEFAULT 1000  -- Số người đang chờ mua tối thiểu
) RETURN NUMBER IS
    v_SoHangDoi NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_SoHangDoi
    FROM HANGDOIAO
    WHERE MaSK = p_MaSK
      AND TrangThai = 'Đang chờ';

    IF v_SoHangDoi >= p_NguongHangDoi THEN
        RETURN 1; -- Giờ vàng
    END IF;
    RETURN 0;
END fn_KiemTraGioVang;
/

