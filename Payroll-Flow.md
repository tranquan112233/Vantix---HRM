# Payroll Flow

## 1. Mục tiêu chức năng

`Payroll` dùng để quản lý bảng lương theo kỳ, bao gồm:

- tạo và quản lý `payroll period`
- sinh các `payroll row` cho từng nhân viên
- tự động lấy dữ liệu từ `Contract`, `Attendance`, `LeaveRequest`
- tính lương gross/net, bảo hiểm, thuế
- điều chỉnh tay từng dòng lương
- duyệt và khóa kỳ lương

Màn hình frontend chính: `Vantix_Web/src/views/Payrolls.vue`

API backend chính: `Vantix/src/main/java/poly/edu/vantix/controller/PayrollController.java`

Service nghiệp vụ: `Vantix/src/main/java/poly/edu/vantix/service/PayrollService.java`

Engine tính lương: `Vantix/src/main/java/poly/edu/vantix/util/VietnamPayrollCalculator.java`

---

## 2. Thành phần chính

### 2.1 Payroll period

`PayrollPeriod` đại diện cho một kỳ lương theo:

- `year`
- `month`
- `startDate`
- `endDate`
- `standardWorkDays`
- `status`

### 2.2 Payroll row

`Payroll` là dòng lương của một nhân viên trong một kỳ, gồm:

- employee
- contract
- dữ liệu chấm công/nghỉ phép/tăng ca
- phụ cấp, thưởng, khấu trừ
- kết quả gross/net
- trạng thái dòng lương

### 2.3 Trạng thái

Các trạng thái được dùng cho cả period và row:

- `DRAFT`
- `CALCULATED`
- `APPROVED`
- `PAID`
- `CANCELLED`

Trong flow hiện tại, các trạng thái được dùng chính là `DRAFT -> CALCULATED -> APPROVED -> PAID`.

---

## 3. Phân quyền

### 3.1 Quyền period và row

- `PAYROLL_VIEW`
- `PAYROLL_CREATE`
- `PAYROLL_UPDATE`
- `PAYROLL_DELETE`
- `PAYROLL_APPROVE`
- `PAYROLL_PAY`
- `PAYROLL_MANAGE`

### 3.2 Quy tắc frontend

- nút tạo kỳ lương hiển thị khi có `PAYROLL_CREATE` hoặc `PAYROLL_MANAGE`
- nút generate hiển thị khi có `PAYROLL_CREATE` hoặc `PAYROLL_MANAGE`
- nút recalculate/adjust khi có `PAYROLL_UPDATE` hoặc `PAYROLL_MANAGE`
- nút approve khi có `PAYROLL_APPROVE` hoặc `PAYROLL_MANAGE`
- nút mark paid khi có `PAYROLL_PAY` hoặc `PAYROLL_MANAGE`

---

## 4. Flow tải màn hình

Khi vào màn hình `Payrolls`:

1. `onMounted()` chạy.
2. Frontend gọi:
   - `fetchPeriods()`
   - `loadDepartments()`
3. `fetchPeriods()` lấy danh sách kỳ lương theo năm nếu có filter.
4. Nếu có ít nhất một kỳ lương và chưa chọn kỳ nào:
   - tự chọn kỳ đầu tiên
   - gọi `fetchRows()`
5. `fetchRows()` tải các dòng lương của kỳ đang chọn.

UI được chia làm 2 cột:

- bên trái: danh sách `payroll periods`
- bên phải: summary, filter và bảng `payroll rows`

---

## 5. Flow xem danh sách kỳ lương

API:

`GET /api/payrolls/periods?year=...`

Flow:

1. Frontend gửi `year` nếu người dùng có lọc năm.
2. Controller gọi `payrollService.listPeriods(year)`.
3. Service gọi `periodRepository.findAllByYear(year)`.
4. Repository:
   - chỉ lấy `deleted = false`
   - join `approvedBy`
   - sort `year DESC, month DESC`
