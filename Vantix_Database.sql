-- =====================================================
-- DATABASE: Vantix_HRM
-- Human Resource Management System
-- =====================================================

CREATE
    DATABASE Vantix_HRM
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE
    Vantix_HRM;

-- =====================================================
-- INSERT SCRIPT FOR HRM SYSTEM DATABASE
-- =====================================================

-- 1. INSERT PERMISSIONS (Quyền hệ thống)
-- =====================================================
INSERT INTO permissions (name, description, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('USER_CREATE', 'Tạo người dùng mới', NOW(), NOW(), 1, 1, false),
       ('USER_VIEW', 'Xem thông tin người dùng', NOW(), NOW(), 1, 1, false),
       ('USER_UPDATE', 'Cập nhật thông tin người dùng', NOW(), NOW(), 1, 1, false),
       ('USER_DELETE', 'Xóa người dùng', NOW(), NOW(), 1, 1, false),
       ('ROLE_CREATE', 'Tạo vai trò mới', NOW(), NOW(), 1, 1, false),
       ('ROLE_VIEW', 'Xem danh sách vai trò', NOW(), NOW(), 1, 1, false),
       ('ROLE_UPDATE', 'Cập nhật vai trò', NOW(), NOW(), 1, 1, false),
       ('ROLE_DELETE', 'Xóa vai trò', NOW(), NOW(), 1, 1, false),
       ('EMPLOYEE_CREATE', 'Tạo nhân viên mới', NOW(), NOW(), 1, 1, false),
       ('EMPLOYEE_VIEW', 'Xem thông tin nhân viên', NOW(), NOW(), 1, 1, false),
       ('EMPLOYEE_UPDATE', 'Cập nhật thông tin nhân viên', NOW(), NOW(), 1, 1, false),
       ('EMPLOYEE_DELETE', 'Xóa nhân viên', NOW(), NOW(), 1, 1, false),
       ('DEPARTMENT_CREATE', 'Tạo phòng ban mới', NOW(), NOW(), 1, 1, false),
       ('DEPARTMENT_VIEW', 'Xem thông tin phòng ban', NOW(), NOW(), 1, 1, false),
       ('DEPARTMENT_UPDATE', 'Cập nhật phòng ban', NOW(), NOW(), 1, 1, false),
       ('DEPARTMENT_DELETE', 'Xóa phòng ban', NOW(), NOW(), 1, 1, false),
       ('POSITION_CREATE', 'Tạo vị trí mới', NOW(), NOW(), 1, 1, false),
       ('POSITION_VIEW', 'Xem thông tin vị trí', NOW(), NOW(), 1, 1, false),
       ('POSITION_UPDATE', 'Cập nhật vị trí', NOW(), NOW(), 1, 1, false),
       ('POSITION_DELETE', 'Xóa vị trí', NOW(), NOW(), 1, 1, false),
       ('ATTENDANCE_VIEW', 'Xem chấm công', NOW(), NOW(), 1, 1, false),
       ('ATTENDANCE_CREATE', 'Tạo chấm công', NOW(), NOW(), 1, 1, false),
       ('LEAVE_APPROVE', 'Phê duyệt đơn xin nghỉ', NOW(), NOW(), 1, 1, false),
       ('SALARY_VIEW', 'Xem bảng lương', NOW(), NOW(), 1, 1, false),
       ('REPORT_VIEW', 'Xem báo cáo', NOW(), NOW(), 1, 1, false),
       ('SYSTEM_CONFIG', 'Cấu hình hệ thống', NOW(), NOW(), 1, 1, false);

-- 2. INSERT ROLES (Vai trò)
-- =====================================================
INSERT INTO roles (name, description, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('ADMIN', 'Quản trị viên hệ thống - có tất cả quyền', NOW(), NOW(), 1, 1, false),
       ('HR_MANAGER', 'Quản lý nhân sự - quản lý nhân viên, phòng ban, vị trí', NOW(), NOW(), 1, 1, false),
       ('DEPARTMENT_MANAGER', 'Trưởng phòng - quản lý nhân viên trong phòng', NOW(), NOW(), 1, 1, false),
       ('EMPLOYEE', 'Nhân viên - quyền cơ bản', NOW(), NOW(), 1, 1, false);

-- 3. ASSIGN PERMISSIONS TO ROLES (role_permissions)
-- =====================================================
-- ADMIN: Tất cả quyền (lấy tất cả permission ID từ 1-26)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id
FROM permissions
WHERE deleted = false;

-- HR_MANAGER (role_id = 2): Quyền về nhân viên, phòng ban, vị trí, báo cáo
INSERT INTO role_permissions (role_id, permission_id)
VALUES (2, 9),
       (2, 10),
       (2, 11),
       (2, 12), -- EMPLOYEE_*
       (2, 13),
       (2, 14),
       (2, 15),
       (2, 16), -- DEPARTMENT_*
       (2, 17),
       (2, 18),
       (2, 19),
       (2, 20), -- POSITION_*
       (2, 24),
       (2, 25);
-- SALARY_VIEW, REPORT_VIEW

-- DEPARTMENT_MANAGER (role_id = 3): Xem nhân viên, chấm công, phê duyệt nghỉ
INSERT INTO role_permissions (role_id, permission_id)
VALUES (3, 10),
       (3, 11), -- EMPLOYEE_VIEW, EMPLOYEE_UPDATE
       (3, 14), -- DEPARTMENT_VIEW
       (3, 18), -- POSITION_VIEW
       (3, 21),
       (3, 22), -- ATTENDANCE_VIEW, ATTENDANCE_CREATE
       (3, 23);
-- LEAVE_APPROVE

-- EMPLOYEE (role_id = 4): Quyền cơ bản
INSERT INTO role_permissions (role_id, permission_id)
VALUES (4, 10), -- EMPLOYEE_VIEW (chỉ xem thông tin của mình)
       (4, 14),
       (4, 18), -- DEPARTMENT_VIEW, POSITION_VIEW
       (4, 21),
       (4, 22);
-- ATTENDANCE_VIEW, ATTENDANCE_CREATE

-- 4. INSERT DEPARTMENTS (Phòng ban)
-- =====================================================
INSERT INTO departments (name, description, manager_id, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('Ban Giám Đốc', 'Quản lý điều hành công ty', NULL, NOW(), NOW(), 1, 1, false),
       ('Phòng Nhân Sự (HR)', 'Quản lý nhân sự, tuyển dụng, đào tạo', NULL, NOW(), NOW(), 1, 1, false),
       ('Phòng Kế Toán', 'Quản lý tài chính, kế toán, lương thưởng', NULL, NOW(), NOW(), 1, 1, false),
       ('Phòng Kinh Doanh', 'Phát triển thị trường, bán hàng', NULL, NOW(), NOW(), 1, 1, false),
       ('Phòng IT', 'Quản trị hệ thống, phát triển phần mềm', NULL, NOW(), NOW(), 1, 1, false),
       ('Phòng Marketing', 'Quảng bá thương hiệu, tiếp thị sản phẩm', NULL, NOW(), NOW(), 1, 1, false);

-- 5. INSERT POSITIONS (Vị trí công việc)
-- =====================================================
-- Ban Giám Đốc (department_id = 1)
INSERT INTO positions (name, description, department_id, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('Giám Đốc', 'Điều hành toàn bộ công ty', 1, NOW(), NOW(), 1, 1, false),
       ('Phó Giám Đốc', 'Hỗ trợ Giám đốc điều hành', 1, NOW(), NOW(), 1, 1, false);

-- Phòng Nhân Sự (department_id = 2)
INSERT INTO positions (name, description, department_id, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('Trưởng Phòng Nhân Sự', 'Quản lý toàn bộ hoạt động nhân sự', 2, NOW(), NOW(), 1, 1, false),
       ('Chuyên Viên Nhân Sự', 'Thực hiện nghiệp vụ nhân sự', 2, NOW(), NOW(), 1, 1, false),
       ('Nhân Viên Tuyển Dụng', 'Phụ trách tuyển dụng', 2, NOW(), NOW(), 1, 1, false);

-- Phòng Kế Toán (department_id = 3)
INSERT INTO positions (name, description, department_id, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('Trưởng Phòng Kế Toán', 'Quản lý tài chính kế toán', 3, NOW(), NOW(), 1, 1, false),
       ('Kế Toán Trưởng', 'Phụ trách kế toán tổng hợp', 3, NOW(), NOW(), 1, 1, false),
       ('Kế Toán Viên', 'Thực hiện công việc kế toán', 3, NOW(), NOW(), 1, 1, false);

-- Phòng Kinh Doanh (department_id = 4)
INSERT INTO positions (name, description, department_id, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('Trưởng Phòng Kinh Doanh', 'Quản lý hoạt động kinh doanh', 4, NOW(), NOW(), 1, 1, false),
       ('Chuyên Viên Kinh Doanh', 'Phát triển khách hàng', 4, NOW(), NOW(), 1, 1, false),
       ('Nhân Viên Bán Hàng', 'Tư vấn và bán hàng', 4, NOW(), NOW(), 1, 1, false);

-- Phòng IT (department_id = 5)
INSERT INTO positions (name, description, department_id, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('Trưởng Phòng IT', 'Quản lý hệ thống và đội ngũ IT', 5, NOW(), NOW(), 1, 1, false),
       ('Developer', 'Phát triển phần mềm', 5, NOW(), NOW(), 1, 1, false),
       ('System Admin', 'Quản trị hệ thống', 5, NOW(), NOW(), 1, 1, false),
       ('Database Admin', 'Quản trị cơ sở dữ liệu', 5, NOW(), NOW(), 1, 1, false);

-- Phòng Marketing (department_id = 6)
INSERT INTO positions (name, description, department_id, created_at, updated_at, created_by, updated_by, deleted)
VALUES ('Trưởng Phòng Marketing', 'Quản lý hoạt động marketing', 6, NOW(), NOW(), 1, 1, false),
       ('Chuyên Viên Marketing', 'Thực hiện chiến dịch marketing', 6, NOW(), NOW(), 1, 1, false),
       ('Content Creator', 'Tạo nội dung truyền thông', 6, NOW(), NOW(), 1, 1, false);

-- 6. INSERT USERS (Tài khoản người dùng)
-- =====================================================
-- Mật khẩu: 123456 (sẽ được mã hóa BCrypt khi chạy ứng dụng)
-- Lưu ý: Trong thực tế, mật khẩu phải được mã hóa trước khi insert
INSERT INTO users (username, password, email, role_id, last_active, status, created_at, updated_at, created_by,
                   updated_by, deleted)
VALUES ('admin', '$2a$10$YourEncryptedPasswordHere', 'admin@company.com', 1, NOW(), 'ACTIVE', NOW(), NOW(), 1, 1,
        false),
       ('hr_manager', '$2a$10$YourEncryptedPasswordHere', 'hr@company.com', 2, NOW(), 'ACTIVE', NOW(), NOW(), 1, 1,
        false),
       ('director', '$2a$10$YourEncryptedPasswordHere', 'director@company.com', 3, NOW(), 'ACTIVE', NOW(), NOW(), 1, 1,
        false),
       ('it_manager', '$2a$10$YourEncryptedPasswordHere', 'it.manager@company.com', 3, NOW(), 'ACTIVE', NOW(), NOW(), 1,
        1, false),
       ('sales_manager', '$2a$10$YourEncryptedPasswordHere', 'sales.manager@company.com', 3, NOW(), 'ACTIVE', NOW(),
        NOW(), 1, 1, false),
       ('employee1', '$2a$10$YourEncryptedPasswordHere', 'nguyen.van.a@company.com', 4, NOW(), 'ACTIVE', NOW(), NOW(),
        1, 1, false),
       ('employee2', '$2a$10$YourEncryptedPasswordHere', 'tran.thi.b@company.com', 4, NOW(), 'ACTIVE', NOW(), NOW(), 1,
        1, false),
       ('employee3', '$2a$10$YourEncryptedPasswordHere', 'le.van.c@company.com', 4, NOW(), 'ACTIVE', NOW(), NOW(), 1, 1,
        false),
       ('employee4', '$2a$10$YourEncryptedPasswordHere', 'pham.thi.d@company.com', 4, NOW(), 'ACTIVE', NOW(), NOW(), 1,
        1, false),
       ('employee5', '$2a$10$YourEncryptedPasswordHere', 'hoang.van.e@company.com', 4, NOW(), 'ACTIVE', NOW(), NOW(), 1,
        1, false);

-- 7. INSERT EMPLOYEES (Nhân viên)
-- =====================================================
INSERT INTO employees (user_id, full_name, gender, birth_date, phone, address, department_id, position_id, work_status,
                       created_at, updated_at, created_by, updated_by, deleted)
VALUES
-- Admin (user_id = 1) - Thuộc Ban Giám Đốc
(1, 'Nguyễn Văn Admin', 'MALE', '1985-01-15', '0901234567', '123 Đường Lê Lợi, Quận 1, TP.HCM', 1, 1, 'WORKING', NOW(),
 NOW(), 1, 1, false),

-- HR Manager (user_id = 2)
(2, 'Trần Thị Hồng', 'FEMALE', '1990-03-20', '0902345678', '456 Đường Nguyễn Huệ, Quận 1, TP.HCM', 2, 3, 'WORKING',
 NOW(), NOW(), 1, 1, false),

-- Director (user_id = 3)
(3, 'Lê Văn Giám Đốc', 'MALE', '1980-05-10', '0903456789', '789 Đường Võ Văn Tần, Quận 3, TP.HCM', 1, 2, 'WORKING',
 NOW(), NOW(), 1, 1, false),

-- IT Manager (user_id = 4)
(4, 'Phạm Văn IT', 'MALE', '1988-07-25', '0904567890', '321 Đường Cách Mạng Tháng 8, Quận 10, TP.HCM', 5, 7, 'WORKING',
 NOW(), NOW(), 1, 1, false),

-- Sales Manager (user_id = 5)
(5, 'Hoàng Thị Kinh Doanh', 'FEMALE', '1992-09-15', '0905678901', '654 Đường Phạm Ngũ Lão, Quận 1, TP.HCM', 4, 5,
 'WORKING', NOW(), NOW(), 1, 1, false),

-- Employee 1 (user_id = 6) - Developer IT
(6, 'Nguyễn Văn A', 'MALE', '1995-02-28', '0912345678', '12 Đường Nguyễn Trãi, Quận 5, TP.HCM', 5, 8, 'WORKING', NOW(),
 NOW(), 1, 1, false),

-- Employee 2 (user_id = 7) - HR Specialist
(7, 'Trần Thị B', 'FEMALE', '1996-04-12', '0923456789', '34 Đường Lê Văn Sỹ, Quận 3, TP.HCM', 2, 4, 'WORKING', NOW(),
 NOW(), 1, 1, false),

-- Employee 3 (user_id = 8) - Accountant
(8, 'Lê Văn C', 'MALE', '1994-06-18', '0934567890', '56 Đường Xô Viết Nghệ Tĩnh, Quận Bình Thạnh, TP.HCM', 3, 5,
 'WORKING', NOW(), NOW(), 1, 1, false),

-- Employee 4 (user_id = 9) - Sales Staff
(9, 'Phạm Thị D', 'FEMALE', '1997-08-22', '0945678901', '78 Đường Lý Thường Kiệt, Quận Tân Bình, TP.HCM', 4, 6,
 'WORKING', NOW(), NOW(), 1, 1, false),

-- Employee 5 (user_id = 10) - Marketing Specialist
(10, 'Hoàng Văn E', 'MALE', '1998-11-30', '0956789012', '90 Đường Hoàng Văn Thụ, Quận Phú Nhuận, TP.HCM', 6, 11,
 'WORKING', NOW(), NOW(), 1, 1, false);

-- 8. UPDATE DEPARTMENT MANAGER (Cập nhật trưởng phòng)
-- =====================================================
UPDATE departments
SET manager_id = (SELECT id FROM employees WHERE user_id = 2)
WHERE id = 2; -- HR Manager
UPDATE departments
SET manager_id = (SELECT id FROM employees WHERE user_id = 4)
WHERE id = 5; -- IT Manager
UPDATE departments
SET manager_id = (SELECT id FROM employees WHERE user_id = 5)
WHERE id = 4; -- Sales Manager
UPDATE departments
SET manager_id = (SELECT id FROM employees WHERE user_id = 3)
WHERE id = 1;
-- Director

-- 9. INSERT AUTH_TOKENS (Mẫu token - thường được tạo động trong ứng dụng)
-- =====================================================
-- Các token này chỉ là mẫu, thường được tạo khi người dùng yêu cầu
INSERT INTO auth_tokens (user_id, token, type, expiry_date, used, created_at, updated_at, created_by, updated_by,
                         deleted)
VALUES (1, '123456', 'OTP', DATE_ADD(NOW(), INTERVAL 10 MINUTE), false, NOW(), NOW(), 1, 1, false),
       (2, 'reset-token-12345', 'RESET_PASSWORD', DATE_ADD(NOW(), INTERVAL 24 HOUR), false, NOW(), NOW(), 1, 1, false);

INSERT INTO salaries (employee_id, salary_month, base_salary_snapshot, standard_work_days, actual_work_days,
                      allowance, bonus, bhxh_amount, bhyt_amount, bhtn_amount, tax_amount,
                      total_income, total_deduction, net_salary, status, note, created_at, updated_at)
VALUES
-- Nhân viên 2: Đi làm đủ, có phụ cấp và thưởng
(2, '2026-03-01', 15000000.00, 22, 22.00, 1000000.00, 500000.00, 1200000.00, 225000.00, 150000.00, 300000.00,
 16500000.00, 1875000.00, 14625000.00, 'PAID', 'Đã thanh toán qua VCB', NOW(), NOW()),

-- Nhân viên 3: Nghỉ 2 ngày (20/22)
(3, '2026-03-01', 12000000.00, 22, 20.00, 500000.00, 0.00, 960000.00, 180000.00, 120000.00, 0.00, 11409090.00,
 1260000.00, 10149090.00, 'PENDING', 'Nghỉ phép 2 ngày', NOW(), NOW()),

-- Nhân viên 4: Lương cao, trạng thái DRAFT
(4, '2026-03-01', 18000000.00, 22, 22.00, 2000000.00, 1000000.00, 1440000.00, 270000.00, 180000.00, 500000.00,
 21000000.00, 2390000.00, 18610000.00, 'DRAFT', 'Đang chờ rà soát lại thuế TNCN', NOW(), NOW()),

-- Nhân viên 5: Nghỉ 1 ngày (21/22)
(5, '2026-03-01', 10000000.00, 22, 21.00, 500000.00, 0.00, 800000.00, 150000.00, 100000.00, 0.00, 10045454.00,
 1050000.00, 8995454.00, 'APPROVED', '', NOW(), NOW()),

-- Nhân viên 6: Cấp quản lý, lương cao
(6, '2026-03-01', 25000000.00, 22, 22.00, 3000000.00, 0.00, 2000000.00, 375000.00, 250000.00, 1000000.00, 28000000.00,
 3625000.00, 24375000.00, 'PAID', 'Thanh toán qua BIDV', NOW(), NOW()),

-- Nhân viên 7: Đi làm đủ, không có thưởng
(7, '2026-03-01', 14000000.00, 22, 22.00, 1000000.00, 0.00, 1120000.00, 210000.00, 140000.00, 0.00, 15000000.00,
 1470000.00, 13530000.00, 'PAID', '', NOW(), NOW()),

-- Nhân viên 8: Nghỉ ốm 3 ngày (19/22)
(8, '2026-03-01', 16000000.00, 22, 19.00, 1000000.00, 0.00, 1280000.00, 240000.00, 160000.00, 0.00, 14818181.00,
 1680000.00, 13138181.00, 'DRAFT', 'Nghỉ ốm 3 ngày có giấy xin phép', NOW(), NOW()),

-- Nhân viên 9: Thưởng dự án
(9, '2026-03-01', 20000000.00, 22, 22.00, 2000000.00, 1500000.00, 1600000.00, 300000.00, 200000.00, 800000.00,
 23500000.00, 2900000.00, 20600000.00, 'PENDING', 'Thưởng nóng hoàn thành dự án tháng 3', NOW(), NOW());
-- =====================================================
-- END OF SCRIPT
-- =====================================================

-- TEST - HGB
-- 1. TẠO 3 CA LÀM VIỆC (SHIFTS)
INSERT INTO shifts (shift_id, shift_name, start_time, end_time)
VALUES (1, 'Ca Sáng', '07:00:00', '15:00:00'),
       (2, 'Ca Chiều', '13:00:00', '21:00:00'),
       (3, 'Hành Chính', '08:00:00', '18:00:00');

-- 2. TẠO HỢP ĐỒNG CHO 3 NHÂN VIÊN (CONTRACTS)
INSERT INTO contracts (contract_id, employee_id, contract_type, start_date, position, base_salary, status)
VALUES (1, 2, 'INDEFINITE', '2025-01-01', 'Nhân Viên Demo', 10000000.00, 'ACTIVE'),
       (2, 3, 'INDEFINITE', '2025-01-01', 'Trưởng Nhóm Demo', 20000000.00, 'ACTIVE'),
       (3, 4, 'INDEFINITE', '2025-01-01', 'Giám Đốc Demo', 80000000.00, 'ACTIVE');

-- 3. TẠO PHỤ LỤC TĂNG LƯƠNG CHO NV ID 3 TỪ THÁNG 3 (CONTRACT ANNEXES)
INSERT INTO contract_annexes (contract_id, effective_date, new_salary, new_positions, content, is_active)
VALUES (2, '2026-03-01', 25000000.00, 'Trưởng Nhóm Cấp Cao', 'Tăng lương định kỳ tháng 3', 1);

-- 4. TẠO LỊCH LÀM VIỆC THÁNG 3/2026 (MONTHLY SCHEDULES)
INSERT INTO monthly_schedules (monthly_schedule_id, employee_id, month, year, status)
VALUES (1, 2, 3, 2026, 'OPEN'),
       (2, 3, 3, 2026, 'OPEN'),
       (3, 4, 3, 2026, 'OPEN');

-- 5. TẠO DỮ LIỆU LỊCH CHI TIẾT TỪNG NGÀY (DAILY SCHEDULES) - CHỈ TẠO NHỮNG NGÀY TRONG TUẦN (BỎ QUA 5 NGÀY CHỦ NHẬT)
-- (Mình đã tính toán để vứt hết ngày 1, 8, 15, 22, 29)
INSERT INTO daily_work_schedules (monthly_schedule_id, work_date, shift_id, day_type)
VALUES
-- NV 2 (Hành Chính)
(1, '2026-03-02', 3, 'WORK'),
(1, '2026-03-03', 3, 'WORK'),
(1, '2026-03-04', 3, 'WORK'),
(1, '2026-03-05', 3, 'WORK'),
(1, '2026-03-06', 3, 'WORK'),
(1, '2026-03-07', 3, 'WORK'),
(1, '2026-03-09', 3, 'WORK'),
(1, '2026-03-10', 3, 'WORK'),
(1, '2026-03-11', 3, 'WORK'),
(1, '2026-03-12', 3, 'WORK'),
(1, '2026-03-13', 3, 'WORK'),
(1, '2026-03-14', 3, 'WORK'),
(1, '2026-03-16', 3, 'WORK'),
(1, '2026-03-17', 3, 'WORK'),
(1, '2026-03-18', 3, 'WORK'),
(1, '2026-03-19', 3, 'WORK'),
(1, '2026-03-20', 3, 'WORK'),
(1, '2026-03-21', 3, 'WORK'),
(1, '2026-03-23', 3, 'WORK'),
(1, '2026-03-24', 3, 'WORK'),
(1, '2026-03-25', 3, 'WORK'),
(1, '2026-03-26', 3, 'WORK'),
(1, '2026-03-27', 3, 'WORK'),
(1, '2026-03-28', 3, 'WORK'),
(1, '2026-03-30', 3, 'WORK'),
(1, '2026-03-31', 3, 'WORK'),
-- NV 3 (Ca Sáng)
(2, '2026-03-02', 1, 'WORK'),
(2, '2026-03-03', 1, 'WORK'),
(2, '2026-03-04', 1, 'WORK'),
(2, '2026-03-05', 1, 'WORK'),
(2, '2026-03-06', 1, 'WORK'),
(2, '2026-03-07', 1, 'WORK'),
(2, '2026-03-09', 1, 'WORK'),
(2, '2026-03-10', 1, 'WORK'),
(2, '2026-03-11', 1, 'WORK'),
(2, '2026-03-12', 1, 'WORK'),
(2, '2026-03-13', 1, 'WORK'),
(2, '2026-03-14', 1, 'WORK'),
(2, '2026-03-16', 1, 'WORK'),
(2, '2026-03-17', 1, 'WORK'),
(2, '2026-03-18', 1, 'WORK'),
(2, '2026-03-19', 1, 'WORK'),
(2, '2026-03-20', 1, 'WORK'),
(2, '2026-03-21', 1, 'WORK'),
(2, '2026-03-23', 1, 'WORK'),
(2, '2026-03-24', 1, 'WORK'),
(2, '2026-03-25', 1, 'WORK'),
(2, '2026-03-26', 1, 'WORK'),
(2, '2026-03-27', 1, 'WORK'),
(2, '2026-03-28', 1, 'WORK'),
(2, '2026-03-30', 1, 'WORK'),
(2, '2026-03-31', 1, 'WORK'),
-- NV 4 (Ca Chiều)
(3, '2026-03-02', 2, 'WORK'),
(3, '2026-03-03', 2, 'WORK'),
(3, '2026-03-04', 2, 'WORK'),
(3, '2026-03-05', 2, 'WORK'),
(3, '2026-03-06', 2, 'WORK'),
(3, '2026-03-07', 2, 'WORK'),
(3, '2026-03-09', 2, 'WORK'),
(3, '2026-03-10', 2, 'WORK'),
(3, '2026-03-11', 2, 'WORK'),
(3, '2026-03-12', 2, 'WORK'),
(3, '2026-03-13', 2, 'WORK'),
(3, '2026-03-14', 2, 'WORK'),
(3, '2026-03-16', 2, 'WORK'),
(3, '2026-03-17', 2, 'WORK'),
(3, '2026-03-18', 2, 'WORK'),
(3, '2026-03-19', 2, 'WORK'),
(3, '2026-03-20', 2, 'WORK'),
(3, '2026-03-21', 2, 'WORK'),
(3, '2026-03-23', 2, 'WORK'),
(3, '2026-03-24', 2, 'WORK'),
(3, '2026-03-25', 2, 'WORK'),
(3, '2026-03-26', 2, 'WORK'),
(3, '2026-03-27', 2, 'WORK'),
(3, '2026-03-28', 2, 'WORK'),
(3, '2026-03-30', 2, 'WORK'),
(3, '2026-03-31', 2, 'WORK');

-- 6. TẠO DỮ LIỆU CHẤM CÔNG THỰC TẾ (ATTENDANCE)
-- NV 2: Đi làm full 26 ngày (Tròn công)
INSERT INTO attendance (employee_id, work_date, shift_id, check_in, check_out, late_minutes, early_leave_minutes,
                        status)
VALUES (2, '2026-03-02', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-03', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-04', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-05', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-06', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-07', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-09', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-10', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-11', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-12', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-13', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-14', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-16', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-17', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-18', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-19', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-20', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-21', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-23', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-24', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-25', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-26', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-27', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-28', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-30', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED'),
       (2, '2026-03-31', 3, '08:00:00', '18:00:00', 0, 0, 'APPROVED');

-- NV 3: Mất công ngày mùng 2 và 3. Ngày mùng 4 đi trễ 45 phút, Ngày mùng 5 về sớm 15 phút.
INSERT INTO attendance (employee_id, work_date, shift_id, check_in, check_out, late_minutes, early_leave_minutes,
                        status)
VALUES (3, '2026-03-04', 1, '07:45:00', '15:00:00', 45, 0, 'APPROVED'),
       (3, '2026-03-05', 1, '07:00:00', '14:45:00', 0, 15, 'APPROVED'),
       (3, '2026-03-06', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-07', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-09', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-10', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-11', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-12', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-13', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-14', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-16', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-17', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-18', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-19', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-20', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-21', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-23', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-24', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-25', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-26', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-27', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-28', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-30', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED'),
       (3, '2026-03-31', 1, '07:00:00', '15:00:00', 0, 0, 'APPROVED');

-- NV 4: Sếp bự đi làm không trượt phát nào (26 công)
INSERT INTO attendance (employee_id, work_date, shift_id, check_in, check_out, late_minutes, early_leave_minutes,
                        status)
VALUES (4, '2026-03-02', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-03', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-04', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-05', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-06', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-07', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-09', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-10', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-11', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-12', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-13', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-14', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-16', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-17', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-18', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-19', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-20', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-21', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-23', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-24', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-25', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-26', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-27', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-28', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-30', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED'),
       (4, '2026-03-31', 2, '13:00:00', '21:00:00', 0, 0, 'APPROVED');
