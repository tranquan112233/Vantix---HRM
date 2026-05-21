# Activity Diagram - Các case chính Vantix HRM

Mỗi case được tách thành một bảng riêng, kèm sơ đồ activity PlantUML riêng để dễ đưa vào báo cáo.

---

## Bảng 1. Công việc

| Thành phần | Nội dung |
|---|---|
| Case | Quản lý công việc |
| Actor chính | Quản lý / Nhân sự |
| Quyền liên quan | `TASK_VIEW`, `TASK_CREATE`, `TASK_UPDATE`, `TASK_DELETE` |
| Đầu vào | Thông tin công việc, người phụ trách, deadline, trạng thái |
| Đầu ra | Danh sách công việc, công việc mới/cập nhật/xóa |

```plantuml
@startuml
title Activity Diagram - Quản lý công việc

|Người dùng|
start
:Đăng nhập hệ thống;
:Mở màn hình Công việc;

|Frontend|
:Gọi API lấy danh sách công việc;

|Backend|
:Kiểm tra JWT;
:Kiểm tra quyền TASK_VIEW;

if (Có quyền?) then (Có)
  :Lọc công việc theo keyword, trạng thái, người phụ trách;
  :Trả danh sách công việc;
else (Không)
  :Trả lỗi 403;
  stop
endif

|Frontend|
:Hiển thị danh sách công việc;

|Người dùng|
if (Chọn hành động?) then (Thêm mới)
  :Nhập thông tin công việc;
  |Frontend|
  :Gửi yêu cầu tạo công việc;
  |Backend|
  :Kiểm tra quyền TASK_CREATE;
  :Validate dữ liệu;
  if (Hợp lệ?) then (Có)
    :Lưu công việc;
    :Tạo thông báo cho người phụ trách nếu có;
    :Trả công việc mới;
  else (Không)
    :Trả lỗi validate;
  endif
elseif (Cập nhật)
  :Sửa thông tin công việc;
  |Frontend|
  :Gửi yêu cầu cập nhật;
  |Backend|
  :Kiểm tra quyền TASK_UPDATE;
  :Cập nhật công việc;
  :Trả kết quả;
else (Xóa)
  :Xác nhận xóa công việc;
  |Frontend|
  :Gửi yêu cầu xóa;
  |Backend|
  :Kiểm tra quyền TASK_DELETE;
  :Xóa mềm công việc;
  :Trả 204;
endif

|Frontend|
:Tải lại danh sách công việc;

|Người dùng|
stop
@enduml
```

---

## Bảng 2. Task

| Thành phần | Nội dung |
|---|---|
| Case | Theo dõi và cập nhật task |
| Actor chính | Nhân viên được giao task / Quản lý |
| Quyền liên quan | `TASK_VIEW`, `TASK_UPDATE` |
| Đầu vào | Task được chọn, trạng thái mới, file đính kèm |
| Đầu ra | Task đã cập nhật trạng thái, danh sách attachment |

```plantuml
@startuml
title Activity Diagram - Theo dõi và cập nhật task

|Nhân viên|
start
:Đăng nhập hệ thống;
:Mở danh sách task được giao;

|Frontend|
:Gửi request lấy task theo người phụ trách;

|Backend|
:Kiểm tra JWT;
:Kiểm tra quyền TASK_VIEW;
:Trả danh sách task;

|Nhân viên|
:Chọn một task;

|Frontend|
:Gọi API chi tiết task;

|Backend|
:Kiểm tra quyền TASK_VIEW;
:Trả thông tin task và file đính kèm;

|Nhân viên|
if (Cập nhật trạng thái?) then (Có)
  :Chọn trạng thái mới;
  |Frontend|
  :Gửi PATCH /tasks/{id}/status;
  |Backend|
  :Kiểm tra quyền TASK_UPDATE;
  if (Trạng thái hợp lệ?) then (Có)
    :Cập nhật trạng thái task;
    if (Trạng thái DONE?) then (Có)
      :Ghi nhận thời điểm hoàn thành;
    endif
    :Trả task đã cập nhật;
  else (Không)
    :Trả lỗi validate;
  endif
endif

|Nhân viên|
if (Upload file minh chứng?) then (Có)
  :Chọn file;
  |Frontend|
  :Gửi multipart file;
  |Backend|
  :Kiểm tra quyền TASK_UPDATE;
  :Lưu file vào storage;
  :Lưu metadata attachment;
  :Trả danh sách attachment;
endif

|Frontend|
:Cập nhật giao diện chi tiết task;

|Nhân viên|
stop
@enduml
```

