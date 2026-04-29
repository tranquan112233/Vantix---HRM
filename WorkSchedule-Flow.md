# WorkSchedule Flow

## 1. Mục tiêu chức năng

`WorkSchedule` dùng để:

- quản lý lịch làm việc theo từng nhân viên và ngày làm việc
- gán `shift` cho lịch
- gán `work location` nếu cần kiểm tra vị trí chấm công
- hỗ trợ xếp lịch đơn lẻ và xếp lịch hàng loạt
- cung cấp lịch làm việc cho chức năng `Attendance`

Màn hình frontend chính: `Vantix_Web/src/views/WorkSchedules.vue`

API backend chính: `Vantix/src/main/java/poly/edu/vantix/controller/WorkScheduleController.java`

Service xử lý nghiệp vụ: `Vantix/src/main/java/poly/edu/vantix/service/WorkScheduleService.java`

---

## 2. Thành phần chính

Chức năng `WorkSchedule` trong UI gồm 3 tab:

- `Schedules`: quản lý lịch làm việc
- `Shifts`: quản lý ca làm việc
- `Work locations`: quản lý địa điểm làm việc

Trong đó tab `Schedules` là phần lõi của flow `WorkSchedule`.

---

## 3. Phân quyền

### 3.1 Quyền lịch làm việc

- `SCHEDULE_VIEW_ALL`: xem toàn bộ lịch
- `SCHEDULE_CREATE`: tạo lịch
- `SCHEDULE_UPDATE`: sửa lịch
- `SCHEDULE_DELETE`: xóa lịch

### 3.2 Quyền liên quan dữ liệu phụ trợ

- `SHIFT_VIEW`, `SHIFT_CREATE`, `SHIFT_UPDATE`, `SHIFT_DELETE`
- `WORK_LOCATION_VIEW`, `WORK_LOCATION_CREATE`, `WORK_LOCATION_UPDATE`, `WORK_LOCATION_DELETE`

### 3.3 Ảnh hưởng của quyền lên flow

- Nếu có `SCHEDULE_VIEW_ALL`: frontend gọi `GET /api/work-schedules`
- Nếu không có `SCHEDULE_VIEW_ALL`: frontend gọi `GET /api/work-schedules/me`
- Nếu không có quyền tạo/xem all, frontend sẽ không nạp danh sách nhân viên và phòng ban để chọn

---

## 4. Flow tải màn hình

Khi vào màn hình `WorkSchedules`:

1. `onMounted()` chạy.
2. Frontend gọi song song:
   - `loadSchedules()`
   - `loadShifts()`
   - `loadLocations()`
   - `loadScheduleLookups()`
3. `loadScheduleLookups()` tiếp tục nạp:
   - danh sách nhân viên
   - danh sách phòng ban
4. Dữ liệu được bind lên:
   - bảng danh sách lịch
   - lịch dạng calendar
   - form tạo/sửa lịch
   - form bulk schedule

### 4.1 Bộ lọc mặc định

Mặc định frontend dùng khoảng ngày:

- `from = hôm nay`
- `to = hôm nay + 30 ngày`

Khi xem dạng calendar, range được đồng bộ theo 42 ô của tháng đang hiển thị.

---

## 5. Flow xem danh sách lịch

### 5.1 Frontend

Người dùng chọn:

- khoảng ngày
- nhân viên
- phòng ban

Sau đó frontend gọi `loadSchedules()`.

### 5.2 Routing API

#### Trường hợp xem toàn bộ

`GET /api/work-schedules?from&to&employeeId&departmentId`

Điều kiện:

- cần quyền `SCHEDULE_VIEW_ALL`

#### Trường hợp nhân viên tự xem lịch của mình

`GET /api/work-schedules/me?from&to`

Backend tự suy ra `employeeId` từ tài khoản đăng nhập.

### 5.3 Backend xử lý

1. Controller nhận request.
2. Nếu là `/me`, backend gọi `currentEmployee(authentication)` để lấy nhân viên hiện tại.
3. Controller gọi `service.search(from, to, employeeId, departmentId)`.
4. Service gọi `scheduleRepository.search(...)`.
5. Repository query:
   - chỉ lấy record `deleted = false`
   - lọc theo `from`, `to`, `employeeId`, `departmentId` nếu có
   - `join fetch` employee, department, shift, location
   - sắp xếp `workDate DESC`, sau đó `shift.startTime ASC`
6. Service map entity sang `WorkScheduleResponse`.
7. Frontend render danh sách hoặc calendar.

### 5.4 Dữ liệu trả về

`WorkScheduleResponse` gồm:

- thông tin nhân viên
- thông tin phòng ban
- thông tin ca làm
- thông tin địa điểm làm việc
- `workDate`
- `note`

---

## 6. Flow xem lịch hôm nay của chính mình

API:

`GET /api/work-schedules/me/today`

Flow:

