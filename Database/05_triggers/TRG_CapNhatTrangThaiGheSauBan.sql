CREATE OR REPLACE TRIGGER TRG_CapNhatTrangThaiGheSauBan
AFTER INSERT ON VE
FOR EACH ROW
BEGIN
    -- Cập nhật ghế sang "Đã bán"
    UPDATE GHENGOI
    SET TrangThaiGhe = 'Đã bán', ThoiGianKhoaTam = NULL, MaPhienKhoa = NULL
    WHERE MaGhe = :NEW.MaGhe;

    -- Cập nhật số ghế đã bán trong khu vực
    UPDATE KHUVUC
    SET SoGheDaBan = SoGheDaBan + 1
    WHERE MaKhuVuc = (SELECT MaKhuVuc FROM GHENGOI WHERE MaGhe = :NEW.MaGhe);

    -- Cập nhật tổng vé đã bán của sự kiện
    UPDATE SUKIEN
    SET SoVeDaBan = SoVeDaBan + 1, CapNhatLanCuoi = SYSTIMESTAMP
    WHERE MaSK = :NEW.MaSK;
END TRG_CapNhatTrangThaiGheSauBan;
/
