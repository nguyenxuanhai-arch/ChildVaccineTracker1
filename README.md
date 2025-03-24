# Hệ Thống Quản Lý Tiêm Chủng Trẻ Em

Hệ thống quản lý tiêm chủng trẻ em toàn diện giúp các cơ sở y tế và phụ huynh theo dõi, lên lịch và quản lý chăm sóc sức khỏe trẻ em một cách hiệu quả.

## Tính Năng Chính

- **Đăng ký và Đăng nhập**: Hệ thống xác thực an toàn với JWT.
- **Hồ sơ trẻ em**: Quản lý thông tin cá nhân và lịch sử tiêm chủng.
- **Đặt lịch hẹn**: Giao diện đặt lịch trực quan với quy trình 4 bước.
- **Thông báo**: Hệ thống thông báo đa kênh về lịch tiêm chủng sắp tới.
- **Tài liệu vắc-xin**: Thông tin chi tiết về các loại vắc-xin, giá cả và hướng dẫn.
- **Quản lý thanh toán**: Theo dõi và xử lý thanh toán cho các dịch vụ.
- **Báo cáo và thống kê**: Công cụ phân tích dành cho quản trị viên.

## Vai Trò Người Dùng

- **Khách (Guest)**: Xem thông tin công khai về dịch vụ, giá cả và loại vắc-xin.
- **Khách hàng (Customer)**: Quản lý hồ sơ trẻ em, đặt lịch hẹn và theo dõi lịch sử tiêm chủng.
- **Nhân viên (Staff)**: Quản lý lịch hẹn và ghi nhận thông tin tiêm chủng.
- **Quản trị viên (Admin)**: Quản lý hệ thống, người dùng và xem báo cáo.

## Công Nghệ Sử Dụng

- **Backend**: Spring Boot, Spring Security, JPA/Hibernate
- **Frontend**: Thymeleaf, Bootstrap, jQuery
- **Cơ sở dữ liệu**: MySQL
- **Xác thực**: JWT (JSON Web Tokens)

## Cài Đặt và Chạy

1. Đảm bảo máy tính đã cài đặt Java 11 hoặc cao hơn và MySQL.
2. Cấu hình cơ sở dữ liệu trong `src/main/resources/application.properties`.
3. Chạy ứng dụng với Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Truy cập ứng dụng tại `http://localhost:8080`.

## Tài Liệu API

API RESTful của hệ thống được tài liệu hóa đầy đủ và có thể truy cập tại `/swagger-ui.html` sau khi khởi động ứng dụng.

## Giấy Phép

Phần mềm này được phân phối theo giấy phép MIT. Xem tệp LICENSE để biết thêm chi tiết.

## Liên Hệ

Để được hỗ trợ hoặc đóng góp ý kiến, vui lòng liên hệ qua email: support@vaccine-management.com