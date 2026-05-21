-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_TaoTaiKhoanNguoiDung (
    p_MaND          IN VARCHAR2,
    p_TenTaiKhoan   IN VARCHAR2,
    p_MatKhauHash   IN VARCHAR2,
    p_Email         IN VARCHAR2,
    p_SDT           IN VARCHAR2,
    p_LoaiND        IN VARCHAR2,    -- 'KhachHang' | 'NhanVien'
    p_MaVaiTro      IN VARCHAR2
) AS
BEGIN
    INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, Email, SDT, ThoiGianTao, TrangThaiND)
    VALUES (p_MaND, p_TenTaiKhoan, p_MatKhauHash, p_Email, p_SDT, SYSTIMESTAMP, 'Đang hoạt động');

    -- Gán vai trò
    INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (p_MaND, p_MaVaiTro);

    -- Tạo hồ sơ khách hàng nếu là khách hàng
    IF p_LoaiND = 'KhachHang' THEN
        INSERT INTO KHACHHANG (MaKH, HoTenKH, TongChiTieu, CapNhatLanCuoi, MaHangThanhVien, MaND)
        VALUES ('KH_' || p_MaND, p_TenTaiKhoan, 0, SYSTIMESTAMP, NULL, p_MaND);
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20700, 'sp_TaoTaiKhoanNguoiDung thất bại: ' || SQLERRM);
END sp_TaoTaiKhoanNguoiDung;
/
