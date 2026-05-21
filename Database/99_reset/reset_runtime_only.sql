-- Reset dữ liệu phát sinh khi demo, giữ user/role/sự kiện/khu vực/ghế.
-- Chạy trong Oracle SQL Developer bằng user/schema của ứng dụng.
-- Script chỉ dùng DELETE/UPDATE, không DROP TABLE/CONSTRAINT/INDEX/FUNCTION/PROCEDURE.

SET DEFINE OFF;
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

PROMPT Dang reset du lieu runtime/demo trong schema Dề Dê Tickets...

DELETE FROM LICHSUSOATVE;
DELETE FROM LICHSUGUI_EMAIL;
DELETE FROM GIAODICHTHANHTOAN;
DELETE FROM VE;
DELETE FROM DONHANG;
DELETE FROM HANGDOIAO;
DELETE FROM LOG_HANH_VI;

UPDATE GHENGOI
SET TrangThaiGhe = 'Trống',
    ThoiGianKhoaTam = NULL,
    MaPhienKhoa = NULL;

UPDATE KHUVUC
SET SoGheDaBan = 0;

UPDATE SUKIEN
SET SoVeDaBan = 0;

COMMIT;

PROMPT Reset runtime/demo hoàn tất. User, role, sự kiện, khu vực và ghế được giữ lại.
