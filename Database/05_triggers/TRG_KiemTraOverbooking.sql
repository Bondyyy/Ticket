CREATE OR REPLACE TRIGGER TRG_KiemTraOverbooking
BEFORE INSERT ON VE
FOR EACH ROW
DECLARE
    v_SoGheDaBan    NUMBER;
    v_SoGheToiDa    NUMBER;
BEGIN
    SELECT SoGheDaBan, SoGheToiDa INTO v_SoGheDaBan, v_SoGheToiDa
    FROM KHUVUC
    WHERE MaKhuVuc = (SELECT MaKhuVuc FROM GHENGOI WHERE MaGhe = :NEW.MaGhe);

    IF v_SoGheDaBan >= v_SoGheToiDa THEN
        RAISE_APPLICATION_ERROR(-20001,
            'Lỗi: Khu vực đã hết vé! Không thể tạo thêm vé mới.');
    END IF;
END TRG_KiemTraOverbooking;
/
