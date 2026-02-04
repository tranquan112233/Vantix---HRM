-- =====================================================
-- DATABASE: Vantix_HRM
-- Human Resource Management System
-- =====================================================

CREATE DATABASE Vantix_HRM
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE Vantix_HRM;

-- =====================================================
-- TABLE: Roles
-- Công dụng: Lưu vai trò người dùng (Admin, HR, Employee)
-- =====================================================
CREATE TABLE Roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY, -- ID vai trò
    role_name VARCHAR(50) NOT NULL UNIQUE,   -- Tên vai trò
    description VARCHAR(255)                 -- Mô tả vai trò
);

-- =====================================================
-- TABLE: Users
-- Công dụng: Tài khoản đăng nhập hệ thống
-- =====================================================
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY, -- ID tài khoản
    username VARCHAR(50) NOT NULL UNIQUE,    -- Tên đăng nhập
    email VARCHAR(100) UNIQUE,               -- Email đăng nhập / nhận thông báo
    password_hash VARCHAR(255) NOT NULL,     -- Mật khẩu đã mã hóa
    role_id INT NOT NULL,                    -- Vai trò người dùng
    status ENUM('ACTIVE','LOCKED') DEFAULT 'ACTIVE', -- Trạng thái tài khoản
    last_login DATETIME,                     -- Lần đăng nhập gần nhất
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày tạo

    FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);

-- =====================================================
-- TABLE: Departments
-- Công dụng: Quản lý phòng ban
-- =====================================================
CREATE TABLE Departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY, -- ID phòng ban
    department_name VARCHAR(100) NOT NULL,        -- Tên phòng ban
    description VARCHAR(255)                      -- Mô tả chức năng
);

-- =====================================================
-- TABLE: Positions
-- Công dụng: Chức vụ trong công ty
-- =====================================================
CREATE TABLE Positions (
    position_id INT AUTO_INCREMENT PRIMARY KEY, -- ID chức vụ
    position_name VARCHAR(100) NOT NULL          -- Tên chức vụ
);

-- =====================================================
-- TABLE: Employees
-- Công dụng: Hồ sơ nhân viên (bảng trung tâm)
-- =====================================================
CREATE TABLE Employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,  -- ID nhân viên
    user_id INT UNIQUE,                          -- Liên kết tài khoản đăng nhập
    full_name VARCHAR(100) NOT NULL,             -- Họ và tên
    gender ENUM('MALE','FEMALE','OTHER'),        -- Giới tính
    birth_date DATE,                             -- Ngày sinh
    phone VARCHAR(20),                           -- Số điện thoại
    address VARCHAR(255),                        -- Địa chỉ
    department_id INT,                           -- Phòng ban
    position_id INT,                             -- Chức vụ
    work_status ENUM('WORKING','RESIGNED') DEFAULT 'WORKING', -- Trạng thái làm việc

    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (department_id) REFERENCES Departments(department_id),
    FOREIGN KEY (position_id) REFERENCES Positions(position_id)
);

