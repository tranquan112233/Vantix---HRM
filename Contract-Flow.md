# Contract Flow

## 1. Mục tiêu chức năng

`Contract` dùng để quản lý hợp đồng lao động của nhân viên, bao gồm:

- thông tin hợp đồng
- loại hợp đồng
- trạng thái hiệu lực
- lương và phụ cấp
- điều khoản làm việc
- chấm dứt hợp đồng

Ngoài việc lưu hồ sơ hợp đồng, dữ liệu `Contract` còn được dùng làm đầu vào cho `Payroll`.

Màn hình frontend chính: `Vantix_Web/src/views/Contracts.vue`

API backend chính: `Vantix/src/main/java/poly/edu/vantix/controller/ContractController.java`

Service nghiệp vụ: `Vantix/src/main/java/poly/edu/vantix/service/ContractService.java`

---

## 2. Thành phần chính

### 2.1 Entity `Contract`

Một hợp đồng gồm các nhóm dữ liệu:

- thông tin nhận diện: `contractCode`, `employee`, `position`
- thông tin pháp lý: `contractType`, `status`, `signedDate`, `startDate`, `endDate`, `probationMonths`
- thông tin lương: `baseSalary`, `insuranceSalary`, các khoản phụ cấp
- điều khoản làm việc: `standardWorkDays`, `hoursPerDay`, `noticePeriodDays`
- chấm dứt hợp đồng: `terminatedDate`, `terminationReason`
- tài liệu/ghi chú: `attachmentPath`, `note`

### 2.2 Trạng thái hợp đồng

Các trạng thái chính trong UI/backend:

- `DRAFT`
- `ACTIVE`
- `EXPIRED`
- `TERMINATED`
- `LIQUIDATED`

### 2.3 Loại hợp đồng

- `INDEFINITE`
- `FIXED_TERM`
- `PROBATION`
- `SEASONAL`
- `APPRENTICESHIP`
- `PART_TIME`
- `SERVICE`

---

## 3. Phân quyền

- `CONTRACT_VIEW`: xem danh sách, chi tiết, danh sách sắp hết hạn
- `CONTRACT_CREATE`: tạo hợp đồng
- `CONTRACT_UPDATE`: sửa, kích hoạt, chấm dứt
- `CONTRACT_DELETE`: xóa

Frontend chỉ hiển thị các nút hành động tương ứng khi user có quyền.

---

## 4. Flow tải màn hình

Khi vào màn hình `Contracts`:

1. `onMounted()` chạy.
2. Frontend gọi:
   - `fetchData()`
   - `loadLookups()`
3. `fetchData()` lấy danh sách hợp đồng có phân trang.
4. `loadLookups()` nạp:
   - danh sách nhân viên
   - danh sách chức vụ
5. UI render:
   - summary cards
   - filter bar
   - bảng danh sách hợp đồng

---

## 5. Flow xem danh sách hợp đồng

### 5.1 Bộ lọc frontend

Người dùng có thể lọc theo:

- `keyword`
- `employeeId`
- `contractType`
- `status`
- `page`
- `size`

### 5.2 Frontend

`fetchData()`:

1. dựng `params` từ `keyword + filter + pagination`
2. gọi `contractApi.list(params)`
3. dùng `applyPage(...)` để map dữ liệu paged vào:
   - `contracts`
   - `pagination`

### 5.3 Backend

API:

`GET /api/contracts`

Flow:

1. Controller nhận params filter.
2. Nếu có `page` và `size` hợp lệ:
   - gọi `contractService.searchPage(...)`
3. Nếu không:
   - gọi `contractService.search(...)`
4. Repository query:
   - chỉ lấy `deleted = false`
   - join `employee`, `department`, `position`
   - lọc theo keyword, employeeId, contractType, status nếu có
   - sort `startDate DESC, id DESC`

### 5.4 Keyword search

`keyword` match theo:

- `contractCode`
- `employee.fullName`
- `employee.employeeCode`

---

