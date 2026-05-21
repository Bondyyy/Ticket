package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.HangDoiAo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface HangDoiAoRepository extends JpaRepository<HangDoiAo, String> {

    long countByMaSKAndTrangThai(String maSK, String trangThai);

    Optional<HangDoiAo> findByTokenHangDoi(String token);

    Optional<HangDoiAo> findTopByMaSKAndTrangThaiOrderByViTriHangAsc(String maSK, String trangThai);

    List<HangDoiAo> findByMaSKAndTrangThaiOrderByViTriHangAsc(String maSK, String trangThai);

    Optional<HangDoiAo> findByMaKHAndMaSKAndTrangThaiIn(String maKH, String maSK, Collection<String> trangThaiList);

    @Query("SELECT DISTINCT h.maSK FROM HangDoiAo h WHERE h.trangThai = 'Đang chờ'")
    List<String> findDistinctMaSKDangCho();

    @Query("SELECT COUNT(h) FROM HangDoiAo h WHERE h.maSK = :maSK AND h.trangThai = 'Đang chờ' AND h.thoiGianVaoHang <= :thoiGianVaoHang")
    long calculateCurrentPosition(@Param("maSK") String maSK, @Param("thoiGianVaoHang") Timestamp thoiGianVaoHang);

    @Query("SELECT COALESCE(MAX(h.viTriHang), 0) FROM HangDoiAo h WHERE h.maSK = :maSK AND h.trangThai = 'Đang chờ'")
    Long findMaxViTriHangByMaSKAndTrangThai(@Param("maSK") String maSK);
}