-- =====================================================
-- TABLE: Contracts
-- Công dụng: Lưu lịch sử hợp đồng lao động
-- =====================================================
CREATE TABLE Contracts (
    contract_id INT AUTO_INCREMENT PRIMARY KEY, -- ID hợp đồng
    employee_id INT NOT NULL,                   -- Nhân viên ký hợp đồng
    contract_type ENUM('YEAR_1', 'YEAR_3', 'INDEFINITE'),-- Loại hợp đồng
    start_date DATE NOT NULL,                   -- Ngày bắt đầu
    end_date DATE,                              -- Ngày kết thúc
    position VARCHAR(100),					-- Chức vụ
    base_salary DECIMAL(15,2),                  -- Lương cơ bản theo hợp đồng
    status ENUM('ACTIVE','EXPIRED'),            -- Trạng thái hợp đồng

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: ContractAnnexes
-- Công dụng: Bảng phụ lục hợp đồng
-- =====================================================
CREATE TABLE ContractAnnexes (
    annex_id INT AUTO_INCREMENT PRIMARY KEY,      -- Mã phụ lục
    contract_id INT NOT NULL,                     -- Hợp đồng gốc						
    
    effective_date DATE NOT NULL,                 -- Ngày hiệu lực
    new_salary DECIMAL(18,2) NULL,                -- Lương mới
    new_positions VARCHAR(100) NULL,              -- Chức vụ mới
    
    content TEXT,                                -- Nội dung
    is_active BOOLEAN,               			 -- Kích hoạt
    
    FOREIGN KEY (contract_id) REFERENCES Contracts(contract_id)
);

-- =====================================================
-- TABLE: Salaries
-- Công dụng: Bảng lương theo tháng
-- =====================================================
CREATE TABLE Salaries (
    salary_id INT AUTO_INCREMENT PRIMARY KEY, -- ID bảng lương
    employee_id INT NOT NULL,                 -- Nhân viên nhận lương
    salary_month DATE NOT NULL,               -- Tháng/Năm tính lương

    base_salary DECIMAL(15,2),                -- Lương cơ bản
    actual_shifts int,			      		  -- Ca đã làm
    allowance DECIMAL(18,2),                  -- Phụ cấp
    overtime_pay DECIMAL(18,2),              -- Tăng ca
    bonus DECIMAL(18,2),                      -- Thưởng
    
    bhxh_amount DECIMAL(18,2),				  -- Bảo hiểm xã hội
	bhyt_amount DECIMAL(18,2),				  -- Bảo hiểm y tế
	bhtn_amount DECIMAL(18,2),				  -- Bảo hiểm thất nghiệp
	tax_amount DECIMAL(18,2), 				  -- Thuế TNCN
    
    total_income DECIMAL(18,2),  			  -- Tổng lương tháng
    total_deduction DECIMAL(18,2),            -- Tổng khấu trừ (do đi trẽ & nghỉ không phép)
    total_salary DECIMAL(18,2),               -- Tổng lương thực nhận

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: Shifts
-- Công dụng: Ca làm việc
-- =====================================================
CREATE TABLE Shifts (
    shift_id INT AUTO_INCREMENT PRIMARY KEY, -- ID ca làm
    shift_name VARCHAR(50),                  -- Tên ca
    start_time TIME,                         -- Giờ bắt đầu
    end_time TIME                            -- Giờ kết thúc
);

-- =====================================================
-- TABLE: Attendance
-- Công dụng: Chấm công hàng ngày
-- =====================================================
CREATE TABLE Attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY, -- ID chấm công
    employee_id INT NOT NULL,                     -- Nhân viên
    work_date DATE NOT NULL,                      -- Ngày làm việc
    shift_id INT NOT NULL,                        -- Ca làm
    
    check_in TIME,                                -- Giờ vào
    check_out TIME,                               -- Giờ ra
    late_minutes INT DEFAULT 0,                   -- Phút trễ
    early_leave_minutes INT DEFAULT 0,            -- Phút về sớm
    
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED'),-- Trạng thái phiếu chấm công

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
    FOREIGN KEY (shift_id) REFERENCES Shifts(shift_id)
);

-- =====================================================
-- TABLE: Leave_Types
-- Công dụng: Loại nghỉ phép
-- =====================================================
CREATE TABLE Leave_Types (
    leave_type_id INT AUTO_INCREMENT PRIMARY KEY, -- ID loại nghỉ
    type_name VARCHAR(50),                        -- Tên loại nghỉ
    is_paid BOOLEAN   			                  -- Có hưởng lương hay không
);

-- =====================================================
-- TABLE: Leave_Requests
-- Công dụng: Đơn xin nghỉ phép
-- =====================================================
CREATE TABLE Leave_Requests (
    leave_id INT AUTO_INCREMENT PRIMARY KEY, -- ID đơn nghỉ
    employee_id INT NOT NULL,                -- Nhân viên gửi đơn
    leave_type_id INT NOT NULL,              -- Loại nghỉ
    
    start_date DATE NOT NULL,                -- Ngày bắt đầu nghỉ
    end_date DATE NOT NULL,                  -- Ngày kết thúc nghỉ
	total_shift int NOT NULL,                -- Tổng số ca
    reason VARCHAR(255),                     -- Lý do nghỉ
    
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING', -- Trạng thái
    approved_by INT,                         -- Người duyệt
	
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,-- Thời gian tạo
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
    FOREIGN KEY (leave_type_id) REFERENCES Leave_Types(leave_type_id)
    FOREIGN KEY (approved_by) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: Notifications
-- Công dụng: Thông báo hệ thống
-- =====================================================
CREATE TABLE Notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY, -- ID thông báo
    employee_id INT,                                -- Người nhận
    role_id INT,									-- Quyền nhận
    position_id INT,								-- Chức vụ nhân
    
    title VARCHAR(100),                             -- Tiêu đề
    content TEXT,                                   -- Nội dung
    
    attachment_url VARCHAR(255),                 -- File đính kèm
    sender_id INT,                               -- Người gửi
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,-- Thời gian tạo

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
	FOREIGN KEY (role_id) REFERENCES Roles(role_id),
    FOREIGN KEY (position_id) REFERENCES Positions(position_id)
);
-- =====================================================
-- DATABASE: Vantix_HRM
-- Human Resource Management System
-- =====================================================

CREATE DATABASE Vantix_HRM
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE Vantix_HRM;

-- =====================================================
-- TABLE: Roles
-- Công dụng: Lưu vai trò người dùng (Admin, HR, Employee)
-- =====================================================
CREATE TABLE Roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY, -- ID vai trò
    role_name VARCHAR(50) NOT NULL UNIQUE,   -- Tên vai trò
    description VARCHAR(255)                 -- Mô tả vai trò
);

