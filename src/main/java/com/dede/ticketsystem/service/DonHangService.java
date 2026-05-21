package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.DonHang;
import com.dede.ticketsystem.model.Ve;
import com.dede.ticketsystem.model.Ghe;
import com.dede.ticketsystem.model.KhuVuc;
import com.dede.ticketsystem.repository.DonHangRepository;
import com.dede.ticketsystem.repository.VeRepository;
import com.dede.ticketsystem.repository.GheRepository;
import com.dede.ticketsystem.repository.KhuVucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DonHangService {

    @Autowired
    private DonHangRepository donHangRepository;

    @Autowired
    private VeRepository veRepository;

    @Autowired
    private GheRepository gheRepository;

    @Autowired
    private KhuVucRepository khuVucRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    public List<DonHang> layTatCa() {
        return donHangRepository.findAll();
    }

    public List<DonHang> timKiem(String keyword, String trangThai) {
        return donHangRepository.search(keyword, trangThai);
    }

    public Optional<DonHang> timTheoMa(String maDonHang) {
        return donHangRepository.findById(maDonHang);
    }

    public void huyDonHang(String maDonHang) {
        donHangRepository.findById(maDonHang).ifPresent(dh -> {
            dh.setTrangThaiDonHang("Đã hủy");
            dh.setCapNhatLanCuoi(new java.sql.Timestamp(System.currentTimeMillis()));
            donHangRepository.save(dh);
        });
    }

    @org.springframework.transaction.annotation.Transactional
    public DonHang taoDonHang(String maSK, int soLuong, String maKH, String maNV) {
        List<Ghe> gheTrong = gheRepository.findByMaSKAndTrangThaiGhe(maSK, "Trống", org.springframework.data.domain.PageRequest.of(0, soLuong));
        
        if (gheTrong.size() < soLuong) {
            throw new RuntimeException("Không đủ số lượng ghế trống cho sự kiện này. Chỉ còn " + gheTrong.size() + " ghế.");
        }
        
        DonHang dh = new DonHang();
        String maDon = idGeneratorService.nextDonHangId();
        dh.setMaDonHang(maDon);
        dh.setSoDonHang(maDon);
        dh.setTrangThaiDonHang("Chờ thanh toán");
        dh.setThoiGianDat(new java.sql.Timestamp(System.currentTimeMillis()));
        dh.setMaKH(maKH);
        dh.setMaNV(maNV);
        
        java.math.BigDecimal tongTien = java.math.BigDecimal.ZERO;
        List<Ve> listVeMoi = new java.util.ArrayList<>();
        
        for (Ghe g : gheTrong) {
            g.setTrangThaiGhe("Đã bán");
            
            KhuVuc kv = khuVucRepository.findById(g.getMaKhuVuc()).orElse(null);
            java.math.BigDecimal giaVe = (kv != null) ? kv.getGiaVe() : java.math.BigDecimal.ZERO;
            
            Ve v = new Ve();
            String maVe = idGeneratorService.nextVeId();
            v.setMaVe(maVe);
            v.setMaQR("QR-" + maVe);
            v.setGiaVe(giaVe);
            v.setTrangThaiVe("Chưa sử dụng");
            v.setThoiGianPhat(new java.sql.Timestamp(System.currentTimeMillis()));
            v.setMaSK(maSK);
            v.setMaGhe(g.getMaGhe());
            v.setMaDonHang(maDon);
            
            listVeMoi.add(v);
            
            if (giaVe != null) {
                tongTien = tongTien.add(giaVe);
            }
        }
        dh.setTongTien(tongTien);
        dh.setThanhTien(tongTien);
        
        DonHang savedDh = donHangRepository.save(dh);
        gheRepository.saveAll(gheTrong);
        veRepository.saveAll(listVeMoi);
        
        return savedDh;
    }
}
