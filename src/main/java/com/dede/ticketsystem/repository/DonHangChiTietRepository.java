package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.DonHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonHangChiTietRepository extends JpaRepository<DonHangChiTiet, Long> {

    List<DonHangChiTiet> findByMaDonHang(String maDonHang);

    List<DonHangChiTiet> findByMaSK(String maSK);

    @Query("SELECT COALESCE(SUM(ct.soLuong), 0) " +
           "FROM DonHangChiTiet ct JOIN DonHang d ON ct.maDonHang = d.maDonHang " +
           "WHERE d.maKH = :maKH AND ct.maSK = :maSK AND ct.maKhuVuc = :maKhuVuc " +
           "AND d.trangThaiDonHang = 'Đã thanh toán'")
    long countPaidQuantityByCustomerAndZone(@Param("maKH") String maKH,
                                            @Param("maSK") String maSK,
                                            @Param("maKhuVuc") String maKhuVuc);
}
