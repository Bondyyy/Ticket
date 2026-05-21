-- LEGACY / DEMO SQL ONLY, Java service is source of truth.
CREATE OR REPLACE PROCEDURE sp_DatVe (
    p_MaKH          IN VARCHAR2,
    p_MaSK          IN VARCHAR2,
    p_DanhSachGhe   IN t_chi_tiet_ve_list,  -- Danh sách ghế muốn đặt
    p_PhuongThucTT  IN VARCHAR2,
    p_MaPGG         IN VARCHAR2 DEFAULT NULL,
    p_MaDonHang     OUT VARCHAR2,
    p_KetQua        OUT VARCHAR2
) AS
    v_MaVe          VARCHAR2(50);
    v_MaQR          VARCHAR2(500);
    v_GiaVe         NUMBER(18,2);
    v_TongTien      NUMBER(18,2) := 0;
    v_ThanhTien     NUMBER(18,2);
    v_GiamGia       NUMBER(18,2) := 0;
    v_SoLuong       NUMBER;
    v_BotCheck      NUMBER;
    v_LoaiGiamGia   VARCHAR2(20);
    v_SoVeToiDa     NUMBER;
    v_SoDaMua       NUMBER;
BEGIN
    -- Kiểm tra bot
    v_BotCheck := fn_KiemTraGioiHanBot(p_MaKH);
    IF v_BotCheck = 1 THEN
        RAISE_APPLICATION_ERROR(-20300, 'Tài khoản bị giới hạn tần suất. Vui lòng thử lại sau.');
    END IF;

    -- Kiểm tra overbooking tổng thể
    v_SoLuong := p_DanhSachGhe.COUNT;
    IF fn_KiemTraOverbooking(p_MaSK, p_DanhSachGhe(1).ma_khu_vuc, v_SoLuong) = 1 THEN
        RAISE_APPLICATION_ERROR(-20301, 'Số lượng vé yêu cầu vượt quá ghế còn trống!');
    END IF;

    -- Lấy giới hạn vé và kiểm tra
    SELECT NVL(SoVeToiDaPerKH, 4) INTO v_SoVeToiDa
    FROM KHUVUC WHERE MaKhuVuc = p_DanhSachGhe(1).ma_khu_vuc;

    SELECT COUNT(*) INTO v_SoDaMua
    FROM VE v JOIN DONHANG d ON v.MaDonHang = d.MaDonHang
    WHERE d.MaKH = p_MaKH AND v.MaSK = p_MaSK AND v.TrangThaiVe != 'Đã hủy';

    IF v_SoDaMua + v_SoLuong > v_SoVeToiDa THEN
        RAISE_APPLICATION_ERROR(-20302, 'Vượt quá giới hạn số vé tối đa cho phép mua trên mỗi khách hàng!');
    END IF;

    -- Sinh mã đơn hàng
    p_MaDonHang := 'DH_' || TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3');

    -- Tạo đơn hàng
    INSERT INTO DONHANG (MaDonHang, SoDonHang, TrangThaiDonHang, ThoiGianDat, ThoiGianHetHan, MaKH, MaPGG)
    VALUES (p_MaDonHang, p_MaDonHang, 'Chờ thanh toán',
            SYSTIMESTAMP, SYSTIMESTAMP + INTERVAL '15' MINUTE,
            p_MaKH, p_MaPGG);

    -- Lặp qua từng ghế, tạo vé
    FOR i IN 1 .. p_DanhSachGhe.COUNT LOOP
        -- Khóa ghế
        sp_KhoaGheTamThoi(p_DanhSachGhe(i).ma_ghe, p_MaDonHang);

        -- Tính giá
        v_GiaVe := fn_TinhGiaVe(p_DanhSachGhe(i).ma_khu_vuc, p_MaKH);
        v_TongTien := v_TongTien + v_GiaVe;

        -- Sinh mã vé và QR
        v_MaVe := 'VE_' || p_DanhSachGhe(i).ma_ghe || '_' || TO_CHAR(SYSTIMESTAMP, 'FF3');
        v_MaQR := fn_SinhMaQRDuyNhat(v_MaVe, p_MaSK);

        -- Tạo bản ghi vé (chưa phát, chờ thanh toán xong)
        INSERT INTO VE (MaVe, MaQR, GiaVe, TrangThaiVe, MaDonHang, MaGhe, MaSK)
        VALUES (v_MaVe, v_MaQR, v_GiaVe, 'Chưa sử dụng', p_MaDonHang,
                p_DanhSachGhe(i).ma_ghe, p_MaSK);
    END LOOP;

    -- Áp dụng phiếu giảm giá nếu có
    IF p_MaPGG IS NOT NULL THEN
        SELECT NVL(GiaTriGiamGia, 0), LoaiGiamGia INTO v_GiamGia, v_LoaiGiamGia
        FROM PHIEUGIAMGIA
        WHERE MaPGG = p_MaPGG
          AND SLDaDung < SLToiDa
          AND SYSTIMESTAMP BETWEEN NgayBatDauApDung AND NgayKetThucApDung;
          
        IF v_LoaiGiamGia = 'PHAN_TRAM' THEN
            v_GiamGia := v_TongTien * (v_GiamGia / 100);
        END IF;
    END IF;

    v_ThanhTien := GREATEST(v_TongTien - v_GiamGia, 0);

    -- Cập nhật tổng tiền đơn hàng
    UPDATE DONHANG
    SET TongTien = v_TongTien, ThanhTien = v_ThanhTien
    WHERE MaDonHang = p_MaDonHang;

    -- Gọi procedure xử lý thanh toán
    sp_XuLyThanhToan(p_MaDonHang, v_ThanhTien, p_PhuongThucTT, p_KetQua);

    IF p_KetQua = 'Thành công' THEN
        COMMIT;
    ELSE
        ROLLBACK;
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_KetQua := 'Thất bại: ' || SQLERRM;
        RAISE;
END sp_DatVe;
/
