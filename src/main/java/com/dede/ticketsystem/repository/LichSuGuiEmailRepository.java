package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.LichSuGuiEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuGuiEmailRepository extends JpaRepository<LichSuGuiEmail, String> {

    boolean existsByLoaiEmailAndMaVe(String loaiEmail, String maVe);

    boolean existsByLoaiEmailAndMaDonHang(String loaiEmail, String maDonHang);
}
