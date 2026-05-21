package com.dede.ticketsystem.controller;

import com.dede.ticketsystem.model.NguoiDung;
import com.dede.ticketsystem.model.TaiKhoanDTO;
import com.dede.ticketsystem.model.VaiTro;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.dede.ticketsystem.service.TaiKhoanService;
import java.util.*;

@Controller
@RequestMapping("/taikhoan")
public class TaiKhoanController {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @GetMapping
    public String danhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            Model model) {

        List<NguoiDung> danhSach;
        if (keyword != null && !keyword.isBlank()) {
            danhSach = taiKhoanService.timKiem(keyword);
        } else if (trangThai != null && !trangThai.isBlank()) {
            danhSach = taiKhoanService.locTheoTrangThai(trangThai);
        } else {
            danhSach = taiKhoanService.getDanhSachTatCa();
        }

        model.addAttribute("danhSachTaiKhoan", danhSach);
        model.addAttribute("danhSachVaiTro", taiKhoanService.getDanhSachVaiTro());
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("taiKhoanMoi", new TaiKhoanDTO());
        model.addAttribute("activeTab", "taiKhoan");
        return "QLTK/QLTK";
    }

    @GetMapping("/api/{maND}")
    @ResponseBody
    public ResponseEntity<?> layThongTinTaiKhoan(@PathVariable String maND) {
        return taiKhoanService.timTheoMa(maND)
                .map(nd -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("maND", nd.getMaND());
                    data.put("tenTaiKhoan", nd.getTenTaiKhoan());
                    data.put("email", nd.getEmail());
                    data.put("sdt", nd.getSdt());
                    data.put("gioiTinh", nd.getGioiTinh());
                    data.put("trangThaiND", nd.getTrangThaiND());
                    data.put("ngaySinh", nd.getNgaySinh() != null ? nd.getNgaySinh().toString() : null);
                    List<String> vaiTros = new ArrayList<>();
                    if (nd.getChiTietVaiTros() != null) {
                        nd.getChiTietVaiTros().forEach(ctv -> vaiTros.add(ctv.getMaVaiTro()));
                    }
                    data.put("danhSachVaiTro", vaiTros);
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/tao-moi")
    public String taoMoi(@ModelAttribute TaiKhoanDTO dto, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.taoTaiKhoan(dto);
            redirectAttributes.addFlashAttribute("thanhCong", "Tạo tài khoản \"" + dto.getTenTaiKhoan() + "\" thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/taikhoan";
    }

  
    @PostMapping("/cap-nhat/{maND}")
    public String capNhat(@PathVariable String maND,
                          @ModelAttribute TaiKhoanDTO dto,
                          RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.capNhatTaiKhoan(maND, dto);
            redirectAttributes.addFlashAttribute("thanhCong", "Cập nhật tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/taikhoan";
    }

    @PostMapping("/xoa/{maND}")
    public String xoa(@PathVariable String maND, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.xoaTaiKhoan(maND);
            redirectAttributes.addFlashAttribute("thanhCong", "Đã khoá tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/taikhoan";
    }

    @GetMapping("/vai-tro")
    public String danhSachVaiTro(Model model) {
        model.addAttribute("vaiTroList", taiKhoanService.getDanhSachVaiTroQuanLy());
        model.addAttribute("activeTab", "vaiTro");
        return "QLTK/vai-tro";
    }

    @GetMapping("/vai-tro/them")
    public String formThemVaiTro(Model model) {
        model.addAttribute("vaiTro", new VaiTro());
        model.addAttribute("mode", "create");
        return "QLTK/vai-tro-form";
    }

    @PostMapping("/vai-tro/them")
    public String themVaiTro(@ModelAttribute VaiTro vaiTro, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.taoVaiTro(vaiTro);
            redirectAttributes.addFlashAttribute("thanhCong", "Tạo vai trò thành công!");
            return "redirect:/taikhoan/vai-tro";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
            return "redirect:/taikhoan/vai-tro/them";
        }
    }

    @GetMapping("/vai-tro/{maVaiTro}/sua")
    public String formSuaVaiTro(@PathVariable String maVaiTro, Model model, RedirectAttributes redirectAttributes) {
        Optional<VaiTro> vaiTroOpt = taiKhoanService.timVaiTro(maVaiTro);
        if (vaiTroOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("loi", "Không tìm thấy vai trò.");
            return "redirect:/taikhoan/vai-tro";
        }
        model.addAttribute("vaiTro", vaiTroOpt.get());
        model.addAttribute("mode", "edit");
        model.addAttribute("nguoiDungCoVaiTro", taiKhoanService.getNguoiDungCoVaiTro(maVaiTro));
        model.addAttribute("tatCaNguoiDung", taiKhoanService.getDanhSachNguoiDung());
        model.addAttribute("roleDisplayName", taiKhoanService.getRoleDisplayName(maVaiTro));
        return "QLTK/vai-tro-form";
    }

    @PostMapping("/vai-tro/{maVaiTro}/sua")
    public String suaVaiTro(@PathVariable String maVaiTro, @ModelAttribute VaiTro vaiTro, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.capNhatVaiTro(maVaiTro, vaiTro);
            redirectAttributes.addFlashAttribute("thanhCong", "Cập nhật vai trò thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/taikhoan/vai-tro/" + maVaiTro + "/sua";
    }

    @PostMapping("/vai-tro/{maVaiTro}/xoa")
    public String xoaVaiTro(@PathVariable String maVaiTro, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.xoaVaiTro(maVaiTro);
            redirectAttributes.addFlashAttribute("thanhCong", "Xóa vai trò thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/taikhoan/vai-tro";
    }

    @PostMapping("/vai-tro/{maVaiTro}/gan-nguoi-dung")
    public String ganNguoiDung(@PathVariable String maVaiTro, @RequestParam String maND, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.ganVaiTroChoNguoiDung(maVaiTro, maND);
            redirectAttributes.addFlashAttribute("thanhCong", "Đã gán vai trò cho người dùng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/taikhoan/vai-tro/" + maVaiTro + "/sua";
    }

    @PostMapping("/vai-tro/{maVaiTro}/go-nguoi-dung")
    public String goNguoiDung(@PathVariable String maVaiTro, @RequestParam String maND, RedirectAttributes redirectAttributes) {
        try {
            taiKhoanService.goVaiTroKhoiNguoiDung(maVaiTro, maND);
            redirectAttributes.addFlashAttribute("thanhCong", "Đã gỡ vai trò khỏi người dùng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/taikhoan/vai-tro/" + maVaiTro + "/sua";
    }
}
