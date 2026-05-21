-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_GiaiPhongGheHetHan AS
    v_SoGhe NUMBER;
BEGIN
    UPDATE GHENGOI
    SET TrangThaiGhe    = 'Trống',
        ThoiGianKhoaTam = NULL,
        MaPhienKhoa     = NULL
    WHERE TrangThaiGhe = 'Đang chọn'
      AND ThoiGianKhoaTam < SYSTIMESTAMP;

    v_SoGhe := SQL%ROWCOUNT;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Đã giải phóng ' || v_SoGhe || ' ghế hết hạn.');
END sp_GiaiPhongGheHetHan;
/
