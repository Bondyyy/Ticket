package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.ChiTietVaiTro;
import com.dede.ticketsystem.model.ChiTietVaiTroID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietVaiTroRepository extends JpaRepository<ChiTietVaiTro, ChiTietVaiTroID> {

    List<ChiTietVaiTro> findByMaND(String maND);

    List<ChiTietVaiTro> findByMaVaiTro(String maVaiTro);

    long countByMaVaiTro(String maVaiTro);

    boolean existsByMaNDAndMaVaiTro(String maND, String maVaiTro);

    void deleteByMaNDAndMaVaiTro(String maND, String maVaiTro);

    void deleteByMaND(String maND);
}
