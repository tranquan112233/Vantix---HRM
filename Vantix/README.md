# Vantix HRM

Vantix HRM là hệ thống quản lý nhân sự gồm backend Spring Boot và frontend Vue/Vite.

## Cách chạy local

Yêu cầu:

- Java 17+
- MySQL đang chạy local
- Node.js 20+

Database mặc định trong `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vantix_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=root
spring.datasource.password=123456
```

Chạy backend:

```powershell
cd D:\Learning\Vantix
.\mvnw.cmd spring-boot:run
```

Backend chạy tại:

```text
http://localhost:8080
```

Chạy frontend:

```powershell
cd D:\Learning\Vantix\vantix_web
npm install
npm run dev
```

Frontend chạy tại:

```text
http://localhost:3000
```

Frontend proxy các request `/api` về backend `http://localhost:8080`.

## Seed data

Khi `app.seed.enabled=true`, hệ thống tự tạo dữ liệu test khi backend khởi động:

- Permission và role mặc định.
- Tài khoản admin.
- Tài khoản test theo nghiệp vụ.
- Phòng ban, chức vụ, nhân viên mẫu.
- Công việc mẫu.
- Ca làm, địa điểm làm việc, lịch làm việc.
- Lịch sử chấm công 2 tuần gần nhất.
- Đơn nghỉ phép và thông báo mẫu.

Seed chạy theo kiểu idempotent: nếu dữ liệu đã tồn tại thì cập nhật phần chính thay vì tạo trùng.

## Tài khoản test

Mật khẩu admin:

```text
Admin@123
```

Mật khẩu các tài khoản test còn lại:

```text
Test@123
```

| Username | Role | Dùng để test |
| --- | --- | --- |
| `admin` | Admin | Toàn quyền hệ thống, cấu hình role/user, kiểm tra mọi màn hình. |
| `hr_test` | HR | Quản lý nhân viên, công việc, nghỉ phép, gửi thông báo, xem chấm công toàn bộ. |
| `manager_test` | Manager | Duyệt đơn nghỉ, xem chấm công/lịch làm toàn bộ, quản lý công việc. |
| `employee_test` | Employee | Tài khoản nhân viên thường: xem hồ sơ, tạo/sửa/hủy đơn nghỉ của mình, xem task, xem thông báo. |
| `scheduler_test` | Scheduler | Quản lý lịch làm việc, ca làm, địa điểm làm việc và xem chấm công toàn bộ. |
| `notifier_test` | Notifier | Gửi thông báo nội bộ, xem danh sách user để chọn người nhận. |
| `auditor_test` | Auditor | Chỉ xem dữ liệu và nhật ký hệ thống, không có quyền tạo/sửa/xóa nghiệp vụ. |

Ngoài các tài khoản trên, hệ thống còn tạo `user001`, `user002`, ... với mật khẩu:

```text
User@123
```

Một số user mẫu có thể bị khóa để test trạng thái tài khoản.

## Cách hoạt động quyền

Mỗi `User` có một `Role`. Mỗi `Role` chứa nhiều `Permission`.

Backend là nơi chặn quyền thật bằng `@PreAuthorize`. Frontend chỉ ẩn/hiện menu và nút theo permission để trải nghiệm rõ ràng hơn, nhưng bảo mật vẫn nằm ở backend.

Quy ước permission:

- `*_VIEW`: xem danh sách/chi tiết.
- `*_CREATE`: tạo mới.
- `*_UPDATE`: cập nhật.
- `*_DELETE`: xóa.
- `*_VIEW_ALL`: xem dữ liệu toàn bộ, ví dụ chấm công/lịch làm của người khác.
- `*_APPROVE`: duyệt hoặc từ chối.
- `*_MANAGE`: quyền quản lý tổng hợp cho một nghiệp vụ.

Ví dụ:

- Nhân viên thường không có `ATTENDANCE_VIEW_ALL`, nên chỉ xem lịch sử chấm công của mình.
- Manager/HR/Scheduler có `ATTENDANCE_VIEW_ALL`, nên xem được lịch sử chấm công người khác.
- Chỉ user có `NOTIFICATION_SEND` mới thấy nút gửi thông báo và gọi được API gửi thông báo.
- API đọc/xóa thông báo kiểm tra chủ sở hữu thông báo; user không được thao tác thông báo của người khác.

## Luồng nghiệp vụ chính

### Nhân viên

Đăng nhập `employee_test`:

1. Vào `Profile` để xem hồ sơ cá nhân.
2. Vào `Attendance` để chấm công và xem lịch sử của mình.
3. Vào `Leave Requests` để tạo đơn nghỉ.
4. Khi đơn còn `PENDING`, nhân viên có thể sửa hoặc hủy nếu có quyền tương ứng.
5. Vào `Notifications` để đọc/xóa thông báo của mình.

### Quản lý hoặc HR

Đăng nhập `manager_test` hoặc `hr_test`:

1. Vào `Leave Requests`.
2. Chọn phạm vi `All Requests`.
3. Duyệt hoặc từ chối đơn `PENDING` của nhân viên khác.
4. Vào `Attendance` để lọc lịch sử chấm công theo nhân viên.
5. Vào `Tasks` để tạo, cập nhật trạng thái và xóa công việc.

### Điều phối lịch

Đăng nhập `scheduler_test`:

1. Vào `Work Schedules`.
2. Tạo lịch đơn lẻ hoặc tạo lịch hàng loạt.
3. Quản lý `Shifts`.
4. Quản lý `Work Locations`.
5. Vào `Attendance` để kiểm tra dữ liệu chấm công toàn bộ.

### Gửi thông báo

Đăng nhập `notifier_test`:

1. Vào `Notifications`.
2. Bấm `Send`.
3. Chọn người nhận hoặc gửi tất cả.
4. Nhập tiêu đề, nội dung, loại thông báo và link điều hướng.
5. Tài khoản không có `NOTIFICATION_SEND` sẽ không thấy nút này và backend cũng từ chối API gửi.

### Kiểm toán

Đăng nhập `auditor_test`:

1. Xem các màn hình dữ liệu chính.
2. Vào `System Logs` để kiểm tra nhật ký.
3. Tài khoản này không có quyền tạo/sửa/xóa, phù hợp test phân quyền chỉ đọc.

## Lưu ý khi test seed

Nếu database đã có dữ liệu cũ và muốn seed lại sạch:

1. Dừng backend.
2. Xóa database `vantix_db` hoặc truncate các bảng test.
3. Khởi động backend lại.

Không bật seed mặc định cho production. Dùng cấu hình:

```properties
app.seed.enabled=false
```

## Lệnh kiểm tra

Backend:

```powershell
cd D:\Learning\Vantix
.\mvnw.cmd -q test
```

Frontend:

```powershell
cd D:\Learning\Vantix\vantix_web
npm run build
```
