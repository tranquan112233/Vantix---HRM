# Hướng Dẫn Cho Agent

## Tổng Quan Dự Án

Vantix là ứng dụng backend Java dùng Spring Boot, đóng gói dạng WAR và đặt package gốc là `poly.edu.vantix`.

Stack chính:

- Java 17
- Spring Boot 4.0.5
- Maven Wrapper (`mvnw.cmd`)
- Spring Web MVC, Spring Data JPA, Spring Security, Validation, Mail
- MySQL runtime driver
- Lombok
- JJWT 0.12.6
- Một số dependency frontend trong `package.json`: Element Plus, Pinia, Axios

## Cấu Trúc Chính

- `src/main/java/poly/edu/vantix/VantixApplication.java`: entry point của ứng dụng.
- `src/main/java/poly/edu/vantix/ServletInitializer.java`: hỗ trợ deploy WAR.
- `src/main/java/poly/edu/vantix/config`: cấu hình ứng dụng, audit JPA và seeding dữ liệu.
- `src/main/java/poly/edu/vantix/controller`: REST controller.
- `src/main/java/poly/edu/vantix/dto`: request/response DTO.
- `src/main/java/poly/edu/vantix/entity`: JPA entity và enum nghiệp vụ.
- `src/main/java/poly/edu/vantix/exception`: exception và handler toàn cục.
- `src/main/java/poly/edu/vantix/repository`: Spring Data repository.
- `src/main/resources/application.properties`: cấu hình runtime.
- `src/test/java`: test JUnit.

## Lệnh Thường Dùng

Chạy test:

```powershell
.\mvnw.cmd test
```

Biên dịch không chạy test:

```powershell
.\mvnw.cmd -DskipTests compile
```

Đóng gói WAR:

```powershell
.\mvnw.cmd -DskipTests package
```

Chạy ứng dụng bằng Spring Boot Maven plugin:

```powershell
.\mvnw.cmd spring-boot:run
```

Nếu dùng dependency frontend, cài đặt bằng:

```powershell
npm install
```

Hiện `package.json` chưa khai báo script npm, nên không tự giả định có `npm run build` hoặc `npm run dev`.

## Cấu Hình Runtime

`application.properties` đang dùng biến môi trường cho JWT:

- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_EXPIRATION`

Cấu hình datasource MySQL đang được để mẫu dưới dạng comment. Khi cần chạy app với database thật, cấu hình bằng biến môi trường, profile riêng, hoặc bật/cập nhật các dòng `spring.datasource.*` và `spring.jpa.hibernate.ddl-auto`.

Không commit secret thật, password database, token, hoặc thông tin môi trường cá nhân.

## Quy Ước Code

- Giữ package dưới `poly.edu.vantix`.
- Với entity, ưu tiên JPA annotation từ `jakarta.persistence` và dùng Lombok theo style hiện có (`@Getter`, `@Setter`, `@Data`, `@Builder` khi phù hợp).
- Entity dùng `BaseEntity` cho id, audit và soft delete nếu thuộc domain chính.
- Enum nghiệp vụ đặt trong `entity/enums`.
- DTO request/response đặt đúng nhánh `dto/request` hoặc `dto/response`.
- Exception nghiệp vụ dùng `BusinessException`; lỗi API thống nhất qua `GlobalExceptionHandler` và `ErrorResponse`.
- Comment hiện có chủ yếu bằng tiếng Việt. Khi cần comment, viết ngắn, giải thích nghiệp vụ hoặc đoạn code khó hiểu; không comment lại điều hiển nhiên.

## Kiểm Thử Và Xác Minh

- Với thay đổi backend, tối thiểu chạy `.\mvnw.cmd test` nếu có thể.
- Với thay đổi chỉ tài liệu hoặc cấu hình không ảnh hưởng compile, có thể không chạy test nhưng cần nêu rõ trong phản hồi cuối.
- Nếu thay đổi entity/repository/service liên quan database, cân nhắc thêm test phù hợp hoặc ít nhất chạy compile để bắt lỗi annotation, Lombok và dependency.

## Lưu Ý Khi Làm Việc

- Thư mục hiện tại không có metadata `.git`; không dựa vào lịch sử Git.
- Không sửa `target/`, `node_modules/`, `.idea/` trừ khi người dùng yêu cầu rõ.
- Không đổi packaging WAR trong `pom.xml` nếu chưa có lý do cụ thể.
- Tránh refactor rộng khi chỉ cần sửa một lỗi nhỏ.
- Khi thêm dependency Maven hoặc npm, cập nhật manifest tương ứng và xác minh build/install nếu môi trường cho phép.
