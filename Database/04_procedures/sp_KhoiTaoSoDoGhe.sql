-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_KhoiTaoSoDoGhe (
    p_MaSK      IN VARCHAR2,
    p_MaKhuVuc  IN VARCHAR2,
    p_SoHang    IN NUMBER,      -- Số hàng ghế (VD: 26 hàng A-Z)
    p_SoCotMoiHang IN NUMBER    -- Số cột / hàng (VD: 50 ghế)
) AS
    v_MaGhe     VARCHAR2(50);
    v_TenGhe    VARCHAR2(20);
    v_HangGhe   VARCHAR2(10);
    v_CotGhe    NUMBER;
BEGIN
    FOR hang IN 1 .. p_SoHang LOOP
        v_HangGhe := CHR(64 + hang);  -- A=65, B=66...
        FOR cot IN 1 .. p_SoCotMoiHang LOOP
            v_TenGhe := v_HangGhe || LPAD(cot, 2, '0');
            v_MaGhe  := p_MaKhuVuc || '_' || v_TenGhe;
            INSERT INTO GHENGOI (MaGhe, TenGhe, HangGhe, CotGhe, TrangThaiGhe, MaKhuVuc, MaSK)
            VALUES (v_MaGhe, v_TenGhe, v_HangGhe, cot, 'Trống', p_MaKhuVuc, p_MaSK);
        END LOOP;
    END LOOP;

    -- Cập nhật SoGheToiDa của khu vực
    UPDATE KHUVUC
    SET SoGheToiDa = p_SoHang * p_SoCotMoiHang
    WHERE MaKhuVuc = p_MaKhuVuc;

    -- Cập nhật TongSoVe của sự kiện
    UPDATE SUKIEN
    SET TongSoVe = (SELECT NVL(SUM(SoGheToiDa), 0) FROM KHUVUC WHERE MaSK = p_MaSK)
    WHERE MaSK = p_MaSK;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20101, 'sp_KhoiTaoSoDoGhe thất bại: ' || SQLERRM);
END sp_KhoiTaoSoDoGhe;
/
