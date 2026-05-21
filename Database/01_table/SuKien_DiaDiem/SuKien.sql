-- Sự kiện chính (Concert, Hội thảo, Workshop...)
CREATE TABLE SUKIEN (
    MaSK                VARCHAR2(50) PRIMARY KEY,
    TenSK               VARCHAR2(300),
    MoTa                CLOB,
    HinhAnh             VARCHAR2(1000),
    HinhAnhThumb        VARCHAR2(1000),
    MoTaNgan            VARCHAR2(500),
    Tags                VARCHAR2(500),
    ThoiGianBatDau      TIMESTAMP,
    ThoiGianKetThuc     TIMESTAMP,
    ThoiGianMoBan       TIMESTAMP,
    ThoiGianDongBan     TIMESTAMP,
    TongSoVe            NUMBER          DEFAULT 0,
    SoVeDaBan           NUMBER          DEFAULT 0,
    TrangThaiSK         VARCHAR2(50),
    ThoiGianTao         TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CapNhatLanCuoi      TIMESTAMP,
    MaLoaiSK            VARCHAR2(50),
    MaDiaDiem           VARCHAR2(50),
    MaNV                VARCHAR2(50)    -- Nhân viên phụ trách tạo sự kiện
);
