package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {

    Optional<NguoiDung> findByTenTaiKhoan(String tenTaiKhoan);

    Optional<NguoiDung> findByTenTaiKhoanIgnoreCase(String tenTaiKhoan);

    Optional<NguoiDung> findByEmail(String email);

    Optional<NguoiDung> findByEmailIgnoreCase(String email);

    boolean existsByTenTaiKhoan(String tenTaiKhoan);

    boolean existsByEmail(String email);

    @Query("SELECT nd FROM NguoiDung nd WHERE " +
           "LOWER(nd.tenTaiKhoan) = LOWER(:identifier) OR " +
           "LOWER(nd.email) = LOWER(:identifier)")
    Optional<NguoiDung> findByUsernameOrEmailIgnoreCase(@Param("identifier") String identifier);

    @Query("SELECT nd FROM NguoiDung nd WHERE " +
           "LOWER(nd.tenTaiKhoan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(nd.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<NguoiDung> timKiemTheoTenHoacEmail(@Param("keyword") String keyword);

    List<NguoiDung> findByTrangThaiND(String trangThaiND);
}
