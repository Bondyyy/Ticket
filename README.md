<p align="center">
  <a href="https://www.uit.edu.vn/" title="Trường Đại học Công nghệ Thông tin">
    <img src="https://i.imgur.com/WmMnSRt.png" alt="Trường Đại học Công nghệ Thông tin | University of Information Technology">
  </a>
</p>

<!-- Title -->
<h1 align="center"><b>IS210 - QUẢN LÝ DỰ ÁN CÔNG NGHỆ THÔNG TIN</b></h1>

---

## BẢNG MỤC LỤC

* [Giới thiệu môn học](#gioithieumonhoc)
* [Giảng viên hướng dẫn](#giangvien)
* [Thành viên nhóm](#thanhvien)
* [Đồ án môn học](#doan)
* [Công nghệ sử dụng](#congnghe)
* [Hướng dẫn cài đặt](#caidat)

---

## GIỚI THIỆU MÔN HỌC

<a name="gioithieumonhoc"></a>

* **Tên môn học**: Quản lý dự án Công nghệ thông tin
* **Mã môn học**: IS210
* **Lớp học**: IS210.Q23
* **Năm học**: 2025-2026

---

## GIẢNG VIÊN HƯỚNG DẪN

<a name="giangvien"></a>

* **Giảng viên hướng dẫn**: Cập nhật sau

---

## THÀNH VIÊN NHÓM

<a name="thanhvien"></a>

| STT | MSSV     | Họ và Tên           | Email                  |
| --- | :------: | ------------------- | ---------------------- |
| 1   | 24520663 | Lai Mộc Huy         | laimochuy@gmail.com    |
| 2   | 24520409 | Sơn Nguyễn Kỳ Duyên | 24520409@gm.uit.edu.vn |
| 3   | 24520336 | Huỳnh Đức Dũng      | 24520336@gm.uit.edu.vn |
| 4   | 24520322 | Nguyễn Thành Đức    | 24520322@gm.uit.edu.vn |
| 5   | 24520319 | Nguyễn Minh Đức     | 24520319@gm.uit.edu.vn |

---

## ĐỒ ÁN MÔN HỌC

<a name="doan"></a>

* **Mã đồ án**: ITPJ2602
* **Tên đồ án**: Hệ thống quản lý sự kiện và bán vé trực tuyến
* **Loại đồ án**: Web Application và 01 module Mobile App đơn giản dùng cho soát vé

### Mô tả đồ án

Dề Dê Tickets là hệ thống hỗ trợ các đơn vị tổ chức sự kiện quản lý sự kiện, mở bán vé trực tuyến, kiểm soát ghế ngồi, xử lý thanh toán giả lập, phát hành vé điện tử bằng mã QR và hỗ trợ nhân viên soát vé tại cổng.

Hệ thống hướng đến việc giải quyết các vấn đề như vé giả, quá tải khi mở bán vé giờ cao điểm, soát vé chậm và thiếu báo cáo doanh thu/phân tích dữ liệu.

### Chức năng chính

* Quản lý sự kiện
* Quản lý địa điểm, khu vực và sơ đồ ghế
* Đặt vé và giữ ghế tạm thời
* Hàng đợi ảo khi mở bán vé cao điểm
* Thanh toán online giả lập
* Retry khi thanh toán thất bại
* Phát hành vé điện tử kèm mã QR
* Module soát vé dành cho nhân viên
* Nhập mã vé thủ công khi không sử dụng được camera
* Mô phỏng offline scan trong thời gian ngắn
* Quản lý đơn hàng, vé và tài khoản người dùng
* Báo cáo doanh thu và thống kê bán vé

---

## CÔNG NGHỆ SỬ DỤNG

<a name="congnghe"></a>

* **Ngôn ngữ**: Java 17
* **Framework**: Spring Boot, Spring MVC, Spring Security
* **Template Engine**: Thymeleaf
* **Giao diện**: HTML, CSS, JavaScript
* **Cơ sở dữ liệu**: MySQL
* **ORM**: Spring Data JPA, Hibernate
* **Build tool**: Maven
* **Thư viện hỗ trợ**: ZXing QR Code, Thymeleaf Layout Dialect
* **Module soát vé**: Web Responsive / PWA mô phỏng Mobile App đơn giản

---

## HƯỚNG DẪN CÀI ĐẶT

<a name="caidat"></a>

### 1. Clone project về máy

```bash
git clone <repository-url>
cd ticket-booking-system
```

### 2. Tạo database MySQL

Mở MySQL và tạo database cho project:

```sql
CREATE DATABASE IF NOT EXISTS dede_tickets
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Có thể tạo user riêng cho project:

```sql
CREATE USER IF NOT EXISTS 'dede'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON dede_tickets.* TO 'dede'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Build project bằng Maven

```bash
mvn clean package
```

Hoặc bỏ qua test khi build:

```bash
mvn clean package -DskipTests
```

### 4. Chạy ứng dụng

```bash
mvn spring-boot:run
```

### 5. Truy cập hệ thống

```text
http://localhost:8080
```

---

## TÀI KHOẢN DEMO

| Tài khoản | Mật khẩu | Vai trò   |
| --------- | -------- | --------- |
| admin     | 123456   | Admin     |
| organizer | 123456   | Organizer |
| staff     | 123456   | Staff     |
| customer  | 123456   | Customer  |

---

## URL DEMO CHÍNH

| URL | Chức năng |
| --- | --------- |
| `/` | Trang chủ |
| `/dang-nhap` | Đăng nhập |
| `/dang-ky` | Đăng ký |
| `/su-kien` | Danh sách sự kiện |
| `/mua-ve/{maSK}` | Chọn ghế và đặt vé |
| `/thanh-toan?orderId=...` | Thanh toán giả lập |
| `/ve-cua-toi` | Xem vé của khách hàng |
| `/don-hang-cua-toi` | Xem đơn hàng của khách hàng |
| `/soat-ve` | Module soát vé dành cho nhân viên |
| `/baocao` | Dashboard báo cáo doanh thu |
| `/sukien` | Quản lý sự kiện |
| `/donhang` | Quản lý đơn hàng |
| `/ve` | Quản lý vé |

---

## GHI CHÚ

* Project sử dụng MySQL làm cơ sở dữ liệu runtime chính.
* Payment Gateway được mô phỏng để phục vụ demo luồng thanh toán.
* Module Mobile App soát vé được triển khai theo hướng Web Responsive/PWA để chạy được trên trình duyệt điện thoại.
