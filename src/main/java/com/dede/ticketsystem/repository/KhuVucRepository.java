package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.KhuVuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhuVucRepository extends JpaRepository<KhuVuc, String> {
    List<KhuVuc> findByMaSK(String maSK);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT k FROM KhuVuc k WHERE k.maKhuVuc = :id")
    Optional<KhuVuc> findByIdWithLock(@Param("id") String id);
}
