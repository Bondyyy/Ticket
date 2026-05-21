package com.dede.ticketsystem.service;

import com.dede.ticketsystem.model.Ghe;
import com.dede.ticketsystem.model.DonHang;
import com.dede.ticketsystem.model.Ve;
import com.dede.ticketsystem.model.KhuVuc;
import com.dede.ticketsystem.model.SuKien;
import com.dede.ticketsystem.model.GiaoDichThanhToan;
import com.dede.ticketsystem.repository.GheRepository;
import com.dede.ticketsystem.repository.DonHangRepository;
import com.dede.ticketsystem.repository.VeRepository;
import com.dede.ticketsystem.repository.KhuVucRepository;
import com.dede.ticketsystem.repository.SuKienRepository;
import com.dede.ticketsystem.repository.GiaoDichThanhToanRepository;
import com.dede.ticketsystem.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class BookingService {

    private final GheRepository gheRepository;
    private final DonHangRepository donHangRepository;
    private final VeRepository veRepository;
    private final KhuVucRepository khuVucRepository;
    private final SuKienRepository suKienRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final IdGeneratorService idGeneratorService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private LogHanhViService logHanhViService;

    @Autowired
    private QueueService queueService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private BookingService self;

    public BookingService(GheRepository gheRepository, 
                          DonHangRepository donHangRepository, 
                          VeRepository veRepository, 
                          KhuVucRepository khuVucRepository,
                          SuKienRepository suKienRepository,
                          GiaoDichThanhToanRepository giaoDichThanhToanRepository,
                          IdGeneratorService idGeneratorService) {
        this.gheRepository = gheRepository;
        this.donHangRepository = donHangRepository;
        this.veRepository = veRepository;
        this.khuVucRepository = khuVucRepository;
        this.suKienRepository = suKienRepository;
        this.giaoDichThanhToanRepository = giaoDichThanhToanRepository;
        this.idGeneratorService = idGeneratorService;
    }

    @Transactional
    public String lockSeats(List<String> maGheList, String maSK, String maKH, String queueToken) {
        if (maGheList == null || maGheList.isEmpty()) {
            throw new RuntimeException("Danh sách ghế chọn không được để trống!");
        }

        // Sort danh sách maGhe tăng dần để giảm nguy cơ deadlock
        java.util.Collections.sort(maGheList);

        // Dùng PESSIMISTIC_WRITE để khóa các bản ghi GHENGOI
        List<Ghe> gheList = gheRepository.findAllByIdWithLock(maGheList);

        // Kiểm tra số lượng ghế load được phải bằng số lượng maGhe request
        if (gheList.size() != maGheList.size()) {
            throw new RuntimeException("Một số ghế không tồn tại hoặc không hợp lệ!");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Validate backend
        SuKien suKien = suKienRepository.findById(maSK)
                .orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại!"));

        // Sự kiện có TrangThaiSK = "Đang mở bán"
        if (!"Đang mở bán".equalsIgnoreCase(suKien.getTrangThaiSK())) {
            throw new RuntimeException("Sự kiện hiện không trong trạng thái mở bán!");
        }

        // Thời gian hiện tại nằm giữa ThoiGianMoBan và ThoiGianDongBan
        if (suKien.getThoiGianMoBan() != null && now.before(suKien.getThoiGianMoBan())) {
            throw new RuntimeException("Thời gian mở bán vé sự kiện chưa bắt đầu!");
        }
        if (suKien.getThoiGianDongBan() != null && now.after(suKien.getThoiGianDongBan())) {
            throw new RuntimeException("Thời gian bán vé sự kiện đã kết thúc!");
        }

        // Validate trạng thái của từng ghế
        for (Ghe ghe : gheList) {
            if (!maSK.equals(ghe.getMaSK())) {
                throw new RuntimeException("Ghế " + ghe.getTenGhe() + " không thuộc sự kiện đang chọn!");
            }
            if (!"Trống".equals(ghe.getTrangThaiGhe())) {
                throw new RuntimeException("Ghế " + ghe.getTenGhe() + " đã được bán hoặc đang được chọn bởi người khác!");
            }
        }

        // Kiểm tra SoVeToiDaPerKH theo từng khu vực và tổng số vé khách đã mua trước đó cho sự kiện/khu vực
        java.util.Map<String, Integer> gheChonTheoKhuVuc = new java.util.HashMap<>();
        for (Ghe ghe : gheList) {
            gheChonTheoKhuVuc.put(ghe.getMaKhuVuc(), gheChonTheoKhuVuc.getOrDefault(ghe.getMaKhuVuc(), 0) + 1);
        }

        java.math.BigDecimal tongTien = java.math.BigDecimal.ZERO;
        for (java.util.Map.Entry<String, Integer> entry : gheChonTheoKhuVuc.entrySet()) {
            String maKhuVuc = entry.getKey();
            int soGheChon = entry.getValue();

            KhuVuc kv = khuVucRepository.findById(maKhuVuc)
                    .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại!"));

            int maxVe = (kv.getSoVeToiDaPerKH() != null && kv.getSoVeToiDaPerKH() > 0) ? kv.getSoVeToiDaPerKH() : 4;

            long veDaMua = veRepository.countBoughtTicketsByKHAndSKAndKhuVuc(maKH, maSK, maKhuVuc);

            if (soGheChon + veDaMua > maxVe) {
                throw new RuntimeException("Bạn đã chọn hoặc mua tổng cộng " + (soGheChon + veDaMua) + 
                        " vé ở khu vực " + kv.getTenKhuVuc() + ". Giới hạn tối đa là " + maxVe + " vé!");
            }

            java.math.BigDecimal giaVe = (kv.getGiaVe() != null) ? kv.getGiaVe() : java.math.BigDecimal.ZERO;
            tongTien = tongTien.add(giaVe.multiply(new java.math.BigDecimal(soGheChon)));
        }

        // Nếu đang bật hàng đợi ảo thì queueToken phải hợp lệ.
        if (queueService.shouldQueue(maSK)) {
            if (queueToken == null || !queueService.validateQueueToken(queueToken, maKH, maSK)) {
                throw new RuntimeException("Bạn cần vào hàng đợi trước khi đặt vé.");
            }
        }

        // Tạo DONHANG trạng thái "Chờ thanh toán"
        String maDonHang = idGeneratorService.nextDonHangId();
        Timestamp thoiGianHetHan = new Timestamp(now.getTime() + 10 * 60 * 1000); // now + 10 phút

        DonHang dh = new DonHang();
        dh.setMaDonHang(maDonHang);
        dh.setSoDonHang(maDonHang);
        dh.setTongTien(tongTien);
        dh.setThanhTien(tongTien);
        dh.setTrangThaiDonHang("Chờ thanh toán");
        dh.setThoiGianDat(now);
        dh.setThoiGianHetHan(thoiGianHetHan);
        dh.setCapNhatLanCuoi(now);
        dh.setMaKH(maKH);

        donHangRepository.save(dh);

        // Cập nhật GHENGOI
        for (Ghe ghe : gheList) {
            ghe.setTrangThaiGhe("Đang chọn");
            ghe.setThoiGianKhoaTam(thoiGianHetHan);
            ghe.setMaPhienKhoa(maDonHang);
        }
        gheRepository.saveAll(gheList);

        return maDonHang;
    }

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Transactional
    public void processCheckout(String orderId, String maKH, String paymentMethod, String simulateResult) {
        PaymentService paymentService = applicationContext.getBean(PaymentService.class);
        paymentService.processPayment(orderId, maKH, paymentMethod, simulateResult);
    }

    @Transactional
    public void completePaidOrder(String orderId) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        DonHang dh = donHangRepository.findByIdWithLock(orderId).orElse(null);
        if (dh == null) return;

        // Nếu DONHANG đã "Đã thanh toán" và đã có VE thì return ngay.
        if ("Đã thanh toán".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            List<Ve> existingTickets = veRepository.findByMaDonHang(orderId);
            if (!existingTickets.isEmpty()) {
                return;
            }
        }

        // Idempotent: chỉ chuyển trạng thái nếu chưa Đã thanh toán
        if (!"Đã thanh toán".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            dh.setTrangThaiDonHang("Đã thanh toán");
            dh.setCapNhatLanCuoi(now);
            donHangRepository.save(dh);
        }

        // Sinh vé và cập nhật trạng thái ghế
        issueTicketsAfterPaymentSuccess(orderId);

        // Gửi email giả lập (LICHSUGUI_EMAIL & console)
        try {
            com.dede.ticketsystem.model.KhachHang kh = khachHangRepository.findById(dh.getMaKH()).orElse(null);
            String email = null;
            if (kh != null && kh.getNguoiDung() != null) {
                email = kh.getNguoiDung().getEmail();
            }

            if (email != null && !email.trim().isEmpty()) {
                // 1. Ghi log XAC_NHAN_DON_HANG
                try {
                    emailService.sendOrderConfirmationEmail(email, dh);
                } catch (Exception e) {
                    System.err.println("Cảnh báo: Lỗi khi gửi email xác nhận: " + e.getMessage());
                }

                // 2. Ghi log QR_CODE cho từng vé mới tạo
                List<Ve> listVe = veRepository.findByMaDonHang(orderId);
                for (Ve ve : listVe) {
                    try {
                        emailService.sendTicketQREmail(email, ve);
                    } catch (Exception e) {
                        System.err.println("Cảnh báo: Lỗi khi gửi email QR: " + e.getMessage());
                    }
                }
            } else {
                System.err.println("Cảnh báo: Không tìm thấy email cho khách hàng: " + dh.getMaKH() + ". Không thực hiện gửi email.");
            }
        } catch (Exception e) {
            System.err.println("Cảnh báo: Lỗi khi xử lý ghi log email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public boolean hasSuccessfulPayment(String orderId) {
        return giaoDichThanhToanRepository.countByMaDonHangAndTrangThaiGD(orderId, "Thành công") > 0;
    }


    @Transactional
    public void issueTicketsAfterPaymentSuccess(String orderId) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Lấy danh sách ghế bằng findByMaPhienKhoa(orderId)
        List<Ghe> gheList = gheRepository.findByMaPhienKhoa(orderId);

        // Dùng danh sách đó để tạo vé
        for (Ghe ghe : gheList) {
            // Chỉ tạo vé khi chưa tồn tại vé cho cặp MaDonHang + MaGhe
            if (!veRepository.existsByMaDonHangAndMaGhe(orderId, ghe.getMaGhe())) {
                KhuVuc kv = khuVucRepository.findById(ghe.getMaKhuVuc())
                        .orElseThrow(() -> new RuntimeException("Khu vực của ghế không tồn tại!"));

                String maVe = idGeneratorService.nextVeId();
                String maQR = buildQrCode(maVe);

                Ve ve = new Ve();
                ve.setMaVe(maVe);
                ve.setMaQR(maQR);
                ve.setGiaVe(kv.getGiaVe() != null ? kv.getGiaVe() : java.math.BigDecimal.ZERO);
                ve.setTrangThaiVe("Chưa sử dụng");
                ve.setThoiGianPhat(now);
                ve.setMaSK(ghe.getMaSK());
                ve.setMaGhe(ghe.getMaGhe());
                ve.setMaDonHang(orderId);

                veRepository.save(ve);

                // Cập nhật SUKIEN.SoVeDaBan
                SuKien sk = suKienRepository.findById(ghe.getMaSK()).orElse(null);
                if (sk != null) {
                    sk.setSoVeDaBan((sk.getSoVeDaBan() != null ? sk.getSoVeDaBan() : 0) + 1);
                    suKienRepository.save(sk);
                }

                // Cập nhật KHUVUC.SoGheDaBan
                kv.setSoGheDaBan((kv.getSoGheDaBan() != null ? kv.getSoGheDaBan() : 0) + 1);
                khuVucRepository.save(kv);
            }
        }

        // Sau khi tạo vé xong mới clear ThoiGianKhoaTam và MaPhienKhoa, và cập nhật GHENGOI = "Đã bán"
        for (Ghe ghe : gheList) {
            ghe.setTrangThaiGhe("Đã bán");
            ghe.setThoiGianKhoaTam(null);
            ghe.setMaPhienKhoa(null);
        }
        gheRepository.saveAll(gheList);
    }

    @Transactional
    public void cancelOrder(String orderId, String maKH) {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Khóa DONHANG bằng PESSIMISTIC_WRITE trước khi xử lý
        DonHang dh = donHangRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        // Kiểm tra đơn hàng thuộc maKH hiện tại
        if (!dh.getMaKH().equals(maKH)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này!");
        }

        // Trạng thái còn "Chờ thanh toán"
        if (!"Chờ thanh toán".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ thanh toán để có thể hủy!");
        }

        // Nhận maSK của đơn hàng từ các ghế đang khóa trước khi nhả
        String maSK = null;
        List<Ghe> seats = gheRepository.findByMaPhienKhoa(orderId);
        if (!seats.isEmpty()) {
            maSK = seats.get(0).getMaSK();
        }

        // Đổi trạng thái sang Đã hủy
        dh.setTrangThaiDonHang("Đã hủy");
        dh.setCapNhatLanCuoi(now);
        donHangRepository.save(dh);

        // Nhả ghế
        releaseSeats(orderId);

        // Ghi log hành vi BO_GIO_HANG
        if (maSK != null) {
            try {
                logHanhViService.log("BO_GIO_HANG", maSK, maKH, "Web");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional
    public void releaseSeats(String orderId) {
        List<Ghe> gheList = gheRepository.findByMaPhienKhoa(orderId);
        for (Ghe ghe : gheList) {
            if ("Đang chọn".equals(ghe.getTrangThaiGhe())) {
                ghe.setTrangThaiGhe("Trống");
                ghe.setThoiGianKhoaTam(null);
                ghe.setMaPhienKhoa(null);
            }
        }
        gheRepository.saveAll(gheList);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void cancelAndReleaseExpiredOrder(String orderId) {
        DonHang dh = donHangRepository.findByIdWithLock(orderId).orElse(null);
        if (dh == null) {
            return;
        }

        // Chỉ hủy nếu đơn hàng ở trạng thái "Chờ thanh toán"
        if (!"Chờ thanh toán".equalsIgnoreCase(dh.getTrangThaiDonHang())) {
            return;
        }

        String maKH = dh.getMaKH();
        dh.setTrangThaiDonHang("Đã hủy");
        dh.setCapNhatLanCuoi(new Timestamp(System.currentTimeMillis()));
        donHangRepository.save(dh);

        String maSK = null;
        List<Ghe> seats = gheRepository.findByMaPhienKhoa(orderId);
        if (!seats.isEmpty()) {
            maSK = seats.get(0).getMaSK();
        }

        releaseSeats(orderId);

        // Ghi log hành vi BO_GIO_HANG
        if (maSK != null) {
            try {
                logHanhViService.log("BO_GIO_HANG", maSK, maKH, "Web");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void saveGiaoDichNewTransaction(GiaoDichThanhToan gd) {
        giaoDichThanhToanRepository.save(gd);
    }

    private String buildQrCode(String maVe) {
        String base = "QR-" + maVe;
        String candidate = base;
        int suffix = 1;
        while (veRepository.existsByMaQR(candidate)) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        return candidate;
    }
}
