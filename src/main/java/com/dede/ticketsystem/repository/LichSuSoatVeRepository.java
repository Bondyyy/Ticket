package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.LichSuSoatVe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuSoatVeRepository extends JpaRepository<LichSuSoatVe, String> {

    List<LichSuSoatVe> findByMaNVOrderByThoiGianQuetDesc(String maNV);

    List<LichSuSoatVe> findByMaVeOrderByThoiGianQuetDesc(String maVe);

    @Query("SELECT l FROM LichSuSoatVe l WHERE l.maNV = :maNV ORDER BY l.thoiGianQuet DESC")
    List<LichSuSoatVe> findRecentHistoryByStaff(@Param("maNV") String maNV, Pageable pageable);
}
