-- Ghế ngồi chi tiết (hỗ trợ tối thiểu 50.000 ghế / sự kiện)
CREATE TABLE GHENGOI (
    MaGhe               VARCHAR2(50) PRIMARY KEY,
    TenGhe              VARCHAR2(20),       -- VD: A01, B15
    HangGhe             VARCHAR2(10),       -- VD: A, B, C
    CotGhe              NUMBER,             -- VD: 1, 2, 3
    TrangThaiGhe        VARCHAR2(50),       -- Trống / Đang chọn / Đã bán / Bảo trì
    ThoiGianKhoaTam     TIMESTAMP,          -- Thời điểm ghế bị khóa tạm (giữ chỗ)
    MaPhienKhoa         VARCHAR2(100),      -- Định danh phiên đang giữ ghế
    MaKhuVuc            VARCHAR2(50),
    MaSK                VARCHAR2(50)
);
