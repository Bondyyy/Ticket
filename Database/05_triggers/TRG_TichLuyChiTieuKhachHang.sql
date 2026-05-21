CREATE OR REPLACE TRIGGER TRG_TichLuyChiTieuKhachHang
AFTER INSERT OR UPDATE ON GIAODICHTHANHTOAN
FOR EACH ROW
DECLARE
    v_MaKH VARCHAR2(50);
BEGIN
    IF :NEW.TrangThaiGD = 'Thành công' AND
       (INSERTING OR (UPDATING AND NVL(:OLD.TrangThaiGD, '') != 'Thành công')) THEN

        BEGIN
            SELECT MaKH INTO v_MaKH FROM DONHANG WHERE MaDonHang = :NEW.MaDonHang;

            IF v_MaKH IS NOT NULL THEN
                UPDATE KHACHHANG
                SET TongChiTieu = NVL(TongChiTieu, 0) + :NEW.SoTienThanhToan,
                    CapNhatLanCuoi = SYSTIMESTAMP
                WHERE MaKH = v_MaKH;
            END IF;
        EXCEPTION WHEN NO_DATA_FOUND THEN NULL;
        END;
    END IF;
END TRG_TichLuyChiTieuKhachHang;
/
