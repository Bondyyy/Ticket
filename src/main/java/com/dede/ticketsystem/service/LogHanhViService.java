package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.LogHanhVi;
import com.dede.ticketsystem.repository.LogHanhViRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
public class LogHanhViService {

    @Autowired
    private LogHanhViRepository logHanhViRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String loaiHanhDong, String maSK, String maKH, String thietBi) {
        try {
            LogHanhVi logObj = new LogHanhVi();
            logObj.setMaLog(idGeneratorService.nextLogHanhViId());
            logObj.setLoaiHanhDong(loaiHanhDong);
            logObj.setMaSK(maSK);
            logObj.setThoiGian(new Timestamp(System.currentTimeMillis()));
            logObj.setMaKH(maKH);
            
            // Normalize thietBi to 'Web' or 'Mobile' based on User-Agent detection
            String normalizedThietBi = "Web";
            if (thietBi != null) {
                String tbLower = thietBi.toLowerCase();
                if (tbLower.contains("mobile") || tbLower.contains("android") || tbLower.contains("iphone") || "mobile".equalsIgnoreCase(thietBi)) {
                    normalizedThietBi = "Mobile";
                }
            }
            logObj.setThietBi(normalizedThietBi);

            logHanhViRepository.save(logObj);
        } catch (Exception e) {
            System.err.println("Cảnh báo: Lỗi ghi log hành vi khách hàng: " + e.getMessage());
            e.printStackTrace();
            // Không throw để tránh ảnh hưởng request chính
        }
    }
}
