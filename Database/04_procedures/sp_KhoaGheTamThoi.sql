-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_KhoaGheTamThoi (
    p_MaGhe         IN VARCHAR2,
    p_MaPhienKhoa   IN VARCHAR2,    -- Session ID của người dùng
    p_ThoiGianGiu   IN NUMBER DEFAULT 600  -- Giây giữ chỗ (mặc định 10 phút)
) AS
    v_TrangThai VARCHAR2(50);
BEGIN
    -- Kiểm tra và khóa ghế trong 1 statement atomic (SELECT FOR UPDATE)
    SELECT TrangThaiGhe INTO v_TrangThai
    FROM GHENGOI WHERE MaGhe = p_MaGhe FOR UPDATE NOWAIT;

    IF v_TrangThai != 'Trống' THEN
        RAISE_APPLICATION_ERROR(-20200, 'Ghế ' || p_MaGhe || ' không còn trống!');
    END IF;

    UPDATE GHENGOI
    SET TrangThaiGhe    = 'Đang chọn',
        ThoiGianKhoaTam = SYSTIMESTAMP + NUMTODSINTERVAL(p_ThoiGianGiu, 'SECOND'),
        MaPhienKhoa     = p_MaPhienKhoa
    WHERE MaGhe = p_MaGhe;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20201, 'Ghế không tồn tại: ' || p_MaGhe);
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_KhoaGheTamThoi;
/