1. Backend xác định nhân viên hiện tại từ token.
2. Tìm lịch theo `employeeId + LocalDate.now()`.
3. Nếu có lịch:
   - trả `200 OK` + `WorkScheduleResponse`
4. Nếu không có lịch:
   - trả `204 No Content`

Flow này được dùng gián tiếp bởi phần chấm công để biết hôm nay có lịch hay không.

---

## 7. Flow tạo lịch đơn lẻ

### 7.1 Frontend

Người dùng bấm `Add schedule` hoặc chọn ngày trên calendar.

Frontend mở dialog và nhập:

- `employeeId`
- `shiftId`
- `locationId` (optional)
- `workDate`
- `note`

Khi bấm lưu:

1. validate form
2. gọi `POST /api/work-schedules`

### 7.2 Backend

1. Controller nhận `WorkScheduleRequest`.
2. Service kiểm tra trùng:
   - `existsByEmployeeIdAndWorkDateAndDeletedFalse(employeeId, workDate)`
3. Nếu đã tồn tại:
   - throw `BusinessException("Employee already has a schedule on this date")`
4. Nếu chưa tồn tại:
   - tạo entity `WorkSchedule`
   - gọi `applyRequest(...)`

### 7.3 `applyRequest(...)`

Hàm này chuẩn hóa dữ liệu trước khi lưu:

1. tìm `Employee` active theo `employeeId`
2. tìm `Shift` active theo `shiftId`
3. nếu có `locationId`, tìm `WorkLocation` active
4. gán vào `schedule`:
   - employee
   - shift
   - location
   - workDate
   - note

Nếu employee/shift/location không tồn tại, backend trả `BusinessException`.

### 7.4 Kết quả

1. Service lưu bằng `scheduleRepository.save(schedule)`
2. map sang `WorkScheduleResponse`
3. frontend hiện thông báo thành công
4. frontend gọi lại `loadSchedules()`

---

## 8. Flow sửa lịch

### 8.1 Frontend

1. Người dùng bấm `Edit`
2. Frontend đổ dữ liệu record hiện tại vào dialog
3. Khi lưu, gọi:

`PUT /api/work-schedules/{id}`

### 8.2 Backend

1. Tìm schedule active theo `id`
2. Nếu không có:
   - throw `BusinessException("Schedule does not exist")`
3. Kiểm tra xem `employeeId` hoặc `workDate` có thay đổi không
4. Nếu có thay đổi, kiểm tra trùng lịch như flow create
5. Gọi lại `applyRequest(...)`
6. Lưu và trả về response mới

### 8.3 Ý nghĩa

Backend chỉ kiểm tra trùng khi cặp `employeeId + workDate` thay đổi, tránh báo trùng giả khi chỉ sửa ca làm hoặc ghi chú.

---

## 9. Flow xóa lịch

API:

`DELETE /api/work-schedules/{id}`

Flow:

1. Frontend confirm người dùng
2. Backend tìm `schedule` active theo `id`
3. Nếu không có:
   - throw `BusinessException("Schedule does not exist")`
4. Nếu có:
   - không xóa cứng
   - set `deleted = true`
   - set `deletedAt = LocalDateTime.now()`
5. Trả `204 No Content`
6. Frontend reload danh sách

Đây là `soft delete`.

---

## 10. Flow xếp lịch hàng loạt

API:

`POST /api/work-schedules/bulk`

### 10.1 Input chính

- `employeeIds` hoặc `departmentIds`
- `shiftId`
- `locationId` (optional)
- `fromDate`
- `toDate`
- `daysOfWeek`
- `skipExisting`
- `skipPublicHolidays`
- `note`

### 10.2 Frontend

1. Người dùng mở dialog `Bulk schedule`
2. Chọn mode:
   - theo nhân viên
   - theo phòng ban
3. Chọn ca làm, địa điểm, khoảng ngày, thứ trong tuần
4. Chọn:
   - bỏ qua ngày đã có lịch hay không
   - bỏ qua ngày nghỉ lễ hay không
5. Submit form

Nếu không chọn target nào, frontend chặn ngay và báo lỗi.

### 10.3 Backend xử lý

1. Kiểm tra `fromDate <= toDate`
2. Tìm `Shift` active
3. Nếu có `locationId`, tìm `WorkLocation` active
4. Gom danh sách nhân viên:
   - từ `employeeIds`
   - từ tất cả nhân viên thuộc `departmentIds`
5. Dùng `LinkedHashMap<Long, Employee>` để:
   - loại trùng
   - giữ thứ tự ổn định
6. Nếu không có nhân viên nào:
   - throw `BusinessException("Select at least one employee or department")`
7. Tạo `EnumSet<DayOfWeek>` từ `daysOfWeek`
8. Với từng nhân viên, lặp từ `fromDate` đến `toDate`
9. Với từng ngày:
   - chỉ xử lý nếu ngày đó nằm trong `daysOfWeek`
   - nếu `skipPublicHolidays = true` và là ngày lễ thì bỏ qua
   - kiểm tra đã có lịch chưa
