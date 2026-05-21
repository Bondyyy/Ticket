-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_XacThucVeTaiCong (
    p_MaQR      IN VARCHAR2,
    p_MaNV      IN VARCHAR2,
    p_CongSoat  IN VARCHAR2,
    p_NguonDL   IN VARCHAR2 DEFAULT 'Online',   -- 'Online' | 'Offline'
    p_KetQua    OUT VARCHAR2,
    p_ThongTin  OUT VARCHAR2
) AS
    v_MaVe      VARCHAR2(50);
    v_KetQua    VARCHAR2(50);
    v_MaLS      VARCHAR2(50);
BEGIN
    -- Bước 1: Xác thực chữ ký QR
    v_KetQua := fn_XacThucMaQR(p_MaQR);
    p_KetQua := v_KetQua;

    IF v_KetQua = 'Hợp lệ' THEN
        -- Lấy mã vé
        SELECT MaVe INTO v_MaVe FROM VE WHERE MaQR = p_MaQR;

        -- Đánh dấu vé đã sử dụng
        UPDATE VE SET TrangThaiVe = 'Đã sử dụng', ThoiGianSuDung = SYSTIMESTAMP
        WHERE MaVe = v_MaVe;

        p_ThongTin := 'Vé hợp lệ - Cho vào!';
    ELSIF v_KetQua = 'Vé đã sử dụng' THEN
        SELECT MaVe INTO v_MaVe FROM VE WHERE MaQR = p_MaQR;
        p_ThongTin := 'Vé này đã được quét trước đó. Từ chối vào!';
    ELSE
        v_MaVe := NULL;
        p_ThongTin := 'Mã QR không hợp lệ hoặc vé giả!';
    END IF;

    -- Bước 2: Ghi lịch sử soát vé
    v_MaLS := 'LS_' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');
    INSERT INTO LICHSUSOATVE (MaLichSu, ThoiGianQuet, KetQuaQuet, CongSoat, NguonDuLieu, DaDongBo, MaVe, MaNV)
    VALUES (v_MaLS, SYSTIMESTAMP, v_KetQua, p_CongSoat, p_NguonDL,
            CASE WHEN p_NguonDL = 'Offline' THEN 'N' ELSE 'Y' END,
            v_MaVe, p_MaNV);

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20500, 'sp_XacThucVeTaiCong thất bại: ' || SQLERRM);
END sp_XacThucVeTaiCong;
/
