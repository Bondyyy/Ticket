package com.dede.ticketsystem.model;

import java.math.BigDecimal;
import java.util.List;

public class ThietLapSanKhauDTO {
    
    private List<KhuVucDTO> danhSachKhuVuc;

    public List<KhuVucDTO> getDanhSachKhuVuc() {
        return danhSachKhuVuc;
    }

    public void setDanhSachKhuVuc(List<KhuVucDTO> danhSachKhuVuc) {
        this.danhSachKhuVuc = danhSachKhuVuc;
    }

    public static class KhuVucDTO {
        private String tenKhuVuc;
        private BigDecimal giaVe;
        private Integer soHang;
        private Integer soGheMoiHang;
        private String mauSacHienThi;
        private Integer soVeToiDaPerKH;

        public String getTenKhuVuc() { return tenKhuVuc; }
        public void setTenKhuVuc(String tenKhuVuc) { this.tenKhuVuc = tenKhuVuc; }

        public BigDecimal getGiaVe() { return giaVe; }
        public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }

        public Integer getSoHang() { return soHang; }
        public void setSoHang(Integer soHang) { this.soHang = soHang; }

        public Integer getSoGheMoiHang() { return soGheMoiHang; }
        public void setSoGheMoiHang(Integer soGheMoiHang) { this.soGheMoiHang = soGheMoiHang; }

        public String getMauSacHienThi() { return mauSacHienThi; }
        public void setMauSacHienThi(String mauSacHienThi) { this.mauSacHienThi = mauSacHienThi; }

        public Integer getSoVeToiDaPerKH() { return soVeToiDaPerKH; }
        public void setSoVeToiDaPerKH(Integer soVeToiDaPerKH) { this.soVeToiDaPerKH = soVeToiDaPerKH; }
    }
}
