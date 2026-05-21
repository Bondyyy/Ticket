package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.Ghe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface GheRepository extends JpaRepository<Ghe, String> {
    List<Ghe> findByMaSK(String maSK);

    List<Ghe> findByMaKhuVuc(String maKhuVuc);

    List<Ghe> findByMaSKAndTrangThaiGhe(String maSK, String trangThaiGhe, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT g FROM Ghe g WHERE g.trangThaiGhe = :trangThai AND g.thoiGianKhoaTam < :thoiGian")
    List<Ghe> findExpiredSeats(@Param("trangThai") String trangThai,
            @Param("thoiGian") Timestamp thoiGian);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Ghe g WHERE g.maGhe IN :ids ORDER BY g.maGhe ASC")
    List<Ghe> findAllByIdWithLock(@Param("ids") List<String> ids);

    List<Ghe> findByMaPhienKhoa(String maPhienKhoa);

    long countByMaSKAndTrangThaiGhe(String maSK, String trangThaiGhe);
}