## 6. Flow hiển thị summary

Summary cards trong frontend không có API riêng.

Chúng được tính từ danh sách `contracts` hiện đang tải:

- tổng số hợp đồng
- số hợp đồng `ACTIVE`
- số hợp đồng `DRAFT`
- số hợp đồng sắp hết hạn
- tổng gross payroll của các hợp đồng active

### 6.1 Điều kiện “sắp hết hạn”

Frontend coi là `expiring soon` nếu:

- `status = ACTIVE`
- có `endDate`
- `endDate` cách hôm nay từ `0` đến `30` ngày

---

## 7. Flow xem chi tiết hợp đồng

Hiện tại màn hình danh sách mở detail drawer bằng chính dữ liệu đang có trong row.

Flow:

1. Người dùng bấm icon `View`
2. Frontend gán `selectedContract = row`
3. Mở drawer hiển thị:
   - thông tin cơ bản
   - lương và phụ cấp
   - điều khoản làm việc
   - thông tin chấm dứt nếu có

Backend vẫn có API chi tiết:

`GET /api/contracts/{id}`

nhưng flow hiện tại của `Contracts.vue` không gọi API này khi mở drawer.

---

## 8. Flow tạo hợp đồng

### 8.1 Frontend

Người dùng bấm `Add contract`.

Frontend:

1. reset form về `defaultForm`
2. mở dialog tạo mới
3. validate các field bắt buộc:
   - `contractCode`
   - `employeeId`
   - `contractType`
   - `startDate`
   - `baseSalary`
4. submit:
   - gọi `POST /api/contracts`

### 8.2 Backend

1. Controller nhận `ContractRequest`
2. Service kiểm tra:
   - `contractCode` chưa tồn tại trong record chưa xóa
3. Gọi `validateBusinessRules(request)`
4. Tạo entity `Contract`
5. Gọi `mapRequestToEntity(request, contract)`
6. Nếu request không truyền `status`:
   - mặc định `DRAFT`
7. Nếu status sau khi map là `ACTIVE`:
   - gọi `supersedePreviousActiveContracts(...)`
8. Lưu DB
9. Trả `ContractResponse`

### 8.3 `mapRequestToEntity(...)`

Hàm này:

1. tìm `Employee` theo `employeeId`
2. chặn nếu employee đã bị xóa
3. nếu có `positionId`:
   - dùng position đó
4. nếu không có `positionId`:
   - lấy position hiện tại của employee
5. map toàn bộ:
   - type, status
   - ngày ký, ngày hiệu lực, ngày hết hạn
   - probation
   - lương, bảo hiểm, phụ cấp
   - work terms
   - thông tin terminate
   - attachment/note

### 8.4 Quy tắc lương mặc định

Nếu `insuranceSalary` không được nhập:

- backend tự gán bằng `baseSalary`

Các khoản phụ cấp null sẽ được đưa về `0`.

---

## 9. Rule nghiệp vụ khi tạo/sửa hợp đồng

`validateBusinessRules(...)` kiểm tra:

- `endDate` phải sau `startDate`
- `INDEFINITE` không được có `endDate`
- `FIXED_TERM` bắt buộc có `endDate`
- `FIXED_TERM` không được vượt quá `36` tháng
- `PROBATION` bắt buộc có `endDate`
- `PROBATION` không được vượt quá `180` ngày
- `probationMonths` không được lớn hơn `6`
- `baseSalary` phải lớn hơn `0`

Các rule này áp dụng cả cho create và update.

---

## 10. Flow sửa hợp đồng

### 10.1 Frontend

1. Người dùng bấm `Edit`
2. Frontend đổ dữ liệu từ row vào form
3. `contractCode` bị disable khi edit
4. Khi lưu:
   - gọi `PUT /api/contracts/{id}`

### 10.2 Backend

1. Tìm contract active theo `id`
2. Gọi `ensureNoPayrollRows(contract)`
3. Kiểm tra `contractCode` có bị trùng với contract khác không
4. Validate rule nghiệp vụ
5. Map request vào entity hiện tại
6. Nếu status sau update là `ACTIVE`:
   - supersede các hợp đồng active cũ của cùng employee
