package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.LoaiSuKien;
import com.dede.ticketsystem.repository.LoaiSuKienRepository;
import com.dede.ticketsystem.service.IdGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loaisukien")
public class LoaiSuKienApiController {

    private final LoaiSuKienRepository loaiSuKienRepository;
    private final IdGeneratorService idGeneratorService;

    public LoaiSuKienApiController(LoaiSuKienRepository loaiSuKienRepository,
                                   IdGeneratorService idGeneratorService) {
        this.loaiSuKienRepository = loaiSuKienRepository;
        this.idGeneratorService = idGeneratorService;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam(required = false, defaultValue = "") String keyword) {
        return loaiSuKienRepository.search(keyword == null ? "" : keyword.trim())
                .stream()
                .limit(12)
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateLoaiSuKienRequest request) {
        String tenLoaiSK = normalizeName(request != null ? request.getTenLoaiSK() : null);
        if (tenLoaiSK == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên loại sự kiện không được để trống."));
        }

        return ResponseEntity.ok(toResponse(
                loaiSuKienRepository.findByTenLoaiSKIgnoreCaseTrimmed(tenLoaiSK)
                        .orElseGet(() -> loaiSuKienRepository.save(new LoaiSuKien(
                                idGeneratorService.nextLoaiSuKienId(),
                                tenLoaiSK,
                                normalizeNullable(request.getMoTa())
                        )))
        ));
    }

    private Map<String, Object> toResponse(LoaiSuKien loaiSuKien) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("maLoaiSK", loaiSuKien.getMaLoaiSK());
        data.put("tenLoaiSK", loaiSuKien.getTenLoaiSK());
        data.put("moTa", loaiSuKien.getMoTa());
        return data;
    }

    private String normalizeName(String value) {
        String clean = normalizeNullable(value);
        if (clean == null) {
            return null;
        }
        return clean.replaceAll("\\s+", " ");
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim();
        return clean.isEmpty() ? null : clean;
    }

    public static class CreateLoaiSuKienRequest {
        private String tenLoaiSK;
        private String moTa;

        public String getTenLoaiSK() {
            return tenLoaiSK;
        }

        public void setTenLoaiSK(String tenLoaiSK) {
            this.tenLoaiSK = tenLoaiSK;
        }

        public String getMoTa() {
            return moTa;
        }

        public void setMoTa(String moTa) {
            this.moTa = moTa;
        }
    }
}
