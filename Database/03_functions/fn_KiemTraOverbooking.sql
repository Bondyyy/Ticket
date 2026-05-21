CREATE OR REPLACE FUNCTION fn_KiemTraOverbooking (
    p_MaSK      IN VARCHAR2,
    p_MaKhuVuc  IN VARCHAR2,
    p_SoLuong   IN NUMBER DEFAULT 1
) RETURN NUMBER IS
    v_ConTrong  NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_ConTrong
    FROM GHENGOI
    WHERE MaSK = p_MaSK
      AND MaKhuVuc = p_MaKhuVuc
      AND TrangThaiGhe = 'Trống';

    IF v_ConTrong < p_SoLuong THEN
        RETURN 1; -- Overbooking
    END IF;
    RETURN 0; -- OK
END fn_KiemTraOverbooking;
/