CREATE OR REPLACE TRIGGER TRG_GhiNhatKyThayDoiQuyen
AFTER INSERT OR DELETE ON CHITIETVAITRO
FOR EACH ROW
DECLARE
    v_MaNK  VARCHAR2(50);
    v_HD    VARCHAR2(50);
    v_MaND  VARCHAR2(50);
    v_MaVT  VARCHAR2(50);
BEGIN
    IF INSERTING THEN
        v_HD := 'GAN_QUYEN'; v_MaND := :NEW.MaND; v_MaVT := :NEW.MaVaiTro;
    ELSE
        v_HD := 'HUY_QUYEN'; v_MaND := :OLD.MaND; v_MaVT := :OLD.MaVaiTro;
    END IF;

    v_MaNK := 'NK_' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');
    INSERT INTO NHATKYQUYENHANTRO (MaNhatKy, HanhDong, MaVaiTroMoi, ThoiGianThucHien, MaND_ThucHien, MaND_BiThayDoi)
    VALUES (v_MaNK, v_HD, v_MaVT, SYSTIMESTAMP, pk_ctx.current_user_id, v_MaND);
END TRG_GhiNhatKyThayDoiQuyen;
/
