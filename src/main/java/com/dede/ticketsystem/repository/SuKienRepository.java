package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.SuKien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuKienRepository extends JpaRepository<SuKien, String> {

    @Query("SELECT s FROM SuKien s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "  LOWER(s.maSK) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(s.tenSK) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(s.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(s.moTaNgan) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR s.trangThaiSK = :trangThai) " +
           "ORDER BY s.thoiGianBatDau DESC")
    List<SuKien> search(@Param("keyword") String keyword, @Param("trangThai") String trangThai);
}
