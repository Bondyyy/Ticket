package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.SuKienBanToChuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuKienBanToChucRepository extends JpaRepository<SuKienBanToChuc, Long> {
    List<SuKienBanToChuc> findByMaSKOrderByLaVaiTroChinhDescIdAsc(String maSK);
    Optional<SuKienBanToChuc> findByMaSKAndMaNV(String maSK, String maNV);
    void deleteByMaSKAndMaNVNotIn(String maSK, List<String> maNVList);
    void deleteByMaSK(String maSK);
}