-- =====================================================
-- TABLE: Users
-- Công dụng: Tài khoản đăng nhập hệ thống
-- =====================================================
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY, -- ID tài khoản
    username VARCHAR(50) NOT NULL UNIQUE,    -- Tên đăng nhập
    email VARCHAR(100) UNIQUE,               -- Email đăng nhập / nhận thông báo
    password_hash VARCHAR(255) NOT NULL,     -- Mật khẩu đã mã hóa
    role_id INT NOT NULL,                    -- Vai trò người dùng
    status ENUM('ACTIVE','LOCKED') DEFAULT 'ACTIVE', -- Trạng thái tài khoản
    last_login DATETIME,                     -- Lần đăng nhập gần nhất
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày tạo

    FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);

-- =====================================================
-- TABLE: Departments
-- Công dụng: Quản lý phòng ban
-- =====================================================
CREATE TABLE Departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY, -- ID phòng ban
    department_name VARCHAR(100) NOT NULL,        -- Tên phòng ban
    description VARCHAR(255)                      -- Mô tả chức năng
);

-- =====================================================
-- TABLE: Positions
-- Công dụng: Chức vụ trong công ty
-- =====================================================
CREATE TABLE Positions (
    position_id INT AUTO_INCREMENT PRIMARY KEY, -- ID chức vụ
    position_name VARCHAR(100) UNIQUE NOT NULL  -- Tên chức vụ
);

-- =====================================================
-- TABLE: Employees
-- Công dụng: Hồ sơ nhân viên (bảng trung tâm)
-- =====================================================
CREATE TABLE Employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,  -- ID nhân viên
    user_id INT UNIQUE,                          -- Liên kết tài khoản đăng nhập
    full_name VARCHAR(100) NOT NULL,             -- Họ và tên
    gender ENUM('MALE','FEMALE','OTHER'),        -- Giới tính
    birth_date DATE,                             -- Ngày sinh
    phone VARCHAR(20),                           -- Số điện thoại
    address VARCHAR(255),                        -- Địa chỉ
    department_id INT,                           -- Phòng ban
    position_id INT,                             -- Chức vụ
    work_status ENUM('WORKING','RESIGNED') DEFAULT 'WORKING', -- Trạng thái làm việc

    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (department_id) REFERENCES Departments(department_id),
    FOREIGN KEY (position_id) REFERENCES Positions(position_id)
);

