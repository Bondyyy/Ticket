CREATE TABLE NGUOIDUNG (
    MaND                VARCHAR2(50) PRIMARY KEY,
    TenTaiKhoan         VARCHAR2(50)    UNIQUE,
    MatKhauMaHoa        VARCHAR2(255),
    AnhDaiDien          VARCHAR2(500),
    GioiTinh            VARCHAR2(10),
    Email               VARCHAR2(100)   UNIQUE,
    SDT                 VARCHAR2(20),
    NgaySinh            DATE,
    ThoiGianTao         TIMESTAMP       DEFAULT SYSTIMESTAMP,
    CapNhatLanCuoi      TIMESTAMP,
    LanCuoiDangNhap     TIMESTAMP,
    TrangThaiND         VARCHAR2(50)
);
