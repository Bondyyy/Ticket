CREATE OR REPLACE TRIGGER TRG_PhatVeSauThanhToanThanhCong
AFTER UPDATE OF TrangThaiGD ON GIAODICHTHANHTOAN
FOR EACH ROW
WHEN (NEW.TrangThaiGD = 'Thành công' AND OLD.TrangThaiGD != 'Thành công')
BEGIN
    -- Cập nhật thời gian phát vé cho tất cả vé trong đơn hàng
    UPDATE VE
    SET ThoiGianPhat = SYSTIMESTAMP
    WHERE MaDonHang = :NEW.MaDonHang
      AND TrangThaiVe = 'Chưa sử dụng';

    -- Giảm số lượng đã dùng của phiếu giảm giá (nếu có)
    UPDATE PHIEUGIAMGIA
    SET SLDaDung = SLDaDung + 1
    WHERE MaPGG = (SELECT MaPGG FROM DONHANG WHERE MaDonHang = :NEW.MaDonHang AND MaPGG IS NOT NULL);
END TRG_PhatVeSauThanhToanThanhCong;
/
