CREATE OR REPLACE TRIGGER TRG_ChanThanhToanDonHangHetHan
BEFORE INSERT ON GIAODICHTHANHTOAN
FOR EACH ROW
DECLARE
    v_TrangThai     VARCHAR2(50);
    v_ThoiGianHetHan TIMESTAMP;
BEGIN
    SELECT TrangThaiDonHang, ThoiGianHetHan
    INTO v_TrangThai, v_ThoiGianHetHan
    FROM DONHANG WHERE MaDonHang = :NEW.MaDonHang;

    IF v_TrangThai != 'Chờ thanh toán' THEN
        RAISE_APPLICATION_ERROR(-20003,
            'Lỗi: Đơn hàng này không ở trạng thái chờ thanh toán (Trạng thái hiện tại: '
            || v_TrangThai || ')');
    END IF;

    IF SYSTIMESTAMP > v_ThoiGianHetHan THEN
        RAISE_APPLICATION_ERROR(-20004,
            'Lỗi: Đơn hàng đã hết hạn giữ vé. Vui lòng đặt lại từ đầu!');
    END IF;
END TRG_ChanThanhToanDonHangHetHan;
/
