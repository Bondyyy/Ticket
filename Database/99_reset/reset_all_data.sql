-- Reset toàn bộ dữ liệu legacy/demo/runtime, giữ nguyên toàn bộ schema object.
-- Chạy trong Oracle SQL Developer bằng user/schema của ứng dụng.
-- Script chỉ dùng DELETE, không DROP TABLE/CONSTRAINT/INDEX/FUNCTION/PROCEDURE.
-- Nếu muốn app tự seed lại dữ liệu mẫu sạch sau khi reset, đặt app.seed.enabled=true rồi restart app.

SET DEFINE OFF;
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

PROMPT Dang reset TOAN BO du lieu trong schema Dề Dê Tickets...

-- Runtime / child tables trước.
DELETE FROM LICHSUSOATVE;
DELETE FROM LICHSUGUI_EMAIL;
DELETE FROM GIAODICHTHANHTOAN;
DELETE FROM VE;
DELETE FROM DONHANG;
DELETE FROM HANGDOIAO;
DELETE FROM LOG_HANH_VI;

-- Sự kiện / ghế.
DELETE FROM GHENGOI;
DELETE FROM KHUVUC;
DELETE FROM SUKIEN;

-- Voucher.
DELETE FROM PHIEUGIAMGIA;

-- Người dùng / nhân sự.
DELETE FROM KHACHHANG;
DELETE FROM NHANVIEN;

-- Phân quyền.
-- Trigger TRG_GhiNhatKyThayDoiQuyen ghi audit theo từng dòng bằng mã timestamp FF3.
-- Xóa từng dòng và dọn audit ngay sau đó để tránh trùng mã audit khi reset nhiều quyền cùng lúc.
BEGIN
    FOR r IN (SELECT ROWID AS rid FROM CHITIETVAITRO) LOOP
        DELETE FROM CHITIETVAITRO WHERE ROWID = r.rid;
        DELETE FROM NHATKYQUYENHANTRO;
    END LOOP;
END;
/

DELETE FROM CHITIETNHOMCHUCNANG;
DELETE FROM CHITIETCHUCNANG;
DELETE FROM NHATKYQUYENHANTRO;

DELETE FROM NGUOIDUNG;
DELETE FROM VAITRO;
DELETE FROM NHOMCHUCNANG;
DELETE FROM CHUCNANG;

-- Master data cuối cùng.
DELETE FROM DIADIEM;
DELETE FROM LOAISUKIEN;
DELETE FROM HANGTHANHVIEN;

COMMIT;

PROMPT Reset toàn bộ dữ liệu hoàn tất. Schema, constraint, index, function, procedure được giữ nguyên.
