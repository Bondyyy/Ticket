package com.dede.ticketsystem.repository;

import com.dede.ticketsystem.model.LogHanhVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogHanhViRepository extends JpaRepository<LogHanhVi, String> {

    long countByMaSKAndLoaiHanhDong(String maSK, String loaiHanhDong);
}