5. Service map sang `PayrollPeriodResponse`.
6. Frontend render list period bên trái.

---

## 6. Flow tạo kỳ lương

API:

`POST /api/payrolls/periods`

### 6.1 Frontend

Người dùng nhập:

- `year`
- `month`
- `startDate` (optional)
- `endDate` (optional)
- `standardWorkDays` (optional)
- `note`

Khi save:

1. validate `year`, `month`
2. gọi API create period

### 6.2 Backend

1. Kiểm tra đã tồn tại kỳ lương cùng `year + month` chưa
2. Nếu đã có:
   - throw `BusinessException("Payroll period for this month already exists")`
3. Tạo `YearMonth`
4. Nếu request không truyền ngày:
   - `startDate = ngày đầu tháng`
   - `endDate = ngày cuối tháng`
5. Nếu request không truyền `standardWorkDays`:
   - mặc định `26`
6. Set:
   - `status = DRAFT`
7. Save và trả `PayrollPeriodResponse`

---

## 7. Flow sửa kỳ lương

API:

`PUT /api/payrolls/periods/{id}`

Flow:

1. Tìm period active theo `id`
2. Gọi `ensurePeriodEditable(period)`
3. Nếu period là `PAID`:
   - chặn sửa
4. Update các field được phép:
   - `standardWorkDays`
   - `startDate`
   - `endDate`
   - `note`
5. Save

Lưu ý:

- backend không đổi `year` và `month` ở flow update

---

## 8. Flow xóa kỳ lương

API:

`DELETE /api/payrolls/periods/{id}`

Flow:

1. Tìm period active theo `id`
2. Nếu status = `PAID`:
   - chặn xóa
3. Nếu hợp lệ:
   - `deleted = true`
   - `deletedAt = now`
4. Save

Đây là `soft delete`.

---

## 9. Flow xem payroll rows của một kỳ

API:

`GET /api/payrolls?periodId=...&keyword=...&departmentId=...`

### 9.1 Frontend

Khi chọn period hoặc đổi filter:

1. dựng params:
   - `periodId`
   - `keyword`
   - `departmentId`
2. gọi `payrollApi.list(params)`

### 9.2 Backend

1. Controller gọi `payrollService.listPayrolls(periodId, keyword, departmentId)`
2. Service gọi `payrollRepository.findByPeriod(...)`
3. Repository:
   - chỉ lấy `deleted = false`
   - join `employee`, `department`, `position`, `period`, `contract`
   - lọc keyword theo `fullName` hoặc `employeeCode`
   - lọc `departmentId` nếu có
   - sort theo `employeeCode ASC`
4. Map sang `PayrollResponse`

---

## 10. Flow sinh payroll rows tự động

API:

`POST /api/payrolls/periods/{id}/generate`

Đây là flow quan trọng nhất của chức năng payroll.

### 10.1 Điều kiện

- period phải tồn tại
- period không được ở trạng thái `PAID`

### 10.2 Chọn nhân viên để sinh lương

Backend chỉ lấy các nhân viên:

- chưa bị xóa
- có `EmploymentStatus = ACTIVE` hoặc `PROBATION`
- `joinDate` không sau `period.endDate`
- `terminationDate` không trước `period.startDate`

### 10.3 Logic generate

Với từng nhân viên đủ điều kiện:

1. Kiểm tra đã có payroll row trong period chưa
2. Nếu đã có:
   - bỏ qua, không ghi đè
3. Nếu chưa có:
   - tạo `Payroll`
   - gán `period`
   - gán `employee`
   - gọi `applyContractDefaults(...)`
   - gọi `autoFillTimesheet(...)`
   - gọi `recalculate(payroll)`
   - save row

### 10.4 Sau khi generate xong

1. Lấy lại tất cả rows của period
2. Nếu:
   - period có row
   - và tất cả row đều `CALCULATED`
   thì period = `CALCULATED`
