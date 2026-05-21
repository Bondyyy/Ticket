-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_DongBoLichSuSoatVeOffline (
    p_MaNV      IN VARCHAR2,
    p_SoDongBo  OUT NUMBER
) AS
BEGIN
    p_SoDongBo := 0;

    FOR rec IN (SELECT * FROM LICHSUSOATVE WHERE MaNV = p_MaNV AND DaDongBo = 'N' ORDER BY ThoiGianQuet ASC) LOOP
        BEGIN
            -- Nếu là "Hợp lệ" nhưng vé đã được đánh dấu dùng rồi (conflict) → ghi đè kết quả
            IF rec.KetQuaQuet = 'Hợp lệ' THEN
                UPDATE VE SET TrangThaiVe = 'Đã sử dụng', ThoiGianSuDung = rec.ThoiGianQuet
                WHERE MaVe = rec.MaVe AND TrangThaiVe = 'Chưa sử dụng';
            END IF;

            -- Đánh dấu đã đồng bộ
            UPDATE LICHSUSOATVE
            SET DaDongBo = 'Y', ThoiGianDongBo = SYSTIMESTAMP
            WHERE MaLichSu = rec.MaLichSu;

            p_SoDongBo := p_SoDongBo + 1;
        EXCEPTION
            WHEN OTHERS THEN NULL; -- Bỏ qua lỗi từng bản ghi, tiếp tục
        END;
    END LOOP;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Đồng bộ thành công ' || p_SoDongBo || ' bản ghi.');
END sp_DongBoLichSuSoatVeOffline;
/
