# JobHunter - Backend RESTful API (Spring Boot)

Hệ thống API cốt lõi cho dự án **JobHunter**, xử lý logic nghiệp vụ tuyển dụng, bảo mật và cơ sở dữ liệu.

---

## 🛠️ Công nghệ sử dụng

- **Framework:** Spring Boot 3.4.2 (LTS)
- **Java Version:** JDK 21 (LTS)
- **Cơ sở dữ liệu:** MySQL (Spring Data JPA & Hibernate)
- **Bảo mật (Security):** Spring Security 6 & OAuth2 Resource Server (JWT)
- **Quản lý Role/Permission:** Dynamic RBAC (User - Role - Permission)
- **Gửi Email:** Spring Boot Starter Mail & Thymeleaf (Xử lý bất đồng bộ `@Async`)
- **Tài liệu API:** SpringDoc OpenAPI 3.0 (Swagger UI)
- **Tiện ích:** Lombok, Spring Filter (Xử lý tìm kiếm nâng cao)

---

## 🏗️ Cấu trúc dự án (Project Structure)

```text
src/main/java/vn/bxh/jobhunter/
├── config/       # Cấu hình Security, CORS, Swagger, v.v.
├── controller/   # Lớp tiếp nhận request (REST Controllers)
├── domain/       # Lớp thực thể (Entities) và DTOs
├── repository/   # Lớp truy vấn dữ liệu (Spring Data JPA)
├── service/      # Lớp xử lý logic nghiệp vụ
└── util/         # Các lớp tiện ích (SecurityUtil, FormatRestResponse)
```

---

## 🚀 Hướng dẫn khởi chạy (Running the project)

### 1. Yêu cầu hệ thống
- **Java JDK 21** trở lên.
- **MySQL 8.x** đang chạy trên máy local hoặc server.
- **Gradle 8.x** (đã đi kèm trong dự án qua Gradle Wrapper).

### 2. Cấu hình Database
Mở file `src/main/resources/application.properties` và cập nhật thông tin kết nối MySQL:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jobhunter_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Chạy ứng dụng
Dùng lệnh sau trong terminal:
```bash
./gradlew bootRun
```

Ứng dụng sẽ mặc định chạy tại cổng **8080**.

---

## 📑 Tài liệu API (Swagger UI)

Sau khi chạy ứng dụng, bạn có thể truy cập tài liệu API tại:
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

## 🌟 Các tính năng chính của Backend
- **Xác thực JWT:** Hỗ trợ Access Token và Refresh Token.
- **Dynamic RBAC:** Hệ thống phân quyền động linh hoạt qua từng API.
- **Tìm kiếm nâng cao:** Sử dụng `spring-filter` để lọc dữ liệu động từ Client.
- **Xử lý Bất đồng bộ:** Tối ưu hiệu năng gửi Email và Thông báo hệ thống.
- **Hệ thống Thông báo (Notification):** Hỗ trợ gửi thông báo quảng bá (Broadcast) tới toàn bộ User.
- **Quên mật khẩu:** Gửi mã xác thực OTP 6 số qua email một cách an toàn.

---

## 📝 Giấy phép
Dự án được thực hiện cho mục đích học tập và đồ án tốt nghiệp.
