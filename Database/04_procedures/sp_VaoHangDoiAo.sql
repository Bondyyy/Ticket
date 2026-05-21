-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_VaoHangDoiAo (
    p_MaKH      IN VARCHAR2,
    p_MaSK      IN VARCHAR2,
    p_ViTri     OUT NUMBER,
    p_Token     OUT VARCHAR2
) AS
    v_MaHD  VARCHAR2(50);
BEGIN
    -- Lấy số thứ tự tiếp theo
    SELECT NVL(MAX(ViTriHang), 0) + 1 INTO p_ViTri
    FROM HANGDOIAO WHERE MaSK = p_MaSK AND TrangThai = 'Đang chờ';

    v_MaHD  := 'HD_' || p_MaSK || '_' || TO_CHAR(SYSTIMESTAMP, 'FF6');
    p_Token := fn_SinhMaQRDuyNhat(v_MaHD, p_MaSK);  -- Tái dùng hàm sinh token duy nhất

    INSERT INTO HANGDOIAO (MaHangDoi, ViTriHang, ThoiGianVaoHang, TrangThai, TokenHangDoi,
                           ThoiGianHetHan, MaKH, MaSK)
    VALUES (v_MaHD, p_ViTri, SYSTIMESTAMP, 'Đang chờ', p_Token,
            SYSTIMESTAMP + INTERVAL '30' MINUTE, p_MaKH, p_MaSK);

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20600, 'sp_VaoHangDoiAo thất bại: ' || SQLERRM);
END sp_VaoHangDoiAo;
/