3. Ngược lại:
   - period = `DRAFT`

---

## 11. Flow lấy dữ liệu mặc định từ Contract

Hàm:

`applyContractDefaults(payroll, employee, period)`

### 11.1 Tìm contract hiệu lực

Backend gọi:

`contractRepository.findEffectiveContracts(employeeId, period.endDate)`

để lấy hợp đồng `ACTIVE` có hiệu lực tại ngày kết thúc kỳ lương.

### 11.2 Dữ liệu được copy sang payroll

Nếu tìm thấy contract:

- `contract`
- `baseSalary`
- `insuranceSalary` hoặc fallback sang `baseSalary`
- `responsibilityAllowance`
- `mealAllowance`
- `transportAllowance`
- `phoneAllowance`
- `otherAllowance`
- `standardWorkDays` từ contract nếu có

Nếu không có contract:

- payroll row vẫn được tạo
- nhưng các giá trị lương/phụ cấp có thể rỗng hoặc 0 tùy field

---

## 12. Flow tự động lấy dữ liệu chấm công và nghỉ phép

Hàm:

`autoFillTimesheet(payroll, employee, period)`

### 12.1 Tính ngày công thực tế từ Attendance

1. Gọi `attendanceRepository.search(from, to, employeeId)`
2. Đếm các attendance có:
   - status khác `ABSENT`
   - status khác `PENDING`
   - có `checkInAt`
3. Mỗi attendance hợp lệ được tính là `1` ngày công
4. Gán vào `actualWorkDays`

Các trạng thái như:

- `ON_TIME`
- `LATE`
- `EARLY_LEAVE`
- `LATE_AND_EARLY`

đều được tính là một ngày công nếu có check-in.

### 12.2 Tính nghỉ phép có lương và không lương

1. Gọi `leaveRequestRepository.search(...)` với:
   - employeeId
   - status = `APPROVED`
   - trong khoảng `from -> to`
2. Với từng leave:
   - cắt về đúng biên của kỳ lương
   - gọi `businessCalendarService.countWorkingLeaveDays(...)`
3. Phân loại:
   - `UNPAID` -> cộng vào `unpaidLeaveDays`
   - `SICK_OR_MATERNITY` -> cũng cộng vào `unpaidLeaveDays`
   - các loại còn lại -> cộng vào `paidLeaveDays`

Kết quả:

- `paidLeaveDays`
- `unpaidLeaveDays`

---

## 13. Flow tính lương

Hàm:

`recalculate(payroll)`

### 13.1 Tạo input cho calculator

Backend dựng `PayrollInput` từ:

- base salary
- insurance salary
- standard work days
- hours per day
- actual work days
- paid leave days
- overtime hours
- allowances
- bonus
- commission
- other deductions
- dependents
- policy từ `PayrollSettingService`

### 13.2 Gọi engine tính lương

`VietnamPayrollCalculator.calculate(input)`

### 13.3 Kết quả được ghi lại

- `workingDaysSalary`
- `overtimePay`
- `totalAllowance`
- `grossIncome`
- `socialInsurance`
- `healthInsurance`
- `unemploymentInsurance`
- `totalEmployeeInsurance`
- `taxableIncome`
- `personalIncomeTax`
- `netIncome`
- `employerInsurance`
- `totalEmployerCost`

### 13.4 Cập nhật trạng thái row

Nếu row đang:

- `null` hoặc `DRAFT`

thì sau khi tính sẽ thành:

- `CALCULATED`

---

## 14. Công thức tính lương tổng quát

Theo `VietnamPayrollCalculator`, flow tính cơ bản là:

1. Tính lương theo ngày công:
   - `baseSalary * (actualWorkDays + paidLeaveDays) / standardWorkDays`
2. Tính tiền tăng ca:
   - dựa trên hourly rate
   - áp hệ số OT từ payroll policy
