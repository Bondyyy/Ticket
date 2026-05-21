package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.GiaoDichThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiaoDichThanhToanRepository extends JpaRepository<GiaoDichThanhToan, String> {

    List<GiaoDichThanhToan> findByMaDonHangOrderByLanThuLaiDesc(String maDonHang);

    @Query("SELECT COALESCE(MAX(g.lanThuLai), 0) FROM GiaoDichThanhToan g WHERE g.maDonHang = :maDonHang")
    Integer findMaxLanThuLaiByMaDonHang(@Param("maDonHang") String maDonHang);

    long countByMaDonHangAndTrangThaiGD(String maDonHang, String trangThaiGD);
}

