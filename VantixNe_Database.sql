-- =====================================================
-- DATABASE: Vantix_HRM
-- Human Resource Management System
-- =====================================================

CREATE DATABASE Vantix_HRMNe
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE Vantix_HRMNe;

-- =====================================================
-- TABLE: Roles
-- =====================================================
CREATE TABLE Roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- =====================================================
-- TABLE: Users
-- =====================================================
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    status ENUM('ACTIVE','LOCKED') DEFAULT 'ACTIVE',
    last_login DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);

-- =====================================================
-- TABLE: Departments
-- =====================================================
CREATE TABLE Departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- =====================================================
-- TABLE: Positions
-- =====================================================
CREATE TABLE Positions (
    position_id INT AUTO_INCREMENT PRIMARY KEY,
    position_name VARCHAR(100) NOT NULL
);

-- =====================================================
-- TABLE: Employees
-- =====================================================
CREATE TABLE Employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    gender ENUM('MALE','FEMALE','OTHER'),
    birth_date DATE,
    phone VARCHAR(20),
    address VARCHAR(255),
    department_id INT,
    position_id INT,
    work_status ENUM('WORKING','RESIGNED') DEFAULT 'WORKING',

    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (department_id) REFERENCES Departments(department_id),
    FOREIGN KEY (position_id) REFERENCES Positions(position_id)
);

-- =====================================================
-- TABLE: Contracts
-- =====================================================
CREATE TABLE Contracts (
    contract_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    contract_type ENUM('YEAR_1', 'YEAR_3', 'INDEFINITE'),
    start_date DATE NOT NULL,
    end_date DATE,
    position VARCHAR(100),
    base_salary DECIMAL(15,2),
    status ENUM('ACTIVE','EXPIRED'),

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: ContractAnnexes
-- =====================================================
CREATE TABLE ContractAnnexes (
    annex_id INT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT NOT NULL,
    
    effective_date DATE NOT NULL,
    new_salary DECIMAL(18,2) NULL,
    new_positions VARCHAR(100) NULL,
    
    content TEXT,
    is_active BOOLEAN,

    FOREIGN KEY (contract_id) REFERENCES Contracts(contract_id)
);

-- =====================================================
-- TABLE: Salaries
-- =====================================================
CREATE TABLE Salaries (
    salary_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    salary_month DATE NOT NULL,

    base_salary DECIMAL(15,2),
    actual_shifts INT,
    allowance DECIMAL(18,2),
    overtime_pay DECIMAL(18,2),
    bonus DECIMAL(18,2),
    
    bhxh_amount DECIMAL(18,2),
    bhyt_amount DECIMAL(18,2),
    bhtn_amount DECIMAL(18,2),
    tax_amount DECIMAL(18,2),
    
    total_income DECIMAL(18,2),
    total_deduction DECIMAL(18,2),
    total_salary DECIMAL(18,2),

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: Shifts
-- =====================================================
CREATE TABLE Shifts (
    shift_id INT AUTO_INCREMENT PRIMARY KEY,
    shift_name VARCHAR(50),
    start_time TIME,
    end_time TIME
);

-- =====================================================
-- TABLE: Attendance
-- =====================================================
CREATE TABLE Attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    work_date DATE NOT NULL,
    shift_id INT NOT NULL,
    
    check_in TIME,
    check_out TIME,
    late_minutes INT DEFAULT 0,
    early_leave_minutes INT DEFAULT 0,
    
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED'),

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
    FOREIGN KEY (shift_id) REFERENCES Shifts(shift_id)
);

-- =====================================================
-- TABLE: Leave_Types
-- =====================================================
CREATE TABLE Leave_Types (
    leave_type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(50),
    is_paid BOOLEAN
);

-- =====================================================
-- TABLE: Leave_Requests
-- =====================================================
CREATE TABLE Leave_Requests (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type_id INT NOT NULL,
    
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_shift INT NOT NULL,
    reason VARCHAR(255),
    
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    approved_by INT,
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
    FOREIGN KEY (leave_type_id) REFERENCES Leave_Types(leave_type_id),
    FOREIGN KEY (approved_by) REFERENCES Employees(employee_id)
);

-- =====================================================
-- TABLE: Notifications
-- =====================================================
CREATE TABLE Notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    role_id INT,
    position_id INT,
    
    title VARCHAR(100),
    content TEXT,
    
    attachment_url VARCHAR(255),
    sender_id INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id),
    FOREIGN KEY (role_id) REFERENCES Roles(role_id),
    FOREIGN KEY (position_id) REFERENCES Positions(position_id)
);