3. Tính tổng phụ cấp
4. Tính `grossIncome`
5. Tính BHXH/BHYT/BHTN từ `insuranceSalary`
6. Tính thu nhập chịu thuế
7. Trừ:
   - bảo hiểm người lao động đóng
   - giảm trừ bản thân
   - giảm trừ người phụ thuộc
8. Tính thuế TNCN lũy tiến
9. Tính `netIncome`
10. Tính thêm `employerInsurance` và `totalEmployerCost`

### 14.1 Policy được áp dụng

Calculator không hardcode hoàn toàn theo constant mặc định.

Khi chạy thực tế, payroll lấy policy từ:

- `PayrollSettingService.getPolicy()`

Nghĩa là:

- hệ số tăng ca
- tỷ lệ bảo hiểm
- giảm trừ thuế
- trần đóng bảo hiểm

đều có thể bị chi phối bởi cấu hình payroll settings hiện tại.

---

## 15. Flow tính lại toàn bộ kỳ lương

API:

`POST /api/payrolls/periods/{id}/recalculate`

Flow:

1. Tìm period active
2. Kiểm tra period không phải `PAID`
3. Lấy tất cả payroll rows của kỳ
4. Với từng row:
   - gọi `recalculate(payroll)`
   - save
5. Set period status = `CALCULATED`
6. Save period

Lưu ý quan trọng:

- flow này giữ nguyên các dữ liệu HR đã chỉnh tay trong row
- chỉ tính lại công thức từ dữ liệu hiện có trong row

Nó không tự chạy lại `applyContractDefaults(...)` hay `autoFillTimesheet(...)`.

---

## 16. Flow điều chỉnh tay một payroll row

API:

`PUT /api/payrolls/{id}`

### 16.1 Frontend

Người dùng mở dialog `Adjust payroll` và có thể sửa:

- ngày công
- ngày nghỉ
- giờ tăng ca
- số người phụ thuộc
- bonus
- commission
- khấu trừ khác
- các khoản phụ cấp
- note

### 16.2 Backend

1. Tìm payroll row active
2. Kiểm tra period của row không phải `PAID`
3. Chỉ cập nhật các field request có truyền
4. Gọi `recalculate(payroll)`
5. Save row
6. Trả `PayrollResponse`

### 16.3 Ý nghĩa

Flow này cho phép HR điều chỉnh ngoại lệ mà không cần regenerate cả kỳ.

---

## 17. Flow duyệt kỳ lương

API:

`POST /api/payrolls/periods/{id}/approve`

### 17.1 Điều kiện duyệt

Backend yêu cầu:

- period chưa `PAID`
- period đang ở `CALCULATED`
- period có ít nhất một payroll row
- tất cả payroll rows đều ở trạng thái `CALCULATED`

Nếu không thỏa, backend trả lỗi nghiệp vụ.

### 17.2 Xử lý duyệt

1. Tìm `approver` theo `approverUserId`
2. Set cho period:
   - `status = APPROVED`
   - `approvedBy`
   - `approvedAt = now`
3. Set tất cả payroll rows trong period:
   - `status = APPROVED`
4. Save period

---

## 18. Flow đánh dấu đã chi trả

API:

`POST /api/payrolls/periods/{id}/pay`

### 18.1 Điều kiện

- chỉ period `APPROVED` mới được mark paid

### 18.2 Xử lý

1. Set `now`
2. Set period:
   - `status = PAID`
   - `lockedAt = now`
3. Với mọi payroll rows trong period:
   - `status = PAID`
   - `paidAt = now`
4. Save period

### 18.3 Hậu quả nghiệp vụ

Sau khi `PAID`:

- không thể sửa period
- không thể regenerate
- không thể recalculate
- không thể adjust rows

Vì `ensurePeriodEditable(...)` sẽ chặn.

---

## 19. Flow xem payroll của chính mình

API:

`GET /api/payrolls/me`

Flow:

