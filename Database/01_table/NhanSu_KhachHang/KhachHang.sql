CREATE TABLE KHACHHANG (
    MaKH                VARCHAR2(50) PRIMARY KEY,
    HoTenKH             VARCHAR2(100),
    TongChiTieu         NUMBER(18,2)    DEFAULT 0,
    CapNhatLanCuoi      TIMESTAMP,
    MaHangThanhVien     VARCHAR2(50),
    MaND                VARCHAR2(50),   UNIQUE(MaND)
);
