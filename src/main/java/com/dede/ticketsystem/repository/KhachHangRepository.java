package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.KhachHang;
import com.dede.ticketsystem.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, String> {

    Optional<KhachHang> findByNguoiDung(NguoiDung nguoiDung);

    @Query("SELECT kh FROM KhachHang kh WHERE kh.nguoiDung.maND = :maND")
    Optional<KhachHang> findByMaND(@Param("maND") String maND);
}
