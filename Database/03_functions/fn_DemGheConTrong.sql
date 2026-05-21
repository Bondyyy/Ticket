CREATE OR REPLACE FUNCTION fn_DemGheConTrong (
    p_MaSK      IN VARCHAR2,
    p_MaKhuVuc  IN VARCHAR2 DEFAULT NULL
) RETURN NUMBER IS
    v_SoGhe NUMBER;
BEGIN
    IF p_MaKhuVuc IS NOT NULL THEN
        SELECT COUNT(*) INTO v_SoGhe
        FROM GHENGOI
        WHERE MaSK = p_MaSK
          AND MaKhuVuc = p_MaKhuVuc
          AND TrangThaiGhe = 'Trống';
    ELSE
        SELECT COUNT(*) INTO v_SoGhe
        FROM GHENGOI
        WHERE MaSK = p_MaSK
          AND TrangThaiGhe = 'Trống';
    END IF;
    RETURN v_SoGhe;
END fn_DemGheConTrong;
/
