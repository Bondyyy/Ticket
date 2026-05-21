package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.BaoCaoSuKienDTO;
import com.dede.ticketsystem.model.BaoCaoTongQuanDTO;
import com.dede.ticketsystem.model.HanhViKhachHangDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BaoCaoService {

    private static final String TRANG_THAI_DA_THANH_TOAN = "Đã thanh toán";

    private final JdbcTemplate jdbcTemplate;

    public BaoCaoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Tổng quan báo cáo. Doanh thu/vé bán luôn chỉ tính đơn đã thanh toán.
     */
    public BaoCaoTongQuanDTO getBaoCaoTongQuan(String maSK, Timestamp tuNgay, Timestamp denNgay, String trangThaiDonHang) {
        String selectedStatus = normalize(trangThaiDonHang);
        boolean includePaidMetrics = selectedStatus == null || TRANG_THAI_DA_THANH_TOAN.equals(selectedStatus);

        BaoCaoTongQuanDTO dto = new BaoCaoTongQuanDTO();
        dto.setTongDoanhThu(includePaidMetrics ? calculateTongDoanhThu(maSK, tuNgay, denNgay) : BigDecimal.ZERO);
        dto.setTongVeDaBan(includePaidMetrics ? countPaidTickets(maSK, tuNgay, denNgay) : 0L);

        dto.setDonChoThanhToan(countDonHangByStatus("Chờ thanh toán", maSK, tuNgay, denNgay, selectedStatus));
        dto.setDonDaThanhToan(countDonHangByStatus(TRANG_THAI_DA_THANH_TOAN, maSK, tuNgay, denNgay, selectedStatus));
        dto.setDonDaHuy(countDonHangByStatus("Đã hủy", maSK, tuNgay, denNgay, selectedStatus));

        long totalTransactions = countTransactions(maSK, tuNgay, denNgay, selectedStatus, null);
        long successTransactions = countTransactions(maSK, tuNgay, denNgay, selectedStatus, "Thành công");
        dto.setTyLeThanhToanThanhCong(totalTransactions > 0 ? (double) successTransactions / totalTransactions * 100.0 : 0.0);

        long tongSoVe = sumTongSoVe(maSK);
        dto.setTyLeLapDayTB(tongSoVe > 0 ? (double) dto.getTongVeDaBan() / tongSoVe * 100.0 : 0.0);
        dto.setSuKienBanChayNhat(includePaidMetrics ? findBestSellerEvent(maSK, tuNgay, denNgay) : "Chưa có");

        return dto;
    }

    /**
     * A. Tổng doanh thu toàn hệ thống:
     * SELECT SUM(DONHANG.ThanhTien) WHERE TrangThaiDonHang = 'Đã thanh toán'.
     * Khi lọc một sự kiện cụ thể, dùng SUM(VE.GiaVe) để phần doanh thu của sự kiện
     * không lấy nhầm toàn bộ giá trị đơn nếu sau này một đơn chứa vé của nhiều sự kiện.
     */
    private BigDecimal calculateTongDoanhThu(String maSK, Timestamp tuNgay, Timestamp denNgay) {
        List<Object> params = new ArrayList<>();
        String selectedEvent = normalize(maSK);
        StringBuilder sql = new StringBuilder();

        if (selectedEvent == null) {
            sql.append("SELECT COALESCE(SUM(dh.ThanhTien), 0) ")
                    .append("FROM DONHANG dh ")
                    .append("WHERE dh.TrangThaiDonHang = ? ");
            params.add(TRANG_THAI_DA_THANH_TOAN);
            appendOrderDateFilter(sql, params, "dh", tuNgay, denNgay);
        } else {
            sql.append("SELECT COALESCE(SUM(v.GiaVe), 0) ")
                    .append("FROM VE v ")
                    .append("JOIN DONHANG dh ON v.MaDonHang = dh.MaDonHang ")
                    .append("WHERE dh.TrangThaiDonHang = ? AND v.MaSK = ? ");
            params.add(TRANG_THAI_DA_THANH_TOAN);
            params.add(selectedEvent);
            appendOrderDateFilter(sql, params, "dh", tuNgay, denNgay);
        }

        BigDecimal value = jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, params.toArray());
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * C. Tổng vé đã bán = COUNT(VE) thuộc đơn đã thanh toán.
     */
    private long countPaidTickets(String maSK, Timestamp tuNgay, Timestamp denNgay) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(v.MaVe) ")
                .append("FROM VE v ")
                .append("JOIN DONHANG dh ON v.MaDonHang = dh.MaDonHang ")
                .append("WHERE dh.TrangThaiDonHang = ? ");
        List<Object> params = new ArrayList<>();
        params.add(TRANG_THAI_DA_THANH_TOAN);

        if (normalize(maSK) != null) {
            sql.append("AND v.MaSK = ? ");
            params.add(normalize(maSK));
        }
        appendOrderDateFilter(sql, params, "dh", tuNgay, denNgay);

        return queryLong(sql.toString(), params);
    }

    private long countDonHangByStatus(String status, String maSK, Timestamp tuNgay, Timestamp denNgay, String selectedStatus) {
        if (selectedStatus != null && !selectedStatus.equals(status)) {
            return 0L;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(dh.MaDonHang) FROM DONHANG dh WHERE dh.TrangThaiDonHang = ? ");
        List<Object> params = new ArrayList<>();
        params.add(status);

        appendEventExistsFilter(sql, params, "dh", maSK);
        appendOrderDateFilter(sql, params, "dh", tuNgay, denNgay);

        return queryLong(sql.toString(), params);
    }

    /**
     * E. Tỷ lệ thanh toán thành công = COUNT(GD Thành công) / COUNT(tất cả GD).
     */
    private long countTransactions(String maSK, Timestamp tuNgay, Timestamp denNgay, String orderStatus, String transactionStatus) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(gd.MaGiaoDich) ")
                .append("FROM GIAODICHTHANHTOAN gd ")
                .append("JOIN DONHANG dh ON gd.MaDonHang = dh.MaDonHang ")
                .append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        appendEventExistsFilter(sql, params, "dh", maSK);
        if (tuNgay != null) {
            sql.append("AND gd.ThoiGianThucHien >= ? ");
            params.add(tuNgay);
        }
        if (denNgay != null) {
            sql.append("AND gd.ThoiGianThucHien <= ? ");
            params.add(denNgay);
        }
        if (orderStatus != null) {
            sql.append("AND dh.TrangThaiDonHang = ? ");
            params.add(orderStatus);
        }
        if (transactionStatus != null) {
            sql.append("AND gd.TrangThaiGD = ? ");
            params.add(transactionStatus);
        }

        return queryLong(sql.toString(), params);
    }

    private long sumTongSoVe(String maSK) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(TongSoVe), 0) FROM SUKIEN WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (normalize(maSK) != null) {
            sql.append("AND MaSK = ? ");
            params.add(normalize(maSK));
        }
        return queryLong(sql.toString(), params);
    }

    private String findBestSellerEvent(String maSK, Timestamp tuNgay, Timestamp denNgay) {
        StringBuilder sql = new StringBuilder()
                .append("SELECT sk.TenSK, COUNT(v.MaVe) AS SoldCount ")
                .append("FROM VE v ")
                .append("JOIN DONHANG dh ON v.MaDonHang = dh.MaDonHang ")
                .append("JOIN SUKIEN sk ON v.MaSK = sk.MaSK ")
                .append("WHERE dh.TrangThaiDonHang = ? ");
        List<Object> params = new ArrayList<>();
        params.add(TRANG_THAI_DA_THANH_TOAN);

        if (normalize(maSK) != null) {
            sql.append("AND v.MaSK = ? ");
            params.add(normalize(maSK));
        }
        appendOrderDateFilter(sql, params, "dh", tuNgay, denNgay);

        sql.append("GROUP BY sk.TenSK ")
                .append("ORDER BY SoldCount DESC ")
                .append("LIMIT 1");

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        if (result.isEmpty() || result.get(0).get("TENSK") == null) {
            return "Chưa có";
        }
        return String.valueOf(result.get(0).get("TENSK"));
    }

    /**
     * Bảng báo cáo theo từng sự kiện.
     */
    public List<BaoCaoSuKienDTO> getBaoCaoSuKien(String maSK, Timestamp tuNgay, Timestamp denNgay, String trangThaiDonHang) {
        String selectedStatus = normalize(trangThaiDonHang);
        boolean includePaidMetrics = selectedStatus == null || TRANG_THAI_DA_THANH_TOAN.equals(selectedStatus);

        StringBuilder sqlSK = new StringBuilder("SELECT MaSK, TenSK, TongSoVe FROM SUKIEN WHERE 1=1 ");
        List<Object> paramsSK = new ArrayList<>();
        if (normalize(maSK) != null) {
            sqlSK.append("AND MaSK = ? ");
            paramsSK.add(normalize(maSK));
        }
        sqlSK.append("ORDER BY TenSK ASC");

        List<Map<String, Object>> events = jdbcTemplate.queryForList(sqlSK.toString(), paramsSK.toArray());
        List<BaoCaoSuKienDTO> reportList = new ArrayList<>();

        for (Map<String, Object> row : events) {
            String eventId = valueAsString(row.get("MASK"));
            long tongSoVe = valueAsLong(row.get("TONGSOVE"));

            BaoCaoSuKienDTO dto = new BaoCaoSuKienDTO();
            dto.setMaSK(eventId);
            dto.setTenSK(valueAsString(row.get("TENSK")));
            dto.setTongSoVe(tongSoVe);

            long veDaBan = includePaidMetrics ? countPaidTickets(eventId, tuNgay, denNgay) : 0L;
            dto.setSoVeDaBan(veDaBan);
            dto.setSoVeConLai(Math.max(tongSoVe - veDaBan, 0L));
            dto.setDoanhThu(includePaidMetrics ? calculateEventRevenue(eventId, tuNgay, denNgay) : BigDecimal.ZERO);
            dto.setTyLeLapDay(tongSoVe > 0 ? (double) veDaBan / tongSoVe * 100.0 : 0.0);

            dto.setLuotXem(countHanhDong(eventId, "XEM_SK", tuNgay, denNgay));
            dto.setLuotClick(countHanhDong(eventId, "CLICK_DAT_VE", tuNgay, denNgay));
            dto.setLuotBoGioHang(countHanhDong(eventId, "BO_GIO_HANG", tuNgay, denNgay));

            long paidOrders = includePaidMetrics ? countPaidOrdersByEvent(eventId, tuNgay, denNgay) : 0L;
            dto.setTyLeChuyenDoi(dto.getLuotXem() > 0 ? (double) paidOrders / dto.getLuotXem() * 100.0 : 0.0);

            reportList.add(dto);
        }

        return reportList;
    }

    /**
     * B. Doanh thu theo sự kiện = SUM(VE.GiaVe), không SUM(DONHANG.ThanhTien)
     * sau khi join nhiều vé.
     */
    private BigDecimal calculateEventRevenue(String maSK, Timestamp tuNgay, Timestamp denNgay) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(v.GiaVe), 0) ")
                .append("FROM VE v ")
                .append("JOIN DONHANG dh ON v.MaDonHang = dh.MaDonHang ")
                .append("WHERE dh.TrangThaiDonHang = ? AND v.MaSK = ? ");
        List<Object> params = new ArrayList<>();
        params.add(TRANG_THAI_DA_THANH_TOAN);
        params.add(maSK);
        appendOrderDateFilter(sql, params, "dh", tuNgay, denNgay);

        BigDecimal value = jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, params.toArray());
        return value != null ? value : BigDecimal.ZERO;
    }

    private long countPaidOrdersByEvent(String maSK, Timestamp tuNgay, Timestamp denNgay) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT dh.MaDonHang) ")
                .append("FROM DONHANG dh ")
                .append("JOIN VE v ON v.MaDonHang = dh.MaDonHang ")
                .append("WHERE v.MaSK = ? AND dh.TrangThaiDonHang = ? ");
        List<Object> params = new ArrayList<>();
        params.add(maSK);
        params.add(TRANG_THAI_DA_THANH_TOAN);
        appendOrderDateFilter(sql, params, "dh", tuNgay, denNgay);
        return queryLong(sql.toString(), params);
    }

    private long countHanhDong(String maSK, String loaiHanhDong, Timestamp tuNgay, Timestamp denNgay) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(MaLog) FROM LOG_HANH_VI WHERE MaSK = ? AND LoaiHanhDong = ? ");
        List<Object> params = new ArrayList<>();
        params.add(maSK);
        params.add(loaiHanhDong);

        if (tuNgay != null) {
            sql.append("AND ThoiGian >= ? ");
            params.add(tuNgay);
        }
        if (denNgay != null) {
            sql.append("AND ThoiGian <= ? ");
            params.add(denNgay);
        }

        return queryLong(sql.toString(), params);
    }

    /**
     * Danh sách 50 hành vi khách hàng gần nhất.
     */
    public List<HanhViKhachHangDTO> getRecentHanhViKhachHang(String maSK, Timestamp tuNgay, Timestamp denNgay) {
        StringBuilder sql = new StringBuilder()
                .append("SELECT l.MaLog, l.LoaiHanhDong, l.MaSK, sk.TenSK, l.ThoiGian, l.MaKH, nd.TenTaiKhoan, l.ThietBi ")
                .append("FROM LOG_HANH_VI l ")
                .append("LEFT JOIN SUKIEN sk ON l.MaSK = sk.MaSK ")
                .append("LEFT JOIN KHACHHANG kh ON l.MaKH = kh.MaKH ")
                .append("LEFT JOIN NGUOIDUNG nd ON kh.MaND = nd.MaND ")
                .append("WHERE l.LoaiHanhDong IN ('XEM_SK', 'CLICK_DAT_VE', 'BO_GIO_HANG') ");
        List<Object> params = new ArrayList<>();

        if (normalize(maSK) != null) {
            sql.append("AND l.MaSK = ? ");
            params.add(normalize(maSK));
        }
        if (tuNgay != null) {
            sql.append("AND l.ThoiGian >= ? ");
            params.add(tuNgay);
        }
        if (denNgay != null) {
            sql.append("AND l.ThoiGian <= ? ");
            params.add(denNgay);
        }

        sql.append("ORDER BY l.ThoiGian DESC ")
                .append("LIMIT 50");

        List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        List<HanhViKhachHangDTO> logs = new ArrayList<>();

        for (Map<String, Object> row : queryResult) {
            HanhViKhachHangDTO logDto = new HanhViKhachHangDTO();
            logDto.setMaLog(valueAsString(row.get("MALOG")));
            logDto.setLoaiHanhDong(valueAsString(row.get("LOAIHANHDONG")));
            logDto.setMaSK(row.get("MASK") != null ? valueAsString(row.get("MASK")) : "");
            logDto.setTenSK(row.get("TENSK") != null ? valueAsString(row.get("TENSK")) : "Hệ thống");
            logDto.setThoiGian((Timestamp) row.get("THOIGIAN"));
            logDto.setMaKH(row.get("MAKH") != null ? valueAsString(row.get("MAKH")) : null);
            logDto.setTenKH(row.get("TENTAIKHOAN") != null ? valueAsString(row.get("TENTAIKHOAN")) : "Khách vãng lai");
            logDto.setThietBi(row.get("THIETBI") != null ? valueAsString(row.get("THIETBI")) : "Web");
            logs.add(logDto);
        }

        return logs;
    }

    private void appendEventExistsFilter(StringBuilder sql, List<Object> params, String orderAlias, String maSK) {
        if (normalize(maSK) == null) {
            return;
        }
        sql.append("AND EXISTS (SELECT 1 FROM VE v_event WHERE v_event.MaDonHang = ")
                .append(orderAlias)
                .append(".MaDonHang AND v_event.MaSK = ?) ");
        params.add(normalize(maSK));
    }

    private void appendOrderDateFilter(StringBuilder sql, List<Object> params, String orderAlias, Timestamp tuNgay, Timestamp denNgay) {
        if (tuNgay != null) {
            sql.append("AND ").append(orderAlias).append(".ThoiGianDat >= ? ");
            params.add(tuNgay);
        }
        if (denNgay != null) {
            sql.append("AND ").append(orderAlias).append(".ThoiGianDat <= ? ");
            params.add(denNgay);
        }
    }

    private long queryLong(String sql, List<Object> params) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, params.toArray());
        return value != null ? value : 0L;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private long valueAsLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
