package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.DiaDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaDiemRepository extends JpaRepository<DiaDiem, String> {
    List<DiaDiem> findAllByOrderByTenDiaDiemAsc();
}
