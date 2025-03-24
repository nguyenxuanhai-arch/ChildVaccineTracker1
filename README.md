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

## Triển Khai với Docker

Dự án này được cấu hình để dễ dàng triển khai sử dụng Docker và Docker Compose, giúp đảm bảo môi trường chạy nhất quán và cô lập.

### Yêu Cầu Tiên Quyết

- [Docker](https://docs.docker.com/get-docker/) đã được cài đặt
- [Docker Compose](https://docs.docker.com/compose/install/) đã được cài đặt (thường đi kèm với Docker Desktop)

### Triển Khai Ứng Dụng với Docker Compose

1. **Sử dụng script triển khai tự động:**
   ```bash
   ./docker-deploy.sh
   ```
   Script này sẽ tự động kiểm tra Docker, xây dựng và khởi động các containers.

2. **Hoặc build và khởi động các containers theo cách thủ công:**
   ```bash
   docker-compose up -d
   ```
   
   Quá trình này sẽ:
   - Build image Docker cho ứng dụng Spring Boot
   - Tạo container MySQL với cấu hình đã được thiết lập
   - Kết nối các containers thông qua mạng nội bộ
   - Lưu trữ dữ liệu MySQL trong volume Docker để dữ liệu được giữ lại giữa các lần chạy

3. **Xem logs của ứng dụng:**
   ```bash
   docker-compose logs -f app
   ```

4. **Dừng các containers:**
   ```bash
   docker-compose down
   ```

5. **Xóa tất cả các containers và volumes:**
   ```bash
   docker-compose down -v
   ```

### Triển Khai Chỉ Ứng Dụng với Docker

Nếu bạn đã có sẵn MySQL hoặc muốn triển khai ứng dụng riêng biệt:

1. **Build Docker image:**
   ```bash
   docker build -t vaccine-management-app .
   ```

2. **Chạy container:**
   ```bash
   docker run -d -p 8080:8080 \
     -e SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/vaccine_management \
     -e SPRING_DATASOURCE_USERNAME=your-username \
     -e SPRING_DATASOURCE_PASSWORD=your-password \
     --name vaccine-app \
     vaccine-management-app
   ```

3. **Xem logs:**
   ```bash
   docker logs -f vaccine-app
   ```

4. **Dừng container:**
   ```bash
   docker stop vaccine-app
   ```

5. **Xóa container:**
   ```bash
   docker rm vaccine-app
   ```

### Môi Trường Production

Để triển khai trong môi trường sản xuất, bạn nên:

1. Sử dụng biến môi trường để cấu hình:
   - Thông tin kết nối cơ sở dữ liệu
   - Khóa bí mật JWT
   - Cấu hình email
   - Các thông số bảo mật khác

2. Cấu hình HTTPS/TLS (có thể sử dụng Nginx làm reverse proxy)

3. Thiết lập giám sát và ghi log

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