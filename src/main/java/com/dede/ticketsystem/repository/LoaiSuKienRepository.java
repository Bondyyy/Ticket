package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.LoaiSuKien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoaiSuKienRepository extends JpaRepository<LoaiSuKien, String> {

    @Query("SELECT l FROM LoaiSuKien l " +
           "WHERE :keyword IS NULL OR :keyword = '' OR " +
           "LOWER(l.tenLoaiSK) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY l.tenLoaiSK")
    List<LoaiSuKien> search(@Param("keyword") String keyword);

    @Query("SELECT l FROM LoaiSuKien l " +
           "WHERE LOWER(TRIM(l.tenLoaiSK)) = LOWER(TRIM(:tenLoaiSK))")
    Optional<LoaiSuKien> findByTenLoaiSKIgnoreCaseTrimmed(@Param("tenLoaiSK") String tenLoaiSK);
}
