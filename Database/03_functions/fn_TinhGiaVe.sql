CREATE OR REPLACE FUNCTION fn_TinhGiaVe (
    p_MaKhuVuc      IN VARCHAR2,
    p_MaKhachHang   IN VARCHAR2 DEFAULT NULL
) RETURN NUMBER IS
    v_GiaGoc        NUMBER(18,2);
    v_PhanTramGiam  NUMBER(5,2) := 0;
    v_GiaSauGiam    NUMBER(18,2);
BEGIN
    SELECT GiaVe INTO v_GiaGoc
    FROM KHUVUC WHERE MaKhuVuc = p_MaKhuVuc;

    IF p_MaKhachHang IS NOT NULL THEN
        BEGIN
            SELECT NVL(h.TyLeGiamGia, 0)
            INTO v_PhanTramGiam
            FROM KHACHHANG kh
            JOIN HANGTHANHVIEN h ON kh.MaHangThanhVien = h.MaHangThanhVien
            WHERE kh.MaKH = p_MaKhachHang;
        EXCEPTION WHEN NO_DATA_FOUND THEN v_PhanTramGiam := 0;
        END;
    END IF;

    v_GiaSauGiam := v_GiaGoc * (1 - v_PhanTramGiam / 100);
    RETURN v_GiaSauGiam;
END fn_TinhGiaVe;
/
