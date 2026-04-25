# Attendance Flow

Tai lieu nay mo ta luong chay hien tai cua chuc nang `Attendance` trong du an, bam theo code o ca frontend `Vantix_Web` va backend `Vantix`.

## 1. Muc tieu chuc nang

Chuc nang Attendance dung de:

- Hien thi lich lam viec hom nay cua nhan vien.
- Lay vi tri hien tai tu trinh duyet de check-in/check-out.
- Ghi nhan thoi gian, toa do va khoang cach so voi dia diem lam viec.
- Tinh trang thai cham cong: `ON_TIME`, `LATE`, `EARLY_LEAVE`, `LATE_AND_EARLY`.
- Hien thi lich su cham cong ca nhan hoac toan bo nhan vien neu co quyen.

## 2. Thanh phan chinh

### Frontend

- Man hinh: `Vantix_Web/src/views/Attendance.vue`
- API client: `Vantix_Web/src/api/index.js`

### Backend

- Controller: `Vantix/src/main/java/poly/edu/vantix/controller/AttendanceController.java`
- Service: `Vantix/src/main/java/poly/edu/vantix/service/AttendanceService.java`
- Repository: `Vantix/src/main/java/poly/edu/vantix/repository/AttendanceRepository.java`
- DTO request: `Vantix/src/main/java/poly/edu/vantix/dto/request/AttendanceCheckRequest.java`
- DTO response: `Vantix/src/main/java/poly/edu/vantix/dto/response/AttendanceResponse.java`
- Entity: `Vantix/src/main/java/poly/edu/vantix/entity/Attendance.java`

## 3. Luong tai man hinh Attendance

Khi vao trang `Attendance.vue`, ham `onMounted()` chay dong thoi 4 viec:

1. `loadToday()`
   Goi `GET /api/attendance/me/today` de lay ban ghi attendance dang mo hoac ban ghi attendance cua hom nay.

2. `loadTodaySchedule()`
   Goi `GET /api/work-schedules/me/today` de lay lich lam viec hom nay.

3. `loadHistory()`
   Lay lich su cham cong trong khoang ngay mac dinh la 30 ngay gan nhat.

4. `loadEmployees()`
   Chi chay khi user co quyen `ATTENDANCE_VIEW_ALL`, dung de do filter nhan vien.

Sau do frontend goi `obtainPosition()` de xin quyen geolocation tu trinh duyet.

## 4. Luong xem lich su cham cong

### 4.1 Frontend

Frontend doc quyen:

- Neu co `ATTENDANCE_VIEW_ALL`:
  goi `attendanceApi.list(params)` tuong ung `GET /api/attendance`
- Neu khong co quyen:
  goi `attendanceApi.my(params)` tuong ung `GET /api/attendance/me`

Filter hien tai gom:

- `from`
- `to`
- `employeeId` neu co quyen xem toan bo

### 4.2 Backend

`AttendanceController` nhan request va chuyen xuong `AttendanceService.search(from, to, employeeId)`.

`AttendanceService.search()` goi `AttendanceRepository.search(...)`.

Repository dung JPQL de:

- loc theo `from`, `to`, `employeeId`
- bo ban ghi `deleted = true`
- `join fetch` employee, department, schedule, shift, location
- sap xep `workDate DESC, checkInAt DESC`

Ket qua duoc map sang `AttendanceResponse` roi tra ve frontend.

## 5. Luong lay du lieu hom nay

Frontend goi song song:

- `GET /api/attendance/me/today`
- `GET /api/work-schedules/me/today`

Hai API nay phuc vu 2 muc dich khac nhau:

- `work-schedules/me/today`: lay lich lam viec de hien thi ca lam, dia diem, ban kinh cho phep.
- `attendance/me/today`: lay trang thai cham cong hien tai de hien thi gio vao, gio ra, trang thai, va quyet dinh bat/tat nut check-in/check-out.

Trong backend, `AttendanceService.getTodayForUser()` uu tien:

1. tim attendance dang mo de checkout
2. neu khong co thi lay attendance cua dung ngay hom nay

Diem nay quan trong voi ca qua dem: user co the check-in tu hom truoc nhung checkout vao hom sau, he thong van lay dung ban ghi dang mo.

## 6. Luong lay vi tri tren frontend