7. Save
8. Trả response

### 10.3 Giới hạn quan trọng

Nếu hợp đồng đã phát sinh dữ liệu payroll:

- backend không cho sửa trực tiếp
- lỗi: `Contract already has payroll data and cannot be changed directly`

---

## 11. Flow kích hoạt hợp đồng

API:

`PATCH /api/contracts/{id}/activate`

### 11.1 Frontend

1. Chỉ hiển thị nút activate khi:
   - có quyền `CONTRACT_UPDATE`
   - `row.status === 'DRAFT'`
2. Confirm người dùng
3. Gọi API activate
4. Reload danh sách

### 11.2 Backend

1. Tìm contract active theo `id`
2. Nếu status là `TERMINATED` hoặc `LIQUIDATED`:
   - chặn activate
3. Set status = `ACTIVE`
4. Gọi `supersedePreviousActiveContracts(employeeId, currentId, startDate)`
5. Save

### 11.3 Ý nghĩa `supersedePreviousActiveContracts(...)`

Hàm này đảm bảo mỗi nhân viên chỉ còn một hợp đồng active hiện hành:

1. Tìm tất cả contract `ACTIVE` của nhân viên
2. Bỏ qua contract hiện tại
3. Với mỗi contract active cũ:
   - set status = `EXPIRED`
   - nếu `endDate` đang null hoặc sau `newStartDate`
     thì set `endDate = newStartDate - 1 ngày`
4. Save từng contract cũ

---

## 12. Flow chấm dứt hợp đồng

API:

`PATCH /api/contracts/{id}/terminate`

Request body:

- `terminatedDate`
- `reason`

### 12.1 Frontend

1. Chỉ hiển thị nút terminate khi:
   - có quyền `CONTRACT_UPDATE`
   - `row.status === 'ACTIVE'`
2. Mở dialog
3. Mặc định `terminatedDate = hôm nay`
4. Gọi API terminate
5. Reload danh sách

### 12.2 Backend

1. Tìm contract active theo `id`
2. Nếu contract đã `TERMINATED` hoặc `LIQUIDATED`:
   - chặn thao tác
3. Xác định ngày hiệu lực terminate:
   - nếu không truyền -> dùng `LocalDate.now()`
4. Nếu `terminatedDate < startDate`:
   - báo lỗi
5. Ghi:
   - `status = TERMINATED`
   - `terminatedDate`
   - `terminationReason`
6. Save contract
7. Nếu trước đó contract là `ACTIVE` và ngày terminate không nằm trong tương lai:
   - update employee:
     - `status = TERMINATED`
     - `terminationDate = effective`
8. Trả `ContractResponse`

### 12.3 Tác động sang nhân viên

Terminate hợp đồng active có thể kéo theo terminate trạng thái employment của nhân viên.

Đây là điểm liên kết nghiệp vụ quan trọng giữa `Contract` và `Employee`.

---

## 13. Flow xóa hợp đồng

API:

`DELETE /api/contracts/{id}`

### 13.1 Frontend

1. Confirm người dùng
2. Gọi API delete
3. Reload danh sách

### 13.2 Backend

1. Tìm contract active theo `id`
2. Gọi `ensureNoPayrollRows(contract)`
3. Nếu contract đang `ACTIVE`:
   - chặn xóa
   - yêu cầu terminate trước
4. Nếu hợp lệ:
   - `deleted = true`
   - `deletedAt = LocalDateTime.now()`
5. Save

Đây là `soft delete`.

### 13.3 Các trường hợp không được xóa

- hợp đồng đang active
- hợp đồng đã có bản ghi payroll

---

## 14. Flow lấy hợp đồng theo nhân viên

API:

`GET /api/contracts/employee/{employeeId}`

Flow:

