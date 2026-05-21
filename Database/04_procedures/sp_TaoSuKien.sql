-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_TaoSuKien (
    p_MaSK          IN VARCHAR2,
    p_TenSK         IN VARCHAR2,
    p_MoTa          IN CLOB,
    p_HinhAnh       IN VARCHAR2,
    p_ThoiGianBD    IN TIMESTAMP,
    p_ThoiGianKT    IN TIMESTAMP,
    p_ThoiGianMoBan IN TIMESTAMP,
    p_ThoiGianDongBan IN TIMESTAMP,
    p_MaLoaiSK      IN VARCHAR2,
    p_MaDiaDiem     IN VARCHAR2,
    p_MaNV          IN VARCHAR2
) AS
BEGIN
    INSERT INTO SUKIEN (
        MaSK, TenSK, MoTa, HinhAnh,
        ThoiGianBatDau, ThoiGianKetThuc,
        ThoiGianMoBan, ThoiGianDongBan,
        TrangThaiSK, ThoiGianTao,
        MaLoaiSK, MaDiaDiem, MaNV
    ) VALUES (
        p_MaSK, p_TenSK, p_MoTa, p_HinhAnh,
        p_ThoiGianBD, p_ThoiGianKT,
        p_ThoiGianMoBan, p_ThoiGianDongBan,
        'Chưa mở bán', SYSTIMESTAMP,
        p_MaLoaiSK, p_MaDiaDiem, p_MaNV
    );
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Tạo sự kiện thành công: ' || p_MaSK);
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20100, 'sp_TaoSuKien thất bại: ' || SQLERRM);
END sp_TaoSuKien;
/
