-- ============================================================
-- CUSTOM TYPES (tương tự t_space_booking_list từ workspace gốc)
-- ============================================================

-- 1. Kiểu đối tượng cho thông tin 1 vé trong đơn hàng
CREATE OR REPLACE TYPE t_chi_tiet_ve_rec AS OBJECT (
    ma_ghe          VARCHAR2(50),
    ma_khu_vuc      VARCHAR2(50),
    ten_khu_vuc     VARCHAR2(100),
    gia_ve          NUMBER,
    ghi_chu         VARCHAR2(255)
);
/

-- 2. Danh sách vé trong 1 đơn hàng
CREATE OR REPLACE TYPE t_chi_tiet_ve_list AS TABLE OF t_chi_tiet_ve_rec;
/

-- 3. Kiểu kết quả soát vé (dùng cho Mobile App)
CREATE OR REPLACE TYPE t_ket_qua_soat_ve AS OBJECT (
    ma_ve           VARCHAR2(50),
    ten_su_kien     VARCHAR2(300),
    ten_khu_vuc     VARCHAR2(100),
    ten_ghe         VARCHAR2(20),
    ho_ten_kh       VARCHAR2(100),
    ket_qua         VARCHAR2(50),
    thoi_gian_quet  TIMESTAMP
);
/
