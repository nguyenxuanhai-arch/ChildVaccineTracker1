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
2. Cấu hình cơ sở dữ liệu trong `src/main/resources/application.properties` hoặc `src/main/resources/application-dev.properties`.
3. Clone dự án:
   ```bash
   git clone https://github.com/your-repo/vaccine-management.git
   cd vaccine-management
   ```

### Sử Dụng Môi Trường Maven

Dự án sử dụng Maven Wrapper nên bạn không cần cài đặt Maven trên hệ thống.

1. **Chạy ứng dụng trong môi trường phát triển:**
   ```bash
   ./run-dev.sh
   ```
   Hoặc
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Chạy ứng dụng trong môi trường sản xuất:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Build dự án:**
   ```bash
   ./mvnw clean package
   ```

4. **Chạy các tests:**
   ```bash
   ./mvnw test
   ```

5. **Để cài đặt các phụ thuộc mới:**
   Thêm phụ thuộc vào tệp `pom.xml` trong phần `<dependencies>`, Maven sẽ tự động tải về khi chạy.

Truy cập ứng dụng tại `http://localhost:8080` (môi trường phát triển) hoặc `http://localhost:8000` (môi trường sản xuất).

## Tài Liệu API

### Swagger UI

API RESTful của hệ thống được tài liệu hóa đầy đủ và có thể truy cập tại `/swagger-ui/index.html` sau khi khởi động ứng dụng. Swagger UI cung cấp:

- Danh sách đầy đủ các API endpoints
- Mô tả chi tiết về các tham số và phản hồi
- Giao diện tương tác để kiểm thử các API trực tiếp

### Sử Dụng Swagger UI:

1. Khởi động ứng dụng
2. Truy cập `http://localhost:8080/swagger-ui/index.html` (môi trường phát triển)
3. Xác thực (nếu cần) bằng cách:
   - Sử dụng endpoint `/api/auth/login` để nhận JWT token
   - Nhấp vào nút "Authorize" ở góc phải và nhập token theo định dạng `Bearer your_token_here`
   - Bây giờ bạn có thể gọi các API được bảo vệ

## Giấy Phép

Phần mềm này được phân phối theo giấy phép MIT. Xem tệp LICENSE để biết thêm chi tiết.

## Liên Hệ

Để được hỗ trợ hoặc đóng góp ý kiến, vui lòng liên hệ qua email: support@vaccine-management.com