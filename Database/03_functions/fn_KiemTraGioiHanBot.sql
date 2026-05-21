CREATE OR REPLACE FUNCTION fn_KiemTraGioiHanBot (
    p_MaKH          IN VARCHAR2,
    p_NguongRequest IN NUMBER DEFAULT 20  -- Tối đa 20 lần đặt vé / 60 giây
) RETURN NUMBER IS
    v_SoRequest NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_SoRequest
    FROM DONHANG
    WHERE MaKH = p_MaKH
      AND ThoiGianDat >= SYSTIMESTAMP - INTERVAL '60' SECOND;

    IF v_SoRequest >= p_NguongRequest THEN
        RETURN 1; -- Bot
    END IF;
    RETURN 0;
END fn_KiemTraGioiHanBot;
/
