CREATE OR REPLACE TRIGGER TRG_ChanVeTrungMaQR
BEFORE INSERT ON VE
FOR EACH ROW
DECLARE
    v_Count NUMBER;
BEGIN
    IF :NEW.MaQR IS NOT NULL THEN
        SELECT COUNT(*) INTO v_Count FROM VE WHERE MaQR = :NEW.MaQR;
        IF v_Count > 0 THEN
            RAISE_APPLICATION_ERROR(-20002,
                'Lỗi nghiêm trọng: Mã QR đã tồn tại trong hệ thống! Vé không được phép trùng mã.');
        END IF;
    END IF;
END TRG_ChanVeTrungMaQR;
/
