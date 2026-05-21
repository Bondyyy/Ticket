-- Khu vực / hạng vé trong sơ đồ ghế động
CREATE TABLE KHUVUC (
    MaKhuVuc        VARCHAR2(50) PRIMARY KEY,
    TenKhuVuc       VARCHAR2(100),
    MauSacHienThi   VARCHAR2(20),
    SoGheToiDa      NUMBER,
    SoGheDaBan      NUMBER          DEFAULT 0,
    SoVeToiDaPerKH  NUMBER          DEFAULT 4,
    GiaVe           NUMBER(18,2),
    TrangThai       VARCHAR2(50),
    MaSK            VARCHAR2(50)
);
