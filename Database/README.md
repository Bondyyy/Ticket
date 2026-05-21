# Oracle Legacy Reference

Các file trong thư mục này là script Oracle/PLSQL legacy của đồ án cũ. Ứng dụng Spring Boot hiện chạy MySQL và không tự execute bất kỳ file nào trong `Database/`.

Nguồn nghiệp vụ runtime nằm trong Java service/JPA và `DataInitializer`; MySQL local dùng `ddl-auto=update` để tạo/cập nhật schema cần thiết.