-- =====================================================
-- TABLE: Contracts
-- Công dụng: Lưu lịch sử hợp đồng lao động
-- =====================================================
CREATE TABLE Contracts (
    contract_id INT AUTO_INCREMENT PRIMARY KEY, -- ID hợp đồng
    employee_id INT NOT NULL,                   -- Nhân viên ký hợp đồng
    contract_type ENUM('YEAR_1', 'YEAR_3', 'INDEFINITE'),-- Loại hợp đồng
    start_date DATE NOT NULL,                   -- Ngày bắt đầu
    end_date DATE,                              -- Ngày kết thúc
    position VARCHAR(100),					-- Chức vụ
    base_salary DECIMAL(15,2),                  -- Lương cơ bản theo hợp đồng
    status ENUM('ACTIVE','EXPIRED'),            -- Trạng thái hợp đồng

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: ContractAnnexes
-- Công dụng: Bảng phụ lục hợp đồng
-- =====================================================
CREATE TABLE ContractAnnexes (
    annex_id INT AUTO_INCREMENT PRIMARY KEY,      -- Mã phụ lục
    contract_id INT NOT NULL,                     -- Hợp đồng gốc						
    
    effective_date DATE NOT NULL,                 -- Ngày hiệu lực
    new_salary DECIMAL(18,2) NULL,                -- Lương mới
    new_positions VARCHAR(100) NULL,              -- Chức vụ mới
    
    content TEXT,                                -- Nội dung
    is_active BOOLEAN,               			 -- Kích hoạt
    
    FOREIGN KEY (contract_id) REFERENCES Contracts(contract_id)
);

-- =====================================================
-- TABLE: Salaries
-- Công dụng: Bảng lương theo tháng
-- =====================================================
CREATE TABLE Salaries (
    salary_id INT AUTO_INCREMENT PRIMARY KEY, -- ID bảng lương
    employee_id INT NOT NULL,                 -- Nhân viên nhận lương
    salary_month DATE NOT NULL,               -- Tháng/Năm tính lương

    base_salary DECIMAL(15,2),                -- Lương cơ bản
    actual_shifts int,			      		  -- Ca đã làm
    allowance DECIMAL(18,2),                  -- Phụ cấp
    overtime_pay DECIMAL(18,2),              -- Tăng ca
    bonus DECIMAL(18,2),                      -- Thưởng
    
    bhxh_amount DECIMAL(18,2),				  -- Bảo hiểm xã hội
	bhyt_amount DECIMAL(18,2),				  -- Bảo hiểm y tế
	bhtn_amount DECIMAL(18,2),				  -- Bảo hiểm thất nghiệp
	tax_amount DECIMAL(18,2), 				  -- Thuế TNCN
    
    total_income DECIMAL(18,2),  			  -- Tổng lương tháng
    total_deduction DECIMAL(18,2),            -- Tổng khấu trừ (do đi trẽ & nghỉ không phép)
    total_salary DECIMAL(18,2),               -- Tổng lương thực nhận

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: Shifts
-- Công dụng: Ca làm việc
-- =====================================================
CREATE TABLE Shifts (
    shift_id INT AUTO_INCREMENT PRIMARY KEY, -- ID ca làm
    shift_name VARCHAR(50),                  -- Tên ca
    start_time TIME,                         -- Giờ bắt đầu
    end_time TIME                            -- Giờ kết thúc
);

-- =====================================================
-- TABLE: Attendance
-- Công dụng: Chấm công hàng ngày
-- =====================================================
CREATE TABLE Attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY, -- ID chấm công
    employee_id INT NOT NULL,                     -- Nhân viên
    work_date DATE NOT NULL,                      -- Ngày làm việc
    shift_id INT NOT NULL,                        -- Ca làm
    
    check_in TIME,                                -- Giờ vào
    check_out TIME,                               -- Giờ ra
    late_minutes INT DEFAULT 0,                   -- Phút trễ
    early_leave_minutes INT DEFAULT 0,            -- Phút về sớm
    
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED'),-- Trạng thái phiếu chấm công

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
    FOREIGN KEY (shift_id) REFERENCES Shifts(shift_id)
);

-- =====================================================
-- TABLE: Leave_Types
-- Công dụng: Loại nghỉ phép
-- =====================================================
CREATE TABLE Leave_Types (
    leave_type_id INT AUTO_INCREMENT PRIMARY KEY, -- ID loại nghỉ
    type_name VARCHAR(50),                        -- Tên loại nghỉ
    is_paid BOOLEAN   			                  -- Có hưởng lương hay không
);

-- =====================================================
-- TABLE: Leave_Requests
-- Công dụng: Đơn xin nghỉ phép
-- =====================================================
CREATE TABLE Leave_Requests (
    leave_id INT AUTO_INCREMENT PRIMARY KEY, -- ID đơn nghỉ
    employee_id INT NOT NULL,                -- Nhân viên gửi đơn
    leave_type_id INT NOT NULL,              -- Loại nghỉ
    
    start_date DATE NOT NULL,                -- Ngày bắt đầu nghỉ
    end_date DATE NOT NULL,                  -- Ngày kết thúc nghỉ
	total_shift int NOT NULL,                -- Tổng số ca
    reason VARCHAR(255),                     -- Lý do nghỉ
    
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING', -- Trạng thái
    approved_by INT,                         -- Người duyệt
	
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,-- Thời gian tạo
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
    FOREIGN KEY (leave_type_id) REFERENCES Leave_Types(leave_type_id),
    FOREIGN KEY (approved_by) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: Notifications
-- Công dụng: Thông báo hệ thống
-- =====================================================
CREATE TABLE Notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY, -- ID thông báo
    employee_id INT,                                -- Người nhận
    role_id INT,									-- Quyền nhận
    position_id INT,								-- Chức vụ nhân
    
    title VARCHAR(100),                             -- Tiêu đề
    content TEXT,                                   -- Nội dung
    
    attachment_url VARCHAR(255),                 -- File đính kèm
    sender_id INT,                               -- Người gửi
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,-- Thời gian tạo

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
	FOREIGN KEY (role_id) REFERENCES Roles(role_id),
    FOREIGN KEY (position_id) REFERENCES Positions(position_id),
    FOREIGN KEY (sender_id) REFERENCES Employees(employee_id)
);
