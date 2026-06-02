package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.Ve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeRepository extends JpaRepository<Ve, String> {

    @Query("SELECT v FROM Ve v WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "  LOWER(v.maVe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(v.maQR) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(v.maSK) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR v.trangThaiVe = :trangThai) " +
           "ORDER BY v.thoiGianPhat DESC")
    List<Ve> search(@Param("keyword") String keyword, @Param("trangThai") String trangThai);

    boolean existsByMaQR(String maQR);

    boolean existsByMaVe(String maVe);

    boolean existsByMaDonHangAndMaGhe(String maDonHang, String maGhe);

    long countByMaDonHangAndMaSKAndMaGheIsNull(String maDonHang, String maSK);

    long countByMaDonHangAndMaSKAndMaKhuVucAndMaGheIsNull(String maDonHang, String maSK, String maKhuVuc);

    List<Ve> findByMaDonHang(String maDonHang);

    @Query("SELECT v FROM Ve v WHERE v.maSK = :maSK AND v.maDonHang = :maDonHang")
    List<Ve> findAvailableTickets(@Param("maSK") String maSK, @Param("maDonHang") String maDonHang, org.springframework.data.domain.Pageable pageable);

    Ve findByMaGhe(String maGhe);

    List<Ve> findByMaSK(String maSK);

    @Query("SELECT COUNT(v) FROM Ve v JOIN DonHang d ON v.maDonHang = d.maDonHang " +
           "WHERE d.maKH = :maKH AND v.maSK = :maSK " +
           "AND v.maGhe IN (SELECT g.maGhe FROM Ghe g WHERE g.maKhuVuc = :maKhuVuc) " +
           "AND d.trangThaiDonHang = 'Đã thanh toán'")
    long countBoughtTicketsByKHAndSKAndKhuVuc(
            @Param("maKH") String maKH,
            @Param("maSK") String maSK,
            @Param("maKhuVuc") String maKhuVuc);

    Optional<Ve> findByMaQR(String maQR);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Ve v WHERE v.maVe = :maVe")
    Optional<Ve> findByMaVeWithLock(@Param("maVe") String maVe);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Ve v WHERE v.maQR = :maQR")
    Optional<Ve> findByMaQRWithLock(@Param("maQR") String maQR);

    @Query("SELECT v FROM Ve v JOIN DonHang d ON v.maDonHang = d.maDonHang WHERE v.maVe = :maVe AND d.maKH = :maKH")
    Optional<Ve> findByMaVeAndMaKH(@Param("maVe") String maVe, @Param("maKH") String maKH);

    @Query("SELECT v FROM Ve v JOIN DonHang d ON v.maDonHang = d.maDonHang WHERE d.maKH = :maKH ORDER BY v.thoiGianPhat DESC")
    List<Ve> findByMaKH(@Param("maKH") String maKH);
}