-- =====================================================
-- NEW MODULE: TASK ASSIGNMENT
-- =====================================================

CREATE TABLE Tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    task_title VARCHAR(200) NOT NULL,
    description TEXT,
    
    difficulty_level INT,
    urgency_level INT,

    created_by INT NOT NULL,
    
    start_date DATE,
    due_date DATE,
    
    status ENUM('OPEN','IN_PROGRESS','DONE','CANCELLED') DEFAULT 'OPEN',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (created_by) REFERENCES Employees(employee_id)
);

CREATE TABLE Task_Assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,

    task_id INT NOT NULL,
    employee_id INT NOT NULL,

    completion_percent INT DEFAULT 0,

    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES Tasks(task_id),
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

CREATE TABLE Task_Reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,

    task_id INT NOT NULL,
    employee_id INT NOT NULL,

    report_date DATE NOT NULL,

    work_description TEXT,

    progress_percent INT,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (task_id) REFERENCES Tasks(task_id),
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

CREATE TABLE KPI_Summary (
    kpi_id INT AUTO_INCREMENT PRIMARY KEY,

    employee_id INT NOT NULL,

    kpi_month INT,
    kpi_year INT,

    total_points DECIMAL(10,2),

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- =====================================================
-- DỮ LIỆU MẪU (CŨ)
-- =====================================================

INSERT INTO Roles (role_name, description) VALUES
('ADMIN', 'Quản trị hệ thống'),
('HR', 'Nhân sự'),
('EMPLOYEE', 'Nhân viên');

INSERT INTO Departments (department_name, description) VALUES
('PKT - Phòng kỹ thuật', 'Sửa chữa máy móc thiết bị'),
('HCNS - Hành chính nhân sự', 'Quản lý nhân sự'),
('KT - Kế toán', 'Quản lý tài chính');

INSERT INTO Positions (position_name) VALUES
('Trưởng phòng'),
('Nhân viên kỹ thuật'),
('Nhân viên hành chính'),
('Kế toán'),
('Thực tập sinh');

INSERT INTO Users (username, email, password_hash, role_id) VALUES
('hr01','hr01@vantix.com','hash_admin',1),
('hr02','hr02@vantix.com','hash_hr',2),
('emp01','emp01@vantix.com','hash01',3),
('emp02','emp02@vantix.com','hash02',3),
('emp03','emp03@vantix.com','hash03',3);

INSERT INTO Employees
(user_id, full_name, gender, birth_date, phone, address, department_id, position_id)
VALUES
(1,'Nguyễn Nhân Sự','MALE','1988-01-01','0909000001','Hà Nội',2,1),
(2,'Trần Nhân Sự','FEMALE','1990-02-02','0909000002','Hà Nội',2,1),
(3,'Nguyễn Văn An','MALE','1995-03-03','0909000003','Hà Nội',1,2),
(4,'Trần Thị Bình','FEMALE','1996-04-04','0909000004','HCM',1,2),
(5,'Lê Văn Cường','MALE','1994-05-05','0909000005','HCM',1,1);

-- =====================================================
-- DỮ LIỆU MẪU (TASK SYSTEM)
-- =====================================================

INSERT INTO Tasks
(task_title, description, difficulty_level, urgency_level, created_by, start_date, due_date)
VALUES
('Thiết kế database HRM','Thiết kế bảng hệ thống',5,4,5,'2024-10-01','2024-10-10'),
('Xây dựng API Employee','API quản lý nhân viên',4,3,5,'2024-10-01','2024-10-07');

INSERT INTO Task_Assignments
(task_id, employee_id, completion_percent)
VALUES
(1,3,100),
(2,4,80);

INSERT INTO Task_Reports
(task_id, employee_id, report_date, work_description, progress_percent)
VALUES
(1,3,'2024-10-02','Thiết kế bảng Employees',50),
(1,3,'2024-10-04','Hoàn thành database',100),
(2,4,'2024-10-03','Viết API Employee',40);