---

## Bảng 3. KPI

| Thành phần | Nội dung |
|---|---|
| Case | Theo dõi KPI từ tiến độ task |
| Actor chính | Quản lý / Nhân sự |
| Quyền liên quan | `TASK_VIEW` |
| Đầu vào | Danh sách task, trạng thái task, deadline |
| Đầu ra | Tỷ lệ hoàn thành, task đang mở, task quá hạn, dashboard KPI |

```plantuml
@startuml
title Activity Diagram - Theo dõi KPI công việc

|Quản lý|
start
:Đăng nhập hệ thống;
:Mở Dashboard hoặc màn hình Công việc;

|Frontend|
:Gọi API thống kê task / danh sách task;

|Backend|
:Kiểm tra JWT;
:Kiểm tra quyền TASK_VIEW;

if (Có quyền?) then (Có)
  :Lấy tổng số task;
  :Đếm task hoàn thành;
  :Đếm task đang mở;
  :Đếm task quá hạn theo deadline;
  :Tính tỷ lệ hoàn thành KPI;
  :Trả dữ liệu thống kê;
else (Không)
  :Trả lỗi 403;
  stop
endif

|Frontend|
:Hiển thị KPI trên dashboard;
:Hiển thị biểu đồ hoặc thanh tiến độ;

|Quản lý|
if (Cần lọc theo nhân viên?) then (Có)
  :Chọn người phụ trách hoặc trạng thái;
  |Frontend|
  :Gửi filter mới;
  |Backend|
  :Tính lại KPI theo điều kiện lọc;
  :Trả kết quả;
  |Frontend|
  :Cập nhật KPI;
endif

|Quản lý|
:Đánh giá hiệu suất dựa trên KPI;
stop
@enduml
```

---

## Bảng 4. Lương

| Thành phần | Nội dung |
|---|---|
| Case | Quản lý bảng lương |
| Actor chính | Nhân sự / Kế toán |
| Quyền liên quan | `PAYROLL_VIEW`, `PAYROLL_CREATE`, `PAYROLL_UPDATE`, `PAYROLL_APPROVE`, `PAYROLL_PAY`, `PAYROLL_MANAGE` |
| Đầu vào | Kỳ lương, hợp đồng, chấm công, nghỉ phép, phụ cấp, khấu trừ |
| Đầu ra | Bảng lương, phiếu lương nhân viên, trạng thái kỳ lương |

```plantuml
@startuml
title Activity Diagram - Quản lý lương

|Nhân sự / Kế toán|
start
:Đăng nhập hệ thống;
:Mở màn hình Lương;

|Frontend|
:Gọi API danh sách kỳ lương;

|Backend|
:Kiểm tra JWT;
:Kiểm tra quyền PAYROLL_VIEW hoặc PAYROLL_MANAGE;

if (Có quyền?) then (Có)
  :Trả danh sách kỳ lương;
else (Không)
  :Trả lỗi 403;
  stop
endif

|Nhân sự / Kế toán|
if (Chưa có kỳ lương?) then (Tạo kỳ)
  :Nhập tháng, năm, ngày công chuẩn;
  |Frontend|
  :Gửi yêu cầu tạo kỳ lương;
  |Backend|
  :Kiểm tra quyền PAYROLL_CREATE hoặc PAYROLL_MANAGE;
  :Validate kỳ lương;
  :Lưu PayrollPeriod trạng thái DRAFT;
endif

|Nhân sự / Kế toán|
:Chọn kỳ lương;
:Bấm sinh bảng lương;

|Frontend|
:Gửi yêu cầu generate payroll;

|Backend|
:Kiểm tra quyền PAYROLL_CREATE hoặc PAYROLL_MANAGE;
:Lấy nhân viên đang làm việc;
:Lấy hợp đồng hiệu lực;
:Lấy dữ liệu chấm công, nghỉ phép, tăng ca;
:Tính gross, bảo hiểm, thuế, net;
:Sinh dòng lương cho từng nhân viên;
:Chuyển kỳ lương sang CALCULATED;

|Frontend|
:Hiển thị bảng lương;

|Nhân sự / Kế toán|
if (Cần điều chỉnh?) then (Có)
  :Nhập thưởng, phụ cấp, khấu trừ;
  |Frontend|
  :Gửi yêu cầu điều chỉnh dòng lương;
  |Backend|
  :Kiểm tra quyền PAYROLL_UPDATE hoặc PAYROLL_MANAGE;
  :Cập nhật và tính lại dòng lương;
endif

|Nhân sự / Kế toán|
if (Duyệt lương?) then (Có)
  |Frontend|
  :Gửi yêu cầu approve;
  |Backend|
  :Kiểm tra quyền PAYROLL_APPROVE hoặc PAYROLL_MANAGE;
  :Ghi người duyệt;
  :Chuyển trạng thái APPROVED;
endif

|Nhân sự / Kế toán|
if (Đã chi trả?) then (Có)
  |Frontend|
  :Gửi yêu cầu mark paid;
  |Backend|
  :Kiểm tra quyền PAYROLL_PAY hoặc PAYROLL_MANAGE;
  :Chuyển trạng thái PAID;
endif

|Frontend|
:Tải lại kỳ lương và dòng lương;

|Nhân sự / Kế toán|
stop
@enduml
```