1. Backend xác định user hiện tại từ token
2. Tìm employee gắn với user
3. Gọi `payrollRepository.findByEmployee(employeeId)`
4. Sort theo:
   - `period.year DESC`
   - `period.month DESC`
5. Trả danh sách payroll rows của chính nhân viên đó

---

## 20. Flow summary và export trên frontend

### 20.1 Summary

Frontend tự tính summary từ `rows` hiện tại:

- số nhân viên
- tổng gross
- tổng bảo hiểm nhân viên đóng
- tổng net
- tổng employer cost

### 20.2 Export

Frontend map `rows` thành:

- danh sách dữ liệu chi tiết
- thêm một dòng tổng cộng

Sau đó đẩy cho component `ExportActions`.

Flow export không có API backend riêng trong màn này.

---

## 21. Luồng tổng quát ngắn gọn

### 21.1 Quản lý kỳ lương

`UI period list` -> `GET/POST/PUT/DELETE /api/payrolls/periods` -> `PayrollPeriodService logic` -> `PayrollPeriodResponse`

### 21.2 Generate bảng lương

`UI generate` -> `POST /api/payrolls/periods/{id}/generate` -> `lọc nhân viên đủ điều kiện` -> `applyContractDefaults` -> `autoFillTimesheet` -> `recalculate` -> `save rows`

### 21.3 Tính lại

`UI recalculate` -> `POST /api/payrolls/periods/{id}/recalculate` -> `recalculate từng row` -> `period = CALCULATED`

### 21.4 Điều chỉnh dòng lương

`UI adjust dialog` -> `PUT /api/payrolls/{id}` -> `update input fields` -> `recalculate row`

### 21.5 Duyệt

`UI approve` -> `POST /api/payrolls/periods/{id}/approve` -> `check all rows calculated` -> `period/rows = APPROVED`

### 21.6 Chi trả

`UI mark paid` -> `POST /api/payrolls/periods/{id}/pay` -> `period/rows = PAID` -> `lock`

---

## 22. Các rule nghiệp vụ chính

- Mỗi tháng chỉ có tối đa 1 payroll period.
- Generate không ghi đè payroll row đã tồn tại.
- Chỉ nhân viên đang làm việc hoặc thử việc trong khoảng kỳ lương mới được generate.
- Dữ liệu mặc định của payroll lấy từ contract có hiệu lực tại `period.endDate`.
- `Attendance` quyết định `actualWorkDays`.
- `LeaveRequest` quyết định `paidLeaveDays` và `unpaidLeaveDays`.
- Recalculate giữ nguyên dữ liệu đã chỉnh tay trong row.
- Period `PAID` là trạng thái khóa, không còn editable.
- Chỉ period `CALCULATED` mới được approve.
- Chỉ period `APPROVED` mới được mark paid.

---

## 23. Liên hệ với các chức năng khác

### 23.1 Contract

Payroll lấy từ contract:

- lương cơ bản
- lương đóng bảo hiểm
- phụ cấp
- ngày công chuẩn
- giờ làm mỗi ngày

### 23.2 Attendance

Payroll dùng attendance để đếm ngày công thực tế.

### 23.3 Leave Request

Payroll dùng leave request đã duyệt để tính:

- nghỉ có lương
- nghỉ không lương

### 23.4 Payroll Settings

Payroll dùng policy từ payroll settings để tính:

- bảo hiểm
- thuế
- hệ số OT
- mức miễn/giảm trừ

---

## 24. Kết luận

`Payroll` là flow tổng hợp dữ liệu từ nhiều module HR để tạo bảng lương theo kỳ. Chuỗi xử lý chính là:

- tạo kỳ lương
- generate dòng lương cho nhân viên đủ điều kiện
- tự lấy dữ liệu từ contract, attendance, leave
- tính gross/net bằng payroll policy hiện hành
- cho phép HR adjust thủ công
- duyệt kỳ lương
- khóa kỳ lương khi đã chi trả
