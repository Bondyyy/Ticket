CREATE TABLE LICHSUGUI_EMAIL (
    MaEmail VARCHAR2(50) PRIMARY KEY,
    LoaiEmail VARCHAR2(50), -- 'QR_CODE' | 'XAC_NHAN_DON_HANG'
    DiaChiNhan VARCHAR2(100),
    TrangThai VARCHAR2(20), -- 'Da_gui' | 'That_bai' | 'Cho_gui'
    SoLanThu NUMBER DEFAULT 0,
    ThoiGianGui TIMESTAMP,
    MaVe VARCHAR2(50), 
    MaDonHang VARCHAR2(50)
);
/
