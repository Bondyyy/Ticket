-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_PhanQuyenVaiTro (
    p_MaND_ThucHien  IN VARCHAR2,
    p_MaND_DoiTuong  IN VARCHAR2,
    p_MaVaiTroMoi    IN VARCHAR2,
    p_HanhDong       IN VARCHAR2    -- 'GAN_QUYEN' | 'HUY_QUYEN'
) AS
    v_MaVaiTroCu VARCHAR2(50);
    v_MaNK       VARCHAR2(50);
BEGIN
    -- Lấy vai trò cũ
    BEGIN
        SELECT MaVaiTro INTO v_MaVaiTroCu FROM CHITIETVAITRO WHERE MaND = p_MaND_DoiTuong AND ROWNUM = 1;
    EXCEPTION WHEN NO_DATA_FOUND THEN v_MaVaiTroCu := NULL;
    END;

    IF p_HanhDong = 'GAN_QUYEN' THEN
        MERGE INTO CHITIETVAITRO USING DUAL
        ON (MaND = p_MaND_DoiTuong AND MaVaiTro = p_MaVaiTroMoi)
        WHEN NOT MATCHED THEN INSERT (MaND, MaVaiTro) VALUES (p_MaND_DoiTuong, p_MaVaiTroMoi);
    ELSIF p_HanhDong = 'HUY_QUYEN' THEN
        DELETE FROM CHITIETVAITRO WHERE MaND = p_MaND_DoiTuong AND MaVaiTro = p_MaVaiTroMoi;
    END IF;

    -- Ghi audit trail
    v_MaNK := 'NK_' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');
    INSERT INTO NHATKYQUYENHANTRO (MaNhatKy, HanhDong, MaVaiTroCu, MaVaiTroMoi,
                                    ThoiGianThucHien, MaND_ThucHien, MaND_BiThayDoi)
    VALUES (v_MaNK, p_HanhDong, v_MaVaiTroCu, p_MaVaiTroMoi,
            SYSTIMESTAMP, p_MaND_ThucHien, p_MaND_DoiTuong);

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20800, 'sp_PhanQuyenVaiTro thất bại: ' || SQLERRM);
END sp_PhanQuyenVaiTro;
/
