-- ============================================================
-- DỮ LIỆU MẪU: Địa điểm, Loại sự kiện & Sự kiện
-- ============================================================

-- Địa điểm
INSERT INTO DIADIEM VALUES ('DD001', 'Nhà thi đấu Phú Thọ', '01 Lữ Gia, Phường 15, Quận 11', 'TP.HCM', 8000, 'Nhà thi đấu đa năng', 'Đang hoạt động');
INSERT INTO DIADIEM VALUES ('DD002', 'GEM Center', '8 Nguyễn Bỉnh Khiêm, Quận 1', 'TP.HCM', 5000, 'Trung tâm hội nghị cao cấp', 'Đang hoạt động');
INSERT INTO DIADIEM VALUES ('DD003', 'Hội trường UIT', 'Khu phố 6, Thủ Đức', 'TP.HCM', 1200, 'Hội trường Đại học CNTT', 'Đang hoạt động');

-- Loại sự kiện
INSERT INTO LOAISUKIEN VALUES ('LSK001', 'Concert', 'Sự kiện âm nhạc trực tiếp');
INSERT INTO LOAISUKIEN VALUES ('LSK002', 'Hội thảo', 'Hội thảo chuyên ngành');
INSERT INTO LOAISUKIEN VALUES ('LSK003', 'Workshop', 'Lớp học thực hành');

-- Sự kiện
INSERT INTO SUKIEN (MaSK, TenSK, MoTa, ThoiGianBatDau, ThoiGianKetThuc, ThoiGianMoBan, ThoiGianDongBan, TrangThaiSK, MaLoaiSK, MaDiaDiem)
VALUES ('SK001', 'Dề Dê Summer Concert 2026', 'Đêm nhạc hoành tráng mùa hè 2026', 
        TO_TIMESTAMP('2026-07-15 19:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_TIMESTAMP('2026-07-15 23:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_TIMESTAMP('2026-05-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_TIMESTAMP('2026-07-14 23:59:59', 'YYYY-MM-DD HH24:MI:SS'),
        'Chưa mở bán', 'LSK001', 'DD001');

INSERT INTO SUKIEN (MaSK, TenSK, MoTa, ThoiGianBatDau, ThoiGianKetThuc, ThoiGianMoBan, ThoiGianDongBan, TrangThaiSK, MaLoaiSK, MaDiaDiem)
VALUES ('SK002', 'Tech Conference 2026', 'Hội thảo công nghệ hàng đầu Việt Nam',
        TO_TIMESTAMP('2026-08-20 08:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_TIMESTAMP('2026-08-20 17:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_TIMESTAMP('2026-06-01 09:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_TIMESTAMP('2026-08-19 23:59:59', 'YYYY-MM-DD HH24:MI:SS'),
        'Chưa mở bán', 'LSK002', 'DD002');

-- Khu vực ghế cho SK001
INSERT INTO KHUVUC VALUES ('KV001_VIP', 'Khu VIP', '#FFD700', 500, 0, 2000000, 'Đang bán', 'SK001');
INSERT INTO KHUVUC VALUES ('KV001_A', 'Khu A', '#FF6B6B', 2000, 0, 800000, 'Đang bán', 'SK001');
INSERT INTO KHUVUC VALUES ('KV001_B', 'Khu B', '#4ECDC4', 3000, 0, 500000, 'Đang bán', 'SK001');
INSERT INTO KHUVUC VALUES ('KV001_C', 'Khu C (Đứng)', '#95E1D3', 2500, 0, 300000, 'Đang bán', 'SK001');

COMMIT;
