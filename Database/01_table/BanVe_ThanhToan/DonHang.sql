CREATE TABLE DONHANG (
    MaDonHang           VARCHAR2(50) PRIMARY KEY,
    SoDonHang           VARCHAR2(50)    UNIQUE,
    TongTien            NUMBER(18,2),
    ThanhTien           NUMBER(18,2),
    TrangThaiDonHang    VARCHAR2(50),   -- 'Chờ thanh toán' | 'Đã thanh toán' | 'Đã hủy'
    ThoiGianDat         TIMESTAMP       DEFAULT SYSTIMESTAMP,
    ThoiGianHetHan      TIMESTAMP,
    CapNhatLanCuoi      TIMESTAMP,
    MaKH                VARCHAR2(50),
    MaPGG               VARCHAR2(50),
    MaNV                VARCHAR2(50)
);
