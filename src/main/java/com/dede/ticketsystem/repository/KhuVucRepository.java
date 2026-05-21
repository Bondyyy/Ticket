package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.KhuVuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KhuVucRepository extends JpaRepository<KhuVuc, String> {
    List<KhuVuc> findByMaSK(String maSK);
}