---

## Bảng 5. Hợp đồng

| Thành phần | Nội dung |
|---|---|
| Case | Quản lý hợp đồng lao động |
| Actor chính | Nhân sự |
| Quyền liên quan | `CONTRACT_VIEW`, `CONTRACT_CREATE`, `CONTRACT_UPDATE`, `CONTRACT_DELETE` |
| Đầu vào | Nhân viên, chức vụ, loại hợp đồng, ngày hiệu lực, lương, phụ cấp |
| Đầu ra | Hợp đồng nháp/hiệu lực/chấm dứt, cảnh báo sắp hết hạn |

```plantuml
@startuml
title Activity Diagram - Quản lý hợp đồng lao động

|Nhân sự|
start
:Đăng nhập hệ thống;
:Mở màn hình Hợp đồng;

|Frontend|
:Tải danh sách hợp đồng;
:Tải danh sách nhân viên và chức vụ;

|Backend|
:Kiểm tra JWT;
:Kiểm tra quyền CONTRACT_VIEW;

if (Có quyền?) then (Có)
  :Lọc hợp đồng theo nhân viên, loại, trạng thái;
  :Trả danh sách hợp đồng;
else (Không)
  :Trả lỗi 403;
  stop
endif

|Nhân sự|
if (Hành động?) then (Tạo hợp đồng)
  :Nhập thông tin hợp đồng và lương;
  |Frontend|
  :Gửi yêu cầu tạo hợp đồng;
  |Backend|
  :Kiểm tra quyền CONTRACT_CREATE;
  :Validate dữ liệu;
  :Tạo hợp đồng trạng thái DRAFT;
elseif (Cập nhật)
  :Sửa thông tin hợp đồng;
  |Frontend|
  :Gửi yêu cầu cập nhật;
  |Backend|
  :Kiểm tra quyền CONTRACT_UPDATE;
  :Cập nhật hợp đồng;
elseif (Kích hoạt)
  :Chọn kích hoạt hợp đồng;
  |Frontend|
  :Gửi PATCH /activate;
  |Backend|
  :Kiểm tra quyền CONTRACT_UPDATE;
  :Kiểm tra thời hạn;
  :Chuyển trạng thái ACTIVE;
elseif (Chấm dứt)
  :Nhập ngày và lý do chấm dứt;
  |Frontend|
  :Gửi PATCH /terminate;
  |Backend|
  :Kiểm tra quyền CONTRACT_UPDATE;
  :Chuyển trạng thái TERMINATED;
else (Xóa)
  :Xác nhận xóa;
  |Frontend|
  :Gửi yêu cầu xóa;
  |Backend|
  :Kiểm tra quyền CONTRACT_DELETE;
  :Xóa mềm hợp đồng;
endif

|Frontend|
:Tải lại danh sách hợp đồng;
:Cập nhật cảnh báo hợp đồng sắp hết hạn;

|Nhân sự|
stop
@enduml
```

---

## Bảng 6. Nhân viên

| Thành phần | Nội dung |
|---|---|
| Case | Quản lý hồ sơ nhân viên |
| Actor chính | Nhân sự |
| Quyền liên quan | `EMPLOYEE_VIEW`, `EMPLOYEE_CREATE`, `EMPLOYEE_UPDATE`, `EMPLOYEE_DELETE` |
| Đầu vào | Thông tin cá nhân, phòng ban, chức vụ, trạng thái làm việc, ảnh, tài liệu |
| Đầu ra | Hồ sơ nhân viên, ảnh đại diện, tài liệu nhân viên |

