package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, String> {

    List<DonHang> findByTrangThaiDonHangIgnoreCase(String trangThai);

    @Query("SELECT d FROM DonHang d WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "  LOWER(d.soDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(d.maKH) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR d.trangThaiDonHang = :trangThai) " +
           "ORDER BY d.thoiGianDat DESC")
    List<DonHang> search(@Param("keyword") String keyword,
                         @Param("trangThai") String trangThai);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DonHang d WHERE d.maDonHang = :id")
    Optional<DonHang> findByIdWithLock(@Param("id") String id);

    @Query("SELECT d FROM DonHang d WHERE d.trangThaiDonHang = 'Chờ thanh toán' AND d.thoiGianHetHan < :now")
    List<DonHang> findExpiredPendingOrders(@Param("now") Timestamp now);

    List<DonHang> findByMaKHOrderByThoiGianDatDesc(String maKH);

    @Query("SELECT COUNT(DISTINCT d.maDonHang) " +
           "FROM DonHang d, Ghe g " +
           "WHERE g.maPhienKhoa = d.maDonHang " +
           "AND g.maSK = :maSK " +
           "AND d.trangThaiDonHang = 'Chờ thanh toán'")
    long countPendingOrdersByMaSK(@Param("maSK") String maSK);
}

