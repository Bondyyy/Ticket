-- Vé điện tử (mỗi ghế = 1 vé)
CREATE TABLE VE (
    MaVe                VARCHAR2(50) PRIMARY KEY,
    MaQR                VARCHAR2(500)   UNIQUE NOT NULL,
    GiaVe               NUMBER(18,2),
    TrangThaiVe         VARCHAR2(50),   -- 'Chưa sử dụng' | 'Đã sử dụng' | 'Đã hủy'
    ThoiGianPhat        TIMESTAMP,
    ThoiGianSuDung      TIMESTAMP,
    MaDonHang           VARCHAR2(50),
    MaGhe               VARCHAR2(50),
    MaSK                VARCHAR2(50)
);