10. Nếu đã có lịch:
   - `skipExisting = true` -> tăng `skipped`
   - `skipExisting = false` -> dừng toàn bộ và throw lỗi
11. Nếu chưa có lịch:
   - tạo `WorkSchedule`
   - add vào `toSave`
   - tăng `created`
12. Sau khi lặp xong:
   - `saveAll(toSave)`
13. Trả về:
   - `created`
   - `skipped`
   - `employeesProcessed`

### 10.4 Lưu ý nghiệp vụ

- Nếu chọn theo phòng ban, hệ thống xếp lịch cho toàn bộ nhân viên active trong phòng ban đó.
- Nếu một nhân viên xuất hiện ở cả `employeeIds` và `departmentIds`, backend tự loại trùng.
- Nếu `skipExisting = false`, chỉ cần gặp một ngày bị trùng là toàn bộ request bị fail.

---

## 11. Flow hiển thị calendar

Calendar chỉ là cách biểu diễn dữ liệu `schedules`, không có API riêng.

Flow:

1. Frontend tính tháng đang xem
2. Sinh grid 42 ô
3. Đồng bộ lại `filters.range` theo grid
4. Gọi `loadSchedules()`
5. Nhóm dữ liệu theo `workDate`
6. Render mỗi ngày:
   - số lượng lịch
   - chip theo ca làm
7. Nếu có quyền tạo, click vào ô ngày sẽ mở form tạo lịch với `workDate` được fill sẵn

---

## 12. Liên hệ với Attendance

`WorkSchedule` là dữ liệu đầu vào bắt buộc của `Attendance`.

### 12.1 Check-in

Khi nhân viên check-in:

1. `AttendanceService.checkIn()` tìm lịch hôm nay bằng:
   - `scheduleRepository.findByEmployeeAndDate(employeeId, today)`
2. Nếu không có lịch:
   - throw `BusinessException("No work schedule for today")`
3. Nếu lịch có `location`:
   - kiểm tra khoảng cách GPS với bán kính cho phép
4. Xác định trạng thái ban đầu:
   - đúng giờ -> `ON_TIME`
   - trễ giờ -> `LATE`

### 12.2 Check-out

Khi check-out:

1. Attendance lấy schedule gắn với lần check-in
2. Nếu schedule có location, tiếp tục validate vị trí
3. So sánh giờ check-in/check-out với ca làm để xác định:
   - `ON_TIME`
   - `LATE`
   - `EARLY_LEAVE`
   - `LATE_AND_EARLY`

### 12.3 Ý nghĩa

Nếu `WorkSchedule` sai hoặc thiếu:

- nhân viên có thể không chấm công được
- trạng thái đúng giờ/trễ/sớm có thể tính sai
- kiểm tra vị trí có thể sai hoặc bị bỏ qua

---

## 13. Luồng tổng quát ngắn gọn

### 13.1 Xem lịch

`UI filter` -> `Controller list/my` -> `Service.search` -> `Repository.search` -> `WorkScheduleResponse` -> `UI table/calendar`

### 13.2 Tạo lịch đơn

`UI dialog` -> `POST /api/work-schedules` -> `validate duplicate` -> `applyRequest` -> `save` -> `reload list`

### 13.3 Sửa lịch

`UI edit` -> `PUT /api/work-schedules/{id}` -> `find active` -> `check duplicate if needed` -> `applyRequest` -> `save`

### 13.4 Xóa lịch

`UI confirm` -> `DELETE /api/work-schedules/{id}` -> `soft delete`

### 13.5 Xếp lịch hàng loạt

`UI bulk dialog` -> `POST /api/work-schedules/bulk` -> `expand employee set` -> `loop date range` -> `skip/check/create` -> `saveAll`

---

## 14. Các rule nghiệp vụ chính

- Một nhân viên chỉ có tối đa 1 lịch trên 1 ngày làm việc.
- Chỉ xử lý schedule `deleted = false`.
- `locationId` là optional.
- Nếu schedule có location, Attendance sẽ kiểm tra GPS theo `radiusMeters`.
- Bulk schedule có thể bỏ qua:
  - ngày đã có lịch
  - ngày nghỉ lễ
- API `/me` và `/me/today` luôn dựa trên nhân viên gắn với user đăng nhập.

---

## 15. Kết luận

`WorkSchedule` là trung tâm của bài toán phân ca và là nguồn dữ liệu nền cho chấm công. Flow chính gồm:

- nạp danh sách theo quyền
- tạo/sửa/xóa lịch đơn
- xếp lịch hàng loạt theo nhân viên hoặc phòng ban
- cung cấp lịch làm việc theo ngày cho Attendance để kiểm tra giờ làm và vị trí
