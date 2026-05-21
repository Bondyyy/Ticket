-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_XuatBaoCaoDoanhThu (
    p_MaSK      IN VARCHAR2 DEFAULT NULL,
    p_TuNgay    IN DATE     DEFAULT NULL,
    p_DenNgay   IN DATE     DEFAULT NULL
) AS
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== BÁO CÁO DOANH THU ===');
    DBMS_OUTPUT.PUT_LINE('Tổng doanh thu: ' ||
        fn_TinhDoanhThu(p_MaSK, p_TuNgay, p_DenNgay) || ' VNĐ');

    FOR rec IN (
        SELECT s.TenSK, COUNT(v.MaVe) AS SoVe,
               SUM(v.GiaVe) AS DoanhThu,
               fn_TinhTyLeChuyenDoi(s.MaSK) AS TyLeCD
        FROM SUKIEN s
        LEFT JOIN VE v ON v.MaSK = s.MaSK AND v.TrangThaiVe = 'Đã sử dụng'
        WHERE (p_MaSK IS NULL OR s.MaSK = p_MaSK)
        GROUP BY s.MaSK, s.TenSK
    ) LOOP
        DBMS_OUTPUT.PUT_LINE('SK: ' || rec.TenSK || ' | Vé bán: ' || rec.SoVe
            || ' | DT: ' || NVL(rec.DoanhThu,0) || ' | Tỉ lệ CD: ' || rec.TyLeCD || '%');
    END LOOP;
END sp_XuatBaoCaoDoanhThu;
/
