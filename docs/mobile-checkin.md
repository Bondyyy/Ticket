# Dề Dê Check-in Mobile PWA

## Mục tiêu

`Dề Dê Check-in` là module mobile web/PWA dành cho nhân viên soát vé. Module dùng lại API và nghiệp vụ soát vé hiện có của hệ thống, không phải native APK và không tạo luồng xác thực vé song song.

## Route sử dụng

- Mobile staff app: `/staff-app`
- Validate vé: `POST /api/soat-ve/validate`
- Đồng bộ offline: `POST /api/soat-ve/sync`
- Lịch sử gần nhất: `GET /api/soat-ve/history`

## Tài khoản test

- `staff001 / 123456`
- Hoặc `staff / 123456` nếu database local của bạn đang dùng bộ seed cũ.
- `admin / 123456` cũng có quyền mở module.

## Chạy local

```bash
mvn spring-boot:run
```

Sau đó mở:

```text
http://localhost:8080/staff-app
```

## Test trên điện thoại cùng Wi-Fi

1. Lấy IP LAN của máy đang chạy Spring Boot, ví dụ `192.168.1.x`.
2. Trên điện thoại mở:

```text
http://192.168.1.x:8080/staff-app
```

Lưu ý: camera trên HTTP LAN có thể bị một số browser chặn. Nếu bị chặn, hãy dùng deploy HTTPS, tunnel HTTPS, hoặc nhập mã thủ công.

## Cài như app

- Android Chrome: mở menu `⋮` rồi chọn `Install app` hoặc `Add to Home Screen`.
- iPhone Safari: bấm `Share` rồi chọn `Add to Home Screen`.

## Demo offline

1. Mở `/staff-app`.
2. Chọn sự kiện.
3. Tắt mạng hoặc bật offline trong DevTools.
4. Quét hoặc nhập mã vé.
5. App hiển thị trạng thái pending offline, không xác nhận vé hợp lệ.
6. Bật mạng lại.
7. Bấm `Đồng bộ ngay` hoặc chờ app tự sync.
8. Kết quả thật được xử lý bởi `/api/soat-ve/sync`.

## Ghi chú

Đây là PWA/mobile web module, không phải native APK. Module không lưu mật khẩu/token vào `localStorage`; chỉ lưu lựa chọn sự kiện, cổng soát, device id cục bộ và hàng đợi lượt quét offline.
