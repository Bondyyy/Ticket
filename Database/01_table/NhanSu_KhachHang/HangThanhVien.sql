CREATE TABLE HANGTHANHVIEN (
    MaHangThanhVien     VARCHAR2(50) PRIMARY KEY,
    TenHang             VARCHAR2(100) NOT NULL,           
    ChiTieuToiThieu     NUMBER(18,2) DEFAULT 0,           
    TyLeGiamGia         NUMBER(5,2)  DEFAULT 0,          
    GioiHanVeMua        NUMBER       DEFAULT 4,          
    MucDoUuTienHangDoi  NUMBER       DEFAULT 0,           
    TrangThai           VARCHAR2(50) DEFAULT 'Hoạt động', 
    ThoiGianTao         TIMESTAMP    DEFAULT SYSTIMESTAMP,
    CapNhatLanCuoi      TIMESTAMP,
    MaNV_ThietLap       VARCHAR2(50)                 
);
