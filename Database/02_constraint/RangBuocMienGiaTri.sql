-- 1. Bảng NGUOIDUNG (tái sử dụng từ workspace-management-system)
ALTER TABLE NGUOIDUNG ADD CONSTRAINT CHK_ND_GioiTinh
    CHECK (GioiTinh IN ('Nam', 'Nữ', 'Khác'));

ALTER TABLE NGUOIDUNG ADD CONSTRAINT CHK_ND_SDT
    CHECK (LENGTH(SDT) = 10 AND SUBSTR(SDT,1,1) = '0' AND REGEXP_LIKE(SDT, '^[0-9]+$'));

ALTER TABLE NGUOIDUNG ADD CONSTRAINT CHK_ND_Email
    CHECK (Email LIKE '%@%.%');

ALTER TABLE NGUOIDUNG ADD CONSTRAINT CHK_ND_TrangThai
    CHECK (TrangThaiND IN ('Đang hoạt động', 'Không hoạt động', 'Bị khóa'));

-- 2. Bảng HANGTHANHVIEN (tái sử dụng)
ALTER TABLE HANGTHANHVIEN ADD CONSTRAINT CHK_HTV_PhanTram
    CHECK (TyLeGiamGia >= 0 AND TyLeGiamGia <= 100);

ALTER TABLE HANGTHANHVIEN ADD CONSTRAINT CHK_HTV_TenHang
    CHECK (TenHang IN ('Không có', 'Đồng', 'Bạc', 'Vàng', 'Kim cương'));

-- 3. Bảng KHACHHANG
ALTER TABLE KHACHHANG ADD CONSTRAINT CHK_KH_TongChiTieu
    CHECK (TongChiTieu >= 0);

-- 4. Bảng NHANVIEN
ALTER TABLE NHANVIEN ADD CONSTRAINT CHK_NV_LoaiNV
    CHECK (LoaiNV IN ('Ban tổ chức', 'Nhân viên soát vé', 'Quản lý'));

ALTER TABLE NHANVIEN ADD CONSTRAINT CHK_NV_LuongCB
    CHECK (LuongCoBan > 0);

ALTER TABLE NHANVIEN ADD CONSTRAINT CHK_NV_PhuCap
    CHECK (PhuCap >= 0);

-- 5. Bảng DIADIEM
ALTER TABLE DIADIEM ADD CONSTRAINT CHK_DD_SucChua
    CHECK (SucChuaToiDa > 0);

ALTER TABLE DIADIEM ADD CONSTRAINT CHK_DD_TrangThai
    CHECK (TrangThai IN ('Đang hoạt động', 'Tạm ngưng', 'Ngừng hoạt động'));

-- 6. Bảng SUKIEN
ALTER TABLE SUKIEN ADD CONSTRAINT CHK_SK_TrangThai
    CHECK (TrangThaiSK IN ('Chưa mở bán', 'Đang mở bán', 'Đã kết thúc', 'Đã hủy', 'Tạm ngưng'));

ALTER TABLE SUKIEN ADD CONSTRAINT CHK_SK_TongSoVe
    CHECK (TongSoVe >= 0);

ALTER TABLE SUKIEN ADD CONSTRAINT CHK_SK_SoVeDaBan
    CHECK (SoVeDaBan >= 0);

ALTER TABLE SUKIEN ADD CONSTRAINT CHK_SK_ThoiGian
    CHECK (ThoiGianKetThuc > ThoiGianBatDau);

-- 7. Bảng KHUVUC
ALTER TABLE KHUVUC ADD CONSTRAINT CHK_KV_GiaVe
    CHECK (GiaVe >= 0);

ALTER TABLE KHUVUC ADD CONSTRAINT CHK_KV_SoGhe
    CHECK (SoGheToiDa > 0);

ALTER TABLE KHUVUC ADD CONSTRAINT CHK_KV_SoGheDaBan
    CHECK (SoGheDaBan >= 0);

ALTER TABLE KHUVUC ADD CONSTRAINT CHK_KV_TrangThai
    CHECK (TrangThai IN ('Đang bán', 'Đã hết vé', 'Tạm khóa'));

-- 8. Bảng GHENGOI
ALTER TABLE GHENGOI ADD CONSTRAINT CHK_GHE_TrangThai
    CHECK (TrangThaiGhe IN ('Trống', 'Đang chọn', 'Đã bán', 'Bảo trì'));

-- 9. Bảng DONHANG
ALTER TABLE DONHANG ADD CONSTRAINT CHK_DH_TrangThai
    CHECK (TrangThaiDonHang IN ('Chờ thanh toán', 'Đã thanh toán', 'Đã hủy', 'Hoàn tiền'));

ALTER TABLE DONHANG ADD CONSTRAINT CHK_DH_TongTien
    CHECK (TongTien > 0);

ALTER TABLE DONHANG ADD CONSTRAINT CHK_DH_ThanhTien
    CHECK (ThanhTien >= 0);

-- 10. Bảng VE
ALTER TABLE VE ADD CONSTRAINT CHK_VE_TrangThai
    CHECK (TrangThaiVe IN ('Chưa sử dụng', 'Đã sử dụng', 'Đã hủy'));

ALTER TABLE VE ADD CONSTRAINT CHK_VE_GiaVe
    CHECK (GiaVe >= 0);

-- 11. Bảng GIAODICHTHANHTOAN
ALTER TABLE GIAODICHTHANHTOAN ADD CONSTRAINT CHK_GD_PhuongThuc
    CHECK (PhuongThucTT IN ('Chuyển khoản', 'Thẻ tín dụng', 'Ví điện tử', 'Tiền mặt'));

ALTER TABLE GIAODICHTHANHTOAN ADD CONSTRAINT CHK_GD_TrangThai
    CHECK (TrangThaiGD IN ('Thành công', 'Thất bại', 'Đang xử lý', 'Hết thời gian'));

ALTER TABLE GIAODICHTHANHTOAN ADD CONSTRAINT CHK_GD_LanThuLai
    CHECK (LanThuLai >= 0);

-- 12. Bảng PHIEUGIAMGIA (tái sử dụng)
ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_GiaTri
    CHECK (GiaTriGiamGia > 0);

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_ToiThieu
    CHECK (GiaTriApDungToiThieu > 0);

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_SLToiDa
    CHECK (SLToiDa > 0);

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_SLDaDung
    CHECK (SLDaDung >= 0);

ALTER TABLE PHIEUGIAMGIA ADD CONSTRAINT CHK_PGG_Loai
    CHECK (LoaiGiamGia IN ('CO_DINH', 'PHAN_TRAM'));

-- 13. Bảng LICHSUSOATVE
ALTER TABLE LICHSUSOATVE ADD CONSTRAINT CHK_LSSV_KetQua
    CHECK (KetQuaQuet IN ('Hợp lệ', 'Vé giả', 'Vé đã sử dụng', 'Sai sự kiện', 'Vé không tìm thấy'));

ALTER TABLE LICHSUSOATVE ADD CONSTRAINT CHK_LSSV_Nguon
    CHECK (NguonDuLieu IN ('Online', 'Offline'));

ALTER TABLE LICHSUSOATVE ADD CONSTRAINT CHK_LSSV_DaDongBo
    CHECK (DaDongBo IN ('Y', 'N'));

-- 14. Bảng HANGDOIAO
ALTER TABLE HANGDOIAO ADD CONSTRAINT CHK_HDT_TrangThai
    CHECK (TrangThai IN ('Đang chờ', 'Được vào', 'Hết hạn'));
