package com.dede.ticketsystem.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class IdGeneratorService {

    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Z][A-Z0-9_]*");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("[A-Z][A-Z0-9]*");

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Integer> issuedCounters = new HashMap<>();

    public IdGeneratorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized String nextId(String prefix, String tableName, String idColumn) {
        String cleanPrefix = validatePrefix(prefix);
        String cleanTable = validateSqlIdentifier(tableName, "tableName");
        String cleanColumn = validateSqlIdentifier(idColumn, "idColumn");

        String key = cleanTable.toUpperCase(Locale.ROOT) + "." + cleanColumn.toUpperCase(Locale.ROOT) + ":" + cleanPrefix;
        int next = Math.max(issuedCounters.getOrDefault(key, 0), findMaxSuffix(cleanPrefix, cleanTable, cleanColumn)) + 1;
        String candidate = format(cleanPrefix, next);
        while (exists(cleanTable, cleanColumn, candidate)) {
            next++;
            candidate = format(cleanPrefix, next);
        }
        issuedCounters.put(key, next);
        return candidate;
    }

    public String nextSuKienId() {
        return nextId("SK", "SUKIEN", "MaSK");
    }

    public String nextLoaiSuKienId() {
        return nextId("LSK", "LOAISUKIEN", "MaLoaiSK");
    }

    public String nextKhuVucId() {
        return nextId("KV", "KHUVUC", "MaKhuVuc");
    }

    public String nextGheId() {
        return nextId("GHE", "GHENGOI", "MaGhe");
    }

    public String nextVeId() {
        return nextId("VE", "VE", "MaVe");
    }

    public String nextDonHangId() {
        return nextId("DH", "DONHANG", "MaDonHang");
    }

    public String nextGiaoDichId() {
        return nextId("GD", "GIAODICHTHANHTOAN", "MaGiaoDich");
    }

    public String nextNguoiDungId() {
        return nextId("ND", "NGUOIDUNG", "MaND");
    }

    public String nextKhachHangId() {
        return nextId("KH", "KHACHHANG", "MaKH");
    }

    public String nextNhanVienId() {
        return nextId("NV", "NHANVIEN", "MaNV");
    }

    public String nextVaiTroCustomId() {
        return nextId("VT", "VAITRO", "MaVaiTro");
    }

    public String nextLichSuSoatVeId() {
        return nextId("LSSV", "LICHSUSOATVE", "MaLichSu");
    }

    public String nextEmailLogId() {
        return nextId("EMAIL", "LICHSUGUI_EMAIL", "MaEmail");
    }

    public String nextHangDoiId() {
        return nextId("HD", "HANGDOIAO", "MaHangDoi");
    }

    public String nextLogHanhViId() {
        return nextId("LOG", "LOG_HANH_VI", "MaLog");
    }

    private int findMaxSuffix(String prefix, String tableName, String idColumn) {
        String sql = "SELECT COALESCE(MAX(TO_NUMBER(SUBSTR(" + idColumn + ", ?))), 0) " +
                "FROM " + tableName + " WHERE REGEXP_LIKE(" + idColumn + ", ?)";
        Number max = jdbcTemplate.queryForObject(sql, Number.class, prefix.length() + 1, "^" + prefix + "[0-9]{4,}$");
        return max == null ? 0 : max.intValue();
    }

    private boolean exists(String tableName, String idColumn, String id) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private String format(String prefix, int number) {
        return prefix + String.format("%04d", number);
    }

    private String validatePrefix(String value) {
        if (value == null) {
            throw new IllegalArgumentException("prefix không được null");
        }
        String clean = value.trim().toUpperCase(Locale.ROOT);
        if (!PREFIX_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException("prefix không hợp lệ: " + value);
        }
        return clean;
    }

    private String validateSqlIdentifier(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " không được null");
        }
        String clean = value.trim();
        if (!SQL_IDENTIFIER.matcher(clean.toUpperCase(Locale.ROOT)).matches()) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ: " + value);
        }
        return clean;
    }
}
