CREATE OR REPLACE FUNCTION fn_TinhTyLeChuyenDoi (
    p_MaSK IN VARCHAR2
) RETURN NUMBER IS
    v_TongMuaThanhCong  NUMBER;
    v_TongVe            NUMBER;
BEGIN
    SELECT NVL(SoVeDaBan, 0), NVL(TongSoVe, 0)
    INTO v_TongMuaThanhCong, v_TongVe
    FROM SUKIEN WHERE MaSK = p_MaSK;

    IF v_TongVe = 0 THEN RETURN 0; END IF;
    RETURN ROUND((v_TongMuaThanhCong / v_TongVe) * 100, 2);
END fn_TinhTyLeChuyenDoi;
/