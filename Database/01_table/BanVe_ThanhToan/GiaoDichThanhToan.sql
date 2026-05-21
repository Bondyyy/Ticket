CREATE TABLE GIAODICHTHANHTOAN (
    MaGiaoDich          VARCHAR2(50) PRIMARY KEY,
    SoTienThanhToan     NUMBER(18,2),
    PhuongThucTT        VARCHAR2(50),   -- 'Chuyển khoản' | 'Thẻ tín dụng' | 'Ví điện tử'
    TrangThaiGD         VARCHAR2(50),   -- 'Thành công' | 'Thất bại' | 'Đang xử lý'
    MaGiaoDichBenThu3   VARCHAR2(200),
    LanThuLai           NUMBER          DEFAULT 0,
    ThoiGianThucHien    TIMESTAMP       DEFAULT SYSTIMESTAMP,
    GhiChuLoi           VARCHAR2(1000),
    MaDonHang           VARCHAR2(50)
);