```plantuml
@startuml
title Activity Diagram - Quản lý nhân viên

|Nhân sự|
start
:Đăng nhập hệ thống;
:Mở màn hình Nhân viên;

|Frontend|
:Gọi API danh sách nhân viên;

|Backend|
:Kiểm tra JWT;
:Kiểm tra quyền EMPLOYEE_VIEW;

if (Có quyền?) then (Có)
  :Lọc theo keyword, phòng ban, giới tính, trạng thái;
  :Trả danh sách nhân viên;
else (Không)
  :Trả lỗi 403;
  stop
endif

|Frontend|
:Hiển thị danh sách nhân viên;

|Nhân sự|
if (Hành động?) then (Tạo hồ sơ)
  :Nhập thông tin cá nhân và công việc;
  |Frontend|
  :Gửi yêu cầu tạo nhân viên;
  |Backend|
  :Kiểm tra quyền EMPLOYEE_CREATE;
  :Validate mã nhân viên, email, dữ liệu bắt buộc;
  :Lưu hồ sơ nhân viên;
elseif (Cập nhật hồ sơ)
  :Sửa thông tin nhân viên;
  |Frontend|
  :Gửi yêu cầu cập nhật;
  |Backend|
  :Kiểm tra quyền EMPLOYEE_UPDATE;
  :Cập nhật hồ sơ;
elseif (Upload ảnh / tài liệu)
  :Chọn file upload;
  |Frontend|
  :Gửi multipart file;
  |Backend|
  :Kiểm tra quyền EMPLOYEE_UPDATE;
  :Lưu file vào storage;
  :Lưu metadata tài liệu;
elseif (Xem chi tiết)
  :Chọn nhân viên;
  |Frontend|
  :Gọi API chi tiết nhân viên;
  |Backend|
  :Kiểm tra quyền EMPLOYEE_VIEW;
  :Trả hồ sơ, tài liệu, tài khoản liên kết;
else (Xóa hồ sơ)
  :Xác nhận xóa;
  |Frontend|
  :Gửi yêu cầu xóa;
  |Backend|
  :Kiểm tra quyền EMPLOYEE_DELETE;
  :Xóa mềm nhân viên;
endif

|Frontend|
:Tải lại danh sách hoặc chi tiết nhân viên;

|Nhân sự|
stop
@enduml
```

---

## Bảng 7. Bảo mật

| Thành phần | Nội dung |
|---|---|
| Case | Đăng nhập, JWT và phân quyền |
| Actor chính | Người dùng / Hệ thống |
| Quyền liên quan | Role và Permission của tài khoản |
| Đầu vào | Username, password, JWT token |
| Đầu ra | Token hợp lệ, thông tin người dùng, quyền truy cập hoặc lỗi 401/403 |

```plantuml
@startuml
title Activity Diagram - Bảo mật, đăng nhập và phân quyền

|Người dùng|
start
:Mở trang đăng nhập;
:Nhập username và password;

|Frontend|
:Gửi POST /api/auth/login;

|Backend|
:Validate request;
:Tìm user theo username;

if (User tồn tại và ACTIVE?) then (Có)
  :Kiểm tra mật khẩu;
else (Không)
  :Trả lỗi đăng nhập;
  stop
endif

if (Mật khẩu đúng?) then (Có)
  :Load role và permissions;
  :Sinh JWT;
  :Trả token và thông tin user;
else (Không)
  :Trả lỗi đăng nhập;
  stop
endif

|Frontend|
:Lưu token;
:Hiển thị menu theo permission;

|Người dùng|
:Thực hiện chức năng nghiệp vụ;

|Frontend|
:Gửi request kèm Bearer token;

|Backend|
:JwtAuthenticationFilter đọc token;
:Validate chữ ký và hạn token;

if (Token hợp lệ?) then (Có)
  :Load user, role, permissions;
  :Đưa Authentication vào SecurityContext;
else (Không)
  :Trả 401;
  stop
endif

:PreAuthorize kiểm tra permission của API;

if (Có permission?) then (Có)
  :Cho phép vào controller;
  :Thực thi nghiệp vụ;
  :Trả response;
else (Không)
  :Trả 403;
endif

|Frontend|
if (Response 401?) then (Có)
  :Xóa token;
  :Điều hướng về đăng nhập;
elseif (Response 403?) then (Có)
  :Hiển thị thông báo không đủ quyền;
else (Thành công)
  :Cập nhật giao diện;
endif

|Người dùng|
stop
@enduml
```
