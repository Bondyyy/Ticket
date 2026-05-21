CREATE TABLE HANGDOIAO (
    MaHangDoi           VARCHAR2(50) PRIMARY KEY,
    ViTriHang           NUMBER,
    ThoiGianVaoHang     TIMESTAMP       DEFAULT SYSTIMESTAMP,
    ThoiGianUocTinh     TIMESTAMP,
    TrangThai           VARCHAR2(50),   -- 'Đang chờ' | 'Được vào' | 'Hết hạn'
    TokenHangDoi        VARCHAR2(200)   UNIQUE,
    ThoiGianHetHan      TIMESTAMP,
    MaKH                VARCHAR2(50),
    MaSK                VARCHAR2(50)
);