`Attendance.vue` dung `navigator.geolocation.getCurrentPosition(...)`.

Ket qua thanh cong duoc luu vao:

- `latitude`
- `longitude`
- `accuracy`

Neu trinh duyet khong ho tro hoac user tu choi quyen vi tri:

- frontend set `positionError`
- khi bam check-in/check-out se canh bao can vi tri truoc

Luu y: frontend chi giu vi tri hien tai trong state, chua co buoc map hoac tracking lien tuc. Moi lan can cap nhat vi tri thi user bam `Refresh location` hoac trang tu lay luc mo man hinh.

## 7. Luong check-in

### 7.1 Frontend

Khi user bam `Check in`:

1. kiem tra `position.value`
2. neu chua co vi tri:
   hien canh bao va goi lai `obtainPosition()`
3. neu co vi tri:
   goi `POST /api/attendance/check-in`

Body request:

```json
{
  "latitude": 10.123456,
  "longitude": 106.123456
}
```

Sau khi thanh cong, frontend:

- hien message `checkedIn`
- goi lai `loadToday()`
- goi lai `loadHistory()`

### 7.2 Backend

`AttendanceController.checkIn()` nhan request va goi `AttendanceService.checkIn(userId, request)`.

### 7.3 Xu ly trong AttendanceService.checkIn()

Thu tu xu ly:

1. `resolveEmployee(userId)`
   Tu tai khoan dang nhap, tim `Employee` dang active.

2. Xac dinh `today = LocalDate.now()`.

3. Tim lich lam hom nay:
   `scheduleRepository.findByEmployeeAndDate(employeeId, today)`

4. Neu khong co lich:
   throw `BusinessException("No work schedule for today")`

5. `validateLocation(schedule, request)`
   Neu schedule co `location`, he thong tinh khoang cach thuc te bang `GeoUtils.distanceInMeters(...)`.

6. Neu khoang cach lon hon `radiusMeters` cua dia diem:
   throw `BusinessException("You are ... meters away ...")`

7. Tim attendance theo `(employeeId, today)`.
   Neu chua co thi tao moi ban ghi Attendance.

8. Neu da co `checkInAt`:
   throw `BusinessException("Already checked in for today")`

9. Ghi du lieu check-in:
   - `checkInAt = LocalDateTime.now()`
   - `checkInLat`, `checkInLng`
   - `checkInDistance`
   - `schedule`

10. Tinh trang thai ban dau bang `computeInitialStatus(...)`:
    - sau gio bat dau ca -> `LATE`
    - nguoc lai -> `ON_TIME`

11. Luu DB va tra ve `AttendanceResponse`

## 8. Luong check-out

### 8.1 Frontend

Khi user bam `Check out`:

1. kiem tra da co vi tri chua
2. goi `POST /api/attendance/check-out`
3. neu thanh cong thi reload `today` va `history`

Body request giong check-in:

```json
{
  "latitude": 10.123456,
  "longitude": 106.123456
}
```

### 8.2 Backend

`AttendanceController.checkOut()` goi `AttendanceService.checkOut(userId, request)`.

### 8.3 Xu ly trong AttendanceService.checkOut()

Thu tu xu ly:

1. Resolve employee tu user dang dang nhap.

2. Tim attendance dang mo bang `findOpenAttendanceForCheckout(employeeId, today)`.

3. Neu khong tim thay:
   throw `BusinessException("You have not checked in for the current shift")`

4. Neu chua co `checkInAt`:
   throw `BusinessException("You have not checked in today")`

5. Neu da co `checkOutAt`:
   throw `BusinessException("Already checked out for today")`

6. Neu attendance co gan `schedule`, tiep tuc `validateLocation(...)`

7. Ghi du lieu check-out:
   - `checkOutAt = LocalDateTime.now()`
   - `checkOutLat`, `checkOutLng`
   - `checkOutDistance`

8. Tinh trang thai cuoi bang `computeFinalStatus(attendance, schedule)`

9. Luu DB va tra ve response

## 9. Rule tinh trang thai

### Khi check-in

`computeInitialStatus(checkInTime, shiftStart)`:

- `checkInTime > shiftStart` -> `LATE`
- con lai -> `ON_TIME`

### Khi check-out

`computeFinalStatus(attendance, schedule)`:

1. Ghep `workDate + shift.startTime` thanh `shiftStartAt`
2. Ghep `workDate + shift.endTime` thanh `shiftEndAt`
3. Neu `endTime <= startTime` thi coi la ca qua dem, cong them 1 ngay cho `shiftEndAt`
4. So sanh:
   - `late = checkInAt > shiftStartAt`
   - `early = checkOutAt < shiftEndAt`

Ket qua:

- `late && early` -> `LATE_AND_EARLY`
- chi `late` -> `LATE`
- chi `early` -> `EARLY_LEAVE`
- khong co loi gio -> `ON_TIME`

## 10. Xu ly ca qua dem

Ham `findOpenAttendanceForCheckout(...)` co logic rieng cho ca qua dem:

1. uu tien tim attendance cua hom nay ma chua checkout
2. neu khong co thi tim attendance cua hom qua
3. chi nhan attendance hom qua neu:
   - da check-in
   - chua check-out
   - co schedule + shift
   - `shift.endTime` khong sau `shift.startTime`

Dieu nay cho phep:

- check-in vao toi hom truoc
- check-out vao sang hom sau
- van checkout vao dung ban ghi cu thay vi tao moi

## 11. Validate du lieu dau vao

`AttendanceCheckRequest` bat buoc:

- `latitude` khac null, trong khoang `-90 -> 90`
- `longitude` khac null, trong khoang `-180 -> 180`

Ngoai validate bean, backend con validate nghiep vu:

- user phai gan voi employee active
- ngay hien tai phai co lich lam khi check-in
- vi tri phai nam trong ban kinh cho phep neu schedule co location
- khong duoc check-in 2 lan
- khong duoc check-out khi chua co check-in

## 12. Cau truc du lieu Attendance

Moi ban ghi attendance luu:

- `employee`
- `schedule`
- `workDate`
- thoi gian/toa do/khoang cach luc check-in
- thoi gian/toa do/khoang cach luc check-out
- `status`
- `note`

DB co unique constraint:

- `employee_id + work_date`

Nghia la moi nhan vien chi co toi da 1 ban ghi attendance cho mot ngay lam viec.

## 13. Phan quyen

CAC API lien quan:

- `GET /api/attendance`
  yeu cau `ATTENDANCE_VIEW_ALL`
- `GET /api/attendance/me`
  user thuong dung de xem lich su ca nhan
- `GET /api/attendance/me/today`
- `POST /api/attendance/check-in`
- `POST /api/attendance/check-out`

Frontend cung dua tren quyen `ATTENDANCE_VIEW_ALL` de:

- hien bo loc nhan vien
- chon API list toan bo hay list ca nhan

## 14. Tom tat luong end-to-end

### Mo man hinh

1. Vue mount
2. Goi API lay attendance hom nay
3. Goi API lay lich lam hom nay
4. Goi API lay lich su
5. Xin geolocation

### Check-in

1. User bam check-in
2. Frontend lay toa do hien tai
3. Goi `POST /api/attendance/check-in`
4. Backend resolve employee
5. Tim schedule hom nay
6. Validate vi tri
7. Tao hoac cap nhat attendance cua hom nay
8. Tinh trang thai ban dau
9. Luu DB
10. Frontend reload du lieu

### Check-out

1. User bam check-out
2. Frontend gui toa do hien tai
3. Backend tim attendance dang mo
4. Ho tro ban ghi hom nay hoac hom qua neu la ca qua dem
5. Validate vi tri
6. Ghi gio ra
7. Tinh trang thai cuoi
8. Luu DB
9. Frontend reload du lieu

## 15. Mot so luu y ky thuat

- `loadHistory()` hien dang phan trang phia frontend bang cach slice mang sau khi lay toan bo du lieu tu backend.
- `GET /api/attendance/me/today` co the tra `204 No Content` neu chua co attendance hom nay.
- `GET /api/work-schedules/me/today` cung co the tra `204 No Content` neu hom nay khong co lich lam.
- Neu schedule khong co `location`, backend bo qua kiem tra khoang cach.
- Trang thai `ABSENT` va `PENDING` co ton tai trong he thong du lieu, nhung luong check-in/check-out chu dong chu yeu sinh ra `ON_TIME`, `LATE`, `EARLY_LEAVE`, `LATE_AND_EARLY`.
