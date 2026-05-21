-- Lịch sử soát vé tại cổng (ghi nhận mọi lần quét, kể cả offline)
CREATE TABLE LICHSUSOATVE (
    MaLichSu            VARCHAR2(50) PRIMARY KEY,
    ThoiGianQuet        TIMESTAMP,
    KetQuaQuet          VARCHAR2(50),   -- 'Hợp lệ' | 'Vé giả' | 'Vé đã sử dụng'
    CongSoat            VARCHAR2(50),
    NguonDuLieu         VARCHAR2(20),   -- 'Online' | 'Offline' (quét khi mất mạng)
    DaDongBo            VARCHAR2(1)     DEFAULT 'N',
    ThoiGianDongBo      TIMESTAMP,
    MaVe                VARCHAR2(50),
    MaNV                VARCHAR2(50)
);
