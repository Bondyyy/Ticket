-- ============================================================
-- DỮ LIỆU MẪU: Người dùng, Hạng thành viên & Khách hàng
-- (Tái sử dụng cấu trúc từ workspace-management-system)
-- ============================================================

-- Hạng thành viên (tái sử dụng logic từ workspace gốc)
INSERT INTO HANGTHANHVIEN (MaHangThanhVien, TenHang, TyLeGiamGia, ChiTieuToiThieu) VALUES ('HANG_DONG', 'Không có', 0, 0);
INSERT INTO HANGTHANHVIEN (MaHangThanhVien, TenHang, TyLeGiamGia, ChiTieuToiThieu) VALUES ('HANG_BẠNG', 'Đồng', 2, 1000000);
INSERT INTO HANGTHANHVIEN (MaHangThanhVien, TenHang, TyLeGiamGia, ChiTieuToiThieu) VALUES ('HANG_BAC', 'Bạc', 5, 5000000);
INSERT INTO HANGTHANHVIEN (MaHangThanhVien, TenHang, TyLeGiamGia, ChiTieuToiThieu) VALUES ('HANG_VANG', 'Vàng', 10, 20000000);
INSERT INTO HANGTHANHVIEN (MaHangThanhVien, TenHang, TyLeGiamGia, ChiTieuToiThieu) VALUES ('HANG_KIM', 'Kim cương', 20, 50000000);

-- Vai trò (tái sử dụng nguyên xi từ workspace gốc)
INSERT INTO VAITRO VALUES ('VT001', 'Quản trị viên', 'Toàn quyền hệ thống');
INSERT INTO VAITRO VALUES ('VT002', 'Ban tổ chức', 'Quản lý sự kiện, sơ đồ ghế, báo cáo');
INSERT INTO VAITRO VALUES ('VT003', 'Nhân viên soát vé', 'Soát vé tại cổng sự kiện');
INSERT INTO VAITRO VALUES ('VT004', 'Khách hàng', 'Mua vé trực tuyến');

-- Người dùng (tái sử dụng cấu trúc từ workspace gốc)
INSERT INTO NGUOIDUNG VALUES ('ND001', 'nguyen_an', 'hash_bcrypt_001', NULL, 'Nam', 'an.nguyen@gmail.com', '0901234567', TO_DATE('1995-05-20','YYYY-MM-DD'), SYSTIMESTAMP, NULL, NULL, 'Đang hoạt động');
INSERT INTO NGUOIDUNG VALUES ('ND002', 'tran_binh', 'hash_bcrypt_002', NULL, 'Nữ', 'binh.tran@yahoo.com', '0912345678', TO_DATE('1998-10-12','YYYY-MM-DD'), SYSTIMESTAMP, NULL, NULL, 'Đang hoạt động');
INSERT INTO NGUOIDUNG VALUES ('ND003', 'le_cuong', 'hash_bcrypt_003', NULL, 'Nam', 'cuong.le@outlook.com', '0923456789', TO_DATE('2000-01-15','YYYY-MM-DD'), SYSTIMESTAMP, NULL, NULL, 'Đang hoạt động');
INSERT INTO NGUOIDUNG VALUES ('ND004', 'admin_spring', 'hash_bcrypt_004', NULL, 'Nam', 'admin@dedee.vn', '0934567890', TO_DATE('1990-01-01','YYYY-MM-DD'), SYSTIMESTAMP, NULL, NULL, 'Đang hoạt động');
INSERT INTO NGUOIDUNG VALUES ('ND005', 'nv_soatve1', 'hash_bcrypt_005', NULL, 'Nữ', 'soatve1@dedee.vn', '0945678901', TO_DATE('2000-03-30','YYYY-MM-DD'), SYSTIMESTAMP, NULL, NULL, 'Đang hoạt động');

-- Khách hàng
INSERT INTO KHACHHANG VALUES ('KH001', 'Nguyễn Văn An', 0, SYSTIMESTAMP, 'HANG_DONG', 'ND001');
INSERT INTO KHACHHANG VALUES ('KH002', 'Trần Thị Bình', 5000000, SYSTIMESTAMP, 'HANG_BAC', 'ND002');
INSERT INTO KHACHHANG VALUES ('KH003', 'Lê Minh Cường', 500000, SYSTIMESTAMP, 'HANG_DONG', 'ND003');

-- Nhân viên
INSERT INTO NHANVIEN VALUES ('NV001', 'Quản lý', TO_DATE('2024-01-01','YYYY-MM-DD'), 'Đang làm việc', 15000000, 2000000, NULL, 'ND004');
INSERT INTO NHANVIEN VALUES ('NV002', 'Nhân viên soát vé', TO_DATE('2025-03-01','YYYY-MM-DD'), 'Đang làm việc', 8000000, 500000, 'NV001', 'ND005');

-- Phân quyền
INSERT INTO CHITIETVAITRO VALUES ('ND001', 'VT004');
INSERT INTO CHITIETVAITRO VALUES ('ND002', 'VT004');
INSERT INTO CHITIETVAITRO VALUES ('ND003', 'VT004');
INSERT INTO CHITIETVAITRO VALUES ('ND004', 'VT001');
INSERT INTO CHITIETVAITRO VALUES ('ND005', 'VT003');

COMMIT;