1. Controller nhận `employeeId`
2. Service gọi `contractRepository.findByEmployee(employeeId)`
3. Repository trả danh sách chưa xóa, sort `startDate DESC`
4. Map sang `ContractResponse`

Flow này phù hợp cho màn employee detail hoặc payroll context, dù chưa phải flow chính của `Contracts.vue`.

---

## 15. Flow lấy hợp đồng sắp hết hạn

API:

`GET /api/contracts/expiring?days=30`

Flow:

1. Backend lấy `today`
2. Tìm contract:
   - `deleted = false`
   - `status = ACTIVE`
   - `endDate != null`
   - `endDate between today and today + days`
3. Sort theo `endDate ASC`
4. Trả danh sách

Flow này được dùng tốt cho dashboard/alert.

---

## 16. Liên hệ với Payroll

`Contract` là nguồn dữ liệu trực tiếp cho payroll.

### 16.1 Ràng buộc chỉnh sửa

Nếu contract đã phát sinh payroll rows:

- không cho update
- không cho delete

Mục đích:

- tránh làm sai lịch sử tính lương
- tránh thay đổi dữ liệu nền của các kỳ payroll đã sinh

### 16.2 Dữ liệu payroll có thể dùng từ contract

- `baseSalary`
- `insuranceSalary`
- phụ cấp
- `standardWorkDays`
- `hoursPerDay`
- hợp đồng active có hiệu lực tại một ngày

Repository còn có flow hỗ trợ:

- `findEffectiveContracts(employeeId, date)`

để tìm hợp đồng active có hiệu lực tại thời điểm tính lương.

---

## 17. Luồng tổng quát ngắn gọn

### 17.1 Xem danh sách

`UI filters + pagination` -> `GET /api/contracts` -> `search/searchPage` -> `Repository.search` -> `ContractResponse` -> `UI table`

### 17.2 Tạo hợp đồng

`UI dialog` -> `POST /api/contracts` -> `validate rules` -> `mapRequestToEntity` -> `optional supersede` -> `save`

### 17.3 Sửa hợp đồng

`UI edit` -> `PUT /api/contracts/{id}` -> `ensure no payroll` -> `validate rules` -> `map` -> `optional supersede` -> `save`

### 17.4 Activate

`UI confirm` -> `PATCH /api/contracts/{id}/activate` -> `set ACTIVE` -> `expire old active contracts`

### 17.5 Terminate

`UI dialog` -> `PATCH /api/contracts/{id}/terminate` -> `set TERMINATED` -> `optionally terminate employee`

### 17.6 Delete

`UI confirm` -> `DELETE /api/contracts/{id}` -> `check payroll` -> `check not ACTIVE` -> `soft delete`

---

## 18. Các rule nghiệp vụ chính

- `contractCode` phải unique trong các record chưa xóa.
- Một employee có thể có nhiều hợp đồng lịch sử.
- Khi một hợp đồng mới được set `ACTIVE`, các hợp đồng active cũ của cùng employee sẽ bị chuyển `EXPIRED`.
- `INDEFINITE` không có `endDate`.
- `FIXED_TERM` bắt buộc có `endDate` và tối đa `36` tháng.
- `PROBATION` bắt buộc có `endDate` và tối đa `180` ngày.
- Không cho sửa/xóa hợp đồng đã phát sinh payroll.
- Không cho xóa hợp đồng đang `ACTIVE`.
- Terminate hợp đồng active có thể cập nhật luôn trạng thái nhân viên.

---

## 19. Kết luận

`Contract` là trung tâm của bài toán quản lý hồ sơ lao động và là nguồn dữ liệu nền cho payroll. Flow chính của chức năng gồm:

- xem danh sách có filter và phân trang
- tạo/sửa hợp đồng với validate nghiệp vụ
- kích hoạt để đưa hợp đồng vào hiệu lực
- chấm dứt hợp đồng và cập nhật trạng thái nhân viên
- bảo vệ dữ liệu lịch sử payroll bằng cách khóa sửa/xóa khi đã phát sinh bảng lương
