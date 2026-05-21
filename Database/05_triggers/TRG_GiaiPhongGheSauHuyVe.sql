CREATE OR REPLACE TRIGGER TRG_GiaiPhongGheSauHuyVe
AFTER UPDATE OF TrangThaiVe ON VE
FOR EACH ROW
WHEN (NEW.TrangThaiVe = 'Đã hủy' AND OLD.TrangThaiVe != 'Đã hủy')
BEGIN
    UPDATE GHENGOI
    SET TrangThaiGhe = 'Trống', ThoiGianKhoaTam = NULL, MaPhienKhoa = NULL
    WHERE MaGhe = :NEW.MaGhe;

    UPDATE KHUVUC
    SET SoGheDaBan = GREATEST(SoGheDaBan - 1, 0)
    WHERE MaKhuVuc = (SELECT MaKhuVuc FROM GHENGOI WHERE MaGhe = :NEW.MaGhe);

    UPDATE SUKIEN
    SET SoVeDaBan = GREATEST(SoVeDaBan - 1, 0), CapNhatLanCuoi = SYSTIMESTAMP
    WHERE MaSK = :NEW.MaSK;
END TRG_GiaiPhongGheSauHuyVe;
/
