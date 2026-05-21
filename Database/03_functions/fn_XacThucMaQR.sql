CREATE OR REPLACE FUNCTION fn_XacThucMaQR (
    p_MaQR  IN VARCHAR2
) RETURN VARCHAR2 IS
    v_TrangThai VARCHAR2(50);
BEGIN
    SELECT TrangThaiVe INTO v_TrangThai
    FROM VE
    WHERE MaQR = p_MaQR;

    IF v_TrangThai = 'Chưa sử dụng' THEN
        RETURN 'Hợp lệ';
    ELSIF v_TrangThai = 'Đã sử dụng' THEN
        RETURN 'Vé đã sử dụng';
    ELSE
        RETURN 'Vé đã hủy';
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN 'Vé giả';
END fn_XacThucMaQR;
/
