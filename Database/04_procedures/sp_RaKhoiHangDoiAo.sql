-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_RaKhoiHangDoiAo (
    p_MaSK      IN VARCHAR2,
    p_MaKH      OUT VARCHAR2,
    p_Token     OUT VARCHAR2
) AS
BEGIN
    SELECT MaKH, TokenHangDoi
    INTO p_MaKH, p_Token
    FROM (
        SELECT MaKH, TokenHangDoi
        FROM HANGDOIAO
        WHERE MaSK = p_MaSK AND TrangThai = 'Đang chờ'
        ORDER BY ViTriHang ASC
    )
    WHERE ROWNUM = 1;

    UPDATE HANGDOIAO
    SET TrangThai = 'Được vào', ThoiGianHetHan = SYSTIMESTAMP + INTERVAL '5' MINUTE
    WHERE TokenHangDoi = p_Token;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_MaKH := NULL; p_Token := NULL;
END sp_RaKhoiHangDoiAo;
/
